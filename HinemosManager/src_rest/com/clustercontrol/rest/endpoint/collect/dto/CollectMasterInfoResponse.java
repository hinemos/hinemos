/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.ArrayList;

public class CollectMasterInfoResponse {

	public CollectMasterInfoResponse() {
	}

	private ArrayList<CollectorCalcMethodMstDataResponse> collectorCalcMethodMstDataList;
	private ArrayList<CollectorCategoryMstDataResponse> collectorCategoryMstDataList;
	private ArrayList<CollectorItemCodeMstDataResponse> collectorItemCodeMstDataList;
	private ArrayList<CollectorItemCalcMethodMstDataResponse> collectorItemCalcMethodMstDataList;
	private ArrayList<CollectorPollingMstDataResponse> collectorPollingMstDataList;
	private ArrayList<CollectorCategoryCollectMstDataResponse> collectorCategoryCollectMstDataList;

	public ArrayList<CollectorCalcMethodMstDataResponse> getCollectorCalcMethodMstDataList() {
		return collectorCalcMethodMstDataList;
	}

	public void setCollectorCalcMethodMstDataList(
			ArrayList<CollectorCalcMethodMstDataResponse> collectorCalcMethodMstDataList) {
		this.collectorCalcMethodMstDataList = collectorCalcMethodMstDataList;
	}

	public ArrayList<CollectorCategoryMstDataResponse> getCollectorCategoryMstDataList() {
		return collectorCategoryMstDataList;
	}

	public void setCollectorCategoryMstDataList(
			ArrayList<CollectorCategoryMstDataResponse> collectorCategoryMstDataList) {
		this.collectorCategoryMstDataList = collectorCategoryMstDataList;
	}

	public ArrayList<CollectorItemCodeMstDataResponse> getCollectorItemCodeMstDataList() {
		return collectorItemCodeMstDataList;
	}

	public void setCollectorItemCodeMstDataList(
			ArrayList<CollectorItemCodeMstDataResponse> cllectorItemCodeMstDataList) {
		this.collectorItemCodeMstDataList = cllectorItemCodeMstDataList;
	}

	public ArrayList<CollectorItemCalcMethodMstDataResponse> getCollectorItemCalcMethodMstDataList() {
		return collectorItemCalcMethodMstDataList;
	}

	public void setCollectorItemCalcMethodMstDataList(
			ArrayList<CollectorItemCalcMethodMstDataResponse> collectorItemCalcMethodMstDataList) {
		this.collectorItemCalcMethodMstDataList = collectorItemCalcMethodMstDataList;
	}

	public ArrayList<CollectorPollingMstDataResponse> getCollectorPollingMstDataList() {
		return collectorPollingMstDataList;
	}

	public void setCollectorPollingMstDataList(
			ArrayList<CollectorPollingMstDataResponse> collectorPollingMstDataList) {
		this.collectorPollingMstDataList = collectorPollingMstDataList;
	}

	public ArrayList<CollectorCategoryCollectMstDataResponse> getCollectorCategoryCollectMstDataList() {
		return collectorCategoryCollectMstDataList;
	}

	public void setCollectorCategoryCollectMstDataList(
			ArrayList<CollectorCategoryCollectMstDataResponse> collectorCategoryCollectMstDataList) {
		this.collectorCategoryCollectMstDataList = collectorCategoryCollectMstDataList;
	}
}
