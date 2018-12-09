/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.IPrivateCloudOption;
import com.clustercontrol.xcloud.factory.IPublicCloudOption;
import com.clustercontrol.xcloud.factory.IUserManagement;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.PrivateCloudScopeEntity;
import com.clustercontrol.xcloud.model.PublicCloudScopeEntity;
import com.clustercontrol.xcloud.util.AuthorizingUtil;
import com.clustercontrol.xcloud.validation.CustomEntityValidator;
import com.clustercontrol.xcloud.validation.EntityValidator.EntityValidationContext;
import com.clustercontrol.xcloud.validation.ValidationConstants;
import com.clustercontrol.xcloud.validation.annotation.CustomEntityValidation;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.Size;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
@CustomEntityValidation(ModifyCloudLoginUserRequest.CloudUserValidator.class)
public class ModifyCloudLoginUserRequest extends Request {
	public static class CloudUserValidator implements CustomEntityValidator<ModifyCloudLoginUserRequest>, ValidationConstants {
		@Override
		public void validate(final ModifyCloudLoginUserRequest entity, String group, EntityValidationContext context) throws CloudManagerException {
			if (
				!AuthorizingUtil.checkHinemousUser_administrators_account_self(Session.current().getHinemosCredential().getUserId(), entity.getCloudScopeId(), entity.getLoginUserId())
				) {
				throw ErrorCode.NEED_ADMINISTRATORS_ROLE_OR_ACCOUT_USER_OR_SELF.cloudManagerFault(Session.current().getHinemosCredential().getUserId(), entity.getCloudScopeId(), entity.getLoginUserId());
			}

			if (entity.getCredential() != null) {
				CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getCloudLoginUser(entity.getCloudScopeId(), entity.getLoginUserId());
				switch (user.getCloudUserType()) {
				case account:
					user.getCloudScope().optionExecuteEx(new CloudScopeEntity.OptionExecutorEx() {
						@Override
						public void execute(PublicCloudScopeEntity scope, IPublicCloudOption option) throws CloudManagerException {
							option.validCredentialAsAccount(entity.getCredential());
						}
						@Override
						public void execute(PrivateCloudScopeEntity scope, IPrivateCloudOption option) throws CloudManagerException {
							option.validCredentialEntityAsAccount(entity.getCredential(), new ArrayList<>(scope.getPrivateLocations().values()));
						}
					});
					break;
				case user:
					user.getCloudScope().optionExecute(new CloudScopeEntity.OptionExecutor() {
						@Override
						public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
							IUserManagement userManager = option.getUserManagement(scope);
							userManager.validCredentialAsUser(entity.getCredential());
						}
					});
					break;
				}
			}
		}
	}

	private String cloudScopeId;
	private String loginUserId;
	private String userName;
	private String description;
	
	private Credential credential;

	@ElementId("XCLOUD_CORE_CLOUDSCOPE_ID")
	@Identity
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	
	@ElementId("XCLOUD_CORE_CLOUDLOGINUSER_ID")
	@Identity
	public String getLoginUserId() {
		return loginUserId;
	}
	public void setLoginUserId(String loginUserId) {
		this.loginUserId = loginUserId;
	}

	@ElementId("XCLOUD_CORE_CLOUDLOGINUSER_NAME")
	@Size(max = 256)
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	@ElementId("XCLOUD_CORE_DESCRIPTION")
	@Size(max = 256)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@ElementId("XCLOUD_CORE_CREDENTIAL")
	@Into
	public Credential getCredential() {
		return credential;
	}
	public void setCredential(Credential credential) {
		this.credential = credential;
	}
}
