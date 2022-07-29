/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.UserCredential;
import com.clustercontrol.xcloud.common.CloudCryptKey;
import com.clustercontrol.xcloud.util.EncryptionUtil;

@Entity
@Table(name="cc_cfg_xcloud_credential_user", schema="setting")
@DiscriminatorValue(UserCredentialEntity.typeName)
public class UserCredentialEntity extends CredentialBaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static class UserCredentialEntityPK implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public String cloudScopeId;
		public String loginUserId;

		public UserCredentialEntityPK() {
		}

		public UserCredentialEntityPK(String cloudScopeId, String loginUserId) {
			this.cloudScopeId = cloudScopeId;
			this.loginUserId = loginUserId;
		}

		public String getScopeId() {
			return cloudScopeId;
		}
		public void setScopeId(String cloudScopeId) {
			this.cloudScopeId = cloudScopeId;
		}

		public String getLoginUserId() {
			return loginUserId;
		}
		public void setLoginUserId(String loginUserId) {
			this.loginUserId = loginUserId;
		}
	}

	public static final String typeName = "User";

	private String user;
	private String password;
	private String passwordCrypt;

	public UserCredentialEntity() {
	}

	public UserCredentialEntity(String loginUserId, String cloudScopeId, String user, String password) {
		super(loginUserId, cloudScopeId);
		this.user = user;
		this.password = password;
	}

	@Column(name="user_name")
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

	@Transient
	public String getPassword()
	{
		if (this.password == null) {
			try {
				this.password = this.passwordCrypt != null ?
						EncryptionUtil.decrypt(this.passwordCrypt, CloudCryptKey.cryptKey): null;
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
				// throw new InternalManagerError(e.getMessage(), e);
			}
		}
		return this.password;
	}
	public void setPassword( String password )
	{
		try {
			setPasswordCrypt(password != null ? EncryptionUtil.crypt(password, CloudCryptKey.cryptKey): null);
			this.password = password;
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			// throw new InternalManagerError(e.getMessage(), e);
		}
	}

	@Column(name="password")
	public String getPasswordCrypt()
	{
		return this.passwordCrypt;
	}
	public void setPasswordCrypt( String passwordCrypt )
	{
		this.passwordCrypt = passwordCrypt;
		this.password = null;
	}

	@Override
	public String getCredentialType() {
		return UserCredentialEntity.typeName;
	}

	@Override
	public UserCredential convertWebElement() {
		return new UserCredential(getUser(), getPassword());
	}
	
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException {
		visitor.visit(this);
	}

	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
		return transformer.transform(this);
	}
}
