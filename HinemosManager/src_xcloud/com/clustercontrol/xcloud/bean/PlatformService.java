/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class PlatformService {
	private String targetId;
	private String name;

	public PlatformService() {
		super();
	}
	public PlatformService(String targetId, String name) {
		super();
		this.targetId = targetId;
		this.name = name;
	}
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
