package gov.usgs.cida.pubs.busservice;

import gov.usgs.cida.pubs.PubsConstants;
import gov.usgs.cida.pubs.busservice.intfc.ICrossRefBusService;
import gov.usgs.cida.pubs.dao.intfc.ICrossRefLogDao;
import gov.usgs.cida.pubs.domain.CrossRefLog;
import gov.usgs.cida.pubs.domain.Publication;
import gov.usgs.cida.pubs.domain.mp.MpPublication;
import gov.usgs.cida.pubs.transform.CrossrefTransformer;
import gov.usgs.cida.pubs.transform.TransformerFactory;
import gov.usgs.cida.pubs.utility.PubsEMailer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CrossRefBusService implements ICrossRefBusService {

	private static final Logger LOG = LoggerFactory.getLogger(CrossRefBusService.class);

	protected final String crossRefProtocol;
	protected final String crossRefHost;
	protected final String crossRefUrl;
	protected final Integer crossRefPort;
	protected final String crossRefUser;
	protected final String crossRefPwd;
	protected final String crossRefSchemaUrl;
	protected final String displayHost;
	protected final PubsEMailer pubsEMailer;
	protected final TransformerFactory transformerFactory;
	protected final ICrossRefLogDao crossRefLogDao;
	@Autowired
	public CrossRefBusService(
			@Qualifier("crossRefProtocol")
			final String crossRefProtocol,
			@Qualifier("crossRefHost")
			final String crossRefHost,
			@Qualifier("crossRefUrl")
			final String crossRefUrl,
			@Qualifier("crossRefPort")
			final Integer crossRefPort,
			@Qualifier("crossRefUser")
			final String crossRefUser,
			@Qualifier("crossRefPwd")
			final String crossRefPwd,
			@Qualifier("crossRefSchemaUrl")
			final String crossRefSchemaUrl,
			@Qualifier("displayHost")
			final String displayHost,
			final PubsEMailer pubsEMailer,
			final TransformerFactory transformerFactory,
			final ICrossRefLogDao crossRefLogDao
	) {
		//url-related variables:
		this.crossRefProtocol = crossRefProtocol;
		this.crossRefHost = crossRefHost;
		this.crossRefUrl = crossRefUrl;
		this.crossRefPort = crossRefPort;
		this.crossRefUser = crossRefUser;
		this.crossRefPwd = crossRefPwd;
		this.displayHost = displayHost;
		//non-url variables:
		this.pubsEMailer = pubsEMailer;
		this.crossRefSchemaUrl = crossRefSchemaUrl;
		this.transformerFactory = transformerFactory;
		this.crossRefLogDao = crossRefLogDao;
	}

	/**
	 * 
	 * @param pub
	 * @return String XML in Crossref Format
	 * @throws UnsupportedEncodingException
	 * @throws IOException 
	 */
	protected String getCrossRefXml(Publication<?> pub) throws UnsupportedEncodingException, IOException {
		String xml = null;
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			CrossrefTransformer transformer = (CrossrefTransformer) transformerFactory.getTransformer(PubsConstants.MEDIA_TYPE_CROSSREF_EXTENSION, baos, null);
			transformer.write(pub);
			transformer.end();
			
			xml = new String(baos.toByteArray(), PubsConstants.DEFAULT_ENCODING);
			
			//it is important to log the XML, even if it is invalid
			CrossRefLog logEntry = new CrossRefLog(transformer.getBatchId(), pub.getId(), xml);
			crossRefLogDao.add(logEntry);
			
		} catch (UnsupportedEncodingException ex){
			throw ex;
		} catch (IOException ex) {
			String msg = "Error converting pub to Crossref XML before submitting to Crossref webservices.";
			throw new IOException(msg, ex);
		}
		return xml;
	}
	
	/**
	 * Null-safe method to create a message for identifying a publication
	 * @param pub
	 * @return a message
	 */
	protected String getIndexIdMessage(Publication<?> pub){
		String msg = "";
		if (null != pub) {
			String indexId = pub.getIndexId();
			if (null != indexId && 0 != indexId.length()) {
				msg = "Publication Index Id: '" + indexId + "'";
			}
		}
		return msg;
	}
	
	/**
	 * Builds a url for registering crossref content.
	 * https://support.crossref.org/hc/en-us/articles/214960123-Using-HTTPS-to-POST-Files
	 * 
	 * @param protocol usually http or https
	 * @param host machine name. Usually test.crossref.org or doi.crossref.org
	 * @param port usually 80 or 443
	 * @param base the base path of the request
	 * @param user the value of the "login_id" parameter
	 * @param password the value of the "login_passwd" parameter
	 * @return String URL with safely-encoded parameters
	 * @throws java.io.UnsupportedEncodingException
	 * @throws java.net.URISyntaxException
	 */
	protected String buildCrossRefUrl(String protocol, String host, int port, String base, String user, String password) throws UnsupportedEncodingException, URISyntaxException {
		String url = null;
		try {
			String query = "?operation=doMDUpload&login_id=" +
			URLEncoder.encode(user, PubsConstants.URL_ENCODING) +
			"&login_passwd=" +
			URLEncoder.encode(password, PubsConstants.URL_ENCODING) +
			"&area=live";
			URI uri = new URI(protocol, null, host, port, base, null, null);
			url = uri.toString() + query;
		}catch (URISyntaxException ex) {
			/**
			 * we omit the original exception because the URI could
			 * contain passwords that we do not want logged or 
			 * emailed.
			 */
			throw new URISyntaxException(String.join(",", protocol, host, ""+port, base, user, "password omitted"), "Could not construct Crossref submission url");
		} catch (UnsupportedEncodingException ex) {
			throw ex;
		}
		return url;
	}
	
	@Override
	public void submitCrossRef(final MpPublication mpPublication) {
		String publicationIndexIdMessage = ""; 
		try (CloseableHttpClient httpClient = HttpClients.createDefault()){
			publicationIndexIdMessage = getIndexIdMessage(mpPublication);
			submitCrossRef(mpPublication, httpClient);
			LOG.info("Publication successfully published. " + publicationIndexIdMessage);
		} catch (Exception ex) {
			/**
			 * There's a lot of I/O going on here, and this isn't a
			 * crucial process, so we use an intentionally broad 
			 * catch to prevent interruption of control flow in 
			 * callers
			 */
			String errorId = UUID.randomUUID().toString();
			String subject = "Error submitting publication to Crossref";
			String logMessage = subject + ". Error ID#:" + errorId + ". "+ publicationIndexIdMessage;
			LOG.error(logMessage, ex);
			String emailMessage = subject + ".\n" + 
				"Error Message: " + ex.getMessage() + "\n" +
				publicationIndexIdMessage + "\n" +
				"More information is available in the server logs\n." +
				"Host: " + displayHost + ".\n" +
				"Error ID#: " + errorId + ".\n";

			pubsEMailer.sendMail(subject, emailMessage);
		}
	}
	
	/**
	 * @param mpPublication
	 * @param httpClient
	 * @throws UnsupportedEncodingException
	 * @throws IOException 
	 * @throws org.apache.http.HttpException 
	 * @throws java.net.URISyntaxException 
	 */
	protected void submitCrossRef(final MpPublication mpPublication, CloseableHttpClient httpClient) throws UnsupportedEncodingException, IOException, HttpException, URISyntaxException {
		String crossRefXml = getCrossRefXml(mpPublication);
		String url = buildCrossRefUrl(crossRefProtocol, crossRefHost, crossRefPort, crossRefUrl, crossRefUser, crossRefPwd);
		HttpPost httpPost = buildCrossRefPost(crossRefXml, url, mpPublication.getIndexId());
		HttpResponse response = performCrossRefPost(httpPost, httpClient);
		handleResponse(response);
	}
	
	/**
	 * 
	 * @param httpPost
	 * @param httpClient
	 * @return the response from Crossref web services
	 * @throws IOException 
	 */
	protected HttpResponse performCrossRefPost(HttpPost httpPost, CloseableHttpClient httpClient) throws IOException {
		LOG.debug("Posting to " + crossRefProtocol + "://" + crossRefHost + ":" + crossRefPort);
		try {
			HttpHost httpHost = new HttpHost(crossRefHost, crossRefPort, crossRefProtocol);
			HttpResponse response = httpClient.execute(httpHost, httpPost, new BasicHttpContext());
			return response;
		} catch (IOException ex) {
			throw new IOException("Unexpected network error when POSTing to Crossref", ex);
		}
	}
	
	/**
	 * 
	 * @param crossRefXml
	 * @param url
	 * @param indexId the index ID of the publication
	 * @return an HttpPost that is ready to send to Crossref web services
	 * @throws IOException 
	 */
	protected HttpPost buildCrossRefPost(String crossRefXml, String url, String indexId) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		
		File crossRefTempFile = writeCrossRefToTempFile(crossRefXml);
		ContentType contentType = ContentType.create(PubsConstants.MEDIA_TYPE_CROSSREF_VALUE, PubsConstants.DEFAULT_ENCODING);
		
		//The filename is displayed in Crossref's dashboard, so put some
		//useful info in it
		String filename = indexId + "." + PubsConstants.MEDIA_TYPE_CROSSREF_EXTENSION;
		
		FileBody fileBody = new FileBody(crossRefTempFile, contentType, filename);
		HttpEntity httpEntity = MultipartEntityBuilder.create()
			.addPart("fname", fileBody)
			.build();
		httpPost.setEntity(httpEntity);
		return httpPost;
	}
	
	/**
	 * 
	 * @param crossRefXML
	 * @return temp file containing the specified XML
	 * @throws IOException 
	 */
	protected File writeCrossRefToTempFile(String crossRefXML) throws IOException {
		try {
			File crossRefTempFile = File.createTempFile("crossref", "xml");
			FileUtils.writeStringToFile(crossRefTempFile, crossRefXML);
			return crossRefTempFile;
		} catch (IOException ex) {
			throw new IOException("Error writing crossref XML to temp file", ex);
		}
	}
	
	/**
	 * Check the response from Crossref web services, throw a 
	 * RuntimeException if anything is wrong.
	 * @param response 
	 * @throws HttpException when the Crossref web services return an error
	 */
	protected void handleResponse(HttpResponse response) throws HttpException {
		String msg = null;
		if (null == response) {
			msg = "Response was null.";
		} else if (null == response.getStatusLine()) {
			msg = "Response status line was null.";
		} else if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
			msg = response.getStatusLine().toString();
		}
		if(null != msg) {
			throw new HttpException("Error in response from Crossref Submission: " + msg);
		}
	}
}
