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

import com.clustercontrol.commons.util.StringUtil;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.AccessKeyCredential;
import com.clustercontrol.xcloud.bean.Credential;
import com.clustercontrol.xcloud.bean.GenericCredential;
import com.clustercontrol.xcloud.bean.UserCredential;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.model.CredentialBaseEntity;

public class AddCloudLoginUserRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDLOGINUSER_ID)
	@RestValidateString(notNull = true, type = CheckType.ID, minLen = 1, maxLen = 128)
	private String loginUserId;
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDLOGINUSER_NAME)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 256)
	private String userName;
	@RestItemName(MessageConstant.XCLOUD_CORE_DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(MessageConstant.XCLOUD_CORE_ROLERELATIONS)
	@RestValidateObject(notNull = true)
	private List<RoleRelationRequest> roleRelations = new ArrayList<>();
	private Boolean isPublic;

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

	public AddCloudLoginUserRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (platform != null) {
			try {
				
				Class<? extends CredentialBaseEntity> credentialEntityClass = CloudManager.singleton().optionCall(platform, (ICloudOption option) -> option.getCloudSpec().getSupportedCredential());
				switch(credentialEntityClass.getSimpleName()) {
				case "UserCredentialEntity":
					if (StringUtil.isNullOrEmpty(user) || password == null) {
						throw new InvalidSetting("set user and password.");
					}
					break;
				case "AccessKeyCredentialEntity":
					if (StringUtil.isNullOrEmpty(accessKey) && secretKey == null) {
						throw new InvalidSetting("set accessKey and secretKey.");
					}
					break;
				case "GenericCredentialEntity":
					if (StringUtil.isNullOrEmpty(jsonCredentialInfo)) {
						throw new InvalidSetting("set JsonCredentialInfo.");
					}
					break;
				default:
					throw new InvalidSetting(String.format("Invalid credential type. type=%s", credentialEntityClass.getSimpleName()));
				}
			} catch (CloudManagerException e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		} else {
			// The following is the original implementation.
			// Since it does not account for GenericCredential used by private cloud, the above implementation was added.
			if (isPublic) {
				if (StringUtil.isNullOrEmpty(accessKey) && secretKey == null) {
					throw new InvalidSetting("set accessKey and secretKey.");
				}
			} else {
				if (StringUtil.isNullOrEmpty(user) || password == null) {
					throw new InvalidSetting("set user and password.");
				}
			}
		}
	}

	public String getLoginUserId() {
		return loginUserId;
	}

	public void setLoginUserId(String loginUserId) {
		this.loginUserId = loginUserId;
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

	public Credential getCredential() throws CloudManagerException {
		if (platform != null) {
			Class<? extends CredentialBaseEntity> credentialEntityClass = CloudManager.singleton().optionCall(platform, (ICloudOption option) -> option.getCloudSpec().getSupportedCredential());
			switch(credentialEntityClass.getSimpleName()) {
			case "UserCredentialEntity":
				credential = new UserCredential(user, password);
				break;
			case "AccessKeyCredentialEntity":
				credential = new AccessKeyCredential(accessKey, secretKey);
				break;
			case "GenericCredentialEntity":
				credential = new GenericCredential(platform, jsonCredentialInfo);
				break;
			default:
				throw new IllegalStateException(String.format("Invalid credential type. type=%s", credentialEntityClass.getSimpleName()));
			}
		} else {
			// The following is the original implementation.
			// Since it does not account for GenericCredential used by private cloud, the above implementation was added.
			if (isPublic) {
				credential = new AccessKeyCredential(accessKey, secretKey);
			} else {
				credential = new UserCredential(user, password);
			}
		}
		return credential;
	}
	
	public void setPublic(boolean isPublic) {
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

}
