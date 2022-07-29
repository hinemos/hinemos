/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AccessInfoRequest implements RequestDto {
	
	@RestItemName(value = MessageConstant.FACILITY_ID)
	@RestValidateString(notNull = true)
	private String facilityId;
	
	@RestItemName(value = MessageConstant.INFRA_MODULE_ID)
	@RestValidateString(notNull = true)
	private String moduleId;
	
	@RestItemName(value = MessageConstant.SSH_USER)
	@RestValidateString(maxLen = 64, minLen = 0)
	private String sshUser;
	
	@RestValidateString(maxLen = 128, minLen = 0)
	private String sshPassword;
	
	@RestItemName(value = MessageConstant.SSH_PRIVATE_KEY_FILEPATH)
	@RestValidateString(maxLen = 1024, minLen = 0)
	private String sshPrivateKeyFilepath;
	
	@RestValidateString(maxLen = 1024, minLen = 0)
	private String sshPrivateKeyPassphrase;
	
	@RestItemName(value = MessageConstant.WINRM_USER)
	@RestValidateString(maxLen = 64, minLen = 0)
	private String winRmUser;
	
	@RestItemName(value = MessageConstant.WINRM_USER_PASSWORD)
	@RestValidateString(maxLen = 128, minLen = 0)
	private String winRmPassword;

	public AccessInfoRequest() {
	}

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
	public void correlationCheck() throws InvalidSetting {
	}

	@Override
	public String toString() {
		return "AccessInfoRequest [facilityId=" + facilityId + ", moduleId=" + moduleId + ", sshUser=" + sshUser
				+ ", sshPassword=" + sshPassword + ", sshPrivateKeyFilepath=" + sshPrivateKeyFilepath
				+ ", sshPrivateKeyPassphrase=" + sshPrivateKeyPassphrase + ", winRmUser=" + winRmUser
				+ ", winRmPassword=" + winRmPassword + "]";
	}
}
