/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;

public class JobSessionImpl {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( JobSessionImpl.class );

	public List<String> getRunUnendSession() throws JobInfoNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		ArrayList<String> list = new ArrayList<String>();
		Collection<JobSessionEntity> collection = null;
		collection = em.createNamedQuery("JobSessionEntity.findUnendSessions", JobSessionEntity.class).getResultList();
		if (collection == null) {
			JobInfoNotFound je = new JobInfoNotFound("JobSessionEntity.findUnendSessions");
			m_log.info("getUnendSessionList() "
					+ je.getClass().getSimpleName() + ", " + je.getMessage());
			throw je;
		}

		for (JobSessionEntity session : collection) {
			m_log.debug("getUnendSessionList() target sessionid is " + session.getSessionId());
			list.add(session.getSessionId());
		}
		return list;
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

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		m_log.debug("_runJob() start : sessionId = " + sessionId);
		//実行状態が実行中、待機のセッションジョブを取得
		List<Integer> statuses = new ArrayList<Integer>();
		statuses.add(StatusConstant.TYPE_RUNNING);
		statuses.add(StatusConstant.TYPE_WAIT);
		
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
