/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class JmxUrlFormatInfo implements Serializable {

	private static final long serialVersionUID = 2802260307520621507L;
	private String jmxUrlFormatName = "";
	private String jmxUrlFormat = "";
	
	public JmxUrlFormatInfo() {}
	
	public JmxUrlFormatInfo(String jmxUrlFormatName, String jmxUrlFormat) {
		super();
		this.jmxUrlFormatName = jmxUrlFormatName;
		this.jmxUrlFormat = jmxUrlFormat;
	}
	
	public String getJmxUrlFormatName() {
		return jmxUrlFormatName;
	}
	
	public void setJmxUrlFormatName(String jmxUrlFormatName) {
		this.jmxUrlFormatName = jmxUrlFormatName;
	}
	
	public String getJmxUrlFormat() {
		return jmxUrlFormat;
	}
	
	public void setJmxUrlFormat(String jmxUrlFormat) {
		this.jmxUrlFormat = jmxUrlFormat;
	}

}
