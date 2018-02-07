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

import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.NotNullContainer;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class ModifyInstanceRequest extends Request {
	private String instanceId;
	private String memo;
	private List<Tag> tags = new ArrayList<Tag>();

	public ModifyInstanceRequest() {
	}	
	
	@ElementId("XCLOUD_CORE_INSTANCE_ID")
	@Identity
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	
	@ElementId("XCLOUD_CORE_TAGS")
	@NotNull
	@NotNullContainer
	@Into
	public List<Tag> getTags() {
		return tags;
	}
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
}
