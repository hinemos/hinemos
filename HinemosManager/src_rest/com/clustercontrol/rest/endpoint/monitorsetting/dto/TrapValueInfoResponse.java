/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.SnmptrapVersionEnum;
import com.clustercontrol.snmptrap.model.TrapValueInfo;

@RestBeanConvertIdClassSet(infoClass = TrapValueInfo.class, idName = "id")
public class TrapValueInfoResponse {
	private String mib;
	private String trapOid;
	private Integer genericId;
	private Integer specificId;
	private String uei;
	@RestBeanConvertEnum
	private SnmptrapVersionEnum version;
	private String logmsg;
	private String description;
	private Boolean procVarbindSpecified;
	@RestBeanConvertEnum
	private PriorityEnum priorityAnyVarBind;
	private String formatVarBinds;
	private Boolean validFlg;
	private List<VarBindPatternResponse> varBindPatterns = new ArrayList<>();

	public TrapValueInfoResponse() {
	}

	public String getMib() {
		return mib;
	}
	public void setMib(String mib) {
		this.mib = mib;
	}
	public String getTrapOid() {
		return trapOid;
	}
	public void setTrapOid(String trapOid) {
		this.trapOid = trapOid;
	}
	public Integer getGenericId() {
		return genericId;
	}
	public void setGenericId(Integer genericId) {
		this.genericId = genericId;
	}
	public Integer getSpecificId() {
		return specificId;
	}
	public void setSpecificId(Integer specificId) {
		this.specificId = specificId;
	}
	public String getUei() {
		return uei;
	}
	public void setUei(String uei) {
		this.uei = uei;
	}
	public SnmptrapVersionEnum getVersion() {
		return version;
	}
	public void setVersion(SnmptrapVersionEnum version) {
		this.version = version;
	}
	public String getLogmsg() {
		return logmsg;
	}
	public void setLogmsg(String logmsg) {
		this.logmsg = logmsg;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getProcVarbindSpecified() {
		return procVarbindSpecified;
	}
	public void setProcVarbindSpecified(Boolean procVarbindSpecified) {
		this.procVarbindSpecified = procVarbindSpecified;
	}
	public PriorityEnum getPriorityAnyVarBind() {
		return priorityAnyVarBind;
	}
	public void setPriorityAnyVarBind(PriorityEnum priorityAnyVarBind) {
		this.priorityAnyVarBind = priorityAnyVarBind;
	}
	public String getFormatVarBinds() {
		return formatVarBinds;
	}
	public void setFormatVarBinds(String formatVarBinds) {
		this.formatVarBinds = formatVarBinds;
	}
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}
	public List<VarBindPatternResponse> getVarBindPatterns() {
		return varBindPatterns;
	}
	public void setVarBindPatterns(List<VarBindPatternResponse> varBindPatterns) {
		this.varBindPatterns = varBindPatterns;
	}
	@Override
	public String toString() {
		return "TrapValueInfo [mib=" + mib + ", trapOid=" + trapOid + ", genericId="
				+ genericId + ", specificId=" + specificId + ", uei=" + uei + ", version=" + version + ", logmsg="
				+ logmsg + ", description=" + description + ", procVarbindSpecified=" + procVarbindSpecified
				+ ", priorityAnyVarBind=" + priorityAnyVarBind + ", formatVarBinds=" + formatVarBinds + ", validFlg="
				+ validFlg + ", varBindPatterns=" + varBindPatterns + "]";
	}

}
