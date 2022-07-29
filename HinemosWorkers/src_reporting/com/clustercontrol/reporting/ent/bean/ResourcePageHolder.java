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
 * Resource page holder class for managing pages
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class ResourcePageHolder {

	private int maxLine;
	private int pageChartNum;

	private List<ResourcePage> pageList = new ArrayList<>();
	private ResourcePage lastPage = null;

	public ResourcePageHolder(int pageChartNum, int maxLine) {
		this.maxLine = maxLine;
		this.pageChartNum = pageChartNum;
	}

	public void appendItem(DataKey dataKey, String subTitle){
		if(null == this.lastPage){
			this.lastPage = new ResourcePage(maxLine);
			this.pageList.add(this.lastPage);
		}
		this.lastPage.appendItem(dataKey, subTitle);

		// Check if page is full
		if(lastPage.count() == pageChartNum && lastPage.isChartFull()){
			// Set null here will create new page in next round
			createNewPage();
		}
	}

	/**
	 * Create a new page
	 * 
	 */
	private void createNewPage(){
		// All you have to do is just set lastPage to null and new ResourcePage() will be added in the next round of appendItem()
		this.lastPage = null;
	}

	public List<ResourcePage> list(){
		return this.pageList;
	}

	public boolean isEmpty(){
		return this.pageList.isEmpty();
	}

	public void newChart(){
		if(null != this.lastPage){
			// First check if page is full
			if(this.lastPage.count() == pageChartNum){
				// If current page is full, then just create new page
				createNewPage();
			}else{
				// Create new chart on current page
				this.lastPage.createNewChart();
			}
		}
	}
}
