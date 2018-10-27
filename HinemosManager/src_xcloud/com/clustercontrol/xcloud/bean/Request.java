/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class Request {
	private List<Option> options;
	
	public Request() {
	}
	
	public void setOptions(List<Option> options) {
		this.options = options;
	}
	
	public List<Option> getOptions() {
		return options;
	}
}
