package com.clustercontrol.monitor.run.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;


/**
 * ノードからそのノードに指定されている監視項目を引くための辞書クラス
 */
public final class NodeToMonitorCache {
	
	// ------ クラスメソッド・クラス変数 -----------
	
	private static Log m_log = LogFactory.getLog( NodeToMonitorCache.class );

	/**
	 * このクラスのインスタンスの実態を格納するMap。（キーは監視タイプ：例えばProcessなど）
	 */
	private static final ConcurrentMap<String, NodeToMonitorCache> instances = new ConcurrentHashMap<>();
	
	/**
	 * 各モニタタイプIDごとに作られている、ノードから監視設定を引くための辞書インスタンスを取得する
	 * 
	 * @param monitorTypeId 対象のモニタタイプID（例：HinemosModuleConstant.MONITOR_PROCESS）
	 * @return ノードから監視を引くための辞書インスタンス
	 */
	public static NodeToMonitorCache getInstance(final String monitorTypeId) {
		final NodeToMonitorCache cachedInstance = instances.get(monitorTypeId);
		if (cachedInstance != null) {
			return cachedInstance;
		}
		synchronized (instanceAddLock) {
			final NodeToMonitorCache newCache = new NodeToMonitorCache(monitorTypeId);
			final NodeToMonitorCache prevCache = instances.putIfAbsent(monitorTypeId, newCache);
			if (prevCache != null) {
				return prevCache;
			} else {
				return newCache;
			}
		}
	}
	
	public static void refreshAll() {
		synchronized (instanceAddLock) {
			for (NodeToMonitorCache target : instances.values()) {
				target.refresh();
			}
		}
	}
	
	private static final Object instanceAddLock = new Object();
	
	// ------ インスタンスメソッド・インスタンス変数 -----------
	
	/**
	 * 特定の監視設定タイプごとに、そのノードに対する監視設定の逆引きインスタンスを作成する。
	 * @param monitorTypeId 対象となる監視設定タイプ （例：HinemosModuleConstant.MONITOR_PROCESS）
	 */
	private NodeToMonitorCache(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}
	
	/**
	 * この辞書インスタンスの対象となるモニタタイプID
	 */
	private final String monitorTypeId;
	
	private final Object lockCacheReadWrite = new Object();
	
	private volatile ConcurrentMap<String, ConcurrentMap<Integer, Set<MonitorInfo>>> node2intervalMonitorCache = null;
	
	private final ConcurrentLinkedQueue<Long> refreshPlanningQueue = new ConcurrentLinkedQueue<>();
	private final static long lazyRefreshMilisec = 5000L;
	
	/**
	 * NodeからMonitorを引くための構造を構築する。
	 * 
	 * 本関数は、スコープ構造が更新されたとき、監視項目が更新されたときに呼び出す必要がある。
	 */
	public void refresh() {
		if (m_log.isDebugEnabled()) {
			m_log.debug("refresh() : start. monitorTypeId = " + monitorTypeId);
		}
		// Utility等により設定変更を連続投入する場合に備え、設定変更時には即座に
		// 計算は行わず、設定変更後の初回参照時に遅延計算する
		
		synchronized (lockCacheReadWrite) {
			// 直近N秒にNode→MonitorInfoのツリー再編成が計画されていなければ
			// N秒後にツリー再編成の計画を立てる。
			// 監視は、監視設定変更やノード構成変更やカレンダ状態遷移などに、
			// 100％リアルタイムに追随する必要性は無いため、
			// 数秒のディレイをはさむことで、Utility等により連続した
			// 設定変更を行うさいの負荷軽減を見込んだ処理
			if (refreshPlanningQueue.size() == 0) {
				refreshPlanningQueue.add(Long.valueOf(HinemosTime.currentTimeMillis() + lazyRefreshMilisec));
				if (m_log.isDebugEnabled())
					m_log.debug("refresh() : planned to rebuild map. monitorTypeId = " + monitorTypeId);
			} else {
				// キューに1個以上値がある時は既にリフレッシュ計画済みなので何もしない
				if (m_log.isDebugEnabled())
					m_log.debug("refresh() : skipped plan to rebuild map. monitorTypeId = " + monitorTypeId);
			}
		}
		if (m_log.isDebugEnabled()) {
			m_log.debug("refresh() : end. monitorTypeId = " + monitorTypeId);
		}
	}
	
	private void refreshNow() {
		node2intervalMonitorCache = null;
	}
	
	private ConcurrentMap<String, ConcurrentMap<Integer, Set<MonitorInfo>>> buildCurrentNode2IntervalMonitor() throws HinemosUnknown {
		if (m_log.isDebugEnabled())
			m_log.debug("buildCurrentNode2IntervalMonitor() : build start. monitorTypeId = " + monitorTypeId);
		
		final int tryCount = 5;
		final int retrySleepMillis = 100;
		for (int i = 0; i < tryCount; i++) {
			JpaTransactionManager jtm = null;
			
			try {
				final RepositoryControllerBean repository = new RepositoryControllerBean();
				jtm = new JpaTransactionManager();
				jtm.begin();
				
				// 全監視項目を列挙し、各監視項目の対象ノードを調べ、Node→監視間隔→監視項目の逆引き構造を作る
				final ConcurrentMap<String, ConcurrentMap<Integer, Set<MonitorInfo>>> node2intervalMonitor = new ConcurrentHashMap<>();
				for (final MonitorInfo monitor : QueryUtil.getMonitorInfoByMonitorTypeId(monitorTypeId)) {
					for (final String targetNode : repository.getExecTargetFacilityIdList(monitor.getFacilityId(), monitor.getOwnerRoleId())) {
						ConcurrentMap<Integer, Set<MonitorInfo>> interval2monitor = node2intervalMonitor.get(targetNode);
						if (interval2monitor == null) {
							interval2monitor = new ConcurrentHashMap<>();
							node2intervalMonitor.put(targetNode, interval2monitor);
							for (int interval : RunInterval.intValues()) {
								interval2monitor.put(interval, new HashSet<MonitorInfo>());
							}
						}
						
						// 監視か収集が有効のものだけ追加する
						if (monitor.getMonitorFlg() || monitor.getCollectorFlg()) {
							interval2monitor.get(monitor.getRunInterval()).add(monitor);
						}
					}
				}
				jtm.commit();
				// 構築した構造体の内容を全出力する（デバッグ時）
				if (m_log.isDebugEnabled()) {
					final StringBuilder sb = new StringBuilder();
					sb.append("buildCurrentNode2IntervalMonitor() : build complete. monitorTypeId = ").append(monitorTypeId).append(", map : [");
					for (Map.Entry<String, ConcurrentMap<Integer, Set<MonitorInfo>>> entry : node2intervalMonitor.entrySet()) {
						String facilityId = entry.getKey();
						sb.append("{ facilityId : ").append(facilityId).append(", interval&monitor : [ ");
						for (Map.Entry<Integer, Set<MonitorInfo>> interval2monitor : entry.getValue().entrySet()) {
							int interval = interval2monitor.getKey();
							sb.append("{ interval : ").append(interval).append(", monitor : [ ");
							for (MonitorInfo monitor : interval2monitor.getValue()) {
								sb.append(monitor.getMonitorId()).append(", ");
							}
							sb.append(" ] }, ");
						}
						sb.append(" ] }, ");
					}
					sb.append(" ]");
					m_log.debug(sb);
				}
				return node2intervalMonitor;
			} catch (HinemosUnknown e) {
				if (jtm != null)
					jtm.rollback();
				m_log.info("buildCurrentNode2IntervalMonitor() : build failed. monitorTypeId = " + monitorTypeId + ", build retry (" + (tryCount - i) + "/" + tryCount + ")");
			} finally {
				if (jtm != null)
					jtm.close();
			}
			try {
				Thread.sleep(retrySleepMillis);
			} catch (InterruptedException e) {
				throw new HinemosUnknown("buildCurrentNode2IntervalMonitor() : failed to build [Node -> Interval -> Monitor] Map. monitorTypeId = " + monitorTypeId, e);
			}
		}
		throw new HinemosUnknown("buildCurrentNode2Monitor() : failed to build [Node -> Monitor] Map. monitorTypeId = " + monitorTypeId);
	}
		
	/**
	 * 指定したFacilityIdに対して、指定された実行間隔で実行対象となるに監視項目の情報を返す。
	 * 
	 * @param facilityId 対象ノードのFacilityId
	 * @param intervals 対象となる実行間隔のリスト
	 * @return 実行対象の監視項目のリスト。本Listはアクセス最中に発生した設定変更内容などが反映されることはない。また、変更不可である。
	 * @throws HinemosUnknown 
	 */
	public Map<Integer, Set<MonitorInfo>> getMonitors(String facilityId, Set<Integer> intervalSecSet) throws HinemosUnknown {
		
		ConcurrentMap<Integer, Set<MonitorInfo>> interval2monitor = null;
		
		synchronized (lockCacheReadWrite) {
			Long nextRefreshTime = refreshPlanningQueue.peek();
			
			// 初期化未実施、または遅延リフレッシュの時間を超えている場合、Node→Monitorのデータ構造を再構築する
			if (node2intervalMonitorCache == null || (nextRefreshTime != null && nextRefreshTime.longValue() <= HinemosTime.currentTimeMillis())) {
				node2intervalMonitorCache = buildCurrentNode2IntervalMonitor();
				
				// 処理済みの計画を削除する。（通常はQueue.pollで十分だが、念のためキュー溢れを防ぐため全クリアする）
				refreshPlanningQueue.clear();
			}
			if (node2intervalMonitorCache.containsKey(facilityId)) {
				// 後からカレンダが非稼動のものを削除するためDeepCopyする
				interval2monitor = new ConcurrentHashMap<Integer, Set<MonitorInfo>>();
				for (Map.Entry<Integer, Set<MonitorInfo>> entry : node2intervalMonitorCache.get(facilityId).entrySet()) {
					interval2monitor.put(entry.getKey(), new HashSet<MonitorInfo>(entry.getValue()));
				}
			}
		}
		
		// 引数で指定されたFacilityIdの情報がそもそも無い場合、空のMapを返す
		if (interval2monitor == null) {
			return Collections.<Integer, Set<MonitorInfo>>emptyMap();
		}
		
		final Map<Integer, Set<MonitorInfo>> returnMap = new HashMap<>();
		
		// 引数で指定された監視間隔の監視項目のみを返す
		for (int interval : intervalSecSet) {
			returnMap.put(interval, interval2monitor.get(interval));
		}
		return returnMap;
	}
	
	public Map<Integer, Set<MonitorInfo>> getMonitorsWithCalendar(String facilityId, Set<Integer> intervalSecSet) throws HinemosUnknown {
		final Map<Integer, Set<MonitorInfo>> returnMap = getMonitors(facilityId, intervalSecSet);
		final CalendarControllerBean calendar = new CalendarControllerBean();
		
		for (int i = 0; i < 5 ; i++) {
			boolean needRetryFlag = false;
			
			// 各MonitorInfoのカレンダを調べ、今実行対象でない場合にはリストから消去する
			for (Set<MonitorInfo> monitors : returnMap.values()) {
				Set<MonitorInfo> removeMonitors = new HashSet<>();
				for (final MonitorInfo monitor : monitors ) {
					final String calendarId = monitor.getCalendarId();
					try {
						if (calendar.isRun(calendarId, HinemosTime.currentTimeMillis()).booleanValue() == false) {
							// カレンダの実行期間外の場合、返却する対象から削除
							removeMonitors.add(monitor);
						}
					} catch (CalendarNotFound e) {
						// MonitorInfoを取得してからカレンダの状態を見るまでの間に、他スレッドにより
						// カレンダの変更が行われた場合、カレンダが見つからない可能性があるので
						// この場合は監視項目情報を再取得する
						needRetryFlag = true;
						if (m_log.isDebugEnabled())
							m_log.debug("getMonitorsWithCalendar() : Calendar check failed becauseof CalendarNotFound. Cache clear and try again. monitorId = " + monitor.getMonitorId());
						break;
					} catch (InvalidRole e) {
						// カレンダを見ることができない場合、実行対象から抜く
						removeMonitors.add(monitor);
						m_log.info("getMonitorsWithCalendar() : Calendar check failed becausof InvalidRole. Skip monitor. monitorId = " + monitor.getMonitorId());
					} catch (Exception e) {
						throw new HinemosUnknown("getMonitorsWithCalendar() : " + e.getMessage(), e);
					}
				}
				monitors.removeAll(removeMonitors);
			}
			
			if (needRetryFlag == false) {
				// 上のループ内で特に問題なく全探索が完了した時点で返却する
				
				// monitorが存在しないintervalSecを削除する
				List<Integer> removeList = new ArrayList<>();
				for (Map.Entry<Integer, Set<MonitorInfo>> entry : returnMap.entrySet()) {
					if (entry.getValue().size() == 0) {
						removeList.add(entry.getKey());
					}
				}
				for (Integer interval : removeList) {
					returnMap.remove(interval);
				}
				
				return returnMap;
			} else {
				// 監視項目を完全に再構築するため、キャッシュを強制削除した上で
				// 再度getMonitorsを呼び出す
				refreshNow();
				// スピンしないようにリトライ前に若干待つ。
				// (本質的にはsleepは不要だが、待ったほうが成功するのではという期待もこめて・・・）
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new HinemosUnknown("", e);
				}
			}
		}
		
		// リトライ回数内で作成できなかった場合、例外を上げる
		throw new HinemosUnknown();
	}

}
