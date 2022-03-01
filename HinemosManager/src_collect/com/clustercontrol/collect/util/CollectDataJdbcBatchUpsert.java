/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.commons.util.JdbcBatchUpsertUtil;

/**
 *  CollectDataにマッピングするテーブルにデータを登録するクラス(ジョブ履歴用)
 */
public class CollectDataJdbcBatchUpsert extends JdbcBatchQuery {
	private static final String SQL = JdbcBatchUpsertUtil.COLLECT_DATA_SQL;
	
	private List<CollectData> entities = null;

	public CollectDataJdbcBatchUpsert(List<CollectData> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (CollectData entity : entities) {
			size++;
			CollectDataPK pk = entity.getId();
			Object[] params = JdbcBatchUpsertUtil.getParameters(pk, entity);
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
