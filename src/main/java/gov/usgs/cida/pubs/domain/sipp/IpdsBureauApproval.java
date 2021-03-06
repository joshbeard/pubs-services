package gov.usgs.cida.pubs.domain.sipp;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.usgs.cida.pubs.dao.sipp.IpdsBureauApprovalDao;

@Component
public class IpdsBureauApproval {
	private static IpdsBureauApprovalDao ipdsBureauApprovalDao;

	@JsonProperty("IPNumber")
	private String ipNumber;
	@JsonProperty("WorkingTitle")
	private String workingTitle;
	@JsonProperty("SeniorUSGSAuthor")
	private String seniorUSGSAuthor;
	@JsonProperty("TeamProjectName")
	private String teamProjectName;
	@JsonProperty("Citation")
	private String citation;
	@JsonProperty("ProductType")
	private String productType;
	@JsonProperty("USGSSeriesType")
	private String usgsSeriesType;
	@JsonProperty("USGSSeriesNumber")
	private String usgsSeriesNumber;
	@JsonProperty("BureauApprovalDate")
	private LocalDateTime bureauApprovalDate;
	@JsonProperty("BureauApprovalBy")
	private String bureauApprovalBy;
	@JsonProperty("DisseminatedDate")
	private LocalDateTime disseminatedDate;
	@JsonProperty("CurrentTask")
	private String currentTask;
	@JsonProperty("CurrentTaskStarted")
	private LocalDateTime currentTaskStarted;
	@JsonProperty("CostCenter")
	private String costCenter;
	@JsonProperty("PublishingServiceCenter")
	private String publishingServiceCenter;
	@JsonProperty("USGSMissionArea")
	private String usgsMissionArea;
	@JsonProperty("USGSRegion")
	private String usgsRegion;
	@JsonProperty("USGSProgram")
	private String usgsProgram;
	@JsonProperty("CostCenterCode")
	private String costCenterCode;
	public String getIpNumber() {
		return ipNumber;
	}
	public void setIpNumber(String ipNumber) {
		this.ipNumber = ipNumber;
	}
	public String getWorkingTitle() {
		return workingTitle;
	}
	public void setWorkingTitle(String workingTitle) {
		this.workingTitle = workingTitle;
	}
	public String getSeniorUSGSAuthor() {
		return seniorUSGSAuthor;
	}
	public void setSeniorUSGSAuthor(String seniorUSGSAuthor) {
		this.seniorUSGSAuthor = seniorUSGSAuthor;
	}
	public String getTeamProjectName() {
		return teamProjectName;
	}
	public void setTeamProjectName(String teamProjectName) {
		this.teamProjectName = teamProjectName;
	}
	public String getCitation() {
		return citation;
	}
	public void setCitation(String citation) {
		this.citation = citation;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getUsgsSeriesType() {
		return usgsSeriesType;
	}
	public void setUsgsSeriesType(String usgsSeriesType) {
		this.usgsSeriesType = usgsSeriesType;
	}
	public String getUsgsSeriesNumber() {
		return usgsSeriesNumber;
	}
	public void setUsgsSeriesNumber(String usgsSeriesNumber) {
		this.usgsSeriesNumber = usgsSeriesNumber;
	}
	public LocalDateTime getBureauApprovalDate() {
		return bureauApprovalDate;
	}
	public void setBureauApprovalDate(LocalDateTime bureauApprovalDate) {
		this.bureauApprovalDate = bureauApprovalDate;
	}
	public String getBureauApprovalBy() {
		return bureauApprovalBy;
	}
	public void setBureauApprovalBy(String bureauApprovalBy) {
		this.bureauApprovalBy = bureauApprovalBy;
	}
	public LocalDateTime getDisseminatedDate() {
		return disseminatedDate;
	}
	public void setDisseminatedDate(LocalDateTime disseminatedDate) {
		this.disseminatedDate = disseminatedDate;
	}
	public String getCurrentTask() {
		return currentTask;
	}
	public void setCurrentTask(String currentTask) {
		this.currentTask = currentTask;
	}
	public LocalDateTime getCurrentTaskStarted() {
		return currentTaskStarted;
	}
	public void setCurrentTaskStarted(LocalDateTime currentTaskStarted) {
		this.currentTaskStarted = currentTaskStarted;
	}
	public String getCostCenter() {
		return costCenter;
	}
	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}
	public String getPublishingServiceCenter() {
		return publishingServiceCenter;
	}
	public void setPublishingServiceCenter(String publishingServiceCenter) {
		this.publishingServiceCenter = publishingServiceCenter;
	}
	public String getUsgsMissionArea() {
		return usgsMissionArea;
	}
	public void setUsgsMissionArea(String usgsMissionArea) {
		this.usgsMissionArea = usgsMissionArea;
	}
	public String getUsgsRegion() {
		return usgsRegion;
	}
	public void setUsgsRegion(String usgsRegion) {
		this.usgsRegion = usgsRegion;
	}
	public String getUsgsProgram() {
		return usgsProgram;
	}
	public void setUsgsProgram(String usgsProgram) {
		this.usgsProgram = usgsProgram;
	}
	public String getCostCenterCode() {
		return costCenterCode;
	}
	public void setCostCenterCode(String costCenterCode) {
		this.costCenterCode = costCenterCode;
	}
	public static IpdsBureauApprovalDao getDao() {
		return ipdsBureauApprovalDao;
	}
	@Autowired
	@Qualifier("ipdsBureauApprovalDao")
	public void setIpdsBureauApprovalDao(IpdsBureauApprovalDao inIpdsBureauApprovalDao) {
		ipdsBureauApprovalDao = inIpdsBureauApprovalDao;
	}
}
