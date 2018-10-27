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

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.util.MessageConstant;

/**
 * 環境差分のある検索処理を実装するクラス（linux）<BR>
 *
 * @version 6.1.0
 */
public class QueryExecutor {

	private static Log m_log = LogFactory.getLog( QueryExecutor.class );

	public static <T> List<T> getListByQueryNameWithTimeout(
		String queryName, Class<T> resultClass, Map<String, Object> parameters, Integer timeout) throws HinemosDbTimeout {

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			HinemosEntityManager em = jtm.getEntityManager();

			setStatementTimeout(timeout);
			TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
			List<T> list = query.getResultList();
			resetStatementTimeout();
			return list;
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

			setStatementTimeout(timeout);
			TypedQuery<T> query = em.createQuery(queryString, resultClass);
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
			if (firstResult != null) {
				query.setFirstResult(firstResult);
			}
			if (maxResults != null) {
				query.setMaxResults(maxResults);
			}
			List<T> list = query.getResultList();
			resetStatementTimeout();
			return list;
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

			setStatementTimeout(timeout);
			TypedQuery<T> query = em.createQuery(queryString, resultClass);
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
			T data = query.getSingleResult();
			resetStatementTimeout();
			return data;
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

			setStatementTimeout(timeout);
			Query query = em.createNativeQuery(queryString);
			for (Integer key : parameters.keySet()) {
				query.setParameter(key.intValue(), parameters.get(key));
			}
			if (firstResult != null) {
				query.setFirstResult(firstResult);
			}
			if (maxResults != null) {
				query.setMaxResults(maxResults);
			}
			List<T> list = query.getResultList();
			resetStatementTimeout();
			return list;
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
	
	private static void setStatementTimeout(int timeout) {
		String sql = "SET local statement_timeout TO " + timeout + ";";
		m_log.trace("setStatementTimeout : " + sql);
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNativeQuery(sql).executeUpdate();
		} catch(Exception e) {
			m_log.error("setStatementTimeout ERROR statement:" + sql);
		}
	}
	
	private static void resetStatementTimeout() {
		String sql = "RESET statement_timeout;";
		m_log.trace("resetStatementTimeout : " + sql);
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNativeQuery(sql).executeUpdate();
		} catch(Exception e) {
			m_log.error("resetStatementTimeout ERROR statement:" + sql);
		}
	}
}