/*
 * Copyright (c) 2024 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import javax.xml.bind.annotation.XmlRootElement;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;

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
		return equals(obj);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jsonCredentialInfo == null) ? 0 : jsonCredentialInfo.hashCode());
		result = prime * result + ((platform == null) ? 0 : platform.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericCredential other = (GenericCredential) obj;
		if (jsonCredentialInfo == null) {
			if (other.jsonCredentialInfo != null)
				return false;
		} else if (!jsonCredentialInfo.equals(other.jsonCredentialInfo))
			return false;
		if (platform == null) {
			if (other.platform != null)
				return false;
		} else if (!platform.equals(other.platform))
			return false;
		return true;
	}
}
