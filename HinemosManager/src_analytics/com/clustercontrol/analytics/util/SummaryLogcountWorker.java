/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.analytics.factory.RunMonitorLogcount;
import com.clustercontrol.analytics.factory.SummaryLogcountCollectData;
import com.clustercontrol.analytics.factory.SummaryLogcountCollectData.LogcountCollectData;
import com.clustercontrol.analytics.model.LogcountCheckInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.bean.StringQueryResult;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * HinemosManager上で過去分ログ件数集計を行うクラス<BR>
 */
public class SummaryLogcountWorker {

	// Logger
	private static Log m_log = LogFactory.getLog(SummaryLogcountWorker.class);

	private static ExecutorService service;
	private static String workerName = "LogcountWorker";

	/** 検索タイムアウト **/
	private static Integer collectLogcountTimeout = 0;

	static {
		int maxThreadPoolSize = HinemosPropertyCommon.collect_logcount_thread_pool_size.getIntegerValue();
		service = new MonitoredThreadPoolExecutor(maxThreadPoolSize, maxThreadPoolSize,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, workerName + "-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());

		collectLogcountTimeout = HinemosPropertyCommon.collect_logcount_timeout.getIntegerValue();
	}

	/**
	 * ログ件数集計実行処理
	 * 
	 * @param monitorId 監視設定ID
	 * @param startDate 収集開始日時
	 * @param endDate 収集終了日時
	 * @param userId ユーザ情報
	 */
	public static void runLogcount(String monitorId, Long startDate, Long endDate) 
			throws MonitorNotFound, InvalidRole, HinemosUnknown {

		if (monitorId == null || monitorId.isEmpty()) {
			throw new HinemosUnknown("runLogcount() Error : monitorId is empty.");
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		m_log.info("runLogcount() monitorId=" + monitorId
				+ ", startDate=" + startDate
				+ ", endDate=" + endDate);
		try {
			// タスクを実行する
			service.execute(new LogcountTask(monitorId, startDate, endDate));
		} catch(Throwable e) {
			throw new HinemosUnknown("runLogcount() error", e);
		}
	}

	/**
	 * ログ件数集計を実行するスレッドクラス
	 */
	private static class LogcountTask extends Thread {

		// Logger
		static private Log m_log = LogFactory.getLog(LogcountTask.class);

		// 入力情報
		private String m_monitorId = null;
		private Long m_startDate = null;
		private Long m_endDate = null;

		/**
		 * コンストラクタ
		 * 
		 * @param monitorId 監視設定ID
		 * @param startDate 収集開始日時
		 * @param endDate 収集終了日時
		 */
		public LogcountTask(String monitorId, Long startDate, Long endDate) {
			m_monitorId = monitorId;
			m_startDate = startDate;
			m_endDate = endDate;
		}

		/**
		 * ログ件数集計を実行するクラス<BR>
		 */
		@Override
		public void run() {

			// 処理開始日時
			Long operateStartDate = HinemosTime.currentTimeMillis();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
			sdf.setTimeZone(HinemosTime.getTimeZone());
			m_log.debug("run() monitorId=" + this.m_monitorId
					+ ", startDate=" + sdf.format(new Date(this.m_startDate))
					+ ", endDate=" + sdf.format(new Date(this.m_endDate)));

			JpaTransactionManager jtm = null;
			MonitorInfo monitorInfo = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				// 監視情報の取得
				monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(
					m_monitorId, ObjectPrivilegeMode.NONE);
				LogcountCheckInfo logcountCheckInfo = monitorInfo.getLogcountCheckInfo();
				if (logcountCheckInfo == null) {
					throw new MonitorNotFound("run() : logcountCheckInfo is null.");
				}

				// 対象監視設定のオブジェクト権限（Read）がない場合は実行されないこと
				try {
					com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(logcountCheckInfo.getTargetMonitorId(), monitorInfo.getOwnerRoleId());
				} catch (InvalidRole | MonitorNotFound e) {
					throw new HinemosUnknown("It does not have access authority to target monitor info. : monitorId=" + logcountCheckInfo.getTargetMonitorId());
				}

				// ファシリティIDの配下全ての一覧を取得
				// 有効/無効フラグがtrueとなっているファシリティIDを取得する
				ArrayList<String> facilityIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(
						monitorInfo.getFacilityId(), monitorInfo.getOwnerRoleId());
				if (facilityIdList.size() == 0) {
					throw new FacilityNotFound("run()"
							+ " monitorId=" + m_monitorId
							+ " facilityId=" + monitorInfo.getFacilityId());
				}

				long interval = monitorInfo.getRunInterval().longValue() * 1000L;

				// SummaryLogcountCollectDataに渡すstartDateは、収集ログ取得の時間に合わせる
				long toDate = m_startDate - (m_startDate % interval);
				long collectStartDate = toDate;
				long fromDate  = toDate - interval;
		        SummaryLogcountCollectData summaryLogcount = new SummaryLogcountCollectData(monitorInfo, toDate, m_endDate);
		        m_log.debug("m_startDate=" + m_startDate + ", m_endDate=" + m_endDate + ", interval=" + interval);
		        m_log.debug("fromDate=" + fromDate + ", toDate=" + toDate);

				// 収集ログより、監視間隔ごとの集計結果を取得する
				while(toDate <= m_endDate) {
					if (monitorInfo.getCalendarId() == null || monitorInfo.getCalendarId().isEmpty() 
							|| new CalendarControllerBean().isRun(monitorInfo.getCalendarId(), toDate)) {
						for (String facilityId : facilityIdList) {
							StringQueryResult stringQueryResult = new RunMonitorLogcount().summaryLogcount(
									monitorInfo, facilityId, fromDate, toDate, false);
							if (logcountCheckInfo.getTag() == null || logcountCheckInfo.getTag().isEmpty()) {
								// 全て集計
								summaryLogcount.addData(facilityId, "", toDate, stringQueryResult.getCount().doubleValue());
							} else if (stringQueryResult.getTagCountMap() == null) {
								// タグごとに集計(データ未存在)
								continue;
							} else {
								// タグごとに集計(データ存在)
								for (Map.Entry<String, Integer> entry : stringQueryResult.getTagCountMap().entrySet()) {
									summaryLogcount.addData(facilityId, entry.getKey(), toDate, entry.getValue().doubleValue());
								}
							}
						}
					}
					fromDate = toDate;
					toDate = fromDate + interval;
				}

				// 計算結果を取得する
				Map<CollectKeyInfoPK, LinkedList<LogcountCollectData>> summaryDataMap = summaryLogcount.createSummaryDataMap();

				// 登録処理
				int count = CollectDataUtil.replace(monitorInfo.getMonitorId(), summaryDataMap, collectStartDate, m_endDate, collectLogcountTimeout);

				// 通知処理(処理終了)
				AplLogger.put(InternalIdCommon.MON_LOGCOUNT_N_SYS_002, 
						new String[]{m_monitorId}, getOrgMessage(operateStartDate, count, m_startDate, m_endDate));

				// 終了処理
				jtm.commit();

			} catch (HinemosDbTimeout e) {
				// 通知処理(エラー)
				AplLogger.put(InternalIdCommon.MON_LOGCOUNT_N_SYS_003, 
						new String[]{m_monitorId}, getOrgMessage(operateStartDate, 0, m_startDate, m_endDate));
				if (jtm != null) {
					jtm.rollback();
				}
			} catch (MonitorNotFound | FacilityNotFound | CalendarNotFound | InvalidRole | InvalidSetting | HinemosUnknown e) {
				// ログ件数集計実行時に失敗
				m_log.warn("run() error : monitorId=" + this.m_monitorId
						+ ", startDate=" + sdf.format(new Date(this.m_startDate))
						+ ", endDate=" + sdf.format(new Date(this.m_endDate)), e);
				// 通知処理(エラー)
				AplLogger.put(InternalIdCommon.MON_LOGCOUNT_N_SYS_003, 
						new String[]{m_monitorId}, getOrgMessage(operateStartDate, 0, m_startDate, m_endDate));
				
				if (jtm != null) {
					jtm.rollback();
				}
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		}

		/**
		 * オリジナルメッセージを返す
		 * 
		 * @param operateStartDate	処理日時(開始)
		 * @param startDate			収集対象日時(開始)
		 * @param endDate			収集対象日時(終了)
		 * @return オリジナルメッセージ
		 */
		private String getOrgMessage(Long operateStartDate, int count, Long startDate, Long endDate) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
			sdf.setTimeZone(HinemosTime.getTimeZone());
			String[] args = {
					sdf.format(new Date(operateStartDate)),
					sdf.format(new Date(HinemosTime.currentTimeMillis())),
					Integer.toString(count),
					sdf.format(new Date(startDate)),
					sdf.format(new Date(endDate))
			};
			return MessageConstant.MESSAGE_MONITOR_ORGMSG_LOGCOUNT_SUMMARY.getMessage(args);
		}
	}

}
