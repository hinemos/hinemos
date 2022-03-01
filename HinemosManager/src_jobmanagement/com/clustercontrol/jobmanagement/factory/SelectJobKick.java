/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.scheduler.QuartzUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.DbmsSchedulerNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JobKickFilterInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkRcv;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParam;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamDetail;
import com.clustercontrol.jobmanagement.bean.JobPlan;
import com.clustercontrol.jobmanagement.bean.JobPlanFilter;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.QuartzConstant;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobkickExpInfoEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamDetailEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.plugin.model.DbmsSchedulerEntity;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;

/**
 * 
 * 実行契機一覧情報[スケジュール＆ファイルチェック]を検索するクラスです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class SelectJobKick {
	private static Log m_log = LogFactory.getLog( SelectJobKick.class );
	
	/**
	 * ジョブ[スケジュール予定]ビューの表示イベント数。<BR>
	 * getPlanListで件数0が指定された場合にこの件数で返す。
	 */
	private final static int PLAN_MAX_DISPLAY_NUMBER = 100;

	/**
	 * jobkickIdのジョブスケジュールを取得します
	 * 
	 * @param jobkickId
	 * @param jobkickType
	 * @return
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public JobKick getJobKick(String jobkickId, Integer jobkickType) throws JobMasterNotFound, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			JobKick jobKick = null;

			m_log.debug("getJobKick() jobkickId = " + jobkickId + ", jobkickType = " + jobkickType);

			JobKickEntity jobKickEntity = em.find(JobKickEntity.class, jobkickId, ObjectPrivilegeMode.READ);
			if (jobKickEntity == null || (jobkickType != null && jobKickEntity.getJobkickType().intValue() != jobkickType.intValue())) {
				JobMasterNotFound je = new JobMasterNotFound("JobKickEntity.findByPrimaryKey"
						+ ", jobkickId = " + jobkickId
						+ ", jobkickType = " + jobkickType);
				m_log.info("getJobKick() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
			
			if (jobKickEntity.getJobkickType() == JobKickConstant.TYPE_SCHEDULE) {
				jobKick = createJobScheduleInfo(jobKickEntity);
			} else if (jobKickEntity.getJobkickType() == JobKickConstant.TYPE_FILECHECK) {
				jobKick = createJobFileCheckInfo(jobKickEntity);
			} else if (jobKickEntity.getJobkickType() == JobKickConstant.TYPE_MANUAL) {
				jobKick = createJobManual(jobKickEntity);
			} else if (jobKickEntity.getJobkickType() == JobKickConstant.TYPE_JOBLINKRCV) {
				jobKick = createJoblinkRcvInfo(jobKickEntity);
			} else {
				// 処理なし
			}
			return jobKick;
		}
	}

	/**
	 * スケジュール一覧情報を取得します。
	 * 
	 * @return スケジュール一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<JobKick> getJobKickList() throws JobMasterNotFound, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			m_log.debug("getJobKickList()");

			ArrayList<JobKick> list = new ArrayList<JobKick>();
			//実行契機情報を取得する
			Collection<JobKickEntity> jobKickList;
			jobKickList = em.createNamedQuery("JobKickEntity.findAll", JobKickEntity.class).getResultList();
			if (jobKickList == null) {
				JobMasterNotFound je = new JobMasterNotFound("JobKickEntity.findAll");
				m_log.info("getJobKickList() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
			for(JobKickEntity jobKickBean : jobKickList){
				if (jobKickBean.getJobkickType() == JobKickConstant.TYPE_SCHEDULE) {
					// スケジュールを取得する
					list.add(createJobScheduleInfo(jobKickBean));
				} else if (jobKickBean.getJobkickType() == JobKickConstant.TYPE_FILECHECK) {
					// ファイルチェックを取得する
					list.add(createJobFileCheckInfo(jobKickBean));
				} else if (jobKickBean.getJobkickType() == JobKickConstant.TYPE_MANUAL) {
					// マニュアル実行契機の場合は追加の取得は不要。
					list.add(createJobManual(jobKickBean));
				} else if (jobKickBean.getJobkickType() == JobKickConstant.TYPE_JOBLINKRCV) {
					// ジョブ連携受信実行契機の場合は追加の取得は不要。
					list.add(createJoblinkRcvInfo(jobKickBean));
				}
			}
			return list;
		}
	}

	/**
	 * スケジュール一覧情報を取得します。
	 * 
	 * @return スケジュール一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<JobKick> getJobKickList(JobKickFilterInfo condition) throws JobMasterNotFound, InvalidRole, HinemosUnknown {

		m_log.debug("getJobKickList() condition");
		if (m_log.isDebugEnabled()) {
			if (condition != null) {
				m_log.debug("getJobKickList() " +
					"jobkickId = " + condition.getJobkickId() +
					", jobkickName = " + condition.getJobkickName() +
					", jobkickType = " + condition.getJobkickType() +
					", jobunitId = " + condition.getJobunitId() +
					", jobId = " + condition.getJobId() +
					", calendarId = " + condition.getCalendarId() +
					", validFlg = " + condition.getValidFlg() +
					", regUser = " + condition.getRegUser() +
					", regFromDate = " + condition.getRegFromDate() +
					", regToDate = " + condition.getRegToDate() +
					", updateUser = " + condition.getUpdateUser() +
					", updateFromDate = " + condition.getUpdateFromDate() +
					", updateToDate = " + condition.getUpdateToDate() +
					", ownerRoleId = " + condition.getOwnerRoleId());
			}
		}

		ArrayList<JobKick> filterList = new ArrayList<JobKick>();
		// 条件未設定の場合は空のリストを返却する
		if (condition == null) {
			m_log.debug("getJobKickList() condition is null");
			return filterList;
		}
		List<JobKickEntity> entityList = QueryUtil.getJobKickEntityFindByFilter(
				condition.getJobkickId(),
				condition.getJobkickName(),
				condition.getJobkickType(),
				condition.getJobunitId(),
				condition.getJobId(),
				condition.getCalendarId(),
				condition.getValidFlg(),
				condition.getRegUser(),
				condition.getRegFromDate(),
				condition.getRegToDate(),
				condition.getUpdateUser(),
				condition.getUpdateFromDate(),
				condition.getUpdateToDate(),
				condition.getOwnerRoleId());

		if (entityList == null) {
			JobMasterNotFound je = new JobMasterNotFound("JobKickEntity.findByFilter");
			m_log.info("getJobKickList() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			throw je;
		}

		for(JobKickEntity entity : entityList){
			if (entity.getJobkickType() == JobKickConstant.TYPE_SCHEDULE) {
				// スケジュールを取得する
				filterList.add(createJobScheduleInfo(entity));
			} else if (entity.getJobkickType() == JobKickConstant.TYPE_FILECHECK) {
				// ファイルチェックを取得する
				filterList.add(createJobFileCheckInfo(entity));
			} else if (entity.getJobkickType() == JobKickConstant.TYPE_MANUAL) {
				// マニュアル実行契機の場合は追加の取得は不要。
				filterList.add(createJobManual(entity));
			} else if (entity.getJobkickType() == JobKickConstant.TYPE_JOBLINKRCV) {
				// ジョブ連携受信実行契機を取得する
				filterList.add(createJoblinkRcvInfo(entity));
			}
		}
		return filterList;
	}
	/**
	 * スケジュール[スケジュール予定]一覧情報を取得します。
	 * ・カレンダが設定されていた場合、カレンダの日程を考慮したスケジュールを取得します。
	 * ・フィルタ処理が有効の場合、フィルタの内容を考慮したスケジュールを取得します。
	 * 
	 * @return スケジュール一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws DbmsSchedulerNotFound 
	 */
	public ArrayList<JobPlan> getPlanList(String userId, JobPlanFilter filter,int plans) throws JobMasterNotFound, InvalidSetting, InvalidRole, HinemosUnknown, DbmsSchedulerNotFound {

		m_log.debug("getPlanList()");

		Collection<JobKickEntity> jobKickList = QueryUtil.getJobKickEntityFindByJobKickType(JobKickConstant.TYPE_SCHEDULE);
		if (jobKickList == null) {
			JobMasterNotFound je = new JobMasterNotFound("JobKickEntity.findByJobKickType jobkickType = " + JobKickConstant.TYPE_SCHEDULE);
			m_log.info("getPlanList() : "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			throw je;
		}

		if (plans <= 0) {
			plans = PLAN_MAX_DISPLAY_NUMBER;
		}

		//ジョブ[スケジュール予定]一覧表示に必要なものだけ抽出
		ArrayList<JobPlan> planList = new ArrayList<JobPlan>();
		for(JobKickEntity jobKickBean : jobKickList){
			JobSchedule js = createJobScheduleInfo(jobKickBean);
			//スケジュールが有効なら表示 0:無効 1:有効
			if (!js.isValid().booleanValue()) {
				continue;
			}

			JobPlanSchedule planInfo;
			if (js.getScheduleType() == ScheduleConstant.TYPE_INTERVAL) {

				// 一覧表示の開始日時（フィルタの開始時間が設定されていたらこれを基準に表示）
				long startTime = HinemosTime.currentTimeMillis();
				if(filter != null && filter.getFromDate() != null){
					startTime = filter.getFromDate();
				}

				// 繰り返し情報は cc_dbms_schedulerテーブルから取得する
				DbmsSchedulerEntity entity = com.clustercontrol.plugin.util.QueryUtil.getDbmsSchedulerPK(js.getId(),
						QuartzConstant.GROUP_NAME, ObjectPrivilegeMode.READ);

				planInfo = new JobPlanSchedule(entity.getNextFireTime(), entity.getRepeatInterval(), startTime, js.getCalendarId());

			} else {
				String str = QuartzUtil.getCronString(js.getScheduleType(),
						js.getWeek(),js.getHour(),js.getMinute(),
						js.getFromXminutes(),js.getEveryXminutes());
				m_log.debug("Cron =" + str);
				//表示開始日時のデフォルトはマネージャへアクセスしたときの日時
				Long startTime = HinemosTime.currentTimeMillis();
				//フィルタの開始時間が設定されていたらこれを基準に表示
				if(filter != null && filter.getFromDate() != null){
					startTime = filter.getFromDate();
				}
				planInfo = new JobPlanSchedule(str, startTime, js.getCalendarId());
			}

			//表示件数分繰り返す
			int counter = 0;
			while (counter < plans) {
				Long date = planInfo.getNextPlan();
				if (date == null) {
					break;
				}
				//フィルタ処理
				boolean filterFlg = true;
				if(filter != null){
					filterFlg = filter.filterAction(js.getId(), date);
				}
				//フィルタ処理を通過、または、フィルタ未設定の場合
				if(filterFlg){
					JobPlan plan = new JobPlan();
					plan.setDate(date);
					plan.setJobKickId(js.getId());
					plan.setJobKickName(js.getName());
					plan.setJobunitId(js.getJobunitId());
					plan.setJobId(js.getJobId());
					plan.setJobName(js.getJobName());
					planList.add(plan);
				}
				counter ++;
			}
		}
		m_log.debug("planList.size()=" + planList.size());
		//昇順ソート
		Collections.sort(planList, new Comparator<JobPlan>() {
			@Override
			public int compare(JobPlan o1, JobPlan o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		});
		//palns数リストにまとめる
		ArrayList<JobPlan> retList = new ArrayList<JobPlan>();
		int counter = 0;
		//表示数分のみ取得
		while(counter < plans){
			//表示数未満のとき
			if(planList.size() <= counter){
				break;
			}
			retList.add(planList.get(counter));
			counter++;
		}
		return retList;
	}

	/**
	 * JobKickEntityよりJobScheduleを作成するクラス
	 * 
	 * @param jobKickEntity
	 * @return スケジュール情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private JobSchedule createJobScheduleInfo(JobKickEntity jobKickEntity) throws JobMasterNotFound, InvalidRole {

		JobSchedule jobSchedule = new JobSchedule();
		createJobKickInfo(jobKickEntity, jobSchedule);

		//スケジュール設定を取得
		jobSchedule.setScheduleType(jobKickEntity.getScheduleType());
		jobSchedule.setHour(jobKickEntity.getHour());
		jobSchedule.setMinute(jobKickEntity.getMinute());
		jobSchedule.setWeek(jobKickEntity.getWeek());
		jobSchedule.setFromXminutes(jobKickEntity.getFromXMinutes());
		jobSchedule.setEveryXminutes(jobKickEntity.getEveryXMinutes());
		jobSchedule.setSessionPremakeFlg(jobKickEntity.getSessionPremakeFlg());
		jobSchedule.setSessionPremakeScheduleType(jobKickEntity.getSessionPremakeScheduleType());
		jobSchedule.setSessionPremakeWeek(jobKickEntity.getSessionPremakeWeek());
		jobSchedule.setSessionPremakeHour(jobKickEntity.getSessionPremakeHour());
		jobSchedule.setSessionPremakeMinute(jobKickEntity.getSessionPremakeMinute());
		jobSchedule.setSessionPremakeEveryXHour(jobKickEntity.getSessionPremakeEveryXHour());
		jobSchedule.setSessionPremakeDate(jobKickEntity.getSessionPremakeDate());
		jobSchedule.setSessionPremakeToDate(jobKickEntity.getSessionPremakeToDate());
		jobSchedule.setSessionPremakeInternalFlg(jobKickEntity.getSessionPremakeInternalFlg());

		return jobSchedule;
	}

	/**
	 * JobKickEntityよりJobFileCheckを作成するクラス
	 * 
	 * @param jobKickEntity
	 * @return ファイルチェック情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private JobFileCheck createJobFileCheckInfo(JobKickEntity jobKickEntity) throws JobMasterNotFound, InvalidRole, HinemosUnknown {

		JobFileCheck jobFileCheck = new JobFileCheck();
		createJobKickInfo(jobKickEntity, jobFileCheck);

		//ファシリティID取得
		jobFileCheck.setFacilityId(jobKickEntity.getFacilityId());
		//ファシリティパス取得
		String facilityId = jobKickEntity.getFacilityId();
		String scopePath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		jobFileCheck.setScope(scopePath);
		//ディレクトリ取得
		jobFileCheck.setDirectory(jobKickEntity.getDirectory());
		//ファイル名取得
		jobFileCheck.setFileName(jobKickEntity.getFileName());
		//ファイルチェック種別取得
		jobFileCheck.setEventType(jobKickEntity.getEventType());
		//ファイルチェック種別が変更の場合 変更種別取得
		jobFileCheck.setModifyType(jobKickEntity.getModifyType());
		//ファイルが使用されている場合判定を持ち越す
		jobFileCheck.setCarryOverJudgmentFlg(jobKickEntity.getCarryOverJudgementFlg());

		return jobFileCheck;
	}

	/**
	 * JobKickEntityよりJobKickを作成するクラス
	 * 
	 * @param jobKickEntity
	 * @return マニュアル実行契機情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private JobKick createJobManual(JobKickEntity jobKickEntity) throws JobMasterNotFound, InvalidRole {

		JobKick jobKick = new JobKick();
		createJobKickInfo(jobKickEntity, jobKick);
		return jobKick;
	}

	/**
	 * JobKickEntityよりJoblinkRcvを作成するクラス
	 * 
	 * @param jobKickEntity
	 * @return ジョブ連携受信情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private JobLinkRcv createJoblinkRcvInfo(JobKickEntity jobKickEntity) throws JobMasterNotFound, InvalidRole, HinemosUnknown {

		JobLinkRcv jobLinkRcv = new JobLinkRcv();
		createJobKickInfo(jobKickEntity, jobLinkRcv);


		// 送信元ファシリティID取得
		jobLinkRcv.setFacilityId(jobKickEntity.getFacilityId());

		// 送信元ファシリティパス取得
		String facilityId = jobKickEntity.getFacilityId();
		String scopePath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		jobLinkRcv.setScope(scopePath);

		// ジョブ連携メッセージID
		jobLinkRcv.setJoblinkMessageId(jobKickEntity.getJoblinkMessageId());

		// 重要度（情報）
		jobLinkRcv.setInfoValidFlg(jobKickEntity.getInfoValidFlg());

		// 重要度（警告）
		jobLinkRcv.setWarnValidFlg(jobKickEntity.getWarnValidFlg());

		// 重要度（危険）
		jobLinkRcv.setCriticalValidFlg(jobKickEntity.getCriticalValidFlg());

		// 重要度（不明）
		jobLinkRcv.setUnknownValidFlg(jobKickEntity.getUnknownValidFlg());

		// アプリケーションフラグ
		jobLinkRcv.setApplicationFlg(jobKickEntity.getApplicationFlg());

		// アプリケーション
		jobLinkRcv.setApplication(jobKickEntity.getApplication());

		// 監視詳細フラグ
		jobLinkRcv.setMonitorDetailIdFlg(jobKickEntity.getMonitorDetailIdFlg());

		// 監視詳細
		jobLinkRcv.setMonitorDetailId(jobKickEntity.getMonitorDetailId());

		// メッセージフラグ
		jobLinkRcv.setMessageFlg(jobKickEntity.getMessageFlg());

		// メッセージ
		jobLinkRcv.setMessage(jobKickEntity.getMessage());

		// 拡張情報フラグ
		jobLinkRcv.setExpFlg(jobKickEntity.getExpFlg());

		// ジョブ連携メッセージの拡張情報設定
		jobLinkRcv.setJobLinkExpList(new ArrayList<>());
		if (jobKickEntity.getJobLinkJobkickExpInfoEntities() != null) {
			for (JobLinkJobkickExpInfoEntity expInfoEntity 
					: jobKickEntity.getJobLinkJobkickExpInfoEntities()) {
				JobLinkExpInfo expInfo = new JobLinkExpInfo();
				expInfo.setKey(expInfoEntity.getId().getKey());
				expInfo.setValue(expInfoEntity.getValue());
				jobLinkRcv.getJobLinkExpList().add(expInfo);
			}
		}

		// 確認済みメッセージ番号
		jobLinkRcv.setJoblinkRcvCheckedPosition(jobKickEntity.getJoblinkRcvCheckedPosition());

		return jobLinkRcv;
	}

	/**
	 * JobKickEntityよりJobKickを作成するクラス
	 * 
	 * @param jobKickEntity データ取得元Entity
	 * @param jobKick データ格納先Bean
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private void createJobKickInfo(JobKickEntity jobKickEntity, JobKick jobKick) throws JobMasterNotFound, InvalidRole {

		//実行契機IDを取得
		jobKick.setId(jobKickEntity.getJobkickId());
		//実行契機名を取得
		jobKick.setName(jobKickEntity.getJobkickName());
		//ジョブIDを取得
		jobKick.setJobId(jobKickEntity.getJobId());
		//ジョブ名を取得
		JobMstEntity jobMstEntity = QueryUtil.getJobMstPK_OR(
				jobKickEntity.getJobunitId(),
				jobKickEntity.getJobId(),
				jobKickEntity.getOwnerRoleId());
		String jobName = jobMstEntity.getJobName();
		jobKick.setJobName(jobName);

		//ジョブユニットIDを取得
		jobKick.setJobunitId(jobKickEntity.getJobunitId());

		//カレンダIDを取得
		jobKick.setCalendarId(jobKickEntity.getCalendarId());

		//有効/無効を取得
		jobKick.setValid(jobKickEntity.getValidFlg());

		// ランタイムジョブ変数情報取得
		if (jobKickEntity.getJobRuntimeParamEntities() != null 
				&& jobKickEntity.getJobRuntimeParamEntities().size() > 0) {

			jobKick.setJobRuntimeParamList(new ArrayList<JobRuntimeParam>());
			for (JobRuntimeParamEntity jobRuntimeParamEntity : jobKickEntity.getJobRuntimeParamEntities()) {
				JobRuntimeParam jobRuntimeParam = new JobRuntimeParam();
				// 名前
				jobRuntimeParam.setParamId(jobRuntimeParamEntity.getId().getParamId());
				// 種別
				jobRuntimeParam.setParamType(jobRuntimeParamEntity.getParamType());
				// デフォルト値
				jobRuntimeParam.setValue(jobRuntimeParamEntity.getDefaultValue());
				// 説明
				jobRuntimeParam.setDescription(jobRuntimeParamEntity.getDescription());
				// 必須フラグ
				jobRuntimeParam.setRequiredFlg(jobRuntimeParamEntity.getRequiredFlg());

				if (jobRuntimeParamEntity.getJobRuntimeParamDetailEntities() != null
						&& jobRuntimeParamEntity.getJobRuntimeParamDetailEntities().size() > 0) {

					// ランタイムジョブ変数詳細情報の取得
					jobRuntimeParam.setJobRuntimeParamDetailList(new ArrayList<JobRuntimeParamDetail>());
					for (JobRuntimeParamDetailEntity jobRuntimeParamDetailEntity 
							: jobRuntimeParamEntity.getJobRuntimeParamDetailEntities()) {
						JobRuntimeParamDetail jobRuntimeParamDetail = new JobRuntimeParamDetail();
						// 値
						jobRuntimeParamDetail.setParamValue(jobRuntimeParamDetailEntity.getParamValue());
						// 説明
						jobRuntimeParamDetail.setDescription(jobRuntimeParamDetailEntity.getDescription());
						jobRuntimeParam.getJobRuntimeParamDetailList().add(jobRuntimeParamDetail);
					}
				}
				jobKick.getJobRuntimeParamList().add(jobRuntimeParam);
			}
		}

		//オーナーロールIDを取得
		jobKick.setOwnerRoleId(jobKickEntity.getOwnerRoleId());

		//登録者を取得
		jobKick.setCreateUser(jobKickEntity.getRegUser());
		//登録日時を取得
		if (jobKickEntity.getRegDate() != null) {
			jobKick.setCreateTime(jobKickEntity.getRegDate());
		}
		//更新者を取得
		jobKick.setUpdateUser(jobKickEntity.getUpdateUser());
		//更新日時を取得
		if (jobKickEntity.getUpdateDate() != null) {
			jobKick.setUpdateTime(jobKickEntity.getUpdateDate());
		}
	}
}
