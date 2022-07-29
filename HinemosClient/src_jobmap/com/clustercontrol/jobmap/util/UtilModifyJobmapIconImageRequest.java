/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmap.util;

import com.clustercontrol.rest.JSON;

public class UtilModifyJobmapIconImageRequest extends org.openapitools.client.model.ModifyJobmapIconImageRequest {

	@Override
	public String toString() {
		return new JSON().serialize(this);
	}

}
