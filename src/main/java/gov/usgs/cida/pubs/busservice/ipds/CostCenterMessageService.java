package gov.usgs.cida.pubs.busservice.ipds;

import gov.usgs.cida.pubs.busservice.intfc.IIpdsService;
import gov.usgs.cida.pubs.domain.Affiliation;
import gov.usgs.cida.pubs.domain.CostCenter;
import gov.usgs.cida.pubs.domain.ProcessType;
import gov.usgs.cida.pubs.domain.ipds.IpdsMessageLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author drsteini
 *
 */
public class CostCenterMessageService implements IIpdsService {

    private final IpdsBinding ipdsBinding;

    private final IpdsWsRequester requester;

    @Autowired
    CostCenterMessageService(final IpdsBinding ipdsBinding, final IpdsWsRequester requester) {
        this.ipdsBinding = ipdsBinding;
        this.requester = requester;
    }

    /** {@inheritDoc}
     * @throws Exception 
     * @see gov.usgs.cida.mypubsJMS.service.intfc.IService#processIpdsMessage(java.lang.Object)
     */
    @Override
    @Transactional
    public void processIpdsMessage(final String nothing) throws Exception {
        String inMessageText = requester.getIpdsCostCenterXml();
        IpdsMessageLog newMessage = new IpdsMessageLog();
        newMessage.setMessageText(inMessageText);
        newMessage.setProcessType(ProcessType.COST_CENTER);
        IpdsMessageLog msg = IpdsMessageLog.getDao().getById(IpdsMessageLog.getDao().add(newMessage));

        String processingDetails = processLog(inMessageText);

        msg.setProcessingDetails(processingDetails);
        IpdsMessageLog.getDao().update(msg);
    }

    protected String processLog(String log) {
        StringBuilder rtn = new StringBuilder();
        int totalEntries = 0;
        int additions = 0;
        int errors = 0;

        try {
	        Document doc = ipdsBinding.makeDocument(log);
	
	        NodeList entries = doc.getElementsByTagName("m:properties");
	        for (int n=0; n<entries.getLength(); n++) {
	            if (entries.item(n).getNodeType() == Node.ELEMENT_NODE) {
	            	totalEntries = totalEntries++;
	            	CostCenter costCenter = new CostCenter();
	                //This is really all we should ever get..
	                Element element = (Element) entries.item(n);
		        	String ipdsId = ipdsBinding.getFirstNodeText(element, "d:Id");
		            String name = ipdsBinding.getFirstNodeText(element, "d:Name");
			        Map<String, Object> filters = new HashMap<>();
			        filters.put("ipdsId", ipdsId);
			        List<Affiliation<?>> costCenters = CostCenter.getDao().getByMap(filters);
			        //TODO what if we get more than one?
			        if (costCenters.isEmpty()) {
			        	costCenter.setIpdsId(ipdsId);
			        	costCenter.setName(name);
			        	CostCenter.getDao().add(costCenter);
			        	additions++;
			        } else {
			        	costCenter = (CostCenter) costCenters.get(0);
			        	costCenter.setName(name);
			        	CostCenter.getDao().update(costCenter);
			        }
	            }
	        }
        } catch (Exception e) {
            rtn.append("\n\tTrouble getting costCenters: " + e.getMessage());
            errors++;
        }

        String counts = "Summary:\n\tTotal Entries: " + totalEntries + "\n\tCostCenters Added: " + additions + "\n\tErrors Encountered: " + errors + "\n";

        rtn.insert(0, counts);

        return rtn.toString();
    }
}
