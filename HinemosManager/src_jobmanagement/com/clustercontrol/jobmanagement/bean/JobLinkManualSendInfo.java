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
import java.util.Arrays;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ジョブ連携送信ジョブに関する情報を保持するクラス
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkManualSendInfo implements Serializable {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -7754208777259920387L;

	private static Log m_log = LogFactory.getLog(JobLinkManualSendInfo.class);

	/** ジョブ連携送信設定ID */
	private String joblinkSendSettingId;

	/** ジョブ連携メッセージID */
	private String joblinkMessageId;

	/** 重要度 */
	private Integer priority;

	/** メッセージ */
	private String message;

	// 監視詳細
	private String monitorDetailId;

	// アプリケーション
	private String application;

	/** ジョブ連携メッセージの拡張情報設定 */
	private ArrayList<JobLinkExpInfo> jobLinkExpList;

	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}

	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public ArrayList<JobLinkExpInfo> getJobLinkExpList() {
		return jobLinkExpList;
	}

	public void setJobLinkExpList(ArrayList<JobLinkExpInfo> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((joblinkSendSettingId == null) ? 0 : joblinkSendSettingId.hashCode());
		result = prime * result + ((joblinkMessageId == null) ? 0 : joblinkMessageId.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((monitorDetailId == null) ? 0 : monitorDetailId.hashCode());
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((jobLinkExpList == null) ? 0 : jobLinkExpList.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JobLinkManualSendInfo)) {
			return false;
		}
		JobLinkManualSendInfo o1 = this;
		JobLinkManualSendInfo o2 = (JobLinkManualSendInfo) obj;

		boolean ret = false;
		ret = equalsSub(o1.getJoblinkSendSettingId(), o2.getJoblinkSendSettingId())
				&& equalsSub(o1.getJoblinkMessageId(), o2.getJoblinkMessageId())
				&& equalsSub(o1.getPriority(), o2.getPriority()) && equalsSub(o1.getMessage(), o2.getMessage())
				&& equalsSub(o1.getMonitorDetailId(), o2.getMonitorDetailId())
				&& equalsSub(o1.getApplication(), o2.getApplication())
				&& equalsArray(o1.getJobLinkExpList(), o2.getJobLinkExpList());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false;
		}
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}

	private boolean equalsArray(ArrayList<?> list1, ArrayList<?> list2) {
		if (list1 != null && !list1.isEmpty()) {
			if (list2 != null && list1.size() == list2.size()) {
				Object[] ary1 = list1.toArray();
				Object[] ary2 = list2.toArray();
				Arrays.sort(ary1);
				Arrays.sort(ary2);

				for (int i = 0; i < ary1.length; i++) {
					if (!ary1[i].equals(ary2[i])) {
						if (m_log.isTraceEnabled()) {
							m_log.trace("equalsArray : " + ary1[i] + "!=" + ary2[i]);
						}
						return false;
					}
				}
				return true;
			}
		} else if (list2 == null || list2.isEmpty()) {
			return true;
		}
		return false;
	}
}