/*

Copyright (C) 2010 NTT DATA Corporation

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

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * ジョブ[スケジュール予定]のフィルタ処理クラス
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobPlanFilter implements Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4307237499663304642L;
	private static Log m_log = LogFactory.getLog( JobPlanFilter.class );

	/** 開始 */
	private Long fromDate = null;
	/** 終了*/
	private Long toDate = null;
	/** 実行契機ID */
	private String jobKickId = null;

	public JobPlanFilter(Long fromDate, Long toDate,
			String jobKickId) {
		super();
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.jobKickId = jobKickId;
	}
	public JobPlanFilter(){}
	/** 開始 */
	public Long getFromDate() {
		return fromDate;
	}
	/** 開始 */
	public void setFromDate(Long fromDate) {
		this.fromDate = fromDate;
	}
	/** 終了*/
	public Long getToDate() {
		return toDate;
	}
	/** 終了*/
	public void setToDate(Long toDate) {
		this.toDate = toDate;
	}
	/** 実行契機ID */
	public String getJobKickId() {
		return jobKickId;
	}
	/** 実行契機ID */
	public void setJobKickId(String jobKickId) {
		this.jobKickId = jobKickId;
	}

	/**
	 * フィルタ処理
	 * 開始時間
	 * 終了時間
	 * 含む - 実行契機ID
	 * 除く - 実行契機ID
	 * 
	 * 表示可能なスケジュールの場合は、Trueが返る
	 * 
	 * @param id
	 * @param date
	 * @return
	 */
	public Boolean filterAction(String id, Long date){
		boolean chkDate = true;
		boolean chkId = true;

		//from > date の場合、フィルタリング
		if(this.fromDate != null && this.fromDate > date){
			chkDate = false;
		}
		//date >= to の場合、フィルタリング
		if(this.toDate != null && date >= this.toDate){
			chkDate = false;
		}

		//jobkickIdが中間一致しているかチェック
		if(this.jobKickId != null && this.jobKickId.length() > 0){
			// 「含まない」検索を行うかの判断に使う値
			String notInclude = "NOT:";
			if(!this.jobKickId.startsWith(notInclude)) {
				chkId = id.matches(".*" + this.jobKickId + ".*");
			} else {
				chkId = !id.matches(".*" + this.jobKickId.substring(notInclude.length()) + ".*");
			}
		}

		m_log.trace("chkDate= " + chkDate + ", chkId= " + chkId);

		return chkDate && chkId;
	}
	@Override
	public String toString() {
		return "JobPlanFilter [" +
				"fromDate=" + fromDate +
				", toDate=" + toDate +
				", jobKickIdContainList=" + jobKickId + "]";
	}
}
