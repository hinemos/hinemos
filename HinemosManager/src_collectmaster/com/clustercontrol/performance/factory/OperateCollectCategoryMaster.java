/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.factory;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryMstData;
import com.clustercontrol.performance.monitor.model.CollectorCategoryMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;

/**
 * 収集カテゴリ情報マスタ情報追加クラス
 * 
 * @version 1.2.0
 * @since 1.2.0
 *
 */
public class OperateCollectCategoryMaster {

	private static Log m_log = LogFactory.getLog(OperateCollectCategoryMaster.class);

	/**
	 * 収集カテゴリ情報マスタ情報を追加します。
	 * 
	 * @param data 収集カテゴリ情報情報
	 * @return 成功した場合、true
	 * @throws EntityExistsException
	 * 
	 */
	public boolean add(CollectorCategoryMstData data) throws EntityExistsException {

		// 収集カテゴリ情報情報の追加
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// インスタンス生成
			CollectorCategoryMstEntity entity = new CollectorCategoryMstEntity(data.getCategoryCode());
			// 重複チェック
			jtm.checkEntityExists(CollectorCategoryMstEntity.class, entity.getCategoryCode());
			entity.setCategoryName(data.getCategoryName());
			// 登録
			em.persist(entity);
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		return true;
	}

	/**
	 * 収集カテゴリ情報マスタ情報を削除します。
	 * @throws CollectorNotFound
	 */
	public boolean delete(String categoryCode) throws CollectorNotFound {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			CollectorCategoryMstEntity entity = QueryUtil.getCollectorCategoryMstPK(categoryCode);
			// pkが同じデータが登録されている場合は、削除する
			em.remove(entity);

			return true;
		}
	}

	/**
	 * 収集カテゴリ情報マスタ情報を全て削除します。
	 */
	public boolean deleteAll() {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			List<CollectorCategoryMstEntity> col
			= QueryUtil.getAllCollectorCategoryMst();
			for (CollectorCategoryMstEntity entity : col) {
				//削除処理
				em.remove(entity);
			}

			return true;
		}
	}

	/**
	 * 収集カテゴリ情報マスタ情報を全て検索します。
	 * @return ArrayList<CollectorCategoryMstData>
	 */
	public ArrayList<CollectorCategoryMstData> findAll() {

		List<CollectorCategoryMstEntity> col
		= QueryUtil.getAllCollectorCategoryMst();

		ArrayList<CollectorCategoryMstData> list = new ArrayList<CollectorCategoryMstData>();
		for (CollectorCategoryMstEntity entity : col) {
			CollectorCategoryMstData data = new CollectorCategoryMstData(
					entity.getCategoryCode(),
					entity.getCategoryName());
			list.add(data);
		}
		return list;
	}

}
