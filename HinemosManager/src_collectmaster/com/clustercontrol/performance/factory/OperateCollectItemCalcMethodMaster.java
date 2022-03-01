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
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstData;
import com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstPK;
import com.clustercontrol.performance.monitor.model.CollectorCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;

/**
 * 収集毎の計算方法マスタ情報追加クラス
 * 
 * @version 1.2.0
 * @since 1.2.0
 *
 */
public class OperateCollectItemCalcMethodMaster {

	private static Log m_log = LogFactory.getLog( OperateCollectItemCalcMethodMaster.class );

	/**
	 * 収集毎の計算方法マスタ情報を追加します。
	 * 
	 * @param data 収集毎の計算方法情報
	 * @return 成功した場合、true
	 * @throws EntityExistsException
	 * @throws CollectorNotFound
	 * @throws FacilityNotFound
	 */
	public boolean add(CollectorItemCalcMethodMstData data) throws EntityExistsException, CollectorNotFound, FacilityNotFound {

		// 収集毎の計算方法情報の追加
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CollectorCalcMethodMstEntity collectorCalcMethodMstEntity = null;
			try {
				collectorCalcMethodMstEntity = QueryUtil.getCollectorCalcMethodMstPK(data.getCalcMethod());
			} catch (CollectorNotFound e) {
			}
			CollectorItemCodeMstEntity collectorItemCodeMstEntity
			= QueryUtil.getCollectorItemCodeMstPK(data.getItemCode());
			CollectorPlatformMstEntity collectorPlatformMstEntity
			= com.clustercontrol.repository.util.QueryUtil.getCollectorPlatformMstPK(data.getPlatformId());

			// インスタンス生成
			CollectorItemCalcMethodMstEntity entity
			= new CollectorItemCalcMethodMstEntity(
					collectorPlatformMstEntity,
					collectorItemCodeMstEntity,
					data.getCollectMethod(),
					data.getSubPlatformId());
			// 重複チェック
			jtm.checkEntityExists(CollectorItemCalcMethodMstEntity.class, entity.getId());
			// 登録
			em.persist(entity);
			entity.relateToCollectorCalcMethodMstEntity(collectorCalcMethodMstEntity);
			entity.relateToCollectorItemCodeMstEntity(collectorItemCodeMstEntity);
			entity.relateToCollectorPlatformMstEntity(collectorPlatformMstEntity);
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (CollectorNotFound e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (FacilityNotFound e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		return true;
	}

	/**
	 * 収集毎の計算方法マスタ情報を削除します。
	 * @throws CollectorNotFound
	 */
	public boolean delete(CollectorItemCalcMethodMstPK pk) throws CollectorNotFound {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			CollectorItemCalcMethodMstEntity entity
			= QueryUtil.getCollectorItemCalcMethodMstPK(
					pk.getCollectMethod(),
					pk.getPlatformId(),
					pk.getSubPlatformId(),
					pk.getItemCode());
			// pkが同じデータが登録されている場合は、削除する
			entity.unchain();	// 削除前処理
			em.remove(entity);

			return true;
		}
	}

	/**
	 * 収集毎の計算方法マスタ情報を全て削除します。
	 */
	public boolean deleteAll() {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			List<CollectorItemCalcMethodMstEntity> col
			= QueryUtil.getAllCollectorItemCalcMethodMst();
			for (CollectorItemCalcMethodMstEntity entity : col) {
				// 削除処理
				entity.unchain();	// 削除前処理
				em.remove(entity);
			}

			return true;
		}
	}

	/**
	 * 収集毎の計算方法マスタ情報を全て検索します。
	 * @return ArrayList<CollectorItemCalcMethodMstEntity>
	 */
	public ArrayList<CollectorItemCalcMethodMstData> findAll() {

		List<CollectorItemCalcMethodMstEntity> col
		= QueryUtil.getAllCollectorItemCalcMethodMst();

		ArrayList<CollectorItemCalcMethodMstData> list = new ArrayList<CollectorItemCalcMethodMstData>();
		for (CollectorItemCalcMethodMstEntity entity : col) {
			CollectorItemCalcMethodMstData data = new CollectorItemCalcMethodMstData(
					entity.getId().getCollectMethod(),
					entity.getId().getPlatformId(),
					entity.getId().getSubPlatformId(),
					entity.getId().getItemCode(),
					entity.getCollectorCalcMethodMstEntity().getCalcMethod());
			list.add(data);
		}
		return list;
	}

}
