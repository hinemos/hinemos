/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;

public class AddMailNotifyRequest extends AbstractAddNotifyRequest {
	private MailNotifyDetailInfoRequest notifyMailInfo;

	public AddMailNotifyRequest() {
	}

	public MailNotifyDetailInfoRequest getNotifyMailInfo() {
		return notifyMailInfo;
	}

	public void setNotifyMailInfo(MailNotifyDetailInfoRequest notifyMailInfo) {
		this.notifyMailInfo = notifyMailInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
