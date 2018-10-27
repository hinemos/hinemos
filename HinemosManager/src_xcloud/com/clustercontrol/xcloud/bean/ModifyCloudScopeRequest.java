/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.validation.CustomEntityValidator;
import com.clustercontrol.xcloud.validation.EntityValidator.EntityValidationContext;
import com.clustercontrol.xcloud.validation.ValidationConstants;
import com.clustercontrol.xcloud.validation.annotation.CustomEntityValidation;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.Size;


@XmlSeeAlso({ModifyPrivateCloudScopeRequest.class, ModifyPublicCloudScopeRequest.class})
@CustomEntityValidation(ModifyCloudScopeRequest.ModifyCloudScopeValidator.class)
public abstract class ModifyCloudScopeRequest extends Request {
	public interface IVisitor {
		void visit(ModifyPublicCloudScopeRequest request) throws CloudManagerException, InvalidRole;
		void visit(ModifyPrivateCloudScopeRequest request) throws CloudManagerException, InvalidRole;
	}
	public interface ITransformer<T> {
		T transform(ModifyPublicCloudScopeRequest request) throws CloudManagerException, InvalidRole;
		T transform(ModifyPrivateCloudScopeRequest request) throws CloudManagerException, InvalidRole;
	}
	
	public static class ModifyCloudScopeValidator implements CustomEntityValidator<ModifyCloudScopeRequest>, ValidationConstants {
		@Override
		public void validate(final ModifyCloudScopeRequest entity, String group, EntityValidationContext context) throws PluginException {
			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<CloudScopeEntity> query = em.createNamedQuery("findCloudScopeByHinemosUserAsAdmin", CloudScopeEntity.class);
			query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
			query.setParameter("cloudScopeId", entity.getCloudScopeId());
			query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
			query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
			
			try {
				query.getSingleResult();
			}
			catch (NoResultException e) {
				throw ErrorCode.NEED_ADMINISTRATORS_ROLE_OR_ACCOUNT_USER.cloudManagerFault(Session.current().getHinemosCredential().getUserId(), entity.getCloudScopeId());
			}
		}
	}
	
	private String cloudScopeId;
	private String scopeName;
	private String description;

	public ModifyCloudScopeRequest() {
	}	

	@ElementId("XCLOUD_CORE_CLOUDSCOPE_ID")
	@Identity
	public String getCloudScopeId() {
		return cloudScopeId;
	}

	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	
	@ElementId("XCLOUD_CORE_CLOUDSCOPE_NAME")
	@Size(max=128)
	public String getScopeName() {
		return scopeName;
	}
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	@ElementId("XCLOUD_CORE_DESCRIPTION")
	@Size(max=256)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public abstract void visit(IVisitor visitor) throws CloudManagerException, InvalidRole;

	public abstract <T> T transform(ITransformer<T> transformer) throws CloudManagerException, InvalidRole;
}
