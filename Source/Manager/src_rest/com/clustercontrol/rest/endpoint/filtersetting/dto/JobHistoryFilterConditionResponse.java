/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.filtersetting.bean.JobHistoryFilterConditionInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = JobHistoryFilterConditionInfo.class)
public class JobHistoryFilterConditionResponse extends JobHistoryFilterConditionRequest {

	// Same as request

}
