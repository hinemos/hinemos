/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.bean;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.performance.monitor.entity.CollectorCalcMethodMstData;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstData;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryMstData;
import com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstData;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.monitor.entity.CollectorPollingMstData;

/**
 * 収集項目マスタを保持するクラスです。
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://collectmaster.ws.clustercontrol.com")
public class CollectMasterInfo implements Serializable
{
	private static final long serialVersionUID = -2246932043662575333L;

	private ArrayList<CollectorCalcMethodMstData> collectorCalcMethodMstDataList;
	private ArrayList<CollectorCategoryMstData> collectorCategoryMstDataList;
	private ArrayList<CollectorItemCodeMstData> collectorItemCodeMstDataList;
	private ArrayList<CollectorItemCalcMethodMstData> collectorItemCalcMethodMstDataList;
	private ArrayList<CollectorPollingMstData> collectorPollingMstDataList;
	private ArrayList<CollectorCategoryCollectMstData> collectorCategoryCollectMstDataList;

	/**
	 * コンストラクタ。
	 */
	public CollectMasterInfo() {
	}

	public ArrayList<CollectorCalcMethodMstData> getCollectorCalcMethodMstDataList() {
		return collectorCalcMethodMstDataList;
	}

	public void setCollectorCalcMethodMstDataList(
			ArrayList<CollectorCalcMethodMstData> collectorCalcMethodMstDataList) {
		this.collectorCalcMethodMstDataList = collectorCalcMethodMstDataList;
	}

	public ArrayList<CollectorCategoryMstData> getCollectorCategoryMstDataList() {
		return collectorCategoryMstDataList;
	}

	public void setCollectorCategoryMstDataList(
			ArrayList<CollectorCategoryMstData> collectorCategoryMstDataList) {
		this.collectorCategoryMstDataList = collectorCategoryMstDataList;
	}

	public ArrayList<CollectorItemCodeMstData> getCollectorItemCodeMstDataList() {
		return collectorItemCodeMstDataList;
	}

	public void setCollectorItemCodeMstDataList(
			ArrayList<CollectorItemCodeMstData> cllectorItemCodeMstDataList) {
		this.collectorItemCodeMstDataList = cllectorItemCodeMstDataList;
	}

	public ArrayList<CollectorItemCalcMethodMstData> getCollectorItemCalcMethodMstDataList() {
		return collectorItemCalcMethodMstDataList;
	}

	public void setCollectorItemCalcMethodMstDataList(
			ArrayList<CollectorItemCalcMethodMstData> collectorItemCalcMethodMstDataList) {
		this.collectorItemCalcMethodMstDataList = collectorItemCalcMethodMstDataList;
	}

	public ArrayList<CollectorPollingMstData> getCollectorPollingMstDataList() {
		return collectorPollingMstDataList;
	}

	public void setCollectorPollingMstDataList(
			ArrayList<CollectorPollingMstData> collectorPollingMstDataList) {
		this.collectorPollingMstDataList = collectorPollingMstDataList;
	}

	public ArrayList<CollectorCategoryCollectMstData> getCollectorCategoryCollectMstDataList() {
		return collectorCategoryCollectMstDataList;
	}

	public void setCollectorCategoryCollectMstDataList(
			ArrayList<CollectorCategoryCollectMstData> collectorCategoryCollectMstDataList) {
		this.collectorCategoryCollectMstDataList = collectorCategoryCollectMstDataList;
	}
}
