/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.hinemosagent.bean.AgentJavaInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = AgentJavaInfo.class)
public class AgentJavaInfoRequest extends AgentRequestDto {

	// ---- from AgentJavaInfo
	private String osName;
	private String osVersion;
	private String osArch;
	private String sunArchDataModel;
	private String javaVendor;
	private String javaVersion;
	private String javaSpecificationVersion;
	private String javaClassVersion;
	private String javaVmInfo;
	private String javaVmVersion;
	private String javaVmName;

	public AgentJavaInfoRequest() {
	}

	// ---- accessors

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getOsArch() {
		return osArch;
	}

	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}

	public String getSunArchDataModel() {
		return sunArchDataModel;
	}

	public void setSunArchDataModel(String sunArchDataModel) {
		this.sunArchDataModel = sunArchDataModel;
	}

	public String getJavaVendor() {
		return javaVendor;
	}

	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public String getJavaSpecificationVersion() {
		return javaSpecificationVersion;
	}

	public void setJavaSpecificationVersion(String javaSpecificationVersion) {
		this.javaSpecificationVersion = javaSpecificationVersion;
	}

	public String getJavaClassVersion() {
		return javaClassVersion;
	}

	public void setJavaClassVersion(String javaClassVersion) {
		this.javaClassVersion = javaClassVersion;
	}

	public String getJavaVmInfo() {
		return javaVmInfo;
	}

	public void setJavaVmInfo(String javaVmInfo) {
		this.javaVmInfo = javaVmInfo;
	}

	public String getJavaVmVersion() {
		return javaVmVersion;
	}

	public void setJavaVmVersion(String javaVmVersion) {
		this.javaVmVersion = javaVmVersion;
	}

	public String getJavaVmName() {
		return javaVmName;
	}

	public void setJavaVmName(String javaVmName) {
		this.javaVmName = javaVmName;
	}

}
