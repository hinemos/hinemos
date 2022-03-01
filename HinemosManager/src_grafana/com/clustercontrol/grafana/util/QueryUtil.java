/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.grafana.util;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	/**
	 * Facility ID の一時格納テーブルを作成する。
	 */
	public static int createTargetFacilityIdsTable() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNativeQuery("CREATE TEMPORARY TABLE cc_target_facility_ids (facility_id varchar(512) NOT NULL)").executeUpdate();
			return ret;
		}
	}

	/**
	 * Facility ID の一時格納テーブルに ID 一覧を格納する。
	 */
	public static int insertTargetFacilityIds(List<String> facilityIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder sbSql = new StringBuilder();
			sbSql.append("INSERT INTO cc_target_facility_ids(facility_id) VALUES ('");
			sbSql.append(String.join("'),('", facilityIds));
			sbSql.append("');");
			int ret = em.createNativeQuery(sbSql.toString()).executeUpdate();
			return ret;
		}
	}
	
	/**
	 * Facility ID の一時格納テーブルを削除する。
	 */
	public static int dropTargetCollectorIdsTable() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNativeQuery("DROP TABLE cc_target_facility_ids").executeUpdate();
			return ret;
		}
	}
}