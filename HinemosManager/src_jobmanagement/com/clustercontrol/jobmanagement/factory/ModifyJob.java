/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.JobMasterDuplicate;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.RpaManagementToolEndStatusMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolRunParamMasterNotFound;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobCommandParam;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkInheritInfo;
import com.clustercontrol.jobmanagement.bean.JobNextJobOrderInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectGroupInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobOutputInfo;
import com.clustercontrol.jobmanagement.bean.JobOutputType;
import com.clustercontrol.jobmanagement.bean.JobParameterInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.bean.RpaJobCheckEndValueInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobEndValueConditionInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobOptionInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobRunParamInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobScreenshot;
import com.clustercontrol.jobmanagement.model.JobCommandParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableMstEntity;
import com.clustercontrol.jobmanagement.model.JobLinkInheritMstEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobExpMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderMstEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobOutputMstEntity;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaCheckEndValueMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaCheckEndValueMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobRpaEndValueConditionMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaEndValueConditionMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobRpaOptionMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaOptionMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobRpaRunParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaRunParamMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobRpaScreenshotEntity;
import com.clustercontrol.jobmanagement.model.JobRpaScreenshotEntityPK;
import com.clustercontrol.jobmanagement.model.JobWaitGroupMstEntity;
import com.clustercontrol.jobmanagement.model.JobWaitMstEntity;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaScreenshotTriggerTypeConstant;
import com.clustercontrol.jobmanagement.util.JobValidator;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.rpa.model.RpaManagementToolEndStatusMst;
import com.clustercontrol.rpa.model.RpaManagementToolRunParamMst;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.EntityExistsException;

/**
 * ジョブツリー情報を登録するクラスです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class ModifyJob {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyJob.class );

	public Long replaceJobunit(List<JobInfo> oldList, List<JobInfo> newList, String userId)
			throws JobInvalid, JobMasterNotFound, EntityExistsException, HinemosUnknown, JobMasterDuplicate, InvalidSetting, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			//ジョブユニットのジョブIDを取得
			String jobunitId = newList.get(0).getJobunitId();

			// ジョブマスタ変更
			long start = HinemosTime.currentTimeMillis();

			HashSet<JobInfo>delJobs = new HashSet<JobInfo>(oldList);
			HashSet<JobInfo>newJobs = new HashSet<JobInfo>(newList);
			delJobs.removeAll(newJobs);

			long timeJobToDelete = HinemosTime.currentTimeMillis();
			m_log.info("Find jobs to delete " + (timeJobToDelete - start) + "ms");

			HashSet<JobInfo> addJobs = newJobs;
			addJobs.removeAll(new HashSet<JobInfo>(oldList));

			long timeJobToAdd = HinemosTime.currentTimeMillis();
			m_log.info("Find jobs to add " + (timeJobToAdd - timeJobToDelete) + "ms");
			m_log.info("oldList=" + oldList.size() + ", newList=" + newList.size() +
					", delJobs=" + delJobs.size() + ", addJobs=" + addJobs.size());

			// #11177: 削除予定ジョブについて(実行契機から参照されていないか)事前チェックを行う
			JobValidator.validateJobsToDelete(delJobs, addJobs);

			for (JobInfo delJob : delJobs) {
				JobMstEntity entity = QueryUtil.getJobMstPK(delJob.getJobunitId(), delJob.getId());
				deleteJob(entity);
			}

			jtm.flush();

			long timestamp = HinemosTime.currentTimeMillis();

			// ジョブユニットを最初に登録する必要があるため。
			for (JobInfo addJob : addJobs) {
				int type = addJob.getType();
				if (type == JobConstant.TYPE_JOBUNIT) {
					String jobId = addJob.getId();
					String parentJobId = addJob.getParentId();
					if (jobunitId.equals(jobId)) {
						parentJobId = CreateJobSession.TOP_JOB_ID;
					}
					createJobMasterData(addJob, jobunitId, parentJobId, userId, timestamp);
					addJobs.remove(addJob);
					break;
				}
			}
			for (JobInfo addJob : addJobs) {
				m_log.debug("replaceJobunit() : addJob JobunitId=" + addJob.getJobunitId() + ", JobId=" + addJob.getId());
				String jobId = addJob.getId();
				String parentJobId = addJob.getParentId();
				if (jobunitId.equals(jobId)) {
					parentJobId = CreateJobSession.TOP_JOB_ID;
				}
				createJobMasterData(addJob, jobunitId, parentJobId, userId, timestamp);
			}

			// ジョブユニットの最終更新日時の更新
			String jobId = newList.get(0).getId();
			JobMstEntity entity = QueryUtil.getJobMstPK(jobunitId, jobId);
			entity.setUpdateDate(timestamp);
			m_log.info("Left tasks in replaceJobunit " + (HinemosTime.currentTimeMillis() - timeJobToAdd) + "ms");
			
			// ジョブユニットの最終更新日時を返す
			return timestamp;
		}
	}

	/**
	 * ジョブツリー情報を登録します。
	 * <p>
	 * <ol>
	 * <li>全ジョブマスタ情報を取得します。</li>
	 * <li>取得したジョブマスタ情報の数だけ以下の処理を行います</li>
	 *   <ol>
	 *   <li>ジョブリレーションマスタを削除します。</li>
	 *   <li>ジョブコマンドマスタを削除します。</li>
	 *   <li>ジョブファイル転送マスタを削除します。</li>
	 *     <ol>
	 *     <li>ジョブ待ち条件ジョブマスタを削除します。</li>
	 *     <li>ジョブ待ち条件時刻マスタを削除します。</li>
	 *     <li>ジョブ待ち条件マスタを削除します。</li>
	 *     </ol>
	 *   <li>ジョブ通知マスタを削除します。</li>
	 *   <li>ジョブ終了状態マスタを削除します。</li>
	 *   <li>ジョブ変数マスタを削除します。</li>
	 *   <li>ジョブマスタを削除します。</li>
	 *   </ol>
	 *   <li>ジョブツリー情報を基に、ジョブマスタを作成します。</li>
	 * </ol>
	 *
	 * @param item ジョブツリー情報
	 * @param userId ユーザID
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 * @throws JobMasterDuplicate
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJob#createJobMaster(JobTreeItem, String)
	 */
	public Long registerJobunit(JobTreeItem jobunit, String userId)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, JobMasterDuplicate, InvalidSetting, InvalidRole {

		//ジョブユニットのジョブIDを取得
		String jobunitId = jobunit.getData().getJobunitId();

		int msec = HinemosTime.getCalendarInstance().get(Calendar.MILLISECOND);
		m_log.debug("registerJobunit() start : " + msec);

		long timestamp = HinemosTime.currentTimeMillis();
		
		// ジョブマスタ作成
		createJobMaster(jobunit, jobunitId, CreateJobSession.TOP_JOB_ID, userId, timestamp);

		m_log.debug("registerJobunit() End : " + msec);
		
		// ジョブユニットの最終更新日時を返す
		return timestamp;
	}

	/**
	 * 指定されたジョブユニットを削除する
	 *
	 * @param jobunitId 削除対象ジョブユニットのジョブID
	 * @throws HinemosUnknown
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws InvalidSetting
	 */
	public void deleteJobunit(String jobunitId, String userId) throws HinemosUnknown, ObjectPrivilege_InvalidRole, InvalidSetting {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 引数で指定されたジョブユニットIDを持つジョブユニットを取得
			Collection<JobMstEntity> ct =
					em.createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class, ObjectPrivilegeMode.MODIFY)
					.setParameter("jobunitId", jobunitId).getResultList();

			// オブジェクト権限チェック(削除対象のリストが空だった場合)
			if (ct == null || ct.size() == 0) {
				ObjectPrivilege_InvalidRole e = new ObjectPrivilege_InvalidRole(
						"targetClass = " + JobMstEntity.class.getSimpleName());
				m_log.info("deleteJobunit() : object privilege invalid. jobunitId = " + jobunitId);

				throw e;
			}

			// #11177: 削除予定ジョブユニットについて(実行契機から参照されていないか)事前チェックを行う
			JobValidator.validateJobunitToDelete(jobunitId);

			for (JobMstEntity jobMstEntity : ct) {
				deleteJob(jobMstEntity);
			}

			// オブジェクト権限情報を削除する。
			ObjectPrivilegeUtil.deleteObjectPrivilege(HinemosModuleConstant.JOB, jobunitId);
		}
	}

	private void deleteJob(JobMstEntity jobMstEntity) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			if (jobMstEntity != null) {
				String jobunitId = jobMstEntity.getId().getJobunitId();
				String jobId = jobMstEntity.getId().getJobId();
				// ジョブユニットに紐づく通知を削除
				// "JOB_MST-junit-job-0"
				em.createNamedQuery("NotifyRelationInfoEntity.deleteByNotifyGroupId", Integer.class)
				.setParameter("notifyGroupId", HinemosModuleConstant.JOB_MST +
						"-" + jobunitId + "-" + jobId + "-0")
						.executeUpdate();
				em.remove(jobMstEntity);
			}
		}
	}

	/**
	 * ジョブマスタを作成します。 再帰呼び出しを行います。
	 * <p>
	 * <ol>
	 * <li>ジョブツリー情報を基に、ジョブマスタを作成します。</li>
	 * <li>ジョブツリー情報の配下のジョブツリー情報を取得します。</li>
	 * <li>取得した配下のジョブツリー情報の数、以下の処理を行います。</li>
	 * <ol>
	 * <li>ジョブツリー情報を基に、ジョブマスタを作成します。</li>
	 * </ol>
	 * </ol>
	 *
	 * @param item
	 *            ジョブツリー情報
	 * @param user
	 *            ユーザID
	 * @throws HinemosUnknown
	 * @throws JobMasterDuplicate
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJob#createJobMasterData(JobTreeItem,
	 *      String)
	 */
	private void createJobMaster(JobTreeItem item, String jobunitId, String parentId, String user, Long updateDate)
			throws HinemosUnknown, JobMasterDuplicate, JobMasterNotFound, InvalidSetting, InvalidRole {

		//ジョブマスタデータ作成
		createJobMasterData(item.getData(), jobunitId, parentId, user, updateDate);

		//子JobTreeItemを取得
		for (JobTreeItem child : item.getChildrenArray()) {
			//ジョブマスタ作成
			createJobMaster(child, jobunitId, item.getData().getId(), user, updateDate);
		}
	}

	/**
	 * ジョブマスタを作成します。
	 * <p>
	 * <ol>
	 * <li>ジョブツリー情報を基に、ジョブマスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブリレーションマスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ待ち条件マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ待ち条件ジョブマスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ待ち条件時刻マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブコマンドマスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブファイル転送マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ通知マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ終了状態マスタを作成します。</li>
	 * <li>ジョブツリー情報を基に、ジョブ変数マスタを作成します。</li>
	 * </ol>
	 *
	 * @param item ジョブツリー情報
	 * @param user ユーザID
	 * @throws HinemosUnknown
	 * @throws JobMasterDuplicate
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private void createJobMasterData(JobInfo info, String jobunitId, String parentId, String user, Long updateDate)
			throws HinemosUnknown, JobMasterDuplicate, JobMasterNotFound, InvalidSetting, InvalidRole {

		m_log.debug("createJobMasterData");

		// ジョブユニット単位で再帰的に登録するため、親となるジョブユニットIDは変わらない
		// ただし、TOPの親ジョブユニットIDはTOPとする
		String parentJobunitId = null;
		if (CreateJobSession.TOP_JOB_ID.equals(parentId)) {
			parentJobunitId = CreateJobSession.TOP_JOBUNIT_ID;
		} else {
			parentJobunitId = jobunitId;
		}

		//判定対象ジョブのジョブユニットも同じ
		String waitJobunitId = jobunitId;

		if(info.getCreateUser() == null) {
			info.setCreateUser(user);
		}

		if(info.getCreateTime() == null) {
			info.setCreateTime(updateDate);
		}

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ作成
			// インスタンス生成
			JobMstEntity jobMst = new JobMstEntity(jobunitId, info.getId(), info.getType());
			// 重複チェック
			jtm.checkEntityExists(JobMstEntity.class, jobMst.getId());
			// 登録
			em.persist(jobMst);
			jobMst.setDescription(info.getDescription());
			jobMst.setJobName(info.getName());
			jobMst.setRegisteredModule(info.isRegisteredModule());
			jobMst.setRegDate(info.getCreateTime());
			jobMst.setRegUser(info.getCreateUser());
			jobMst.setUpdateDate(updateDate);
			jobMst.setUpdateUser(user);
			jobMst.setParentJobunitId(parentJobunitId);
			jobMst.setParentJobId(parentId);
			jobMst.setIconId(info.getIconId());
			if (info.getType() == null) {
				// 何もしない
			} else if(info.getType() == JobConstant.TYPE_JOBUNIT){
				jobMst.setOwnerRoleId(info.getOwnerRoleId());
				jobMst.setExpNodeRuntimeFlg(info.getExpNodeRuntimeFlg());
			} else {
				// JOBUNIT以外は、JOBUNITの値を設定する
				try {
					JobMstEntity jobMstEntity
						= QueryUtil.getJobMstPK_NONE(new JobMstEntityPK(jobMst.getId().getJobunitId(), jobMst.getId().getJobunitId()));
					if (jobMstEntity.getOwnerRoleId() == null) {
						jobMst.setOwnerRoleId(RoleIdConstant.INTERNAL);
					} else {
						jobMst.setOwnerRoleId(jobMstEntity.getOwnerRoleId());
					}
				} catch (JobMasterNotFound e) {
					jobMst.setOwnerRoleId(RoleIdConstant.INTERNAL);
				}
				jobMst.setExpNodeRuntimeFlg(null);
			}

			//待ち条件作成
			if(info.getWaitRule() != null){
				m_log.debug("info.getWaitRule = " + info.getWaitRule());
				JobWaitRuleInfo waitRule = info.getWaitRule();
				Long startDelayTime = null;
				Long endDelayTime = null;
				if (waitRule.getStart_delay_time_value() != null) {
					startDelayTime = waitRule.getStart_delay_time_value();
				}
				if (waitRule.getEnd_delay_time_value() != null) {
					endDelayTime = waitRule.getEnd_delay_time_value();
				}
				jobMst.setConditionType(waitRule.getCondition());
				jobMst.setSuspend(waitRule.isSuspend());
				jobMst.setSkip(waitRule.isSkip());
				jobMst.setSkipEndStatus(waitRule.getSkipEndStatus());
				jobMst.setSkipEndValue(waitRule.getSkipEndValue());
				jobMst.setUnmatchEndFlg(waitRule.isEndCondition());
				jobMst.setUnmatchEndStatus(waitRule.getEndStatus());
				jobMst.setUnmatchEndValue(waitRule.getEndValue());
				jobMst.setExclusiveBranchFlg(waitRule.isExclusiveBranch());
				jobMst.setExclusiveBranchEndStatus(waitRule.getExclusiveBranchEndStatus());
				jobMst.setExclusiveBranchEndValue(waitRule.getExclusiveBranchEndValue());
				jobMst.setCalendar(waitRule.isCalendar());
				jobMst.setCalendarId(waitRule.getCalendarId());
				jobMst.setCalendarEndStatus(waitRule.getCalendarEndStatus());
				jobMst.setCalendarEndValue(waitRule.getCalendarEndValue());
				jobMst.setStartDelay(waitRule.isStart_delay());
				jobMst.setStartDelaySession(waitRule.isStart_delay_session());
				jobMst.setStartDelaySessionValue(waitRule.getStart_delay_session_value());
				jobMst.setStartDelayTime(waitRule.isStart_delay_time());
				jobMst.setStartDelayTimeValue(startDelayTime);
				jobMst.setStartDelayConditionType(waitRule.getStart_delay_condition_type());
				jobMst.setStartDelayNotify(waitRule.isStart_delay_notify());
				jobMst.setStartDelayNotifyPriority(waitRule.getStart_delay_notify_priority());
				jobMst.setStartDelayOperation(waitRule.isStart_delay_operation());
				jobMst.setStartDelayOperationType(waitRule.getStart_delay_operation_type());
				jobMst.setStartDelayOperationEndStatus(waitRule.getStart_delay_operation_end_status());
				jobMst.setStartDelayOperationEndValue(waitRule.getStart_delay_operation_end_value());
				jobMst.setEndDelay(waitRule.isEnd_delay());
				jobMst.setEndDelaySession(waitRule.isEnd_delay_session());
				jobMst.setEndDelaySessionValue(waitRule.getEnd_delay_session_value());
				jobMst.setEndDelayJob(waitRule.isEnd_delay_job());
				jobMst.setEndDelayJobValue(waitRule.getEnd_delay_job_value());
				jobMst.setEndDelayTime(waitRule.isEnd_delay_time());
				jobMst.setEndDelayTimeValue(endDelayTime);
				jobMst.setEndDelayConditionType(waitRule.getEnd_delay_condition_type());
				jobMst.setEndDelayNotify(waitRule.isEnd_delay_notify());
				jobMst.setEndDelayNotifyPriority(waitRule.getEnd_delay_notify_priority());
				jobMst.setEndDelayOperation(waitRule.isEnd_delay_operation());
				jobMst.setEndDelayOperationType(waitRule.getEnd_delay_operation_type());
				jobMst.setEndDelayOperationEndStatus(waitRule.getEnd_delay_operation_end_status());
				jobMst.setEndDelayOperationEndValue(waitRule.getEnd_delay_operation_end_value());
				jobMst.setEndDelayChangeMount(waitRule.isEnd_delay_change_mount());
				jobMst.setEndDelayChangeMountValue(waitRule.getEnd_delay_change_mount_value());
				jobMst.setMultiplicityNotify(waitRule.isMultiplicityNotify());
				jobMst.setMultiplicityNotifyPriority(waitRule.getMultiplicityNotifyPriority());
				jobMst.setMultiplicityOperation(waitRule.getMultiplicityOperation());
				jobMst.setMultiplicityEndValue(waitRule.getMultiplicityEndValue());
				jobMst.setJobRetryFlg(waitRule.getJobRetryFlg());
				jobMst.setJobRetry(waitRule.getJobRetry());
				jobMst.setJobRetryInterval(waitRule.getJobRetryInterval());
				jobMst.setJobRetryEndStatus(waitRule.getJobRetryEndStatus());
				jobMst.setQueueFlg(waitRule.getQueueFlg());
				jobMst.setQueueId(waitRule.getQueueId());
				if(waitRule.getObjectGroup() != null){
					int orderNo = 0;
					for(JobObjectGroupInfo objectGroupInfo : waitRule.getObjectGroup()) {
						if (objectGroupInfo == null) {
							continue;
						}
						JobWaitGroupMstEntity jobWaitGroupMstEntity = new JobWaitGroupMstEntity(jobMst, orderNo);
						jobWaitGroupMstEntity.setConditionType(objectGroupInfo.getConditionType());
						jobWaitGroupMstEntity.setJobWaitMstEntities(new ArrayList<>());
						// 重複チェック
						jtm.checkEntityExists(JobWaitGroupMstEntity.class, jobWaitGroupMstEntity.getId());
						for (JobObjectInfo objectInfo : objectGroupInfo.getJobObjectList()) {
							if(objectInfo == null){
								continue;
							}
							if(objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getStatus = " + objectInfo.getStatus());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								// インスタンス生成
								JobWaitMstEntity jobWaitJobMstEntity = JobWaitMstEntity.createJobEndStatus(
										jobWaitGroupMstEntity,
										waitJobunitId,
										objectInfo.getJobId(),
										objectInfo.getStatus());
								// 重複チェック
								jtm.checkEntityExists(JobWaitMstEntity.class, jobWaitJobMstEntity.getId());
								jobWaitJobMstEntity.relateToJobWaitGroupMstEntity(jobWaitGroupMstEntity);
								jobWaitJobMstEntity.setDescription(objectInfo.getDescription());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getDecisionCondition = " + objectInfo.getDecisionCondition());
								m_log.debug("objectInfo.getValue = " + objectInfo.getValue());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								// インスタンス生成
								JobWaitMstEntity jobWaitJobMstEntity = JobWaitMstEntity.createJobEndValue(
										jobWaitGroupMstEntity,
										waitJobunitId,
										objectInfo.getJobId(),
										objectInfo.getDecisionCondition(),
										objectInfo.getValue());
								// 重複チェック
								jtm.checkEntityExists(JobWaitMstEntity.class, jobWaitJobMstEntity.getId());
								jobWaitJobMstEntity.relateToJobWaitGroupMstEntity(jobWaitGroupMstEntity);
								jobWaitJobMstEntity.setDescription(objectInfo.getDescription());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_TIME) {
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getTime = " + objectInfo.getTime());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								// インスタンス生成
								JobWaitMstEntity jobWaitJobMstEntity = JobWaitMstEntity.createTime(
										jobWaitGroupMstEntity,
										objectInfo.getTime());
								// 重複チェック
								jtm.checkEntityExists(JobWaitMstEntity.class, jobWaitJobMstEntity.getId());
								jobWaitJobMstEntity.relateToJobWaitGroupMstEntity(jobWaitGroupMstEntity);
								jobWaitJobMstEntity.setDescription(objectInfo.getDescription());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getStartMinute = " + objectInfo.getStartMinute());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								// インスタンス生成
								JobWaitMstEntity jobWaitJobMstEntity = JobWaitMstEntity.createStartMinute(
										jobWaitGroupMstEntity,
										objectInfo.getStartMinute());
								// 重複チェック
								jtm.checkEntityExists(JobWaitMstEntity.class, jobWaitJobMstEntity.getId());
								jobWaitJobMstEntity.relateToJobWaitGroupMstEntity(jobWaitGroupMstEntity);
								jobWaitJobMstEntity.setDescription(objectInfo.getDescription());
							}
							else if (objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getDecisionValue = " + objectInfo.getDecisionValue());
								m_log.debug("objectInfo.getDecisionCondition = " + objectInfo.getDecisionCondition());
								m_log.debug("objectInfo.getValue = " + objectInfo.getValue());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								// インスタンス生成
								JobWaitMstEntity jobWaitJobMstEntity = JobWaitMstEntity.createJobParameter(
										jobWaitGroupMstEntity,
										objectInfo.getDecisionCondition(),
										objectInfo.getDecisionValue(),
										objectInfo.getValue());
								// 重複チェック
								jtm.checkEntityExists(JobWaitMstEntity.class, jobWaitJobMstEntity.getId());
								jobWaitJobMstEntity.relateToJobWaitGroupMstEntity(jobWaitGroupMstEntity);
								jobWaitJobMstEntity.setDescription(objectInfo.getDescription());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getStatus = " + objectInfo.getStatus());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								m_log.debug("objectInfo.getCrossSessionRange = " + objectInfo.getCrossSessionRange());
								// インスタンス生成
								JobWaitMstEntity jobWaitJobMstEntity = JobWaitMstEntity.createCrossSessionJobEndStatus(
										jobWaitGroupMstEntity,
										waitJobunitId,
										objectInfo.getJobId(),
										objectInfo.getStatus(),
										objectInfo.getCrossSessionRange());
								// 重複チェック
								jtm.checkEntityExists(JobWaitMstEntity.class, jobWaitJobMstEntity.getId());
								jobWaitJobMstEntity.relateToJobWaitGroupMstEntity(jobWaitGroupMstEntity);
								jobWaitJobMstEntity.setDescription(objectInfo.getDescription());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getDecisionCondition = " + objectInfo.getDecisionCondition());
								m_log.debug("objectInfo.getValue = " + objectInfo.getValue());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								m_log.debug("objectInfo.getCrossSessionRange = " + objectInfo.getCrossSessionRange());

								// インスタンス生成
								JobWaitMstEntity jobWaitJobMstEntity = JobWaitMstEntity.createCrossSessionJobEndValue(
										jobWaitGroupMstEntity,
										waitJobunitId,
										objectInfo.getJobId(),
										objectInfo.getDecisionCondition(),
										objectInfo.getCrossSessionRange(),
										objectInfo.getValue());
								// 重複チェック
								jtm.checkEntityExists(JobWaitMstEntity.class, jobWaitJobMstEntity.getId());
								jobWaitJobMstEntity.relateToJobWaitGroupMstEntity(jobWaitGroupMstEntity);
								jobWaitJobMstEntity.setDescription(objectInfo.getDescription());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getDecisionCondition = " + objectInfo.getDecisionCondition());
								m_log.debug("objectInfo.getValue = " + objectInfo.getValue());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());

								// インスタンス生成
								JobWaitMstEntity jobWaitJobMstEntity = JobWaitMstEntity.createJobReturnValue(
										jobWaitGroupMstEntity,
										waitJobunitId,
										objectInfo.getJobId(),
										objectInfo.getDecisionCondition(),
										objectInfo.getValue());
								// 重複チェック
								jtm.checkEntityExists(JobWaitMstEntity.class, jobWaitJobMstEntity.getId());
								jobWaitJobMstEntity.relateToJobWaitGroupMstEntity(jobWaitGroupMstEntity);
								jobWaitJobMstEntity.setDescription(objectInfo.getDescription());
							}
						}
						if (jobWaitGroupMstEntity.getJobWaitMstEntities().isEmpty()) {
							continue;
						}
						jobWaitGroupMstEntity.setIsGroup(jobWaitGroupMstEntity.getJobWaitMstEntities().size() > 1);
						jobWaitGroupMstEntity.relateToJobMstEntity(jobMst);
						// 登録
						em.persist(jobWaitGroupMstEntity);
						orderNo++;
					}
				}
				//排他分岐設定を取得
				if (info.getWaitRule().isExclusiveBranch() && info.getWaitRule().getExclusiveBranchNextJobOrderList() != null) {
					jobMst.setExclusiveBranchFlg(waitRule.isExclusiveBranch());
					jobMst.setExclusiveBranchEndStatus(waitRule.getExclusiveBranchEndStatus());
					jobMst.setExclusiveBranchEndValue(waitRule.getExclusiveBranchEndValue());
					List<JobNextJobOrderInfo> nextJobOrderList = waitRule.getExclusiveBranchNextJobOrderList();

					//後続ジョブ優先度インスタンス生成
					for (JobNextJobOrderInfo nextJobOrderInfo: nextJobOrderList) {
						m_log.debug("nextJobOrderInfo : JobunitId=" + nextJobOrderInfo.getJobunitId());
						m_log.debug("nextJobOrderInfo : JobId=" + nextJobOrderInfo.getJobId());
						m_log.debug("nextJobOrderInfo : NextJobId=" + nextJobOrderInfo.getNextJobId());
						JobNextJobOrderMstEntity nextJobOrderMst = new JobNextJobOrderMstEntity(new JobNextJobOrderMstEntityPK(
							nextJobOrderInfo.getJobunitId(), nextJobOrderInfo.getJobId(), nextJobOrderInfo.getNextJobId())
						);
						// 登録
						em.persist(nextJobOrderMst);
						//nextJobOrderListは優先度順になっているためインデックスを優先度として登録する
						nextJobOrderMst.setOrder(nextJobOrderList.indexOf(nextJobOrderInfo) + 1);
					}
					
				}
			}

			//実行コマンド作成
			if(info.getCommand() != null){
				jobMst.setFacilityId(info.getCommand().getFacilityID());
				jobMst.setProcessMode(info.getCommand().getProcessingMethod());
				jobMst.setStartCommand(info.getCommand().getStartCommand());
				jobMst.setStopType(info.getCommand().getStopType());
				jobMst.setStopCommand(info.getCommand().getStopCommand());
				jobMst.setSpecifyUser(info.getCommand().getSpecifyUser());
				jobMst.setEffectiveUser(info.getCommand().getUser());
				jobMst.setMessageRetry(info.getCommand().getMessageRetry());
				jobMst.setMessageRetryEndFlg(info.getCommand().getMessageRetryEndFlg());
				jobMst.setMessageRetryEndValue(info.getCommand().getMessageRetryEndValue());
				jobMst.setArgumentJobId("");
				jobMst.setArgument("");
				jobMst.setCommandRetryFlg(info.getCommand().getCommandRetryFlg());
				jobMst.setCommandRetry(info.getCommand().getCommandRetry());
				jobMst.setCommandRetryEndStatus(info.getCommand().getCommandRetryEndStatus());
				if (info.getCommand().getJobCommandParamList() != null
						&& info.getCommand().getJobCommandParamList().size() > 0) {
					for (JobCommandParam jobCommandParam : info.getCommand().getJobCommandParamList()) {
						JobCommandParamMstEntity jobCommandParamEntity = new JobCommandParamMstEntity(jobMst, jobCommandParam.getParamId());
						jobCommandParamEntity.setJobStandardOutputFlg(jobCommandParam.getJobStandardOutputFlg());
						jobCommandParamEntity.setValue(jobCommandParam.getValue());
						// 登録
						em.persist(jobCommandParamEntity);
						jobCommandParamEntity.relateToJobMstEntity(jobMst);
					}
				}
				jobMst.setManagerDistribution(info.getCommand().getManagerDistribution());
				jobMst.setScriptName(info.getCommand().getScriptName());
				jobMst.setScriptEncoding(info.getCommand().getScriptEncoding());
				jobMst.setScriptContent(info.getCommand().getScriptContent());
				
				//環境変数
				if(info.getCommand().getEnvVariableInfo() != null) {
					for(JobEnvVariableInfo envVariableInfo : info.getCommand().getEnvVariableInfo()){
						if(envVariableInfo != null){
							// インスタンス生成
							JobEnvVariableMstEntity jobEnvVariableMstEntity = new JobEnvVariableMstEntity(jobMst, envVariableInfo.getEnvVariableId());
							// 重複チェック
							jtm.checkEntityExists(JobEnvVariableMstEntity.class, jobEnvVariableMstEntity.getId());
							jobEnvVariableMstEntity.setDescription(envVariableInfo.getDescription());
							jobEnvVariableMstEntity.setValue(envVariableInfo.getValue());
							// 登録
							em.persist(jobEnvVariableMstEntity);
							jobEnvVariableMstEntity.relateToJobMstEntity(jobMst);
						}
					}
				}

				// 標準出力のファイル出力
				JobOutputInfo outputInfo = info.getCommand().getNormalJobOutputInfo();
				// インスタンス生成
				JobOutputMstEntity jobOutputMstEntity = new JobOutputMstEntity(jobMst, JobOutputType.STDOUT.getCode());
				jobOutputMstEntity.setSameNormalFlg(outputInfo.getSameNormalFlg());
				jobOutputMstEntity.setDirectory(outputInfo.getDirectory());
				jobOutputMstEntity.setFileName(outputInfo.getFileName());
				jobOutputMstEntity.setAppendFlg(outputInfo.getAppendFlg());
				jobOutputMstEntity.setFailureOperationFlg(outputInfo.getFailureOperationFlg());
				jobOutputMstEntity.setFailureOperationType(outputInfo.getFailureOperationType());
				jobOutputMstEntity.setFailureOperationEndStatus(outputInfo.getFailureOperationEndStatus());
				jobOutputMstEntity.setFailureOperationEndValue(outputInfo.getFailureOperationEndValue());
				jobOutputMstEntity.setFailureNotifyFlg(outputInfo.getFailureNotifyFlg());
				jobOutputMstEntity.setFailureNotifyPriority(outputInfo.getFailureNotifyPriority());
				jobOutputMstEntity.setValid(outputInfo.getValid());
				// 登録
				em.persist(jobOutputMstEntity);
				jobOutputMstEntity.relateToJobMstEntity(jobMst);

				// 標準エラー出力のファイル出力
				outputInfo = info.getCommand().getErrorJobOutputInfo();
				// インスタンス生成
				jobOutputMstEntity = new JobOutputMstEntity(jobMst, JobOutputType.STDERR.getCode());
				jobOutputMstEntity.setSameNormalFlg(outputInfo.getSameNormalFlg());
				jobOutputMstEntity.setDirectory(outputInfo.getDirectory());
				jobOutputMstEntity.setFileName(outputInfo.getFileName());
				jobOutputMstEntity.setAppendFlg(outputInfo.getAppendFlg());
				jobOutputMstEntity.setFailureOperationFlg(outputInfo.getFailureOperationFlg());
				jobOutputMstEntity.setFailureOperationType(outputInfo.getFailureOperationType());
				jobOutputMstEntity.setFailureOperationEndStatus(outputInfo.getFailureOperationEndStatus());
				jobOutputMstEntity.setFailureOperationEndValue(outputInfo.getFailureOperationEndValue());
				jobOutputMstEntity.setFailureNotifyFlg(outputInfo.getFailureNotifyFlg());
				jobOutputMstEntity.setFailureNotifyPriority(outputInfo.getFailureNotifyPriority());
				jobOutputMstEntity.setValid(outputInfo.getValid());
				// 登録
				em.persist(jobOutputMstEntity);
				jobOutputMstEntity.relateToJobMstEntity(jobMst);
			}

			//ファイル転送作成
			if(info.getFile() != null){
				jobMst.setProcessMode(info.getFile().getProcessingMethod());
				jobMst.setSrcFacilityId(info.getFile().getSrcFacilityID());
				jobMst.setDestFacilityId(info.getFile().getDestFacilityID());
				jobMst.setSrcFile(info.getFile().getSrcFile());
				jobMst.setSrcWorkDir(info.getFile().getSrcWorkDir());
				jobMst.setDestDirectory(info.getFile().getDestDirectory());
				jobMst.setDestWorkDir(info.getFile().getDestWorkDir());
				jobMst.setCompressionFlg(info.getFile().isCompressionFlg());
				jobMst.setCheckFlg(info.getFile().isCheckFlg());
				jobMst.setSpecifyUser(info.getFile().isSpecifyUser());
				jobMst.setEffectiveUser(info.getFile().getUser());
				jobMst.setMessageRetry(info.getFile().getMessageRetry());
				jobMst.setMessageRetryEndFlg(info.getFile().isMessageRetryEndFlg());
				jobMst.setMessageRetryEndValue(info.getFile().getMessageRetryEndValue());
				// 繰り返し実行は行わない
				jobMst.setCommandRetryFlg(false);
				jobMst.setCommandRetry(10);
				jobMst.setCommandRetryEndStatus(EndStatusConstant.TYPE_NORMAL);
			}

			// 監視ジョブ作成
			if (info.getMonitor() != null) {
				jobMst.setFacilityId(info.getMonitor().getFacilityID());
				jobMst.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
				jobMst.setProcessMode(info.getMonitor().getProcessingMethod());
				jobMst.setArgumentJobId("");
				jobMst.setArgument("");
				jobMst.setMonitorId(info.getMonitor().getMonitorId());
				jobMst.setMonitorInfoEndValue(info.getMonitor().getMonitorInfoEndValue());
				jobMst.setMonitorWarnEndValue(info.getMonitor().getMonitorWarnEndValue());
				jobMst.setMonitorCriticalEndValue(info.getMonitor().getMonitorCriticalEndValue());
				jobMst.setMonitorUnknownEndValue(info.getMonitor().getMonitorUnknownEndValue());
				jobMst.setMonitorWaitTime(info.getMonitor().getMonitorWaitTime());
				jobMst.setMonitorWaitEndValue(info.getMonitor().getMonitorWaitEndValue());
				// 繰り返し実行は行わない
				jobMst.setCommandRetryFlg(false);
				jobMst.setCommandRetry(10);
				jobMst.setCommandRetryEndStatus(EndStatusConstant.TYPE_NORMAL);
				// エラー時は終了にする（戻り値は不明と同じ。）
				jobMst.setMessageRetry(0);
				jobMst.setMessageRetryEndFlg(true);
				jobMst.setMessageRetryEndValue(info.getMonitor().getMonitorUnknownEndValue());
			}

			// ファイルチェックジョブ作成
			if (info.getJobFileCheck() != null) {
				// スコープ
				jobMst.setFacilityId(info.getJobFileCheck().getFacilityID());
				// ディレクトリ
				jobMst.setDirectory(info.getJobFileCheck().getDirectory());
				// ファイル名
				jobMst.setFileName(info.getJobFileCheck().getFileName());
				// スコープ処理
				jobMst.setProcessMode(info.getJobFileCheck().getProcessingMethod());
				// チェック種別（作成）
				jobMst.setCreateValidFlg(info.getJobFileCheck().getCreateValidFlg());
				// ジョブ開始前に作成されたファイルも対象にする
				jobMst.setCreateBeforeJobStartFlg(info.getJobFileCheck().getCreateBeforeJobStartFlg());
				// チェック種別（削除）
				jobMst.setDeleteValidFlg(info.getJobFileCheck().getDeleteValidFlg());
				// チェック種別（変更）
				jobMst.setModifyValidFlg(info.getJobFileCheck().getModifyValidFlg());
				// 変更判定
				jobMst.setModifyType(info.getJobFileCheck().getModifyType());
				// ファイルの使用中は判定しない
				jobMst.setNotJudgeFileInUseFlg(info.getJobFileCheck().getNotJudgeFileInUseFlg());
				// 条件を満たした場合の終了値
				jobMst.setSuccessEndValue(info.getJobFileCheck().getSuccessEndValue());
				// 条件を満たさなければ終了する
				jobMst.setFailureEndFlg(info.getJobFileCheck().getFailureEndFlg());
				// 条件を満たさなければ終了する - タイムアウト
				jobMst.setFailureWaitTime(info.getJobFileCheck().getFailureWaitTime());
				// 条件を満たさなければ終了する - 終了値
				jobMst.setFailureEndValue(info.getJobFileCheck().getFailureEndValue());
				// 停止種別
				jobMst.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
				// リトライ回数
				jobMst.setMessageRetry(info.getJobFileCheck().getMessageRetry());
				// 実行失敗時終了フラグ
				jobMst.setMessageRetryEndFlg(info.getJobFileCheck().getMessageRetryEndFlg());
				// 実行失敗時終了値
				jobMst.setMessageRetryEndValue(info.getJobFileCheck().getMessageRetryEndValue());
				// 繰り返し実行は行わない	
				jobMst.setCommandRetryFlg(false);
				jobMst.setCommandRetry(10);
				jobMst.setCommandRetryEndStatus(EndStatusConstant.TYPE_NORMAL);
			}

			// ジョブ連携送信ジョブ作成
			if (info.getJobLinkSend() != null) {
				jobMst.setProcessMode(info.getJobLinkSend().getProcessingMethod());
				// 送信に失敗した場合に再送する
				jobMst.setRetryFlg(info.getJobLinkSend().getRetryFlg());
				// 送信に失敗した場合に再送する - 再送回数
				jobMst.setRetryCount(info.getJobLinkSend().getRetryCount());
				// 送信失敗時の操作
				jobMst.setFailureOperation(info.getJobLinkSend().getFailureOperation());
				// 送信失敗時の終了状態
				jobMst.setFailureEndStatus(info.getJobLinkSend().getFailureEndStatus());
				// ジョブ連携メッセージID
				jobMst.setJoblinkMessageId(info.getJobLinkSend().getJoblinkMessageId());
				// 重要度
				jobMst.setPriority(info.getJobLinkSend().getPriority());
				// メッセージ
				jobMst.setMessage(info.getJobLinkSend().getMessage());
				// 拡張情報
				if(info.getJobLinkSend().getJobLinkExpList() != null) {
					for(JobLinkExpInfo expInfo : info.getJobLinkSend().getJobLinkExpList()){
						if(expInfo != null){
							// インスタンス生成
							JobLinkJobExpMstEntity expEntity = new JobLinkJobExpMstEntity(jobMst, expInfo.getKey());
							// 重複チェック
							jtm.checkEntityExists(JobLinkJobExpMstEntity.class, expEntity.getId());
							expEntity.setValue(expInfo.getValue());
							// 登録
							em.persist(expEntity);
							expEntity.relateToJobMstEntity(jobMst);
						}
					}
				}
				// 終了値 - 送信成功
				jobMst.setSuccessEndValue(info.getJobLinkSend().getSuccessEndValue());
				// 終了値 - 送信失敗
				jobMst.setFailureEndValue(info.getJobLinkSend().getFailureEndValue());
				// ジョブ連携送信ID
				jobMst.setJoblinkSendSettingId(info.getJobLinkSend().getJoblinkSendSettingId());
				// 停止種別
				jobMst.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
			}

			// ジョブ連携待機ジョブ作成
			if (info.getJobLinkRcv() != null) {
				jobMst.setProcessMode(info.getJobLinkRcv().getProcessingMethod());
				// スコープ
				jobMst.setFacilityId(info.getJobLinkRcv().getFacilityID());
				// 過去に発生したジョブ連携メッセージを確認する
				jobMst.setPastFlg(info.getJobLinkRcv().getPastFlg());
				// 過去に発生したジョブ連携メッセージを確認する - 対象期間
				jobMst.setPastMin(info.getJobLinkRcv().getPastMin());
				// ジョブ連携メッセージID
				jobMst.setJoblinkMessageId(info.getJobLinkRcv().getJoblinkMessageId());
				// 重要度 - 情報
				jobMst.setInfoValidFlg(info.getJobLinkRcv().getInfoValidFlg());
				// 重要度 - 警告
				jobMst.setWarnValidFlg(info.getJobLinkRcv().getWarnValidFlg());
				// 重要度 - 危険
				jobMst.setCriticalValidFlg(info.getJobLinkRcv().getCriticalValidFlg());
				// 重要度 - 不明
				jobMst.setUnknownValidFlg(info.getJobLinkRcv().getUnknownValidFlg());
				// アプリケーションフラグ
				jobMst.setApplicationFlg(info.getJobLinkRcv().getApplicationFlg());
				// アプリケーション
				jobMst.setApplication(info.getJobLinkRcv().getApplication());
				// 監視詳細フラグ
				jobMst.setMonitorDetailIdFlg(info.getJobLinkRcv().getMonitorDetailIdFlg());
				// 監視詳細
				jobMst.setMonitorDetailId(info.getJobLinkRcv().getMonitorDetailId());
				// メッセージフラグ
				jobMst.setMessageFlg(info.getJobLinkRcv().getMessageFlg());
				// メッセージ
				jobMst.setMessage(info.getJobLinkRcv().getMessage());
				// 拡張情報フラグ
				jobMst.setExpFlg(info.getJobLinkRcv().getExpFlg());
				// 拡張情報
				if(info.getJobLinkRcv().getJobLinkExpList() != null) {
					for(JobLinkExpInfo expInfo : info.getJobLinkRcv().getJobLinkExpList()){
						if(expInfo != null){
							// インスタンス生成
							JobLinkJobExpMstEntity expEntity = new JobLinkJobExpMstEntity(jobMst, expInfo.getKey());
							// 重複チェック
							jtm.checkEntityExists(JobLinkJobExpMstEntity.class, expEntity.getId());
							expEntity.setValue(expInfo.getValue());
							// 登録
							em.persist(expEntity);
							expEntity.relateToJobMstEntity(jobMst);
						}
					}
				}
				// 終了値 - メッセージが得られたら常にフラグ
				jobMst.setMonitorAllEndValueFlg(info.getJobLinkRcv().getMonitorAllEndValueFlg());
				// 終了値 - メッセージが得られたら常に
				jobMst.setMonitorAllEndValue(info.getJobLinkRcv().getMonitorAllEndValue());
				// 終了値 - 情報
				jobMst.setMonitorInfoEndValue(info.getJobLinkRcv().getMonitorInfoEndValue());
				// 終了値 - 警告
				jobMst.setMonitorWarnEndValue(info.getJobLinkRcv().getMonitorWarnEndValue());
				// 終了値 - 危険
				jobMst.setMonitorCriticalEndValue(info.getJobLinkRcv().getMonitorCriticalEndValue());
				// 終了値 - 不明
				jobMst.setMonitorUnknownEndValue(info.getJobLinkRcv().getMonitorUnknownEndValue());
				// メッセージが得られない場合に終了する
				jobMst.setFailureEndFlg(info.getJobLinkRcv().getFailureEndFlg());
				// メッセージが得られない場合に終了する - タイムアウト
				jobMst.setMonitorWaitTime(info.getJobLinkRcv().getMonitorWaitTime());
				// メッセージが得られない場合に終了する - 終了値
				jobMst.setMonitorWaitEndValue(info.getJobLinkRcv().getMonitorWaitEndValue());
				// メッセージ情報の引継ぎ
				if(info.getJobLinkRcv().getJobLinkInheritList() != null) {
					for(JobLinkInheritInfo inheritInfo : info.getJobLinkRcv().getJobLinkInheritList()){
						if(inheritInfo != null){
							// インスタンス生成
							JobLinkInheritMstEntity inheritEntity = new JobLinkInheritMstEntity(
									jobMst, inheritInfo.getParamId());
							// 重複チェック
							jtm.checkEntityExists(JobLinkInheritMstEntity.class, inheritEntity.getId());
							inheritEntity.setKeyInfo(inheritInfo.getKeyInfo());
							inheritEntity.setExpKey(inheritInfo.getExpKey());
							// 登録
							em.persist(inheritEntity);
							inheritEntity.relateToJobMstEntity(jobMst);
						}
					}
				}
				// 停止種別
				jobMst.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
			}

			// リソース制御ジョブ作成
			if (info.getResource() != null) {
				jobMst.setResourceCloudScopeId(info.getResource().getResourceCloudScopeId());
				jobMst.setResourceLocationId(info.getResource().getResourceLocationId());
				jobMst.setResourceType(info.getResource().getResourceType());
				jobMst.setResourceAction(info.getResource().getResourceAction());
				jobMst.setResourceTargetId(info.getResource().getResourceTargetId());
				jobMst.setResourceStatusConfirmTime(info.getResource().getResourceStatusConfirmTime());
				jobMst.setResourceStatusConfirmInterval(info.getResource().getResourceStatusConfirmInterval());
				jobMst.setResourceAttachNode(info.getResource().getResourceAttachNode());
				jobMst.setResourceAttachDevice(info.getResource().getResourceAttachDevice());
				// 通知先はfacilityIdにセットする
				jobMst.setFacilityId(info.getResource().getResourceNotifyScope());
				jobMst.setResourceSuccessValue(info.getResource().getResourceSuccessValue());
				jobMst.setResourceFailureValue(info.getResource().getResourceFailureValue());
				// エラー時は終了にする。
				jobMst.setMessageRetry(0);
				jobMst.setMessageRetryEndFlg(true);
				jobMst.setMessageRetryEndValue(info.getResource().getResourceFailureValue());
			}

			// RPAシナリオジョブ作成
			if (info.getRpa() != null) {
				jobMst.setRpaJobType(info.getRpa().getRpaJobType());
				// 直接実行
				jobMst.setFacilityId(info.getRpa().getFacilityID());
				jobMst.setRpaToolId(info.getRpa().getRpaToolId());
				jobMst.setRpaExeFilepath(info.getRpa().getRpaExeFilepath());
				jobMst.setRpaScenarioFilepath(info.getRpa().getRpaScenarioFilepath());
				jobMst.setRpaLogDirectory(info.getRpa().getRpaLogDirectory());
				jobMst.setRpaLogFileName(info.getRpa().getRpaLogFileName());
				jobMst.setRpaLogEncoding(info.getRpa().getRpaLogEncoding());
				jobMst.setRpaLogReturnCode(info.getRpa().getRpaLogReturnCode());
				jobMst.setRpaLogPatternHead(info.getRpa().getRpaLogPatternHead());
				jobMst.setRpaLogPatternTail(info.getRpa().getRpaLogPatternTail());
				jobMst.setRpaLogMaxBytes(info.getRpa().getRpaLogMaxBytes());
				jobMst.setRpaDefaultEndValue(info.getRpa().getRpaDefaultEndValue());
				jobMst.setRpaLoginFlg(info.getRpa().getRpaLoginFlg());
				jobMst.setRpaLoginUserId(info.getRpa().getRpaLoginUserId());
				jobMst.setRpaLoginPassword(info.getRpa().getRpaLoginPassword());
				jobMst.setRpaLoginRetry(info.getRpa().getRpaLoginRetry());
				jobMst.setRpaLoginEndValue(info.getRpa().getRpaLoginEndValue());
				jobMst.setRpaLoginResolution(info.getRpa().getRpaLoginResolution());
				jobMst.setRpaLogoutFlg(info.getRpa().getRpaLogoutFlg());
				jobMst.setRpaScreenshotEndDelayFlg(info.getRpa().getRpaScreenshotEndDelayFlg());
				jobMst.setRpaScreenshotEndValueFlg(info.getRpa().getRpaScreenshotEndValueFlg());
				jobMst.setRpaScreenshotEndValue(info.getRpa().getRpaScreenshotEndValue());
				jobMst.setRpaScreenshotEndValueCondition(info.getRpa().getRpaScreenshotEndValueCondition());
				jobMst.setRpaNotLoginNotify(info.getRpa().getRpaNotLoginNotify());
				jobMst.setRpaNotLoginNotifyPriority(info.getRpa().getRpaNotLoginNotifyPriority());
				jobMst.setRpaNotLoginEndValue(info.getRpa().getRpaNotLoginEndValue());
				jobMst.setRpaAlreadyRunningNotify(info.getRpa().getRpaAlreadyRunningNotify());
				jobMst.setRpaAlreadyRunningNotifyPriority(info.getRpa().getRpaAlreadyRunningNotifyPriority());
				jobMst.setRpaAlreadyRunningEndValue(info.getRpa().getRpaAlreadyRunningEndValue());
				jobMst.setRpaAbnormalExitNotify(info.getRpa().getRpaAbnormalExitNotify());
				jobMst.setRpaAbnormalExitNotifyPriority(info.getRpa().getRpaAbnormalExitNotifyPriority());
				jobMst.setRpaAbnormalExitEndValue(info.getRpa().getRpaAbnormalExitEndValue());
				// 実行オプション
				for (RpaJobOptionInfo optionInfo : info.getRpa().getRpaJobOptionInfos()) {
					JobRpaOptionMstEntity rpaOptionMst = new JobRpaOptionMstEntity(new JobRpaOptionMstEntityPK(info.getJobunitId(), info.getId(),
							optionInfo.getOrderNo()));
					rpaOptionMst.setOption(optionInfo.getOption());
					rpaOptionMst.setDescription(optionInfo.getDescription());
					em.persist(rpaOptionMst);
					rpaOptionMst.relateToJobMstEntity(jobMst);
				}
				// 終了値判定条件
				for (RpaJobEndValueConditionInfo endValueInfo : info.getRpa().getRpaJobEndValueConditionInfos()) {
					JobRpaEndValueConditionMstEntity rpaEndValueMst =  new JobRpaEndValueConditionMstEntity(new JobRpaEndValueConditionMstEntityPK(info.getJobunitId(), info.getId(),
							endValueInfo.getOrderNo()));
					rpaEndValueMst.setConditionType(endValueInfo.getConditionType());
					rpaEndValueMst.setPattern(endValueInfo.getPattern());
					rpaEndValueMst.setCaseSensitivityFlg(endValueInfo.getCaseSensitivityFlg());
					rpaEndValueMst.setProcessType(endValueInfo.getProcessType());
					rpaEndValueMst.setReturnCode(endValueInfo.getReturnCode());
					rpaEndValueMst.setReturnCodeCondition(endValueInfo.getReturnCodeCondition());
					rpaEndValueMst.setUseCommandReturnCodeFlg(endValueInfo.getUseCommandReturnCodeFlg());
					rpaEndValueMst.setEndValue(endValueInfo.getEndValue());
					rpaEndValueMst.setDescription(endValueInfo.getDescription());
					em.persist(rpaEndValueMst);
					rpaEndValueMst.relateToJobMstEntity(jobMst);
				}
				// 間接実行
				jobMst.setRpaScopeId(info.getRpa().getRpaScopeId());
				jobMst.setRpaRunType(info.getRpa().getRpaRunType());
				jobMst.setRpaScenarioParam(info.getRpa().getRpaScenarioParam());
				jobMst.setRpaStopType(info.getRpa().getRpaStopType());
				jobMst.setRpaStopMode(info.getRpa().getRpaStopMode());
				jobMst.setRpaRunConnectTimeout(info.getRpa().getRpaRunConnectTimeout());
				jobMst.setRpaRunRequestTimeout(info.getRpa().getRpaRunRequestTimeout());
				jobMst.setRpaRunEndFlg(info.getRpa().getRpaRunEndFlg());
				jobMst.setRpaRunRetry(info.getRpa().getRpaRunRetry());
				jobMst.setRpaRunEndValue(info.getRpa().getRpaRunEndValue());
				jobMst.setRpaCheckConnectTimeout(info.getRpa().getRpaCheckConnectTimeout());
				jobMst.setRpaCheckRequestTimeout(info.getRpa().getRpaCheckRequestTimeout());
				jobMst.setRpaCheckEndFlg(info.getRpa().getRpaCheckEndFlg());
				jobMst.setRpaCheckRetry(info.getRpa().getRpaCheckRetry());
				jobMst.setRpaCheckEndValue(info.getRpa().getRpaCheckEndValue());
				// 起動パラメータ
				for (RpaJobRunParamInfo runParamInfo : info.getRpa().getRpaJobRunParamInfos()) {
					JobRpaRunParamMstEntity rpaRunParamMst = new JobRpaRunParamMstEntity(new JobRpaRunParamMstEntityPK(info.getJobunitId(), info.getId(),
							runParamInfo.getParamId()));
					rpaRunParamMst.setParamValue(runParamInfo.getParamValue());
					// 設定値以外は起動パラメータマスタから取得する
					try {
						RpaManagementToolRunParamMst runParamMst = com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolRunParamMstPK(runParamInfo.getParamId());
						rpaRunParamMst.setParamName(runParamMst.getParamName());
						rpaRunParamMst.setParamType(runParamMst.getParamType());
						rpaRunParamMst.setArrayFlg(runParamMst.getArrayFlg());
					} catch (RpaManagementToolRunParamMasterNotFound e) {
						m_log.warn("createJobMasterData() : RpaManagementToolRunParamMst not found, paramId=" + runParamInfo.getParamId(), e);
						throw new InvalidSetting(e);
					}
					em.persist(rpaRunParamMst);
					rpaRunParamMst.relateToJobMstEntity(jobMst);
				}
				// 終了値判定条件
				for (RpaJobCheckEndValueInfo checkEndValueInfo : info.getRpa().getRpaJobCheckEndValueInfos()) {
					JobRpaCheckEndValueMstEntity checkEndValueMst = new JobRpaCheckEndValueMstEntity(new JobRpaCheckEndValueMstEntityPK(info.getJobunitId(), info.getId(),
							checkEndValueInfo.getEndStatusId()));
					checkEndValueMst.setEndValue(checkEndValueInfo.getEndValue());
					// 終了値以外は終了状態マスタから取得する
					try {
						RpaManagementToolEndStatusMst endStatusMst = com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolEndStatusMstPK(checkEndValueInfo.getEndStatusId());
						checkEndValueMst.setEndStatus(endStatusMst.getEndStatus());
					} catch (RpaManagementToolEndStatusMasterNotFound e) {
						m_log.warn("createJobMasterData() : RpaManagementToolEndStatusMst not found, endStatusId=" + checkEndValueInfo.getEndStatusId(), e);
						throw new InvalidSetting(e);
					}
					em.persist(checkEndValueMst);
					checkEndValueMst.relateToJobMstEntity(jobMst);
				}
				// 直接実行・間接実行共通
				if (info.getRpa().getRpaJobType() == RpaJobTypeConstant.DIRECT) {
					jobMst.setProcessMode(info.getRpa().getProcessingMethod());
				} else {
					jobMst.setProcessMode(ProcessingMethodConstant.TYPE_ALL_NODE);
				}
				jobMst.setMessageRetry(info.getRpa().getMessageRetry());
				jobMst.setMessageRetryEndFlg(info.getRpa().getMessageRetryEndFlg());
				jobMst.setMessageRetryEndValue(info.getRpa().getMessageRetryEndValue());
				jobMst.setCommandRetryFlg(info.getRpa().getCommandRetryFlg());
				jobMst.setCommandRetry(info.getRpa().getCommandRetry());
				jobMst.setCommandRetryEndStatus(info.getRpa().getCommandRetryEndStatus());
				jobMst.setStopType(CommandStopTypeConstant.DESTROY_PROCESS);
			}
			
			//通知メッセージを取得
			jobMst.setBeginPriority(info.getBeginPriority());
			jobMst.setNormalPriority(info.getNormalPriority());
			jobMst.setWarnPriority(info.getWarnPriority());
			jobMst.setAbnormalPriority(info.getAbnormalPriority());

			//notifyGroupIdを更新
			String notifyGroupId = NotifyGroupIdGenerator.generate(info);
			jobMst.setNotifyGroupId(notifyGroupId);

			if (info.getNotifyRelationInfos() != null) {
				for (NotifyRelationInfo notifyRelationInfo : info.getNotifyRelationInfos()) {
					notifyRelationInfo.setNotifyGroupId(notifyGroupId);
					notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.JOB_MASTER.name());
				}
			}
			new NotifyControllerBean().addNotifyRelation(info.getNotifyRelationInfos(), jobMst.getOwnerRoleId()); // jobMstに設定済のオーナーロールIDを使用する

			//終了状態を取得
			if(info.getEndStatus() != null){
				for (JobEndStatusInfo endInfo : info.getEndStatus()) {
					if(endInfo != null){
						if (endInfo.getType() == EndStatusConstant.TYPE_NORMAL) {
							// 正常
							jobMst.setNormalEndValue(endInfo.getValue());
							jobMst.setNormalEndValueFrom(endInfo.getStartRangeValue());
							jobMst.setNormalEndValueTo(endInfo.getEndRangeValue());
						} else if (endInfo.getType() == EndStatusConstant.TYPE_WARNING) {
							// 警告
							jobMst.setWarnEndValue(endInfo.getValue());
							jobMst.setWarnEndValueFrom(endInfo.getStartRangeValue());
							jobMst.setWarnEndValueTo(endInfo.getEndRangeValue());
						} else if (endInfo.getType() == EndStatusConstant.TYPE_ABNORMAL) {
							// 異常
							jobMst.setAbnormalEndValue(endInfo.getValue());
							jobMst.setAbnormalEndValueFrom(endInfo.getStartRangeValue());
							jobMst.setAbnormalEndValueTo(endInfo.getEndRangeValue());
						}
					}
				}
			}

			//パラメータを取得
			if(info.getParam() != null){
				for(JobParameterInfo paramInfo : info.getParam()){
					if(paramInfo != null){
						// インスタンス生成
						JobParamMstEntity jobParamMstEntity = new JobParamMstEntity(jobMst, paramInfo.getParamId());
						// 重複チェック
						jtm.checkEntityExists(JobParamMstEntity.class, jobParamMstEntity.getId());
						jobParamMstEntity.setDescription(paramInfo.getDescription());
						jobParamMstEntity.setParamType(paramInfo.getType());
						jobParamMstEntity.setValue(paramInfo.getValue());
						// 登録
						em.persist(jobParamMstEntity);
						jobParamMstEntity.relateToJobMstEntity(jobMst);
					}
				}
			}

			//参照ジョブ/参照ジョブネットを設定
			if(info.getType() == JobConstant.TYPE_REFERJOB || info.getType() == JobConstant.TYPE_REFERJOBNET){
				if(info.getReferJobUnitId() != null){
					jobMst.setReferJobUnitId(info.getReferJobUnitId());
				}
				if(info.getReferJobId() != null){
					jobMst.setReferJobId(info.getReferJobId());
				}
				if(info.getReferJobSelectType() != null){
					jobMst.setReferJobSelectType(info.getReferJobSelectType());
				}
			}
			//承認ジョブを設定
			if(info.getType() == JobConstant.TYPE_APPROVALJOB){
				if(info.getApprovalReqRoleId() != null){
					jobMst.setApprovalReqRoleId(info.getApprovalReqRoleId());
				}
				if(info.getApprovalReqUserId() != null){
					jobMst.setApprovalReqUserId(info.getApprovalReqUserId());
				}
				if(info.getApprovalReqSentence() != null){
					jobMst.setApprovalReqSentence(info.getApprovalReqSentence());
				}
				if(info.getApprovalReqMailTitle() != null){
					jobMst.setApprovalReqMailTitle(info.getApprovalReqMailTitle());
				}
				if(info.getApprovalReqMailBody() != null){
					jobMst.setApprovalReqMailBody(info.getApprovalReqMailBody());
				}
				jobMst.setUseApprovalReqSentence(info.isUseApprovalReqSentence());
			}

		} catch (HinemosUnknown e) {
			throw e;
		} catch (EntityExistsException e) {
			JobMasterDuplicate jmd = new JobMasterDuplicate(e.getMessage(), e);
			m_log.info("createJobMasterData() : "
					+ jmd.getClass().getSimpleName() + ", " + jmd.getMessage());
			throw jmd;
		}
	}
	
	/**
	 * RPAシナリオジョブのスクリーンショットを登録します。
	 * @param info
	 */
	public void registerJobRpaScreenshot(RpaJobScreenshot info) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JobRpaScreenshotEntity jobRpaScreenshotEntity = new JobRpaScreenshotEntity(
					new JobRpaScreenshotEntityPK(info.getSessionId(), info.getJobunitId(), info.getJobId(),
						info.getFacilityId(), info.getOutputDate()));
			jobRpaScreenshotEntity.setFiledata(info.getFiledata());
			switch(info.getTriggerType()) {
				case(RpaScreenshotTriggerTypeConstant.END_DELAY):
					jobRpaScreenshotEntity.setDescription(MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_BY_END_DELAY.getMessage());
					break;
				case(RpaScreenshotTriggerTypeConstant.END_VALUE):
					jobRpaScreenshotEntity.setDescription(MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_BY_END_VALUE.getMessage());
					break;
				case(RpaScreenshotTriggerTypeConstant.MANUAL):
				default:
					jobRpaScreenshotEntity.setDescription(MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_BY_MANUAL.getMessage());
					break;
			}
			em.persist(jobRpaScreenshotEntity);
		}
	}
}
