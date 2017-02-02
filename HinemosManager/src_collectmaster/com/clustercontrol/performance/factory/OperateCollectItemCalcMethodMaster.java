/*

Copyright (C) since 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.performance.factory;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;

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

		JpaTransactionManager jtm = new JpaTransactionManager();

		// 収集毎の計算方法情報の追加
		try {
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
					data.getSubPlatformId(),
					collectorCalcMethodMstEntity);
			// 重複チェック
			jtm.checkEntityExists(CollectorItemCalcMethodMstEntity.class, entity.getId());
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

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

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

	/**
	 * 収集毎の計算方法マスタ情報を全て削除します。
	 */
	public boolean deleteAll() {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		List<CollectorItemCalcMethodMstEntity> col
		= QueryUtil.getAllCollectorItemCalcMethodMst();
		for (CollectorItemCalcMethodMstEntity entity : col) {
			// 削除処理
			entity.unchain();	// 削除前処理
			em.remove(entity);
		}

		return true;
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
