package gov.usgs.cida.pubs.webservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

import gov.usgs.cida.pubs.PubsConstantsHelper;
import gov.usgs.cida.pubs.dao.BaseDao;
import gov.usgs.cida.pubs.dao.PublicationDao;
import gov.usgs.cida.pubs.dao.mp.MpPublicationDao;
import gov.usgs.cida.pubs.dao.pw.PwPublicationDao;
import gov.usgs.cida.pubs.utility.DataNormalizationUtils;
import gov.usgs.cida.pubs.utility.PubsUtils;

public abstract class MvcService<D> {

	public static final String TEXT_SEARCH = BaseDao.TEXT_SEARCH;
	public static final String ACTIVE_SEARCH = "active";
	public static final String TEXT_SEARCH_STARTS_WITH_SUFFIX = ":*";
	public static final String TEXT_SEARCH_AND = " & ";

	private static final Pattern G_PATTERN = Pattern.compile("^polygon\\(\\((-?\\d+\\.?\\d* -?\\d+\\.?\\d*,){3,}-?\\d+\\.?\\d* -?\\d+\\.?\\d*\\)\\)$");

	protected Map<String, Object> buildFilters(Boolean chorus, String[] contributingOffice, String[] contributor,
			String[] orcid, Boolean doi, String endYear, String g, String global, String[] indexId, String[] ipdsId, String[] listId,
			String modDateHigh, String modDateLow, String modXDays, String orderBy, String page_number,
			String page_row_start, String page_size, String[] prodId, String[] pubAbstract, String pubDateHigh,
			String pubDateLow, String pubXDays, String q, String[] linkType, String[] noLinkType, String[] reportNumber, String[] seriesName,
			String startYear, String[] subtypeName, String[] title, String[] typeName, String[] year) {
		Map<String, Object> filters = new HashMap<>();

		filters.put(PublicationDao.PUB_ABSTRACT, pubAbstract);
		filters.put(PwPublicationDao.CHORUS, chorus);
		filters.put(PublicationDao.CONTRIBUTING_OFFICE, contributingOffice);
		filters.put(PublicationDao.CONTRIBUTOR, configureContributorFilter(contributor));
		filters.put(PublicationDao.ORCID, DataNormalizationUtils.normalizeOrcid(orcid));
		filters.put(PublicationDao.DOI, doi);
		filters.put(PublicationDao.END_YEAR, endYear);
		if (null != g && G_PATTERN.matcher(g.toLowerCase()).matches()) {
			filters.put(PwPublicationDao.G, g.toLowerCase());
		}
		filters.put(MpPublicationDao.GLOBAL, global);
		filters.put(PublicationDao.INDEX_ID, indexId);
		filters.put(PublicationDao.IPDS_ID, ipdsId);
		filters.put(MpPublicationDao.LIST_ID, null == listId ? null : mapListId(listId));
		filters.put(PwPublicationDao.MOD_DATE_HIGH, modDateHigh);
		filters.put(PwPublicationDao.MOD_DATE_LOW, modDateLow);
		filters.put(PwPublicationDao.MOD_X_DAYS, modXDays);
		filters.put(PublicationDao.ORDER_BY, orderBy);
		filters.put(BaseDao.PAGE_NUMBER, PubsUtils.parseInteger(page_number));
		filters.put(PublicationDao.PAGE_ROW_START, PubsUtils.parseInteger(page_row_start));
		filters.put(PublicationDao.PAGE_SIZE, PubsUtils.parseInteger(page_size));
		filters.put(PublicationDao.PROD_ID, prodId);
		filters.put(PwPublicationDao.PUB_DATE_HIGH, pubDateHigh);
		filters.put(PwPublicationDao.PUB_DATE_LOW, pubDateLow);
		filters.put(PwPublicationDao.PUB_X_DAYS, pubXDays);
		filters.putAll(configureSingleSearchFilters(q));
		filters.put(PublicationDao.LINK_TYPE, linkType);
		filters.put(PublicationDao.NO_LINK_TYPE, noLinkType);
		filters.put(PublicationDao.REPORT_NUMBER, reportNumber);
		filters.put(PublicationDao.SERIES_NAME, seriesName);
		filters.put(PublicationDao.START_YEAR, startYear);
		filters.put(PublicationDao.SUBTYPE_NAME, subtypeName);
		filters.put(PublicationDao.TITLE, title);
		filters.put(PublicationDao.TYPE_NAME, typeName);
		filters.put(PublicationDao.YEAR, year);

		return filters;
	}

	protected Integer[] mapListId(String[] listId) {
		if (null == listId) {
			return null;
		}
		Integer[] listIdInt = new Integer[listId.length];
		int index = 0;
		for (int i=0; i < listId.length; i++) {
			Integer x = PubsUtils.parseInteger(listId[i]);
			if (null != x) {
				listIdInt[index] = x;
				index++;
			}
		}
		return Arrays.copyOf(listIdInt, index);
	}

	protected Map<String, Object> configureSingleSearchFilters(String searchTerms) {
		Map<String, Object> rtn = new HashMap<>();
		//On the MP side, We split the input on spaces and commas to ultimately create an "and" query on each word
		//On the warehouse side, we are doing Text queries
		if (StringUtils.isNotBlank(searchTerms)) {
			List<String> splitTerms = Arrays.stream(searchTerms.trim().toLowerCase().split(PubsConstantsHelper.SEARCH_TERMS_SPLIT_REGEX))
					.filter(x -> StringUtils.isNotEmpty(x))
					.collect(Collectors.toList());
			if (!splitTerms.isEmpty()) {
				rtn.put(MpPublicationDao.SEARCH_TERMS, buildSearchTerms(splitTerms));

				rtn.put(PublicationDao.Q, buildQ(splitTerms));
			}
		}
		return rtn;
	}

	protected String[] buildSearchTerms(List<String> splitTerms) {
		List<String> newList = new LinkedList<>();
		if (null != splitTerms) {
			Iterator<String> i = splitTerms.iterator(); 
			while (i.hasNext()) {
				String term = i.next();
				if (StringUtils.isNotBlank(term)) {
					newList.add(term);
				}
			}
		}
		if (newList.isEmpty()) {
			return null;
		} else {
			return newList.toArray(new String[newList.size()]);
		}
	}

	protected String buildQ(List<String> splitTerms) {
		//The context search should suffix each term with TEXT_SEARCH_STARTS_WITH_SUFFIX and join them with TEXT_SEARCH_AND.
		//This gives us a "stem" search with the logical "and" applied to the terms. 
		if (null == splitTerms || splitTerms.isEmpty()) {
			return null;
		}
		List<String> newList = new LinkedList<>();
		Iterator<String> i = splitTerms.iterator(); 
		while (i.hasNext()) {
			String term = i.next();
			if (StringUtils.isNotBlank(term)) {
				newList.add(term + TEXT_SEARCH_STARTS_WITH_SUFFIX);
			}
		}
		return StringUtils.join(newList, TEXT_SEARCH_AND);
	}

	protected String configureContributorFilter(String[] text) {
		//This is overly complicated because any comma that the user enters in the search box is interpreted as an
		//addition parameter, rather than as a comma - for instance "carvin, rebecca" ends up as {"carvin", " rebecca"}
		String rtn = null;
		List<String> values = new ArrayList<String>();
		if (null != text && 0 < text.length) {
			//Arrays.asList returns a fixed size java.util.Arrays$ArrayList, so we actually need to create a real list to 
			//be able to add entries to it.
			List<String> textList = new LinkedList<>(Arrays.asList(text));
			for (String temp : textList) {
				if (null != temp) {
					List<String> splitTerms = Arrays.asList(temp.trim().toLowerCase().split(" "));
					Iterator<String> i = splitTerms.iterator(); 
					while (i.hasNext()) {
						String term = i.next();
						if (StringUtils.isNotBlank(term)) {
							values.add(term + TEXT_SEARCH_STARTS_WITH_SUFFIX);
						}
					}					
				}
			}
			if (!values.isEmpty()) {
				rtn = StringUtils.join(values, TEXT_SEARCH_AND);
			}
		}
		return rtn;
	}

	protected Map<String, Object> buildPaging (String inPageRowStart, String inPageSize, String inPageNumber) {
		Integer pageRowStart = PubsUtils.parseInteger(inPageRowStart);
		Integer pageSize = PubsUtils.parseInteger(inPageSize);
		Integer pageNumber = PubsUtils.parseInteger(inPageNumber);
		Map<String, Object> paging = new HashMap<>();
		if (null != pageNumber) {
			//pageNumber overrides the pageRowStart
			if (null == pageSize) {
				//default pageSize with pageNumber
				pageSize = 25;
			}
			pageRowStart = ((pageNumber - 1) * pageSize);
		} else {
			if (null == pageRowStart) {
				pageRowStart = 0;
			}
			if (null == pageSize) {
				//default pageSize with pageRowStart
				pageSize = 15;
			}
		}
		paging.put(BaseDao.PAGE_ROW_START, pageRowStart);
		paging.put(BaseDao.PAGE_SIZE, pageSize);
		paging.put(BaseDao.PAGE_NUMBER, pageNumber);
		return paging;
	}

	//This check is meant to slow any denial-of-service attacks against insecure ("permitAll") endpoints (see the securityContext.xml).
	//It should not be necessary to use it on any other endpoints. 
	protected boolean validateParametersSetHeaders(HttpServletRequest request, HttpServletResponse response) {
		boolean rtn = true;
		setHeaders(response);
		if (request.getParameterMap().isEmpty()
				&& ObjectUtils.isEmpty(request.getHeader(PubsConstantsHelper.ACCEPT_HEADER))) {
			rtn = false;
			response.setStatus(HttpStatus.BAD_REQUEST.value());
		}
		return rtn;
	}

	protected void setHeaders(HttpServletResponse response) {
		response.setCharacterEncoding(PubsConstantsHelper.DEFAULT_ENCODING);
	}

}
