package gov.usgs.cida.pubs.busservice.mp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.pubs.BaseSpringTest;
import gov.usgs.cida.pubs.domain.CostCenter;
import gov.usgs.cida.pubs.domain.PublicationCostCenter;
import gov.usgs.cida.pubs.domain.PublicationSeries;
import gov.usgs.cida.pubs.domain.PublicationSubtype;
import gov.usgs.cida.pubs.domain.PublicationType;
import gov.usgs.cida.pubs.domain.mp.MpPublication;
import gov.usgs.cida.pubs.domain.mp.MpPublicationCostCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MpPublicationBusServiceTest extends BaseSpringTest {

    @Autowired
    public Validator validator;

    private class BusService extends MpPublicationBusService {
        public BusService(Validator validator) {
            this.validator = validator;
        }
    }
    private BusService busService;

    @Before
    public void initTest() {
        busService = new BusService(validator);
    }

    @Test
    public void getObjectTest() {
        assertNull(busService.getObject(-1));
        assertNotNull(busService.getObject(1));
    }

    @Test
    public void getObjectsTest() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("id", -1);
        Collection<MpPublication> pubs = busService.getObjects(filters);
        assertNotNull(pubs);
        assertEquals(0, pubs.size());

        filters.put("id", 1);
        pubs = busService.getObjects(filters);
        assertNotNull(pubs);
        assertEquals(1, pubs.size());
    }

    @Test
    public void createObjectTest() {
        //TODO both a good create and a create w/validation errors.
        //public MpPublication createObject(MpPublication object)
        MpPublication pub = busService.createObject(new MpPublication());
        assertNotNull(pub.getId());
    }

    @Test
    public void updateObjectTest() {
        //TODO both a good update and an update w/validation errors.
        //public MpPublication updateObject(MpPublication object)
    }

    @Test
    public void deleteObjectTest() {
        //TODO both a good delete and a delete w/validation errors.
        //public ValidationResults deleteObject(MpPublication object)
    }

    @Test
    public void preProcessingTest() {
        //TODO - more than just the basic doiName testing
        MpPublication inPublication = new MpPublication();
        PublicationType pubType = new PublicationType();
        pubType.setId(PublicationType.REPORT);
        inPublication.setPublicationType(pubType);
        PublicationSubtype pubSubtype = new PublicationSubtype();
        pubSubtype.setId(PublicationSubtype.USGS_NUMBERED_SERIES);
        inPublication.setPublicationSubtype(pubSubtype);
        PublicationSeries pubSeries = new PublicationSeries();
        pubSeries.setId(PublicationSeries.SIR);
        inPublication.setPublicationSeries(pubSeries);
        inPublication.setSeriesNumber("nu-m,be r");
        MpPublication outPublication = busService.publicationPreProcessing(inPublication);
        assertNotNull(outPublication);
        assertNotNull(outPublication.getId());
        assertEquals("sirnumber", outPublication.getIndexId());
        assertEquals(MpPublicationBusService.DOI_PREFIX + "/" + outPublication.getIndexId(), outPublication.getDoiName());

        inPublication = new MpPublication();
        inPublication.setSeriesNumber("nu-m,be r");
        inPublication.setId(123);
        outPublication = busService.publicationPreProcessing(inPublication);
        assertNotNull(outPublication);
        assertNotNull(outPublication.getId());
        assertEquals("123", outPublication.getIndexId());
        assertNull(outPublication.getDoiName());

        inPublication = new MpPublication();
        pubSeries.setId(508);
        inPublication.setPublicationSeries(pubSeries);
        outPublication = busService.publicationPreProcessing(inPublication);
        assertNotNull(outPublication);
        assertNotNull(outPublication.getId());
        assertEquals(outPublication.getId().toString(), outPublication.getIndexId());
        assertNull(outPublication.getDoiName());

        inPublication = new MpPublication();
        pubSubtype.setId(PublicationSubtype.USGS_UNNUMBERED_SERIES);
        inPublication.setPublicationType(pubType);
        inPublication.setPublicationSubtype(pubSubtype);
        inPublication.setPublicationSeries(pubSeries);
        outPublication = busService.publicationPreProcessing(inPublication);
        assertNotNull(outPublication);
        assertNotNull(outPublication.getId());
        assertEquals(outPublication.getId().toString(), outPublication.getIndexId());
        assertEquals(MpPublicationBusService.DOI_PREFIX + "/" + outPublication.getIndexId(), outPublication.getDoiName());

    }

    @Test
    public void isUsgsNumberedSeriesTest() {
        assertFalse(busService.isUsgsNumberedSeries(null));
        PublicationSubtype pubSubtype = new PublicationSubtype();
        assertFalse(busService.isUsgsNumberedSeries(pubSubtype));
        pubSubtype.setId(1);
        assertFalse(busService.isUsgsNumberedSeries(pubSubtype));
        pubSubtype.setId(PublicationSubtype.USGS_UNNUMBERED_SERIES);
        assertFalse(busService.isUsgsNumberedSeries(pubSubtype));
        pubSubtype.setId(PublicationSubtype.USGS_NUMBERED_SERIES);
        assertTrue(busService.isUsgsNumberedSeries(pubSubtype));
    }

    @Test
    public void isUsgsUnnumberedSeriesTest() {
        assertFalse(busService.isUsgsUnnumberedSeries(null));
        PublicationSubtype pubSubtype = new PublicationSubtype();
        assertFalse(busService.isUsgsUnnumberedSeries(pubSubtype));
        pubSubtype.setId(1);
        assertFalse(busService.isUsgsUnnumberedSeries(pubSubtype));
        pubSubtype.setId(PublicationSubtype.USGS_NUMBERED_SERIES);
        assertFalse(busService.isUsgsUnnumberedSeries(pubSubtype));
        pubSubtype.setId(PublicationSubtype.USGS_UNNUMBERED_SERIES);
        assertTrue(busService.isUsgsUnnumberedSeries(pubSubtype));
    }

    @Test
    public void getUsgsNumberedSeriesIndexId() {
        PublicationSeries pubSeries = new PublicationSeries();
        assertNull(busService.getUsgsNumberedSeriesIndexId(null, null));
        assertNull(busService.getUsgsNumberedSeriesIndexId(pubSeries, null));
        assertNull(busService.getUsgsNumberedSeriesIndexId(null, "123"));
        pubSeries.setId(-1);
        assertNull(busService.getUsgsNumberedSeriesIndexId(pubSeries, "123"));
        pubSeries.setId(508);
        assertNull(busService.getUsgsNumberedSeriesIndexId(pubSeries, "123"));
        pubSeries.setId(330);
        assertEquals("ofr123456", busService.getUsgsNumberedSeriesIndexId(pubSeries, "1- 2-3,4,5 6"));
    }

    @Test
    public void doiNameTest() {
        assertNull(MpPublicationBusService.getDoiName(null));
        assertNull(MpPublicationBusService.getDoiName(""));
        assertEquals(MpPublicationBusService.DOI_PREFIX + "/abc", MpPublicationBusService.getDoiName("abc"));
    }

    @Test
    public void publicationPostProcessingNumberedTest() {
        MpPublication pub = new MpPublication();
        pub.setId(MpPublication.getDao().getNewProdId());
//        pub.setPublicationTypeId(PublicationType.USGS_NUMBERED_SERIES);
//        pub.setIpdsReviewProcessState(ProcessType.SPN_PRODUCTION.getIpdsValue());
//        pub.setIpdsId("IPDSX-" + pub.getId());
        pub.setDoiName("test");
        MpPublication.getDao().add(pub);
        busService.publicationPostProcessing(pub);

//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("prodId", pub.getId());
//        params.put("listId", MpList.PENDING_USGS_SERIES);
//        List<MpListPubsRel> listEntries = MpListPubsRel.getDao().getByMap(params);
//        assertEquals(1, listEntries.size());
//
//        Map<String, Object> filters = new HashMap<String, Object>();
//        filters.put("prodId", pub.getId());
//        filters.put("doiLink", MpLinkDim.DOI_LINK_SITE);
//        List<MpLinkDim> doiLinks = MpLinkDim.getDao().getByMap(filters);
//        assertEquals(0, doiLinks.size());


        pub = new MpPublication();
        pub.setId(MpPublication.getDao().getNewProdId());
//        pub.setPublicationTypeId(PublicationType.USGS_NUMBERED_SERIES);
//        pub.setIpdsReviewProcessState(ProcessType.DISSEMINATION.getIpdsValue());
//        pub.setIpdsId("IPDSX-" + pub.getId());
        pub.setDoiName("test");
        MpPublication.getDao().add(pub);
        busService.publicationPostProcessing(pub);

//        params = new HashMap<String, Object>();
//        params.put("prodId", pub.getId());
//        params.put("listId", MpList.PENDING_USGS_SERIES);
//        listEntries = MpListPubsRel.getDao().getByMap(params);
//        assertEquals(0, listEntries.size());
//
//        filters = new HashMap<String, Object>();
//        filters.put("prodId", pub.getId());
//        filters.put("doiLink", MpLinkDim.DOI_LINK_SITE);
//        doiLinks = MpLinkDim.getDao().getByMap(filters);
//        assertEquals(0, doiLinks.size());
    }

    @Test
    public void publicationPostProcessingUnNumberedTest() {
        MpPublication pub = new MpPublication();
        pub.setId(MpPublication.getDao().getNewProdId());
//        pub.setPublicationTypeId(PublicationType.USGS_UNNUMBERED_SERIES);
//        pub.setIpdsReviewProcessState(ProcessType.SPN_PRODUCTION.getIpdsValue());
//        pub.setIpdsId("IPDSX-" + pub.getId());
        pub.setDoiName("test");
        MpPublication.getDao().add(pub);
        busService.publicationPostProcessing(pub);

//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("prodId", pub.getId());
//        params.put("listId", MpList.PENDING_USGS_SERIES);
//        List<MpListPubsRel> listEntries = MpListPubsRel.getDao().getByMap(params);
//        assertEquals(1, listEntries.size());
//
//        Map<String, Object> filters = new HashMap<String, Object>();
//        filters.put("prodId", pub.getId());
//        filters.put("doiLink", MpLinkDim.DOI_LINK_SITE);
//        List<MpLinkDim> doiLinks = MpLinkDim.getDao().getByMap(filters);
//        assertEquals(0, doiLinks.size());


        pub = new MpPublication();
        pub.setId(MpPublication.getDao().getNewProdId());
//        pub.setPublicationTypeId(PublicationType.USGS_UNNUMBERED_SERIES);
//        pub.setIpdsReviewProcessState(ProcessType.DISSEMINATION.getIpdsValue());
//        pub.setIpdsId("IPDSX-" + pub.getId());
        pub.setDoiName("test");
        MpPublication.getDao().add(pub);
        busService.publicationPostProcessing(pub);

//        params = new HashMap<String, Object>();
//        params.put("prodId", pub.getId());
//        params.put("listId", MpList.PENDING_USGS_SERIES);
//        listEntries = MpListPubsRel.getDao().getByMap(params);
//        assertEquals(0, listEntries.size());
//
//        filters = new HashMap<String, Object>();
//        filters.put("prodId", pub.getId());
//        filters.put("doiLink", MpLinkDim.DOI_LINK_SITE);
//        doiLinks = MpLinkDim.getDao().getByMap(filters);
//        assertEquals(0, doiLinks.size());

    }

    @Test
    public void publicationPostProcessingNPETest() {
        MpPublication pub = new MpPublication();
        pub.setId(MpPublication.getDao().getNewProdId());
        MpPublication.getDao().add(pub);
        busService.publicationPostProcessing(pub);

//        pub.setIpdsId("IPDS-" + pub.getId());
//        pub.setPublicationTypeId(PublicationType.USGS_NUMBERED_SERIES);
        busService.publicationPostProcessing(pub);

//        pub.setPublicationTypeId(PublicationType.USGS_UNNUMBERED_SERIES);
        busService.publicationPostProcessing(pub);
    }

    @Test
    public void updateCostCentersTest() {
        MpPublication mpPub = MpPublication.getDao().getById(2);
        Integer id = MpPublication.getDao().getNewProdId();
        mpPub.setId(id);
        mpPub.setIndexId(String.valueOf(id));
        mpPub.setIpdsId("ipds_" + id);
        MpPublication.getDao().add(mpPub);

        //update with no cost centers either side
        busService.updateCostCenters(mpPub);
        Map<String, Object> filters = new HashMap<>();
        filters.put("publicationId", id);
        assertEquals(0, MpPublicationCostCenter.getDao().getByMap(filters).size());

        //Add some cost centers
        Collection<PublicationCostCenter<?>> mpccs = new ArrayList<>();
        CostCenter cc1 = CostCenter.getDao().getById(1);
        MpPublicationCostCenter mpcc1 = new MpPublicationCostCenter();
        mpcc1.setPublicationId(id);
        mpcc1.setCostCenter(cc1);
        mpccs.add(mpcc1);
        CostCenter cc2 = CostCenter.getDao().getById(2);
        MpPublicationCostCenter mpcc2 = new MpPublicationCostCenter();
        mpcc2.setPublicationId(id);
        mpcc2.setCostCenter(cc2);
        mpccs.add(mpcc2);
        mpPub.setCostCenters(mpccs);
        busService.updateCostCenters(mpPub);
        Collection<MpPublicationCostCenter> addedccs = MpPublicationCostCenter.getDao().getByMap(filters);
        assertEquals(2, addedccs.size());
        boolean gotOne = false;
        boolean gotTwo = false;
        for (MpPublicationCostCenter ccs : addedccs) {
            assertEquals(id, ccs.getPublicationId());
            if (1 == ccs.getCostCenter().getId()) {
                gotOne = true;
            } else if (2 == ccs.getCostCenter().getId()) {
                gotTwo = true;
            }
        }
        assertTrue(gotOne);
        assertTrue(gotTwo);

        //Now add one, take one away (and leave one alone).
        mpccs = new ArrayList<>();
        mpccs.add(mpcc2);
        CostCenter cc3= CostCenter.getDao().getById(3);
        MpPublicationCostCenter mpcc3 = new MpPublicationCostCenter();
        mpcc3.setPublicationId(id);
        mpcc3.setCostCenter(cc3);
        mpccs.add(mpcc3);
        mpPub.setCostCenters(mpccs);
        busService.updateCostCenters(mpPub);
        Collection<MpPublicationCostCenter> updccs = MpPublicationCostCenter.getDao().getByMap(filters);
        assertEquals(2, updccs.size());
        gotOne = false;
        gotTwo = false;
        boolean gotThree = false;
        for (MpPublicationCostCenter ccs : updccs) {
            assertEquals(id, ccs.getPublicationId());
            if (1 == ccs.getCostCenter().getId()) {
                gotOne = true;
            } else if (2 == ccs.getCostCenter().getId()) {
                gotTwo = true;
            } else if (3 == ccs.getCostCenter().getId()) {
                gotThree = true;
            }
        }
        assertFalse(gotOne);
        assertTrue(gotTwo);
        assertTrue(gotThree);
    }

    //public ValidationResults publish(final Integer prodId)
    //private void defaultThumbnail(final MpPublication mpPub)

}
