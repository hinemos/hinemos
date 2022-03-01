/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class AddCollectMasterRequest implements RequestDto {	
	private ArrayList<CollectorCalcMethodMstDataRequest> collectorCalcMethodMstDataList;
	private ArrayList<CollectorCategoryMstDataRequest> collectorCategoryMstDataList;
	private ArrayList<CollectorItemCodeMstDataRequest> collectorItemCodeMstDataList;
	private ArrayList<CollectorItemCalcMethodMstDataRequest> collectorItemCalcMethodMstDataList;
	private ArrayList<CollectorPollingMstDataRequest> collectorPollingMstDataList;
	private ArrayList<CollectorCategoryCollectMstDataRequest> collectorCategoryCollectMstDataList;
	public ArrayList<CollectorCalcMethodMstDataRequest> getCollectorCalcMethodMstDataList() {
		return collectorCalcMethodMstDataList;
	}

	public void setCollectorCalcMethodMstDataList(
			ArrayList<CollectorCalcMethodMstDataRequest> collectorCalcMethodMstDataList) {
		this.collectorCalcMethodMstDataList = collectorCalcMethodMstDataList;
	}

	public ArrayList<CollectorCategoryMstDataRequest> getCollectorCategoryMstDataList() {
		return collectorCategoryMstDataList;
	}

	public void setCollectorCategoryMstDataList(
			ArrayList<CollectorCategoryMstDataRequest> collectorCategoryMstDataList) {
		this.collectorCategoryMstDataList = collectorCategoryMstDataList;
	}

	public ArrayList<CollectorItemCodeMstDataRequest> getCollectorItemCodeMstDataList() {
		return collectorItemCodeMstDataList;
	}

	public void setCollectorItemCodeMstDataList(
			ArrayList<CollectorItemCodeMstDataRequest> cllectorItemCodeMstDataList) {
		this.collectorItemCodeMstDataList = cllectorItemCodeMstDataList;
	}

	public ArrayList<CollectorItemCalcMethodMstDataRequest> getCollectorItemCalcMethodMstDataList() {
		return collectorItemCalcMethodMstDataList;
	}

	public void setCollectorItemCalcMethodMstDataList(
			ArrayList<CollectorItemCalcMethodMstDataRequest> collectorItemCalcMethodMstDataList) {
		this.collectorItemCalcMethodMstDataList = collectorItemCalcMethodMstDataList;
	}

	public ArrayList<CollectorPollingMstDataRequest> getCollectorPollingMstDataList() {
		return collectorPollingMstDataList;
	}

	public void setCollectorPollingMstDataList(
			ArrayList<CollectorPollingMstDataRequest> collectorPollingMstDataList) {
		this.collectorPollingMstDataList = collectorPollingMstDataList;
	}

	public ArrayList<CollectorCategoryCollectMstDataRequest> getCollectorCategoryCollectMstDataList() {
		return collectorCategoryCollectMstDataList;
	}

	public void setCollectorCategoryCollectMstDataList(
			ArrayList<CollectorCategoryCollectMstDataRequest> collectorCategoryCollectMstDataList) {
		this.collectorCategoryCollectMstDataList = collectorCategoryCollectMstDataList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
