/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

public class CreateAccessInfoListForDialogResponse {
	private String facilityId;
	private String moduleId;
	private String sshUser;
	private String sshPassword;
	private String sshPrivateKeyFilepath;
	private String sshPrivateKeyPassphrase;
	private String winRmUser;
	private String winRmPassword;
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	public String getSshUser() {
		return sshUser;
	}
	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}
	public String getSshPassword() {
		return sshPassword;
	}
	public void setSshPassword(String sshPassword) {
		this.sshPassword = sshPassword;
	}
	public String getSshPrivateKeyFilepath() {
		return sshPrivateKeyFilepath;
	}
	public void setSshPrivateKeyFilepath(String sshPrivateKeyFilepath) {
		this.sshPrivateKeyFilepath = sshPrivateKeyFilepath;
	}
	public String getSshPrivateKeyPassphrase() {
		return sshPrivateKeyPassphrase;
	}
	public void setSshPrivateKeyPassphrase(String sshPrivateKeyPassphrase) {
		this.sshPrivateKeyPassphrase = sshPrivateKeyPassphrase;
	}
	public String getWinRmUser() {
		return winRmUser;
	}
	public void setWinRmUser(String winRmUser) {
		this.winRmUser = winRmUser;
	}
	public String getWinRmPassword() {
		return winRmPassword;
	}
	public void setWinRmPassword(String winRmPassword) {
		this.winRmPassword = winRmPassword;
	}
	@Override
	public String toString() {
		return "CreateAccessInfoListForDialogResponse [facilityId=" + facilityId + ", moduleId=" + moduleId
				+ ", sshUser=" + sshUser + ", sshPassword=" + sshPassword + ", sshPrivateKeyFilepath="
				+ sshPrivateKeyFilepath + ", sshPrivateKeyPassphrase=" + sshPrivateKeyPassphrase + ""
				+ ", winRmUser=" + winRmUser + ", winRmPassword=" + winRmPassword + "]";
	}
}
