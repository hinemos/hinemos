/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.grafana.session;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.grafana.factory.EventAggregation;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationResponse;
import com.clustercontrol.grafana.factory.JobHistoryAggregation;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationResponse;
import com.clustercontrol.grafana.factory.JobLastRunTime;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobLastRunTimeListRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobLastRunTimeListResponse;
import com.clustercontrol.grafana.factory.StatusAggregation;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationResponse;

/**
 * Grafana用APIに対するsessin beanです
 *
 */
public class GrafanaControllerBean {

	private static Logger m_log = Logger.getLogger(GrafanaControllerBean.class);

	/**
	 * イベント集計を取得します。
	 * 
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public GetEventAggregationResponse getEventAggregation(GetEventAggregationRequest dtoReq)
			throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		GetEventAggregationResponse dtoRes = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			dtoRes = new EventAggregation().getEventAggregation(dtoReq);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getEventAggregation() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return dtoRes;
	}

	/**
	 * ステータス集計を取得します。
	 * 
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public GetStatusAggregationResponse getStatusAggregation(GetStatusAggregationRequest dtoReq)
			throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		GetStatusAggregationResponse dtoRes = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			dtoRes = new StatusAggregation().getStatusAggregation(dtoReq);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getStatusAggregation() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return dtoRes;
	}

	/**
	 * ジョブ実行履歴集計を取得します。
	 * 
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public GetJobHistoryAggregationResponse getJobHistoryAggregation(GetJobHistoryAggregationRequest dtoReq)
			throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		GetJobHistoryAggregationResponse dtoRes = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			dtoRes = new JobHistoryAggregation().getJobHistoryAggregation(dtoReq);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getJobHistoryAggregation() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return dtoRes;
	}

	/**
	 * ジョブ最終実行時刻一覧を取得します。
	 * 
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public GetJobLastRunTimeListResponse getJobLastRunTimeList(GetJobLastRunTimeListRequest dtoReq)
			throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		GetJobLastRunTimeListResponse dtoRes = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			dtoRes = new JobLastRunTime().getJobLastRunTimeList(dtoReq);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getJobLastRunTimeList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return dtoRes;
	}
}