/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.mail.util;

import org.openapitools.client.model.AddMailTemplateRequest;
import org.openapitools.client.model.MailTemplateInfoResponse;
import org.openapitools.client.model.ModifyMailTemplateRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.RestClientBeanUtil;

public class MailBeanUtil {

	public static AddMailTemplateRequest convertToAddMailTemplateRequest(MailTemplateInfoResponse response)
			throws HinemosUnknown {
		AddMailTemplateRequest request = new AddMailTemplateRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

	public static ModifyMailTemplateRequest convertToModifyMailTemplateRequest(MailTemplateInfoResponse response)
			throws HinemosUnknown {
		ModifyMailTemplateRequest request = new ModifyMailTemplateRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

}
