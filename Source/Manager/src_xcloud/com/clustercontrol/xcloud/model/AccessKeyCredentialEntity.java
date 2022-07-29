/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.AccessKeyCredential;
import com.clustercontrol.xcloud.common.CloudCryptKey;
import com.clustercontrol.xcloud.util.EncryptionUtil;

@Entity
@Table(name="cc_cfg_xcloud_credential_accesskey", schema="setting")
@DiscriminatorValue(AccessKeyCredentialEntity.typeName)
public class AccessKeyCredentialEntity extends CredentialBaseEntity {
	public static final String typeName = "AccessKey";

	private String accessKey;
	private String secretKey;
	private String secretKeyCrypt;

	public AccessKeyCredentialEntity() {
	}

	public AccessKeyCredentialEntity(String loginUserId, String cloudScopeId, String accessKey, String secretKey) {
		super(loginUserId, cloudScopeId);
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	@Column(name="access_key")
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	@Transient
	public String getSecretKey()
	{
		if (this.secretKey == null) {
			try {
				this.secretKey = this.secretKeyCrypt != null ?
						EncryptionUtil.decrypt(this.secretKeyCrypt, CloudCryptKey.cryptKey): null;
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
//				throw new InternalManagerError(e.getMessage(), e);
			}
		}
		return this.secretKey;
	}
	public void setSecretKey( String secretKey )
	{
		try {
			setSecretKeyCrypt(secretKey != null ? EncryptionUtil.crypt(secretKey, CloudCryptKey.cryptKey): null);
			this.secretKey = secretKey;
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
//			throw new InternalManagerError(e.getMessage(), e);
		}
	}

	@Column(name="secret_key")
	public String getSecretKeyCrypt()
	{
		return this.secretKeyCrypt;
	}
	public void setSecretKeyCrypt( String secretKeyCrypt )
	{
		this.secretKeyCrypt = secretKeyCrypt;
		this.secretKey = null;
	}

	@Override
	public String getCredentialType() {
		return typeName;
	}

	@Override
	public AccessKeyCredential convertWebElement() {
		return new AccessKeyCredential(getAccessKey(), getSecretKey());
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
