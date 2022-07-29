/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class AddJobNotifyRequest extends AbstractAddNotifyRequest {

	private JobNotifyDetailInfoRequest notifyJobInfo;

	public AddJobNotifyRequest() {
	}

	public JobNotifyDetailInfoRequest getNotifyJobInfo() {
		return notifyJobInfo;
	}

	public void setNotifyJobInfo(JobNotifyDetailInfoRequest notifyJobInfo) {
		this.notifyJobInfo = notifyJobInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
