/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class CollectDataInfoResponse {
	private Integer collectorId;
	@RestBeanConvertDatetime
	private String time;
	private Float value;
	private Float average;
	private Float standardDeviation;
	private Long position;

	public CollectDataInfoResponse() {
	}

	public Integer getCollectorId() {
		return collectorId;
	}
	public void setCollectorId(Integer collectorId) {
		this.collectorId = collectorId;
	}

	public String getTime(){
		return time;
	}
	public void setTime(String time){
		this.time = time;
	}
	
	public Float getValue() {
		if (value == null) {
			return Float.NaN;
		} else {
			return value;
		}
	}
	public void setValue(Float value) {
		this.value = value;
	}

	public Float getAverage() {
		if (average == null) {
			return Float.NaN;
		} else {
			return average;
		}
	}
	public void setAverage(Float average) {
		this.average = average;
	}

	public Float getStandardDeviation() {
		if (standardDeviation == null) {
			return Float.NaN;
		} else {
			return standardDeviation;
		}
	}
	public void setStandardDeviation(Float standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public Long getPosition(){
		return position;
	}
	public void setPosition(Long position){
		this.position = position;
	}
}
