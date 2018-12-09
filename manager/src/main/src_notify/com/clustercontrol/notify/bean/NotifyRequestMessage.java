/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlType;


/**
 * 各通知方式に出力を依頼するためのメッセージ
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class NotifyRequestMessage implements Serializable {
	private static final long serialVersionUID = -2188950801974373408L;

	private OutputBasicInfo _outputInfo;
	private String _notifyId; // 通知ID
	private Date _outputDate; // 監視結果受信日時
	private boolean _priorityChangeFlag; // 重要度が変更されたことを示すフラグ

	public void putOutputInfo(OutputBasicInfo outputInfo) {
		_outputInfo = outputInfo;
	}
	
	public void setOutputInfo(OutputBasicInfo outputInfo) {
		this._outputInfo = outputInfo;
	}
	
	public OutputBasicInfo getOutputInfo() {
		return _outputInfo;
	}

	public void setNotifyId(String notifyId) {
		_notifyId = notifyId;
	}
	
	public String getNotifyId() {
		return _notifyId;
	}

	public void setOutputDate(Date outputDate) {
		_outputDate = outputDate;
	}
	
	public Date getOutputDate() {
		return _outputDate;
	}

	public void setPriorityChangeFlag(boolean priorityChangeFlag) {
		_priorityChangeFlag = priorityChangeFlag;
	}
	
	public boolean isPriorityChangeFlag() {
		return _priorityChangeFlag;
	}
	
	// for cluster jax-ws
	public NotifyRequestMessage() { }

	public NotifyRequestMessage(
			OutputBasicInfo outputInfo,
			String notifyId,
			Date outputDate,
			boolean priorityChangeFlag){
		_outputInfo = outputInfo;
		_notifyId = notifyId;
		_outputDate = outputDate;
		_priorityChangeFlag = priorityChangeFlag;
	}

	@Override
	public String toString(){
		String str = "NotifyID = " + _notifyId + ", OutputDate=" + _outputDate + ", OutputInfo=" + _outputInfo;
		return str;
	}

}
