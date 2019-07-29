package gov.usgs.cida.pubs.busservice.ipds;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.xml.sax.SAXException;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;

import gov.usgs.cida.pubs.ConfigurationService;
import gov.usgs.cida.pubs.busservice.CostCenterBusService;
import gov.usgs.cida.pubs.busservice.OutsideAffiliationBusService;
import gov.usgs.cida.pubs.busservice.PersonContributorBusService;
import gov.usgs.cida.pubs.dao.LinkTypeDao;
import gov.usgs.cida.pubs.dao.PublicationSeriesDao;
import gov.usgs.cida.pubs.dao.PublishingServiceCenterDao;
import gov.usgs.cida.pubs.dao.ipds.IpdsMessageLogDaoIT;
import gov.usgs.cida.pubs.dao.ipds.IpdsPubTypeConvDao;
import gov.usgs.cida.pubs.domain.LinkType;
import gov.usgs.cida.pubs.domain.ProcessType;
import gov.usgs.cida.pubs.domain.PublicationLink;
import gov.usgs.cida.pubs.domain.PublicationSeries;
import gov.usgs.cida.pubs.domain.PublicationSubtype;
import gov.usgs.cida.pubs.domain.PublishingServiceCenter;
import gov.usgs.cida.pubs.domain.ipds.IpdsMessageLog;
import gov.usgs.cida.pubs.domain.ipds.IpdsPubTypeConv;
import gov.usgs.cida.pubs.domain.ipds.PublicationMap;
import gov.usgs.cida.pubs.domain.mp.MpPublication;
import gov.usgs.cida.pubs.domain.mp.MpPublicationContributor;
import gov.usgs.cida.pubs.springinit.DbTestConfig;
import gov.usgs.cida.pubs.springinit.TestSpringConfig;
import gov.usgs.cida.pubs.utility.PubsEMailer;

@SpringBootTest(webEnvironment=WebEnvironment.NONE,
	classes={DbTestConfig.class, TestSpringConfig.class, LocalValidatorFactoryBean.class,
			CostCenterBusService.class, IpdsUsgsContributorService.class,
			IpdsParserService.class, IpdsWsRequester.class, ConfigurationService.class,
			PublicationSeries.class, PublicationSeriesDao.class, PubsEMailer.class,
			IpdsCostCenterService.class, IpdsPubContributorService.class, IpdsBinding.class,
			PersonContributorBusService.class, IpdsOutsideContributorService.class,
			OutsideAffiliationBusService.class, LinkType.class, LinkTypeDao.class,
			IpdsPubTypeConv.class, IpdsPubTypeConvDao.class, PublishingServiceCenter.class,
			PublishingServiceCenterDao.class})
@DatabaseSetups({
	@DatabaseSetup("classpath:/testCleanup/clearAll.xml"),
	@DatabaseSetup("classpath:/testData/publicationType.xml"),
	@DatabaseSetup("classpath:/testData/publicationSubtype.xml"),
	@DatabaseSetup("classpath:/testData/publicationSeries.xml"),
	@DatabaseSetup("classpath:/testData/ipdsPubsTypeConv.xml")
})
public class IpdsBindingIT extends BaseIpdsTest {

	@Autowired
	public String contributorsXml;

	@Autowired
	public String notesXml;

	@Autowired
	private IpdsCostCenterService ipdsCostCenterService;

	@Autowired
	private IpdsPubContributorService ipdsContributorService;

	private IpdsBinding binding;

	@Before
	public void setUp() throws Exception {
		binding = new IpdsBinding(ipdsParser, ipdsCostCenterService, ipdsContributorService);
	}

	@Test
	public void bindNotesTest() throws SAXException, IOException {
		PublicationMap map = binding.bindNotes(null, null);
		assertEquals(0, map.getFields().size());

		map = binding.bindNotes("", null);
		assertEquals(0, map.getFields().size());

		map = binding.bindNotes(notesXml, null);
		assertEquals(0, map.getFields().size());

		Set<String> tags = new HashSet<>();
		map = binding.bindNotes(notesXml, tags);
		assertEquals(0, map.getFields().size());

		tags.add("d:NoteComment");
		map = binding.bindNotes(notesXml, tags);
		assertEquals(1, map.getFields().size());
		assertEquals("d:NoteComment", map.getFields().get(0));
		assertEquals("M10-0272|", map.get("d:NoteComment"));

		tags.clear();
		tags.add("com");
		map = binding.bindNotes("<root><com>hi</com><com>dave</com><com/></root>", tags);
		assertEquals(1, map.getFields().size());
		assertEquals("com", map.getFields().get(0));
		assertEquals("hi|dave||", map.get("com"));

		tags.clear();
		tags.add("com");
		map = binding.bindNotes("<root><com/></root>", tags);
		assertEquals(0, map.getFields().size());

		tags.clear();
		tags.add("dog");
		map = binding.bindNotes("<root><com/></root>", tags);
		assertEquals(0, map.getFields().size());
	}

	@Test
	public void bindPublishedURLTest() {
		Map<String, Object> pubMap = new HashMap<>();
		assertNull(binding.bindPublishedURL(null));
		assertNull(binding.bindPublishedURL(pubMap));
		pubMap.put(IpdsMessageLog.PUBLISHEDURL, null);
		assertNull(binding.bindPublishedURL(pubMap));
		pubMap.put(IpdsMessageLog.PUBLISHEDURL, "");
		assertNull(binding.bindPublishedURL(pubMap));
		pubMap.put(IpdsMessageLog.PUBLISHEDURL, ",yada ,yada, yada");
		assertNull(binding.bindPublishedURL(pubMap));
		pubMap.put(IpdsMessageLog.PUBLISHEDURL, "http://dave.com/this/url, Howdy!");
		Collection<PublicationLink<?>> links = binding.bindPublishedURL(pubMap);
		assertNotNull(links);
		assertEquals(1, links.size());
		PublicationLink<?> link = (PublicationLink<?>) links.toArray()[0];
		assertEquals("http://dave.com/this/url", link.getUrl());
		assertEquals(LinkType.INDEX_PAGE, link.getLinkType().getId());
	}

	@Test
	public void fixRanksTest() {
		List<MpPublicationContributor> contributors = new ArrayList<>();
		MpPublicationContributor contributorA = new MpPublicationContributor();
		contributorA.setId(1);
		contributorA.setRank(1);
		contributors.add(contributorA);
		MpPublicationContributor contributorB = new MpPublicationContributor();
		contributorB.setId(2);
		contributorB.setRank(2);
		contributors.add(contributorB);
		Collection<MpPublicationContributor> fixed = binding.fixRanks(contributors);
		assertEquals(2, fixed.size());
		for (Iterator<MpPublicationContributor> fixedIter = fixed.iterator(); fixedIter.hasNext();) {
			MpPublicationContributor test = fixedIter.next();
			if (1 == test.getId()) {
				assertEquals(1, test.getRank().intValue());
			} else {
				assertEquals(2, test.getRank().intValue());
			}
		}

		MpPublicationContributor contributorC = new MpPublicationContributor();
		contributorC.setId(3);
		contributorC.setRank(1);
		contributors.add(contributorC);
		fixed = binding.fixRanks(contributors);
		assertEquals(3, fixed.size());
		for (int i=0; i<fixed.size(); i++) {
			assertEquals(i+1, ((MpPublicationContributor) fixed.toArray()[i]).getRank().intValue());
		}
	}

	@Test
	public void bindContributorsTest() {
		
	}

	@Test
	public void getSeriesTitlePubMapTest() {
		Map<String, Object> pubMap = null;
		PublicationSubtype subtype = new PublicationSubtype();
		assertNull(binding.getSeriesTitle(null, pubMap));
		assertNull(binding.getSeriesTitle(subtype, pubMap));
		pubMap = new HashMap<>();
		assertNull(binding.getSeriesTitle(subtype, pubMap));
		assertNull(binding.getSeriesTitle(null, pubMap));

		subtype.setId(PublicationSubtype.USGS_NUMBERED_SERIES);
		assertNull(binding.getSeriesTitle(subtype, pubMap));

		pubMap.put(IpdsMessageLog.USGSSERIESTYPEVALUE, "");
		assertNull(binding.getSeriesTitle(subtype, pubMap));

		pubMap.put(IpdsMessageLog.USGSSERIESTYPEVALUE, "Coal Map");
		assertEquals(309, binding.getSeriesTitle(subtype, pubMap).getId().intValue());
	}

	@Test
	public void getSeriesTitleStringTest() {
		String text = null;
		PublicationSubtype subtype = new PublicationSubtype();
		assertNull(binding.getSeriesTitle(null, text));
		assertNull(binding.getSeriesTitle(subtype, text));
		text = "";
		assertNull(binding.getSeriesTitle(subtype, text));
		assertNull(binding.getSeriesTitle(null, text));

		subtype.setId(PublicationSubtype.USGS_NUMBERED_SERIES);
		assertNull(binding.getSeriesTitle(subtype, text));

		text = "Coal Map";
		assertEquals(309, binding.getSeriesTitle(subtype, text).getId().intValue());
	}

	@Test
	public void bindPublicationTest() {
		Map<String, Object> pubMap = new HashMap<>();
		assertNull(binding.bindPublication(null, IpdsProcessTest.TEST_IPDS_CONTEXT));
		assertNull(binding.bindPublication(pubMap, IpdsProcessTest.TEST_IPDS_CONTEXT));

		pubMap = IpdsMessageLogDaoIT.createPubMap1();
		MpPublication pub1 = binding.bindPublication(pubMap, IpdsProcessTest.TEST_IPDS_CONTEXT);
		assertPub1(pub1);

		pubMap = IpdsMessageLogDaoIT.createPubMap2();
		MpPublication pub2 = binding.bindPublication(pubMap, IpdsProcessTest.TEST_IPDS_CONTEXT);
		assertPub2(pub2);

		pubMap = IpdsMessageLogDaoIT.createPubMap3();
		MpPublication pub3 = binding.bindPublication(pubMap, IpdsProcessTest.TEST_IPDS_CONTEXT);
		assertPub3(pub3);

		pubMap = IpdsMessageLogDaoIT.createPubMap4();
		MpPublication pub4 = binding.bindPublication(pubMap, IpdsProcessTest.TEST_IPDS_CONTEXT);
		assertPub4(pub4);
	}

	@Test
	public void getStringValueTest() {
		Map<String, Object> pubMap = new HashMap<>();
		assertNull(binding.getStringValue(null, null));
		assertNull(binding.getStringValue(pubMap, null));
		assertNull(binding.getStringValue(pubMap, "xx"));
		assertNull(binding.getStringValue(pubMap, "xx"));
		assertNull(binding.getStringValue(null, "xx"));

		pubMap.put("xxx", "  owiytuiwruto   ");
		assertEquals("owiytuiwruto", binding.getStringValue(pubMap, "xxx"));

		pubMap.put("xxx", "");
		assertNull(binding.getStringValue(pubMap, "xxx"));
	}

	protected void assertPub1(MpPublication pub) {
		assertPubCommon(pub);

		assertEquals(18, pub.getPublicationType().getId().intValue());
		assertEquals(5, pub.getPublicationSubtype().getId().intValue());
		assertEquals(330, pub.getSeriesTitle().getId().intValue());

		assertEquals("12.1", pub.getSeriesNumber());
		assertEquals("a", pub.getChapter());
		assertEquals("My Final Title", pub.getTitle());

		assertEquals("My Abstract", pub.getDocAbstract());
		assertEquals("U.S. Geological Survey", pub.getPublisher());
		assertEquals("Reston VA", pub.getPublisherLocation());

		assertEquals("doi", pub.getDoi());
		assertEquals("I really want to cooperate", pub.getCollaboration());

		assertEquals("A short citation", pub.getUsgsCitation());
		assertEquals("physical desc", pub.getProductDescription());
		assertEquals("pages 1-5", pub.getStartPage());

		assertEquals("what a summary", pub.getNotes());
		assertEquals("IP1234", pub.getIpdsId());
		assertEquals(ProcessType.SPN_PRODUCTION.getIpdsValue(), pub.getIpdsReviewProcessState());

		assertEquals("453228", pub.getIpdsInternalId());
		assertEquals("A Journal", pub.getLargerWorkTitle());
		assertEquals(currentYear, pub.getPublicationYear());

		assertEquals("V1", pub.getVolume());
		assertEquals("I1", pub.getIssue());
		assertEquals("E1", pub.getEdition());

		assertEquals(2, pub.getPublishingServiceCenter().getId().intValue());
		assertEquals(IpdsProcessTest.TEST_IPDS_CONTEXT, pub.getIpdsContext());
	}

	protected void assertPub2(MpPublication pub) {
		assertPubCommon(pub);

		assertNull(pub.getPublicationType());
		assertNull(pub.getPublicationSubtype());
		assertNull(pub.getSeriesTitle());

		assertNull(pub.getSeriesNumber());
		assertEquals("a", pub.getChapter());
		assertEquals("My Working Title", pub.getTitle());

		assertEquals("My Abstract", pub.getDocAbstract());
		assertEquals("Not one of those USGS Publishers", pub.getPublisher());
		assertNull(pub.getPublisherLocation());

		assertEquals("doi", pub.getDoi());
		assertEquals("I really want to cooperate", pub.getCollaboration());

		assertEquals("A short citation", pub.getUsgsCitation());
		assertEquals("physical desc", pub.getProductDescription());
		assertEquals("pages 1-5", pub.getStartPage());

		assertEquals("what a summary", pub.getNotes());
		assertEquals(ProcessType.SPN_PRODUCTION.getIpdsValue(), pub.getIpdsReviewProcessState());

		assertEquals("453228", pub.getIpdsInternalId());
		assertEquals("A Journal Title", pub.getLargerWorkTitle());
		assertEquals(currentYear, pub.getPublicationYear());

		assertEquals("V2", pub.getVolume());
		assertEquals("I2", pub.getIssue());
		assertEquals("E2", pub.getEdition());

		assertEquals(3, pub.getPublishingServiceCenter().getId().intValue());
		assertEquals(IpdsProcessTest.TEST_IPDS_CONTEXT, pub.getIpdsContext());
	}

	protected void assertPub3(MpPublication pub) {
		assertPubCommon(pub);

		assertEquals(2, pub.getPublicationType().getId().intValue());
		assertNull(pub.getPublicationSubtype());
		assertNull(pub.getSeriesTitle());

		assertNull(pub.getSeriesNumber());
		assertEquals("a", pub.getChapter());
		assertEquals("My Final Title", pub.getTitle());

		assertEquals("My Abstract", pub.getDocAbstract());
		assertEquals("U.S. Geological Survey", pub.getPublisher());
		assertEquals("Reston VA", pub.getPublisherLocation());

		assertEquals("doi", pub.getDoi());
		assertEquals("I really want to cooperate", pub.getCollaboration());

		assertEquals("A short citation", pub.getUsgsCitation());
		assertEquals("physical desc", pub.getProductDescription());
		assertEquals("pages 1-5", pub.getStartPage());

		assertEquals("what a summary", pub.getNotes());
		assertEquals("IP1234", pub.getIpdsId());
		assertEquals(ProcessType.SPN_PRODUCTION.getIpdsValue(), pub.getIpdsReviewProcessState());

		assertEquals("453228", pub.getIpdsInternalId());
		//TODO pub.setLargerWorkTitle(getStringValue(inPub, IpdsMessageLog.JOURNALTITLE));
		assertEquals(currentYear, pub.getPublicationYear());

		assertEquals("V3", pub.getVolume());
		assertEquals("I3", pub.getIssue());
		assertEquals("E3", pub.getEdition());

		assertEquals(4, pub.getPublishingServiceCenter().getId().intValue());
		assertEquals(IpdsProcessTest.TEST_IPDS_CONTEXT, pub.getIpdsContext());
	}

	private void assertPub4(MpPublication pub) {
		assertPubCommon(pub);

		assertEquals(21, pub.getPublicationType().getId().intValue());
		assertEquals(28, pub.getPublicationSubtype().getId().intValue());
		assertNull(pub.getSeriesTitle());

		assertNull(pub.getSeriesNumber());
		assertEquals("a", pub.getChapter());
		assertEquals("My Final Title", pub.getTitle());

		assertEquals("My Abstract", pub.getDocAbstract());
		assertEquals("Not one of those USGS Publishers", pub.getPublisher());
		assertNull(pub.getPublisherLocation());

		assertEquals("doi", pub.getDoi());
		assertEquals("I really want to cooperate", pub.getCollaboration());

		assertEquals("A short citation", pub.getUsgsCitation());
		assertEquals("physical desc", pub.getProductDescription());
		assertEquals("pages 1-5", pub.getStartPage());

		assertEquals("what a summary", pub.getNotes());
		assertEquals("IP1234", pub.getIpdsId());
		assertEquals(ProcessType.SPN_PRODUCTION.getIpdsValue(), pub.getIpdsReviewProcessState());

		assertEquals("453228", pub.getIpdsInternalId());
		assertEquals("A Journal", pub.getLargerWorkTitle());
		assertEquals(currentYear, pub.getPublicationYear());

		assertEquals("V4", pub.getVolume());
		assertEquals("I4", pub.getIssue());
		assertEquals("E4", pub.getEdition());

		assertEquals(5, pub.getPublishingServiceCenter().getId().intValue());
		assertEquals(IpdsProcessTest.TEST_IPDS_CONTEXT, pub.getIpdsContext());
	}

	private void assertPubCommon(MpPublication pub) {
		assertNotNull(pub);

		assertNull(pub.getId());
		assertNull(pub.getIndexId());
		assertNull(pub.getDisplayToPublicDate());

		assertNull(pub.getSubseriesTitle());
		assertNull(pub.getSubchapterNumber());
		assertEquals("English", pub.getLanguage());

		assertNull(pub.getIssn());
		assertNull(pub.getEndPage());
		assertNull(pub.getNumberOfPages());

		assertNull(pub.getOnlineOnly());
		assertNull(pub.getAdditionalOnlineFiles());
		assertNull(pub.getTemporalStart());

		assertNull(pub.getTemporalEnd());
		assertNull(pub.getLargerWorkType());
		assertNull(pub.getConferenceTitle());

		assertNull(pub.getConferenceDate());
		assertNull(pub.getConferenceLocation());
		assertNull(pub.getContributors());

		assertNull(pub.getCostCenters());
		assertNull(pub.getLinks());
		assertNull(pub.getContact());

		assertNull(pub.getComments());
		assertNull(pub.getTableOfContents());
	}
}