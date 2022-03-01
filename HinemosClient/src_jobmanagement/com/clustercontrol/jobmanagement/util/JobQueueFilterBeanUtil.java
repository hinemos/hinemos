/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import org.openapitools.client.model.GetJobQueueActivityInfoRequest;
import org.openapitools.client.model.GetJobQueueListSearchRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.JobQueueActivityViewFilter;
import com.clustercontrol.jobmanagement.bean.JobQueueSettingViewFilter;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.TimezoneUtil;

public class JobQueueFilterBeanUtil {

	public static GetJobQueueListSearchRequest convertToRequest(JobQueueSettingViewFilter queueFilter)
			throws HinemosUnknown {
		GetJobQueueListSearchRequest request = new GetJobQueueListSearchRequest();
		RestClientBeanUtil.convertBean(queueFilter, request);
		if(queueFilter.getRegDateFrom() != null){
			request.setRegDateFrom(TimezoneUtil.getSimpleDateFormat().format(queueFilter.getRegDateFrom()));
		}
		RestClientBeanUtil.convertBean(queueFilter, request);
		if(queueFilter.getRegDateTo() != null){
			request.setRegDateTo(TimezoneUtil.getSimpleDateFormat().format(queueFilter.getRegDateTo()));
		}
		RestClientBeanUtil.convertBean(queueFilter, request);
		if(queueFilter.getUpdateDateFrom() != null){
			request.setUpdateDateFrom(TimezoneUtil.getSimpleDateFormat().format(queueFilter.getUpdateDateFrom()));
		}
		RestClientBeanUtil.convertBean(queueFilter, request);
		if(queueFilter.getUpdateDateTo() != null){
			request.setUpdateDateTo(TimezoneUtil.getSimpleDateFormat().format(queueFilter.getUpdateDateTo()));
		}
		return request;
	}

	public static GetJobQueueActivityInfoRequest convertToRequest(JobQueueActivityViewFilter queueFilter)
			throws HinemosUnknown {
		GetJobQueueActivityInfoRequest request = new GetJobQueueActivityInfoRequest();
		RestClientBeanUtil.convertBean(queueFilter, request);
		if(queueFilter.getRegDateFrom() != null){
			request.setRegDateFrom(TimezoneUtil.getSimpleDateFormat().format(queueFilter.getRegDateFrom()));
		}
		RestClientBeanUtil.convertBean(queueFilter, request);
		if(queueFilter.getRegDateTo() != null){
			request.setRegDateTo(TimezoneUtil.getSimpleDateFormat().format(queueFilter.getRegDateTo()));
		}
		RestClientBeanUtil.convertBean(queueFilter, request);
		if(queueFilter.getUpdateDateFrom() != null){
			request.setUpdateDateFrom(TimezoneUtil.getSimpleDateFormat().format(queueFilter.getUpdateDateFrom()));
		}
		RestClientBeanUtil.convertBean(queueFilter, request);
		if(queueFilter.getUpdateDateTo() != null){
			request.setUpdateDateTo(TimezoneUtil.getSimpleDateFormat().format(queueFilter.getUpdateDateTo()));
		}
		return request;
	}
}
