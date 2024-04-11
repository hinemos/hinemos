/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openapitools.client.model.JobCommandInfoResponse;
import org.openapitools.client.model.JobCommandParamResponse;
import org.openapitools.client.model.JobDetailInfoResponse;
import org.openapitools.client.model.JobEndStatusInfoResponse;
import org.openapitools.client.model.JobEnvVariableInfoResponse;
import org.openapitools.client.model.JobFileCheckInfoResponse;
import org.openapitools.client.model.JobFileInfoResponse;
import org.openapitools.client.model.JobInfoRequest;
import org.openapitools.client.model.JobInfoResponse;
import org.openapitools.client.model.JobLinkRcvInfoResponse;
import org.openapitools.client.model.JobLinkSendInfoResponse;
import org.openapitools.client.model.JobMonitorInfoResponse;
import org.openapitools.client.model.JobNextJobOrderInfoResponse;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobParameterInfoResponse;
import org.openapitools.client.model.JobResourceInfoResponse;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobTreeItemRequest;
import org.openapitools.client.model.JobTreeItemResponseP1;
import org.openapitools.client.model.JobTreeItemResponseP2;
import org.openapitools.client.model.JobTreeItemResponseP3;
import org.openapitools.client.model.JobTreeItemResponseP4;
import org.openapitools.client.model.JobWaitRuleInfoResponse;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.TimezoneUtil;

/**
 * JobItemTreeユーティリティクラス
 *
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class JobTreeItemUtil {

	/** パスセパレータ */
	public static final String SEPARATOR = ">";

	/**
	 * ジョブツリーアイテムの親子関係を表現するパス文字列を返す。<BR>
	 * <p>
	 * 例）以下のジョブツリーにて、getPath()を呼び出す
	 * <p>
	 * <ul>
	 *  <li>料金システム
	 *  <ul>
	 *   <li>顧客管理
	 *   <ul>
	 *    <li>WEB  <- このインスタンスにてgetPath()を呼び出す
	 *    <li>DB
	 *   </ul>
	 *  </ul>
	 * </ul>
	 * <p>
	 * 結果 ： "料金システム>顧客管理>WEB"。<BR>
	 *
	 * @return パス文字列
	 */
	public static String getPath(JobTreeItemWrapper jobTreeItem) {

		// トップ("ジョブ")の場合は、文字を出力しません。
		if (jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.COMPOSITE) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();

		buffer.append(jobTreeItem.getData().getName());

		/*
		 * 再起呼び出しすることでもないので。
		 */

		JobTreeItemWrapper parent = jobTreeItem.getParent();
		while (parent != null
				&& parent.getData().getType() != JobInfoWrapper.TypeEnum.COMPOSITE) {

			buffer.insert(0, SEPARATOR);
			buffer.insert(0, parent.getData().getName());
			parent = parent.getParent();
		}

		return buffer.toString();
	}

	public static JobInfoWrapper getNewJobInfo(String jobunitId, JobInfoWrapper.TypeEnum type) {
		JobInfoWrapper jobInfo = JobTreeItemUtil.createJobInfoWrapper();
		jobInfo.setJobunitId(jobunitId);
		jobInfo.setId("");
		jobInfo.setName("");
		jobInfo.setType(type);

		jobInfo.setWaitRule(getNewJobWaitRuleInfo());
		return jobInfo;
	}

	public static JobWaitRuleInfoResponse getNewJobWaitRuleInfo() {
		JobWaitRuleInfoResponse jobWaitRuleInfo = JobTreeItemUtil.createJobWaitRuleInfoResponse();

		// 待ち条件タブ
		jobWaitRuleInfo.setCondition(JobWaitRuleInfoResponse.ConditionEnum.AND);
		jobWaitRuleInfo.setEndCondition(true);
		jobWaitRuleInfo.setEndStatus(JobWaitRuleInfoResponse.EndStatusEnum.ABNORMAL);
		jobWaitRuleInfo.setEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);

		// 制御タブ
		jobWaitRuleInfo.setCalendar(false);
		jobWaitRuleInfo.setCalendarEndStatus(JobWaitRuleInfoResponse.CalendarEndStatusEnum.ABNORMAL);
		jobWaitRuleInfo.setCalendarEndValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		jobWaitRuleInfo.setSuspend(false);
		jobWaitRuleInfo.setSkip(false);
		jobWaitRuleInfo.setSkipEndStatus(JobWaitRuleInfoResponse.SkipEndStatusEnum.ABNORMAL);
		jobWaitRuleInfo.setSkipEndValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		jobWaitRuleInfo.setQueueFlg(false);
		jobWaitRuleInfo.setJobRetryFlg(false);
		jobWaitRuleInfo.setJobRetryEndStatus(JobWaitRuleInfoResponse.JobRetryEndStatusEnum.NORMAL);
		jobWaitRuleInfo.setJobRetry(10);
		jobWaitRuleInfo.setJobRetryInterval(0);
		jobWaitRuleInfo.setExclusiveBranch(false);
		jobWaitRuleInfo.setExclusiveBranchEndStatus(JobWaitRuleInfoResponse.ExclusiveBranchEndStatusEnum.NORMAL);
		jobWaitRuleInfo.setExclusiveBranchEndValue(EndStatusConstant.INITIAL_VALUE_WARNING);

		// 開始遅延タブ
		jobWaitRuleInfo.setStartDelay(false);
		jobWaitRuleInfo.setStartDelayConditionType(JobWaitRuleInfoResponse.StartDelayConditionTypeEnum.AND);
		jobWaitRuleInfo.setStartDelayNotify(false);
		jobWaitRuleInfo.setStartDelayNotifyPriority(JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.CRITICAL);
		jobWaitRuleInfo.setStartDelayOperationType(JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.AT_ONCE);
		jobWaitRuleInfo.setStartDelayOperation(false);
		jobWaitRuleInfo.setStartDelayOperationEndStatus(JobWaitRuleInfoResponse.StartDelayOperationEndStatusEnum.ABNORMAL);
		jobWaitRuleInfo.setStartDelayOperationEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setStartDelaySession(false);
		jobWaitRuleInfo.setStartDelaySessionValue(1);
		jobWaitRuleInfo.setStartDelayTime(false);

		// 終了遅延タブ
		jobWaitRuleInfo.setEndDelay(false);
		jobWaitRuleInfo.setEndDelayConditionType(JobWaitRuleInfoResponse.EndDelayConditionTypeEnum.AND);
		jobWaitRuleInfo.setEndDelayJob(false);
		jobWaitRuleInfo.setEndDelayJobValue(1);
		jobWaitRuleInfo.setEndDelayNotify(false);
		jobWaitRuleInfo.setEndDelayNotifyPriority(JobWaitRuleInfoResponse.EndDelayNotifyPriorityEnum.CRITICAL);
		jobWaitRuleInfo.setEndDelayOperation(false);
		jobWaitRuleInfo.setEndDelayOperationEndStatus(JobWaitRuleInfoResponse.EndDelayOperationEndStatusEnum.ABNORMAL);
		jobWaitRuleInfo.setEndDelayOperationEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setEndDelayOperationType(JobWaitRuleInfoResponse.EndDelayOperationTypeEnum.AT_ONCE);
		jobWaitRuleInfo.setEndDelaySession(false);
		jobWaitRuleInfo.setEndDelaySessionValue(1);
		jobWaitRuleInfo.setEndDelayTime(false);
		jobWaitRuleInfo.setEndDelayChangeMount(false);
		jobWaitRuleInfo.setEndDelayChangeMountValue(1D);

		// 多重度タブ
		jobWaitRuleInfo.setMultiplicityEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setMultiplicityNotify(true);
		jobWaitRuleInfo.setMultiplicityNotifyPriority(JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.WARNING);
		jobWaitRuleInfo.setMultiplicityOperation(JobWaitRuleInfoResponse.MultiplicityOperationEnum.WAIT);
		
		return jobWaitRuleInfo;
	}

	/**
	 * @param origItem
	 * @param parentItem
	 * @return
	 */
	public static JobTreeItemWrapper clone(JobTreeItemWrapper origItem, JobTreeItemWrapper parentItem) {
		JobTreeItemWrapper cloneItem = new JobTreeItemWrapper();

		if (origItem.getData() != null) {
			JobInfoWrapper jobInfo = JobTreeItemUtil.createJobInfoWrapper();
			JobInfoWrapper orgInfo = origItem.getData();
			if (orgInfo.getCreateTime() != null) {
				jobInfo.setCreateTime(orgInfo.getCreateTime());
			}
			jobInfo.setCreateUser(orgInfo.getCreateUser());
			jobInfo.setDescription(orgInfo.getDescription());
			jobInfo.setOwnerRoleId(orgInfo.getOwnerRoleId());
			jobInfo.setRegistered(orgInfo.getRegistered());
			jobInfo.setId(orgInfo.getId());
			jobInfo.setJobunitId(orgInfo.getJobunitId());
			jobInfo.setName(orgInfo.getName());
			jobInfo.setPropertyFull(orgInfo.getPropertyFull());
			jobInfo.setType(orgInfo.getType());
			jobInfo.setIconId(orgInfo.getIconId());
			jobInfo.setExpNodeRuntimeFlg(orgInfo.getExpNodeRuntimeFlg());
			if (orgInfo.getUpdateTime() != null) {
				jobInfo.setUpdateTime(orgInfo.getUpdateTime());
			}
			jobInfo.setUpdateUser(orgInfo.getUpdateUser());

			if (orgInfo.getCommand() != null) {
				JobCommandInfoResponse jobCommandInfo =JobTreeItemUtil.createJobCommandInfoResponse();
				jobCommandInfo.setMessageRetryEndFlg(orgInfo.getCommand().getMessageRetryEndFlg());
				jobCommandInfo.setMessageRetryEndValue(orgInfo.getCommand().getMessageRetryEndValue());
				jobCommandInfo.setCommandRetry(orgInfo.getCommand().getCommandRetry());
				jobCommandInfo.setCommandRetryFlg(orgInfo.getCommand().getCommandRetryFlg());
				jobCommandInfo.setCommandRetryEndStatus(orgInfo.getCommand().getCommandRetryEndStatus());
				jobCommandInfo.setFacilityID(orgInfo.getCommand().getFacilityID());
				jobCommandInfo.setProcessingMethod(orgInfo.getCommand().getProcessingMethod());
				jobCommandInfo.setScope(orgInfo.getCommand().getScope());
				jobCommandInfo.setStartCommand(orgInfo.getCommand().getStartCommand());
				jobCommandInfo.setStopCommand(orgInfo.getCommand().getStopCommand());
				jobCommandInfo.setStopType(orgInfo.getCommand().getStopType());
				jobCommandInfo.setSpecifyUser(orgInfo.getCommand().getSpecifyUser());
				jobCommandInfo.setUser(orgInfo.getCommand().getUser());
				jobCommandInfo.setMessageRetry(orgInfo.getCommand().getMessageRetry());
				jobCommandInfo.setManagerDistribution(orgInfo.getCommand().getManagerDistribution());
				jobCommandInfo.setScriptName(orgInfo.getCommand().getScriptName());
				jobCommandInfo.setScriptEncoding(orgInfo.getCommand().getScriptEncoding());
				jobCommandInfo.setScriptContent(orgInfo.getCommand().getScriptContent());
				jobCommandInfo.getEnvVariable().addAll(orgInfo.getCommand().getEnvVariable());
				jobCommandInfo.getJobCommandParamList().addAll(orgInfo.getCommand().getJobCommandParamList());
				jobCommandInfo.setNormalJobOutputInfo(orgInfo.getCommand().getNormalJobOutputInfo());
				jobCommandInfo.setErrorJobOutputInfo(orgInfo.getCommand().getErrorJobOutputInfo());
				jobInfo.setCommand(jobCommandInfo);
			}

			if (orgInfo.getEndStatus() != null) {
				for(JobEndStatusInfoResponse item : orgInfo.getEndStatus()) {
					JobEndStatusInfoResponse jobEndStatusInfo = new JobEndStatusInfoResponse();
					jobEndStatusInfo.setEndRangeValue(item.getEndRangeValue());
					jobEndStatusInfo.setStartRangeValue(item.getStartRangeValue());
					jobEndStatusInfo.setType(item.getType());
					jobEndStatusInfo.setValue(item.getValue());
					jobInfo.getEndStatus().add(jobEndStatusInfo);
				}
			}

			if (orgInfo.getFile() != null) {
				JobFileInfoResponse jobFileInfo = new JobFileInfoResponse();
				jobFileInfo.setCheckFlg(orgInfo.getFile().getCheckFlg());
				jobFileInfo.setCompressionFlg(orgInfo.getFile().getCompressionFlg());
				jobFileInfo.setDestDirectory(orgInfo.getFile().getDestDirectory());
				jobFileInfo.setDestFacilityID(orgInfo.getFile().getDestFacilityID());
				jobFileInfo.setDestScope(orgInfo.getFile().getDestScope());
				jobFileInfo.setDestWorkDir(orgInfo.getFile().getDestWorkDir());
				jobFileInfo.setProcessingMethod(orgInfo.getFile().getProcessingMethod());
				jobFileInfo.setSrcFacilityID(orgInfo.getFile().getSrcFacilityID());
				jobFileInfo.setSrcFile(orgInfo.getFile().getSrcFile());
				jobFileInfo.setSrcScope(orgInfo.getFile().getSrcScope());
				jobFileInfo.setSrcWorkDir(orgInfo.getFile().getSrcWorkDir());
				jobFileInfo.setUser(orgInfo.getFile().getUser());
				jobFileInfo.setSpecifyUser(orgInfo.getFile().getSpecifyUser());
				jobFileInfo.setMessageRetry(orgInfo.getFile().getMessageRetry());
				jobFileInfo.setMessageRetryEndFlg(orgInfo.getFile().getMessageRetryEndFlg());
				jobFileInfo.setMessageRetryEndValue(orgInfo.getFile().getMessageRetryEndValue());
				jobInfo.setFile(jobFileInfo);
			}

			if (orgInfo.getMonitor() != null) {
				JobMonitorInfoResponse monitorJobInfo = new JobMonitorInfoResponse();
				monitorJobInfo.setFacilityID(orgInfo.getMonitor().getFacilityID());
				monitorJobInfo.setProcessingMethod(orgInfo.getMonitor().getProcessingMethod());
				monitorJobInfo.setScope(orgInfo.getMonitor().getScope());
				monitorJobInfo.setMonitorCriticalEndValue(orgInfo.getMonitor().getMonitorCriticalEndValue());
				monitorJobInfo.setMonitorId(orgInfo.getMonitor().getMonitorId());
				monitorJobInfo.setMonitorInfoEndValue(orgInfo.getMonitor().getMonitorInfoEndValue());
				monitorJobInfo.setMonitorUnknownEndValue(orgInfo.getMonitor().getMonitorUnknownEndValue());
				monitorJobInfo.setMonitorWaitEndValue(orgInfo.getMonitor().getMonitorWaitEndValue());
				monitorJobInfo.setMonitorWaitTime(orgInfo.getMonitor().getMonitorWaitTime());
				monitorJobInfo.setMonitorWarnEndValue(orgInfo.getMonitor().getMonitorWarnEndValue());
				jobInfo.setMonitor(monitorJobInfo);
			}
			
			if (orgInfo.getRpa() != null) {
				JobRpaInfoResponse rpaJobInfo = new JobRpaInfoResponse();
				rpaJobInfo.setMessageRetry(orgInfo.getRpa().getMessageRetry());
				rpaJobInfo.setMessageRetryEndFlg(orgInfo.getRpa().getMessageRetryEndFlg());
				rpaJobInfo.setMessageRetryEndValue(orgInfo.getRpa().getMessageRetryEndValue());
				rpaJobInfo.setCommandRetry(orgInfo.getRpa().getCommandRetry());
				rpaJobInfo.setCommandRetryFlg(orgInfo.getRpa().getCommandRetryFlg());
				rpaJobInfo.setCommandRetryEndStatus(orgInfo.getRpa().getCommandRetryEndStatus());
				rpaJobInfo.setRpaJobType(orgInfo.getRpa().getRpaJobType());
				// 直接実行
				rpaJobInfo.setFacilityID(orgInfo.getRpa().getFacilityID());
				rpaJobInfo.setProcessingMethod(orgInfo.getRpa().getProcessingMethod());
				rpaJobInfo.setScope(orgInfo.getRpa().getScope());
				rpaJobInfo.setRpaToolId(orgInfo.getRpa().getRpaToolId());
				rpaJobInfo.setRpaExeFilepath(orgInfo.getRpa().getRpaExeFilepath());
				rpaJobInfo.setRpaScenarioFilepath(orgInfo.getRpa().getRpaScenarioFilepath());
				rpaJobInfo.setRpaLogDirectory(orgInfo.getRpa().getRpaLogDirectory());
				rpaJobInfo.setRpaLogFileName(orgInfo.getRpa().getRpaLogFileName());
				rpaJobInfo.setRpaLogEncoding(orgInfo.getRpa().getRpaLogEncoding());
				rpaJobInfo.setRpaLogReturnCode(orgInfo.getRpa().getRpaLogReturnCode());
				rpaJobInfo.setRpaLogPatternHead(orgInfo.getRpa().getRpaLogPatternHead());
				rpaJobInfo.setRpaLogPatternTail(orgInfo.getRpa().getRpaLogPatternTail());
				rpaJobInfo.setRpaLogMaxBytes(orgInfo.getRpa().getRpaLogMaxBytes());
				rpaJobInfo.setRpaDefaultEndValue(orgInfo.getRpa().getRpaDefaultEndValue());
				rpaJobInfo.setRpaLoginFlg(orgInfo.getRpa().getRpaLoginFlg());
				rpaJobInfo.setRpaLoginUserId(orgInfo.getRpa().getRpaLoginUserId());
				rpaJobInfo.setRpaLoginPassword(orgInfo.getRpa().getRpaLoginPassword());
				rpaJobInfo.setRpaLoginRetry(orgInfo.getRpa().getRpaLoginRetry());
				rpaJobInfo.setRpaLoginEndValue(orgInfo.getRpa().getRpaLoginEndValue());
				rpaJobInfo.setRpaLoginResolution(orgInfo.getRpa().getRpaLoginResolution());
				rpaJobInfo.setRpaLogoutFlg(orgInfo.getRpa().getRpaLogoutFlg());
				rpaJobInfo.setRpaNotLoginNotify(orgInfo.getRpa().getRpaNotLoginNotify());
				rpaJobInfo.setRpaNotLoginNotifyPriority(orgInfo.getRpa().getRpaNotLoginNotifyPriority());
				rpaJobInfo.setRpaNotLoginEndValue(orgInfo.getRpa().getRpaNotLoginEndValue());
				rpaJobInfo.setRpaAlreadyRunningNotify(orgInfo.getRpa().getRpaAlreadyRunningNotify());
				rpaJobInfo.setRpaAlreadyRunningNotifyPriority(orgInfo.getRpa().getRpaAlreadyRunningNotifyPriority());
				rpaJobInfo.setRpaAlreadyRunningEndValue(orgInfo.getRpa().getRpaAlreadyRunningEndValue());
				rpaJobInfo.setRpaAbnormalExitNotify(orgInfo.getRpa().getRpaAbnormalExitNotify());
				rpaJobInfo.setRpaAbnormalExitNotifyPriority(orgInfo.getRpa().getRpaAbnormalExitNotifyPriority());
				rpaJobInfo.setRpaAbnormalExitEndValue(orgInfo.getRpa().getRpaAbnormalExitEndValue());
				rpaJobInfo.setRpaScreenshotEndDelayFlg(orgInfo.getRpa().getRpaScreenshotEndDelayFlg());
				rpaJobInfo.setRpaScreenshotEndValueFlg(orgInfo.getRpa().getRpaScreenshotEndValueFlg());
				rpaJobInfo.setRpaScreenshotEndValue(orgInfo.getRpa().getRpaScreenshotEndValue());
				rpaJobInfo.setRpaScreenshotEndValueCondition(orgInfo.getRpa().getRpaScreenshotEndValueCondition());
				rpaJobInfo.setRpaJobOptionInfos(orgInfo.getRpa().getRpaJobOptionInfos());
				rpaJobInfo.setRpaJobEndValueConditionInfos(new ArrayList<>());
				if (orgInfo.getRpa().getRpaJobEndValueConditionInfos() != null){
					rpaJobInfo.getRpaJobEndValueConditionInfos().addAll(orgInfo.getRpa().getRpaJobEndValueConditionInfos());
				}
				// 間接実行
				rpaJobInfo.setRpaScopeId(orgInfo.getRpa().getRpaScopeId());
				rpaJobInfo.setRpaRunType(orgInfo.getRpa().getRpaRunType());
				rpaJobInfo.setRpaScenarioParam(orgInfo.getRpa().getRpaScenarioParam());
				rpaJobInfo.setRpaStopType(orgInfo.getRpa().getRpaStopType());
				rpaJobInfo.setRpaStopMode(orgInfo.getRpa().getRpaStopMode());
				rpaJobInfo.setRpaRunConnectTimeout(orgInfo.getRpa().getRpaRunConnectTimeout());
				rpaJobInfo.setRpaRunRequestTimeout(orgInfo.getRpa().getRpaRunRequestTimeout());
				rpaJobInfo.setRpaRunEndFlg(orgInfo.getRpa().getRpaRunEndFlg());
				rpaJobInfo.setRpaRunRetry(orgInfo.getRpa().getRpaRunRetry());
				rpaJobInfo.setRpaRunEndValue(orgInfo.getRpa().getRpaRunEndValue());
				rpaJobInfo.setRpaCheckConnectTimeout(orgInfo.getRpa().getRpaCheckConnectTimeout());
				rpaJobInfo.setRpaCheckRequestTimeout(orgInfo.getRpa().getRpaCheckRequestTimeout());
				rpaJobInfo.setRpaCheckEndFlg(orgInfo.getRpa().getRpaCheckEndFlg());
				rpaJobInfo.setRpaCheckRetry(orgInfo.getRpa().getRpaCheckRetry());
				rpaJobInfo.setRpaCheckEndValue(orgInfo.getRpa().getRpaCheckEndValue());
				rpaJobInfo.setRpaJobRunParamInfos(orgInfo.getRpa().getRpaJobRunParamInfos());
				rpaJobInfo.setRpaJobCheckEndValueInfos(orgInfo.getRpa().getRpaJobCheckEndValueInfos());
				jobInfo.setRpa(rpaJobInfo);
			}

			if (orgInfo.getJobFileCheck() != null) {
				JobFileCheckInfoResponse filecheckJobInfo = new JobFileCheckInfoResponse();
				filecheckJobInfo.setFacilityID(orgInfo.getJobFileCheck().getFacilityID());
				filecheckJobInfo.setScope(orgInfo.getJobFileCheck().getScope());
				filecheckJobInfo.setProcessingMethod(orgInfo.getJobFileCheck().getProcessingMethod());
				filecheckJobInfo.setSuccessEndValue(orgInfo.getJobFileCheck().getSuccessEndValue());
				filecheckJobInfo.setFailureEndFlg(orgInfo.getJobFileCheck().getFailureEndFlg());
				filecheckJobInfo.setFailureWaitTime(orgInfo.getJobFileCheck().getFailureWaitTime());
				filecheckJobInfo.setFailureEndValue(orgInfo.getJobFileCheck().getFailureEndValue());
				filecheckJobInfo.setDirectory(orgInfo.getJobFileCheck().getDirectory());
				filecheckJobInfo.setFileName(orgInfo.getJobFileCheck().getFileName());
				filecheckJobInfo.setCreateValidFlg(orgInfo.getJobFileCheck().getCreateValidFlg());
				filecheckJobInfo.setCreateBeforeJobStartFlg(orgInfo.getJobFileCheck().getCreateBeforeJobStartFlg());
				filecheckJobInfo.setDeleteValidFlg(orgInfo.getJobFileCheck().getDeleteValidFlg());
				filecheckJobInfo.setModifyValidFlg(orgInfo.getJobFileCheck().getModifyValidFlg());
				filecheckJobInfo.setModifyType(orgInfo.getJobFileCheck().getModifyType());
				filecheckJobInfo.setNotJudgeFileInUseFlg(orgInfo.getJobFileCheck().getNotJudgeFileInUseFlg());
				filecheckJobInfo.setMessageRetryEndFlg(orgInfo.getJobFileCheck().getMessageRetryEndFlg());
				filecheckJobInfo.setMessageRetryEndValue(orgInfo.getJobFileCheck().getMessageRetryEndValue());
				filecheckJobInfo.setMessageRetry(orgInfo.getJobFileCheck().getMessageRetry());
				jobInfo.setJobFileCheck(filecheckJobInfo);
			}

			if (orgInfo.getJobLinkSend() != null) {
				JobLinkSendInfoResponse joblinksendJobInfo = new JobLinkSendInfoResponse();
				joblinksendJobInfo.setRetryFlg(orgInfo.getJobLinkSend().getRetryFlg());
				joblinksendJobInfo.setRetryCount(orgInfo.getJobLinkSend().getRetryCount());
				joblinksendJobInfo.setFailureOperation(orgInfo.getJobLinkSend().getFailureOperation());
				joblinksendJobInfo.setJoblinkMessageId(orgInfo.getJobLinkSend().getJoblinkMessageId());
				joblinksendJobInfo.setPriority(orgInfo.getJobLinkSend().getPriority());
				joblinksendJobInfo.setMessage(orgInfo.getJobLinkSend().getMessage());
				joblinksendJobInfo.setSuccessEndValue(orgInfo.getJobLinkSend().getSuccessEndValue());
				joblinksendJobInfo.setFailureEndStatus(orgInfo.getJobLinkSend().getFailureEndStatus());
				joblinksendJobInfo.setFailureEndValue(orgInfo.getJobLinkSend().getFailureEndValue());
				joblinksendJobInfo.setJoblinkSendSettingId(orgInfo.getJobLinkSend().getJoblinkSendSettingId());
				joblinksendJobInfo.getJobLinkExpList().addAll(orgInfo.getJobLinkSend().getJobLinkExpList());
				jobInfo.setJobLinkSend(joblinksendJobInfo);
			}

			if (orgInfo.getJobLinkRcv() != null) {
				JobLinkRcvInfoResponse joblinkrcvJobInfo = new JobLinkRcvInfoResponse();
				joblinkrcvJobInfo.setFacilityID(orgInfo.getJobLinkRcv().getFacilityID());
				joblinkrcvJobInfo.setScope(orgInfo.getJobLinkRcv().getScope());
				joblinkrcvJobInfo.setMonitorInfoEndValue(orgInfo.getJobLinkRcv().getMonitorInfoEndValue());
				joblinkrcvJobInfo.setMonitorWarnEndValue(orgInfo.getJobLinkRcv().getMonitorWarnEndValue());
				joblinkrcvJobInfo.setMonitorCriticalEndValue(orgInfo.getJobLinkRcv().getMonitorCriticalEndValue());
				joblinkrcvJobInfo.setMonitorUnknownEndValue(orgInfo.getJobLinkRcv().getMonitorUnknownEndValue());
				joblinkrcvJobInfo.setFailureEndFlg(orgInfo.getJobLinkRcv().getFailureEndFlg());
				joblinkrcvJobInfo.setMonitorWaitTime(orgInfo.getJobLinkRcv().getMonitorWaitTime());
				joblinkrcvJobInfo.setMonitorWaitEndValue(orgInfo.getJobLinkRcv().getMonitorWaitEndValue());
				joblinkrcvJobInfo.setJoblinkMessageId(orgInfo.getJobLinkRcv().getJoblinkMessageId());
				joblinkrcvJobInfo.setMessage(orgInfo.getJobLinkRcv().getMessage());
				joblinkrcvJobInfo.setPastFlg(orgInfo.getJobLinkRcv().getPastFlg());
				joblinkrcvJobInfo.setPastMin(orgInfo.getJobLinkRcv().getPastMin());
				joblinkrcvJobInfo.setInfoValidFlg(orgInfo.getJobLinkRcv().getInfoValidFlg());
				joblinkrcvJobInfo.setWarnValidFlg(orgInfo.getJobLinkRcv().getWarnValidFlg());
				joblinkrcvJobInfo.setCriticalValidFlg(orgInfo.getJobLinkRcv().getCriticalValidFlg());
				joblinkrcvJobInfo.setUnknownValidFlg(orgInfo.getJobLinkRcv().getUnknownValidFlg());
				joblinkrcvJobInfo.setApplicationFlg(orgInfo.getJobLinkRcv().getApplicationFlg());
				joblinkrcvJobInfo.setApplication(orgInfo.getJobLinkRcv().getApplication());
				joblinkrcvJobInfo.setMonitorDetailIdFlg(orgInfo.getJobLinkRcv().getMonitorDetailIdFlg());
				joblinkrcvJobInfo.setMonitorDetailId(orgInfo.getJobLinkRcv().getMonitorDetailId());
				joblinkrcvJobInfo.setMessageFlg(orgInfo.getJobLinkRcv().getMessageFlg());
				joblinkrcvJobInfo.setExpFlg(orgInfo.getJobLinkRcv().getExpFlg());
				joblinkrcvJobInfo.setMonitorAllEndValueFlg(orgInfo.getJobLinkRcv().getMonitorAllEndValueFlg());
				joblinkrcvJobInfo.setMonitorAllEndValue(orgInfo.getJobLinkRcv().getMonitorAllEndValue());
				joblinkrcvJobInfo.getJobLinkInheritList().addAll(orgInfo.getJobLinkRcv().getJobLinkInheritList());
				joblinkrcvJobInfo.getJobLinkExpList().addAll(orgInfo.getJobLinkRcv().getJobLinkExpList());
				jobInfo.setJobLinkRcv(joblinkrcvJobInfo);
			}

			if (orgInfo.getResource() != null) {
				JobResourceInfoResponse resourceJobInfo = new JobResourceInfoResponse();
				resourceJobInfo.setResourceType(orgInfo.getResource().getResourceType());
				resourceJobInfo.setResourceCloudScopeId(orgInfo.getResource().getResourceCloudScopeId());
				resourceJobInfo.setResourceLocationId(orgInfo.getResource().getResourceLocationId());
				resourceJobInfo.setResourceTargetId(orgInfo.getResource().getResourceTargetId());
				resourceJobInfo.setResourceAction(orgInfo.getResource().getResourceAction());
				resourceJobInfo.setResourceStatusConfirmTime(orgInfo.getResource().getResourceStatusConfirmTime());
				resourceJobInfo.setResourceStatusConfirmInterval(orgInfo.getResource().getResourceStatusConfirmInterval());
				resourceJobInfo.setResourceAttachNode(orgInfo.getResource().getResourceAttachNode());
				resourceJobInfo.setResourceAttachDevice(orgInfo.getResource().getResourceAttachDevice());
				resourceJobInfo.setResourceNotifyScope(orgInfo.getResource().getResourceNotifyScope());
				resourceJobInfo.setResourceNotifyScopePath(orgInfo.getResource().getResourceNotifyScopePath());
				resourceJobInfo.setResourceSuccessValue(orgInfo.getResource().getResourceSuccessValue());
				resourceJobInfo.setResourceFailureValue(orgInfo.getResource().getResourceFailureValue());
				jobInfo.setResource(resourceJobInfo);
			}

			jobInfo.setBeginPriority(orgInfo.getBeginPriority());
			jobInfo.setNormalPriority(orgInfo.getNormalPriority());
			jobInfo.setWarnPriority(orgInfo.getWarnPriority());
			jobInfo.setAbnormalPriority(orgInfo.getAbnormalPriority());
			for (NotifyRelationInfoResponse relationInfo : orgInfo.getNotifyRelationInfos()) {
				jobInfo.getNotifyRelationInfos().add(relationInfo);
			}

			if (orgInfo.getParam() != null) {
				for(JobParameterInfoResponse item : orgInfo.getParam()) {
					JobParameterInfoResponse jobParameterInfo = new JobParameterInfoResponse();
					jobParameterInfo.setDescription(item.getDescription());
					jobParameterInfo.setParamId(item.getParamId());
					jobParameterInfo.setType(item.getType());
					jobParameterInfo.setValue(item.getValue());
					jobInfo.getParam().add(jobParameterInfo);
				}
			}

			if (orgInfo.getWaitRule() != null) {
				JobWaitRuleInfoResponse jobWaitRuleInfo = JobTreeItemUtil.createJobWaitRuleInfoResponse();
				jobWaitRuleInfo.setCalendar(orgInfo.getWaitRule().getCalendar());
				jobWaitRuleInfo.setCalendarEndStatus(orgInfo.getWaitRule().getCalendarEndStatus());
				jobWaitRuleInfo.setCalendarEndValue(orgInfo.getWaitRule().getCalendarEndValue());
				jobWaitRuleInfo.setCalendarId(orgInfo.getWaitRule().getCalendarId());
				jobWaitRuleInfo.setCondition(orgInfo.getWaitRule().getCondition());
				jobWaitRuleInfo.setEndCondition(orgInfo.getWaitRule().getEndCondition());
				jobWaitRuleInfo.setEndStatus(orgInfo.getWaitRule().getEndStatus());
				jobWaitRuleInfo.setEndValue(orgInfo.getWaitRule().getEndValue());
				jobWaitRuleInfo.setEndDelay(orgInfo.getWaitRule().getEndDelay());
				jobWaitRuleInfo.setEndDelayConditionType(orgInfo.getWaitRule().getEndDelayConditionType());
				jobWaitRuleInfo.setEndDelayJob(orgInfo.getWaitRule().getEndDelayJob());
				jobWaitRuleInfo.setEndDelayJobValue(orgInfo.getWaitRule().getEndDelayJobValue());
				jobWaitRuleInfo.setEndDelayNotify(orgInfo.getWaitRule().getEndDelayNotify());
				jobWaitRuleInfo.setEndDelayNotifyPriority(orgInfo.getWaitRule().getEndDelayNotifyPriority());
				jobWaitRuleInfo.setEndDelayOperation(orgInfo.getWaitRule().getEndDelayOperation());
				jobWaitRuleInfo.setEndDelayOperationEndStatus(orgInfo.getWaitRule().getEndDelayOperationEndStatus());
				jobWaitRuleInfo.setEndDelayOperationEndValue(orgInfo.getWaitRule().getEndDelayOperationEndValue());
				jobWaitRuleInfo.setEndDelayOperationType(orgInfo.getWaitRule().getEndDelayOperationType());
				jobWaitRuleInfo.setEndDelaySession(orgInfo.getWaitRule().getEndDelaySession());
				jobWaitRuleInfo.setEndDelaySessionValue(orgInfo.getWaitRule().getEndDelaySessionValue());
				jobWaitRuleInfo.setEndDelayTime(orgInfo.getWaitRule().getEndDelayTime());
				if (orgInfo.getWaitRule().getEndDelayTimeValue() != null) {
					jobWaitRuleInfo.setEndDelayTimeValue(orgInfo.getWaitRule().getEndDelayTimeValue());
				}
				jobWaitRuleInfo.setEndDelayChangeMount(orgInfo.getWaitRule().getEndDelayChangeMount());
				jobWaitRuleInfo.setEndDelayChangeMountValue(orgInfo.getWaitRule().getEndDelayChangeMountValue());
				jobWaitRuleInfo.setMultiplicityNotify(orgInfo.getWaitRule().getMultiplicityNotify());
				jobWaitRuleInfo.setMultiplicityNotifyPriority(orgInfo.getWaitRule().getMultiplicityNotifyPriority());
				jobWaitRuleInfo.setMultiplicityOperation(orgInfo.getWaitRule().getMultiplicityOperation());
				jobWaitRuleInfo.setMultiplicityEndValue(orgInfo.getWaitRule().getMultiplicityEndValue());
				jobWaitRuleInfo.setSkip(orgInfo.getWaitRule().getSkip());
				jobWaitRuleInfo.setSkipEndStatus(orgInfo.getWaitRule().getSkipEndStatus());
				jobWaitRuleInfo.setSkipEndValue(orgInfo.getWaitRule().getSkipEndValue());
				jobWaitRuleInfo.setExclusiveBranch(orgInfo.getWaitRule().getExclusiveBranch());
				jobWaitRuleInfo.setExclusiveBranchEndStatus(orgInfo.getWaitRule().getExclusiveBranchEndStatus());
				jobWaitRuleInfo.setExclusiveBranchEndValue(orgInfo.getWaitRule().getExclusiveBranchEndValue());
				jobWaitRuleInfo.setJobRetryFlg(orgInfo.getWaitRule().getJobRetryFlg());
				jobWaitRuleInfo.setJobRetryEndStatus(orgInfo.getWaitRule().getJobRetryEndStatus());
				jobWaitRuleInfo.setJobRetry(orgInfo.getWaitRule().getJobRetry());
				jobWaitRuleInfo.setJobRetryInterval(orgInfo.getWaitRule().getJobRetryInterval());
				jobWaitRuleInfo.setStartDelay(orgInfo.getWaitRule().getStartDelay());
				jobWaitRuleInfo.setStartDelayConditionType(orgInfo.getWaitRule().getStartDelayConditionType());
				jobWaitRuleInfo.setStartDelayNotify(orgInfo.getWaitRule().getStartDelayNotify());
				jobWaitRuleInfo.setStartDelayNotifyPriority(orgInfo.getWaitRule().getStartDelayNotifyPriority());
				jobWaitRuleInfo.setStartDelayOperation(orgInfo.getWaitRule().getStartDelayOperation());
				jobWaitRuleInfo.setStartDelayOperationEndStatus(orgInfo.getWaitRule().getStartDelayOperationEndStatus());
				jobWaitRuleInfo.setStartDelayOperationEndValue(orgInfo.getWaitRule().getStartDelayOperationEndValue());
				jobWaitRuleInfo.setStartDelayOperationType(orgInfo.getWaitRule().getStartDelayOperationType());
				jobWaitRuleInfo.setStartDelaySession(orgInfo.getWaitRule().getStartDelaySession());
				jobWaitRuleInfo.setStartDelaySessionValue(orgInfo.getWaitRule().getStartDelaySessionValue());
				jobWaitRuleInfo.setStartDelayTime(orgInfo.getWaitRule().getStartDelayTime());
				if (orgInfo.getWaitRule().getStartDelayTimeValue() != null) {
					jobWaitRuleInfo.setStartDelayTimeValue(orgInfo.getWaitRule().getStartDelayTimeValue());
				}
				jobWaitRuleInfo.setSuspend(orgInfo.getWaitRule().getSuspend());

				if (orgInfo.getWaitRule().getObjectGroup() != null) {
					List<JobObjectGroupInfoResponse> objectGroupList = new ArrayList<>();
					for(JobObjectGroupInfoResponse itemGroup : orgInfo.getWaitRule().getObjectGroup()) {
						if (itemGroup.getJobObjectList() == null) {
							continue;
						}
						List<JobObjectInfoResponse> objectList = new ArrayList<>();
						for(JobObjectInfoResponse item : itemGroup.getJobObjectList()) {
							JobObjectInfoResponse jobObjectInfo = new JobObjectInfoResponse();
							jobObjectInfo.setJobId(item.getJobId());
							if (item.getTime() != null) {
								jobObjectInfo.setTime(item.getTime());
							}
							if (item.getStartMinute() != null) {
								jobObjectInfo.setStartMinute(item.getStartMinute());
							}
							jobObjectInfo.setType(item.getType());
							jobObjectInfo.setValue(item.getValue());
							jobObjectInfo.setStatus(item.getStatus());
							jobObjectInfo.setDescription(item.getDescription());
							jobObjectInfo.setDecisionValue(item.getDecisionValue());
							jobObjectInfo.setDecisionCondition(item.getDecisionCondition());
							jobObjectInfo.setCrossSessionRange(item.getCrossSessionRange());
							objectList.add(jobObjectInfo);
						}
						JobObjectGroupInfoResponse jobObjectGroupInfo = new JobObjectGroupInfoResponse();
						jobObjectGroupInfo.setConditionType(itemGroup.getConditionType());
						jobObjectGroupInfo.setJobObjectList(objectList);
						jobObjectGroupInfo.setOrderNo(itemGroup.getOrderNo());
						jobObjectGroupInfo.setIsGroup(itemGroup.getIsGroup());
						objectGroupList.add(jobObjectGroupInfo);
					}
					jobWaitRuleInfo.setObjectGroup(objectGroupList);
				}
				//後続ジョブ優先度設定
				jobWaitRuleInfo.getExclusiveBranchNextJobOrderList().addAll(orgInfo.getWaitRule().getExclusiveBranchNextJobOrderList());
				// ジョブキュー
				jobWaitRuleInfo.setQueueFlg(orgInfo.getWaitRule().getQueueFlg());
				jobWaitRuleInfo.setQueueId(orgInfo.getWaitRule().getQueueId());
				
				jobInfo.setWaitRule(jobWaitRuleInfo);
			}
			//参照ジョブ
			if(orgInfo.getReferJobUnitId() != null){
				jobInfo.setReferJobUnitId(orgInfo.getReferJobUnitId());
			}
			if(orgInfo.getReferJobId() != null){
				jobInfo.setReferJobId(orgInfo.getReferJobId());
			}
			if(orgInfo.getReferJobSelectType() != null){
				jobInfo.setReferJobSelectType(orgInfo.getReferJobSelectType());
			}
			//承認ジョブ
			if(orgInfo.getApprovalReqRoleId() != null){
				jobInfo.setApprovalReqRoleId(orgInfo.getApprovalReqRoleId());
			}
			if(orgInfo.getApprovalReqUserId() != null){
				jobInfo.setApprovalReqUserId(orgInfo.getApprovalReqUserId());
			}
			if(orgInfo.getApprovalReqSentence() != null){
				jobInfo.setApprovalReqSentence(orgInfo.getApprovalReqSentence());
			}
			if(orgInfo.getApprovalReqMailTitle() != null){
				jobInfo.setApprovalReqMailTitle(orgInfo.getApprovalReqMailTitle());
			}
			if(orgInfo.getApprovalReqMailBody() != null){
				jobInfo.setApprovalReqMailBody(orgInfo.getApprovalReqMailBody());
			}
			jobInfo.setIsUseApprovalReqSentence(orgInfo.getIsUseApprovalReqSentence());

			if(orgInfo.getExpNodeRuntimeFlg() != null){
				jobInfo.setExpNodeRuntimeFlg(orgInfo.getExpNodeRuntimeFlg());
			}
			cloneItem.setData(jobInfo);
		}

		if (origItem.getDetail() != null) {
			JobDetailInfoResponse jobDetailInfo = new JobDetailInfoResponse();
			if (origItem.getDetail().getEndDate() != null) {
				jobDetailInfo.setEndDate(origItem.getDetail().getEndDate());
			}
			if (origItem.getDetail().getEndStatus() != null) {
				jobDetailInfo.setEndStatus(origItem.getDetail().getEndStatus());
			}
			if (origItem.getDetail().getEndValue() != null) {
				jobDetailInfo.setEndValue(origItem.getDetail().getEndValue());
			}
			jobDetailInfo.setFacilityId(origItem.getDetail().getFacilityId());
			jobDetailInfo.setScope(origItem.getDetail().getScope());
			if (origItem.getDetail().getStartDate() != null) {
				jobDetailInfo.setStartDate(origItem.getDetail().getStartDate());
			}
			if (origItem.getDetail().getStatus() != null) {
				jobDetailInfo.setStatus(origItem.getDetail().getStatus());
			}
			if (origItem.getDetail().getWaitRuleTimeList() != null || !origItem.getDetail().getWaitRuleTimeList().isEmpty()) {
				jobDetailInfo.setWaitRuleTimeList(origItem.getDetail().getWaitRuleTimeList());
			}
			cloneItem.setDetail(jobDetailInfo);
		}
	
		List<JobTreeItemWrapper> listOrig = origItem.getChildren();
		if (listOrig != null) {
			for (JobTreeItemWrapper orgChild : listOrig) {
				if(orgChild != null){
					cloneItem.getChildren().add(clone(orgChild, cloneItem));
				}
			}
		}

		cloneItem.setParent(parentItem);

		return cloneItem;
	}

	public static void addChildren(JobTreeItemWrapper parent, JobTreeItemWrapper child){
		parent.getChildren().add(child);
		child.setParent(parent);

		return;
	}

	public static void removeChildren(JobTreeItemWrapper parent, JobTreeItemWrapper child) {
		List<JobTreeItemWrapper> children = parent.getChildren();
		for (int i = 0; i < children.size(); i++) {
			if (child.equals(children.get(i))) {
				children.remove(i);
				break;
			}
		}
	}

	public static String getManagerName(JobTreeItemWrapper item) {
		JobTreeItemWrapper managerItem = getManager(item);
		if (null != managerItem && managerItem.getData() != null) {
			return managerItem.getData().getName();
		} else {
			return null;
		}
	}

	public static JobTreeItemWrapper getManager(JobTreeItemWrapper item) {
		if (item == null) {
			return null;
		} else if (item.getData() != null && item.getData().getType() == JobInfoWrapper.TypeEnum.MANAGER) {
			return item;
		}
		return getManager(item.getParent());
	}

	/**
	 * 
	 * RESTのDTO（JobTreeItemResponseP1）を元に JobTreeItemWrapper を生成
	 * 
	 * @param dto JobTreeItemResponseP1
 	 * @return JobTreeItemWrapper
	 */
	public static JobTreeItemWrapper getItemFromP1(JobTreeItemResponseP1 dto) {
		JobTreeItemWrapper info = new JobTreeItemWrapper();
		setItemRecursiveFromP1(dto, info);
		return info;
	}

	private static void setItemRecursiveFromP1(JobTreeItemResponseP1 dto, JobTreeItemWrapper info) {
		JobInfoWrapper infoData = JobTreeItemUtil.createJobInfoWrapper();
		// データコピー
		infoData.setId(dto.getData().getId());
		infoData.setJobunitId(dto.getData().getJobunitId());
		infoData.setName(dto.getData().getName());
		infoData.setType(JobInfoWrapper.TypeEnum.fromValue(dto.getData().getType().getValue()));
		infoData.setPropertyFull(false);
		infoData.setOwnerRoleId(dto.getData().getOwnerRoleId());
		infoData.setExpNodeRuntimeFlg(dto.getData().getExpNodeRuntimeFlg());
		infoData.setUpdateTime(dto.getData().getUpdateTime());
		info.setData(infoData);

		info.setChildren(new ArrayList<JobTreeItemWrapper>());
		for (JobTreeItemResponseP1 dtoChild : dto.getChildren()) {
			JobTreeItemWrapper infoChild = new JobTreeItemWrapper();
			setItemRecursiveFromP1(dtoChild, infoChild);
			// 上位方向のリンクを補足
			infoChild.setParent(info);
			info.getChildren().add(infoChild);
		}

	}

	/**
	 * 
	 * RESTのDTO（JobTreeItemResponseP2）を元に JobTreeItemWrapper を生成
	 * 
	 * @param dto JobTreeItemResponseP2
 	 * @return JobTreeItemWrapper
	 * @throws HinemosUnknown 
	 */
	public static JobTreeItemWrapper getItemFromP2(JobTreeItemResponseP2 dto) throws HinemosUnknown {
		JobTreeItemWrapper info = new JobTreeItemWrapper();
		setItemRecursiveFromP2(dto, info);
		return info;
	}

	private static void setItemRecursiveFromP2(JobTreeItemResponseP2 dto, JobTreeItemWrapper info) throws HinemosUnknown {

		JobInfoWrapper infoData = JobTreeItemUtil.createJobInfoWrapper();
		RestClientBeanUtil.convertBean(dto.getData(), infoData);
		info.setData(infoData);
		paddingJobInfoWrapper(info.getData());

		info.setChildren(new ArrayList<JobTreeItemWrapper>());
		for (JobTreeItemResponseP2 dtoChild : dto.getChildren()) {
			JobTreeItemWrapper infoChild = new JobTreeItemWrapper();
			setItemRecursiveFromP2(dtoChild, infoChild);
			// 上位方向のリンクを補足
			infoChild.setParent(info);
			info.getChildren().add(infoChild);
		}
	}

	/**
	 * 
	 * RESTのDTO（JobTreeItemResponseP2）を元に JobTreeItemWrapper を生成
	 *  
	 * ただし、 TreeView向けに設定するメンバ項目を限定する。
	 * 
	 * @param dto JobTreeItemResponseP2
 	 * @return JobTreeItemWrapper
	 */
	public static JobTreeItemWrapper getItemFromP2ForTreeView(JobTreeItemResponseP2 dto) {
		JobTreeItemWrapper info = new JobTreeItemWrapper();
		setItemRecursiveFromP2ForTreeView(dto, info);
		return info;
	}

	private static void setItemRecursiveFromP2ForTreeView(JobTreeItemResponseP2 dto, JobTreeItemWrapper info) {
		JobInfoWrapper infoData = JobTreeItemUtil.createJobInfoWrapper();
		// データコピー（ツリーに必要な情報のみ）
		infoData.setId(dto.getData().getId());
		infoData.setJobunitId(dto.getData().getJobunitId());
		infoData.setName(dto.getData().getName());
		infoData.setType(JobInfoWrapper.TypeEnum.fromValue(dto.getData().getType().getValue()));
		infoData.setPropertyFull(false);
		infoData.setOwnerRoleId(dto.getData().getOwnerRoleId());
		infoData.setUpdateTime(dto.getData().getUpdateTime());
		infoData.setDescription(dto.getData().getDescription());
		infoData.setIconId(dto.getData().getIconId());
		infoData.setExpNodeRuntimeFlg(dto.getData().getExpNodeRuntimeFlg());

		infoData.setWaitRule(dto.getData().getWaitRule());
		infoData.setReferJobId(dto.getData().getReferJobId());
		infoData.setReferJobSelectType(
				JobInfoWrapper.ReferJobSelectTypeEnum.fromValue(dto.getData().getReferJobSelectType().getValue()));
		infoData.setReferJobUnitId(dto.getData().getReferJobUnitId());
		infoData.setRegistered(dto.getData().getRegistered());

		info.setData(infoData);

		info.setChildren(new ArrayList<JobTreeItemWrapper>());
		for (JobTreeItemResponseP2 dtoChild : dto.getChildren()) {
			JobTreeItemWrapper infoChild = new JobTreeItemWrapper();
			setItemRecursiveFromP2ForTreeView(dtoChild, infoChild);
			// 上位方向のリンクを補足
			infoChild.setParent(info);
			info.getChildren().add(infoChild);
		}
	}
	
	/**
	 * 
	 * RESTのDTO（JobTreeItemResponseP1）を元に JobTreeItemWrapper を生成
	 *  
	 * ただし、 TreeView向けに設定するメンバ項目を限定する。
	 * 
	 * @param dto JobTreeItemResponseP2
 	 * @return JobTreeItemWrapper
	 */
	public static JobTreeItemWrapper getItemFromP1ForTreeView(JobTreeItemResponseP1 dto) {
		JobTreeItemWrapper info = new JobTreeItemWrapper();
		setItemRecursiveFromP1ForTreeView(dto, info);
		return info;
	}

	private static void setItemRecursiveFromP1ForTreeView(JobTreeItemResponseP1 dto, JobTreeItemWrapper info) {
		JobInfoWrapper infoData = JobTreeItemUtil.createJobInfoWrapper();
		// データコピー（ツリーに必要な情報のみ）
		infoData.setId(dto.getData().getId());
		infoData.setJobunitId(dto.getData().getJobunitId());
		infoData.setName(dto.getData().getName());
		infoData.setType(JobInfoWrapper.TypeEnum.fromValue(dto.getData().getType().getValue()));
		infoData.setPropertyFull(false);
		infoData.setOwnerRoleId(dto.getData().getOwnerRoleId());
		infoData.setUpdateTime(dto.getData().getUpdateTime());
		infoData.setDescription(dto.getData().getDescription());
		infoData.setIconId(dto.getData().getIconId());
		infoData.setExpNodeRuntimeFlg(dto.getData().getExpNodeRuntimeFlg());

		infoData.setWaitRule(dto.getData().getWaitRule());
		infoData.setReferJobId(dto.getData().getReferJobId());
		infoData.setReferJobSelectType(
				JobInfoWrapper.ReferJobSelectTypeEnum.fromValue(dto.getData().getReferJobSelectType().getValue()));
		infoData.setReferJobUnitId(dto.getData().getReferJobUnitId());
		infoData.setRegistered(dto.getData().getRegistered());

		info.setData(infoData);

		info.setChildren(new ArrayList<JobTreeItemWrapper>());
		for (JobTreeItemResponseP1 dtoChild : dto.getChildren()) {
			JobTreeItemWrapper infoChild = new JobTreeItemWrapper();
			setItemRecursiveFromP1ForTreeView(dtoChild, infoChild);
			// 上位方向のリンクを補足
			infoChild.setParent(info);
			info.getChildren().add(infoChild);
		}
	}

	/**
	 * 
	 * RESTのDTO（JobTreeItemResponseP3）を元に JobTreeItemWrapper を生成
	 * 
	 * @param dto JobTreeItemResponseP3
 	 * @return JobTreeItemWrapper
	 * @throws HinemosUnknown 
	 */
	public static JobTreeItemWrapper getItemFromP3(JobTreeItemResponseP3 dto) throws HinemosUnknown {
		JobTreeItemWrapper info = new JobTreeItemWrapper();
		setItemRecursiveFromP3(dto, info);
		return info;
	}

	private static void setItemRecursiveFromP3(JobTreeItemResponseP3 dto, JobTreeItemWrapper info) throws HinemosUnknown {
		JobInfoWrapper infoData = JobTreeItemUtil.createJobInfoWrapper();
		RestClientBeanUtil.convertBean(dto.getData(), infoData);
		infoData.setPropertyFull(true);
		info.setData(infoData);
		
		paddingJobInfoWrapper(info.getData());

		info.setChildren(new ArrayList<JobTreeItemWrapper>());
	}

	/**
	 * 
	 * RESTのDTO（JobTreeItemResponseP4）を元に JobTreeItemWrapper を生成
	 * 
	 * @param dto JobTreeItemResponseP4
 	 * @return JobTreeItemWrapper
	 * @throws HinemosUnknown 
	 */
	public static JobTreeItemWrapper getItemFromP4(JobTreeItemResponseP4 dto) throws HinemosUnknown {
		JobTreeItemWrapper info = new JobTreeItemWrapper();
		setItemRecursiveFromP4(dto, info);
		return info;
	}

	private static void setItemRecursiveFromP4(JobTreeItemResponseP4 dto, JobTreeItemWrapper info) throws HinemosUnknown {

		JobInfoWrapper infoData = JobTreeItemUtil.createJobInfoWrapper();
		RestClientBeanUtil.convertBean(dto.getData(), infoData);
		info.setData(infoData);
		paddingJobInfoWrapper(info.getData());

		info.setDetail(dto.getDetail());

		info.setChildren(new ArrayList<JobTreeItemWrapper>());
		for (JobTreeItemResponseP4 dtoChild : dto.getChildren()) {
			JobTreeItemWrapper infoChild = new JobTreeItemWrapper();
			setItemRecursiveFromP4(dtoChild, infoChild);
			// 上位方向のリンクを補足
			infoChild.setParent(info);
			info.getChildren().add(infoChild);
		}
	}

	/**
	 * 
	 * 持ち回り用データ（JobTreeItemWrapper）を元に 登録リクエスト用DTO（JobTreeItemRequest） を生成
	 * 
	 * @param info JobTreeItemWrapper
 	 * @return JobTreeItemRequest
	 */
	public static JobTreeItemRequest getRequestFromItem(JobTreeItemWrapper info) throws HinemosUnknown {
		JobTreeItemRequest dto = new JobTreeItemRequest();
		setReqItemRecursive(info, dto);
		return dto;
	}
	private static void	setReqItemRecursive(JobTreeItemWrapper itemInfo, JobTreeItemRequest itemDto) throws HinemosUnknown{
		
		JobInfoRequest jobDataDto = new JobInfoRequest();
		RestClientBeanUtil.convertBean(itemInfo.getData(), jobDataDto);

		//クラス間の名称が違うのでロジック変換
		jobDataDto.setUpdateTaget(itemInfo.getData().getPropertyFull() );

		itemDto.setData(jobDataDto);

		itemDto.setChildren(new ArrayList<JobTreeItemRequest>());
		for (JobTreeItemWrapper infoItemChild : itemInfo.getChildren()) {
			JobTreeItemRequest dtoItemChild = new JobTreeItemRequest();
			setReqItemRecursive(infoItemChild, dtoItemChild);
			itemDto.getChildren().add(dtoItemChild);
		}
		
	}

	/**
	 * 
	 * 持ち回り用データ[JobInfoResponse]のパディング（設定必須の値の補完）を実施
	 * 
	 * @param dto JobInfoResponse
	 */
	public static void paddingJobInfoResponse(JobInfoResponse dto) {
		if (dto == null) {
			return;
		}
		// null補完 旧SOAPでは基本Listのnullはなかったので 同調
		if (dto.getRegistered() == null) {
			dto.setRegistered(false);
		}
		if (dto.getIsUseApprovalReqSentence() == null) {
			dto.setIsUseApprovalReqSentence(false);
		}

		if (dto.getWaitRule() != null) {
			paddingJobWaitRuleInfoResponse(dto.getWaitRule());
		}

		if (dto.getEndStatus() == null) {
			dto.setEndStatus(new ArrayList<JobEndStatusInfoResponse>());
		}
		if (dto.getNotifyRelationInfos() == null) {
			dto.setNotifyRelationInfos(new ArrayList<NotifyRelationInfoResponse>());
		}
		if (dto.getParam() == null) {
			dto.setParam(new ArrayList<JobParameterInfoResponse>());
		}
		if (dto.getCommand() != null) {
			paddingJobCommandInfoResponse(dto.getCommand());
		}
		if (dto.getJobLinkRcv() != null) {
			paddingJobLinkRcvInfoResponse(dto.getJobLinkRcv());
		}
		if (dto.getExpNodeRuntimeFlg() == null) {
			dto.setExpNodeRuntimeFlg(false);
		}

	}
	/**
	 * 
	 * 持ち回り用データ[JobInfoWrapper]のパディング（設定必須の値の補完）を実施
	 * 
	 * @param dto JobInfoWrapper
	 */
	
	public static void paddingJobInfoWrapper(JobInfoWrapper dto) {
		if (dto == null) {
			return;
		}
		paddingJobInfoResponse(dto);
	}
	
	private static void paddingJobCommandInfoResponse(JobCommandInfoResponse dto) {
		if (dto == null) {
			return;
		}
		// null補完 旧SOAPでは基本ListのNULLはなかったので 同調
		if (dto.getEnvVariable() == null) {
			dto.setEnvVariable(new ArrayList<JobEnvVariableInfoResponse>());
		}
		if (dto.getJobCommandParamList() == null) {
			dto.setJobCommandParamList(new ArrayList<JobCommandParamResponse>());
		}

	}

	private static void paddingJobLinkRcvInfoResponse(JobLinkRcvInfoResponse dto) {
		if (dto == null) {
			return;
		}
		// null補完 旧SOAPでは基本ListのNULLはなかったので 同調
		if (dto.getJobLinkExpList() == null) {
			dto.setJobLinkExpList(new ArrayList<>());
		}
		if (dto.getJobLinkInheritList() == null) {
			dto.setJobLinkInheritList(new ArrayList<>());
		}
	}

	private static void paddingJobWaitRuleInfoResponse(JobWaitRuleInfoResponse dto) {
		if (dto == null) {
			return;
		}
		// null補完 旧SOAPでは基本ListのNULLはなかったので 同調
		if (dto.getObjectGroup() == null) {
			dto.setObjectGroup(new ArrayList<JobObjectGroupInfoResponse>());
		}
		if (dto.getExclusiveBranchNextJobOrderList() == null) {
			dto.setExclusiveBranchNextJobOrderList(new ArrayList<JobNextJobOrderInfoResponse>());
		}

	}

	/**
	 * 
	 * ジョブ向けの日時文字列のエポックミリ秒変換（Long値）を実施
	 * 
	 * @param timeValue 日時文字列
 	 * @return エポックミリ秒
	 */
	public static Long convertDtStringtoLong(String timeValue) {
		if (timeValue == null) {
			return null;
		}
		SimpleDateFormat formatter = TimezoneUtil.getSimpleDateFormat();
		Date date = null;
		try {
			date = formatter.parse(timeValue);
		} catch (ParseException e) {
			return 0L;
		}
		return date.getTime();
	}

	/**
	 * 
	 * ジョブ向けの時刻文字列（ HH:mm:ss 24H以上あり）のエポックミリ秒変換（Long値）を実施
	 * 
	 * @param timeValue 日時文字列
 	 * @return エポックミリ秒
	 */
	public static Long convertTimeStringtoLong(String timeValue) {
		// 24時以降以降の出力に対応する
		Date date = null;
		try {
			date = TimeStringConverter.parseTime(timeValue);
		} catch (ParseException e) {
			return 0L;
		}
		return date.getTime();
	}

	/**
	 * 
	 * ジョブ向けのエポックミリ秒の日時文字列変換（ HH:mm:ss 24H以上あり）を実施
	 * 
	 * @param timeValue エポックミリ秒
 	 * @return 日時文字列
	 */
	public static String convertTimeLongtoString(Long timeValue) {
		// 24時以降以降の入力に対応する
		return TimeStringConverter.formatTime(new Date(timeValue));
	}

	/**
	 * 
	 * 持ち回り用データ[JobInfoWrapper] の初期値を生成
	 * 
 	 * @return JobInfoWrapper
	 */
	public static JobInfoWrapper createJobInfoWrapper() {
		// 旧 JobInfoだと生成時にNullでなかった項目あるので補完して生成
		JobInfoWrapper ret = new JobInfoWrapper();
		paddingJobInfoResponse(ret);
		ret.setPropertyFull(false);
		return ret;
	}

	
	private static JobWaitRuleInfoResponse createJobWaitRuleInfoResponse() {
		// 旧 JobWaitだと生成時にNullでなかった項目あるので補完して生成
		JobWaitRuleInfoResponse ret = new JobWaitRuleInfoResponse();
		paddingJobWaitRuleInfoResponse(ret);
		return ret;
	}

	/**
	 * 
	 * 持ち回り用データ[JobCommandInfoResponse] の初期値を生成
	 * 
 	 * @return JobCommandInfoResponse
	 */
	public static JobCommandInfoResponse createJobCommandInfoResponse() {
		// 旧 JobCommandInfoだと生成時にNullでなかった項目あるので補完して生成
		JobCommandInfoResponse ret = new JobCommandInfoResponse();
		paddingJobCommandInfoResponse(ret);
		return ret;
	}

	/**
	 * 
	 * 持ち回り用データ[JobLinkRcvInfoResponse] の初期値を生成
	 * 
 	 * @return JobLinkRcvInfoResponse
	 */
	public static JobLinkRcvInfoResponse createJobLinkRcvInfoResponse() {
		JobLinkRcvInfoResponse ret = new JobLinkRcvInfoResponse();
		paddingJobLinkRcvInfoResponse(ret);
		return ret;
	}
	
	/**
	 * 
	 * RESTのDTO（JobInfoResponse）を元に JobInfoWrapper を生成
	 * 
	 * @param dto JobInfoResponse
 	 * @return JobInfoWrapper
	 * @throws HinemosUnknown 
	 */
	public static JobInfoWrapper getInfoFromDto(JobInfoResponse dto) {
		try{
			JobInfoWrapper infoData = JobTreeItemUtil.createJobInfoWrapper();
			RestClientBeanUtil.convertBean(dto, infoData);
			return infoData;
		}catch(Exception e){
			//ここには来ない想定（convertBeanでエラーにはならない想定）
			return null;
		}
	}
	
	/**
	 * 
	 * RESTのDTO（List<JobInfoResponse>）を元に List<JobInfoWrapper> を生成
	 * 
	 * @param dtoList List<JobInfoResponse>
 	 * @return List<JobInfoWrapper>
	 * @throws HinemosUnknown 
	 */
	public static List<JobInfoWrapper> getInfoListFromDtoList(List<JobInfoResponse> dtoList) throws HinemosUnknown {
		List<JobInfoWrapper> retList  = new ArrayList<JobInfoWrapper>();
		for(JobInfoResponse response : dtoList ){
			retList.add(getInfoFromDto(response));
		}
		return retList;
	}
}
