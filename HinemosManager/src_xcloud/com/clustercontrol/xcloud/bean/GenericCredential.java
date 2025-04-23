/*
 * Copyright (c) 2024 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@XmlRootElement(namespace = "http://xcloud.ws.clustercontrol.com")
public class GenericCredential extends Credential {
	private String platform;
	private String jsonCredentialInfo;

	public GenericCredential() {
	}

	public GenericCredential(String platform, String jsonCredentialInfo) {
		this.platform = platform;
		this.jsonCredentialInfo = jsonCredentialInfo;
	}

	@ElementId("platform")
	@Size(max = 64)
	@NotNull
	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	@ElementId("jsonCredentialInfo")
	@NotNull
	public String getJsonCredentialInfo() {
		return jsonCredentialInfo;
	}

	public void setJsonCredentialInfo(String jsonCredentialInfo) {
		this.jsonCredentialInfo = jsonCredentialInfo;
	}

	@Override
	public void visit(IVisitor visitor) throws CloudManagerException {
		visitor.visit(this);
	}

	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
		return transformer.transform(this);
	}

	@Override
	public boolean match(Credential obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericCredential other = (GenericCredential) obj;
		HashMap<String, String> jsonCredentialInfoMap = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		try {
			jsonCredentialInfoMap = om.readValue(jsonCredentialInfo, new TypeReference<HashMap<String, String>>() {
			});
		} catch (JsonProcessingException e) {
			Logger.getLogger(this.getClass()).error("match():Unable to parse JsonString to Map" + e.getMessage());
		}
		if (jsonCredentialInfoMap.get("PrivateKeyFileName").isEmpty()) {
			if (other.jsonCredentialInfo != null)
				return false;
		} else if (!jsonCredentialInfo.equals(other.jsonCredentialInfo))
			return false;
		return true;
	}
}
