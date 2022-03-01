/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PredictionMethodEnum;

public abstract class AbstractNumericMonitorResponse extends AbstractMonitorResponse {

	public AbstractNumericMonitorResponse() {

	}

	protected Boolean collectorFlg;
	protected String itemName;
	protected String measure;
	protected Boolean predictionFlg;
	@RestBeanConvertEnum
	protected PredictionMethodEnum predictionMethod;
	protected Integer predictionAnalysysRange;
	protected Integer predictionTarget;
	protected String predictionApplication;
	protected Boolean changeFlg;
	protected Integer changeAnalysysRange;
	protected String changeApplication;
	protected List<MonitorNumericValueInfoResponse> numericValueInfo = new ArrayList<>();
	protected List<NotifyRelationInfoResponse> predictionNotifyRelationList = new ArrayList<>();
	protected List<NotifyRelationInfoResponse> changeNotifyRelationList = new ArrayList<>();
	public Boolean getCollectorFlg() {
		return collectorFlg;
	}
	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	public Boolean getPredictionFlg() {
		return predictionFlg;
	}
	public void setPredictionFlg(Boolean predictionFlg) {
		this.predictionFlg = predictionFlg;
	}
	public PredictionMethodEnum getPredictionMethod() {
		return predictionMethod;
	}
	public void setPredictionMethod(PredictionMethodEnum predictionMethod) {
		this.predictionMethod = predictionMethod;
	}
	public Integer getPredictionAnalysysRange() {
		return predictionAnalysysRange;
	}
	public void setPredictionAnalysysRange(Integer predictionAnalysysRange) {
		this.predictionAnalysysRange = predictionAnalysysRange;
	}
	public Integer getPredictionTarget() {
		return predictionTarget;
	}
	public void setPredictionTarget(Integer predictionTarget) {
		this.predictionTarget = predictionTarget;
	}
	public String getPredictionApplication() {
		return predictionApplication;
	}
	public void setPredictionApplication(String predictionApplication) {
		this.predictionApplication = predictionApplication;
	}
	public Boolean getChangeFlg() {
		return changeFlg;
	}
	public void setChangeFlg(Boolean changeFlg) {
		this.changeFlg = changeFlg;
	}
	public Integer getChangeAnalysysRange() {
		return changeAnalysysRange;
	}
	public void setChangeAnalysysRange(Integer changeAnalysysRange) {
		this.changeAnalysysRange = changeAnalysysRange;
	}
	public String getChangeApplication() {
		return changeApplication;
	}
	public void setChangeApplication(String changeApplication) {
		this.changeApplication = changeApplication;
	}
	public List<MonitorNumericValueInfoResponse> getNumericValueInfo() {
		return numericValueInfo;
	}
	public void setNumericValueInfo(List<MonitorNumericValueInfoResponse> numericValueInfo) {
		this.numericValueInfo = numericValueInfo;
	}
	public List<NotifyRelationInfoResponse> getPredictionNotifyRelationList() {
		return predictionNotifyRelationList;
	}
	public void setPredictionNotifyRelationList(List<NotifyRelationInfoResponse> predictionNotifyRelationList) {
		this.predictionNotifyRelationList = predictionNotifyRelationList;
	}
	public List<NotifyRelationInfoResponse> getChangeNotifyRelationList() {
		return changeNotifyRelationList;
	}
	public void setChangeNotifyRelationList(List<NotifyRelationInfoResponse> changeNotifyRelationList) {
		this.changeNotifyRelationList = changeNotifyRelationList;
	}

	
}
