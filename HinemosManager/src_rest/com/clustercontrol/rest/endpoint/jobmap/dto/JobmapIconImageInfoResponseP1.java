/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.jobmap.dto;

import java.util.List;

public class JobmapIconImageInfoResponseP1 {

	public JobmapIconImageInfoResponseP1() {
	}

	private List<String> iconIdList;

	public List<String> getIconIdList() {
		return iconIdList;
	}

	public void setIconIdList(List<String> iconIdList) {
		this.iconIdList = iconIdList;
	}

}
