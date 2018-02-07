/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class AccessKeyCredential extends Credential {
	private String accessKey;
	private String secretKey;

	public AccessKeyCredential() {
	}
	
	public AccessKeyCredential(String accessKey, String secretKey) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}
	
	@ElementId("accessKey")
	@Size(max = 1024)
	@NotNull
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	
	@ElementId("secretKey")
	@Size(max = 8192)
	@NotNull
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey)  {
		this.secretKey = secretKey;
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
		AccessKeyCredential other = (AccessKeyCredential) obj;
		if (accessKey == null) {
			if (other.accessKey != null)
				return false;
		} else if (!accessKey.equals(other.accessKey))
			return false;
		return true;
	}
}
