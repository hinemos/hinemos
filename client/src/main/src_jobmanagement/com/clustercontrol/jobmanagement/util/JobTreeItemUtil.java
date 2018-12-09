/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.List;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.jobmanagement.bean.ConditionTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.ws.jobmanagement.JobCommandInfo;
import com.clustercontrol.ws.jobmanagement.JobDetailInfo;
import com.clustercontrol.ws.jobmanagement.JobEndStatusInfo;
import com.clustercontrol.ws.jobmanagement.JobFileInfo;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobObjectInfo;
import com.clustercontrol.ws.jobmanagement.JobParameterInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;
import com.clustercontrol.ws.jobmanagement.MonitorJobInfo;
import com.clustercontrol.ws.notify.NotifyRelationInfo;


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
	public static String getPath(JobTreeItem jobTreeItem) {

		// トップ("ジョブ")の場合は、文字を出力しません。
		if (jobTreeItem.getData().getType() == JobConstant.TYPE_COMPOSITE) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();

		buffer.append(jobTreeItem.getData().getName());

		/*
		 * 再起呼び出しすることでもないので。
		 */

		JobTreeItem parent = jobTreeItem.getParent();
		while (parent != null
				&& parent.getData().getType() != JobConstant.TYPE_COMPOSITE) {

			buffer.insert(0, SEPARATOR);
			buffer.insert(0, parent.getData().getName());
			parent = parent.getParent();
		}

		return buffer.toString();
	}

	public static JobInfo getNewJobInfo(String jobunitId, int type) {
		JobInfo jobInfo = new JobInfo();
		jobInfo.setJobunitId(jobunitId);
		jobInfo.setId("");
		jobInfo.setName("");
		jobInfo.setType(type);

		jobInfo.setWaitRule(getNewJobWaitRuleInfo());
		return jobInfo;
	}

	public static JobWaitRuleInfo getNewJobWaitRuleInfo() {
		JobWaitRuleInfo jobWaitRuleInfo = new JobWaitRuleInfo();

		// 待ち条件タブ
		jobWaitRuleInfo.setCondition(ConditionTypeConstant.TYPE_AND);
		jobWaitRuleInfo.setEndCondition(true);
		jobWaitRuleInfo.setEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);

		// 制御タブ
		jobWaitRuleInfo.setCalendar(false);
		jobWaitRuleInfo.setCalendarEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setCalendarEndValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		jobWaitRuleInfo.setSuspend(false);
		jobWaitRuleInfo.setSkip(false);
		jobWaitRuleInfo.setSkipEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setSkipEndValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		jobWaitRuleInfo.setJobRetryFlg(false);
		jobWaitRuleInfo.setJobRetryEndStatus(EndStatusConstant.INITIAL_VALUE_NORMAL);
		jobWaitRuleInfo.setJobRetry(10);
		jobWaitRuleInfo.setExclusiveBranch(false);
		jobWaitRuleInfo.setExclusiveBranchEndStatus(EndStatusConstant.TYPE_NORMAL);
		jobWaitRuleInfo.setExclusiveBranchEndValue(EndStatusConstant.INITIAL_VALUE_WARNING);

		// 開始遅延タブ
		jobWaitRuleInfo.setStartDelay(false);
		jobWaitRuleInfo.setStartDelayConditionType(ConditionTypeConstant.TYPE_AND);
		jobWaitRuleInfo.setStartDelayNotify(false);
		jobWaitRuleInfo.setStartDelayNotifyPriority(PriorityConstant.TYPE_CRITICAL);
		jobWaitRuleInfo.setStartDelayOperationType(OperationConstant.TYPE_STOP_AT_ONCE);
		jobWaitRuleInfo.setStartDelayOperation(false);
		jobWaitRuleInfo.setStartDelayOperationEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setStartDelayOperationEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setStartDelaySession(false);
		jobWaitRuleInfo.setStartDelaySessionValue(1);
		jobWaitRuleInfo.setStartDelayTime(false);

		// 終了遅延タブ
		jobWaitRuleInfo.setEndDelay(false);
		jobWaitRuleInfo.setEndDelayConditionType(ConditionTypeConstant.TYPE_AND);
		jobWaitRuleInfo.setEndDelayJob(false);
		jobWaitRuleInfo.setEndDelayJobValue(1);
		jobWaitRuleInfo.setEndDelayNotify(false);
		jobWaitRuleInfo.setEndDelayNotifyPriority(PriorityConstant.TYPE_CRITICAL);
		jobWaitRuleInfo.setEndDelayOperation(false);
		jobWaitRuleInfo.setEndDelayOperationEndStatus(EndStatusConstant.TYPE_ABNORMAL);
		jobWaitRuleInfo.setEndDelayOperationEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setEndDelayOperationType(OperationConstant.TYPE_STOP_AT_ONCE);
		jobWaitRuleInfo.setEndDelaySession(false);
		jobWaitRuleInfo.setEndDelaySessionValue(1);
		jobWaitRuleInfo.setEndDelayTime(false);
		jobWaitRuleInfo.setEndDelayChangeMount(false);
		jobWaitRuleInfo.setEndDelayChangeMountValue(1D);

		// 多重度タブ
		jobWaitRuleInfo.setMultiplicityEndValue(EndStatusConstant.INITIAL_VALUE_ABNORMAL);
		jobWaitRuleInfo.setMultiplicityNotify(true);
		jobWaitRuleInfo.setMultiplicityNotifyPriority(PriorityConstant.TYPE_WARNING);
		jobWaitRuleInfo.setMultiplicityOperation(StatusConstant.TYPE_WAIT);

		return jobWaitRuleInfo;
	}

	/**
	 * @param origItem
	 * @param parentItem
	 * @return
	 */
	public static JobTreeItem clone(JobTreeItem origItem, JobTreeItem parentItem) {
		JobTreeItem cloneItem = new JobTreeItem();

		if (origItem.getData() != null) {
			JobInfo jobInfo = new JobInfo();
			JobInfo orgInfo = origItem.getData();
			if (orgInfo.getCreateTime() != null) {
				jobInfo.setCreateTime(orgInfo.getCreateTime());
			}
			jobInfo.setCreateUser(orgInfo.getCreateUser());
			jobInfo.setDescription(orgInfo.getDescription());
			jobInfo.setOwnerRoleId(orgInfo.getOwnerRoleId());
			jobInfo.setRegisteredModule(orgInfo.isRegisteredModule());
			jobInfo.setId(orgInfo.getId());
			jobInfo.setJobunitId(orgInfo.getJobunitId());
			jobInfo.setName(orgInfo.getName());
			jobInfo.setPropertyFull(orgInfo.isPropertyFull());
			jobInfo.setType(orgInfo.getType());
			jobInfo.setIconId(orgInfo.getIconId());
			if (orgInfo.getUpdateTime() != null) {
				jobInfo.setUpdateTime(orgInfo.getUpdateTime());
			}
			jobInfo.setUpdateUser(orgInfo.getUpdateUser());

			if (orgInfo.getCommand() != null) {
				JobCommandInfo jobCommandInfo = new JobCommandInfo();
				jobCommandInfo.setMessageRetryEndFlg(orgInfo.getCommand().isMessageRetryEndFlg());
				jobCommandInfo.setMessageRetryEndValue(orgInfo.getCommand().getMessageRetryEndValue());
				jobCommandInfo.setCommandRetry(orgInfo.getCommand().getCommandRetry());
				jobCommandInfo.setCommandRetryFlg(orgInfo.getCommand().isCommandRetryFlg());
				jobCommandInfo.setCommandRetryEndStatus(orgInfo.getCommand().getCommandRetryEndStatus());
				jobCommandInfo.setFacilityID(orgInfo.getCommand().getFacilityID());
				jobCommandInfo.setProcessingMethod(orgInfo.getCommand().getProcessingMethod());
				jobCommandInfo.setScope(orgInfo.getCommand().getScope());
				jobCommandInfo.setStartCommand(orgInfo.getCommand().getStartCommand());
				jobCommandInfo.setStopCommand(orgInfo.getCommand().getStopCommand());
				jobCommandInfo.setStopType(orgInfo.getCommand().getStopType());
				jobCommandInfo.setSpecifyUser(orgInfo.getCommand().isSpecifyUser());
				jobCommandInfo.setUser(orgInfo.getCommand().getUser());
				jobCommandInfo.setMessageRetry(orgInfo.getCommand().getMessageRetry());
				jobCommandInfo.setManagerDistribution(orgInfo.getCommand().isManagerDistribution());
				jobCommandInfo.setScriptName(orgInfo.getCommand().getScriptName());
				jobCommandInfo.setScriptEncoding(orgInfo.getCommand().getScriptEncoding());
				jobCommandInfo.setScriptContent(orgInfo.getCommand().getScriptContent());
				jobCommandInfo.getEnvVariableInfo().addAll(orgInfo.getCommand().getEnvVariableInfo());
				jobCommandInfo.getJobCommandParamList().addAll(orgInfo.getCommand().getJobCommandParamList());
				jobInfo.setCommand(jobCommandInfo);
			}

			if (orgInfo.getEndStatus() != null) {
				for(JobEndStatusInfo item : orgInfo.getEndStatus()) {
					JobEndStatusInfo jobEndStatusInfo = new JobEndStatusInfo();
					jobEndStatusInfo.setEndRangeValue(item.getEndRangeValue());
					jobEndStatusInfo.setStartRangeValue(item.getStartRangeValue());
					jobEndStatusInfo.setType(item.getType());
					jobEndStatusInfo.setValue(item.getValue());
					jobInfo.getEndStatus().add(jobEndStatusInfo);
				}
			}

			if (orgInfo.getFile() != null) {
				JobFileInfo jobFileInfo = new JobFileInfo();
				jobFileInfo.setCheckFlg(orgInfo.getFile().isCheckFlg());
				jobFileInfo.setCompressionFlg(orgInfo.getFile().isCompressionFlg());
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
				jobFileInfo.setSpecifyUser(orgInfo.getFile().isSpecifyUser());
				jobFileInfo.setMessageRetry(orgInfo.getFile().getMessageRetry());
				jobFileInfo.setMessageRetryEndFlg(orgInfo.getFile().isMessageRetryEndFlg());
				jobFileInfo.setMessageRetryEndValue(orgInfo.getFile().getMessageRetryEndValue());
				jobFileInfo.setCommandRetry(orgInfo.getFile().getCommandRetry());
				jobFileInfo.setCommandRetryFlg(orgInfo.getFile().isCommandRetryFlg());
				jobInfo.setFile(jobFileInfo);
			}

			if (orgInfo.getMonitor() != null) {
				MonitorJobInfo monitorJobInfo = new MonitorJobInfo();
				monitorJobInfo.setCommandRetry(orgInfo.getMonitor().getCommandRetry());
				monitorJobInfo.setCommandRetryFlg(orgInfo.getMonitor().isCommandRetryFlg());
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

			jobInfo.setBeginPriority(orgInfo.getBeginPriority());
			jobInfo.setNormalPriority(orgInfo.getNormalPriority());
			jobInfo.setWarnPriority(orgInfo.getWarnPriority());
			jobInfo.setAbnormalPriority(orgInfo.getAbnormalPriority());

			for (NotifyRelationInfo relationInfo : orgInfo.getNotifyRelationInfos()) {
				jobInfo.getNotifyRelationInfos().add(relationInfo);
			}

			if (orgInfo.getParam() != null) {
				for(JobParameterInfo item : orgInfo.getParam()) {
					JobParameterInfo jobParameterInfo = new JobParameterInfo();
					jobParameterInfo.setDescription(item.getDescription());
					jobParameterInfo.setParamId(item.getParamId());
					jobParameterInfo.setType(item.getType());
					jobParameterInfo.setValue(item.getValue());
					jobInfo.getParam().add(jobParameterInfo);
				}
			}

			if (orgInfo.getWaitRule() != null) {
				JobWaitRuleInfo jobWaitRuleInfo = new JobWaitRuleInfo();
				jobWaitRuleInfo.setCalendar(orgInfo.getWaitRule().isCalendar());
				jobWaitRuleInfo.setCalendarEndStatus(orgInfo.getWaitRule().getCalendarEndStatus());
				jobWaitRuleInfo.setCalendarEndValue(orgInfo.getWaitRule().getCalendarEndValue());
				jobWaitRuleInfo.setCalendarId(orgInfo.getWaitRule().getCalendarId());
				jobWaitRuleInfo.setCondition(orgInfo.getWaitRule().getCondition());
				jobWaitRuleInfo.setEndCondition(orgInfo.getWaitRule().isEndCondition());
				jobWaitRuleInfo.setEndStatus(orgInfo.getWaitRule().getEndStatus());
				jobWaitRuleInfo.setEndValue(orgInfo.getWaitRule().getEndValue());
				jobWaitRuleInfo.setEndDelay(orgInfo.getWaitRule().isEndDelay());
				jobWaitRuleInfo.setEndDelayConditionType(orgInfo.getWaitRule().getEndDelayConditionType());
				jobWaitRuleInfo.setEndDelayJob(orgInfo.getWaitRule().isEndDelayJob());
				jobWaitRuleInfo.setEndDelayJobValue(orgInfo.getWaitRule().getEndDelayJobValue());
				jobWaitRuleInfo.setEndDelayNotify(orgInfo.getWaitRule().isEndDelayNotify());
				jobWaitRuleInfo.setEndDelayNotifyPriority(orgInfo.getWaitRule().getEndDelayNotifyPriority());
				jobWaitRuleInfo.setEndDelayOperation(orgInfo.getWaitRule().isEndDelayOperation());
				jobWaitRuleInfo.setEndDelayOperationEndStatus(orgInfo.getWaitRule().getEndDelayOperationEndStatus());
				jobWaitRuleInfo.setEndDelayOperationEndValue(orgInfo.getWaitRule().getEndDelayOperationEndValue());
				jobWaitRuleInfo.setEndDelayOperationType(orgInfo.getWaitRule().getEndDelayOperationType());
				jobWaitRuleInfo.setEndDelaySession(orgInfo.getWaitRule().isEndDelaySession());
				jobWaitRuleInfo.setEndDelaySessionValue(orgInfo.getWaitRule().getEndDelaySessionValue());
				jobWaitRuleInfo.setEndDelayTime(orgInfo.getWaitRule().isEndDelayTime());
				if (orgInfo.getWaitRule().getEndDelayTimeValue() != null) {
					jobWaitRuleInfo.setEndDelayTimeValue(orgInfo.getWaitRule().getEndDelayTimeValue());
				}
				jobWaitRuleInfo.setEndDelayChangeMount(orgInfo.getWaitRule().isEndDelayChangeMount());
				jobWaitRuleInfo.setEndDelayChangeMountValue(orgInfo.getWaitRule().getEndDelayChangeMountValue());
				jobWaitRuleInfo.setMultiplicityNotify(orgInfo.getWaitRule().isMultiplicityNotify());
				jobWaitRuleInfo.setMultiplicityNotifyPriority(orgInfo.getWaitRule().getMultiplicityNotifyPriority());
				jobWaitRuleInfo.setMultiplicityOperation(orgInfo.getWaitRule().getMultiplicityOperation());
				jobWaitRuleInfo.setMultiplicityEndValue(orgInfo.getWaitRule().getMultiplicityEndValue());
				jobWaitRuleInfo.setSkip(orgInfo.getWaitRule().isSkip());
				jobWaitRuleInfo.setSkipEndStatus(orgInfo.getWaitRule().getSkipEndStatus());
				jobWaitRuleInfo.setSkipEndValue(orgInfo.getWaitRule().getSkipEndValue());
				jobWaitRuleInfo.setExclusiveBranch(orgInfo.getWaitRule().isExclusiveBranch());
				jobWaitRuleInfo.setExclusiveBranchEndStatus(orgInfo.getWaitRule().getExclusiveBranchEndStatus());
				jobWaitRuleInfo.setExclusiveBranchEndValue(orgInfo.getWaitRule().getExclusiveBranchEndValue());
				jobWaitRuleInfo.setJobRetryFlg(orgInfo.getWaitRule().isJobRetryFlg());
				jobWaitRuleInfo.setJobRetryEndStatus(orgInfo.getWaitRule().getJobRetryEndStatus());
				jobWaitRuleInfo.setJobRetry(orgInfo.getWaitRule().getJobRetry());
				jobWaitRuleInfo.setStartDelay(orgInfo.getWaitRule().isStartDelay());
				jobWaitRuleInfo.setStartDelayConditionType(orgInfo.getWaitRule().getStartDelayConditionType());
				jobWaitRuleInfo.setStartDelayNotify(orgInfo.getWaitRule().isStartDelayNotify());
				jobWaitRuleInfo.setStartDelayNotifyPriority(orgInfo.getWaitRule().getStartDelayNotifyPriority());
				jobWaitRuleInfo.setStartDelayOperation(orgInfo.getWaitRule().isStartDelayOperation());
				jobWaitRuleInfo.setStartDelayOperationEndStatus(orgInfo.getWaitRule().getStartDelayOperationEndStatus());
				jobWaitRuleInfo.setStartDelayOperationEndValue(orgInfo.getWaitRule().getStartDelayOperationEndValue());
				jobWaitRuleInfo.setStartDelayOperationType(orgInfo.getWaitRule().getStartDelayOperationType());
				jobWaitRuleInfo.setStartDelaySession(orgInfo.getWaitRule().isStartDelaySession());
				jobWaitRuleInfo.setStartDelaySessionValue(orgInfo.getWaitRule().getStartDelaySessionValue());
				jobWaitRuleInfo.setStartDelayTime(orgInfo.getWaitRule().isStartDelayTime());
				if (orgInfo.getWaitRule().getStartDelayTimeValue() != null) {
					jobWaitRuleInfo.setStartDelayTimeValue(orgInfo.getWaitRule().getStartDelayTimeValue());
				}
				jobWaitRuleInfo.setSuspend(orgInfo.getWaitRule().isSuspend());

				if (orgInfo.getWaitRule().getObject() != null) {
					for(JobObjectInfo item : orgInfo.getWaitRule().getObject()) {
						JobObjectInfo jobObjectInfo = new JobObjectInfo();
						jobObjectInfo.setJobId(item.getJobId());
						jobObjectInfo.setJobName(item.getJobName());
						if (item.getTime() != null) {
							jobObjectInfo.setTime(item.getTime());
						}
						if (item.getStartMinute() != null) {
							jobObjectInfo.setStartMinute(item.getStartMinute());
						}
						jobObjectInfo.setType(item.getType());
						jobObjectInfo.setValue(item.getValue());
						jobObjectInfo.setDescription(item.getDescription());
						jobObjectInfo.setDecisionValue01(item.getDecisionValue01());
						jobObjectInfo.setDecisionValue02(item.getDecisionValue02());
						jobObjectInfo.setDecisionCondition(item.getDecisionCondition());
						jobObjectInfo.setCrossSessionRange(item.getCrossSessionRange());
						jobWaitRuleInfo.getObject().add(jobObjectInfo);
					}
				}
				//後続ジョブ優先度設定
				jobWaitRuleInfo.getExclusiveBranchNextJobOrderList().addAll(orgInfo.getWaitRule().getExclusiveBranchNextJobOrderList());
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
			jobInfo.setUseApprovalReqSentence(orgInfo.isUseApprovalReqSentence());

			cloneItem.setData(jobInfo);
		}

		if (origItem.getDetail() != null) {
			JobDetailInfo jobDetailInfo = new JobDetailInfo();
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
			if (origItem.getDetail().getWaitRuleTime() != null) {
				jobDetailInfo.setWaitRuleTime(origItem.getDetail().getWaitRuleTime());
			}
			cloneItem.setDetail(jobDetailInfo);
		}

		List<JobTreeItem> listOrig = origItem.getChildren();
		if (listOrig != null) {
			for (JobTreeItem orgChild : listOrig) {
				if(orgChild != null){
					cloneItem.getChildren().add(clone(orgChild, cloneItem));
				}
			}
		}

		cloneItem.setParent(parentItem);

		return cloneItem;
	}

	public static void addChildren(JobTreeItem parent, JobTreeItem child){
		parent.getChildren().add(child);
		child.setParent(parent);

		return;
	}

	public static void removeChildren(JobTreeItem parent, JobTreeItem child) {
		List<JobTreeItem> children = parent.getChildren();
		for (int i = 0; i < children.size(); i++) {
			if (child.equals(children.get(i))) {
				children.remove(i);
				break;
			}
		}
	}

	public static String getManagerName(JobTreeItem item) {
		JobTreeItem managerItem = getManager(item);
		if(null != managerItem){
			return managerItem.getData().getName();
		}else{
			return null;
		}
	}

	public static JobTreeItem getManager(JobTreeItem item) {
		if (item == null) {
			return null;
		} else if (item.getData().getType() == JobConstant.TYPE_MANAGER) {
			return item;
		}
		return getManager(item.getParent());
	}
}
