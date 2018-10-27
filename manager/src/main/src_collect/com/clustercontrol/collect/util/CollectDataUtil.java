/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.analytics.factory.SummaryLogcountCollectData.LogcountCollectData;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.collect.bean.CollectConstant;
import com.clustercontrol.collect.bean.PerfData;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * 性能情報を登録するユーティティクラス<BR>
 *
 * @version 5.1.0
 * @since 5.1.0
 */
public class CollectDataUtil {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(CollectDataUtil.class);

	/** エポック秒を時間に直す際使用するタイムゾーン **/
	private static final long TIMEZONE = HinemosTime.getTimeZoneOffset();

	private static Integer maxId = null;
	private static Object maxLock = new Object();
	private static Map<CollectKeyInfoPK, Integer> collectKeyInfoMap = new ConcurrentHashMap<>();

	/** 集計データのマップ() */
	private static Map<Integer, SummaryInfo> summaryHourInfoMap = new ConcurrentHashMap<>();
	private static Map<Integer, SummaryInfo> summaryDayInfoMap = new ConcurrentHashMap<>();
	private static Map<Integer, SummaryInfo> summaryMonthInfoMap = new ConcurrentHashMap<>();

	static {
		JpaTransactionManager jtm = new JpaTransactionManager();
		if (!jtm.isNestedEm()) {
			m_log.warn("static : transactioin has not been begined.");
			jtm.close();
		} else {
			try {
				// CollectorId最大値取得
				maxId = QueryUtil.getMaxId();
				if (maxId == null) {
					maxId = -1;
				}
				// CollectKeyInfo取得
				List<CollectKeyInfo> collectKeyList = QueryUtil.getCollectKeyInfoAll();
				for (CollectKeyInfo collectKeyInfo : collectKeyList) {
					collectKeyInfoMap.put(collectKeyInfo.getId(), collectKeyInfo.getCollectorid());
				}
			} catch (Exception e) {
				m_log.warn("static() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}
	}

	private static Integer getId(String itemName, String displayName, String monitorId, String facilityId, JpaTransactionManager jtm) {
		synchronized(maxLock) {
			// collectKeyInfo(のcollectorid)を取ってくることができるか確認
			CollectKeyInfoPK collectKeyInfoPK = new CollectKeyInfoPK(itemName, displayName, monitorId, facilityId);
			if (collectKeyInfoMap.containsKey(collectKeyInfoPK)) {
				return collectKeyInfoMap.get(collectKeyInfoPK);
			}
			// collectorIdが存在しなかった場合は新たに作る
			maxId++;
			Integer collectorId = maxId;
			try {
				HinemosEntityManager em = jtm.getEntityManager();
				em.persist(new CollectKeyInfo(itemName, displayName, monitorId, facilityId, collectorId));
				em.flush();
				collectKeyInfoMap.put(collectKeyInfoPK, collectorId);
				return collectorId;
			} catch (Exception e1) {
				m_log.warn("put() : " + e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
				if (jtm != null) {
					jtm.rollback();
				}
			}
		}
		m_log.warn("getId : error");
		return null;
	}

	/**
	 * ジョブ履歴(ジョブ)を性能情報を登録するために Queue に 1件 put する
	 * 
	 * @param sessionJob　	ジョブ履歴(ジョブ)
	 */
	public static void put(JobSessionJobEntity sessionJob) {
		if (sessionJob == null
				|| sessionJob.getId() == null
				|| sessionJob.getId().getJobunitId() == null 
				|| sessionJob.getId().getJobunitId().isEmpty()
				|| sessionJob.getId().getJobId() == null 
				|| sessionJob.getId().getJobId().isEmpty()
				|| sessionJob.getStartDate() == null
				|| sessionJob.getEndDate() == null) {
			// 値が設定されていないため処理終了
			String strParam = "";
			if (sessionJob != null) {
				strParam = String.format(" jobunitId=%s, jobId=%s", 
						sessionJob.getId().getJobunitId(), sessionJob.getId().getJobId());
			}
			m_log.warn("put(sessionJob) : Information is insufficient. " + strParam);
			return;
		}

		// 対象（ジョブセッション、ジョブネット、参照ジョブネット）以外は処理終了
		if (sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBUNIT
				&& sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOBNET
				&& sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_REFERJOBNET) {
			return;
		}

		// 対象ステータス以外は処理終了
		if (sessionJob.getStatus() != StatusConstant.TYPE_END
				&& sessionJob.getStatus() != StatusConstant.TYPE_MODIFIED
				&& sessionJob.getStatus() != StatusConstant.TYPE_END_END_DELAY) {
			return;
		}

		// JobMstに存在しないジョブは対象外
		try {
			com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstPK_NONE(
					new JobMstEntityPK(sessionJob.getId().getJobunitId(), sessionJob.getId().getJobId()));
		} catch (JobMasterNotFound e) {
			m_log.debug("put(sessionJob) : jobMst is not found. "
					+ "jobunitId=" + sessionJob.getId().getJobunitId()
					+ ", jobId=" + sessionJob.getId().getJobId());
			return;
		}

		List<Sample> sampleList = new ArrayList<>();
		Sample sample = new Sample(new Date(sessionJob.getJobSessionEntity().getScheduleDate()), CollectConstant.COLLECT_TYPE_JOB);
		sample.set(
				RoleSettingTreeConstant.ROOT_ID, 
				MessageConstant.COLLECT_TYPE_JOB_EXECUTION_HISTORY.getMessage(), 
				Double.valueOf((double)((sessionJob.getEndDate() - sessionJob.getStartDate())/1000D)), 
				CollectedDataErrorTypeConstant.NOT_ERROR, 
				sessionJob.getId().getJobunitId() + CollectConstant.COLLECT_TYPE_JOB_DELIMITER + sessionJob.getId().getJobId());
		sampleList.add(sample);
		put(sampleList);
	}

	/**
	 * ジョブ履歴(ノード)を性能情報を登録するために Queue に 1件 put する
	 * 
	 * @param sessionNode　	ジョブ履歴(ノード)
	 */
	public static void put(JobSessionNodeEntity sessionNode) {
		if (sessionNode == null
				|| sessionNode.getId() == null
				|| sessionNode.getId().getJobunitId() == null 
				|| sessionNode.getId().getJobunitId().isEmpty()
				|| sessionNode.getId().getJobId() == null 
				|| sessionNode.getId().getJobId().isEmpty()
				|| sessionNode.getId().getFacilityId() == null 
				|| sessionNode.getId().getFacilityId().isEmpty()
				|| sessionNode.getStartDate() == null
				|| sessionNode.getEndDate() == null) {
			// 値が設定されていないため処理終了
			String strParam = "";
			if (sessionNode != null) {
				strParam = String.format(" jobunitId=%s, jobId=%s, facilityId=%s", 
						sessionNode.getId().getJobunitId(), sessionNode.getId().getJobId(), sessionNode.getId().getFacilityId());
			}
			m_log.warn("put(sessionNode) : Information is insufficient. " + strParam);
			return;
		}

		// 対象（コマンドジョブ、ファイル転送ジョブ、参照ジョブ、監視ジョブ）以外は処理終了
		JobSessionJobEntity sessionJob = sessionNode.getJobSessionJobEntity();
		if (sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_JOB
				&& sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_REFERJOB
				&& sessionJob.getJobInfoEntity().getJobType() != JobConstant.TYPE_MONITORJOB) {
			return;
		}

		// 対象ステータス以外は処理終了
		if (sessionNode.getStatus() != StatusConstant.TYPE_END
				&& sessionNode.getStatus() != StatusConstant.TYPE_MODIFIED
				&& sessionNode.getStatus() != StatusConstant.TYPE_END_END_DELAY) {
			return;
		}

		// JobMstに存在しないジョブは対象外
		try {
			com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstPK_NONE(
					new JobMstEntityPK(sessionNode.getId().getJobunitId(), sessionNode.getId().getJobId()));
		} catch (JobMasterNotFound e) {
			m_log.debug("put(sessionNode) : jobMst is not found. "
					+ "jobunitId=" + sessionJob.getId().getJobunitId()
					+ ", jobId=" + sessionJob.getId().getJobId());
			return;
		}

		List<Sample> sampleList = new ArrayList<>();
		Sample sample = new Sample(new Date(sessionJob.getJobSessionEntity().getScheduleDate()), CollectConstant.COLLECT_TYPE_JOB);
		sample.set(
				sessionNode.getId().getFacilityId(), 
				MessageConstant.COLLECT_TYPE_JOB_EXECUTION_HISTORY_NODE.getMessage(), 
				Double.valueOf((double)((sessionNode.getEndDate() - sessionNode.getStartDate())/1000D)), 
				CollectedDataErrorTypeConstant.NOT_ERROR, 
				sessionNode.getId().getJobunitId() + CollectConstant.COLLECT_TYPE_JOB_DELIMITER + sessionNode.getId().getJobId());
		sampleList.add(sample);
		put(sampleList);
	}

	/**
	 * 性能情報を登録するために Queue に put する
	 *
	 * @param sample
	 *            性能情報
	 * @throws HinemosUnknown
	 * @throws Exception
	 */
	public static void put(List<Sample> sampleList) {
		m_log.debug("put() start");

		List<CollectData> collectdata_entities = new ArrayList<CollectData>();
		List<CollectData> collectdata_job_entities = new ArrayList<CollectData>();
		List<SummaryHour> summaryhour_entities = new ArrayList<SummaryHour>();
		List<SummaryDay> summaryday_entities = new ArrayList<SummaryDay>();
		List<SummaryMonth> summarymonth_entities = new ArrayList<SummaryMonth>();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();

			for (Sample sample : sampleList) {
				// for debug
				if (m_log.isDebugEnabled()) {
					m_log.debug("put() dateTime = " + sample.getDateTime());
					ArrayList<PerfData> list = sample.getPerfDataList();
					for (PerfData data : list) {
						m_log.info("put() list facilityId = " + data.getFacilityId() + ", value = " + data.getValue());
					}
				}
				
				ArrayList<PerfData> list = sample.getPerfDataList();
				String monitorId = sample.getMonitorId();
				
				Long time = HinemosTime.currentTimeMillis();
				if (sample.getDateTime() != null) {
					time = sample.getDateTime().getTime();
				}
				for (PerfData data : list) {
					m_log.debug("persist itemCode = " + data.getItemName());
					String itemName = data.getItemName();
					String facilityId = data.getFacilityId();
					String displayName = data.getDisplayName();
					SummaryInfo summaryHour_c = null;
					SummaryInfo summaryDay_c = null;
					SummaryInfo summaryMonth_c = null;

					Integer collectorid = getId(itemName, displayName, monitorId, facilityId, jtm);
					CollectDataPK pk = new CollectDataPK(collectorid, time);
					Float value = null;
					if(data.getValue() != null){
						value = Float.parseFloat(data.getValue().toString());
					}
					Float average = null;
					if(data.getAverage() != null){
						average = Float.parseFloat(data.getAverage().toString());
					}
					Float standardDeviation = null;
					if(data.getStandardDeviation() != null){
						standardDeviation = Float.parseFloat(data.getStandardDeviation().toString());
					}
					if (monitorId.equals(CollectConstant.COLLECT_TYPE_JOB)) {
						// ジョブ履歴の場合
						collectdata_job_entities.add(new CollectData(pk, value));
					} else {
						// ジョブ履歴以外の場合
						collectdata_entities.add(new CollectData(pk, value, average, standardDeviation));
						//　収集データキャッシュにデータが存在する場合は追加する(監視のみ)
						if (MonitorCollectDataCache.getMonitorCollectDataInfo(monitorId, facilityId, displayName, itemName) != null) {
							MonitorCollectDataCache.add(
									monitorId, 
									facilityId, 
									displayName,
									itemName,
									time, 
									value);
						}
					}

					// Summary関連の計算を行う
					// エポック秒を時間で切り捨てて再計算
					Long hour = (time + TIMEZONE) / 1000 / 3600 * 3600 * 1000 - TIMEZONE;
					// エポック秒を日単位で切り捨てて計算
					Long day = (time + TIMEZONE) / 1000 / 3600 / 24 * 24 * 3600 * 1000 - TIMEZONE;
					// エポック秒から年月を取得
					Calendar calendar = HinemosTime.getCalendarInstance();
					calendar.setTimeInMillis(time);
					int y = calendar.get(Calendar.YEAR);
					int m = calendar.get(Calendar.MONTH);
					calendar.clear();
					calendar.set(y, m, 1);
					Long month = calendar.getTimeInMillis();
					
					// SummaryHour
					CollectDataPK pk_h = new CollectDataPK(collectorid, hour);
					SummaryInfo summaryHour = getSummaryHourInfo(pk_h);
					summaryHour_c = getNewSummaryInfo(summaryHour, pk_h, value, average, standardDeviation);

					// SummaryDay
					CollectDataPK pk_d = new CollectDataPK(collectorid, day);
					SummaryInfo summaryDay = getSummaryDayInfo(pk_d);
					summaryDay_c = getNewSummaryInfo(summaryDay, pk_d, value, average, standardDeviation);

					// SummaryMonth
					CollectDataPK pk_m = new CollectDataPK(collectorid, month);
					SummaryInfo summaryMonth = getSummaryMonthInfo(pk_m);
					summaryMonth_c = getNewSummaryInfo(summaryMonth, pk_m, value, average, standardDeviation);

					// 各Summaryについて、追加するデータの精査を行う 
					if (summaryHour_c != null) {
						summaryhour_entities.add(
								new SummaryHour(
								new CollectDataPK(summaryHour_c.getCollectorid(), summaryHour_c.getTime()),
								summaryHour_c.getAvg(),
								summaryHour_c.getMin(),
								summaryHour_c.getMax(),
								summaryHour_c.getCount(),
								summaryHour_c.getAverageAvg(),
								summaryHour_c.getAverageCount(),
								summaryHour_c.getStandardDeviationAvg(),
								summaryHour_c.getStandardDeviationCount()));
						// map追加
						summaryHourInfoMap.put(summaryHour_c.getCollectorid(), summaryHour_c);
					}
					if (summaryDay_c != null) {
						summaryday_entities.add(
								new SummaryDay(
								new CollectDataPK(summaryDay_c.getCollectorid(), summaryDay_c.getTime()),
								summaryDay_c.getAvg(),
								summaryDay_c.getMin(),
								summaryDay_c.getMax(),
								summaryDay_c.getCount(),
								summaryDay_c.getAverageAvg(),
								summaryDay_c.getAverageCount(),
								summaryDay_c.getStandardDeviationAvg(),
								summaryDay_c.getStandardDeviationCount()));
						// map追加
						summaryDayInfoMap.put(summaryDay_c.getCollectorid(), summaryDay_c);
					}
					if (summaryMonth_c != null) {
						summarymonth_entities.add(
								new SummaryMonth(
								new CollectDataPK(summaryMonth_c.getCollectorid(), summaryMonth_c.getTime()),
								summaryMonth_c.getAvg(),
								summaryMonth_c.getMin(),
								summaryMonth_c.getMax(),
								summaryMonth_c.getCount(),
								summaryMonth_c.getAverageAvg(),
								summaryMonth_c.getAverageCount(),
								summaryMonth_c.getStandardDeviationAvg(),
								summaryMonth_c.getStandardDeviationCount()));
						// map追加
						summaryMonthInfoMap.put(summaryMonth_c.getCollectorid(), summaryMonth_c);
					}
				}
				m_log.debug(
						"insert() end : dateTime = " + sample.getDateTime());
			}
			jtm.commit();
			List<JdbcBatchQuery> query = new ArrayList<JdbcBatchQuery>();
			// データを更新(挿入)する
			if(!collectdata_entities.isEmpty()){
				query.add(new CollectDataJdbcBatchInsert(collectdata_entities));
			}
			if(!collectdata_job_entities.isEmpty()){
				// ジョブ履歴（セッションID登録）
				query.add(new CollectDataJdbcBatchUpsert(collectdata_job_entities));
			}
			if(!summaryhour_entities.isEmpty()){
				query.add(new SummaryHourJdbcBatchUpsert(summaryhour_entities));
			}
			if(!summaryday_entities.isEmpty()){
				query.add(new SummaryDayJdbcBatchUpsert(summaryday_entities));
			}
			if(!summarymonth_entities.isEmpty()){
				query.add(new SummaryMonthJdbcBatchUpsert(summarymonth_entities));
			}
			JdbcBatchExecutor.execute(query);
		}
		m_log.debug("put() end");
	}

	/**
	 * 性能情報を登録するために Queue に put する。(過去分のログ件数集計で使用する。)
	 * ただし、集計データは計算をしなおす。
	 *
	 * @param monitorId  監視項目ID
	 * @param summaryDataMap 性能情報
	 * @param fromDate 開始日時
	 * @param toDate 終了日時
	 * @param timeout 検索タイムアウト
	 * @return cc_collect_data_rawに登録した件数
	 * @throws HinemosDbTimeout
	 */
	public static int replace(String monitorId, Map<CollectKeyInfoPK, LinkedList<LogcountCollectData>> summaryDataMap, Long fromDate, Long toDate, Integer timeout)
			throws HinemosDbTimeout {
		m_log.debug("replace() start");

		List<CollectData> collectdata_entities = new ArrayList<CollectData>();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();

			// 削除対象キー取得
			List<CollectKeyInfo> deleteCollectKeyList = QueryUtil.getCollectKeyInfoListByMonitorId(monitorId);

			// 集計データ保持用マップ
			Map<CollectDataPK, SummaryInfo> summaryhour_entitiesMap = new HashMap<>();
			Map<CollectDataPK, SummaryInfo> summaryday_entitiesMap = new HashMap<>();
			Map<CollectDataPK, SummaryInfo> summarymonth_entitiesMap = new HashMap<>();

			// 更新データ作成
			for (Map.Entry<CollectKeyInfoPK, LinkedList<LogcountCollectData>> entry : summaryDataMap.entrySet()) {
				m_log.info("replace() collectKeyInfoPK=" + entry.getKey());

				// 指定された監視設定ID以外は対象外
				if (!monitorId.equals(entry.getKey().getMonitorId())) {
					continue;
				}

				// キー
				String itemName = entry.getKey().getItemName();
				String facilityId = entry.getKey().getFacilityid();
				String displayName = entry.getKey().getDisplayName();
				Integer collectorid = getId(itemName, displayName, monitorId, facilityId, jtm);

				// 日時の昇順にソートする
				Comparator<LogcountCollectData> comparator = new Comparator<LogcountCollectData>() {
					@Override
					public int compare(LogcountCollectData o1, LogcountCollectData o2) {
						return o1.getTime().compareTo(o2.getTime());
					}
				};
				entry.getValue().sort(comparator);

				boolean isFirst = true;
				Long time = null;
				CollectDataPK pk_h = null;
				CollectDataPK pk_d = null;
				CollectDataPK pk_m = null;
				for (LogcountCollectData logcountCollectData : entry.getValue()) {
					m_log.debug("replace() : " + logcountCollectData.toString());

					if (logcountCollectData.getTime() == null){
						m_log.debug("replace() : time is null.");
						continue;
					}
					time = logcountCollectData.getTime().longValue();
					CollectDataPK pk = new CollectDataPK(collectorid, time);

					Float value = null;
					if(logcountCollectData.getValue() != null){
						value = Float.parseFloat(logcountCollectData.getValue().toString());
					}
					Float average = null;
					if(logcountCollectData.getAverage() != null){
						average = Float.parseFloat(logcountCollectData.getAverage().toString());
					}
					Float standardDeviation = null;
					if(logcountCollectData.getStandardDeviation() != null){
						standardDeviation = Float.parseFloat(logcountCollectData.getStandardDeviation().toString());
					}
					collectdata_entities.add(new CollectData(pk, value, average, standardDeviation));

					// 日時計算(時)
					Long hour = (time + TIMEZONE) / 1000 / 3600 * 3600 * 1000 - TIMEZONE;
					pk_h = new CollectDataPK(collectorid, hour);

					// 日時計算(日)
					Long day = (time + TIMEZONE) / 1000 / 3600 / 24 * 24 * 3600 * 1000 - TIMEZONE;
					pk_d = new CollectDataPK(collectorid, day);

					// 日時計算(週)
					Calendar calendar = HinemosTime.getCalendarInstance();
					calendar.setTimeInMillis(time);
					int y = calendar.get(Calendar.YEAR);
					int m = calendar.get(Calendar.MONTH);
					calendar.clear();
					calendar.set(y, m, 1);
					Long month = calendar.getTimeInMillis();
					pk_m = new CollectDataPK(collectorid, month);

					// 初回
					if (isFirst) {
						// SummaryHour
						if (hour < fromDate) {
							List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
								Arrays.asList(new Integer[]{collectorid}), hour, fromDate - 1, timeout);
							for (CollectData addCollectData : addCollectDataList) {
								SummaryInfo summaryHour = getNewSummaryInfo(
										summaryhour_entitiesMap.get(pk_h),
										pk_h, 
										addCollectData.getValue(),
										addCollectData.getAverage(),
										addCollectData.getStandardDeviation());
								if (summaryHour != null) {
									summaryhour_entitiesMap.put(pk_h, summaryHour);
								}
							}
						}
						// SummaryDay
						if (day < fromDate) {
							List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
									Arrays.asList(new Integer[]{collectorid}), day, fromDate - 1, timeout);
							for (CollectData addCollectData : addCollectDataList) {
								SummaryInfo summaryDay = getNewSummaryInfo(
										summaryday_entitiesMap.get(pk_d), 
										pk_d, 
										addCollectData.getValue(),
										addCollectData.getAverage(),
										addCollectData.getStandardDeviation());
								if (summaryDay != null) {
									summaryday_entitiesMap.put(pk_d, summaryDay);
								}
							}
						}
						// SummaryMonth
						if (month < fromDate) {
							List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
									Arrays.asList(new Integer[]{collectorid}), month, fromDate - 1, timeout);
							for (CollectData addCollectData : addCollectDataList) {
								SummaryInfo summaryMonth = getNewSummaryInfo(
										summarymonth_entitiesMap.get(pk_m), 
										pk_m, 
										addCollectData.getValue(),
										addCollectData.getAverage(),
										addCollectData.getStandardDeviation());
								if (summaryMonth != null) {
									summarymonth_entitiesMap.put(pk_m, summaryMonth);
								}
							}
						}
						isFirst = false;
					}

					
					// SummaryHour
					SummaryInfo summaryHour = getNewSummaryInfo(summaryhour_entitiesMap.get(pk_h), 
							pk_h, value, average, standardDeviation);
					if (summaryHour != null) {
						summaryhour_entitiesMap.put(pk_h, summaryHour);
					}

					// SummaryDay
					SummaryInfo summaryDay = getNewSummaryInfo(summaryday_entitiesMap.get(pk_d), 
							pk_d, value, average, standardDeviation);
					if (summaryDay != null) {
						summaryday_entitiesMap.put(pk_d, summaryDay);
					}

					// SummaryMonth
					SummaryInfo summaryMonth = getNewSummaryInfo(summarymonth_entitiesMap.get(pk_m), 
							pk_m, value, average, standardDeviation);
					if (summaryMonth != null) {
						summarymonth_entitiesMap.put(pk_m, summaryMonth);
					}
				}

				// 以降のSummaryデータ
				// 日時計算(時)
				Calendar calendar = HinemosTime.getCalendarInstance();
				calendar.setTimeInMillis(time);
				int y = calendar.get(Calendar.YEAR);
				int m = calendar.get(Calendar.MONTH);
				int d = calendar.get(Calendar.DATE);
				int h = calendar.get(Calendar.HOUR_OF_DAY);
				calendar.clear();
				calendar.set(y, m, d, h, 0);
				calendar.add(Calendar.HOUR_OF_DAY, 1);
				long nextHour = calendar.getTimeInMillis();
	
				// 日時計算(日)
				calendar.clear();
				calendar.set(y, m, d);
				calendar.add(Calendar.DATE, 1);
				long nextDay = calendar.getTimeInMillis();
	
				// 日時計算(月)
				calendar.clear();
				calendar.set(y, m, 1);
				calendar.add(Calendar.MONTH, 1);
				long nextMonth = calendar.getTimeInMillis();

				// SummaryHour
				if (nextHour - 1 > toDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
							Arrays.asList(new Integer[]{collectorid}), toDate + 1, nextHour - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryHour = getNewSummaryInfo(
								summaryhour_entitiesMap.get(pk_h), 
								pk_h, 
								addCollectData.getValue(),
								addCollectData.getAverage(),
								addCollectData.getStandardDeviation());
						if (summaryHour != null) {
							summaryhour_entitiesMap.put(pk_h, summaryHour);
						}
					}
				}
				// SummaryDay
				if (nextDay - 1 > toDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
							Arrays.asList(new Integer[]{collectorid}), toDate + 1, nextDay - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryDay = getNewSummaryInfo(
								summaryday_entitiesMap.get(pk_d), 
								pk_d, 
								addCollectData.getValue(),
								addCollectData.getAverage(),
								addCollectData.getStandardDeviation());
						if (summaryDay != null) {
							summaryday_entitiesMap.put(pk_d, summaryDay);
						}
					}
				}
				// SummaryMonth
				if (nextMonth - 1 > toDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
							Arrays.asList(new Integer[]{collectorid}), toDate + 1, nextMonth - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryMonth = getNewSummaryInfo(
								summarymonth_entitiesMap.get(pk_m), 
								pk_m, 
								addCollectData.getValue(),
								addCollectData.getAverage(),
								addCollectData.getStandardDeviation());
						if (summaryMonth != null) {
							summarymonth_entitiesMap.put(pk_m, summaryMonth);
						}
					}
				}

				m_log.debug("replace() end :  collectKeyInfoPK=" + entry.getKey());
			}

			// 再集計対象データを取得
			List<CollectKeyInfo> resummaryCollectKeyList = new ArrayList<>();
			for (CollectKeyInfo collectKeyInfo : deleteCollectKeyList) {
				if (!summaryDataMap.containsKey(collectKeyInfo.getId())) {
					// 集計対象
					resummaryCollectKeyList.add(
							new CollectKeyInfo(collectKeyInfo.getItemName(), collectKeyInfo.getDisplayName(),
							collectKeyInfo.getMonitorId(), collectKeyInfo.getFacilityid(), collectKeyInfo.getCollectorid()));
				}
			}

			// 再集計
			for (CollectKeyInfo collectKeyInfo : resummaryCollectKeyList) {
				CollectDataPK pk_h = null;
				CollectDataPK pk_d = null;
				CollectDataPK pk_m = null;

				// 日時計算(時)
				Long hour = (fromDate.longValue() + TIMEZONE) / 1000 / 3600 * 3600 * 1000 - TIMEZONE;
				pk_h = new CollectDataPK(collectKeyInfo.getCollectorid(), hour);

				// 日時計算(日)
				Long day = (fromDate.longValue() + TIMEZONE) / 1000 / 3600 / 24 * 24 * 3600 * 1000 - TIMEZONE;
				pk_d = new CollectDataPK(collectKeyInfo.getCollectorid(), day);

				// 日時計算(週)
				Calendar calendar = HinemosTime.getCalendarInstance();
				calendar.setTimeInMillis(fromDate.longValue());
				int y = calendar.get(Calendar.YEAR);
				int m = calendar.get(Calendar.MONTH);
				calendar.clear();
				calendar.set(y, m, 1);
				Long month = calendar.getTimeInMillis();
				pk_m = new CollectDataPK(collectKeyInfo.getCollectorid(), month);

				// SummaryHour
				if (hour < fromDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
						Arrays.asList(new Integer[]{collectKeyInfo.getCollectorid()}), 
						hour, fromDate - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryHour = getNewSummaryInfo(
							summaryhour_entitiesMap.get(pk_h),
							pk_h, 
							addCollectData.getValue(),
							addCollectData.getAverage(),
							addCollectData.getStandardDeviation());
						if (summaryHour != null) {
							summaryhour_entitiesMap.put(pk_h, summaryHour);
						}
					}
				}
				// SummaryDay
				if (day < fromDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
						Arrays.asList(new Integer[]{collectKeyInfo.getCollectorid()}),
						day, fromDate - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryDay = getNewSummaryInfo(
							summaryday_entitiesMap.get(pk_d), 
							pk_d, 
							addCollectData.getValue(),
							addCollectData.getAverage(),
							addCollectData.getStandardDeviation());
						if (summaryDay != null) {
							summaryday_entitiesMap.put(pk_d, summaryDay);
						}
					}
				}
				// SummaryMonth
				if (month < fromDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
						Arrays.asList(new Integer[]{collectKeyInfo.getCollectorid()}),
						month, fromDate - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryMonth = getNewSummaryInfo(
							summarymonth_entitiesMap.get(pk_m), 
							pk_m, 
							addCollectData.getValue(),
							addCollectData.getAverage(),
							addCollectData.getStandardDeviation());
						if (summaryMonth != null) {
							summarymonth_entitiesMap.put(pk_m, summaryMonth);
						}
					}
				}

				// 以降のSummaryデータ
				// 日時計算(時)
				calendar = HinemosTime.getCalendarInstance();
				calendar.setTimeInMillis(toDate.longValue());
				y = calendar.get(Calendar.YEAR);
				m = calendar.get(Calendar.MONTH);
				int d = calendar.get(Calendar.DATE);
				int h = calendar.get(Calendar.HOUR_OF_DAY);
				calendar.clear();
				calendar.set(y, m, d, h, 0);
				calendar.add(Calendar.HOUR_OF_DAY, 1);
				long nextHour = calendar.getTimeInMillis();

				// 日時計算(日)
				calendar.clear();
				calendar.set(y, m, d);
				calendar.add(Calendar.DATE, 1);
				long nextDay = calendar.getTimeInMillis();

				// 日時計算(月)
				calendar.clear();
				calendar.set(y, m, 1);
				calendar.add(Calendar.MONTH, 1);
				long nextMonth = calendar.getTimeInMillis();

				// SummaryHour
				if (nextHour - 1 > toDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
						Arrays.asList(new Integer[]{collectKeyInfo.getCollectorid()}),
						toDate + 1, nextHour - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryHour = getNewSummaryInfo(
							summaryhour_entitiesMap.get(pk_h), 
							pk_h, 
							addCollectData.getValue(),
							addCollectData.getAverage(),
							addCollectData.getStandardDeviation());
						if (summaryHour != null) {
							summaryhour_entitiesMap.put(pk_h, summaryHour);
						}
					}
				}
				// SummaryDay
				if (nextDay - 1 > toDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
						Arrays.asList(new Integer[]{collectKeyInfo.getCollectorid()}),
						toDate + 1, nextDay - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryDay = getNewSummaryInfo(
							summaryday_entitiesMap.get(pk_d), 
							pk_d, 
							addCollectData.getValue(),
							addCollectData.getAverage(),
							addCollectData.getStandardDeviation());
						if (summaryDay != null) {
							summaryday_entitiesMap.put(pk_d, summaryDay);
						}
					}
				}
				// SummaryMonth
				if (nextMonth - 1 > toDate) {
					List<CollectData> addCollectDataList = QueryUtil.getCollectDataList(
						Arrays.asList(new Integer[]{collectKeyInfo.getCollectorid()}),
						toDate + 1, nextMonth - 1, timeout);
					for (CollectData addCollectData : addCollectDataList) {
						SummaryInfo summaryMonth = getNewSummaryInfo(
							summarymonth_entitiesMap.get(pk_m), 
							pk_m, 
							addCollectData.getValue(),
							addCollectData.getAverage(),
							addCollectData.getStandardDeviation());
						if (summaryMonth != null) {
							summarymonth_entitiesMap.put(pk_m, summaryMonth);
						}
					}
				}
			}
			jtm.commit();
			List<JdbcBatchQuery> query = new ArrayList<JdbcBatchQuery>();

			// データを削除する
			if (!deleteCollectKeyList.isEmpty()) {
				query.add(new CollectDataJdbcBatchDelete(deleteCollectKeyList, fromDate, toDate));
				query.add(new SummaryHourJdbcBatchDelete(deleteCollectKeyList, fromDate, toDate));
				query.add(new SummaryDayJdbcBatchDelete(deleteCollectKeyList, fromDate, toDate));
				query.add(new SummaryMonthJdbcBatchDelete(deleteCollectKeyList, fromDate, toDate));
			}
			// データを挿入する
			if(!collectdata_entities.isEmpty()){
				query.add(new CollectDataJdbcBatchInsert(collectdata_entities));
			}
			if(!summaryhour_entitiesMap.isEmpty()){
				List<SummaryHour> list = new ArrayList<>();
				for (SummaryInfo summaryInfo : summaryhour_entitiesMap.values()) {
					list.add(
							new SummaryHour(
							new CollectDataPK(
							summaryInfo.getCollectorid(), 
							summaryInfo.getTime()),
							summaryInfo.getAvg(),
							summaryInfo.getMin(),
							summaryInfo.getMax(),
							summaryInfo.getCount(),
							summaryInfo.getAverageAvg(),
							summaryInfo.getAverageCount(),
							summaryInfo.getStandardDeviationAvg(),
							summaryInfo.getStandardDeviationCount()));
				}
				query.add(new SummaryHourJdbcBatchUpsert(list));
			}
			if(!summaryday_entitiesMap.isEmpty()){
				List<SummaryDay> list = new ArrayList<>();
				for (SummaryInfo summaryInfo : summaryday_entitiesMap.values()) {
					list.add(
							new SummaryDay(
							new CollectDataPK(
							summaryInfo.getCollectorid(), 
							summaryInfo.getTime()),
							summaryInfo.getAvg(),
							summaryInfo.getMin(),
							summaryInfo.getMax(),
							summaryInfo.getCount(),
							summaryInfo.getAverageAvg(),
							summaryInfo.getAverageCount(),
							summaryInfo.getStandardDeviationAvg(),
							summaryInfo.getStandardDeviationCount()));
				}
				query.add(new SummaryDayJdbcBatchUpsert(list));
			}
			if(!summarymonth_entitiesMap.isEmpty()){
				List<SummaryMonth> list = new ArrayList<>();
				for (SummaryInfo summaryInfo : summarymonth_entitiesMap.values()) {
					list.add(
							new SummaryMonth(
							new CollectDataPK(
							summaryInfo.getCollectorid(), 
							summaryInfo.getTime()),
							summaryInfo.getAvg(),
							summaryInfo.getMin(),
							summaryInfo.getMax(),
							summaryInfo.getCount(),
							summaryInfo.getAverageAvg(),
							summaryInfo.getAverageCount(),
							summaryInfo.getStandardDeviationAvg(),
							summaryInfo.getStandardDeviationCount()));
				}
				query.add(new SummaryMonthJdbcBatchUpsert(list));
			}
			JdbcBatchExecutor.execute(query);

			// 収集データキャッシュの更新処理
			MonitorCollectDataCache.refresh(monitorId);
		}
		m_log.debug("replace() end");

		return collectdata_entities.size();
	}

	/**
	 * SummaryHourに値を追加してCloneで返す
	 * 
	 * @param oldSummaryHour 反映先SummaryHour
	 * @param pk
	 * @param value
	 * @param average
	 * @param standardDeviation
	 * @return CloneのSummaryHour
	 */
	private static SummaryInfo getNewSummaryInfo(SummaryInfo oldSummaryInfo, CollectDataPK pk, 
			Float value, Float average, Float standardDeviation) {
		SummaryInfo newSummaryInfo = null;
		if (oldSummaryInfo == null) {
			if ((value != null && !Float.isNaN(value))
					|| (average != null && !Float.isNaN(average))
					|| (standardDeviation == null || Float.isNaN(standardDeviation))) {
				Float newValue = null;
				Integer valueCount = 0;
				Float newAverage = null;
				Integer averageCount = 0;
				Float newStandardDeviation = null;
				Integer standardDeviationCount = 0;
				if (value != null && !Float.isNaN(value)) {
					newValue = value;
					valueCount++;
				}
				if (average != null && !Float.isNaN(average)) {
					newAverage = average;
					averageCount++;
				}
				if (standardDeviation != null && !Float.isNaN(standardDeviation)) {
					newStandardDeviation = standardDeviation;
					standardDeviationCount++;
				}
				newSummaryInfo = new SummaryInfo(pk.getCollectorid(), pk.getTime(), newValue, newValue, newValue, valueCount, 
						newAverage, averageCount, 
						newStandardDeviation, standardDeviationCount);
			}
		} else {
			newSummaryInfo = oldSummaryInfo.clone();
			if ((value != null && !Float.isNaN(value))
					|| (average != null && !Float.isNaN(average))
					|| (standardDeviation == null || Float.isNaN(standardDeviation))) {
				if (value != null && !Float.isNaN(value)) {
					if (oldSummaryInfo.getCount() == 0) {
						newSummaryInfo.setAvg(value);
						newSummaryInfo.setMin(value);
						newSummaryInfo.setMax(value);
						newSummaryInfo.setCount(1);
					} else {
						newSummaryInfo.setAvg((value + oldSummaryInfo.getAvg() * oldSummaryInfo.getCount()) / (oldSummaryInfo.getCount() + 1));
						newSummaryInfo.setMin(oldSummaryInfo.getMin() < value ? oldSummaryInfo.getMin() : value);
						newSummaryInfo.setMax(oldSummaryInfo.getMax() > value ? oldSummaryInfo.getMax() : value);
						newSummaryInfo.setCount(oldSummaryInfo.getCount() + 1);
					}
				}
				if (average != null && !Float.isNaN(average)) {
					if (oldSummaryInfo.getAverageCount() == 0) {
						newSummaryInfo.setAverageAvg(average);
						newSummaryInfo.setAverageCount(1);
					} else {
						newSummaryInfo.setAverageAvg((average + oldSummaryInfo.getAverageAvg() * oldSummaryInfo.getAverageCount()) 
								/ (oldSummaryInfo.getAverageCount() + 1));
						newSummaryInfo.setAverageCount(oldSummaryInfo.getAverageCount() + 1);
					}
				}
				if (standardDeviation != null && !Float.isNaN(standardDeviation)) {
					if (oldSummaryInfo.getStandardDeviationCount() == 0) {
						newSummaryInfo.setStandardDeviationAvg(standardDeviation);
						newSummaryInfo.setStandardDeviationCount(1);
					} else {
						newSummaryInfo.setStandardDeviationAvg((standardDeviation 
								+ oldSummaryInfo.getStandardDeviationAvg() * oldSummaryInfo.getStandardDeviationCount()) 
								/ (oldSummaryInfo.getStandardDeviationCount() + 1));
						newSummaryInfo.setStandardDeviationCount(oldSummaryInfo.getStandardDeviationCount() + 1);
					}
				}
			}
		}
		return newSummaryInfo;
	}

	/**
	 * マップから集計データ（時間）を取得する
	 * 存在しない場合はDBより取得する
	 * 
	 * @param pk 取得するキー情報
	 * @return 集計データ（時間）
	 */
	private static SummaryInfo getSummaryHourInfo(CollectDataPK pk) {
		SummaryInfo summaryInfo = null;
		if (summaryHourInfoMap.containsKey(pk.getCollectorid())) {
			summaryInfo = summaryHourInfoMap.get(pk.getCollectorid());
			if (!summaryInfo.getTime().equals(pk.getTime())) {
				summaryInfo = null;
			}
		} else {
			try {
				SummaryHour summaryHour = QueryUtil.getSummaryHour(pk);
				summaryInfo = new SummaryInfo(
						summaryHour.getCollectorId(),
						summaryHour.getTime(),
						summaryHour.getAvg(),
						summaryHour.getMin(),
						summaryHour.getMax(),
						summaryHour.getCount(),
						summaryHour.getAverageAvg(),
						summaryHour.getAverageCount(),
						summaryHour.getStandardDeviationAvg(),
						summaryHour.getStandardDeviationCount());
			} catch (CollectKeyNotFound e) {
				// 何も処理をしない
			}
		}
		return summaryInfo;
	}

	/**
	 * マップから集計データ（日）を取得する
	 * 存在しない場合はDBより取得する
	 * 
	 * @param pk 取得するキー情報
	 * @return 集計データ（日）
	 */
	private static SummaryInfo getSummaryDayInfo(CollectDataPK pk) {
		SummaryInfo summaryInfo = null;
		if (summaryDayInfoMap.containsKey(pk.getCollectorid())) {
			summaryInfo = summaryDayInfoMap.get(pk.getCollectorid());
			if (!summaryInfo.getTime().equals(pk.getTime())) {
				summaryInfo = null;
			}
		} else {
			try {
				SummaryDay summaryDay = QueryUtil.getSummaryDay(pk);
				summaryInfo = new SummaryInfo(
						summaryDay.getCollectorId(),
						summaryDay.getTime(),
						summaryDay.getAvg(),
						summaryDay.getMin(),
						summaryDay.getMax(),
						summaryDay.getCount(),
						summaryDay.getAverageAvg(),
						summaryDay.getAverageCount(),
						summaryDay.getStandardDeviationAvg(),
						summaryDay.getStandardDeviationCount());
			} catch (CollectKeyNotFound e) {
				// 何も処理をしない
			}
		}
		return summaryInfo;
	}

	/**
	 * マップから集計データ（月）を取得する
	 * 存在しない場合はDBより取得する
	 * 
	 * @param pk 取得するキー情報
	 * @return 集計データ（月）
	 */
	private static SummaryInfo getSummaryMonthInfo(CollectDataPK pk) {
		SummaryInfo summaryInfo = null;
		if (summaryMonthInfoMap.containsKey(pk.getCollectorid())) {
			summaryInfo = summaryMonthInfoMap.get(pk.getCollectorid());
			if (!summaryInfo.getTime().equals(pk.getTime())) {
				summaryInfo = null;
			}
		} else {
			try {
				SummaryMonth summaryMonth = QueryUtil.getSummaryMonth(pk);
				summaryInfo = new SummaryInfo(
						summaryMonth.getCollectorId(),
						summaryMonth.getTime(),
						summaryMonth.getAvg(),
						summaryMonth.getMin(),
						summaryMonth.getMax(),
						summaryMonth.getCount(),
						summaryMonth.getAverageAvg(),
						summaryMonth.getAverageCount(),
						summaryMonth.getStandardDeviationAvg(),
						summaryMonth.getStandardDeviationCount());
			} catch (CollectKeyNotFound e) {
				// 何も処理をしない
			}
		}
		return summaryInfo;
	}

	/**
	 * 集計データ情報
	 */
	public static class SummaryInfo implements Cloneable {
		private Integer collectorid;
		private Long time;
		private Float avg;
		private Float min;
		private Float max;
		private Integer count;
		private Float averageAvg;
		private Integer averageCount;
		private Float standardDeviationAvg;
		private Integer standardDeviationCount;

		public SummaryInfo (Integer collectorid, Long time, Float avg, Float min, Float max, Integer count, 
				Float averageAvg, Integer averageCount, 
				Float standardDeviationAvg, Integer standardDeviationCount) {
			this.collectorid = collectorid;
			this.time = time;
			this.avg = avg;
			this.min = min;
			this.max = max;
			this.count = count;
			this.averageAvg = averageAvg;
			this.averageCount = averageCount;
			this.standardDeviationAvg = standardDeviationAvg;
			this.standardDeviationCount = standardDeviationCount;
		}
		public Integer getCollectorid() {
			return collectorid;
		}
		public void setCollectorid(Integer collectorid) {
			this.collectorid = collectorid;
		}
		public Long getTime() {
			return time;
		}
		public void setTime(Long time) {
			this.time = time;
		}
		public Float getAvg() {
			return avg;
		}
		public void setAvg(Float avg) {
			this.avg = avg;
		}
		public Float getMin() {
			return min;
		}
		public void setMin(Float min) {
			this.min = min;
		}
		public Float getMax() {
			return max;
		}
		public void setMax(Float max) {
			this.max = max;
		}
		public Integer getCount() {
			return count;
		}
		public void setCount(Integer count) {
			this.count = count;
		}
		public Float getAverageAvg() {
			return averageAvg;
		}
		public void setAverageAvg(Float averageAvg) {
			this.averageAvg = averageAvg;
		}
		public Integer getAverageCount() {
			return averageCount;
		}
		public void setAverageCount(Integer averageCount) {
			this.averageCount = averageCount;
		}
		public Float getStandardDeviationAvg() {
			return standardDeviationAvg;
		}
		public void setStandardDeviationAvg(Float standardDeviationAvg) {
			this.standardDeviationAvg = standardDeviationAvg;
		}
		public Integer getStandardDeviationCount() {
			return standardDeviationCount;
		}
		public void setStandardDeviationCount(Integer standardDeviationCount) {
			this.standardDeviationCount = standardDeviationCount;
		}
		@Override
		public SummaryInfo clone() {
				SummaryInfo summaryInfo = null;
				try {
					summaryInfo = (SummaryInfo)super.clone();
				} catch (CloneNotSupportedException e) {
					m_log.debug("SummaryInfo.clone() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
				}
			return summaryInfo;
		}
	}
}