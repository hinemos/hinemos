/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;



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
	// cc_job_start_time_mst
	private Long startTime;
	private String startTimeDescription;
	private Integer startMinute;
	private String startMinuteDescription;

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
	private List<JobStartJobMstEntity> jobStartJobMstEntities;
	private List<JobCommandParamMstEntity> jobCommandParamEntities;
	private List<JobEnvVariableMstEntity> jobEnvVariableMstEntities;
	private List<JobStartParamMstEntity> jobStartParamMstEntities;
	private List<JobNextJobOrderMstEntity> jobNextJobOrderMstEntities;
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

	// ジョブ同時実行制御キュー
	private Boolean queueFlg;
	private String queueId;
	
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


	// cc_job_start_time_mst
	@Column(name="start_time")
	public Long getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	@Column(name="start_time_description")
	public String getStartTimeDescription() {
		return this.startTimeDescription;
	}

	public void setStartTimeDescription(String description) {
		this.startTimeDescription = description;
	}

	@Column(name="start_minute")
	public Integer getStartMinute() {
		return this.startMinute;
	}

	public void setStartMinute(Integer startMinute) {
		this.startMinute = startMinute;
	}

	@Column(name="start_minute_description")
	public String getStartMinuteDescription() {
		return this.startMinuteDescription;
	}

	public void setStartMinuteDescription(String description) {
		this.startMinuteDescription = description;
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

	//bi-directional many-to-one association to JobStartJobMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobStartJobMstEntity> getJobStartJobMstEntities() {
		return this.jobStartJobMstEntities;
	}

	public void setJobStartJobMstEntities(List<JobStartJobMstEntity> jobStartJobMstEntities) {
		this.jobStartJobMstEntities = jobStartJobMstEntities;
	}

	//bi-directional many-to-one association to JobCommandParamMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobCommandParamMstEntity> getJobCommandParamEntities() {
		return this.jobCommandParamEntities;
	}

	public void setJobCommandParamEntities(List<JobCommandParamMstEntity> jobCommandParamEntities) {
		this.jobCommandParamEntities = jobCommandParamEntities;
	}

	//bi-directional many-to-one association to JobStartParamMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobStartParamMstEntity> getJobStartParamMstEntities() {
		return this.jobStartParamMstEntities;
	}

	public void setJobStartParamMstEntities(List<JobStartParamMstEntity> jobStartParamMstEntities) {
		this.jobStartParamMstEntities = jobStartParamMstEntities;
	}

	//bi-directional many-to-one association to JobNextJobOrderMstEntity
	@OneToMany(mappedBy="jobMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobNextJobOrderMstEntity> getJobNextJobOrderMstEntities() {
		return this.jobNextJobOrderMstEntities;
	}

	public void setJobNextJobOrderMstEntities(List<JobNextJobOrderMstEntity> jobNextJobOrderMstEntities) {
		this.jobNextJobOrderMstEntities = jobNextJobOrderMstEntities;
	}
}