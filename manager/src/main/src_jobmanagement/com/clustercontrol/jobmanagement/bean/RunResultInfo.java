/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 実行結果情報を保持するクラス<BR>
 *
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunResultInfo extends RunInfo implements Serializable {
	private static final long serialVersionUID = -5920913289024178396L;

	/** 実行状態 */
	private Integer status = 0;
	/** 時刻 */
	private Long time = 0l;
	/** ファイルリスト */
	private List<String> fileList = new ArrayList<>();
	/** 終了値 */
	private Integer endValue = 0;
	/** メッセージ */
	private String message;
	/** エラーメッセージ */
	private String errorMessage;

	/**
	 * 時刻を返します。
	 * 
	 * @return 時刻
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * 時刻を設定します。
	 * 
	 * @param time 時刻
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/**
	 * 終了値を返します。
	 * 
	 * @return 終了値
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * 終了値を設定します。
	 * 
	 * @param endValue 終了値
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * エラーメッセージを返します。
	 * 
	 * @return エラーメッセージ
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * エラーメッセージを設定します。
	 * 
	 * @param errorMessage エラーメッセージ
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * メッセージを返します。
	 * 
	 * @return メッセージ
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージを設定します。
	 * 
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 実行状態を返します。
	 * 
	 * @return 実行状態
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.RunStatusConstant
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 実行状態を設定します。
	 * 
	 * @param status 実行状態
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.RunStatusConstant
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * ファイルリストを返します。
	 * 
	 * @return ファイルリスト
	 */
	public List<String> getFileList() {
		return fileList;
	}

	/**
	 * ファイルリストを設定します。
	 * 
	 * @param fileList ファイルリスト
	 */
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((endValue == null) ? 0 : endValue.hashCode());
		result = prime * result
				+ ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result
				+ ((fileList == null) ? 0 : fileList.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RunResultInfo other = (RunResultInfo) obj;
		if (endValue == null) {
			if (other.endValue != null)
				return false;
		} else if (!endValue.equals(other.endValue))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (fileList == null) {
			if (other.fileList != null)
				return false;
		} else if (!fileList.equals(other.fileList))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}
}
