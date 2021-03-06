package gov.usgs.cida.pubs.webservice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONArrayAs;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import gov.usgs.cida.pubs.BaseTest;
import gov.usgs.cida.pubs.ConfigurationService;
import gov.usgs.cida.pubs.PubsConstantsHelper;
import gov.usgs.cida.pubs.dao.intfc.IDao;
import gov.usgs.cida.pubs.dao.intfc.IPersonContributorDao;
import gov.usgs.cida.pubs.domain.Contributor;
import gov.usgs.cida.pubs.domain.PersonContributor;
import gov.usgs.cida.pubs.domain.UsgsContributor;

@EnableWebMvc
@AutoConfigureMockMvc(secure=false)
@SpringBootTest(webEnvironment=WebEnvironment.MOCK,
	classes={ConfigurationService.class, LookupMvcService.class, PersonContributor.class, Contributor.class})
public class LkupMvcServiceTest extends BaseTest {

	@MockBean(name="personContributorDao")
	IPersonContributorDao personContributorDao;
	@MockBean(name="contributorDao")
	IDao<Contributor<?>> contributorDao;
	PersonContributor<?> personContributor;
	@Autowired
	MockMvc mockMvc;

	@Before
	public void setup() {
		personContributor = new UsgsContributor();
	}

	@Test
	public void getPeopleTest() throws Exception {
		mockMvc.perform(get("/lookup/people").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
		mockMvc.perform(get("/lookup/people?text=a").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
		when(personContributorDao.getByMap(anyMap())).thenReturn(getPeople());

		MvcResult rtn = mockMvc.perform(get("/lookup/people?text=kr").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
		.andExpect(content().encoding(PubsConstantsHelper.DEFAULT_ENCODING))
		.andReturn();
		assertThat(getRtnAsJSONArray(rtn),
				sameJSONArrayAs(new JSONArray("[{\"id\":1,\"text\":\"Kreft, James M. jkreft@usgs.gov\"}]")).allowingAnyArrayOrdering());
	}

	List<Contributor<?>> getPeople() {
		List<Contributor<?>> rtn = new ArrayList<>();
		UsgsContributor contributor = new UsgsContributor();
		contributor.setId(1);
		contributor.setFamily("Kreft");
		contributor.setGiven("James M.");
		contributor.setEmail("jkreft@usgs.gov");
		rtn.add(contributor);
		return rtn;
	}
}
