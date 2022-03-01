/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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

import com.clustercontrol.notify.model.NotifyRelationInfo;

/**
 * ジョブの基本情報を保持するクラス
 * 
 * @version 4.1.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobInfo implements Serializable, Cloneable {

	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobInfo.class );

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -1453680330220941926L;

	/**
	 * ジョブツリーの情報だけの場合はfalse
	 * 全てのプロパティ値が入っている場合はtrue
	 **/
	private Boolean propertyFull = false;

	/** 所属ジョブユニットのジョブID */
	private String jobunitId;

	/** ジョブID */
	private String id;

	/**
	 * 親ジョブID
	 * ModifyJob.java内部でのみ利用する。
	 * それ以外では利用しないこと。
	 * 個々のジョブのadd、modifyからも使用する。
	 **/
	private String parentId;

	/** ジョブ名 */
	private String name;

	/** ジョブ種別 com.clustercontrol.bean.JobConstant */
	private Integer type = 0;

	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfo waitRule;

	/** ジョブコマンド情報 */
	private JobCommandInfo command;

	/** ジョブファイル転送情報 */
	private JobFileInfo file;

	/** 監視ジョブ情報 */
	private MonitorJobInfo monitor;

	/** ジョブ連携送信ジョブ情報 */
	private JobLinkSendInfo jobLinkSend;

	/** ジョブ連携待機ジョブ情報 */
	private JobLinkRcvInfo jobLinkRcv;

	/** ファイルチェックジョブ情報 */
	private JobFileCheckInfo jobFileCheck;

	/** リソース制御ジョブ情報 */
	private ResourceJobInfo resource;

	/** RPAシナリオジョブ情報 */
	private RpaJobInfo rpa;

	/** ジョブ終了状態情報 */
	private ArrayList<JobEndStatusInfo> endStatus;

	/** ジョブ変数情報 */
	private ArrayList<JobParameterInfo> param;

	/** 参照先ジョブユニットID */
	private String referJobUnitId;

	/** 参照先ジョブID */
	private String referJobId;

	/** アイコンID */
	private String iconId;

	/** 参照ジョブ選択種別 */
	private Integer referJobSelectType = 0;

	/** 作成日時 */
	private Long createTime;

	/** 最終更新日時 */
	private Long updateTime;

	/** 新規作成ユーザ */
	private String createUser;

	/** 最終更新ユーザ */
	private String updateUser;

	/** 説明 */
	private String description = "";

	/** オーナーロールID */
	private String ownerRoleId = "";
	
	/** モジュール登録済フラグ */
	private boolean registered = false;
	
	/** 承認依頼先ロールID */
	private String approvalReqRoleId = "";
	
	/** 承認依頼先ユーザID */
	private String approvalReqUserId = "";
	
	/** 承認依頼文 */
	private String approvalReqSentence = "";
	
	/** 承認依頼メール件名 */
	private String approvalReqMailTitle = "";
	
	/** 承認依頼メール本文 */
	private String approvalReqMailBody = "";
	
	/** 承認依頼文の利用有無フラグ */
	private boolean isUseApprovalReqSentence = false;

	/** 実行対象ノードの決定タイミング */
	private boolean expNodeRuntimeFlg = false;

	//ジョブ通知関連
	private Integer beginPriority = 0;
	private Integer normalPriority = 0;
	private Integer warnPriority = 0;
	private Integer abnormalPriority = 0;
	/** 通知ID**/
	private ArrayList<NotifyRelationInfo> notifyRelationInfos;

	/** ジョブ待ち条件情報が変更したか */
	private Boolean isWaitRuleChanged = false;

	/** 参照先ジョブ情報が変更したか */
	private Boolean isReferJobChanged = false;

	public JobInfo() {}

	/**
	 * コンストラクタ
	 * 
	 * @param id ジョブID
	 * @param name ジョブ名
	 * @param type ジョブ種別
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public JobInfo(String jobunit_id, String id, String name, Integer type) {
		setJobunitId(jobunit_id);
		setId(id);
		setName(name);
		setType(type);
	}

	/**
	 * ジョブプロパティ値
	 * @return
	 */
	public Boolean isPropertyFull() {
		return propertyFull;
	}

	public void setPropertyFull(Boolean propertyFull) {
		this.propertyFull = propertyFull;
	}

	/**
	 * ジョブ終了状態情報を返す。<BR>
	 * 
	 * @return ジョブ終了状態情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public ArrayList<JobEndStatusInfo> getEndStatus() {
		return endStatus;

	}

	/**
	 * ジョブ終了状態情報を設定する
	 * 
	 * @param endStatus ジョブ終了状態情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public void setEndStatus(ArrayList<JobEndStatusInfo> endStatus) {
		this.endStatus = endStatus;
	}

	/**
	 * ジョブコマンド情報を返す。<BR>
	 * 
	 * @return ジョブコマンド情報
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public JobCommandInfo getCommand() {
		return command;
	}

	/**
	 * ジョブコマンド情報を設定する。<BR>
	 * 
	 * @param command ジョブコマンド情報
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public void setCommand(JobCommandInfo command) {
		this.command = command;
	}

	/**
	 * ジョブファイル転送情報を返す。<BR>
	 * 
	 * @return ジョブファイル転送情報
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public JobFileInfo getFile() {
		return file;
	}

	/**
	 * ジョブファイル転送情報を設定する。<BR>
	 * 
	 * @param file ジョブファイル転送情報
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public void setFile(JobFileInfo file) {
		this.file = file;
	}

	/**
	 * 監視ジョブ情報を返す。<BR>
	 * 
	 * @return 監視ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.MonitorJobInfo
	 */
	public MonitorJobInfo getMonitor() {
		return monitor;
	}

	/**
	 * 監視ジョブ情報を設定する。<BR>
	 * 
	 * @param monitor 監視ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.MonitorJobInfo
	 */
	public void setMonitor(MonitorJobInfo monitor) {
		this.monitor = monitor;
	}

	/**
	 * ジョブ連携送信ジョブ情報を返す。<BR>
	 * 
	 * @return ジョブ連携送信ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobLinkSendInfo
	 */
	public JobLinkSendInfo getJobLinkSend() {
		return jobLinkSend;
	}

	/**
	 * ジョブ連携送信ジョブ情報を設定する。<BR>
	 * 
	 * @param jobLinkSend ジョブ連携送信ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobLinkSendInfo
	 */
	public void setJobLinkSend(JobLinkSendInfo jobLinkSend) {
		this.jobLinkSend = jobLinkSend;
	}

	/**
	 * ジョブ連携待機ジョブ情報を返す。<BR>
	 * 
	 * @return ジョブ連携待機ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobLinkRcvInfo
	 */
	public JobLinkRcvInfo getJobLinkRcv() {
		return jobLinkRcv;
	}

	/**
	 * ジョブ連携待機ジョブ情報を設定する。<BR>
	 * 
	 * @param jobLinkRcv ジョブ連携待機ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobLinkRcvInfo
	 */
	public void setJobLinkRcv(JobLinkRcvInfo jobLinkRcv) {
		this.jobLinkRcv = jobLinkRcv;
	}

	/**
	 * ファイルチェックジョブ情報を返す。<BR>
	 * 
	 * @return ファイルチェックジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobFileCheckInfo
	 */
	public JobFileCheckInfo getJobFileCheck() {
		return jobFileCheck;
	}

	/**
	 * ファイルチェックジョブ情報を設定する。<BR>
	 * 
	 * @param jobFileCheck ファイルチェックジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobFileCheckInfo
	 */
	public void setJobFileCheck(JobFileCheckInfo jobFileCheck) {
		this.jobFileCheck = jobFileCheck;
	}

	/**
	 * リソース制御ジョブ情報を返す。<BR>
	 * 
	 * @return リソース制御ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.ResourceJobInfo
	 */
	public ResourceJobInfo getResource() {
		return resource;
	}

	/**
	 * リソース制御ジョブ情報を設定する。<BR>
	 * 
	 * @param resource リソース制御ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.ResourceJobInfo
	 */
	public void setResource(ResourceJobInfo resource) {
		this.resource = resource;
	}

	/**
	 * RPAシナリオジョブ情報を返す。<BR>
	 * @return RPAシナリオジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.RpaJobInfo
	 */
	public RpaJobInfo getRpa() {
		return rpa;
	}

	/**
	 * RPAシナリオジョブ情報を設定する。<BR>
	 * @param rpa RPAシナリオジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.RpaJobInfo
	 */
	public void setRpa(RpaJobInfo rpa) {
		this.rpa = rpa;
	}

	/**
	 * 所属ジョブユニットのジョブIDを返す。
	 * 
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定します。
	 * 
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	/**
	 * ジョブIDを返す。<BR>
	 * 
	 * @return ジョブID
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * ジョブIDを設定する。<BR>
	 * 
	 * @param id ジョブID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 親ジョブIDを返す。<BR>
	 * 
	 * @return 親ジョブID
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * 親ジョブIDを設定する。<BR>
	 * 
	 * @param id 親ジョブID
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * ジョブ変数情報を返す。<BR>
	 * 
	 * @return ジョブ変数情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobParameterInfo
	 */
	public ArrayList<JobParameterInfo> getParam() {
		return param;
	}

	/**
	 * ジョブ変数情報を設定する。<BR>
	 * 
	 * @param param ジョブ変数情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobParameterInfo
	 */
	public void setParam(ArrayList<JobParameterInfo> param) {
		this.param = param;
	}

	/**
	 * ジョブ名を返す。<BR>
	 * 
	 * @return ジョブ名
	 */
	public String getName() {
		return name;
	}

	/**
	 * ジョブ名を設定する。<BR>
	 * 
	 * @param name ジョブ名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * モジュール登録済フラグを返す。<BR>
	 * 
	 * @return モジュール登録済フラグ
	 */
	public boolean isRegisteredModule() {
		return registered;
	}

	/**
	 * モジュール登録済フラグを設定する。<BR>
	 * 
	 * @param regist モジュール登録済フラグ
	 */
	public void setRegisteredModule(boolean regist) {
		this.registered = regist;
	}
	
	/**
	 * ジョブ待ち条件情報を返す。<BR>
	 * 
	 * @return ジョブ待ち条件情報
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public JobWaitRuleInfo getWaitRule() {
		return waitRule;
	}

	/**
	 * ジョブ待ち条件情報を設定する。<BR>
	 * 
	 * @param waitRule ジョブ待ち条件情報
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public void setWaitRule(JobWaitRuleInfo waitRule) {
		this.waitRule = waitRule;
	}

	/**
	 * ジョブ種別を返す。<BR>
	 * 
	 * @return ジョブ種別
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * ジョブ種別を設定する。<BR>
	 * 
	 * @param type ジョブ種別
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * 参照先ジョブユニットIDを返す。<BR>
	 * @return 参照先ジョブユニットID
	 */
	public String getReferJobUnitId() {
		return referJobUnitId;
	}
	/**
	 * 参照先ジョブユニットIDを設定する。<BR>
	 * @param referJobUnitId 参照先ジョブユニットID
	 */
	public void setReferJobUnitId(String referJobUnitId) {
		this.referJobUnitId = referJobUnitId;
	}
	/**
	 * 参照先ジョブIDを返す。<BR>
	 * @return 参照先ジョブID
	 */
	public String getReferJobId() {
		return referJobId;
	}
	/**
	 * 参照先ジョブIDを設定する。<BR>
	 * @param referJobId 参照先ジョブID
	 */
	public void setReferJobId(String referJobId) {
		this.referJobId = referJobId;
	}

	/**
	 * アイコンIDを返す。<BR>
	 * @return アイコンID
	 */
	public String getIconId() {
		return iconId;
	}
	/**
	 * アイコンIDを設定する。<BR>
	 * @param iconId アイコンID
	 */
	public void setIconId(String iconId) {
		this.iconId = iconId;
	}

	/**
	 * 参照ジョブ選択種別を返す。<BR>
	 * @return 参照ジョブ選択種別
	 */
	public Integer getReferJobSelectType() {
		return referJobSelectType;
	}
	/**
	 * 参照ジョブ選択種別を設定する。<BR>
	 * @param selectType 参照ジョブ選択種別
	 */
	public void setReferJobSelectType(Integer selectType) {
		this.referJobSelectType = selectType;
	}

	/**
	 * 作成日時を返す。<BR>
	 * @return 作成日時
	 */
	public Long getCreateTime() {
		return createTime;
	}

	/**
	 * 作成日時を設定する。<BR>
	 * @param createTime 作成日時
	 */
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	/**
	 * 最終更新日時を返す。<BR>
	 * @return 最終更新日時
	 */
	public Long getUpdateTime() {
		return updateTime;
	}

	/**
	 * 最終更新日時を設定する。<BR>
	 * @param updateTime 最終更新日時
	 */
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 新規作成ユーザを返す。<BR>
	 * @return 新規作成ユーザ
	 */
	public String getCreateUser() {
		return createUser;
	}

	/**
	 * 新規作成ユーザを設定する。<BR>
	 * @param createUser 新規作成ユーザ
	 */
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	/**
	 * 最終更新ユーザを返す。<BR>
	 * @return 最終更新ユーザ
	 */
	public String getUpdateUser() {
		return updateUser;
	}

	/**
	 * 最終更新ユーザを設定する。<BR>
	 * @param updateUser 最終更新ユーザ
	 */
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	/**
	 * 説明を返す。<BR>
	 * @return 説明
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 説明を設定する。<BR>
	 * @param description 説明
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * オーナーロールIDを返す。<BR>
	 * @return オーナーロールID
	 */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定する。<BR>
	 * @param ownerRoleId オーナーロールID
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/**
	 * 承認依頼先ロールIDを返す。<BR>
	 * 
	 * @return 承認依頼先ロールID
	 */
	public String getApprovalReqRoleId() {
		return approvalReqRoleId;
	}

	/**
	 * 承認依頼先ロールIDを設定する。<BR>
	 * 
	 * @param approvalReqSentence 承認依頼先ロールID
	 */
	public void setApprovalReqRoleId(String approvalReqRoleId) {
		this.approvalReqRoleId = approvalReqRoleId;
	}

	/**
	 * 承認依頼先ユーザIDを返す。<BR>
	 * 
	 * @return 承認依頼先ユーザID
	 */
	public String getApprovalReqUserId() {
		return approvalReqUserId;
	}

	/**
	 * 承認依頼先ユーザIDを設定する。<BR>
	 * 
	 * @param approvalReqSentence 承認依頼先ユーザID
	 */
	public void setApprovalReqUserId(String approvalReqUserId) {
		this.approvalReqUserId = approvalReqUserId;
	}

	/**
	 * 承認依頼文を返す。<BR>
	 * 
	 * @return 承認依頼文
	 */
	public String getApprovalReqSentence() {
		return approvalReqSentence;
	}

	/**
	 * 承認依頼文を設定する。<BR>
	 * 
	 * @param approvalReqSentence 承認依頼文
	 */
	public void setApprovalReqSentence(String approvalReqSentence) {
		this.approvalReqSentence = approvalReqSentence;
	}

	/**
	 * 承認依頼メール件名を返す。<BR>
	 * 
	 * @return 承認依頼メール件名
	 */
	public String getApprovalReqMailTitle() {
		return approvalReqMailTitle;
	}

	/**
	 * 承認依頼メール件名を設定する。<BR>
	 * 
	 * @param approvalReqMailTitle 承認依頼メール件名
	 */
	public void setApprovalReqMailTitle(String approvalReqMailTitle) {
		this.approvalReqMailTitle = approvalReqMailTitle;
	}

	/**
	 * 承認依頼メール本文を返す。<BR>
	 * 
	 * @return 承認依頼メール本文
	 */
	public String getApprovalReqMailBody() {
		return approvalReqMailBody;
	}

	/**
	 * 承認依頼メール本文を設定する。<BR>
	 * 
	 * @param approvalRequesMailBody 承認依頼メール本文
	 */
	public void setApprovalReqMailBody(String approvalReqMailBody) {
		this.approvalReqMailBody = approvalReqMailBody;
	}

	/**
	 * 承認依頼文の利用有無フラグを返す。<BR>
	 * 
	 * @return 承認依頼文の利用有無フラグ
	 */
	public boolean isUseApprovalReqSentence() {
		return isUseApprovalReqSentence;
	}

	/**
	 * 承認依頼文の利用有無フラグを設定する。<BR>
	 * 
	 * @param isUseRequestSentence 承認依頼文の利用有無フラグ
	 */
	public void setUseApprovalReqSentence(boolean isUseApprovalReqSentence) {
		this.isUseApprovalReqSentence = isUseApprovalReqSentence;
	}

	/**
	 * 実行対象ノードの決定タイミングを返す。
	 * 
	 * @return 実行対象ノードの決定タイミング
	 */
	public boolean getExpNodeRuntimeFlg() {
		return expNodeRuntimeFlg;
	}

	/**
	 * 実行対象ノードの決定タイミングを設定する。
	 * 
	 * @param expNodeRuntimeFlg 実行対象ノードの決定タイミング
	 */
	public void setExpNodeRuntimeFlg(boolean expNodeRuntimeFlg) {
		this.expNodeRuntimeFlg = expNodeRuntimeFlg;
	}

	public Integer getBeginPriority() {
		return beginPriority;
	}

	public void setBeginPriority(Integer beginPriority) {
		this.beginPriority = beginPriority;
	}

	public Integer getNormalPriority() {
		return normalPriority;
	}

	public void setNormalPriority(Integer normalPriority) {
		this.normalPriority = normalPriority;
	}

	public Integer getWarnPriority() {
		return warnPriority;
	}

	public void setWarnPriority(Integer warnPriority) {
		this.warnPriority = warnPriority;
	}

	public Integer getAbnormalPriority() {
		return abnormalPriority;
	}

	public void setAbnormalPriority(Integer abnormalPriority) {
		this.abnormalPriority = abnormalPriority;
	}

	/**
	 * 通知IDを返します。
	 * @return　通知IDのコレクション
	 */
	public ArrayList<NotifyRelationInfo> getNotifyRelationInfos() {
		return notifyRelationInfos;
	}

	/**
	 * 通知IDを設定します。
	 * 
	 * @param notifyRelationInfos
	 */
	public void setNotifyRelationInfos(ArrayList<NotifyRelationInfo> notifyRelationInfos) {
		this.notifyRelationInfos = notifyRelationInfos;
	}

	public Boolean isWaitRuleChanged() {
		return isWaitRuleChanged;
	}

	public void setWaitRuleChanged(Boolean isWaitRuleChanged) {
		this.isWaitRuleChanged = isWaitRuleChanged;
	}

	public Boolean isReferJobChanged() {
		return isReferJobChanged;
	}

	public void setReferJobChanged(Boolean isReferJobChanged) {
		this.isReferJobChanged = isReferJobChanged;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 0;
		hash = hash * prime + this.id.hashCode();
		hash = hash * prime + this.name.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobInfo)) {
			return false;
		}
		JobInfo o1 = this;
		JobInfo o2 = (JobInfo)o;

		boolean ret = false;
		// equalsではdateを比較しない。
		ret = 	equalsSub(o1.getId(), o2.getId()) &&
				equalsSub(o1.getCommand(), o2.getCommand()) &&
				equalsSub(o1.getDescription(), o2.getDescription()) &&
				equalsSub(o1.getFile(), o2.getFile()) &&
				equalsSub(o1.getMonitor(), o2.getMonitor()) &&
				equalsSub(o1.getJobLinkSend(), o2.getJobLinkSend()) &&
				equalsSub(o1.getJobLinkRcv(), o2.getJobLinkRcv()) &&
				equalsSub(o1.getJobFileCheck(), o2.getJobFileCheck()) &&
				equalsSub(o1.getResource(), o2.getResource()) &&
				equalsSub(o1.getRpa(), o2.getRpa()) &&
				equalsSub(o1.getJobunitId(), o2.getJobunitId()) &&
				equalsSub(o1.getParentId(), o2.getParentId()) &&
				equalsSub(o1.getName(), o2.getName()) &&
				equalsSub(o1.getReferJobId(), o2.getReferJobId()) &&
				equalsSub(o1.getReferJobUnitId(), o2.getReferJobUnitId()) &&
				equalsSub(o1.isRegisteredModule(), o2.isRegisteredModule()) &&
				equalsSub(o1.getReferJobSelectType(), o2.getReferJobSelectType()) &&
				equalsSub(o1.getType(), o2.getType()) &&
				equalsSub(o1.getWaitRule(), o2.getWaitRule()) &&
				equalsSub(o1.getOwnerRoleId(), o2.getOwnerRoleId()) &&
				equalsArray(o1.getEndStatus(), o2.getEndStatus()) &&
				equalsArray(o1.getParam(), o2.getParam()) &&
				equalsSub(o1.getType(), o2.getType()) &&
				equalsSub(o1.getApprovalReqRoleId(), o2.getApprovalReqRoleId()) &&
				equalsSub(o1.getApprovalReqUserId(), o2.getApprovalReqUserId()) &&
				equalsSub(o1.getApprovalReqSentence(), o2.getApprovalReqSentence()) &&
				equalsSub(o1.getApprovalReqMailTitle(), o2.getApprovalReqMailTitle()) &&
				equalsSub(o1.getApprovalReqMailBody(), o2.getApprovalReqMailBody()) &&
				equalsSub(o1.isUseApprovalReqSentence(), o2.isUseApprovalReqSentence()) &&
				equalsSub(o1.getExpNodeRuntimeFlg(), o2.getExpNodeRuntimeFlg()) &&
				equalsSub(o1.getBeginPriority(), o2.getBeginPriority()) &&
				equalsSub(o1.getNormalPriority(), o2.getNormalPriority()) &&
				equalsSub(o1.getWarnPriority(), o2.getWarnPriority()) &&
				equalsSub(o1.getAbnormalPriority(), o2.getAbnormalPriority()) &&
				equalsSub(o1.getIconId(), o2.getIconId()) &&
				equalsArray(o1.getNotifyRelationInfos(), o2.getNotifyRelationInfos());

		if (!ret && o1.getId().equals(o2.getId())) {
			m_log.debug("equals(o1,o2) : o1=" + o1.getId() + ", o2=" + o2.getId() + ", " + ret);
		}
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		
		if (o1 == null)
			return false;
		
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + " != " + o2);
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
	
	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}
	public static void testEquals(){
		System.out.println("*** all agreement ***");
		JobInfo info1 = createSampleInfo();
		JobInfo info2 = createSampleInfo();
		judge(true, info1.equals(info2));

		String[] str = {
				"Command",
				"Description",
				"FileInfo",
				"Id",
				"JobunitId",
				"Name",
				"ReferJobId",
				"ReferJobUnitId",
				"Type",
				"WaitRule",
				"EndStatus",
				"ManagementUser",
				"Notifications",
				"Param",
				"ParentId",
				"IconId"
		};
		/**
		 * 比較するパラメータの回数繰り返す
		 * カウントアップするごとに、パラメータの値を変える。
		 * 常に、いずれか１つのパラメータがcreateSampleInfo()にて作成されたデータと違う
		 */
		for (int i = 0; i < 16 ; i++) {
			info2 = createSampleInfo();
			switch(i) {
			case 0 :
				info2.getCommand().setMessageRetryEndFlg(true);
				break;
			case 1 :
				info2.setDescription("description_001");
				break;
			case 2 :
				info2.getFile().setCheckFlg(true);
				break;
			case 3 :
				info2.setId("job_Id");
				break;
			case 4 :
				info2.setJobunitId("unit_Id");
				break;
			case 5 :
				info2.setName("job_Name");
				break;
			case 6 :
				info2.setReferJobId("refer_Job_Id");
				break;
			case 7 :
				info2.setReferJobUnitId("refer_JobUnit_Id");
				break;
			case 8 :
				info2.setType(1);
				break;
			case 9 :
				info2.getWaitRule().setCalendar(true);
				break;
			case 10 :
				info2.setOwnerRoleId("ALL_USERS");
				break;
			case 11 :
				if (info2.getEndStatus() == null) {
					JobEndStatusInfo end = JobEndStatusInfo.createSampleInfo();
					info2.getEndStatus().add(end);
				}
				info2.getEndStatus().get(0).setEndRangeValue(2);
				break;
			case 12 :
				break;
			case 13 :
				if (info2.getParam() == null) {
					JobParameterInfo notify = JobParameterInfo.createSampleInfo();
					info2.getParam().add(notify);
				}
				info2.getParam().get(0).setType(2);
				break;
			case 14 :
				info2.setParentId("Parent_Id");
				break;
			case 15 :
				info2.setParentId("Icon_Id");
				break;
			default:
				break;
			}

			System.out.println("*** Only " + str[i] + " is different ***");
			judge(false, info1.equals(info2));
		}
	}
	/**
	 * 単体テストの結果が正しいものか判断する
	 * @param judge
	 * @param result
	 */
	private static void judge(boolean judge, boolean result){

		System.out.println("expect : " + judge);
		System.out.print("result : " + result);
		String ret = "NG";
		if (judge == result) {
			ret = "OK";
		}
		System.out.println("    is ...  " + ret);
	}

	/**
	 * 単体テスト用
	 * @return
	 */
	public static JobInfo createSampleInfo() {

		JobInfo info = new JobInfo();

		info.setId("jobId");
		info.setJobunitId("unitId");
		info.setName("jobName");
		info.setDescription("description");
		info.setType(0);

		info.setReferJobId("referJobId");
		info.setReferJobUnitId("referJobUnitId");

		info.setOwnerRoleId("ALL_USERS");
		info.setParentId("ParentId");

		info.setIconId("IconId");

		JobCommandInfo command = JobCommandInfo.createSampleInfo();

		JobFileInfo file = JobFileInfo.createSampleInfo();

		JobWaitRuleInfo waitRule= createJobWaitRuleSample();

		ArrayList<JobEndStatusInfo> endStatusList = new ArrayList<JobEndStatusInfo>();
		JobEndStatusInfo endStatus = JobEndStatusInfo.createSampleInfo();
		endStatusList.add(endStatus);

		ArrayList<JobParameterInfo> paramList = new ArrayList<JobParameterInfo>();
		JobParameterInfo parameter = JobParameterInfo.createSampleInfo();
		paramList.add(parameter);

		info.setCommand(command);
		info.setFile(file);
		info.setWaitRule(waitRule);
		info.setEndStatus(endStatusList);
		info.setParam(paramList);

		return info;
	}

	/**
	 * ジョブの待ち条件に関する情報のサンプルデータを作成する
	 * 単体テスト用
	 * @return
	 */
	public static JobWaitRuleInfo createJobWaitRuleSample() {
		JobWaitRuleInfo info1 = new JobWaitRuleInfo();
		info1.setSuspend(false);

		info1.setSkip(false);
		info1.setSkipEndStatus(0);
		info1.setSkipEndValue(0);

		info1.setJobRetryFlg(false);
		info1.setJobRetryEndStatus(0);
		info1.setJobRetry(0);
		info1.setJobRetryInterval(0);

		info1.setCondition(0);
		ArrayList<JobObjectInfo> objList = new ArrayList<JobObjectInfo>();
		{
			JobObjectInfo objInfo = new JobObjectInfo();
			objInfo.setType(0);
			objInfo.setJobId("jobId");
			objInfo.setJobName("jobName");
			objInfo.setStatus(0);
			objInfo.setTime(0L);
			objInfo.setDescription("description");
			objList.add(objInfo);
		}
		ArrayList<JobObjectGroupInfo> objGroupList = new ArrayList<>();
		JobObjectGroupInfo groupInfo = new JobObjectGroupInfo();
		groupInfo.setOrderNo(0);
		groupInfo.setJobObjectList(objList);
		objGroupList.add(groupInfo);
		info1.setObjectGroup(objGroupList);
		info1.setEndCondition(false);
		info1.setEndStatus(0);
		info1.setEndValue(0);
		info1.setCalendar(false);
		info1.setCalendarId("calendarId");
		info1.setCalendarEndStatus(0);
		info1.setCalendarEndValue(0);

		info1.setStart_delay(false);
		info1.setStart_delay_session(false);
		info1.setStart_delay_session_value(0);

		info1.setStart_delay_time(false);
		info1.setStart_delay_time_value(0L);

		info1.setStart_delay_condition_type(0);

		info1.setStart_delay_notify(false);
		info1.setStart_delay_notify_priority(0);

		info1.setStart_delay_operation(false);
		info1.setStart_delay_operation_type(0);
		info1.setStart_delay_operation_end_status(0);
		info1.setStart_delay_operation_end_value(0);

		info1.setEnd_delay(false);

		info1.setEnd_delay_session(false);
		info1.setEnd_delay_session_value(0);

		info1.setEnd_delay_job(false);
		info1.setEnd_delay_job_value(0);

		info1.setEnd_delay_time(false);
		info1.setEnd_delay_time_value(0L);

		info1.setEnd_delay_condition_type(0);

		info1.setEnd_delay_notify(false);
		info1.setEnd_delay_notify_priority(0);

		info1.setEnd_delay_operation(false);
		info1.setEnd_delay_operation_type(0);
		info1.setEnd_delay_operation_end_status(0);
		info1.setEnd_delay_operation_end_value(0);

		info1.setEnd_delay_change_mount(false);
		info1.setEnd_delay_change_mount_value(1D);

		info1.setMultiplicityNotify(false);
		info1.setMultiplicityNotifyPriority(0);
		info1.setMultiplicityOperation(0);
		info1.setMultiplicityEndValue(0);

		info1.setExclusiveBranch(false);
		info1.setExclusiveBranchEndStatus(0);
		info1.setExclusiveBranchEndValue(0);
		ArrayList<JobNextJobOrderInfo> nextJobOrderInfos = new ArrayList<>();
		{
			JobNextJobOrderInfo nextJobOrderInfo1 = new JobNextJobOrderInfo();
			nextJobOrderInfo1.setJobId("job_id");
			nextJobOrderInfo1.setJobunitId("jobunit_id");
			nextJobOrderInfo1.setNextJobId("next_job_id1");
			nextJobOrderInfos.add(nextJobOrderInfo1);

			JobNextJobOrderInfo nextJobOrderInfo2 = new JobNextJobOrderInfo();
			nextJobOrderInfo2.setJobId("job_id");
			nextJobOrderInfo2.setJobunitId("jobunit_id");
			nextJobOrderInfo2.setNextJobId("next_job_id2");
			nextJobOrderInfos.add(nextJobOrderInfo2);
		}
		info1.setExclusiveBranchNextJobOrderList(nextJobOrderInfos);

		return info1;
	}

	public Object cloneDeepWaitRuleOnly() throws CloneNotSupportedException {
		JobInfo jobInfo = (JobInfo) super.clone();
		if (this.waitRule != null) {
			jobInfo.waitRule = (JobWaitRuleInfo) this.waitRule.clone();
		}
		return jobInfo;
	}
}