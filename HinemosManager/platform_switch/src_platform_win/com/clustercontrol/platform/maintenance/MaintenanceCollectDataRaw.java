/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.maintenance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.factory.MaintenanceObject;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.util.HinemosTime;

/**
 * 性能実績の削除処理
 *
 * @version 5.1.0
 * @since 5.1.0
 *
 */
public class MaintenanceCollectDataRaw extends MaintenanceObject {

	private static Log m_log = LogFactory.getLog( MaintenanceCollectDataRaw.class );
	// 重複チェックを直列化するためのロック
	private static final ReentrantLock lock = new ReentrantLock();

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId) {
		
		m_log.info("deleteCollectData() : start");

		long start = HinemosTime.currentTimeMillis();
		int ret = 0;
		final Long f_boundary = boundary;
		final boolean f_status = status;
		final String f_ownerRoleId = ownerRoleId;
		final Counter counter = new Counter();
		
		// トランザクション分離のため、削除用スレッド定義
		Thread deleteThread = new Thread(new Runnable() {
			@Override
			public void run() {
				deleteConditions(f_boundary, f_status, f_ownerRoleId, counter);
			}
		}, "delete-collectData");
		
		deleteThread.start();
		
		try {
			// 1つの削除設定毎確実に処理させるために待機
			deleteThread.join();
		} catch (InterruptedException e) {
			m_log.error("deleteCollectData() : " + deleteThread.getName()
					+ " was interrupted. messages : " + e.getMessage(), e);
		}
		
		ret = counter.get();
		m_log.info("deleteCollectData() result : " + ret +
				 ", time : " + (HinemosTime.currentTimeMillis() - start));
		return ret;
	}

	/**
	 * 引数の条件から削除対象のSQLを選定し、発行する
	 * @param boundary
	 * @param status
	 * @param ownerRoleId
	 * @param counter
	 */
	private void deleteConditions(Long boundary, boolean status, String ownerRoleId, Counter counter) {
		// 複数のスケジュールによりメンテナンス削除処理が行われる際に、
		// 処理が入れ子になり処理件数が混在してしまわないよう排他する
		JpaTransactionManager jpaTran = null;
		try{
			lock.lock();
			long start = 0;
			// AdminRoleの場合は監視IDを条件にせず、全て削除
			if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
				m_log.debug("Is Administrators");
				
				List<CollectKeyInfo> collectKeyList = com.clustercontrol.collect.util.QueryUtil.getCollectKeyInfoAll();
				
				// 収集項目ID毎に削除する
				for (CollectKeyInfo collectKeyInfo : collectKeyList) {
					start = HinemosTime.currentTimeMillis();
					jpaTran = new JpaTransactionManager();
					jpaTran.begin();
					int result = delete(boundary, status, collectKeyInfo.getCollectorid());
					counter.countUp(result);
					jpaTran.commit();
					jpaTran.close();
					m_log.debug("Delete collect data. collectID : " + collectKeyInfo.getCollectorid()
							+ ", result : " + result + ", time : " + (HinemosTime.currentTimeMillis() - start));
				}
			} else {
				m_log.debug("Not Administrators");
				
				// AdminRole以外の場合は、オーナーロールIDに紐づく監視項目IDを抽出して削除する
				ArrayList<MonitorInfo> monitorList = new MonitorSettingControllerBean().getMonitorList();
				
				ArrayList<String> monitorIdList = new ArrayList<>();
				
				for (MonitorInfo monitorInfo : monitorList) {
					if (RoleIdConstant.isAdministratorRole(ownerRoleId) 
							|| monitorInfo.getOwnerRoleId().equals(ownerRoleId)) {
						monitorIdList.add(monitorInfo.getMonitorId());
					}
				}
				
				// 監視項目IDが取得できなければ削除対象は存在しないので処理終了
				if (monitorIdList.isEmpty()) {
					m_log.info("MonitorId is nothing. ownerRoleId : " + ownerRoleId);
					return;
				}
				// 監視IDのリストから全てのcollectorIdを取得
				List<Integer> collectIdList = getCollectIds(status, monitorIdList);
				
				// 収集項目IDが取得できなければ削除対象は存在しないので処理終了
				if (collectIdList.isEmpty()) {
					m_log.info("CollectorId is nothing. ownerRoleId : " + ownerRoleId);
					return;
				}
				
				// 収集項目ID毎に削除する
				for(Integer collectId : collectIdList) {
					start = HinemosTime.currentTimeMillis();
					jpaTran = new JpaTransactionManager();
					jpaTran.begin();
					int result = delete(boundary, status, collectId);
					counter.countUp(result);
					jpaTran.commit();
					jpaTran.close();
					m_log.debug("Delete collect data. collectID : " + collectId
							+ ", result : " + result + ", time : " + (HinemosTime.currentTimeMillis() - start));
				}
			}
			// 収集キーデータのキャッシュを念の為、確認
			MonitorCollectDataCache.removeUnnecessaryData();
			
		} catch(Exception e){
			counter.setError();
			m_log.error("deleteCollectData() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jpaTran != null)
				jpaTran.rollback();
		} finally {
			if (jpaTran != null) {
				jpaTran.close();
			}
			lock.unlock();
		}
	}
	
	/**
	 * 削除実行
	 * @param boundary
	 * @param status
	 * @param collectId
	 * @return
	 */
	protected int delete(Long boundary, boolean status, int collectId) {
		m_log.debug("_delete() start : status = " + status + ", collectorId = " + collectId);
		int ret = -1;
		
		//SQL文の実行
		// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		ret  = QueryUtil.deleteCollectDataByDateTimeAndCollectorId(
				boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), collectId);
		
		//終了
		m_log.debug("_delete() count : " + ret);
		return ret;
	}

	/**
	 * 監視項目IDのリストから収集項目IDのリストを取得
	 * @param status
	 * @param monitorIdList
	 * @return
	 */
	protected List<Integer> getCollectIds(boolean status, ArrayList<String> monitorIdList) {
		m_log.debug("getCollectIds() start : status = " + status + ", monitorIdList");
		
		//SQL文の実行
		List<Integer> ret = QueryUtil.getCollectoridByMonitorIdList(monitorIdList);
		
		//終了
		m_log.debug("getCollectIds() count : " + ret);
		return ret;
	}
	
	/**
	 * 削除件数を保存する内部クラス
	 */
	private class Counter {
		
		private int count;
		private int errorCode = -1;
		
		public Counter() {
			this.count = 0;
		}
		
		public void countUp(int retRun) {
			this.count += retRun;
		}

		public void setError() {
			this.count = errorCode;
		}

		public int get() {
			return this.count;
		}
	}
}
