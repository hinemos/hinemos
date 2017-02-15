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
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.monitor.model.CollectorCategoryMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;

/**
 * 収集項目コード定義マスタ情報追加クラス
 * 
 * @version 1.2.0
 * @since 1.2.0
 *
 */
public class OperateCollectItemCodeMaster {

	private static Log m_log = LogFactory.getLog(OperateCollectItemCodeMaster.class);

	/**
	 * 収集項目コード定義マスタ情報を追加します。
	 * 
	 * @param data 収集項目コード定義情報
	 * @return 成功した場合、true
	 * @throws EntityExistsException
	 * 
	 */
	public boolean add(CollectorItemCodeMstData data) throws EntityExistsException {

		JpaTransactionManager jtm = new JpaTransactionManager();

		// 収集項目コード定義情報の追加
		try {
			CollectorCategoryMstEntity collectorCategoryMstEntity = null;
			try {
				collectorCategoryMstEntity = QueryUtil.getCollectorCategoryMstPK(data.getCategoryCode());
			} catch (CollectorNotFound e) {
			}
			// インスタンス生成
			CollectorItemCodeMstEntity entity = new CollectorItemCodeMstEntity(data.getItemCode(),
					collectorCategoryMstEntity);
			// 重複チェック
			jtm.checkEntityExists(CollectorItemCodeMstEntity.class, entity.getItemCode());
			entity.setDeviceSupport(data.isDeviceSupport());
			entity.setDeviceType(data.getDeviceType());
			entity.setGraphRange(data.isGraphRange());
			entity.setItemName(data.getItemName());
			entity.setMeasure(data.getMeasure());
			entity.setParentItemCode(data.getParentItemCode());
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		return true;
	}

	/**
	 * 収集項目コード定義マスタ情報を削除します。
	 * @throws CollectorNotFound
	 */
	public boolean delete(String itemCode) throws CollectorNotFound {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		CollectorItemCodeMstEntity entity
		= QueryUtil.getCollectorItemCodeMstPK(itemCode);
		// pkが同じデータが登録されている場合は、削除する
		entity.unchain();	// 	削除前処理
		em.remove(entity);

		return true;
	}

	/**
	 * 収集項目コード定義マスタ情報を全て削除します。
	 */
	public boolean deleteAll() {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		List<CollectorItemCodeMstEntity> col
		= QueryUtil.getAllCollectorItemCodeMst();
		for (CollectorItemCodeMstEntity entity : col) {
			// 削除処理
			entity.unchain();	// 	削除前処理
			em.remove(entity);
		}

		return true;
	}

	/**
	 * 収集項目コード定義マスタ情報を全て検索します。
	 * @return ArrayList<CollectorItemCodeMstData>
	 */
	public ArrayList<CollectorItemCodeMstData> findAll() {

		List<CollectorItemCodeMstEntity> col
		= QueryUtil.getAllCollectorItemCodeMst();

		ArrayList<CollectorItemCodeMstData> list = new ArrayList<CollectorItemCodeMstData>();
		for (CollectorItemCodeMstEntity entity : col) {
			CollectorItemCodeMstData data = new CollectorItemCodeMstData(
					entity.getItemCode(),
					entity.getCollectorCategoryMstEntity().getCategoryCode(),
					entity.getParentItemCode(),
					entity.getItemName(),
					entity.getMeasure(),
					entity.getDeviceSupport(),
					entity.getDeviceType(),
					entity.getGraphRange());
			list.add(data);
		}
		return list;
	}

}
