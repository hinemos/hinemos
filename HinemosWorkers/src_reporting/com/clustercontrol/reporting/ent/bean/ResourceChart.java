/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource chart class
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class ResourceChart {

	private String subTitle;
	private List<DataKey> itemList = new ArrayList<>();

	public ResourceChart(String subTitle) {
		this.subTitle = subTitle;
	}

	public void appendItem(DataKey dataKey){
		itemList.add(dataKey);
	}

	public List<DataKey> getItems(){
		return this.itemList;
	}

	public int count(){
		return itemList.size();
	}

	public String getSubTitle(){
		return this.subTitle;
	}
}
