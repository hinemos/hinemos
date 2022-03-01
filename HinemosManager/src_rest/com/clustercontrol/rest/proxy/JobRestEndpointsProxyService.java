/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.proxy;

import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageResponse;

/**
 * 他マネージャのジョブ機能REST APIにアクセスするためのServiceインタフェース
 *
 */
public interface JobRestEndpointsProxyService {

	public RegistJobLinkMessageResponse registJobLinkMessage(RegistJobLinkMessageRequest arg);
}
