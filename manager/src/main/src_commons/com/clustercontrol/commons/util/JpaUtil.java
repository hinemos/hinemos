package com.clustercontrol.commons.util;

import javax.persistence.Query;

import org.eclipse.persistence.internal.jpa.EJBQueryImpl;

/**
 * JPAツール（Eclipselink等）の差異を吸収するUtilクラス
 *
 */
public class JpaUtil {

	/**
	 * QueryからJPQLを取得する
	 * 　　下記の方法以外なら、orm.xmlを解析して取得する方法（JPA Securityで行っている実装）がある。
	 * 
	 * @param query
	 * @return
	 */
	public static String getJpqlString(Query query) {
		return query.unwrap(EJBQueryImpl.class).getDatabaseQuery().getJPQLString();
	}
}
