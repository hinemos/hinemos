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
import com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstData;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK;
import com.clustercontrol.performance.monitor.model.CollectorCategoryCollectMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorCategoryMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;

/**
 * カテゴリ別収集方法マスタ情報追加クラス
 * 
 * @version 1.2.0
 * @since 1.2.0
 *
 */
public class OperateCollectCategoryCollectMaster {

	private static Log m_log = LogFactory.getLog(OperateCollectCategoryCollectMaster.class);

	/**
	 * カテゴリ別収集方法マスタ情報を追加します。
	 * 
	 * @param data カテゴリ別収集方法情報
	 * @return 成功した場合、true
	 * @throws EntityExistsException
	 * @throws CollectorNotFound
	 * @throws FacilityNotFound
	 * 
	 */
	public boolean add(CollectorCategoryCollectMstData data) throws EntityExistsException, CollectorNotFound, FacilityNotFound {

		// カテゴリ別収集方法情報の追加
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CollectorCategoryMstEntity collectorCategoryMstEntity
			= QueryUtil.getCollectorCategoryMstPK(data.getCategoryCode());
			CollectorPlatformMstEntity collectorPlatformMstEntity
			= com.clustercontrol.repository.util.QueryUtil.getCollectorPlatformMstPK(data.getPlatformId());

			// インスタンス生成
			CollectorCategoryCollectMstEntity entity
			= new CollectorCategoryCollectMstEntity(
					collectorPlatformMstEntity,
					collectorCategoryMstEntity,
					data.getSubPlatformId());
			// 重複チェック
			jtm.checkEntityExists(CollectorCategoryCollectMstEntity.class, entity.getId());
			entity.setCollectMethod(data.getCollectMethod());
			// 登録
			em.persist(entity);
			entity.relateToCollectorCategoryMstEntity(collectorCategoryMstEntity);
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
	 * カテゴリ別収集方法マスタ情報を削除します。
	 * @throws CollectorNotFound
	 */
	public boolean delete(CollectorCategoryCollectMstPK pk) throws CollectorNotFound {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			CollectorCategoryCollectMstEntity entity
			= QueryUtil.getCollectorCategoryCollectMstPK(
					pk.getPlatformId(),
					pk.getSubPlatformId(),
					pk.getCategoryCode());
			// pkが同じデータが登録されている場合は、削除する
			entity.unchain();	// 削除前処理
			em.remove(entity);

			return true;
		}
	}

	/**
	 * カテゴリ別収集方法マスタ情報を全て削除します。
	 */
	public boolean deleteAll() {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			List<CollectorCategoryCollectMstEntity> col = QueryUtil.getAllCollectorCategoryCollectMst();
			for (CollectorCategoryCollectMstEntity entity : col) {
				// 削除処理
				entity.unchain();	// 削除前処理
				em.remove(entity);
			}

			return true;
		}
	}

	/**
	 * カテゴリ別収集方法マスタ情報を全て検索します。
	 * @return ArrayList<CollectorCategoryCollectMstData>
	 */
	public ArrayList<CollectorCategoryCollectMstData> findAll() {

		List<CollectorCategoryCollectMstEntity> col = QueryUtil.getAllCollectorCategoryCollectMst();
		ArrayList<CollectorCategoryCollectMstData> list = new ArrayList<CollectorCategoryCollectMstData>();
		for (CollectorCategoryCollectMstEntity entity : col) {
			CollectorCategoryCollectMstData data = new CollectorCategoryCollectMstData(
					entity.getId().getPlatformId(),
					entity.getId().getSubPlatformId(),
					entity.getId().getCategoryCode(),
					entity.getCollectMethod());
			list.add(data);
		}
		return list;
	}

}
