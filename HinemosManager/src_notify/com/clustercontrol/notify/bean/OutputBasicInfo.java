/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;

/**
 * 通知の基本情報を保持するクラス<BR>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class OutputBasicInfo implements java.io.Serializable, Cloneable {

	private static final long serialVersionUID = 8483134102030125386L;

	/**
	 * 重要度。
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private int m_priority;

	/**
	 * 出力日時
	 */
	private Long m_generationDate;

	/**
	 * プラグインID
	 */
	private String m_pluginId;

	/**
	 * 監視項目ID
	 */
	private String m_monitorId;

	/**
	 * 抑制用のサブキー（任意の文字列）
	 */
	private String m_subKey = "";

	/**
	 * ファシリティID
	 */
	private String m_facilityId;

	/**
	 * スコープ
	 */
	private String m_scopeText;

	/**
	 * アプリケーション
	 */
	private String m_application;

	/**
	 * メッセージ
	 */
	private String m_message;

	/**
	 * オリジナルメッセージ
	 */
	private String m_messageOrg;

	/**
	 * 多重化ID
	 */
	private String m_multiId;

	/**
	 * 承認依頼文
	 */
	private String m_jobApprovalText;

	/**
	 * 承認依頼メール本文
	 */
	private String m_jobApprovalMail;

	/**
	 * 監視ジョブ指示情報
	 */
	private RunInstructionInfo m_runInstructionInfo;

	/**
	 * 標準出力
	 */
	private List<String> m_jobMessage = new ArrayList<>();
	
	/**
	 * 標準出力ノード
	 */
	private List<String> m_jobFacilityId = new ArrayList<>();
	
	/**
	 * 通知グループID
	 */
	private String m_notifyGroupId;
	
	public OutputBasicInfo() {}

	/**
	 * アプリケーションを返します。
	 * 
	 * @return アプリケーション
	 */
	public String getApplication() {
		return m_application;
	}
	/**
	 * アプリケーションを設定します。
	 * 
	 * @param application アプリケーション
	 */
	public void setApplication(String application) {
		this.m_application = application;
	}
	/**
	 * ファシリティIDを返します。
	 * 
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return m_facilityId;
	}
	/**
	 * ファシリティIDを設定します。
	 * 
	 * @param id ファシリティID
	 */
	public void setFacilityId(String id) {
		m_facilityId = id;
	}
	/**
	 * 出力日時を返します。
	 * 
	 * @return 出力日時
	 */
	public Long getGenerationDate() {
		return m_generationDate;
	}
	/**
	 * 出力日時を設定します。
	 * 
	 * @param date 出力日時
	 */
	public void setGenerationDate(Long date) {
		m_generationDate = date;
	}
	/**
	 * メッセージを返します。
	 * 
	 * @return メッセージ
	 */
	public String getMessage() {
		return m_message;
	}
	/**
	 * メッセージを設定します。
	 * 
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.m_message = message;
	}
	/**
	 * オリジナルメッセージを返します。
	 * 
	 * @return オリジナルメッセージ
	 */
	public String getMessageOrg() {
		return m_messageOrg;
	}
	/**
	 * オリジナルメッセージを設定します。
	 * 
	 * @param org オリジナルメッセージ
	 */
	public void setMessageOrg(String org) {
		m_messageOrg = org;
	}
	/**
	 * 監視項目IDを返します。
	 * 
	 * @return 監視項目ID
	 */
	public String getMonitorId() {
		return m_monitorId;
	}
	/**
	 * 監視項目IDを設定します。
	 * 
	 * @param id 監視項目ID
	 */
	public void setMonitorId(String id) {
		m_monitorId = id;
	}
	/**
	 * 抑制用のサブキー（任意の文字列）を返します。
	 * 
	 * @return 抑制用のサブキー（任意の文字列）
	 */
	public String getSubKey() {
		return m_subKey;
	}
	/**
	 * 抑制用のサブキー（任意の文字列）を設定します。
	 * 
	 * @param subkey 抑制用のサブキー（任意の文字列）
	 */
	public void setSubKey(String subkey) {
		m_subKey = subkey;
	}
	/**
	 * プラグインIDを返します。
	 * 
	 * @return プラグインID
	 */
	public String getPluginId() {
		return m_pluginId;
	}
	/**
	 * プラグインIDを設定します。
	 * 
	 * @param id プラグインID
	 */
	public void setPluginId(String id) {
		m_pluginId = id;
	}
	/**
	 * 重要度を返します。
	 * 
	 * @return 重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public int getPriority() {
		return m_priority;
	}
	/**
	 * 重要度を設定します。
	 * 
	 * @param priority 重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setPriority(int priority) {
		this.m_priority = priority;
	}
	/**
	 * スコープを返します。
	 * 
	 * @return スコープ
	 */
	public String getScopeText() {
		return m_scopeText;
	}
	/**
	 * スコープを設定します。
	 * 
	 * @param text スコープ
	 */
	public void setScopeText(String text) {
		m_scopeText = text;
	}
	/**
	 * 多重化IDを返します。
	 * 
	 * @return 多重化ID
	 */
	public String getMultiId() {
		return m_multiId;
	}
	/**
	 * 多重化IDを設定します。
	 * クラスタ構成の場合、設定します。
	 * 
	 * @param id 多重化ID
	 */
	public void setMultiId(String id) {
		m_multiId = id;
	}
	
	/**
	 * 承認ジョブにおける承認依頼文を返します。
	 * 
	 * @return 承認依頼文
	 */
	public String getJobApprovalText() {
		return m_jobApprovalText;
	}
	/**
	 * 承認ジョブにおける承認依頼文を設定します。
	 * 
	 * @param txt 承認依頼文
	 */
	public void setJobApprovalText(String txt) {
		m_jobApprovalText = txt;
	}
	/**
	 * 承認ジョブにおける承認依頼メール本文を返します。
	 * 
	 * @return 承認依頼メール本文
	 */
	public String getJobApprovalMail() {
		return m_jobApprovalMail;
	}
	/**
	 * 承認ジョブにおける承認依頼メール本文を設定します。
	 * 
	 * @param txt 承認依頼メール本文
	 */
	public void setJobApprovalMail(String mail) {
		m_jobApprovalMail = mail;
	}

	/**
	 * 監視ジョブ指示情報を返します。
	 * 
	 * @return 監視ジョブ指示情報
	 */
	public RunInstructionInfo getRunInstructionInfo() {
		return m_runInstructionInfo;
	}
	/**
	 * 監視ジョブ指示情報を設定します。
	 * 
	 * @param runInstructionInfo 監視ジョブ指示情報
	 */
	public void setRunInstructionInfo(RunInstructionInfo runInstructionInfo) {
		m_runInstructionInfo = runInstructionInfo;
	}

	/**
	 *  標準出力を返します。
	 * 
	 * @return  標準出力
	 */
	public List<String> getJobMessage() {
		return m_jobMessage;
	}
	/**
	 *  標準出力を設定します。
	 * クラスタ構成の場合、設定します。
	 * 
	 * @param message  標準出力
	 */
	public void setJobMessage(List<String> message) {
		m_jobMessage = message;
	}

	
	/**
	 *  標準出力ノードを返します。
	 * 
	 * @return  標準出力ノード
	 */
	public List<String> getJobFacilityId() {
		return m_jobFacilityId;
	}
	/**
	 *  標準出力ノードを設定します。
	 * クラスタ構成の場合、設定します。
	 * 
	 * @param node  標準出力ノード
	 */
	public void setJobFacilityId(List<String> node) {
		m_jobFacilityId = node;
	}

	
	/**
	 *  通知グループIDを返します。
	 * 
	 * @return  通知グループID
	 */
	public String getNotifyGroupId() {
		return m_notifyGroupId;
	}
	/**
	 *  通知グループIDを設定します。
	 * 
	 * @param notifyGroupId  通知グループID
	 */
	public void setNotifyGroupId(String notifyGroupId) {
		m_notifyGroupId = notifyGroupId;
	}

	@Override
	public String toString(){
		String str = "OutputBasicInfo : "
				+ "FacilityId = " + m_facilityId 
				+ ", PluginId = " + m_pluginId 
				+ ", MonitorId = " + m_monitorId
				+ ", NotifyGroupId = " + m_notifyGroupId;
		return str;
	}
	
	@Override
	public OutputBasicInfo clone(){
		OutputBasicInfo clonedInfo = null;
		try {
			clonedInfo = (OutputBasicInfo) super.clone();
			clonedInfo.setApplication(m_application);
			clonedInfo.setFacilityId(m_facilityId);
			clonedInfo.setGenerationDate(m_generationDate);
			clonedInfo.setMessage(m_message);
			clonedInfo.setMessageOrg(m_messageOrg);
			clonedInfo.setMonitorId(m_monitorId);
			clonedInfo.setMultiId(m_multiId);
			clonedInfo.setPluginId(m_pluginId);
			clonedInfo.setPriority(m_priority);
			clonedInfo.setScopeText(m_scopeText);
			clonedInfo.setSubKey(m_subKey);
			clonedInfo.setNotifyGroupId(m_notifyGroupId);
		} catch (CloneNotSupportedException e) {
			// do nothing
		}
		return clonedInfo;
	}
}
