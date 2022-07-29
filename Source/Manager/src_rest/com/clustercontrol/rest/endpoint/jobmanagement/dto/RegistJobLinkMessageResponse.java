/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class RegistJobLinkMessageResponse {

	// 送信元ファシリティID
	private ArrayList<String> srcFacilityIdList;

	// 受信日時
	@RestBeanConvertDatetime
	private String acceptDate;

	// 実行結果
	private Boolean result;

	// 実行結果詳細
	private String resultDetail;

	public RegistJobLinkMessageResponse() {
	}

	public ArrayList<String> getSrcFacilityIdList() {
		return srcFacilityIdList;
	}

	public void setSrcFacilityIdList(ArrayList<String> srcFacilityIdList) {
		this.srcFacilityIdList = srcFacilityIdList;
	}

	public String getAcceptDate() {
		return acceptDate;
	}

	public void setAcceptDate(String acceptDate) {
		this.acceptDate = acceptDate;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	public String getResultDetail() {
		return resultDetail;
	}

	public void setResultDetail(String resultDetail) {
		this.resultDetail = resultDetail;
	}

	
}
