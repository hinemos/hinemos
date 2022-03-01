/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.maintenance.util;

import org.openapitools.client.model.AddHinemosPropertyRequest;
import org.openapitools.client.model.HinemosPropertyResponse;
import org.openapitools.client.model.ModifyHinemosPropertyRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.RestClientBeanUtil;

public class HinemosPropertyBeanUtil {

	public static AddHinemosPropertyRequest convertToAddHinemosPropertyRequest(HinemosPropertyResponse response)
			throws HinemosUnknown {
		AddHinemosPropertyRequest request = new AddHinemosPropertyRequest();
		RestClientBeanUtil.convertBean(response, request);

		// Enumのメンバを個別にセット
		request.setType(AddHinemosPropertyRequest.TypeEnum.fromValue(response.getType().getValue()));

		return request;
	}

	public static ModifyHinemosPropertyRequest convertToModifyHinemosPropertyRequest(HinemosPropertyResponse response)
			throws HinemosUnknown {
		ModifyHinemosPropertyRequest request = new ModifyHinemosPropertyRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

}
