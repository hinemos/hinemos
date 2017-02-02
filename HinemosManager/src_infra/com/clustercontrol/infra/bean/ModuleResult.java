/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
	private boolean hasNext;
	private List<ModuleNodeResult> nodeResultList = new ArrayList<>();
	
	// for jaxws
	public ModuleResult() {
		
	}
	
	public ModuleResult(String sessionid, String moduleId) {
		this.sessionId = sessionid;
		this.moduleId = moduleId;
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
