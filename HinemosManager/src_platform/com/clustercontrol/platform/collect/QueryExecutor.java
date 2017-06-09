/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform.collect;

import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

/**
 * 環境差分のあるHinemosPropertyのデフォルト値を定数として格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class QueryExecutor {
	private static Log m_log = LogFactory.getLog( QueryExecutor.class );
	
	public static <T> List<T> getListWithTimeout(String queryName, Class<T> resultClass, Map<String, Object> parameters) {
		setStatementTimeout();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
		for (String key : parameters.keySet()) {
			query.setParameter(key, parameters.get(key));
		}
		List<T> list = query.getResultList();
		resetStatementTimeout();
		return list;
	}
	
	private static void setStatementTimeout() {
		long timeout = HinemosPropertyUtil.getHinemosPropertyNum("collect.graph.timeout", Long.valueOf(50000));
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		String sql = "SET local statement_timeout TO " + timeout + ";";
		m_log.trace("setStatementTimeout : " + sql);
		try {
			em.createNativeQuery(sql).executeUpdate();
		} catch(Exception e) {
			m_log.error("setStatementTimeout ERROR statement:" + sql);
		}
	}
	
	private static void resetStatementTimeout() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		String sql = "RESET statement_timeout;";
		m_log.trace("resetStatementTimeout : " + sql);
		try {
			em.createNativeQuery(sql).executeUpdate();
		} catch(Exception e) {
			m_log.error("resetStatementTimeout ERROR statement:" + sql);
		}
	}
	
}