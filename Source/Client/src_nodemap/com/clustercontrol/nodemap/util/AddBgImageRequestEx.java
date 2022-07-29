/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.nodemap.util;

import com.clustercontrol.rest.JSON;

public class AddBgImageRequestEx extends org.openapitools.client.model.AddBgImageRequest {
	@Override
	public String toString() {
		return new JSON().serialize(this);
	}
}
