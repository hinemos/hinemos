/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform;

import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.persistence.config.QueryHints;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.util.MessageConstant;

/**
 * 環境差分のある収集蓄積の検索処理を実装するクラス（windows）<BR>
 *
 * @version 6.1.0
 */
public class QueryExecutor {

	public static final Log m_log = LogFactory.getLog(QueryExecutor.class);
	
	public static <T> List<T> getListByQueryNameWithTimeout(
			String queryName, Class<T> resultClass, Map<String, Object> parameters, Integer timeout) throws HinemosDbTimeout {

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			HinemosEntityManager em = jtm.getEntityManager();

			TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
			if (timeout != null) {
				Double tmpTimeout = Math.ceil(Double.valueOf(timeout) / 1000D);
				query.setHint(QueryHints.JDBC_TIMEOUT, tmpTimeout.longValue());
				m_log.debug("getListByQueryNameWithTimeout() timeout=" + tmpTimeout.longValue());
			}
			return query.getResultList();
		} catch (PersistenceException e) {
			if (jtm != null) {
				jtm.rollback();
			}
			if (QueryDivergence.isQueryTimeout(e)) {
				m_log.warn("getListByQueryNameWithTimeout() : Timeout occurred. Please try again after change the condition.", e);
				throw new HinemosDbTimeout(MessageConstant.MESSAGE_SEARCH_TIMEOUT.getMessage());
			} else {
				throw e;
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	public static <T> List<T> getListByJpqlWithTimeout(
			String queryString, Class<T> resultClass, Map<String, Object> parameters, Integer timeout) 
			throws HinemosDbTimeout {
		return getListByJpqlWithTimeout(queryString, resultClass, parameters, timeout, null, null);
	}

	public static <T> List<T> getListByJpqlWithTimeout(
			String queryString, Class<T> resultClass, Map<String, Object> parameters, Integer timeout, Integer firstResult, Integer maxResults)
			throws HinemosDbTimeout {

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			HinemosEntityManager em = jtm.getEntityManager();

			TypedQuery<T> query = em.createQuery(queryString, resultClass);
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
			if (timeout != null) {
				Double tmpTimeout = Math.ceil(Double.valueOf(timeout) / 1000D);
				query.setHint(QueryHints.JDBC_TIMEOUT, tmpTimeout.longValue());
				m_log.debug("getListByJpqlWithTimeout() timeout=" + tmpTimeout.longValue());
			}
			if (firstResult != null) {
				query.setFirstResult(firstResult);
			}
			if (maxResults != null) {
				query.setMaxResults(maxResults);
			}
			return query.getResultList();
		} catch (PersistenceException e) {
			if (jtm != null) {
				jtm.rollback();
			}
			if (QueryDivergence.isQueryTimeout(e)) {
				m_log.warn("getListByJpqlWithTimeout() : Timeout occurred. Please try again after change the condition.", e);
				throw new HinemosDbTimeout(MessageConstant.MESSAGE_SEARCH_TIMEOUT.getMessage());
			} else {
				throw e;
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	public static <T> T getDataByJpqlWithTimeout(
			String queryString, Class<T> resultClass, Map<String, Object> parameters, Integer timeout) 
			throws HinemosDbTimeout {

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			HinemosEntityManager em = jtm.getEntityManager();

			TypedQuery<T> query = em.createQuery(queryString, resultClass);
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
			if (timeout != null) {
				Double tmpTimeout = Math.ceil(Double.valueOf(timeout) / 1000D);
				query.setHint(QueryHints.JDBC_TIMEOUT, tmpTimeout.longValue());
				m_log.debug("getDataByJpqlWithTimeout() timeout=" + tmpTimeout.longValue());
			}
			return query.getSingleResult();
		} catch (PersistenceException e) {
			if (jtm != null) {
				jtm.rollback();
			}
			if (QueryDivergence.isQueryTimeout(e)) {
				m_log.warn("getDataByJpqlWithTimeout() : Timeout occurred. Please try again after change the condition.", e);
				throw new HinemosDbTimeout(MessageConstant.MESSAGE_SEARCH_TIMEOUT.getMessage());
			} else {
				throw e;
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	public static <T> List<T> getListByNativeQueryWithTimeout(
			String queryString, Map<Integer, Object> parameters, Integer timeout) 
			throws HinemosDbTimeout {
		return getListByNativeQueryWithTimeout(queryString, parameters, timeout, null, null);
	}

	public static <T> List<T> getListByNativeQueryWithTimeout(
			String queryString, Map<Integer, Object> parameters, Integer timeout, Integer firstResult, Integer maxResults)
			throws HinemosDbTimeout {

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			HinemosEntityManager em = jtm.getEntityManager();

			Query query = em.createNativeQuery(queryString);
			for (Integer key : parameters.keySet()) {
				query.setParameter(key.intValue(), parameters.get(key));
			}
			if (timeout != null) {
				Double tmpTimeout = Math.ceil(Double.valueOf(timeout) / 1000D);
				query.setHint(QueryHints.JDBC_TIMEOUT, tmpTimeout.longValue());
				m_log.debug("getListByJpqlWithTimeout() timeout=" + tmpTimeout.longValue());
			}
			if (firstResult != null) {
				query.setFirstResult(firstResult);
			}
			if (maxResults != null) {
				query.setMaxResults(maxResults);
			}
			return query.getResultList();
		} catch (PersistenceException e) {
			if (jtm != null) {
				jtm.rollback();
			}
			if (QueryDivergence.isQueryTimeout(e)) {
				m_log.warn("getListByNativeQueryWithTimeout() : Timeout occurred. Please try again after change the condition.", e);
				throw new HinemosDbTimeout(MessageConstant.MESSAGE_SEARCH_TIMEOUT.getMessage());
			} else {
				throw e;
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
}