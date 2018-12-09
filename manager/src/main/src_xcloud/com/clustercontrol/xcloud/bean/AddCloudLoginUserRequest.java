/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.util.AuthorizingUtil;
import com.clustercontrol.xcloud.validation.CustomEntityValidator;
import com.clustercontrol.xcloud.validation.EntityValidator.EntityValidationContext;
import com.clustercontrol.xcloud.validation.ValidationConstants;
import com.clustercontrol.xcloud.validation.annotation.CustomEntityValidation;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;

/**
 * クラウドユーザ作成要求に必要な情報を保持するクラス。 
 * {@link com.clustercontrol.ws.cloud.CloudEndpoint#addCloudUser(AddCloudLoginUserRequest) addCloudUser 関数} にて使用される。
 *
 */
@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
@CustomEntityValidation(AddCloudLoginUserRequest.CloudUserValidator.class)
public class AddCloudLoginUserRequest {
	public static class CloudUserValidator implements CustomEntityValidator<AddCloudLoginUserRequest>, ValidationConstants {
		@Override
		public void validate(final AddCloudLoginUserRequest entity, String group, EntityValidationContext context) throws PluginException {
			if (
				!AuthorizingUtil.checkHinemousUser_administrators_account(Session.current().getHinemosCredential().getUserId(), entity.getCloudScopeId())
				) {
				throw ErrorCode.NEED_ADMINISTRATORS_ROLE_OR_ACCOUNT_USER.cloudManagerFault(Session.current().getHinemosCredential().getUserId(), entity.getCloudScopeId());
			}
			// クラウド側にユーザー情報が存在するか確認。
			CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScope(entity.getCloudScopeId());
			scope.optionExecute(new CloudScopeEntity.OptionExecutor() {
				@Override
				public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
					option.getUserManagement(scope).validCredentialAsUser(entity.getCredential());
				}
			});
		}
	}
	
	private String cloudScopeId;
	private String loginUserId;
	private String userName;
	private String description;
	
	private List<RoleRelation> roleRelations = new ArrayList<>();
	
	private Credential credential;
	
	/**
	 */
	@ElementId("XCLOUD_CORE_CLOUDSCOPE_ID")
	@Size(max = 256)
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	/**
	 */
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	
	/**
	 * クラウドユーザーIDを取得する。
	 * 
	 * @return クラウドユーザーID。
	 */
	@ElementId("XCLOUD_CORE_CLOUDLOGINUSER_ID")
	@NotNull
	@Identity
	public String getLoginUserId() {
		return loginUserId;
	}
	/**
	 * クラウドユーザーIDを設定する。
	 * 
	 * @param XCLOUD_CORE_CLOUDLOGINUSER_ID クラウドユーザーID。
	 */
	public void setLoginUserId(String loginUserId) {
		this.loginUserId = loginUserId;
	}
	
	/**
	 * クラウドユーザー名を取得する。
	 * 
	 * @return クラウドユーザー名。
	 */
	@ElementId("XCLOUD_CORE_CLOUDLOGINUSER_NAME")
	@NotNull
	@Size(max=256)
	public String getUserName() {
		return userName;
	}
	/**
	 * クラウドユーザー名を設定する。
	 * 
	 * @param cloudUserName クラウドユーザー名。
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
	/**
	 * クラウドのシークレットキーを取得する。
	 * 
	 * @return クラウドのシークレットキー。
	 */
	@ElementId("XCLOUD_CORE_CREDENTIAL")
	@NotNull
	public Credential getCredential() {
		return credential;
	}
	/**
	 * クラウドのシークレットキーを設定する。
	 * 
	 * @param secretKey クラウドのシークレットキー。
	 */
	public void setCredential(Credential credential) {
		this.credential = credential;
	}
	
	@ElementId("XCLOUD_CORE_DESCRIPTION")
	@Size(max=256)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@ElementId("XCLOUD_CORE_ROLERELATIONS")
	@NotNull
	public List<RoleRelation> getRoleRelations() {
		return roleRelations;
	}
	public void setRoleRelations(List<RoleRelation> roleRelations) {
		this.roleRelations = roleRelations;
	}
}
