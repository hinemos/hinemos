/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.bean.EndStatusConstant;
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
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobCommandParam;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobNextJobOrderInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobParameterInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.model.JobCommandParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobStartParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderMstEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobMstEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.util.HinemosTime;

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
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 *
	 */
	public void deleteJobunit(String jobunitId, String userId) throws HinemosUnknown, JobMasterNotFound, JobInvalid, ObjectPrivilege_InvalidRole {
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
			} else {
				// JOBUNIT以外は、JOBUNITのowner_role_idを設定する
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
				jobMst.setJobRetryEndStatus(waitRule.getJobRetryEndStatus());
				jobMst.setQueueFlg(waitRule.getQueueFlg());
				jobMst.setQueueId(waitRule.getQueueId());
				if(waitRule.getObject() != null){
					for(JobObjectInfo objectInfo : waitRule.getObject()) {
						if(objectInfo != null){
							if(objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getValue = " + objectInfo.getValue());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								// インスタンス生成
								JobStartJobMstEntity jobStartJobMstEntity
								= new JobStartJobMstEntity(
										jobMst,
										waitJobunitId,
										objectInfo.getJobId(),
										JudgmentObjectConstant.TYPE_JOB_END_STATUS,
										objectInfo.getValue());
								em.persist(jobStartJobMstEntity);
								jobStartJobMstEntity.relateToJobMstEntity(jobMst);
								jobStartJobMstEntity.setTargetJobDescription(objectInfo.getDescription());
								// 重複チェック
								jtm.checkEntityExists(JobStartJobMstEntity.class, jobStartJobMstEntity.getId());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getValue = " + objectInfo.getValue());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								// インスタンス生成
								JobStartJobMstEntity jobStartJobMstEntity
								= new JobStartJobMstEntity(
										jobMst,
										waitJobunitId,
										objectInfo.getJobId(),
										JudgmentObjectConstant.TYPE_JOB_END_VALUE,
										objectInfo.getValue());
								em.persist(jobStartJobMstEntity);
								jobStartJobMstEntity.relateToJobMstEntity(jobMst);
								jobStartJobMstEntity.setTargetJobDescription(objectInfo.getDescription());
								// 重複チェック
								jtm.checkEntityExists(JobStartJobMstEntity.class, jobStartJobMstEntity.getId());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_TIME) {
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getTime = " + objectInfo.getTime());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								jobMst.setStartTime(objectInfo.getTime());
								jobMst.setStartTimeDescription(objectInfo.getDescription());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getStartMinute = " + objectInfo.getStartMinute());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								jobMst.setStartMinute(objectInfo.getStartMinute());
								jobMst.setStartMinuteDescription(objectInfo.getDescription());
							}
							else if (objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getDecisionValue01 = " + objectInfo.getDecisionValue01());
								m_log.debug("objectInfo.getDecisionCondition = " + objectInfo.getDecisionCondition());
								m_log.debug("objectInfo.getDecisionValue02 = " + objectInfo.getDecisionValue02());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								JobStartParamMstEntity jobStartParamMstEntity = new JobStartParamMstEntity(
										jobMst,
										objectInfo.getDecisionValue01(),
										objectInfo.getDecisionValue02(),
										objectInfo.getType(),
										objectInfo.getDecisionCondition());
								// 重複チェック
								jtm.checkEntityExists(JobStartParamMstEntity.class, jobStartParamMstEntity.getId());
								jobStartParamMstEntity.setDecisionDescription(objectInfo.getDescription());
								em.persist(jobStartParamMstEntity);
								jobStartParamMstEntity.relateToJobMstEntity(jobMst);
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getValue = " + objectInfo.getValue());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								m_log.debug("objectInfo.getCrossSessionRange = " + objectInfo.getCrossSessionRange());
								// インスタンス生成
								JobStartJobMstEntity jobStartJobMstEntity
								= new JobStartJobMstEntity(
										jobMst,
										waitJobunitId,
										objectInfo.getJobId(),
										JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS,
										objectInfo.getValue());
								em.persist(jobStartJobMstEntity);
								jobStartJobMstEntity.relateToJobMstEntity(jobMst);
								jobStartJobMstEntity.setTargetJobDescription(objectInfo.getDescription());
								jobStartJobMstEntity.setTargetJobCrossSessionRange(objectInfo.getCrossSessionRange());
								// 重複チェック
								jtm.checkEntityExists(JobStartJobMstEntity.class, jobStartJobMstEntity.getId());
							}
							else if(objectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE){
								m_log.debug("objectInfo.getType = " + objectInfo.getType());
								m_log.debug("objectInfo.getJobId = " + objectInfo.getJobId());
								m_log.debug("objectInfo.getValue = " + objectInfo.getValue());
								m_log.debug("objectInfo.getDescription = " + objectInfo.getDescription());
								m_log.debug("objectInfo.getCrossSessionRange = " + objectInfo.getCrossSessionRange());
								// インスタンス生成
								JobStartJobMstEntity jobStartJobMstEntity
								= new JobStartJobMstEntity(
										jobMst,
										waitJobunitId,
										objectInfo.getJobId(),
										JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE,
										objectInfo.getValue());
								em.persist(jobStartJobMstEntity);
								jobStartJobMstEntity.relateToJobMstEntity(jobMst);
								jobStartJobMstEntity.setTargetJobDescription(objectInfo.getDescription());
								jobStartJobMstEntity.setTargetJobCrossSessionRange(objectInfo.getCrossSessionRange());
								// 重複チェック
								jtm.checkEntityExists(JobStartJobMstEntity.class, jobStartJobMstEntity.getId());
							}
						}
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
				jobMst.setCommandRetryFlg(info.getFile().isCommandRetryFlg());
				jobMst.setCommandRetry(info.getFile().getCommandRetry());
				jobMst.setCommandRetryEndStatus(info.getFile().getCommandRetryEndStatus());
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
				// エラー時は終了にする（戻り値は不明と同じ。）
				jobMst.setMessageRetry(0);
				jobMst.setMessageRetryEndFlg(true);
				jobMst.setMessageRetryEndValue(info.getMonitor().getMonitorUnknownEndValue());
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
				}
			}
			new NotifyControllerBean().addNotifyRelation(info.getNotifyRelationInfos());

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
}
