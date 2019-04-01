/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

import javax.xml.bind.annotation.XmlType;

/**
 * エージェントのJava環境情報を格納します。
 * <p>
 * 原則として、エージェント側は Java のシステムプロパティから取得したままの raw な情報を格納するものとします。
 * 例えば、OS が Windows か Linux であるかという判定は、マネージャ側で行います。
 * これは、判定処理の更新が必要となった場合に、マネージャ側のアップデートのほうが用意であろうという判断によります。
 * 
 * @since 6.2.0
 */
@XmlType(namespace = "http://agent.ws.clustercontrol.com")
public class AgentJavaInfo {
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

	// eclipse generated
	@Override
	public String toString() {
		return "AgentJavaInfo [osName=" + osName + ", osVersion=" + osVersion + ", osArch=" + osArch
				+ ", sunArchDataModel=" + sunArchDataModel + ", javaVendor=" + javaVendor + ", javaVersion="
				+ javaVersion + ", javaSpecificationVersion=" + javaSpecificationVersion + ", javaClassVersion="
				+ javaClassVersion + ", javaVmInfo=" + javaVmInfo + ", javaVmVersion=" + javaVmVersion + ", javaVmName="
				+ javaVmName + "]";
	}

}
