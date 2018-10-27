/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.bean.Credential;

@Entity
@Table(name="cc_cfg_xcloud_credential_base", schema="setting")
@DiscriminatorColumn(name="credential_type")
@Inheritance(strategy=InheritanceType.JOINED)
@IdClass(CredentialBaseEntity.CredentialBaseEntityPK.class)
public abstract class CredentialBaseEntity {
	public static class CredentialBaseEntityPK {
		private String cloudScopeId;
		private String loginUserId;

		public CredentialBaseEntityPK() {
		}

		public CredentialBaseEntityPK(String cloudScopeId, String loginUserId) {
			this.cloudScopeId = cloudScopeId;
			this.loginUserId = loginUserId;
		}

		public String getCloudScopeId() {
			return cloudScopeId;
		}
		public void setCloudScopeId(String cloudScopeId) {
			this.cloudScopeId = cloudScopeId;
		}

		public String getLoginUserId() {
			return loginUserId;
		}
		public void setLoginUserId(String loginUserId) {
			this.loginUserId = loginUserId;
		}
	}

	
	public interface IVisitor {
		void visit(AccessKeyCredentialEntity credential) throws CloudManagerException;
		void visit(UserCredentialEntity credential) throws CloudManagerException;
	}
	
	public interface ITransformer<T> {
		T transform(AccessKeyCredentialEntity credential) throws CloudManagerException;
		T transform(UserCredentialEntity credential) throws CloudManagerException;
	}
	
	public static class Visitor implements IVisitor {
		@Override
		public void visit(AccessKeyCredentialEntity credential) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public void visit(UserCredentialEntity credential) throws CloudManagerException {
			throw new InternalManagerError();
		}
	}

	public static class Transformer<T> implements ITransformer<T> {
		@Override
		public T transform(AccessKeyCredentialEntity credential) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public T transform(UserCredentialEntity credential) throws CloudManagerException {
			throw new InternalManagerError();
		}
	}
	
    private String loginUserId;
    private String cloudScopeId;
    
    public CredentialBaseEntity() {
    	super();
	}    

    public CredentialBaseEntity(String loginUserId, String cloudScopeId) {
    	super();
    	// 自動生成なので省略。
		this.loginUserId = loginUserId;
		this.cloudScopeId = cloudScopeId;
	}    
	
	@Id
	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	@Id
	@Column(name="cloud_login_user_id")
	public String getLoginUserId() {
		return loginUserId;
	}
	public void setLoginUserId(String loginUserId) {
		this.loginUserId = loginUserId;
	}
	
	public abstract String getCredentialType();
	
	public abstract Credential convertWebElement();
	
	public abstract void visit(IVisitor visitor) throws CloudManagerException;
	
	public abstract <T> T transform(ITransformer<T> transformer) throws CloudManagerException;
}
