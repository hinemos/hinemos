/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import org.openapitools.client.model.AddFileCheckRequest;
import org.openapitools.client.model.AddJobLinkRcvRequest;
import org.openapitools.client.model.AddScheduleRequest;
import org.openapitools.client.model.AddJobManualRequest;
import org.openapitools.client.model.JobKickResponse;
import org.openapitools.client.model.ModifyScheduleRequest;
import org.openapitools.client.model.ModifyFileCheckRequest;
import org.openapitools.client.model.ModifyJobLinkRcvRequest;
import org.openapitools.client.model.ModifyJobManualRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.RestClientBeanUtil;

public class JobKickBeanUtil {

	public static AddScheduleRequest convertToAddScheduleRequest(JobKickResponse response)
			throws HinemosUnknown {
		AddScheduleRequest request = new AddScheduleRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

	public static AddFileCheckRequest convertToAddFileCheckRequest(JobKickResponse response)
			throws HinemosUnknown {
		AddFileCheckRequest request = new AddFileCheckRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

	public static AddJobLinkRcvRequest convertToAddJobLinkRcvRequest(JobKickResponse response)
			throws HinemosUnknown {
		AddJobLinkRcvRequest request = new AddJobLinkRcvRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

	public static AddJobManualRequest convertToAddJobManualRequest(JobKickResponse response)
			throws HinemosUnknown {
		AddJobManualRequest request = new AddJobManualRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

	public static ModifyScheduleRequest convertToModifyScheduleRequest(JobKickResponse response)
			throws HinemosUnknown {
		ModifyScheduleRequest request = new ModifyScheduleRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

	public static ModifyFileCheckRequest convertToModifyFileCheckRequest(JobKickResponse response)
			throws HinemosUnknown {
		ModifyFileCheckRequest request = new ModifyFileCheckRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

	public static ModifyJobLinkRcvRequest convertToModifyJobLinkRcvRequest(JobKickResponse response)
			throws HinemosUnknown {
		ModifyJobLinkRcvRequest request = new ModifyJobLinkRcvRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}

	public static ModifyJobManualRequest convertToModifyJobManualRequest(JobKickResponse response)
			throws HinemosUnknown {
		ModifyJobManualRequest request = new ModifyJobManualRequest();
		RestClientBeanUtil.convertBean(response, request);

		return request;
	}
}
