/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.repository.bean.NodeConfigRunCollectManagerInfo;
import com.clustercontrol.repository.bean.NodeConfigSetting;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfo;
import com.clustercontrol.repository.session.NodeConfigSettingControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

public class NodeConfigRunCollectManager {

	// ログ出力関連.
	private static Log m_log = LogFactory.getLog(NodeConfigRunCollectManager.class);
	private static final String DELIMITER = "() : ";

	// staticフィールド.
	/** 即時実行マップ{@literal <ファシリティID, 即時実行情報>} */
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo>> runCollectMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo>>();

	private static Long cleanInterval = null;

	/** 実行ステータス */
	public static enum RunStatus {
		/**
		 * Managerキャッシュへの登録直後<br>
		 * RUNNING移行後、Agent停止等で中止された場合もこのステータスに戻す.
		 **/
		SETTING,
		/** AgentからGetTopicで設定取得した直後 */
		RUNNNING
	}

	private static boolean initialized = false;

	// 初回起動時、定期的に長期間実行されてない即時実行がないか確認させるスレッドを立ち上げておく.
	// static{}だとHinemosPropertyCommonクラスの初期化で呼ばれてエラーとなるので、初回の即時実行で初期化するようメソッド定義.
	public static void initialize() {
		if (initialized) {
			return;
		}

		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "NodeConfigRunCollectManager");
			}
		});
		cleanInterval = HinemosPropertyCommon.repository_node_config_run_clean_interval.getNumericValue();
		scheduler.scheduleWithFixedDelay( //
				new NodeConfigRunCollectCleanTask(), cleanInterval, cleanInterval, TimeUnit.MINUTES);
		initialized = true;
	}

	/**
	 * 長期間実行されてない即時実行情報がないか確認するタスク.
	 */
	private static class NodeConfigRunCollectCleanTask extends Thread {
		@Override
		public void run() {
			String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
			if (runCollectMap == null || runCollectMap.isEmpty()) {
				m_log.debug(methodName + DELIMITER + "'runCollectMap' is empty.");
				return;
			}

			StringBuilder loginfo = new StringBuilder();
			StringBuilder originMsg = new StringBuilder();
			int removeCount = 0;

			Iterator<Entry<String, ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo>>> parentIterator = runCollectMap
					.entrySet().iterator();
			while (parentIterator.hasNext()) {
				Entry<String, ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo>> runningEntry = parentIterator
						.next();
				String facilityId = runningEntry.getKey();
				if (facilityId == null) {
					m_log.warn(methodName + DELIMITER //
							+ "'facilityId' is null in 'runCollectMap'.");
					parentIterator.remove();
					continue;
				}
				ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo> runningMap = runningEntry.getValue();
				if (runningMap == null || runningMap.isEmpty()) {
					m_log.warn(methodName + DELIMITER //
							+ "'runningMap' is empty in 'runCollectMap'." //
							+ " facilityId=" + facilityId);
					parentIterator.remove();
					continue;
				}

				int beforeMapCount = runningMap.size();
				Iterator<Entry<String, NodeConfigRunCollectManagerInfo>> childIterator = runningMap.entrySet()
						.iterator();
				boolean facilityTop = true;
				int facilityRemove = 0;
				while (childIterator.hasNext()) {
					Entry<String, NodeConfigRunCollectManagerInfo> entry = childIterator.next();
					String settingId = entry.getKey();
					// 古すぎる場合は削除.
					if (isTooOld(facilityId, settingId, false)) {
						if (facilityTop) {
							if (removeCount > 0) {
								loginfo.append(", ");
								originMsg.append("\n");
							}
							loginfo.append(facilityId + " : ");
							originMsg.append(facilityId + " :\n");
						}
						if (facilityRemove > 0) {
							loginfo.append(", ");
							originMsg.append("\n");
						}
						String instructedTime = new Date(entry.getValue().getInstructedDate()).toString();
						loginfo.append("<" + settingId + ", " + instructedTime + ">");
						originMsg.append("    " + settingId + " : " + instructedTime);

						facilityRemove++;
						facilityTop = false;
						childIterator.remove();
					}
				}
				removeCount = removeCount + facilityRemove;
				if (beforeMapCount == facilityRemove) {
					parentIterator.remove();
					m_log.debug(methodName + DELIMITER //
							+ "removed 'parentIterator', because cildren have been empty." //
							+ " facilityId=" + facilityId);
				}
			}

			if (removeCount == 0) {
				m_log.debug(methodName + DELIMITER + "finished to remove, but no target.");
			} else {
				m_log.info(methodName + DELIMITER //
						+ "removed too old information to run collecting configuration of node." //
						+ " count=" + removeCount //
						+ ", targets[ facilityID : <settingID, instructedTime> ]=[ " + loginfo.toString() + " ]");
				// マネージャ通知.
				Long margin = HinemosPropertyCommon.repository_node_config_run_too_old.getNumericValue();
				String[] args = {};
				String[] detailArgs = { margin.toString(), cleanInterval.toString(), originMsg.toString() };
				AplLogger.put(InternalIdCommon.NODE_CONFIG_SETTING_SYS_004, args,
						MessageConstant.MESSAGE_FAILED_TO_GET_NODE_CONFIG_RUN_DETAIL.getMessage(detailArgs)); // オリジナルメッセージ.
			}

		}
	}

	// メソッド.
	/**
	 * 即時実行マップ登録
	 * 
	 * @param facilityId
	 *            ファシリティID
	 * @param instructedDate
	 *            実行指示日時
	 */
	public static List<String> addRunCollectMap(List<String> facilityIdList, Long instructedDate, String settingId) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		if (m_log.isDebugEnabled()) {
			String instructedDateStr = new Date(instructedDate).toString();
			m_log.debug(methodName + DELIMITER + "start." //
					+ " count(facilityID)=[" + facilityIdList.size() + "]"//
					+ ", instructedDate=[" + instructedDateStr + "]");
		}
		StringBuilder sb = new StringBuilder();
		boolean isTop = true;
		List<String> addedList = new ArrayList<String>();
		for (String facilityId : facilityIdList) {

			// 未実行のFacilityID・設定IDの情報は上書きしない.
			if (!isTooOld(facilityId, settingId, true)) {
				m_log.debug(methodName + DELIMITER + "skipped because it is contained." //
						+ " facilityId=[" + facilityId + "]"//
						+ ", settingId=[" + settingId + "]");
				continue;
			}

			if (!isTop) {
				sb.append(", ");
			}
			ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo> runningMap = runCollectMap.get(facilityId);
			if (runningMap == null) {
				runningMap = new ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo>();
			}
			NodeConfigRunCollectManagerInfo runCollectInfo = new NodeConfigRunCollectManagerInfo(instructedDate);
			runningMap.put(settingId, runCollectInfo);
			runCollectMap.put(facilityId, runningMap);
			addedList.add(facilityId);
			sb.append(facilityId);
			isTop = false;
		}
		m_log.debug(methodName + DELIMITER + "added." //
				+ " settingId=[" + settingId + "]"//
				+ ", facilityId=[" + sb.toString() + "]");
		return addedList;
	}

	/**
	 * 保存されている即時実行情報が古過ぎないか判定.
	 * 
	 * @param facilityId
	 *            ファシリティID
	 * @param settingId
	 *            構成情報収集設定ID
	 * @param containNotExist
	 *            存在しない場合に削除対象として含めるか<br>
	 *            true:存在しない場合はreturn=true、false:存在しない場合はreturn=false
	 * @return true:古すぎるので削除対象、false:実行中なので削除対象外
	 */
	private static boolean isTooOld(String facilityId, String settingId, boolean containNotExist) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		long maxWaitMillis = getMaxWaitMillis();

		ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo> runningMap = runCollectMap.get(facilityId);
		if (runningMap == null || runningMap.isEmpty()) {
			return containNotExist;
		}

		NodeConfigRunCollectManagerInfo runnningInfo = runningMap.get(settingId);
		if (runnningInfo == null) {
			return containNotExist;
		}

		Long oldInstructedDate = runnningInfo.getInstructedDate();
		Long currentTime = Long.valueOf(HinemosTime.currentTimeMillis());
		long elapsedMillis = currentTime.longValue() - oldInstructedDate.longValue();

		// 即時実行情報が古過ぎる場合.
		if (elapsedMillis > maxWaitMillis) {
			m_log.info(methodName + DELIMITER + "RunCollectInfo in RunCollectMap is too old." //
					+ ", settingID=" + settingId);
			return true;
		}
		return false;
	}

	/**
	 * Hinemosプロパティから長期間実行なし判定の最大待ち時間を算出.
	 */
	private static long getMaxWaitMillis() {
		Long margin = HinemosPropertyCommon.repository_node_config_run_too_old.getNumericValue();
		Long distribution = getDistributionTime();
		long maxWaitMillis = distribution.longValue() + margin.longValue();
		return maxWaitMillis;
	}

	/**
	 * 負荷分散間隔をHinemosプロパティから取得
	 */
	public static Long getDistributionTime() {
		Long distribution = HinemosPropertyCommon.repository_node_config_run_distribution_time.getNumericValue();
		return distribution;
	}

	/**
	 * 即時実行マップ取得
	 * 
	 * @param facilityId
	 *            ファシリティID
	 * @return {@literal <設定情報, 実行指示日時>}
	 * @throws HinemosUnknown
	 */
	public static HashMap<NodeConfigSetting, Long> getRunCollectMap(String facilityId) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start." //
				+ " facilityId=" + facilityId);
		ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo> runningMap = runCollectMap.get(facilityId);

		// 存在しない場合.
		if (runningMap == null || runningMap.isEmpty()) {
			m_log.debug(
					methodName + DELIMITER + "existed no facilityID in RunCollectMap." + " facilityID=" + facilityId);
			return null;
		}

		HashMap<NodeConfigSetting, Long> returnMap = new HashMap<NodeConfigSetting, Long>();
		ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo> putMap = new ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo>();
		for (Entry<String, NodeConfigRunCollectManagerInfo> entry : runningMap.entrySet()) {
			NodeConfigRunCollectManagerInfo runCollectInfo = entry.getValue();
			String settingId = entry.getKey();
			// nullチェック(ありえない想定).
			if (runCollectInfo == null) {
				m_log.warn(methodName + DELIMITER + "existed no runCollectInfo in RunCollectMap" //
						+ " facilityID=" + facilityId//
						+ ", settingId=" + settingId);
				continue;
			}
			if (runCollectInfo.getRunStatus() == null) {
				m_log.warn(methodName + DELIMITER + "existed no runStatus in RunCollectMap" //
						+ " facilityID=" + facilityId//
						+ ", settingId=" + settingId);
				continue;
			}
			if (runCollectInfo.getInstructedDate() == null) {
				m_log.warn(methodName + DELIMITER + "existed no instructedDate in RunCollectMap" //
						+ " facilityID=" + facilityId//
						+ ", settingId=" + settingId);
				continue;
			}

			// 既にgetTopicが走ってAgent側で実行もしくは待機中の場合はスキップ.
			if (RunStatus.RUNNNING == runCollectInfo.getRunStatus()) {
				m_log.debug(methodName + DELIMITER + "running to collect configuration of node." //
						+ " facilityID=" + facilityId//
						+ ", settingId=" + settingId);
				putMap.put(settingId, runCollectInfo);
				continue;

			}

			// DBから設定IDに紐づく設定情報を取得する.
			NodeConfigSettingInfo settingInfo = null;
			try {
				settingInfo = new NodeConfigSettingControllerBean().getNodeConfigSettingInfo(settingId);
			} catch (NodeConfigSettingNotFound | InvalidRole | HinemosUnknown e) {
				m_log.debug(methodName + DELIMITER + "failed to get setting from DB." //
						+ " facilityID=" + facilityId//
						+ ", settingId=" + settingId);
				continue;
			}
			if (settingInfo == null) {
				continue;
			}
			CalendarInfo calInfo = null;
			if (settingInfo.getCalendarId() != null && !settingInfo.getCalendarId().equals("")) {
				try {
					calInfo = new CalendarControllerBean().getCalendarFull(settingInfo.getCalendarId());
				} catch (CalendarNotFound | InvalidRole | HinemosUnknown e) {
					m_log.debug(methodName + DELIMITER + "failed to get calendar from DB." //
							+ " facilityID=" + facilityId//
							+ ", settingId=" + settingId);
				}
			}
			List<String> itemInfo = new ArrayList<>();
			if (settingInfo.getNodeConfigSettingItemList() != null) {
				for (NodeConfigSettingItemInfo item : settingInfo.getNodeConfigSettingItemList()) {
					itemInfo.add(item.getSettingItemId());
				}
			}

			// Agent向けの情報を設定.
			NodeConfigSetting setting = //
					new NodeConfigSetting( //
							settingInfo.getSettingId(), //
							settingInfo.getSettingName(), //
							facilityId, settingInfo.getRunInterval() * 1000, //
							calInfo, //
							itemInfo, //
							settingInfo.getNodeConfigCustomList(), //
							null, //
							null);
			runCollectInfo.setRunStatus(RunStatus.RUNNNING);
			returnMap.put(setting, runCollectInfo.getInstructedDate());
			putMap.put(settingId, runCollectInfo);
		}

		// ステータスを更新.
		runCollectMap.put(facilityId, putMap);
		return returnMap;
	}

	/**
	 * 即時実行の中止.<br>
	 * <br>
	 * Agentの再起動等で停止する場合、ステータスをRUNNING→SETTINGに戻す.
	 * 
	 * @param facilityId
	 *            ファシリティID
	 */
	public static void stopRunCollect(String facilityId) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo> runningMap = runCollectMap.get(facilityId);
		if (runningMap == null || runningMap.isEmpty()) {
			m_log.debug(
					methodName + DELIMITER + "stop to run, but 'runningMap' is empty." + " facilityID=" + facilityId);
			return;
		}

		ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo> putMap = new ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo>(
				runningMap);
		for (Entry<String, NodeConfigRunCollectManagerInfo> entry : runningMap.entrySet()) {
			NodeConfigRunCollectManagerInfo runCollectInfo = entry.getValue();
			String settingId = entry.getKey();

			if (runCollectInfo == null) {
				m_log.warn(methodName + DELIMITER + "existed no runCollectInfo in RunCollectMap" //
						+ " facilityID=" + facilityId//
						+ ", settingId=" + settingId);
				continue;
			}
			if (runCollectInfo.getRunStatus() == null) {
				m_log.warn(methodName + DELIMITER + "existed no runStatus in RunCollectMap" //
						+ " facilityID=" + facilityId//
						+ ", settingId=" + settingId);
				continue;
			}
			if (RunStatus.SETTING == runCollectInfo.getRunStatus()) {
				m_log.debug(methodName + DELIMITER + "stop to run, but status is 'SETTING'." //
						+ " facilityID=" + facilityId //
						+ " settingID=" + settingId);
				continue;
			}

			// SETTINGに変更.
			runCollectInfo.setRunStatus(RunStatus.SETTING);
			putMap.put(settingId, runCollectInfo);
			m_log.debug(methodName + DELIMITER + "stopped to run collecting configuration of node." //
					+ " facilityID=" + facilityId //
					+ " settingID=" + settingId);
		}

		runCollectMap.put(facilityId, putMap);
	}

	/**
	 * 即時実行の終了.<br>
	 * <br>
	 * マップから削除する.
	 * 
	 * @param facilityId
	 *            ファシリティID
	 */
	public static void endRunCollect(String facilityId, String settingId) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		ConcurrentHashMap<String, NodeConfigRunCollectManagerInfo> runningMap = runCollectMap.get(facilityId);
		if (runningMap == null || runningMap.isEmpty()) {
			m_log.debug(
					methodName + DELIMITER + "stop to run, but 'runningMap' is empty." + " facilityID=" + facilityId);
			return;
		}

		NodeConfigRunCollectManagerInfo runCollectInfo = runningMap.get(settingId);
		// 定期実行の場合はここ、もしくは即時実行待機中に定期実行が先に動いて削除されてしまった場合.
		if (runCollectInfo == null) {
			m_log.info(methodName + DELIMITER + "existed no runCollectInfo in RunCollectMap" //
					+ " facilityID=" + facilityId//
					+ ", settingId=" + settingId);
			return;
		}
		if (runCollectInfo.getRunStatus() == null) {
			m_log.warn(methodName + DELIMITER + "existed no runStatus in RunCollectMap" //
					+ " facilityID=" + facilityId//
					+ ", settingId=" + settingId);
			return;
		}

		// 即時実行処理中に定期実行された場合はここ.
		if (RunStatus.SETTING == runCollectInfo.getRunStatus()) {
			m_log.info(methodName + DELIMITER + "existed no information to run collecting."//
					+ " facilityID=" + facilityId//
					+ ", settingId=" + settingId);
			return;
		}

		// 即時実行中のステータス削除(定期実行で即時実行が同時に動いてるパターンもここだが、即時実行が止まるわけではないのでOK).
		if (RunStatus.RUNNNING == runCollectInfo.getRunStatus()) {
			runningMap.remove(settingId);
			if (runningMap.isEmpty()) {
				runCollectMap.remove(facilityId);
			} else {
				runCollectMap.put(facilityId, runningMap);
			}
			return;
		}

		// ありえない想定.
		m_log.warn(methodName + DELIMITER + "finished to run collecting, but don't removed 'runCollectMap'"//
				+ " facilityID=" + facilityId//
				+ ", settingId=" + settingId);
	}
}
