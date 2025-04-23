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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.RetryWaitStatusConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;

public class JobSessionImpl {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( JobSessionImpl.class );
	
	public List<String> getRunUnendSession() throws JobInfoNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			ArrayList<String> list = new ArrayList<String>();

			Collection<JobSessionJobEntity> collection = null;
			collection = em.createNamedQuery("JobSessionJobEntity.findUnendSessions", JobSessionJobEntity.class)
					.setParameter("statuses", StatusConstant.getUnendList())
					.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
					.getResultList();
			if (collection == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findUnendSessions");
				m_log.info("getUnendSessionList() "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}

			for (JobSessionJobEntity session : collection) {
				m_log.debug("getUnendSessionList() target sessionid is " + session.getId().getSessionId());
				list.add(session.getId().getSessionId());
			}
			return list;
		}
	}

	/**
	 * ジョブの実行チェックをします。
	 * 開始遅延や終了遅延などに時刻が入っている場合や、待ち条件に時刻待ちがある場合に、
	 * このメソッドは呼ばれます。
	 * 
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws FacilityNotFound 
	 */
	public void runningCheck(String sessionId) throws JobInfoNotFound, InvalidRole, HinemosUnknown, FacilityNotFound {
		m_log.info("runningCheck() start : sessionid = " + sessionId);
		JobSessionJobImpl.maxCheckDate(sessionId);
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			m_log.debug("_runJob() start : sessionId = " + sessionId);
			//実行状態が実行中、待機のセッションジョブを取得
			List<Integer> statuses = new ArrayList<Integer>();
			statuses.add(StatusConstant.TYPE_RUNNING);
			statuses.add(StatusConstant.TYPE_WAIT);
			statuses.add(StatusConstant.TYPE_RUNNING_QUEUE);
			
			Collection<JobSessionJobEntity> collection = null;
			collection = em.createNamedQuery("JobSessionJobEntity.findBySessionStatuses", JobSessionJobEntity.class)
					.setParameter("sessionId", sessionId)
					.setParameter("statuses", statuses)
					.getResultList();
			for (JobSessionJobEntity sessionJob : collection){
				String jobunitId = sessionJob.getId().getJobunitId();
				String jobId = sessionJob.getId().getJobId();
				//ジョブを取得
				JobInfoEntity job = sessionJob.getJobInfoEntity();
				int type = job.getJobType();
				m_log.debug("runningCheck() target : sessionId = " + sessionId +
								", jobunitId = " + jobunitId + ", jobId = " + jobId + ", type = " + type);
				new JobSessionJobImpl().startJob(sessionId, jobunitId, jobId);
			}
		}
	}
	
	/**
	 * 待機状態が解除されるかを定期的にチェックする 
	 * 対象のジョブはジョブ変数待ち条件、セッション横断待ち条件を持っているジョブ
	 * 
	 * @param sessionId
	 * @param jobImpl  ユニットテストで入れ替えられるよう引数にしている
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 */
	public void waitingCheck(String sessionId) throws JobInfoNotFound, HinemosUnknown, InvalidRole, FacilityNotFound {
		m_log.debug("waitingCheck() start : sessionid = " + sessionId);
		JobSessionJobImpl jobImpl = new JobSessionJobImpl();
		List<String[]> waitCheckJobIdList = jobImpl.getWaitCheckJob(sessionId);
		if (waitCheckJobIdList != null && waitCheckJobIdList.size() > 0) {
			m_log.info("waitingCheck() exist target job : sessionid = " + sessionId);
		}
		//ジョブの実行前に現在の待機中の対象ジョブのリストをclearしておく
		jobImpl.clearWaitCheckMap(sessionId);

		if (waitCheckJobIdList == null) {
			return;
		}
		for (String[] jobunitIdJobId : waitCheckJobIdList){
			String jobunitId = jobunitIdJobId[0];
			String jobId = jobunitIdJobId[1];
			if( m_log.isDebugEnabled()){
				m_log.debug("waitingCheck() target : sessionId = " + sessionId + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
			}
			JobSessionJobEntity parentSessionJob = null;
			try{
				// 親ジョブが停止となっている場合があるので、親ジョブが実行中の場合のみ開始を試行
				//  実行中でないなら以後の定期チェックは一旦不要。（再度実行中になった時に必要に応じて再開される）
				//  再度の定期チェックが必要なら、startJob内にてチェックリストに再登録される前提である。
				JobSessionJobEntity targetSessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
				parentSessionJob = QueryUtil.getJobSessionJobPK(sessionId,
						targetSessionJob.getParentJobunitId(), targetSessionJob.getParentJobId());
				if (parentSessionJob.getStatus() == StatusConstant.TYPE_RUNNING ) {
					jobImpl.startJob(sessionId, jobunitId, jobId);
				}
			}catch (JobInfoNotFound | InvalidRole e){
				//親ジョブ情報の取得でエラーとなった場合、該当セッションの情報が削除されたものとみなす。
				m_log.warn("waitingCheck() not exist target job : sessionId = " + sessionId + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
			}
			
		}
	}

	/**
	 * リトライ待ち中のジョブに対して、待ち時間経過したかを定期的にチェック
	 * @param sessionId
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public void retryWaitingCheck(String sessionId) throws JobInfoNotFound, InvalidRole, FacilityNotFound, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// リトライ待ち中候補のジョブを取得（実行中のもの）
			Collection<JobSessionJobEntity> collection = null;
			collection = em.createNamedQuery("JobSessionJobEntity.findByJobSessionIdAndStatus", JobSessionJobEntity.class)
					.setParameter("sessionId", sessionId)
					.setParameter("status", StatusConstant.TYPE_RUNNING)
					.getResultList();

			// リトライ待ち中のジョブの終了処理を再開
			// 判定は JobSessionJobImpl#endJob()メソッドの「繰り返し実行判定」にまかせる
			for (JobSessionJobEntity sessionJob : collection) {
				String jobunitId = sessionJob.getId().getJobunitId();
				String jobId = sessionJob.getId().getJobId();
				if (sessionJob.getRetryWaitStatus().equals(RetryWaitStatusConstant.WAIT)) {
					new JobSessionJobImpl().endJob(sessionId, jobunitId, jobId, sessionJob.getResult(), false);
				}
			}
		}
	}

	/**
	 * ジョブのタイムアウトを定期的にチェックする<BR>
	 * 
	 * @param sessionId
	 * @return チェックを実施したジョブの件数
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public int jobTimeoutCheck(String sessionId) throws JobInfoNotFound, InvalidRole, FacilityNotFound, HinemosUnknown {
		m_log.debug("timeoutCheck() start : sessionid = " + sessionId);
		int jobCount = 0;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 現時点では実行中のジョブのみが対象
			List<Integer> statuses = new ArrayList<Integer>();
			statuses.add(StatusConstant.TYPE_RUNNING);
			// この時点でタイムアウトチェックが必要な特定のジョブ種別に絞る
			List<Integer> jobTypes = new ArrayList<Integer>();
			jobTypes.add(JobConstant.TYPE_FILECHECKJOB);

			Collection<JobSessionJobEntity> collection = em
					.createNamedQuery("JobSessionJobEntity.findBySessionStatusesAndJobTypes", JobSessionJobEntity.class)
					.setParameter("sessionId", sessionId)
					.setParameter("statuses", statuses)
					.setParameter("jobTypes", jobTypes)
					.getResultList();
			for (JobSessionJobEntity sessionJob : collection) {
				if (sessionJob.getStartDate() == null){
					// ジョブ繰り返し実行の待機中など、ジョブが開始されていない場合は対象外
					continue;
				}
				jobCount++;
				new JobSessionJobImpl().checkJobTimeout(sessionJob.getId());
			}
		}
		return jobCount;
	}
}
