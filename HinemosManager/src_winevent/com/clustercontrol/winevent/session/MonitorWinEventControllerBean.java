/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorOnAgentUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.winevent.bean.WinEventResultDTO;
import com.clustercontrol.winevent.factory.RunMonitorWinEventString;

/**
 * 
 * @since 4.1
 */
public class MonitorWinEventControllerBean {

	private static Log m_log = LogFactory.getLog( MonitorWinEventControllerBean.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorWinEventControllerBean.class.getName());
		
		try {
			_lock.writeLock();
			
			ArrayList<MonitorInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				refreshCache();
			}
		} finally {
			_lock.writeUnlock();
			m_log.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : " + (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<MonitorInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_WINEVENT);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_WINEVENT + " : " + cache);
		return cache == null ? null : (ArrayList<MonitorInfo>)cache;
	}
	
	private static void storeCache(ArrayList<MonitorInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_WINEVENT + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_WINEVENT, newCache);
	}
	
	/**
	 * Windowsイベント監視一覧リストを返します。
	 * 
	 * 
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getWinEventList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectMonitor().getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_WINEVENT);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getWinEventList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	public static void refreshCache() {
		m_log.info("refreshCache()");
		
		long startTime = HinemosTime.currentTimeMillis();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();
			
			em.clear();
			ArrayList<MonitorInfo> winEventCache = new MonitorWinEventControllerBean().getWinEventList();
			storeCache(winEventCache);
			
			m_log.info("refresh winEventCache " + (HinemosTime.currentTimeMillis() - startTime) +
					"ms. size=" + winEventCache.size());
		} catch (Exception e) {
			m_log.warn("failed refreshing cache.", e);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * 
	 * 要求してきたエージェントのノードに設定されたWindowsイベント監視設定を返す
	 * 
	 * 更新処理実行が並行して動作している場合、更新前・後のいずれの情報が取得できるかは保証されない。
	 * (パーシャル特性がない = 全置換えのキャッシュの更新特性、多重実行時の排他制御のコスト観点からreadLockは意図的に取得しない)
	 * 
	 * @param requestedFacilityId エージェントが対応するノードのfacilityId
	 * @return Windowsイベント監視設定の一覧
	 * @throws InvalidRole
	 * @throws HinemosUnknown 予期せぬエラーが発生した場合
	 * 
	 */
	public ArrayList<MonitorInfo> getWinEventList(String requestedFacilityId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			{
				// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
				// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
				ArrayList<MonitorInfo> monitorList = getCache();
				
				for (MonitorInfo monitorInfo : monitorList) {
					String scope = monitorInfo.getFacilityId();
					if (new RepositoryControllerBean().containsFacilityIdWithoutList(scope, requestedFacilityId, monitorInfo.getOwnerRoleId())) {
						String calendarId = monitorInfo.getCalendarId();
						if(calendarId != null){
							CalendarInfo calendar = new CalendarControllerBean().getCalendarFull(calendarId);
							monitorInfo.setCalendar(calendar);
						}
						list.add(monitorInfo);
					}
				}
			}

			jtm.commit();
		} catch (InvalidRole | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getWinEventList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}
	
	/**
	 * Windowsイベント監視結果を通知する.
	 * 
	 * @param results ログファイル監視結果のリスト
	 * @throws HinemosUnknown 
	 */
	public void run(String facilityId, List<WinEventResultDTO> results) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();
		List<OutputBasicInfo> notifyInfoList = new ArrayList<>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			if (results != null) {
				for (WinEventResultDTO result : results) {
					if (! MonitorOnAgentUtil.checkFacilityId(facilityId, result.runInstructionInfo, result.monitorInfo)) {
						m_log.debug("skip to run because facilityId is not contained.");
						continue;
					}
					RunMonitorWinEventString runMonitorWinEventString = new RunMonitorWinEventString();
					notifyInfoList.add(runMonitorWinEventString.run(facilityId, result));
					monitorJobEndNodeList.addAll(runMonitorWinEventString.getMonitorJobEndNodeList());
				}
			}

			// 通知設定
			jtm.addCallback(new NotifyCallback(notifyInfoList));

			jtm.commit();
		} catch (HinemosUnknown e) {
			m_log.warn("failed storeing result.", e);
			jtm.rollback();
			
			throw e;
		} finally {
			if (jtm != null)
				jtm.close(this.getClass().getName());
		}

		// 監視ジョブEndNode処理
		try {
			if (monitorJobEndNodeList != null && monitorJobEndNodeList.size() > 0) {
				// 同一キーに対する監視ジョブの終了処理は初回以外意味がないため、ここで重複を排除する
				Map<String, MonitorJobEndNode> monitorJobEndNodeMap = new LinkedHashMap<>();

				for (MonitorJobEndNode monitorJobEndNode : monitorJobEndNodeList) {
					String monitorJobKey = MonitorJobWorker.getKey(monitorJobEndNode.getRunInstructionInfo());
					if (!monitorJobEndNodeMap.containsKey(monitorJobKey)) {
						m_log.debug("run() : endMonitorJob target=" + monitorJobKey);
						monitorJobEndNodeMap.put(monitorJobKey, monitorJobEndNode);
					}
				}
				
				for (MonitorJobEndNode monitorJobEndNode : monitorJobEndNodeMap.values()) {
					MonitorJobWorker.endMonitorJob(
							monitorJobEndNode.getRunInstructionInfo(),
							monitorJobEndNode.getMonitorTypeId(),
							monitorJobEndNode.getMessage(),
							monitorJobEndNode.getErrorMessage(),
							monitorJobEndNode.getStatus(),
							monitorJobEndNode.getEndValue());
				}
			}
		} catch (Exception e) {
			m_log.warn("run() MonitorJobWorker.endMonitorJob() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	
}
