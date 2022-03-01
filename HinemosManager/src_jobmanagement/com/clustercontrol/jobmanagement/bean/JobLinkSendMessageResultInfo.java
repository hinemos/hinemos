/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * ジョブ連携送信結果を格納するクラスです。
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkSendMessageResultInfo implements Serializable {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -6261056044375804886L;

	// 送信元ファシリティID
	private ArrayList<String> srcFacilityIdList;

	// 受信日時
	private Long acceptDate;

	// 実行結果
	private Boolean result = Boolean.TRUE;

	public ArrayList<String> getSrcFacilityIdList() {
		return srcFacilityIdList;
	}

	public void setSrcFacilityIdList(ArrayList<String> srcFacilityIdList) {
		this.srcFacilityIdList = srcFacilityIdList;
	}

	public Long getAcceptDate() {
		return acceptDate;
	}

	public void setAcceptDate(Long acceptDate) {
		this.acceptDate = acceptDate;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}
}
