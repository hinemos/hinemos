/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log.util;

import java.util.Date;

public class CloudLogRawObject{
	
	Date d;
	String mes;
	String logStreamName;
	boolean detected = false;
	boolean carryOver = false;
	
	public CloudLogRawObject(Date d, String mes, String logStreamName){
		this.d = d;
		this.mes = mes;
		this.logStreamName = logStreamName;
	}

	public Date getDate() {
		return d;
	}

	public String getMes() {
		return mes;
	}
	
	public void setMes(String mes) {
		this.mes = mes;
	}

	public String getStreamName(){
		return logStreamName;
	}
	
	public void setDetected(boolean detected) {
		this.detected = detected;
	}

	public boolean hasDetected() {
		return this.detected;
	}
	
	public void setCarryOver(boolean carryOver) {
		this.carryOver = carryOver;
	}

	public boolean isCarryOver() {
		return this.carryOver;
	}
}
