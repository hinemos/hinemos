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
import javax.xml.bind.annotation.XmlSeeAlso;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.model.CloudPlatformEntity;
import com.clustercontrol.xcloud.validation.CustomEntityValidator;
import com.clustercontrol.xcloud.validation.EntityValidator.EntityValidationContext;
import com.clustercontrol.xcloud.validation.ValidationConstants;
import com.clustercontrol.xcloud.validation.annotation.CustomEntityValidation;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;

@CustomEntityValidation(AddCloudScopeRequest.PlatformValidator.class)
@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com")
@XmlSeeAlso({AddPublicCloudScopeRequest.class, AddPrivateCloudScopeRequest.class})
public abstract class AddCloudScopeRequest extends Request {
	public static class PlatformValidator implements CustomEntityValidator<AddCloudScopeRequest>, ValidationConstants {
		@Override
		public void validate(final AddCloudScopeRequest entity, String group, EntityValidationContext context) throws PluginException {
			CloudPlatformEntity platform = Session.current().getEntityManager().find(CloudPlatformEntity.class, entity.getPlatformId(), ObjectPrivilegeMode.READ);
			if (platform == null)
				throw ErrorCode.CLOUD_PLATFORM_NOT_FOUND.cloudManagerFault(entity.getPlatformId());
		}
	}
	
	public interface IVisitor {
		void visit(AddPublicCloudScopeRequest request) throws CloudManagerException, InvalidRole;
		void visit(AddPrivateCloudScopeRequest request) throws CloudManagerException, InvalidRole;
	}
	public interface ITransformer<T> {
		T transform(AddPublicCloudScopeRequest request) throws CloudManagerException, InvalidRole;
		T transform(AddPrivateCloudScopeRequest request) throws CloudManagerException, InvalidRole;
	}
	
	public static class Account {
		private String loginUserId;
		private String userName;
		private String description;
		
		private Credential credential;
		
		private List<RoleRelation> roleRelations = new ArrayList<>();
		
		@ElementId("XCLOUD_CORE_CLOUDLOGINUSER_ID")
		@Size(max=128)
		@NotNull
		public String getLoginUserId() {
			return loginUserId;
		}
		public void setLoginUserId(String userId) {
			this.loginUserId = userId;
		}
		@ElementId("XCLOUD_CORE_CLOUDLOGINUSER_NAME")
		@Size(max=128)
		@NotNull
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		
		@ElementId("XCLOUD_CORE_CREDENTIAL")
		@NotNull
		@Into
		public Credential getCredential() {
			return credential;
		}
		public void setCredential(Credential credential) {
			this.credential = credential;
		}
		
		@ElementId("XCLOUD_CORE_ROLERELATIONS")
		@Identity
		public List<RoleRelation> getRoleRelations() {
			return roleRelations;
		}
		public void setRoleRelations(List<RoleRelation> roleRelations) {
			this.roleRelations = roleRelations;
		}
		
		@ElementId("XCLOUD_CORE_DESCRIPTION")
		@Size(max=256)
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
	}
	
	private String platformId;
	private String cloudScopeId;
	private String scopeName;
	private String ownerRoleId;
	private String description;
	
	private Account account;

	public AddCloudScopeRequest() {
	}	

	@ElementId("XCLOUD_CORE_CLOUDPLATFORM_ID")
	@Identity
	public String getPlatformId() {
		return platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	@ElementId("XCLOUD_CORE_CLOUDSCOPE_ID")
	@Identity
	@NotNull
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	@ElementId("XCLOUD_CORE_CLOUDSCOPE_NAME")
	@Size(max=256)
	@NotNull
	public String getScopeName() {
		return scopeName;
	}
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	@ElementId("XCLOUD_CORE_OWNERROLE_ID")
	@Identity
	@NotNull
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@ElementId("XCLOUD_CORE_DESCRIPTION")
	@Size(max=256)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@ElementId("XCLOUD_CORE_CLOUDSCOPE_ACCOUNT")
	@NotNull
	@Into
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	
	public abstract void visit(IVisitor visitor) throws CloudManagerException, InvalidRole;

	public abstract <T> T transform(ITransformer<T> transformer) throws CloudManagerException, InvalidRole;
}
