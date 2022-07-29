/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.proxy;

import java.util.ArrayList;

import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobLinkExpInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageResponse;
import com.clustercontrol.rest.proxy.util.JobRestClientWrapper;

/**
 * 他マネージャのジョブ機能REST APIにアクセスするためのService
 *
 */
public class JobRestEndpointsProxyServiceImpl implements JobRestEndpointsProxyService {

	@Override
	public RegistJobLinkMessageResponse registJobLinkMessage(RegistJobLinkMessageRequest request) {

		// 戻り値
		RegistJobLinkMessageResponse response = new RegistJobLinkMessageResponse();

		// request作成
		org.openapitools.client.model.RegistJobLinkMessageRequest restRequest
			= new org.openapitools.client.model.RegistJobLinkMessageRequest();
		restRequest.setJoblinkMessageId(request.getJoblinkMessageId());
		restRequest.setSourceIpAddressList(request.getSourceIpAddressList());
		restRequest.setSendDate(request.getSendDate());
		restRequest.setMonitorDetailId(request.getMonitorDetailId());
		restRequest.setApplication(request.getApplication());
		restRequest.setPriority(
				org.openapitools.client.model.RegistJobLinkMessageRequest.PriorityEnum.valueOf(request.getPriority().name()));
		restRequest.setMessage(request.getMessage());
		restRequest.setMessageOrg(request.getMessageOrg());
		if (request.getJobLinkExpInfoList() != null
				&& request.getJobLinkExpInfoList().size() > 0) {
			restRequest.setJobLinkExpInfoList(new ArrayList<org.openapitools.client.model.JobLinkExpInfoRequest>());
			for (JobLinkExpInfoRequest expRequest : request.getJobLinkExpInfoList()) {
				org.openapitools.client.model.JobLinkExpInfoRequest expRestRequest
					= new org.openapitools.client.model.JobLinkExpInfoRequest();
				expRestRequest.setKey(expRequest.getKey());
				expRestRequest.setValue(expRequest.getValue());
				restRequest.getJobLinkExpInfoList().add(expRestRequest);
			}
		}
		// 実行
		try {
			JobRestClientWrapper wrapper = new JobRestClientWrapper(request.getJoblinkSendSettingId(), request.getFacilityId());
			org.openapitools.client.model.RegistJobLinkMessageResponse restResponse
				= wrapper.registJobLinkMessage(restRequest);
	
			// response作成
			response.setAcceptDate(restResponse.getAcceptDate());
			response.setResult(restResponse.getResult());
		} catch (Exception e) {
			response.setResultDetail(e.getMessage());
			response.setResult(false);
		}
		return response;
	}
}
