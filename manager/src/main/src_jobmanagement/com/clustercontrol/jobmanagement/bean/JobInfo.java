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
public class JobInfo implements Serializable {

	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobInfo.class );

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -1453680330220941926L;

	/**
	 * ジョブツリーの情報だけの場合はfalse
	 * 全てのプロパティ値が入っている場合はtrue
	 **/
	private boolean propertyFull = false;

	/** 所属ジョブユニットのジョブID */
	private String m_jobunitId;

	/** ジョブID */
	private String m_id;

	/**
	 * 親ジョブID
	 * ModifyJob.java内部でのみ利用する。
	 * それ以外では利用しないこと。
	 **/
	private String m_parentId;

	/** ジョブ名 */
	private String m_name;

	/** ジョブ種別 com.clustercontrol.bean.JobConstant */
	private Integer m_type = 0;

	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfo m_waitRule;

	/** ジョブコマンド情報 */
	private JobCommandInfo m_command;

	/** ジョブファイル転送情報 */
	private JobFileInfo m_file;

	/** 監視ジョブコ情報 */
	private MonitorJobInfo m_monitor;

	/** ジョブ終了状態情報 */
	private ArrayList<JobEndStatusInfo> m_endStatus;

	/** ジョブ変数情報 */
	private ArrayList<JobParameterInfo> m_param;

	/** 参照先ジョブユニットID */
	private String m_referJobUnitId;

	/** 参照先ジョブID */
	private String m_referJobId;

	/** アイコンID */
	private String m_iconId;

	/** 参照ジョブ選択種別 */
	private Integer m_referJobSelectType = 0;

	/** 作成日時 */
	private Long m_createTime;

	/** 最終更新日時 */
	private Long m_updateTime;

	/** 新規作成ユーザ */
	private String m_createUser;

	/** 最終更新ユーザ */
	private String m_updateUser;

	/** 説明 */
	private String m_description = "";

	/** オーナーロールID */
	private String m_ownerRoleId = "";
	
	/** モジュール登録済フラグ */
	private boolean m_registered = false;
	
	/** 承認依頼先ロールID */
	private String m_approvalReqRoleId = "";
	
	/** 承認依頼先ユーザID */
	private String m_approvalReqUserId = "";
	
	/** 承認依頼文 */
	private String m_approvalReqSentence = "";
	
	/** 承認依頼メール件名 */
	private String m_approvalReqMailTitle = "";
	
	/** 承認依頼メール本文 */
	private String m_approvalReqMailBody = "";
	
	/** 承認依頼文の利用有無フラグ */
	private boolean m_isUseApprovalReqSentence = false;
	
	//ジョブ通知関連
	private Integer beginPriority = 0;
	private Integer normalPriority = 0;
	private Integer warnPriority = 0;
	private Integer abnormalPriority = 0;
	/** 通知ID**/
	private ArrayList<NotifyRelationInfo> m_notifyRelationInfos;


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
	public boolean isPropertyFull() {
		return propertyFull;
	}

	public void setPropertyFull(boolean propertyFull) {
		this.propertyFull = propertyFull;
	}

	/**
	 * ジョブ終了状態情報を返す。<BR>
	 * 
	 * @return ジョブ終了状態情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public ArrayList<JobEndStatusInfo> getEndStatus() {
		return m_endStatus;

	}

	/**
	 * ジョブ終了状態情報を設定する
	 * 
	 * @param endStatus ジョブ終了状態情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public void setEndStatus(ArrayList<JobEndStatusInfo> endStatus) {
		this.m_endStatus = endStatus;
	}

	/**
	 * ジョブコマンド情報を返す。<BR>
	 * 
	 * @return ジョブコマンド情報
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public JobCommandInfo getCommand() {
		return m_command;
	}

	/**
	 * ジョブコマンド情報を設定する。<BR>
	 * 
	 * @param command ジョブコマンド情報
	 * @see com.clustercontrol.jobmanagement.bean.JobCommandInfo
	 */
	public void setCommand(JobCommandInfo command) {
		this.m_command = command;
	}

	/**
	 * ジョブファイル転送情報を返す。<BR>
	 * 
	 * @return ジョブファイル転送情報
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public JobFileInfo getFile() {
		return m_file;
	}

	/**
	 * ジョブファイル転送情報を設定する。<BR>
	 * 
	 * @param file ジョブファイル転送情報
	 * @see com.clustercontrol.jobmanagement.bean.JobFileInfo
	 */
	public void setFile(JobFileInfo file) {
		this.m_file = file;
	}

	/**
	 * 監視ジョブ情報を返す。<BR>
	 * 
	 * @return 監視ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.MonitorJobInfo
	 */
	public MonitorJobInfo getMonitor() {
		return m_monitor;
	}

	/**
	 * 監視ジョブ情報を設定する。<BR>
	 * 
	 * @param monitor 監視ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.MonitorJobInfo
	 */
	public void setMonitor(MonitorJobInfo monitor) {
		this.m_monitor = monitor;
	}


	/**
	 * 所属ジョブユニットのジョブIDを返す。
	 * 
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return m_jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定します。
	 * 
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		m_jobunitId = jobunitId;
	}

	/**
	 * ジョブIDを返す。<BR>
	 * 
	 * @return ジョブID
	 */
	public String getId() {
		return m_id;
	}

	/**
	 * ジョブIDを設定する。<BR>
	 * 
	 * @param id ジョブID
	 */
	public void setId(String id) {
		this.m_id = id;
	}

	/**
	 * 親ジョブIDを返す。<BR>
	 * 
	 * @return 親ジョブID
	 */
	public String getParentId() {
		return m_parentId;
	}

	/**
	 * 親ジョブIDを設定する。<BR>
	 * 
	 * @param id 親ジョブID
	 */
	public void setParentId(String parentId) {
		this.m_parentId = parentId;
	}

	/**
	 * ジョブ変数情報を返す。<BR>
	 * 
	 * @return ジョブ変数情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobParameterInfo
	 */
	public ArrayList<JobParameterInfo> getParam() {
		return m_param;
	}

	/**
	 * ジョブ変数情報を設定する。<BR>
	 * 
	 * @param param ジョブ変数情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobParameterInfo
	 */
	public void setParam(ArrayList<JobParameterInfo> param) {
		this.m_param = param;
	}

	/**
	 * ジョブ名を返す。<BR>
	 * 
	 * @return ジョブ名
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * ジョブ名を設定する。<BR>
	 * 
	 * @param name ジョブ名
	 */
	public void setName(String name) {
		this.m_name = name;
	}

	/**
	 * モジュール登録済フラグを返す。<BR>
	 * 
	 * @return モジュール登録済フラグ
	 */
	public boolean isRegisteredModule() {
		return m_registered;
	}

	/**
	 * モジュール登録済フラグを設定する。<BR>
	 * 
	 * @param regist モジュール登録済フラグ
	 */
	public void setRegisteredModule(boolean regist) {
		this.m_registered = regist;
	}
	
	/**
	 * ジョブ待ち条件情報を返す。<BR>
	 * 
	 * @return ジョブ待ち条件情報
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public JobWaitRuleInfo getWaitRule() {
		return m_waitRule;
	}

	/**
	 * ジョブ待ち条件情報を設定する。<BR>
	 * 
	 * @param waitRule ジョブ待ち条件情報
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public void setWaitRule(JobWaitRuleInfo waitRule) {
		this.m_waitRule = waitRule;
	}

	/**
	 * ジョブ種別を返す。<BR>
	 * 
	 * @return ジョブ種別
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public Integer getType() {
		return m_type;
	}

	/**
	 * ジョブ種別を設定する。<BR>
	 * 
	 * @param type ジョブ種別
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setType(Integer type) {
		this.m_type = type;
	}

	/**
	 * 参照先ジョブユニットIDを返す。<BR>
	 * @return 参照先ジョブユニットID
	 */
	public String getReferJobUnitId() {
		return m_referJobUnitId;
	}
	/**
	 * 参照先ジョブユニットIDを設定する。<BR>
	 * @param referJobUnitId 参照先ジョブユニットID
	 */
	public void setReferJobUnitId(String referJobUnitId) {
		this.m_referJobUnitId = referJobUnitId;
	}
	/**
	 * 参照先ジョブIDを返す。<BR>
	 * @return 参照先ジョブID
	 */
	public String getReferJobId() {
		return m_referJobId;
	}
	/**
	 * 参照先ジョブIDを設定する。<BR>
	 * @param referJobId 参照先ジョブID
	 */
	public void setReferJobId(String referJobId) {
		this.m_referJobId = referJobId;
	}

	/**
	 * アイコンIDを返す。<BR>
	 * @return アイコンID
	 */
	public String getIconId() {
		return m_iconId;
	}
	/**
	 * アイコンIDを設定する。<BR>
	 * @param iconId アイコンID
	 */
	public void setIconId(String iconId) {
		this.m_iconId = iconId;
	}

	/**
	 * 参照ジョブ選択種別を返す。<BR>
	 * @return 参照ジョブ選択種別
	 */
	public Integer getReferJobSelectType() {
		return m_referJobSelectType;
	}
	/**
	 * 参照ジョブ選択種別を設定する。<BR>
	 * @param selectType 参照ジョブ選択種別
	 */
	public void setReferJobSelectType(Integer selectType) {
		this.m_referJobSelectType = selectType;
	}

	/**
	 * 作成日時を返す。<BR>
	 * @return 作成日時
	 */
	public Long getCreateTime() {
		return m_createTime;
	}

	/**
	 * 作成日時を設定する。<BR>
	 * @param createTime 作成日時
	 */
	public void setCreateTime(Long createTime) {
		this.m_createTime = createTime;
	}

	/**
	 * 最終更新日時を返す。<BR>
	 * @return 最終更新日時
	 */
	public Long getUpdateTime() {
		return m_updateTime;
	}

	/**
	 * 最終更新日時を設定する。<BR>
	 * @param updateTime 最終更新日時
	 */
	public void setUpdateTime(Long updateTime) {
		this.m_updateTime = updateTime;
	}

	/**
	 * 新規作成ユーザを返す。<BR>
	 * @return 新規作成ユーザ
	 */
	public String getCreateUser() {
		return m_createUser;
	}

	/**
	 * 新規作成ユーザを設定する。<BR>
	 * @param createUser 新規作成ユーザ
	 */
	public void setCreateUser(String createUser) {
		this.m_createUser = createUser;
	}

	/**
	 * 最終更新ユーザを返す。<BR>
	 * @return 最終更新ユーザ
	 */
	public String getUpdateUser() {
		return m_updateUser;
	}

	/**
	 * 最終更新ユーザを設定する。<BR>
	 * @param updateUser 最終更新ユーザ
	 */
	public void setUpdateUser(String updateUser) {
		this.m_updateUser = updateUser;
	}

	/**
	 * 説明を返す。<BR>
	 * @return 説明
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * 説明を設定する。<BR>
	 * @param description 説明
	 */
	public void setDescription(String description) {
		this.m_description = description;
	}

	/**
	 * オーナーロールIDを返す。<BR>
	 * @return オーナーロールID
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定する。<BR>
	 * @param ownerRoleId オーナーロールID
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
	}

	/**
	 * 承認依頼先ロールIDを返す。<BR>
	 * 
	 * @return 承認依頼先ロールID
	 */
	public String getApprovalReqRoleId() {
		return m_approvalReqRoleId;
	}

	/**
	 * 承認依頼先ロールIDを設定する。<BR>
	 * 
	 * @param approvalReqSentence 承認依頼先ロールID
	 */
	public void setApprovalReqRoleId(String approvalReqRoleId) {
		this.m_approvalReqRoleId = approvalReqRoleId;
	}

	/**
	 * 承認依頼先ユーザIDを返す。<BR>
	 * 
	 * @return 承認依頼先ユーザID
	 */
	public String getApprovalReqUserId() {
		return m_approvalReqUserId;
	}

	/**
	 * 承認依頼先ユーザIDを設定する。<BR>
	 * 
	 * @param approvalReqSentence 承認依頼先ユーザID
	 */
	public void setApprovalReqUserId(String approvalReqUserId) {
		this.m_approvalReqUserId = approvalReqUserId;
	}

	/**
	 * 承認依頼文を返す。<BR>
	 * 
	 * @return 承認依頼文
	 */
	public String getApprovalReqSentence() {
		return m_approvalReqSentence;
	}

	/**
	 * 承認依頼文を設定する。<BR>
	 * 
	 * @param approvalReqSentence 承認依頼文
	 */
	public void setApprovalReqSentence(String approvalReqSentence) {
		this.m_approvalReqSentence = approvalReqSentence;
	}

	/**
	 * 承認依頼メール件名を返す。<BR>
	 * 
	 * @return 承認依頼メール件名
	 */
	public String getApprovalReqMailTitle() {
		return m_approvalReqMailTitle;
	}

	/**
	 * 承認依頼メール件名を設定する。<BR>
	 * 
	 * @param approvalReqMailTitle 承認依頼メール件名
	 */
	public void setApprovalReqMailTitle(String approvalReqMailTitle) {
		this.m_approvalReqMailTitle = approvalReqMailTitle;
	}

	/**
	 * 承認依頼メール本文を返す。<BR>
	 * 
	 * @return 承認依頼メール本文
	 */
	public String getApprovalReqMailBody() {
		return m_approvalReqMailBody;
	}

	/**
	 * 承認依頼メール本文を設定する。<BR>
	 * 
	 * @param approvalRequesMailBody 承認依頼メール本文
	 */
	public void setApprovalReqMailBody(String approvalReqMailBody) {
		this.m_approvalReqMailBody = approvalReqMailBody;
	}

	/**
	 * 承認依頼文の利用有無フラグを返す。<BR>
	 * 
	 * @return 承認依頼文の利用有無フラグ
	 */
	public boolean isUseApprovalReqSentence() {
		return m_isUseApprovalReqSentence;
	}

	/**
	 * 承認依頼文の利用有無フラグを設定する。<BR>
	 * 
	 * @param isUseRequestSentence 承認依頼文の利用有無フラグ
	 */
	public void setUseApprovalReqSentence(boolean isUseApprovalReqSentence) {
		this.m_isUseApprovalReqSentence = isUseApprovalReqSentence;
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
		return m_notifyRelationInfos;
	}

	/**
	 * 通知IDを設定します。
	 * 
	 * @param notifyRelationInfos
	 */
	public void setNotifyRelationInfos(ArrayList<NotifyRelationInfo> notifyRelationInfos) {
		this.m_notifyRelationInfos = notifyRelationInfos;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 0;
		hash = hash * prime + this.m_id.hashCode();
		hash = hash * prime + this.m_name.hashCode();
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

		JobWaitRuleInfo waitRule= JobWaitRuleInfo.createSampleInfo();

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

}