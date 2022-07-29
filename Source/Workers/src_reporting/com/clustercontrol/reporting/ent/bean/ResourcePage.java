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
 * Resource page class
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class ResourcePage {
	private int maxLine;

	private List<ResourceChart> chartList = new ArrayList<>();
	private ResourceChart lastChart = null;

	public ResourcePage(int maxLine){
		this.maxLine = maxLine;
	}

	public void appendItem(DataKey dataKey, String subTitle){
		if(null == this.lastChart){
			this.lastChart = new ResourceChart(subTitle);
			this.chartList.add(this.lastChart);
		}
		this.lastChart.appendItem(dataKey);

		// Check if page is full
		if( maxLine == this.lastChart.count()){
			// Set null here will create new chart in next round
			this.lastChart = null;
		}
	}

	/**
	 * Create a new chart on current page
	 * 
	 */
	public void createNewChart(){
		// All you have to do is just set lastPage to null and new ResourcePage() will be added in the next round of appendItem()
		this.lastChart = null;
	}

	public List<ResourceChart> getChartList(){
		return this.chartList;
	}

	public int count(){
		return this.chartList.size();
	}

	public boolean isChartFull(){
		return (!this.chartList.isEmpty() && null == this.lastChart);
	}

}
