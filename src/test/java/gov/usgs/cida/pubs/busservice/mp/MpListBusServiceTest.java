package gov.usgs.cida.pubs.busservice.mp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gov.usgs.cida.pubs.dao.BaseSpringDaoTest;
import gov.usgs.cida.pubs.dao.mp.MpListDao;
import gov.usgs.cida.pubs.domain.mp.MpList;
import gov.usgs.cida.pubs.utility.PubsUtilitiesTest;
import gov.usgs.cida.pubs.webservice.security.PubsRoles;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

public class MpListBusServiceTest extends BaseSpringDaoTest {

	@Autowired
    public Validator validator;

    private MpListBusService busService;

    @Before
    public void initTest() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        busService = new MpListBusService(validator);
    }

    @Test
    public void getObjectsTest() {
        busService.getObjects(null);
        busService.getObjects(new HashMap<String, Object>());

        Map<String, Object> filters = new HashMap<>();
        filters.put(MpListDao.ID_SEARCH, -1);
        Collection<MpList> mpLists = busService.getObjects(filters);
        assertNotNull(mpLists);
        assertEquals(0, mpLists.size());

        filters.put(MpListDao.ID_SEARCH, 1);
        mpLists = busService.getObjects(filters);
        assertNotNull(mpLists);
        assertEquals(1, mpLists.size());

        filters.clear();
        filters.put(MpListDao.TEXT_SEARCH, "ipds");
        mpLists = busService.getObjects(filters);
        assertNotNull(mpLists);
        assertEquals(6, mpLists.size());

        filters.put(MpListDao.ID_SEARCH, 3);
        mpLists = busService.getObjects(filters);
        assertNotNull(mpLists);
        assertEquals(1, mpLists.size());

        //Get all Lists
        filters.clear();
        mpLists = busService.getObjects(filters);
        assertNotNull(mpLists);
        assertEquals(31, mpLists.size());

        //Now we want just spn...
        PubsUtilitiesTest.buildTestAuthentication("dummy", Arrays.asList(PubsRoles.PUBS_SPN_USER.name()));
        mpLists = busService.getObjects(filters);
        assertNotNull(mpLists);
        assertEquals(14, mpLists.size());

    }

}
