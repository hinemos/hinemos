/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.filtersetting.bean.EventFilterConditionInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = EventFilterConditionInfo.class)
public class EventFilterConditionResponse extends EventFilterConditionRequest {

	// Same as the request

}
