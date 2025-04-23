/*
 * Copyright (c) 2024 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.GenericCredential;
import com.clustercontrol.xcloud.common.CloudCryptKey;
import com.clustercontrol.xcloud.util.EncryptionUtil;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "cc_cfg_xcloud_credential_generic", schema = "setting")
@DiscriminatorValue(GenericCredentialEntity.typeName)
public class GenericCredentialEntity extends CredentialBaseEntity {
	public static final String typeName = "Generic";

	private String platform;
	private String jsonCredentialInfo;
	private String jsonCredentialInfoCrypt;

	public GenericCredentialEntity() {
	}

	public GenericCredentialEntity(String loginUserId, String cloudScopeId, String platform,
			String jsonCredentialInfoCrypt) {
		super(loginUserId, cloudScopeId);
		this.platform = platform;
		this.jsonCredentialInfoCrypt = jsonCredentialInfoCrypt;
	}

	@Column(name = "platform")
	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	@Transient
	public String getJsonCredentialInfo() {
		if (this.jsonCredentialInfo == null) {
			try {
				this.jsonCredentialInfo = this.jsonCredentialInfoCrypt != null
						? EncryptionUtil.decrypt(this.jsonCredentialInfoCrypt, CloudCryptKey.cryptKey) : null;
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
		return this.jsonCredentialInfo;
	}

	public void setJsonCredentialInfo(String jsonCredentialInfo) {
		try {
			setJsonCredentialInfoCrypt(jsonCredentialInfo != null
					? EncryptionUtil.crypt(jsonCredentialInfo, CloudCryptKey.cryptKey) : null);
			this.jsonCredentialInfo = jsonCredentialInfo;
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}

	@Column(name = "json_credential_info")
	public String getJsonCredentialInfoCrypt() {
		return this.jsonCredentialInfoCrypt;
	}

	public void setJsonCredentialInfoCrypt(String jsonCredentialInfoCrypt) {
		this.jsonCredentialInfoCrypt = jsonCredentialInfoCrypt;
		this.jsonCredentialInfo = null;
	}

	@Override
	public String getCredentialType() {
		return typeName;
	}

	@Override
	public GenericCredential convertWebElement() {
		return new GenericCredential(getPlatform(), getJsonCredentialInfo());
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
