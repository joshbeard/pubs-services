package gov.usgs.cida.pubs.domain;

import gov.usgs.cida.pubs.dao.intfc.IDao;
import gov.usgs.cida.pubs.utility.PubsUtils;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Component
public class CostCenter extends Affiliation<CostCenter> implements Serializable {

	private static final long serialVersionUID = -7804226743028056085L;

	private static IDao<CostCenter> costCenterDao;

	@JsonIgnore
	private Integer ipdsId;

	public CostCenter() {
		usgs = true;
		active = true;
	}

	public Integer getIpdsId() {
		return ipdsId;
	}

	public void setIpdsId(final Integer inIpdsId) {
		ipdsId = inIpdsId;
	}

	public void setIpdsId(final String inIpdsId) {
		ipdsId = PubsUtils.parseInteger(inIpdsId);
	}

	public static IDao<CostCenter> getDao() {
		return costCenterDao;
	}

	@Autowired
	@Qualifier("costCenterDao")
	public void setCostCenterDao(final IDao<CostCenter> inCostCenterDao) {
		costCenterDao = inCostCenterDao;
	}
}
