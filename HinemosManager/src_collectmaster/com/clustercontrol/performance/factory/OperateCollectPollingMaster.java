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

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.performance.monitor.entity.CollectorPollingMstData;
import com.clustercontrol.performance.monitor.entity.CollectorPollingMstPK;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorPollingMstEntity;
import com.clustercontrol.performance.monitor.model.SnmpValueTypeMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;

/**
 * 収集方法・プラットフォーム毎の収集項目マスタ情報追加クラス
 * 
 * @version 1.2.0
 * @since 1.2.0
 *
 */
public class OperateCollectPollingMaster {

	private static Log m_log = LogFactory.getLog(OperateCollectPollingMaster.class);

	/**
	 * 収集方法・プラットフォーム毎の収集項目マスタ情報を追加します。
	 * 
	 * @param data 収集方法・プラットフォーム毎の収集項目情報
	 * @return 成功した場合、true
	 * @throws EntityExistsException
	 * @throws CollectorNotFound
	 * @throws FacilityNotFound
	 * 
	 */
	public boolean add(CollectorPollingMstData data) throws EntityExistsException, CollectorNotFound, FacilityNotFound {

		// 収集方法毎の収集項目情報の追加
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CollectorItemCodeMstEntity collectorItemCodeMstEntity
			= QueryUtil.getCollectorItemCodeMstPK(data.getItemCode());
			CollectorPlatformMstEntity collectorPlatformMstEntity
			= com.clustercontrol.repository.util.QueryUtil.getCollectorPlatformMstPK(data.getPlatformId());
			SnmpValueTypeMstEntity snmpValueTypeMstEntity = null;
			try {
				snmpValueTypeMstEntity = QueryUtil.getSnmpValueTypeMstPK(data.getValueType());
			} catch (CollectorNotFound e) {
			}

			// インスタンス生成
			CollectorPollingMstEntity entity = new CollectorPollingMstEntity(
					collectorPlatformMstEntity,
					collectorItemCodeMstEntity,
					data.getCollectMethod(),
					data.getSubPlatformId(),
					data.getVariableId());
			// 重複チェック
			jtm.checkEntityExists(CollectorPollingMstEntity.class, entity.getId());
			entity.setEntryKey(data.getEntryKey());
			entity.setFailureValue(data.getFailureValue());
			entity.setPollingTarget(data.getPollingTarget());
			// 登録
			em.persist(entity);
			entity.relateToCollectorItemCodeMstEntity(collectorItemCodeMstEntity);
			entity.relateToCollectorPlatformMstEntity(collectorPlatformMstEntity);
			entity.relateToSnmpValueTypeMstEntity(snmpValueTypeMstEntity);
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
	 * 収集方法・プラットフォーム毎の収集項目マスタ情報を削除します。
	 * @throws CollectorNotFound
	 */
	public boolean delete(CollectorPollingMstPK pk) throws CollectorNotFound {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			CollectorPollingMstEntity entity
			= QueryUtil.getCollectorPollingMstPK(
					pk.getCollectMethod(),
					pk.getPlatformId(),
					pk.getSubPlatformId(),
					pk.getItemCode(),
					pk.getVariableId());
			// pkが同じデータが登録されている場合は、削除する
			entity.unchain();	// 削除前処理
			em.remove(entity);

			return true;
		}
	}

	/**
	 * 収集方法・プラットフォーム毎の収集項目マスタ情報を全て削除します。
	 */
	public boolean deleteAll() {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			List<CollectorPollingMstEntity> col = QueryUtil.getAllCollectorPollingMst();
			for (CollectorPollingMstEntity entity : col) {
				// 削除処理
				entity.unchain();	// 削除前処理
				em.remove(entity);
			}

			return true;
		}
	}

	/**
	 * 収集方法・プラットフォーム毎の収集項目マスタ情報を全て検索します。
	 * @return ArrayList<CollectorPollingMstData>
	 */
	public ArrayList<CollectorPollingMstData> findAll() {

		List<CollectorPollingMstEntity> col
		= QueryUtil.getAllCollectorPollingMst();

		ArrayList<CollectorPollingMstData> list = new ArrayList<CollectorPollingMstData>();
		for (CollectorPollingMstEntity entity : col) {
			CollectorPollingMstData data = new CollectorPollingMstData(
					entity.getId().getCollectMethod(),
					entity.getId().getPlatformId(),
					entity.getId().getSubPlatformId(),
					entity.getId().getItemCode(),
					entity.getId().getVariableId(),
					entity.getEntryKey(),
					entity.getSnmpValueTypeMstEntity() == null ? null : entity.getSnmpValueTypeMstEntity().getValueType(),
							entity.getPollingTarget(),
							entity.getFailureValue());
			list.add(data);
		}
		return list;
	}

}
