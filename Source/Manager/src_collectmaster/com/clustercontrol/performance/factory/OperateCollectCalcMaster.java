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
import com.clustercontrol.performance.monitor.entity.CollectorCalcMethodMstData;
import com.clustercontrol.performance.monitor.model.CollectorCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;

/**
 * 計算ロジックマスタ情報操作クラス
 * 
 * @version 1.2.0
 * @since 1.2.0
 *
 */
public class OperateCollectCalcMaster {

	private static Log m_log = LogFactory.getLog(OperateCollectCalcMaster.class);

	/**
	 * 計算ロジックマスタ情報を追加します。
	 * 
	 * @param data 計算ロジック情報
	 * @return 成功した場合、true
	 * @throws EntityExistsException
	 * 
	 */
	public boolean add(CollectorCalcMethodMstData data) throws EntityExistsException {

		// 計算ロジック情報の追加
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// インスタンス生成
			CollectorCalcMethodMstEntity entity = new CollectorCalcMethodMstEntity(data.getCalcMethod());
			// 重複チェック
			jtm.checkEntityExists(CollectorCalcMethodMstEntity.class, entity.getCalcMethod());
			entity.setClassName(data.getClassName());
			entity.setExpression(data.getExpression());
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
	 * 計算ロジックマスタ情報を削除します。
	 * @throws CollectorNotFound
	 */
	public boolean delete(String calcMethod) throws CollectorNotFound {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			CollectorCalcMethodMstEntity entity
			= QueryUtil.getCollectorCalcMethodMstPK(calcMethod);
			// pkが同じデータが登録されている場合は、削除する
			em.remove(entity);

			return true;
		}
	}

	/**
	 * 計算ロジックマスタ情報を全て削除します。
	 */
	public boolean deleteAll() {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			List<CollectorCalcMethodMstEntity> col
			= QueryUtil.getAllCollectorCalcMethodMst();
			for (CollectorCalcMethodMstEntity entity : col) {
				// 削除処理
				em.remove(entity);
			}

			return true;
		}
	}

	/**
	 * 計算ロジックマスタ情報を全て検索します。
	 * @return ArrayList<CollectorCalcMethodMstData>
	 */
	public ArrayList<CollectorCalcMethodMstData> findAll() {

		List<CollectorCalcMethodMstEntity> col
		= QueryUtil.getAllCollectorCalcMethodMst();

		ArrayList<CollectorCalcMethodMstData> list = new ArrayList<CollectorCalcMethodMstData>();
		for (CollectorCalcMethodMstEntity entity : col) {
			CollectorCalcMethodMstData data = new CollectorCalcMethodMstData(
					entity.getCalcMethod(),
					entity.getClassName(),
					entity.getExpression());
			list.add(data);
		}
		return list;
	}

}
