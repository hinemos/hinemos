/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.ws.xcloud.PlatformServiceCondition;
import com.clustercontrol.xcloud.model.base.Element;

public class ServiceCondition extends Element implements IServiceCondition {
	private String id;
	private String name;
	private String status;
	private String detail;
	
	public ServiceCondition() {
	}
	
	@Override
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		internalSetProperty(p.name, name, ()->this.name, (s)->this.name=s);
	}

	@Override
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		internalSetProperty(p.status, status, ()->this.status, (s)->this.status=s);
	}

	@Override
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		internalSetProperty(p.detail, detail, ()->this.detail, (s)->this.detail=s);
	}
	
	public void update(PlatformServiceCondition condition) {
		setId(condition.getId());
		setName(condition.getServiceName());
		setStatus(condition.getStatus().name());
		setDetail(condition.getDetail());
	}
	
	public static ServiceCondition convert(PlatformServiceCondition source) {
		ServiceCondition endpoint = new ServiceCondition();
		endpoint.update(source);
		return endpoint;
	}
}
