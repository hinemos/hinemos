/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent;

import com.clustercontrol.rest.JSON;

/**
 * スクリーンショットアップロード用DTOクラス
 */
public class ForwardRpaScreenshotJsonRequest extends org.openapitools.client.model.ForwardRpaScreenshotRequest {

	@Override
	public String toString() {
		return new JSON().serialize(this);
	}
}
