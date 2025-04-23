/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.bean.AccessKeyCredential;
import com.clustercontrol.xcloud.bean.Credential;
import com.clustercontrol.xcloud.bean.GenericCredential;
import com.clustercontrol.xcloud.bean.UserCredential;

public class ModifyCloudLoginUserRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDLOGINUSER_NAME)
	@RestValidateString(notNull = true, maxLen = 256)
	private String userName;
	@RestItemName(MessageConstant.XCLOUD_CORE_DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(MessageConstant.XCLOUD_CORE_ROLERELATIONS)
	@RestValidateObject(notNull = true)
	private List<RoleRelationRequest> roleRelations = new ArrayList<>();

	@RestItemName(MessageConstant.XCLOUD_CORE_ACCESSKEY)
	@RestValidateString(maxLen = 1024)
	private String accessKey;
	@RestItemName(MessageConstant.XCLOUD_CORE_SECRETKEY)
	@RestValidateString(maxLen = 8192)
	private String secretKey;
	private String user;
	private String password;
	// newly added fields
	@RestItemName(MessageConstant.XCLOUD_CORE_PLATFORM)
	@RestValidateString(maxLen = 1024)
	private String platform;// This field will be null for AWS and AZURE
	@RestItemName(MessageConstant.XCLOUD_CORE_JSONCREDENTIALINFO)
	private String jsonCredentialInfo;

	/**
	 * DTO変換用のメンバ変数。リクエスト入力値としては使用しない。 実態はアカウント情報({@link #getCredential()}
	 */
	private Credential credential;
	private Boolean isPublic;

	public ModifyCloudLoginUserRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (isPublic && platform == null) {
			if (accessKey == null && secretKey == null) {
				throw new InvalidSetting("set accessKey and secretKey.");
			}
		} else {
			if ((user == null || password == null) && platform == null) {
				throw new InvalidSetting("set user and password.");
			}
		}
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<RoleRelationRequest> getRoleRelations() {
		return roleRelations;
	}

	public void setRoleRelations(List<RoleRelationRequest> roleRelations) {
		this.roleRelations = roleRelations;
	}

	public Credential getCredential() {
		if (isPublic && platform == null) {
			credential = new AccessKeyCredential(accessKey, secretKey);
		} else if (isPublic && platform != null) {
			credential = new GenericCredential(platform, jsonCredentialInfo);
		} else {
			credential = new UserCredential(user, password);
		}
		return credential;
	}

	public void setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getJsonCredentialInfo() {
		return jsonCredentialInfo;
	}

	public void setJsonCredentialInfo(String jsonCredentialInfo) {
		this.jsonCredentialInfo = jsonCredentialInfo;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}
}
