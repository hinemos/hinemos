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

package com.clustercontrol.platform.hub;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.QueryHints;

/**
 * HubControllerBeanクラスの環境差分（windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class HubControllerUtil {
	
	public static Connection setStatementTimeout(EntityManager em, int timeout) throws SQLException {
		// do nothing. rhel only.
		return null;
	}
	
	public static void resetStatementTimeout(Connection cn, EntityManager em, int timeout) throws SQLException {
		// do nothing. rhel only.
	}
	
	public static <T> TypedQuery<T> createQuery(EntityManager em, String queryStr, Class<T> resultClass, int timeout) {
		TypedQuery<T> query = em.createQuery(queryStr, resultClass);
		query.setHint(QueryHints.JDBC_TIMEOUT, (timeout / 1000));
		return query;
	}
}