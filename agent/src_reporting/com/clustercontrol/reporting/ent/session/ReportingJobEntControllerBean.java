/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.session;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.reporting.ent.factory.SelectReportingEntJob;


/**
*
* <!-- begin-user-doc --> Enterprise用のJob情報の取得を行うsession bean <!-- end-user-doc --> *
*
*/
public class ReportingJobEntControllerBean {

	private static Log m_log = LogFactory.getLog( ReportingJobEntControllerBean.class );
	
	/**
	 * JobInfoEntityを取得します。<BR>
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @return
	 * @throws JobInfoNotFound
	 */
	public JobInfoEntity getJobInfoEntityPK(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound {
		JobInfoEntity entity = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingEntJob select = new SelectReportingEntJob();
			entity = select.getJobInfoEntityPK(sessionId, jobunitId, jobId);
		} catch (Exception e) {
			m_log.error("error", e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return entity;
	}
	
	/**
	 * JobSessionJobのサマリーを取得します。<BR>
	 * 
	 * @param fromTime
	 * @param toTime
	 * @param jobunitId
	 * @param jobId
	 * @param excJobId
	 * @param jobOrderKey
	 * @param ownerRoleId
	 * @param orderNum
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryJobSessionJob(Long fromTime, Long toTime, String jobunitId, String jobId,
			String excJobId, String jobOrderKey, String ownerRoleId, int orderNum) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingEntJob select = new SelectReportingEntJob();
			list = select.getSummaryJobSessionJob(fromTime, toTime, jobunitId, jobId, excJobId, jobOrderKey, ownerRoleId, orderNum);
		} catch (HinemosDbTimeout e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.error("error", e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return list;
	}
	
	/**
	 * 最大実行時間のJobSessionJobEntityを取得します。<BR>
	 * 
	 * @param maxTime
	 * @param jobunitId
	 * @param jobId
	 * @return
	 */
	public List<JobSessionJobEntity> getJobSessionJobEntityByMaxTime(Long maxTime, String jobunitId, String jobId) {
		List<JobSessionJobEntity> entities = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingEntJob select = new SelectReportingEntJob();
			entities = select.getJobSessionJobEntityByMaxTime(maxTime, jobunitId, jobId);
		} catch (Exception e) {
			m_log.error("error", e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return entities;
	}
	
	/**
	 * jobunitIdとjobIdを条件にJobSessionJobEntityを取得します。<BR>
	 * 
	 * @param fromTime
	 * @param toTime
	 * @param jobunitId
	 * @param jobId
	 * @param ownerRoleId
	 * @return
	 */
	public List<JobSessionJobEntity> getJobSessionJobByJobunitIdAndJobId(Long fromTime, Long toTime, String jobunitId, String jobId, String ownerRoleId){
		List<JobSessionJobEntity> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingEntJob select = new SelectReportingEntJob();
			list = select.getJobSessionJobByJobunitIdAndJobId(fromTime, toTime, jobunitId, jobId, ownerRoleId);
		} catch (Exception e) {
			m_log.error("error", e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return list;
	}
}
