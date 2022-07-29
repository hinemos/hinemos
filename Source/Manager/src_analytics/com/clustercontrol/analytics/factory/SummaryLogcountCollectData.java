/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.util.OperatorCommonUtil;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosArithmeticException;
import com.clustercontrol.fault.HinemosIllegalArgumentException;
import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * ログ件数監視集計で使用する収集データ用のキャッシュ
 *
 * @version 6.1.0
 */
public class SummaryLogcountCollectData {

	private static Log m_log = LogFactory.getLog( SummaryLogcountCollectData.class );

	// Map<キー情報,MAP<収集日時、値・合計値>>
	private Map<CollectKeyInfoPK, LinkedList<LogcountCollectData>> logcountCollectMap = new HashMap<>();

	// 収集開始日時
	private Long m_startDate;

	// 収集終了日時
	private Long m_endDate;

	// 監視設定情報
	private MonitorInfo m_monitorInfo;

	// 収集期間（単位を日時にあわせたもの）
	private Long m_changeAnalysysRange;

	/**
	 * 収集データBean
	 * 収集日時、値は、計算時に解析時にデータ型を統一する必要があるため、
	 * Double型にして保持している。
	 */
	public static class LogcountCollectData {
		// 日時
		private Double time;
		// 値
		private Double value;
		// 処理対象用合計値（ログ件数は最小0）
		private List<Double> summaryValueList = new ArrayList<>();
		// 平均値
		private Double average = null;
		// 標準偏差
		private Double standardDeviation = null;

		public LogcountCollectData(Double time, Double value) {
			this.setTime(time);
			this.setValue(value);
		}

		public LogcountCollectData(Double time, Double value, Double average, Double standardDeviation) {
			this.setTime(time);
			this.setValue(value);
			this.setAverage(average);
			this.setStandardDeviation(standardDeviation);
		}

		public Double getTime() {
			return this.time;
		}
		public void setTime(Double time) {
			this.time = time;
		}
		public Double getValue() {
			return this.value;
		}
		public void setValue(Double value) {
			this.value = value;
		}
		public Double getAverage() {
			return average;
		}
		public void setAverage(Double average) {
			this.average = average;
		}
		public Double getStandardDeviation() {
			return standardDeviation;
		}
		public void setStandardDeviation(Double standardDeviation) {
			this.standardDeviation = standardDeviation;
		}
		public List<Double> getSummaryValueList() {
			return this.summaryValueList;
		}

		@Override
		public String toString() {
			String[] names = {
					"time",
					"value",
					"average",
					"standardDeviation"
			};
			String[] values = {
					String.valueOf(this.time),
					String.valueOf(this.value),
					String.valueOf(this.average),
					String.valueOf(this.standardDeviation)
			};
			return Arrays.toString(names) + " = " + Arrays.toString(values);
		}
	}

	// コンストラクタ
	public SummaryLogcountCollectData (MonitorInfo monitorInfo, Long startDate, Long endDate) {
		this.m_monitorInfo = monitorInfo;
		this.m_startDate = startDate;
		this.m_endDate = endDate;
		m_changeAnalysysRange = m_monitorInfo.getChangeAnalysysRange().longValue() * 60L * 1000L;
	}

	// データ設定
	public void addData(String facilityId, String displayName, Long time, Double value) {
		if (time > m_endDate) {
			return;
		}
		CollectKeyInfoPK pk = new CollectKeyInfoPK(
				m_monitorInfo.getItemName(), displayName, this.m_monitorInfo.getMonitorId(), facilityId);
		if (!logcountCollectMap.containsKey((pk))) {
			logcountCollectMap.put(pk, new LinkedList<>());
		}
		LinkedList<LogcountCollectData> dataList = logcountCollectMap.get(pk);
		dataList.add(new LogcountCollectData(time.doubleValue(), value));
	}

	// 集計処理
	public Map<CollectKeyInfoPK, LinkedList<LogcountCollectData>> createSummaryDataMap() {
		// 前後データをDBより取得
		dbData();

		// 合計値設定
		summaryCollectData();

		// 平均値・標準偏差計算
		calculateCollectData();

		// 集計データ作成
		return getLogcountCollectDataMap();
	}

	// 前後データをDBより取得
	private void dbData() {

		for (Map.Entry<CollectKeyInfoPK, LinkedList<LogcountCollectData>> entry : logcountCollectMap.entrySet()) {
			CollectKeyInfo collectKeyInfo = null;
			try {
				// キー取得
				collectKeyInfo = QueryUtil.getCollectKeyPK(entry.getKey());
			} catch (CollectKeyNotFound e) {
				// DBにデータが存在しない
				continue;
			}

			// 前データ取得対象日時の算出
			Long fromDate = m_startDate - m_changeAnalysysRange;

			// 前データ取得
			List<CollectData> dbCollectDataList 
				= QueryUtil.getCollectDataListOrderByTimeAsc(collectKeyInfo.getCollectorid(), fromDate, m_startDate);
			if (dbCollectDataList != null) {
				for (int i = 0; i < dbCollectDataList.size(); i++) {
					Double value = null;
					if (dbCollectDataList.get(i).getValue() != null) {
						value = dbCollectDataList.get(i).getValue().doubleValue();
					}
					entry.getValue().add(i, new LogcountCollectData(dbCollectDataList.get(i).getTime().doubleValue(), value));
				}
			}
		}
	}

	// 集計データ取得
	private void summaryCollectData() {
		for (Map.Entry<CollectKeyInfoPK, LinkedList<LogcountCollectData>> entry : logcountCollectMap.entrySet()) {
			// キーごとに、平均値、標準偏差算出に使用する収集データ合計を設定する
			from : for (LogcountCollectData dataFrom : entry.getValue()) {
				for (LogcountCollectData dataTo : entry.getValue()) {
					if (dataTo.getTime() - m_changeAnalysysRange > dataFrom.getTime()) {
						continue from;
					}
					if (dataTo.getTime() <= dataFrom.getTime()) {
						continue;
					}
					if (dataTo.getTime() - m_changeAnalysysRange <= dataFrom.getTime() 
							&& dataFrom.getTime() < dataTo.getTime()) {
						dataTo.getSummaryValueList().add(dataFrom.getValue());
					}
				}
			}
		}
	}

	// 平均値・標準偏差計算
	private void calculateCollectData() {
		Long dataCount = HinemosPropertyCommon.monitor_change_lower_limit.getNumericValue();
		for (Map.Entry<CollectKeyInfoPK, LinkedList<LogcountCollectData>> entry : logcountCollectMap.entrySet()) {
			// キーごとに、平均値、標準偏差を計算する
			for (LogcountCollectData data : entry.getValue()) {
				if (data.getTime() < m_startDate) {
					continue;
				}
				Double average = null;
				Double standardDeviation = null;
				if (data.getSummaryValueList().size() < dataCount) {
					m_log.info("calculateCollectData():"
							+ " monitorId=" + m_monitorInfo.getMonitorId()
							+ ", facilityId=" + entry.getKey().getFacilityid()
							+ ", displayName=" + entry.getKey().getDisplayName()
							+ ", itemName=" + m_monitorInfo.getItemName()
							+ ", targetDate=" + new Date(data.getTime().longValue())
							+ ": The number of data is insufficient. (change mount)"
							+ " required count=" + dataCount.toString()
							+ ", data count=" + Integer.toString(data.getSummaryValueList().size()));
				} else {
					try {
						average = OperatorCommonUtil.getAverage(data.getSummaryValueList());
						standardDeviation = OperatorCommonUtil.getStandardDeviation(data.getSummaryValueList());
					} catch (HinemosArithmeticException e) {
						m_log.warn("calculateCollectData():"
								+ " monitorId=" + m_monitorInfo.getMonitorId()
								+ ", facilityId=" + entry.getKey().getFacilityid()
								+ ", displayName=" + entry.getKey().getDisplayName()
								+ ", itemName=" + m_monitorInfo.getItemName()
								+ ", targetDate=" + new Date(data.getTime().longValue())
								+ "\n" + e.getMessage());
					} catch (HinemosIllegalArgumentException e) {
						m_log.info("calculateCollectData():"
								+ " monitorId=" + m_monitorInfo.getMonitorId()
								+ ", facilityId=" + entry.getKey().getFacilityid()
								+ ", displayName=" + entry.getKey().getDisplayName()
								+ ", itemName=" + m_monitorInfo.getItemName()
								+ ", targetDate=" + new Date(data.getTime().longValue())
								+ "\n" + e.getMessage());
					}
				}
				data.setAverage(average);
				data.setStandardDeviation(standardDeviation);
			}
		}
	}

	// 収集データ作成
	private Map<CollectKeyInfoPK, LinkedList<LogcountCollectData>> getLogcountCollectDataMap() {
		Map<CollectKeyInfoPK, LinkedList<LogcountCollectData>> logcountCollectDataMap = new HashMap<>();

		for (Map.Entry<CollectKeyInfoPK, LinkedList<LogcountCollectData>> entry : logcountCollectMap.entrySet()) {
			for (LogcountCollectData data : entry.getValue()) {
				if (data.getTime() < m_startDate) {
					continue;
				}
				if (!logcountCollectDataMap.containsKey(entry.getKey())) {
					logcountCollectDataMap.put(entry.getKey(), new LinkedList<>());
				}
				logcountCollectDataMap.get(entry.getKey()).add(
					new LogcountCollectData(
							data.getTime(),
							data.getValue(),
							data.getAverage(),
							data.getStandardDeviation()));
			}
		}
		return logcountCollectDataMap;
	}
}
