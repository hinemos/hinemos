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
import java.sql.Statement;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * HubControllerBeanクラスの環境差分（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class HubControllerUtil {

	public static Connection setStatementTimeout(EntityManager em, int timeout) throws SQLException {
		// クエリタイムアウト。
		em.getTransaction().begin();
		Connection cn = em.unwrap(java.sql.Connection.class);
		try (Statement s = cn.createStatement()) {
			s.execute("SET SESSION statement_timeout TO " + timeout);
			em.getTransaction().commit();
		}catch(Exception e){
			em.getTransaction().rollback();
			throw(e);
		}
		return cn;
	}
	
	public static void resetStatementTimeout(Connection cn, EntityManager em, int timeout) throws SQLException {
		// クエリタイムアウト解除。
		em.getTransaction().begin();
		try (Statement s = cn.createStatement()) {
			s.execute("RESET statement_timeout");
			em.getTransaction().commit();
		}catch(Exception e){
			em.getTransaction().rollback();
			throw(e);
		}
	}

	public static <T> TypedQuery<T> createQuery(EntityManager em, String queryStr, Class<T> resultClass, int timeout) {
		TypedQuery<T> query = em.createQuery(queryStr, resultClass);
		return query;
	}
}
