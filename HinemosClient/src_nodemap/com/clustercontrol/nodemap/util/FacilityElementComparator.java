/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.io.Serializable;
import java.util.Comparator;

import org.openapitools.client.model.FacilityElementResponse;

/**
 * FacilityElement用Comparator
 * 
 */
public class FacilityElementComparator implements Comparator<FacilityElementResponse>, Serializable {
	private static final long serialVersionUID = 1L;

	// 順序付けのために 2 つの引数を比較します。
	@Override
	public int compare(FacilityElementResponse element1, FacilityElementResponse element2) {
		return element1.getFacilityId().compareTo(element2.getFacilityId());
	}
}
