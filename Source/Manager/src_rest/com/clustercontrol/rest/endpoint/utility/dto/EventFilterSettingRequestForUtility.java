/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import com.clustercontrol.rest.endpoint.filtersetting.dto.EventFilterSettingResponse;

//クライアント側で持ち回りに使っている EventFilterSettingResponseをそのまま取得する
public class EventFilterSettingRequestForUtility extends EventFilterSettingResponse {
	
	public EventFilterSettingRequestForUtility(){
	}
}
