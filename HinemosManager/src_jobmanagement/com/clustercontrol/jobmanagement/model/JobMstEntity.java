/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.util.List;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CryptUtil;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;



/**
 * The persistent class for the cc_job_mst database table.
 *
 */
@Entity
@Table(name="cc_job_mst", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.JOB,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="jobunit_id", insertable=false, updatable=false))
public class JobMstEntity extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private JobMstEntityPK id;
	private String description;
	private String jobName;
	private Integer jobType;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private Boolean registeredModule;
	// cc_job_command_mst
	private String argument;
	private String argumentJobId;
	private Boolean specifyUser;
	private String effectiveUser;
	private Boolean messageRetryEndFlg;
	private Integer messageRetryEndValue;
	private Boolean commandRetryFlg;
	private Integer commandRetryEndStatus;
	private Boolean jobRetryFlg;
	private Integer jobRetryEndStatus;
	private String facilityId;
	private Integer processMode;
	private String startCommand;
	private Integer stopType;
	private String stopCommand;
	private Boolean managerDistribution;
	private String scriptName;
	private String scriptEncoding;
	private String scriptContent;
	// cc_job_start_mst
	private Boolean calendar;
	private Integer calendarEndStatus;
	private Integer calendarEndValue;
	private Integer conditionType;
	private Boolean endDelay;
	private Integer endDelayConditionType;
	private Boolean endDelayJob;
	private Integer endDelayJobValue;
	private Boolean endDelayNotify;
	private Integer endDelayNotifyPriority;
	private Boolean endDelayOperation;
	private Integer endDelayOperationEndStatus;
	private Integer endDelayOperationEndValue;
	private Integer endDelayOperationType;
	private Boolean endDelaySession;
	private Integer endDelaySessionValue;
	private Boolean endDelayTime;
	private Long endDelayTimeValue;
	private Boolean endDelayChangeMount;
	private Double endDelayChangeMountValue;
	private Boolean multiplicity_notify;
	private Integer multiplicity_notify_priority;
	private Integer multiplicity_operation;
	private Integer multiplicity_end_value;
	private Integer messageRetry;
	private Integer commandRetry;
	private Integer jobRetry;
	private Integer jobRetryInterval;
	private Boolean skip;
	private Integer skipEndStatus;
	private Integer skipEndValue;
	private Boolean startDelay;
	private Integer startDelayConditionType;
	private Boolean startDelayNotify;
	private Integer startDelayNotifyPriority;
	private Boolean startDelayOperation;
	private Integer startDelayOperationEndStatus;
	private Integer startDelayOperationEndValue;
	private Integer startDelayOperationType;
	private Boolean startDelaySession;
	private Integer startDelaySessionValue;
	private Boolean startDelayTime;
	private Long startDelayTimeValue;
	private Boolean suspend;
	private Boolean unmatchEndFlg;
	private Integer unmatchEndStatus;
	private Integer unmatchEndValue;
	private Boolean exclusiveBranchFlg;
	private Integer exclusiveBranchEndStatus;
	private Integer exclusiveBranchEndValue;
	private String calendarId;
	// cc_job_file_mst
	private Boolean checkFlg;
	private Boolean compressionFlg;
	private String destDirectory;
	private String destWorkDir;
	private String srcFile;
	private String srcWorkDir;
	private String srcFacilityId;
	private String destFacilityId;

	//ジョブ通知関連
	private String notifyGroupId;
	private Integer beginPriority;
	private Integer normalPriority;
	private Integer warnPriority;
	private Integer abnormalPriority;

	// 終了値
	private Integer normalEndValue;
	private Integer normalEndValueFrom;
	private Integer normalEndValueTo;
	private Integer warnEndValue;
	private Integer warnEndValueFrom;
	private Integer warnEndValueTo;
	private Integer abnormalEndValue;
	private Integer abnormalEndValueFrom;
	private Integer abnormalEndValueTo;

	private List<JobParamMstEntity> jobParamMstEntities;
	private List<JobCommandParamMstEntity> jobCommandParamEntities;
	private List<JobEnvVariableMstEntity> jobEnvVariableMstEntities;
	private List<JobNextJobOrderMstEntity> jobNextJobOrderMstEntities;
	private List<JobRpaOptionMstEntity> jobRpaOptionMstEntities;
	private List<JobRpaEndValueConditionMstEntity> jobRpaEndValueConditionMstEntities;
	private List<JobRpaRunParamMstEntity> jobRpaRunParamMstEntities;
	private List<JobRpaCheckEndValueMstEntity> jobRpaCheckEndValueMstEntities;
	private String parentJobunitId;
	private String parentJobId;

	private String referJobUnitId;
	private String referJobId;
	private Integer referJobSelectType;

	private String iconId;
	
	private String approvalReqRoleId;
	private String approvalReqUserId;
	private String approvalReqSentence;
	private String approvalReqMailTitle;
	private String approvalReqMailBody;
	private Boolean useApprovalReqSentence;

	private String monitorId;
	private Integer monitorInfoEndValue;
	private Integer monitorWarnEndValue;
	private Integer monitorCriticalEndValue;
	private Integer monitorUnknownEndValue;
	private Integer monitorWaitTime;
	private Integer monitorWaitEndValue;

	// リソース制御ジョブ情報
	private String resourceCloudScopeId;
	private String resourceLocationId;
	private Integer resourceType;
	private Integer resourceAction;
	private String resourceTargetId;
	private Integer resourceStatusConfirmTime;
	private Integer resourceStatusConfirmInterval;
	private String resourceAttachNode;
	private String resourceAttachDevice;
	private Integer resourceSuccessValue;
	private Integer resourceFailureValue;

	// ジョブ同時実行制御キュー
	private Boolean queueFlg;
	private String queueId;

	// ジョブセッション事前生成
	private Boolean expNodeRuntimeFlg;

	// 失敗した場合再実行する
	private Boolean retryFlg;
	// 再実行回数
	private Integer retryCount;
	// 失敗時の操作
	private Integer failureOperation;
	// ジョブ連携メッセージID
	private String joblinkMessageId;
	// 重要度
	private Integer priority;
	// メッセージ
	private String message;
	// 確認期間フラグ
	private Boolean pastFlg;
	// 確認期間（分）
	private Integer pastMin;
	// 重要度（情報）有効/無効
	private Boolean infoValidFlg;
	// 重要度（警告）有効/無効
	private Boolean warnValidFlg;
	// 重要度（危険）有効/無効
	private Boolean criticalValidFlg;
	// 重要度（不明）有効/無効
	private Boolean unknownValidFlg;
	// アプリケーションフラグ
	private Boolean applicationFlg;
	// アプリケーション
	private String application;
	// 監視詳細フラグ
	private Boolean monitorDetailIdFlg;
	// 監視詳細
	private String monitorDetailId;
	// メッセージフラグ
	private Boolean messageFlg;
	// 拡張情報フラグ
	private Boolean expFlg;
	// 終了値 - 「常に」フラグ
	private Boolean monitorAllEndValueFlg;
	// 終了値 - 「常に」
	private Integer monitorAllEndValue;
	// 条件を満たした場合の終了値
	private Integer successEndValue;
	// 条件を満たさない場合に終了するか
	private Boolean failureEndFlg;
	// タイムアウト（分）
	private Integer failureWaitTime;
	// 条件を満たさない場合の終了値
	private Integer failureEndValue;
	// ディレクトリ
	private String directory;
	// ファイル名（正規表現）
	private String fileName;
	// チェック種別 - 作成
	private Boolean createValidFlg;
	// ジョブ開始前に作成されたファイルも対象とする
	private Boolean createBeforeJobStartFlg;
	// チェック種別 - 削除
	private Boolean deleteValidFlg;
	// チェック種別 - 変更
	private Boolean modifyValidFlg;
	// 変更判定（タイムスタンプ変更/ファイルサイズ変更）
	private Integer modifyType;
	// ファイルの使用中は判定しないか
	private Boolean notJudgeFileInUseFlg;
	// ジョブ連携送信設定ID
	private String joblinkSendSettingId;
	// 失敗時の終了状態
	private Integer failureEndStatus;

	// ジョブ連携の引継ぎ設定
	private List<JobLinkInheritMstEntity> jobLinkInheritMstEntities;

	// ジョブ連携メッセージの拡張情報設定
	private List<JobLinkJobExpMstEntity> jobLinkJobExpMstEntities;

	// 標準出力ファイル情報
	private List<JobOutputMstEntity> jobOutputMstEntities;

	// 待ち条件グループ情報
	private List<JobWaitGroupMstEntity> jobWaitGroupMstEntities;

	// RPAシナリオジョブ
	// RPAシナリオジョブ種別（直接実行/間接実行）
	private Integer rpaJobType;
	// 直接実行
	// RPAツール種別（RPAツールID）
	private String rpaToolId;
	// 実行ファイルパス
	private String rpaExeFilepath;
	// シナリオファイルパス
	private String rpaScenarioFilepath;
	// RPAツールログファイルディレクトリ
	private String rpaLogDirectory;
	// RPAツールログファイル名
	private String rpaLogFileName;
	// RPAツールログファイルエンコーディング
	private String rpaLogEncoding;
	// RPAツールログファイル改行コード
	private String rpaLogReturnCode;
	// RPAツールログファイル先頭パターン
	private String rpaLogPatternHead; 
	// RPAツールログファイル終端パターン
	private String rpaLogPatternTail; 
	// RPAツールログファイル最大読み取り文字数
	private Integer rpaLogMaxBytes;
	// いずれの条件にも一致しなかった場合の終了値
	private Integer rpaDefaultEndValue;
	// シナリオの実行前後でログインする
	private Boolean rpaLoginFlg;
	// ログインユーザID
	private String rpaLoginUserId;
	// ログインパスワード
	private String rpaLoginPassword;
	// ログインできない場合のリトライ回数
	private Integer rpaLoginRetry;
	// ログインできない場合の終了値
	private Integer rpaLoginEndValue;
	// ログイン解像度
	private String rpaLoginResolution;
	// 異常発生時もログアウトする
	private Boolean rpaLogoutFlg;
	// 終了遅延発生時にスクリーンショットを取得する
	private Boolean rpaScreenshotEndDelayFlg;
	// 終了値が条件を満たした場合にスクリーンショットを取得する
	private Boolean rpaScreenshotEndValueFlg;
	// スクリーンショットを取得する終了値
	private String rpaScreenshotEndValue;
	// スクリーンショットを取得する終了値判定条件
	private Integer rpaScreenshotEndValueCondition;
	// ログインできない場合に通知する
	private Boolean rpaNotLoginNotify;
	// ログインできない場合の通知重要度
	private Integer rpaNotLoginNotifyPriority;
	// ログインできない場合の終了値
	private Integer rpaNotLoginEndValue;
	// RPAツールが既に起動している場合に通知する
	private Boolean rpaAlreadyRunningNotify;
	// RPAツールが既に起動している場合の通知重要度
	private Integer rpaAlreadyRunningNotifyPriority;
	// RPAツールが既に起動している場合の終了値
	private Integer rpaAlreadyRunningEndValue;
	// RPAツールが異常終了した場合に通知する
	private Boolean rpaAbnormalExitNotify;
	// RPAツールが異常終了した場合の通知重要度
	private Integer rpaAbnormalExitNotifyPriority;
	// RPAツールが異常終了した場合の終了値
	private Integer rpaAbnormalExitEndValue;
	// 間接実行
	// RPA管理ツールアカウント（RPAスコープID）
	private String rpaScopeId;
	// RPA管理ツール実行種別
	private Integer rpaRunType;
	// シナリオ入力パラメータ
	private String rpaScenarioParam;
	// 停止種別（シナリオを終了する/シナリオは終了せず、ジョブのみ終了する）
	private Integer rpaStopType;
	// 停止方法
	private Integer rpaStopMode;
	// コネクションタイムアウト
	private Integer rpaRunConnectTimeout;
	// リクエストタイムアウト
	private Integer rpaRunRequestTimeout;
	// 実行できない場合に終了する
	private Boolean rpaRunEndFlg;
	// 実行できない場合のリトライ回数
	private Integer rpaRunRetry;
	// 実行できない場合の終了値
	private Integer rpaRunEndValue;
	// 結果が確認できない場合に終了する
	private Integer rpaCheckConnectTimeout;
	// 結果が確認できない場合のリトライ回数
	private Integer rpaCheckRequestTimeout;
	// 結果が確認できない場合の終了値
	private Boolean rpaCheckEndFlg;
	// 結果が確認できない場合のリトライ回数
	private Integer rpaCheckRetry;
	// 結果が確認できない場合の終了値
	private Integer rpaCheckEndValue;
	// RPA管理ツールアカウント
	private RpaManagementToolAccount rpaManagementToolAccount;
	

	@Deprecated
	public JobMstEntity() {
	}

	public JobMstEntity(JobMstEntityPK pk, Integer jobType) {
		this.setId(pk);
		this.jobType = jobType;
		this.setObjectId(this.getId().getJobunitId());
	}

	public JobMstEntity(String jobunitId, String jobId, Integer jobType) {
		this(new JobMstEntityPK(jobunitId, jobId), jobType);
	}


	@EmbeddedId
	public JobMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobMstEntityPK id) {
		this.id = id;
	}


	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="job_name")
	public String getJobName() {
		return this.jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}


	@Column(name="job_type")
	public Integer getJobType() {
		return this.jobType;
	}

	public void setJobType(Integer jobType) {
		this.jobType = jobType;
	}


	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}


	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}


	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	@Column(name="registered_module")
	public Boolean isRegisteredModule() {
		return registeredModule;
	}

	public void setRegisteredModule(Boolean regist) {
		this.registeredModule = regist;
	}


	// cc_job_command_mst
	@Column(name="argument")
	public String getArgument() {
		return this.argument;
	}

	public void setArgument(String argument) {
		this.argument = argument;
	}


	@Column(name="argument_job_id")
	public String getArgumentJobId() {
		return this.argumentJobId;
	}

	public void setArgumentJobId(String argumentJobId) {
		this.argumentJobId = argumentJobId;
	}


	@Column(name="specify_user")
	public Boolean getSpecifyUser() {
		return this.specifyUser;
	}

	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}


	@Column(name="effective_user")
	public String getEffectiveUser() {
		return this.effectiveUser;
	}

	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}


	@Column(name="message_retry_end_flg")
	public Boolean getMessageRetryEndFlg() {
		return this.messageRetryEndFlg;
	}

	public void setMessageRetryEndFlg(Boolean messageRetryEndFlg) {
		this.messageRetryEndFlg = messageRetryEndFlg;
	}


	@Column(name="message_retry_end_value")
	public Integer getMessageRetryEndValue() {
		return this.messageRetryEndValue;
	}

	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.messageRetryEndValue = messageRetryEndValue;
	}


	@Column(name="command_retry_flg")
	public Boolean getCommandRetryFlg() {
		return this.commandRetryFlg;
	}

	public void setCommandRetryFlg(Boolean commandRetryFlg) {
		this.commandRetryFlg = commandRetryFlg;
	}

	@Column(name="command_retry_end_status")
	public Integer getCommandRetryEndStatus() {
		return commandRetryEndStatus;
	}

	public void setCommandRetryEndStatus(Integer commandRetryEndStatus) {
		this.commandRetryEndStatus = commandRetryEndStatus;
	}

	@Column(name="job_retry_flg")
	public Boolean getJobRetryFlg() {
		return jobRetryFlg;
	}

	public void setJobRetryFlg(Boolean jobRetryFlg) {
		this.jobRetryFlg = jobRetryFlg;
	}

	@Column(name="job_retry_end_status")
	public Integer getJobRetryEndStatus() {
		return jobRetryEndStatus;
	}

	public void setJobRetryEndStatus(Integer jobRetryEndStatus) {
		this.jobRetryEndStatus = jobRetryEndStatus;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	@Column(name="process_mode")
	public Integer getProcessMode() {
		return this.processMode;
	}

	public void setProcessMode(Integer processMode) {
		this.processMode = processMode;
	}


	@Column(name="start_command")
	public String getStartCommand() {
		return this.startCommand;
	}

	public void setStartCommand(String startCommand) {
		this.startCommand = startCommand;
	}

	@Column(name="stop_type")
	public Integer getStopType() {
		return this.stopType;
	}

	public void setStopType(Integer stopType) {
		this.stopType = stopType;
	}

	@Column(name="stop_command")
	public String getStopCommand() {
		return this.stopCommand;
	}

	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
	}

	@Column(name="manager_distribution")
	public Boolean getManagerDistribution() {
		return this.managerDistribution;
	}

	public void setManagerDistribution(Boolean managerDistribution) {
		this.managerDistribution = managerDistribution;
	}

	@Column(name="script_name")
	public String getScriptName() {
		return this.scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	@Column(name="script_encoding")
	public String getScriptEncoding() {
		return this.scriptEncoding;
	}

	public void setScriptEncoding(String scriptEncoding) {
		this.scriptEncoding = scriptEncoding;
	}
	
	@Column(name="script_content")
	public String getScriptContent() {
		return this.scriptContent;
	}

	public void setScriptContent(String scriptContent) {
		this.scriptContent = scriptContent;
	}
	
	// cc_job_start_mst


	@Column(name="calendar")
	public Boolean getCalendar() {
		return this.calendar;
	}

	public void setCalendar(Boolean calendar) {
		this.calendar = calendar;
	}


	@Column(name="calendar_end_status")
	public Integer getCalendarEndStatus() {
		return this.calendarEndStatus;
	}

	public void setCalendarEndStatus(Integer calendarEndStatus) {
		this.calendarEndStatus = calendarEndStatus;
	}


	@Column(name="calendar_end_value")
	public Integer getCalendarEndValue() {
		return this.calendarEndValue;
	}

	public void setCalendarEndValue(Integer calendarEndValue) {
		this.calendarEndValue = calendarEndValue;
	}


	@Column(name="condition_type")
	public Integer getConditionType() {
		return this.conditionType;
	}

	public void setConditionType(Integer conditionType) {
		this.conditionType = conditionType;
	}


	@Column(name="end_delay")
	public Boolean getEndDelay() {
		return this.endDelay;
	}

	public void setEndDelay(Boolean endDelay) {
		this.endDelay = endDelay;
	}


	@Column(name="end_delay_condition_type")
	public Integer getEndDelayConditionType() {
		return this.endDelayConditionType;
	}

	public void setEndDelayConditionType(Integer endDelayConditionType) {
		this.endDelayConditionType = endDelayConditionType;
	}


	@Column(name="end_delay_job")
	public Boolean getEndDelayJob() {
		return this.endDelayJob;
	}

	public void setEndDelayJob(Boolean endDelayJob) {
		this.endDelayJob = endDelayJob;
	}


	@Column(name="end_delay_job_value")
	public Integer getEndDelayJobValue() {
		return this.endDelayJobValue;
	}

	public void setEndDelayJobValue(Integer endDelayJobValue) {
		this.endDelayJobValue = endDelayJobValue;
	}


	@Column(name="end_delay_notify")
	public Boolean getEndDelayNotify() {
		return this.endDelayNotify;
	}

	public void setEndDelayNotify(Boolean endDelayNotify) {
		this.endDelayNotify = endDelayNotify;
	}


	@Column(name="end_delay_notify_priority")
	public Integer getEndDelayNotifyPriority() {
		return this.endDelayNotifyPriority;
	}

	public void setEndDelayNotifyPriority(Integer endDelayNotifyPriority) {
		this.endDelayNotifyPriority = endDelayNotifyPriority;
	}


	@Column(name="end_delay_operation")
	public Boolean getEndDelayOperation() {
		return this.endDelayOperation;
	}

	public void setEndDelayOperation(Boolean endDelayOperation) {
		this.endDelayOperation = endDelayOperation;
	}


	@Column(name="end_delay_operation_end_status")
	public Integer getEndDelayOperationEndStatus() {
		return this.endDelayOperationEndStatus;
	}

	public void setEndDelayOperationEndStatus(Integer endDelayOperationEndStatus) {
		this.endDelayOperationEndStatus = endDelayOperationEndStatus;
	}


	@Column(name="end_delay_operation_end_value")
	public Integer getEndDelayOperationEndValue() {
		return this.endDelayOperationEndValue;
	}

	public void setEndDelayOperationEndValue(Integer endDelayOperationEndValue) {
		this.endDelayOperationEndValue = endDelayOperationEndValue;
	}


	@Column(name="end_delay_operation_type")
	public Integer getEndDelayOperationType() {
		return this.endDelayOperationType;
	}

	public void setEndDelayOperationType(Integer endDelayOperationType) {
		this.endDelayOperationType = endDelayOperationType;
	}


	@Column(name="end_delay_session")
	public Boolean getEndDelaySession() {
		return this.endDelaySession;
	}

	public void setEndDelaySession(Boolean endDelaySession) {
		this.endDelaySession = endDelaySession;
	}


	@Column(name="end_delay_session_value")
	public Integer getEndDelaySessionValue() {
		return this.endDelaySessionValue;
	}

	public void setEndDelaySessionValue(Integer endDelaySessionValue) {
		this.endDelaySessionValue = endDelaySessionValue;
	}


	@Column(name="end_delay_time")
	public Boolean getEndDelayTime() {
		return this.endDelayTime;
	}

	public void setEndDelayTime(Boolean endDelayTime) {
		this.endDelayTime = endDelayTime;
	}


	@Column(name="end_delay_time_value")
	public Long getEndDelayTimeValue() {
		return this.endDelayTimeValue;
	}

	public void setEndDelayTimeValue(Long endDelayTimeValue) {
		this.endDelayTimeValue = endDelayTimeValue;
	}

	@Column(name="end_delay_change_mount")
	public Boolean getEndDelayChangeMount() {
		return this.endDelayChangeMount;
	}

	public void setEndDelayChangeMount(Boolean endDelayChangeMount) {
		this.endDelayChangeMount = endDelayChangeMount;
	}

	@Column(name="end_delay_change_mount_value")
	public Double getEndDelayChangeMountValue() {
		return this.endDelayChangeMountValue;
	}

	public void setEndDelayChangeMountValue(Double endDelayChangeMountValue) {
		this.endDelayChangeMountValue = endDelayChangeMountValue;
	}

	@Column(name="message_retry")
	public Integer getMessageRetry() {
		return this.messageRetry;
	}

	public void setMessageRetry(Integer messageRetry) {
		this.messageRetry = messageRetry;
	}

	@Column(name="command_retry")
	public Integer getCommandRetry() {
		return this.commandRetry;
	}

	public void setCommandRetry(Integer commandRetry) {
		this.commandRetry = commandRetry;
	}

	@Column(name="job_retry")
	public Integer getJobRetry() {
		return jobRetry;
	}

	public void setJobRetry(Integer jobRetry) {
		this.jobRetry = jobRetry;
	}

	@Column(name="multiplicity_notify")
	public Boolean getMultiplicityNotify() {
		return this.multiplicity_notify;
	}

	public void setMultiplicityNotify(Boolean multiplicity_notify) {
		this.multiplicity_notify = multiplicity_notify;
	}

	@Column(name="multiplicity_notify_priority")
	public Integer getMultiplicityNotifyPriority() {
		return this.multiplicity_notify_priority;
	}

	public void setMultiplicityNotifyPriority(Integer multiplicity_notify_priority) {
		this.multiplicity_notify_priority = multiplicity_notify_priority;
	}

	@Column(name="multiplicity_operation")
	public Integer getMultiplicityOperation() {
		return this.multiplicity_operation;
	}

	public void setMultiplicityOperation(Integer multiplicity_operation) {
		this.multiplicity_operation = multiplicity_operation;
	}

	@Column(name="multiplicity_end_value")
	public Integer getMultiplicityEndValue() {
		return this.multiplicity_end_value;
	}

	public void setMultiplicityEndValue(Integer multiplicity_end_value) {
		this.multiplicity_end_value = multiplicity_end_value;
	}

	@Column(name="skip")
	public Boolean getSkip() {
		return this.skip;
	}

	public void setSkip(Boolean skip) {
		this.skip = skip;
	}


	@Column(name="skip_end_status")
	public Integer getSkipEndStatus() {
		return this.skipEndStatus;
	}

	public void setSkipEndStatus(Integer skipEndStatus) {
		this.skipEndStatus = skipEndStatus;
	}


	@Column(name="skip_end_value")
	public Integer getSkipEndValue() {
		return this.skipEndValue;
	}

	public void setSkipEndValue(Integer skipEndValue) {
		this.skipEndValue = skipEndValue;
	}


	@Column(name="start_delay")
	public Boolean getStartDelay() {
		return this.startDelay;
	}

	public void setStartDelay(Boolean startDelay) {
		this.startDelay = startDelay;
	}


	@Column(name="start_delay_condition_type")
	public Integer getStartDelayConditionType() {
		return this.startDelayConditionType;
	}

	public void setStartDelayConditionType(Integer startDelayConditionType) {
		this.startDelayConditionType = startDelayConditionType;
	}


	@Column(name="start_delay_notify")
	public Boolean getStartDelayNotify() {
		return this.startDelayNotify;
	}

	public void setStartDelayNotify(Boolean startDelayNotify) {
		this.startDelayNotify = startDelayNotify;
	}


	@Column(name="start_delay_notify_priority")
	public Integer getStartDelayNotifyPriority() {
		return this.startDelayNotifyPriority;
	}

	public void setStartDelayNotifyPriority(Integer startDelayNotifyPriority) {
		this.startDelayNotifyPriority = startDelayNotifyPriority;
	}


	@Column(name="start_delay_operation")
	public Boolean getStartDelayOperation() {
		return this.startDelayOperation;
	}

	public void setStartDelayOperation(Boolean startDelayOperation) {
		this.startDelayOperation = startDelayOperation;
	}


	@Column(name="start_delay_operation_end_status")
	public Integer getStartDelayOperationEndStatus() {
		return this.startDelayOperationEndStatus;
	}

	public void setStartDelayOperationEndStatus(Integer startDelayOperationEndStatus) {
		this.startDelayOperationEndStatus = startDelayOperationEndStatus;
	}


	@Column(name="start_delay_operation_end_value")
	public Integer getStartDelayOperationEndValue() {
		return this.startDelayOperationEndValue;
	}

	public void setStartDelayOperationEndValue(Integer startDelayOperationEndValue) {
		this.startDelayOperationEndValue = startDelayOperationEndValue;
	}


	@Column(name="start_delay_operation_type")
	public Integer getStartDelayOperationType() {
		return this.startDelayOperationType;
	}

	public void setStartDelayOperationType(Integer startDelayOperationType) {
		this.startDelayOperationType = startDelayOperationType;
	}


	@Column(name="start_delay_session")
	public Boolean getStartDelaySession() {
		return this.startDelaySession;
	}

	public void setStartDelaySession(Boolean startDelaySession) {
		this.startDelaySession = startDelaySession;
	}


	@Column(name="start_delay_session_value")
	public Integer getStartDelaySessionValue() {
		return this.startDelaySessionValue;
	}

	public void setStartDelaySessionValue(Integer startDelaySessionValue) {
		this.startDelaySessionValue = startDelaySessionValue;
	}


	@Column(name="start_delay_time")
	public Boolean getStartDelayTime() {
		return this.startDelayTime;
	}

	public void setStartDelayTime(Boolean startDelayTime) {
		this.startDelayTime = startDelayTime;
	}


	@Column(name="start_delay_time_value")
	public Long getStartDelayTimeValue() {
		return this.startDelayTimeValue;
	}

	public void setStartDelayTimeValue(Long startDelayTimeValue) {
		this.startDelayTimeValue = startDelayTimeValue;
	}


	@Column(name="suspend")
	public Boolean getSuspend() {
		return this.suspend;
	}

	public void setSuspend(Boolean suspend) {
		this.suspend = suspend;
	}


	@Column(name="unmatch_end_flg")
	public Boolean getUnmatchEndFlg() {
		return this.unmatchEndFlg;
	}

	public void setUnmatchEndFlg(Boolean unmatchEndFlg) {
		this.unmatchEndFlg = unmatchEndFlg;
	}


	@Column(name="unmatch_end_status")
	public Integer getUnmatchEndStatus() {
		return this.unmatchEndStatus;
	}

	public void setUnmatchEndStatus(Integer unmatchEndStatus) {
		this.unmatchEndStatus = unmatchEndStatus;
	}


	@Column(name="unmatch_end_value")
	public Integer getUnmatchEndValue() {
		return this.unmatchEndValue;
	}

	public void setUnmatchEndValue(Integer unmatchEndValue) {
		this.unmatchEndValue = unmatchEndValue;
	}
	@Column(name="exclusive_branch_flg")
	public Boolean getExclusiveBranchFlg() {
		return this.exclusiveBranchFlg;
	}

	public void setExclusiveBranchFlg(Boolean exclusiveBranchFlg) {
		this.exclusiveBranchFlg = exclusiveBranchFlg;
	}

	@Column(name="exclusive_branch_end_status")
	public Integer getExclusiveBranchEndStatus() {
		return this.exclusiveBranchEndStatus;
	}

	public void setExclusiveBranchEndStatus(Integer exclusiveBranchEndStatus) {
		this.exclusiveBranchEndStatus = exclusiveBranchEndStatus;
	}

	@Column(name="exclusive_branch_end_value")
	public Integer getExclusiveBranchEndValue() {
		return this.exclusiveBranchEndValue;
	}

	public void setExclusiveBranchEndValue(Integer exclusiveBranchEndValue) {
		this.exclusiveBranchEndValue = exclusiveBranchEndValue;
	}

	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	// cc_job_file_mst
	@Column(name="check_flg")
	public Boolean getCheckFlg() {
		return this.checkFlg;
	}

	public void setCheckFlg(Boolean checkFlg) {
		this.checkFlg = checkFlg;
	}


	@Column(name="compression_flg")
	public Boolean getCompressionFlg() {
		return this.compressionFlg;
	}

	public void setCompressionFlg(Boolean compressionFlg) {
		this.compressionFlg = compressionFlg;
	}


	@Column(name="dest_directory")
	public String getDestDirectory() {
		return this.destDirectory;
	}

	public void setDestDirectory(String destDirectory) {
		this.destDirectory = destDirectory;
	}


	@Column(name="dest_work_dir")
	public String getDestWorkDir() {
		return this.destWorkDir;
	}

	public void setDestWorkDir(String destWorkDir) {
		this.destWorkDir = destWorkDir;
	}


	@Column(name="src_file")
	public String getSrcFile() {
		return this.srcFile;
	}

	public void setSrcFile(String srcFile) {
		this.srcFile = srcFile;
	}


	@Column(name="src_work_dir")
	public String getSrcWorkDir() {
		return this.srcWorkDir;
	}

	public void setSrcWorkDir(String srcWorkDir) {
		this.srcWorkDir = srcWorkDir;
	}


	@Column(name="src_facility_id")
	public String getSrcFacilityId() {
		return this.srcFacilityId;
	}

	public void setSrcFacilityId(String srcFacilityId) {
		this.srcFacilityId = srcFacilityId;
	}


	@Column(name="dest_facility_id")
	public String getDestFacilityId() {
		return this.destFacilityId;
	}

	public void setDestFacilityId(String destFacilityId) {
		this.destFacilityId = destFacilityId;
	}

	@Column(name="parent_jobunit_id")
	public String getParentJobunitId() {
		return parentJobunitId;
	}

	public void setParentJobunitId(String parentJobunitId) {
		this.parentJobunitId = parentJobunitId;
	}

	@Column(name="parent_job_id")
	public String getParentJobId() {
		return parentJobId;
	}

	public void setParentJobId(String parentJobId) {
		this.parentJobId = parentJobId;
	}

	@Column(name="refer_jobunit_id")
	public String getReferJobUnitId() {
		return referJobUnitId;
	}

	public void setReferJobUnitId(String referJobUnitId) {
		this.referJobUnitId = referJobUnitId;
	}

	@Column(name="refer_job_id")
	public String getReferJobId() {
		return referJobId;
	}

	public void setReferJobId(String referJobId) {
		this.referJobId = referJobId;
	}

	@Column(name="refer_job_select_type")
	public Integer getReferJobSelectType() {
		return referJobSelectType;
	}

	public void setReferJobSelectType(Integer referJobSelectType) {
		this.referJobSelectType = referJobSelectType;
	}

	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	@Column(name="begin_priority")
	public Integer getBeginPriority() {
		return beginPriority;
	}

	public void setBeginPriority(Integer beginPriority) {
		this.beginPriority = beginPriority;
	}

	@Column(name="normal_priority")
	public Integer getNormalPriority() {
		return normalPriority;
	}

	public void setNormalPriority(Integer normalPriority) {
		this.normalPriority = normalPriority;
	}

	@Column(name="warn_priority")
	public Integer getWarnPriority() {
		return warnPriority;
	}

	public void setWarnPriority(Integer warnPriority) {
		this.warnPriority = warnPriority;
	}

	@Column(name="abnormal_priority")
	public Integer getAbnormalPriority() {
		return abnormalPriority;
	}

	public void setAbnormalPriority(Integer abnormalPriority) {
		this.abnormalPriority = abnormalPriority;
	}

	@Column(name="normal_end_value")
	public Integer getNormalEndValue() {
		return normalEndValue;
	}

	public void setNormalEndValue(Integer normalEndValue) {
		this.normalEndValue = normalEndValue;
	}

	@Column(name="normal_end_value_from")
	public Integer getNormalEndValueFrom() {
		return normalEndValueFrom;
	}

	public void setNormalEndValueFrom(Integer normalEndValueFrom) {
		this.normalEndValueFrom = normalEndValueFrom;
	}

	@Column(name="normal_end_value_to")
	public Integer getNormalEndValueTo() {
		return normalEndValueTo;
	}

	public void setNormalEndValueTo(Integer normalEndValueTo) {
		this.normalEndValueTo = normalEndValueTo;
	}

	@Column(name="warn_end_value")
	public Integer getWarnEndValue() {
		return warnEndValue;
	}

	public void setWarnEndValue(Integer warnEndValue) {
		this.warnEndValue = warnEndValue;
	}

	@Column(name="warn_end_value_from")
	public Integer getWarnEndValueFrom() {
		return warnEndValueFrom;
	}

	public void setWarnEndValueFrom(Integer warnEndValueFrom) {
		this.warnEndValueFrom = warnEndValueFrom;
	}

	@Column(name="warn_end_value_to")
	public Integer getWarnEndValueTo() {
		return warnEndValueTo;
	}

	public void setWarnEndValueTo(Integer warnEndValueTo) {
		this.warnEndValueTo = warnEndValueTo;
	}

	@Column(name="abnormal_end_value")
	public Integer getAbnormalEndValue() {
		return abnormalEndValue;
	}

	public void setAbnormalEndValue(Integer abnormalEndValue) {
		this.abnormalEndValue = abnormalEndValue;
	}

	@Column(name="abnormal_end_value_from")
	public Integer getAbnormalEndValueFrom() {
		return abnormalEndValueFrom;
	}

	public void setAbnormalEndValueFrom(Integer abnormalEndValueFrom) {
		this.abnormalEndValueFrom = abnormalEndValueFrom;
	}

	@Column(name="abnormal_end_value_to")
	public Integer getAbnormalEndValueTo() {
		return abnormalEndValueTo;
	}

	public void setAbnormalEndValueTo(Integer abnormalEndValueTo) {
		this.abnormalEndValueTo = abnormalEndValueTo;
	}

	@Column(name="icon_id")
	public String getIconId() {
		return this.iconId;
	}

	public void setIconId(String iconId) {
		this.iconId = iconId;
	}

	@Column(name="approval_req_role_id")
	public String getApprovalReqRoleId() {
		return approvalReqRoleId;
	}

	public void setApprovalReqRoleId(String approvalReqRoleId) {
		this.approvalReqRoleId = approvalReqRoleId;
	}

	@Column(name="approval_req_user_id")
	public String getApprovalReqUserId() {
		return approvalReqUserId;
	}

	public void setApprovalReqUserId(String approvalReqUserId) {
		this.approvalReqUserId = approvalReqUserId;
	}

	@Column(name="approval_req_sentence")
	public String getApprovalReqSentence() {
		return approvalReqSentence;
	}

	public void setApprovalReqSentence(String approvalReqSentence) {
		this.approvalReqSentence = approvalReqSentence;
	}

	@Column(name="approval_req_mail_title")
	public String getApprovalReqMailTitle() {
		return approvalReqMailTitle;
	}

	public void setApprovalReqMailTitle(String approvalReqMailTitle) {
		this.approvalReqMailTitle = approvalReqMailTitle;
	}

	@Column(name="approval_req_mail_body")
	public String getApprovalReqMailBody() {
		return approvalReqMailBody;
	}

	public void setApprovalReqMailBody(String approvalReqMailBody) {
		this.approvalReqMailBody = approvalReqMailBody;
	}

	@Column(name="use_approval_req_sentence")
	public Boolean isUseApprovalReqSentence() {
		return useApprovalReqSentence;
	}

	public void setUseApprovalReqSentence(Boolean useApprovalReqSentence) {
		this.useApprovalReqSentence = useApprovalReqSentence;
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="monitor_info_end_value")
	public Integer getMonitorInfoEndValue() {
		return this.monitorInfoEndValue;
	}

	public void setMonitorInfoEndValue(Integer monitorInfoEndValue) {
		this.monitorInfoEndValue = monitorInfoEndValue;
	}

	@Column(name="monitor_warn_end_value")
	public Integer getMonitorWarnEndValue() {
		return this.monitorWarnEndValue;
	}

	public void setMonitorWarnEndValue(Integer monitorWarnEndValue) {
		this.monitorWarnEndValue = monitorWarnEndValue;
	}

	@Column(name="monitor_critical_end_value")
	public Integer getMonitorCriticalEndValue() {
		return this.monitorCriticalEndValue;
	}

	public void setMonitorCriticalEndValue(Integer monitorCriticalEndValue) {
		this.monitorCriticalEndValue = monitorCriticalEndValue;
	}

	@Column(name="monitor_unknown_end_value")
	public Integer getMonitorUnknownEndValue() {
		return this.monitorUnknownEndValue;
	}

	public void setMonitorUnknownEndValue(Integer monitorUnknownEndValue) {
		this.monitorUnknownEndValue = monitorUnknownEndValue;
	}

	@Column(name="monitor_wait_time")
	public Integer getMonitorWaitTime() {
		return this.monitorWaitTime;
	}

	public void setMonitorWaitTime(Integer monitorWaitTime) {
		this.monitorWaitTime = monitorWaitTime;
	}

	@Column(name="monitor_wait_end_value")
	public Integer getMonitorWaitEndValue() {
		return this.monitorWaitEndValue;
	}

	public void setMonitorWaitEndValue(Integer monitorWaitEndValue) {
		this.monitorWaitEndValue = monitorWaitEndValue;
	}

	@Column(name="resource_cloud_scope_id")
	public String getResourceCloudScopeId() {
		return resourceCloudScopeId;
	}

	public void setResourceCloudScopeId(String resourceCloudScopeId) {
		this.resourceCloudScopeId = resourceCloudScopeId;
	}

	@Column(name="resource_location_id")
	public String getResourceLocationId() {
		return resourceLocationId;
	}

	public void setResourceLocationId(String resourceLocationId) {
		this.resourceLocationId = resourceLocationId;
	}

	@Column(name="resource_type")
	public Integer getResourceType() {
		return resourceType;
	}

	public void setResourceType(Integer resourceType) {
		this.resourceType = resourceType;
	}

	@Column(name="resource_action")
	public Integer getResourceAction() {
		return resourceAction;
	}

	public void setResourceAction(Integer resourceAction) {
		this.resourceAction = resourceAction;
	}

	@Column(name="resource_target_id")
	public String getResourceTargetId() {
		return resourceTargetId;
	}

	public void setResourceTargetId(String resourceTargetId) {
		this.resourceTargetId = resourceTargetId;
	}

	@Column(name="resource_status_confirm_time")
	public Integer getResourceStatusConfirmTime() {
		return resourceStatusConfirmTime;
	}

	public void setResourceStatusConfirmTime(Integer resourceStatusConfirmTime) {
		this.resourceStatusConfirmTime = resourceStatusConfirmTime;
	}

	@Column(name="resource_status_confirm_interval")
	public Integer getResourceStatusConfirmInterval() {
		return resourceStatusConfirmInterval;
	}

	public void setResourceStatusConfirmInterval(Integer resourceStatusConfirmInterval) {
		this.resourceStatusConfirmInterval = resourceStatusConfirmInterval;
	}

	@Column(name="resource_attach_node")
	public String getResourceAttachNode() {
		return resourceAttachNode;
	}

	public void setResourceAttachNode(String resourceAttachNode) {
		this.resourceAttachNode = resourceAttachNode;
	}

	@Column(name="resource_attach_device")
	public String getResourceAttachDevice() {
		return resourceAttachDevice;
	}

	public void setResourceAttachDevice(String resourceAttachDevice) {
		this.resourceAttachDevice = resourceAttachDevice;
	}

	@Column(name="resource_success_value")
	public Integer getResourceSuccessValue() {
		return resourceSuccessValue;
	}

	public void setResourceSuccessValue(Integer resourceSuccessValue) {
		this.resourceSuccessValue = resourceSuccessValue;
	}

	@Column(name="resource_failure_value")
	public Integer getResourceFailureValue() {
		return resourceFailureValue;
	}

	public void setResourceFailureValue(Integer resourceFailureValue) {
		this.resourceFailureValue = resourceFailureValue;
	}

	@Column(name="queue_flg")
	public Boolean getQueueFlg() {
		return queueFlg;
	}

	public void setQueueFlg(Boolean queueFlg) {
		this.queueFlg = queueFlg;
	}

	@Column(name="queue_id")
	public String getQueueId() {
		return queueId;
	}
	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	@Column(name="exp_node_runtime_flg")
	public Boolean getExpNodeRuntimeFlg() {
		return expNodeRuntimeFlg;
	}
	public void setExpNodeRuntimeFlg(Boolean expNodeRuntimeFlg) {
		this.expNodeRuntimeFlg = expNodeRuntimeFlg;
	}

	@Column(name="job_retry_interval")
	public Integer getJobRetryInterval() {
		return jobRetryInterval;
	}
	public void setJobRetryInterval(Integer jobRetryInterval) {
		this.jobRetryInterval = jobRetryInterval;
	}

	@Column(name="retry_flg")
	public Boolean getRetryFlg() {
		return retryFlg;
	}
	public void setRetryFlg(Boolean retryFlg) {
		this.retryFlg = retryFlg;
	}

	@Column(name="retry_count")
	public Integer getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	@Column(name="failure_operation")
	public Integer getFailureOperation() {
		return failureOperation;
	}
	public void setFailureOperation(Integer failureOperation) {
		this.failureOperation = failureOperation;
	}

	@Column(name="joblink_message_id")
	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	@Column(name="priority")
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Column(name="message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name="past_flg")
	public Boolean getPastFlg() {
		return pastFlg;
	}
	public void setPastFlg(Boolean pastFlg) {
		this.pastFlg = pastFlg;
	}

	@Column(name="past_min")
	public Integer getPastMin() {
		return pastMin;
	}
	public void setPastMin(Integer pastMin) {
		this.pastMin = pastMin;
	}

	@Column(name="info_valid_flg")
	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}
	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	@Column(name="warn_valid_flg")
	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}
	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	@Column(name="critical_valid_flg")
	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}
	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	@Column(name="unknown_valid_flg")
	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}
	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	@Column(name="application_flg")
	public Boolean getApplicationFlg() {
		return applicationFlg;
	}
	public void setApplicationFlg(Boolean applicationFlg) {
		this.applicationFlg = applicationFlg;
	}

	@Column(name="application")
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}

	@Column(name="monitor_detail_id_flg")
	public Boolean getMonitorDetailIdFlg() {
		return monitorDetailIdFlg;
	}
	public void setMonitorDetailIdFlg(Boolean monitorDetailIdFlg) {
		this.monitorDetailIdFlg = monitorDetailIdFlg;
	}

	@Column(name="monitor_detail_id")
	public String getMonitorDetailId() {
		return monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	@Column(name="message_flg")
	public Boolean getMessageFlg() {
		return messageFlg;
	}
	public void setMessageFlg(Boolean messageFlg) {
		this.messageFlg = messageFlg;
	}

	@Column(name="exp_flg")
	public Boolean getExpFlg() {
		return expFlg;
	}
	public void setExpFlg(Boolean expFlg) {
		this.expFlg = expFlg;
	}

	@Column(name="monitor_all_end_value_flg")
	public Boolean getMonitorAllEndValueFlg() {
		return monitorAllEndValueFlg;
	}
	public void setMonitorAllEndValueFlg(Boolean monitorAllEndValueFlg) {
		this.monitorAllEndValueFlg = monitorAllEndValueFlg;
	}

	@Column(name="monitor_all_end_value")
	public Integer getMonitorAllEndValue() {
		return monitorAllEndValue;
	}
	public void setMonitorAllEndValue(Integer monitorAllEndValue) {
		this.monitorAllEndValue = monitorAllEndValue;
	}

	@Column(name="success_end_value")
	public Integer getSuccessEndValue() {
		return successEndValue;
	}
	public void setSuccessEndValue(Integer successEndValue) {
		this.successEndValue = successEndValue;
	}

	@Column(name="failure_end_flg")
	public Boolean getFailureEndFlg() {
		return failureEndFlg;
	}
	public void setFailureEndFlg(Boolean failureEndFlg) {
		this.failureEndFlg = failureEndFlg;
	}

	@Column(name="failure_wait_time")
	public Integer getFailureWaitTime() {
		return failureWaitTime;
	}
	public void setFailureWaitTime(Integer failureWaitTime) {
		this.failureWaitTime = failureWaitTime;
	}

	@Column(name="failure_end_value")
	public Integer getFailureEndValue() {
		return failureEndValue;
	}
	public void setFailureEndValue(Integer failureEndValue) {
		this.failureEndValue = failureEndValue;
	}

	@Column(name="directory")
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Column(name="file_name")
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name="create_valid_flg")
	public Boolean getCreateValidFlg() {
		return createValidFlg;
	}
	public void setCreateValidFlg(Boolean createValidFlg) {
		this.createValidFlg = createValidFlg;
	}

	@Column(name="create_before_job_start_flg")
	public Boolean getCreateBeforeJobStartFlg() {
		return createBeforeJobStartFlg;
	}
	public void setCreateBeforeJobStartFlg(Boolean createBeforeJobStartFlg) {
		this.createBeforeJobStartFlg = createBeforeJobStartFlg;
	}

	@Column(name="delete_valid_flg")
	public Boolean getDeleteValidFlg() {
		return deleteValidFlg;
	}
	public void setDeleteValidFlg(Boolean deleteValidFlg) {
		this.deleteValidFlg = deleteValidFlg;
	}

	@Column(name="modify_valid_flg")
	public Boolean getModifyValidFlg() {
		return modifyValidFlg;
	}
	public void setModifyValidFlg(Boolean modifyValidFlg) {
		this.modifyValidFlg = modifyValidFlg;
	}

	@Column(name="modify_type")
	public Integer getModifyType() {
		return modifyType;
	}
	public void setModifyType(Integer modifyType) {
		this.modifyType = modifyType;
	}

	@Column(name="not_judge_file_in_use_flg")
	public Boolean getNotJudgeFileInUseFlg() {
		return notJudgeFileInUseFlg;
	}
	public void setNotJudgeFileInUseFlg(Boolean notJudgeFileInUseFlg) {
		this.notJudgeFileInUseFlg = notJudgeFileInUseFlg;
	}

	@Column(name="joblink_send_setting_id")
	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}
	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}

	@Column(name="failure_end_status")
	public Integer getFailureEndStatus() {
		return failureEndStatus;
	}
	public void setFailureEndStatus(Integer failureEndStatus) {
		this.failureEndStatus = failureEndStatus;
	}

	@Column(name="rpa_job_type")
	public Integer getRpaJobType() {
		return rpaJobType;
	}

	public void setRpaJobType(Integer rpaJobType) {
		this.rpaJobType = rpaJobType;
	}

	@Column(name="rpa_tool_id")
	public String getRpaToolId() {
		return rpaToolId;
	}

	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	@Column(name="rpa_exe_filepath")
	public String getRpaExeFilepath() {
		return rpaExeFilepath;
	}

	public void setRpaExeFilepath(String rpaExeFilepath) {
		this.rpaExeFilepath = rpaExeFilepath;
	}

	@Column(name="rpa_scenario_filepath")
	public String getRpaScenarioFilepath() {
		return rpaScenarioFilepath;
	}

	public void setRpaScenarioFilepath(String rpaScenarioFilepath) {
		this.rpaScenarioFilepath = rpaScenarioFilepath;
	}

	@Column(name="rpa_log_directory")
	public String getRpaLogDirectory() {
		return rpaLogDirectory;
	}

	public void setRpaLogDirectory(String rpaLogDirectory) {
		this.rpaLogDirectory = rpaLogDirectory;
	}

	@Column(name="rpa_log_file_name")
	public String getRpaLogFileName() {
		return rpaLogFileName;
	}

	public void setRpaLogFileName(String rpaLogFileName) {
		this.rpaLogFileName = rpaLogFileName;
	}

	@Column(name="rpa_log_encoding")
	public String getRpaLogEncoding() {
		return rpaLogEncoding;
	}

	public void setRpaLogEncoding(String rpaLogEncoding) {
		this.rpaLogEncoding = rpaLogEncoding;
	}

	@Column(name="rpa_log_return_code")
	public String getRpaLogReturnCode() {
		return rpaLogReturnCode;
	}

	public void setRpaLogReturnCode(String rpaLogReturnCode) {
		this.rpaLogReturnCode = rpaLogReturnCode;
	}

	@Column(name="rpa_log_pattern_head")
	public String getRpaLogPatternHead() {
		return rpaLogPatternHead;
	}

	public void setRpaLogPatternHead(String rpaLogPatternHead) {
		this.rpaLogPatternHead = rpaLogPatternHead;
	}

	@Column(name="rpa_log_pattern_tail")
	public String getRpaLogPatternTail() {
		return rpaLogPatternTail;
	}

	public void setRpaLogPatternTail(String rpaLogPatternTail) {
		this.rpaLogPatternTail = rpaLogPatternTail;
	}

	@Column(name="rpa_log_max_bytes")
	public Integer getRpaLogMaxBytes() {
		return rpaLogMaxBytes;
	}

	public void setRpaLogMaxBytes(Integer rpaLogMaxBytes) {
		this.rpaLogMaxBytes = rpaLogMaxBytes;
	}

	@Column(name="rpa_default_end_value")
	public Integer getRpaDefaultEndValue() {
		return rpaDefaultEndValue;
	}

	public void setRpaDefaultEndValue(Integer rpaDefaultEndValue) {
		this.rpaDefaultEndValue = rpaDefaultEndValue;
	}

	@Column(name="rpa_login_flg")
	public Boolean getRpaLoginFlg() {
		return rpaLoginFlg;
	}

	public void setRpaLoginFlg(Boolean rpaLoginFlg) {
		this.rpaLoginFlg = rpaLoginFlg;
	}

	@Column(name="rpa_login_user_id")
	public String getRpaLoginUserId() {
		return rpaLoginUserId;
	}

	public void setRpaLoginUserId(String rpaLoginUserId) {
		this.rpaLoginUserId = rpaLoginUserId;
	}

	@Transient
	public String getRpaLoginPassword() {
		return CryptUtil.decrypt(getRpaLoginPasswordCrypt());
	}

	public void setRpaLoginPassword(String rpaLoginPassword) {
		setRpaLoginPasswordCrypt(CryptUtil.encrypt(rpaLoginPassword));
	}

	@Column(name="rpa_login_password")
	public String getRpaLoginPasswordCrypt() {
		return rpaLoginPassword;
	}

	public void setRpaLoginPasswordCrypt(String rpaLoginPassword) {
		this.rpaLoginPassword = rpaLoginPassword;
	}

	@Column(name="rpa_login_retry")
	public Integer getRpaLoginRetry() {
		return rpaLoginRetry;
	}

	public void setRpaLoginRetry(Integer rpaLoginRetry) {
		this.rpaLoginRetry = rpaLoginRetry;
	}

	@Column(name="rpa_login_end_value")
	public Integer getRpaLoginEndValue() {
		return rpaLoginEndValue;
	}

	public void setRpaLoginEndValue(Integer rpaLoginEndValue) {
		this.rpaLoginEndValue = rpaLoginEndValue;
	}

	@Column(name="rpa_login_resolution")
	public String getRpaLoginResolution() {
		return rpaLoginResolution;
	}

	public void setRpaLoginResolution(String rpaLoginResolution) {
		this.rpaLoginResolution = rpaLoginResolution;
	}

	@Column(name="rpa_logout_flg")
	public Boolean getRpaLogoutFlg() {
		return rpaLogoutFlg;
	}

	public void setRpaLogoutFlg(Boolean rpaLogoutFlg) {
		this.rpaLogoutFlg = rpaLogoutFlg;
	}

	@Column(name="rpa_screenshot_end_delay_flg")
	public Boolean getRpaScreenshotEndDelayFlg() {
		return rpaScreenshotEndDelayFlg;
	}

	public void setRpaScreenshotEndDelayFlg(Boolean rpaScreenshotEndDelayFlg) {
		this.rpaScreenshotEndDelayFlg = rpaScreenshotEndDelayFlg;
	}

	@Column(name="rpa_screenshot_end_value_flg")
	public Boolean getRpaScreenshotEndValueFlg() {
		return rpaScreenshotEndValueFlg;
	}

	public void setRpaScreenshotEndValueFlg(Boolean rpaScrlenshotEndValueFlg) {
		this.rpaScreenshotEndValueFlg = rpaScrlenshotEndValueFlg;
	}

	@Column(name="rpa_screenshot_end_value")
	public String getRpaScreenshotEndValue() {
		return rpaScreenshotEndValue;
	}

	public void setRpaScreenshotEndValue(String rpaScreenshotEndValue) {
		this.rpaScreenshotEndValue = rpaScreenshotEndValue;
	}

	@Column(name="rpa_screenshot_end_value_condition")
	public Integer getRpaScreenshotEndValueCondition() {
		return rpaScreenshotEndValueCondition;
	}

	public void setRpaScreenshotEndValueCondition(Integer rpaScreenshotEndValueCondition) {
		this.rpaScreenshotEndValueCondition = rpaScreenshotEndValueCondition;
	}

	@Column(name="rpa_not_login_notify")
	public Boolean getRpaNotLoginNotify() {
		return rpaNotLoginNotify;
	}

	public void setRpaNotLoginNotify(Boolean rpaNotLoginNotify) {
		this.rpaNotLoginNotify = rpaNotLoginNotify;
	}

	@Column(name="rpa_not_login_notify_priority")
	public Integer getRpaNotLoginNotifyPriority() {
		return rpaNotLoginNotifyPriority;
	}

	public void setRpaNotLoginNotifyPriority(Integer rpaNotLoginNotifyPriority) {
		this.rpaNotLoginNotifyPriority = rpaNotLoginNotifyPriority;
	}

	@Column(name="rpa_not_login_end_value")
	public Integer getRpaNotLoginEndValue() {
		return rpaNotLoginEndValue;
	}

	public void setRpaNotLoginEndValue(Integer rpaNotLoginEndValue) {
		this.rpaNotLoginEndValue = rpaNotLoginEndValue;
	}

	@Column(name="rpa_already_running_notify")
	public Boolean getRpaAlreadyRunningNotify() {
		return rpaAlreadyRunningNotify;
	}

	public void setRpaAlreadyRunningNotify(Boolean rpaAlreadyRunningNotify) {
		this.rpaAlreadyRunningNotify = rpaAlreadyRunningNotify;
	}

	@Column(name="rpa_already_running_notify_priority")
	public Integer getRpaAlreadyRunningNotifyPriority() {
		return rpaAlreadyRunningNotifyPriority;
	}

	public void setRpaAlreadyRunningNotifyPriority(Integer rpaAlreadyRunningNotifyPriority) {
		this.rpaAlreadyRunningNotifyPriority = rpaAlreadyRunningNotifyPriority;
	}

	@Column(name="rpa_already_running_end_value")
	public Integer getRpaAlreadyRunningEndValue() {
		return rpaAlreadyRunningEndValue;
	}

	public void setRpaAlreadyRunningEndValue(Integer rpaAlreadyRunningEndValue) {
		this.rpaAlreadyRunningEndValue = rpaAlreadyRunningEndValue;
	}

	@Column(name="rpa_abnormal_exit_notify")
	public Boolean getRpaAbnormalExitNotify() {
		return rpaAbnormalExitNotify;
	}

	public void setRpaAbnormalExitNotify(Boolean rpaAbnormalExitNotify) {
		this.rpaAbnormalExitNotify = rpaAbnormalExitNotify;
	}

	@Column(name="rpa_abnormal_exit_notify_priority")
	public Integer getRpaAbnormalExitNotifyPriority() {
		return rpaAbnormalExitNotifyPriority;
	}

	public void setRpaAbnormalExitNotifyPriority(Integer rpaAbnormalExitNotifyPriority) {
		this.rpaAbnormalExitNotifyPriority = rpaAbnormalExitNotifyPriority;
	}

	@Column(name="rpa_abnormal_exit_end_value")
	public Integer getRpaAbnormalExitEndValue() {
		return rpaAbnormalExitEndValue;
	}

	public void setRpaAbnormalExitEndValue(Integer rpaAbnormalExitEndValue) {
		this.rpaAbnormalExitEndValue = rpaAbnormalExitEndValue;
	}

	@Column(name="rpa_scope_id")
	public String getRpaScopeId() {
		return rpaScopeId;
	}

	public void setRpaScopeId(String rpaScopeId) {
		this.rpaScopeId = rpaScopeId;
	}

	@Column(name="rpa_run_type")
	public Integer getRpaRunType() {
		return rpaRunType;
	}

	public void setRpaRunType(Integer rpaRunType) {
		this.rpaRunType = rpaRunType;
	}

	@Column(name="rpa_scenario_param")
	public String getRpaScenarioParam() {
		return rpaScenarioParam;
	}

	public void setRpaScenarioParam(String rpaScenarioParam) {
		this.rpaScenarioParam = rpaScenarioParam;
	}

	@Column(name="rpa_stop_type")
	public Integer getRpaStopType() {
		return this.rpaStopType;
	}

	public void setRpaStopType(Integer rpaStopType) {
		this.rpaStopType = rpaStopType;
	}

	@Column(name="rpa_stop_mode")
	public Integer getRpaStopMode() {
		return this.rpaStopMode;
	}

	public void setRpaStopMode(Integer rpaStopMode) {
		this.rpaStopMode = rpaStopMode;
	}

	@Column(name="rpa_run_connect_timeout")
	public Integer getRpaRunConnectTimeout() {
		return rpaRunConnectTimeout;
	}

	public void setRpaRunConnectTimeout(Integer rpaRunConnectTimeout) {
		this.rpaRunConnectTimeout = rpaRunConnectTimeout;
	}

	@Column(name="rpa_run_request_timeout")
	public Integer getRpaRunRequestTimeout() {
		return rpaRunRequestTimeout;
	}

	public void setRpaRunRequestTimeout(Integer rpaRunRequestTimeout) {
		this.rpaRunRequestTimeout = rpaRunRequestTimeout;
	}

	@Column(name="rpa_run_end_flg")
	public Boolean getRpaRunEndFlg() {
		return rpaRunEndFlg;
	}

	public void setRpaRunEndFlg(Boolean rpaRunEndFlg) {
		this.rpaRunEndFlg = rpaRunEndFlg;
	}

	@Column(name="rpa_run_retry")
	public Integer getRpaRunRetry() {
		return rpaRunRetry;
	}

	public void setRpaRunRetry(Integer rpaRunRetry) {
		this.rpaRunRetry = rpaRunRetry;
	}

	@Column(name="rpa_run_end_value")
	public Integer getRpaRunEndValue() {
		return rpaRunEndValue;
	}

	public void setRpaRunEndValue(Integer rpaRunEndValue) {
		this.rpaRunEndValue = rpaRunEndValue;
	}

	@Column(name="rpa_check_connect_timeout")
	public Integer getRpaCheckConnectTimeout() {
		return rpaCheckConnectTimeout;
	}

	public void setRpaCheckConnectTimeout(Integer rpaCheckConnectTimeout) {
		this.rpaCheckConnectTimeout = rpaCheckConnectTimeout;
	}

	@Column(name="rpa_check_request_timeout")
	public Integer getRpaCheckRequestTimeout() {
		return rpaCheckRequestTimeout;
	}

	public void setRpaCheckRequestTimeout(Integer rpaCheckRequestTimeout) {
		this.rpaCheckRequestTimeout = rpaCheckRequestTimeout;
	}

	@Column(name="rpa_check_end_flg")
	public Boolean getRpaCheckEndFlg() {
		return rpaCheckEndFlg;
	}

	public void setRpaCheckEndFlg(Boolean rpaCheckEndFlg) {
		this.rpaCheckEndFlg = rpaCheckEndFlg;
	}

	@Column(name="rpa_check_retry")
	public Integer getRpaCheckRetry() {
		return rpaCheckRetry;
	}

	public void setRpaCheckRetry(Integer rpaCheckRetry) {
		this.rpaCheckRetry = rpaCheckRetry;
	}

	@Column(name="rpa_check_end_value")
	public Integer getRpaCheckEndValue() {
		return rpaCheckEndValue;
	}

	public void setRpaCheckEndValue(Integer rpaCheckEndValue) {
		this.rpaCheckEndValue = rpaCheckEndValue;
	}

	//bi-directional many-to-one association to JobParamMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobParamMstEntity> getJobParamMstEntities() {
		return this.jobParamMstEntities;
	}

	public void setJobParamMstEntities(List<JobParamMstEntity> jobParamMstEntities) {
		this.jobParamMstEntities = jobParamMstEntities;
	}

	//bi-directional many-to-one association to JobParamMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobEnvVariableMstEntity> getJobEnvVariableMstEntities() {
		return this.jobEnvVariableMstEntities;
	}

	public void setJobEnvVariableMstEntities(List<JobEnvVariableMstEntity> jobEnvVariableMstEntities) {
		this.jobEnvVariableMstEntities = jobEnvVariableMstEntities;
	}

	//bi-directional many-to-one association to JobCommandParamMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobCommandParamMstEntity> getJobCommandParamEntities() {
		return this.jobCommandParamEntities;
	}

	public void setJobCommandParamEntities(List<JobCommandParamMstEntity> jobCommandParamEntities) {
		this.jobCommandParamEntities = jobCommandParamEntities;
	}

	//bi-directional many-to-one association to JobNextJobOrderMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobNextJobOrderMstEntity> getJobNextJobOrderMstEntities() {
		return this.jobNextJobOrderMstEntities;
	}

	public void setJobNextJobOrderMstEntities(List<JobNextJobOrderMstEntity> jobNextJobOrderMstEntities) {
		this.jobNextJobOrderMstEntities = jobNextJobOrderMstEntities;
	}

	//bi-directional many-to-one association to JobLinkInheritMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobLinkInheritMstEntity> getJobLinkInheritMstEntities() {
		return this.jobLinkInheritMstEntities;
	}

	public void setJobLinkInheritMstEntities(List<JobLinkInheritMstEntity> jobLinkInheritMstEntities) {
		this.jobLinkInheritMstEntities = jobLinkInheritMstEntities;
	}

	//bi-directional many-to-one association to JobLinkJobExpMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobLinkJobExpMstEntity> getJobLinkJobExpMstEntities() {
		return this.jobLinkJobExpMstEntities;
	}

	public void setJobLinkJobExpMstEntities(List<JobLinkJobExpMstEntity> jobLinkJobExpMstEntities) {
		this.jobLinkJobExpMstEntities = jobLinkJobExpMstEntities;
	}

	//bi-directional many-to-one association to JobOutputMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobOutputMstEntity> getJobOutputMstEntities() {
		return this.jobOutputMstEntities;
	}

	public void setJobOutputMstEntities(List<JobOutputMstEntity> jobOutputMstEntities) {
		this.jobOutputMstEntities = jobOutputMstEntities;
	}

	//bi-directional many-to-one association to JobWaitGroupMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobWaitGroupMstEntity> getJobWaitGroupMstEntities() {
		return this.jobWaitGroupMstEntities;
	}

	public void setJobWaitGroupMstEntities(List<JobWaitGroupMstEntity> jobWaitGroupMstEntities) {
		this.jobWaitGroupMstEntities = jobWaitGroupMstEntities;
	}

	//bi-directional many-to-one association to JobRpaOptionMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobRpaOptionMstEntity> getJobRpaOptionMstEntities() {
		return this.jobRpaOptionMstEntities;
	}

	public void setJobRpaOptionMstEntities(List<JobRpaOptionMstEntity> jobRpaOptionMstEntities) {
		this.jobRpaOptionMstEntities = jobRpaOptionMstEntities;
	}

	//bi-directional many-to-one association to JobRpaEndValueMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobRpaEndValueConditionMstEntity> getJobRpaEndValueConditionMstEntities() {
		return this.jobRpaEndValueConditionMstEntities;
	}

	public void setJobRpaEndValueConditionMstEntities(List<JobRpaEndValueConditionMstEntity> jobRpaEndValueConditionMstEntities) {
		this.jobRpaEndValueConditionMstEntities = jobRpaEndValueConditionMstEntities;
	}

	//bi-directional many-to-one association to JobRpaRunParamMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobRpaRunParamMstEntity> getJobRpaRunParamMstEntities() {
		return this.jobRpaRunParamMstEntities;
	}

	public void setJobRpaRunParamMstEntities(List<JobRpaRunParamMstEntity> jobRpaRunParamMstEntities) {
		this.jobRpaRunParamMstEntities = jobRpaRunParamMstEntities;
	}

	//bi-directional many-to-one association to JobRpaCheckEndValueMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobRpaCheckEndValueMstEntity> getJobRpaCheckEndValueMstEntities() {
		return this.jobRpaCheckEndValueMstEntities;
	}

	public void setJobRpaCheckEndValueMstEntities(List<JobRpaCheckEndValueMstEntity> jobRpaCheckEndValueMstEntities) {
		this.jobRpaCheckEndValueMstEntities = jobRpaCheckEndValueMstEntities;
	}

	//uni-directional many-to-one association to RpaManagementToolAccount
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="rpa_scope_id", referencedColumnName = "rpa_scope_id", insertable = false, updatable = false)
	public RpaManagementToolAccount getRpaManagementToolAccount() {
		return rpaManagementToolAccount;
	}

	public void setRpaManagementToolAccount(RpaManagementToolAccount rpaManagementToolAccount) {
		this.rpaManagementToolAccount = rpaManagementToolAccount;
	}
}