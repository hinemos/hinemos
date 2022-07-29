/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 実行結果を格納する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://infra.ws.clustercontrol.com")
public class ModuleResult {
	private int moduleType;
	private String sessionId;
	private String moduleId;
	private String subModuleId;
	private String subManagementId;
	private boolean hasNext;
	private List<ModuleNodeResult> nodeResultList = new ArrayList<>();
	
	// for jaxws
	public ModuleResult() {
		
	}
	
	public ModuleResult(String sessionid, String moduleId) {
		this.sessionId = sessionid;
		this.moduleId = moduleId;
	}

	public ModuleResult(String sessionid, String moduleId, String subModuleId, String subManagementId) {
		this.sessionId = sessionid;
		this.moduleId = moduleId;
		this.subModuleId = subModuleId;
		this.subManagementId = subManagementId;
	}

	public int getModuleType() {
		return moduleType;
	}
	public void setModuleType(int moduleType) {
		this.moduleType = moduleType;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	public String getSubModuleId() {
		return subModuleId;
	}
	public void setSubModuleId(String subModuleId) {
		this.subModuleId = subModuleId;
	}
	
	public String getSubManagementId() {
		return subManagementId;
	}
	public void setSubManagementId(String subManagementId) {
		this.subManagementId = subManagementId;
	}
	
	public boolean isHasNext() {
		return hasNext;
	}

	public void setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
	}

	public List<ModuleNodeResult> getModuleNodeResultList() {
		return nodeResultList;
	}

	public void setModuleNodeResultList(List<ModuleNodeResult> nodeResultList) {
		this.nodeResultList = nodeResultList;
	}
	
	
}
