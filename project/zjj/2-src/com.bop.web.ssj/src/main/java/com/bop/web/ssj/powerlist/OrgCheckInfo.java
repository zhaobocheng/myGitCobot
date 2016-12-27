package com.bop.web.ssj.powerlist;

public class OrgCheckInfo {
	
	// 事项名称
	private String itemName;
	// 事项ID
	private String itemID;
	// 机构代码
	private String orgCode;
	// 信用代码
	private String creditCode;
	// 企业名称
	private String entName;
	// 注册地址
	private String regAddr;
	// 注册地址所在区县代码
	private String regAddrCode;
	// 注册地址所在区县名称
	private String regAddrName;
	// 生产地址
	private String procAddr;
	//生产地所在区县代码
	private String procAddrCode;
	// 联系人
	private String personName;
	// 联系电话
	private String personPhone;
	// 风险等级
	private String riskLevel;
	// 字码
	private String subCode;
	
	
	/*
	 * 
	 */
	public OrgCheckInfo(String itemID,String orgCode){
		
		this.itemID = itemID;
		this.orgCode = orgCode;
	}

	public String getRegAddrName() {
		return regAddrName;
	}

	public void setRegAddrName(String regAddrName) {
		this.regAddrName = regAddrName;
	}

	public String getItemName() {
		return itemName;
	}

	public void  setItemName(String itemName){
		this.itemName = itemName;
	}
	public String getOrgCode() {
		return orgCode;
	}

	public String getEntName() {
		return entName;
	}

	public void setEntName(String entName) {
		this.entName = entName;
	}

	public String getRegAddr() {
		return regAddr;
	}

	public void setRegAddr(String regAddr) {
		this.regAddr = regAddr;
	}

	public String getRegAddrCode() {
		return regAddrCode;
	}

	public void setRegAddrCode(String regAddrCode) {
		this.regAddrCode = regAddrCode;
	}

	public String getProcAddr() {
		return procAddr;
	}

	public void setProcAddr(String procAddr) {
		this.procAddr = procAddr;
	}

	public String getProcAddrCode() {
		return procAddrCode;
	}

	public void setProcAddrCode(String procAddrCode) {
		this.procAddrCode = procAddrCode;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public String getPersonPhone() {
		return personPhone;
	}

	public void setPersonPhone(String personPhone) {
		this.personPhone = personPhone;
	}

	public String getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(String riskLevel) {
		this.riskLevel = riskLevel;
	}

	public String getSubCode() {
		return subCode;
	}

	public void setSubCode(String subCode) {
		this.subCode = subCode;
	}

	public String getItemID() {
		return itemID;
	}

	public String getCreditCode() {
		return creditCode;
	}

	public void setCreditCode(String creditCode) {
		this.creditCode = creditCode;
	}	
}
