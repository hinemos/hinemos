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

import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.NotNull;

/**
 * インスタンス作成要求に必要な情報を保持するクラス。 
 * {@link com.clustercontrol.ws.cloud.CloudEndpoint#addInstance(String,String,CreateInstanceRequest request) addInstance 関数} にて使用される。
 *
 */
public abstract class AddInstanceRequest extends Request {
	private String instanceName;
	private String memo;
	private List<Tag> tags = new ArrayList<Tag>();

	public AddInstanceRequest() {
	}	
	
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}

	@ElementId("XCLOUD_CORE_TAGS")
	@NotNull
	@Into
	public List<Tag> getTags() {
		return tags;
	}
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
}
