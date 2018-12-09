/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.util.OperatorCommonUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosArithmeticException;
import com.clustercontrol.fault.HinemosIllegalArgumentException;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache.MonitorCollectData;
import com.clustercontrol.util.HinemosTime;

/**
 * 将来予測監視、変化点監視機能のユーティリティクラス<br/>
 * 
 * @since 6.1.0
 */
public class MonitorOperatorChangeMountUtil {

	private static Log m_log = LogFactory.getLog(MonitorOperatorChangeMountUtil.class);

	/**
	 * 平均値、標準偏差値、収集データを取得する
	 * データの追加もここでおこなう。
	 * 
	 * @param monitorId			監視設定ID
	 * @param facilityId		ファシリティID
	 * @param displayName		DisplayName
	 * @param itemName			ItemName
	 * @param targetDate		監視時点の日時
	 * @param latestValue		最新の取得値
	 * @return	平均値、標準偏差値、収集データのBean
	 */
	public static MonitorChangeMountDataInfo getChangeMountDataInfo(
			MonitorInfo monitorInfo,
			String facilityId, 
			String displayName,
			String itemName,
			Long targetDate,
			Double latestValue) {
		
		MonitorChangeMountDataInfo rtn = new MonitorChangeMountDataInfo();

		m_log.debug("getChangeMountDataInfo start : "
				+ "monitorId=" + monitorInfo.getMonitorId()
				+ ", facilityId=" + facilityId
				+ ", displayName=" + displayName
				+ ", itemName=" + itemName
				+ ", targetDate=" + targetDate
				+ ", latestValue=" + latestValue);
		if (monitorInfo.getMonitorId() == null
				|| monitorInfo.getMonitorId().isEmpty()
				|| facilityId == null 
				|| facilityId.isEmpty()
				|| targetDate == null) {
			return rtn;
		}
		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}
		// 収集値を取得する
		List<MonitorCollectData> monitorCollectDataList 
			= MonitorCollectDataCache.getMonitorCollectDataList(
					monitorInfo.getMonitorId(), facilityId, displayName, itemName, targetDate,
					monitorInfo.getChangeAnalysysRange().doubleValue());

		if (monitorCollectDataList == null 
				|| monitorCollectDataList.isEmpty()) {
			return rtn;
		}
		// 値リストの作成
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < monitorCollectDataList.size(); i++) {
			String strValue = "-";
			if (monitorCollectDataList.get(i).getValue() != null 
					&& !monitorCollectDataList.get(i).getValue().isNaN()) {
				strValue = monitorCollectDataList.get(i).getValue().toString();
			}
			sb.append(String.format("(%s, %s)%n", 
					sdf.format(new Date(monitorCollectDataList.get(i).getTime().longValue())), 
					strValue));
		}
		rtn.setValuesStr(sb.toString());

		List<Double> collectList = new ArrayList<>();
		for (int i = 1; i < monitorCollectDataList.size(); i++) {
			if (monitorCollectDataList.get(i).getValue() != null 
					&& !monitorCollectDataList.get(i).getValue().isNaN()) {
				collectList.add(monitorCollectDataList.get(i).getValue());
			}
		}
		// 範囲情報の取得
		Double average = null;
		Double standardDeviation = null;
		Long dataCount = HinemosPropertyCommon.monitor_change_lower_limit.getNumericValue();
		if (collectList.size() < dataCount) {
			rtn.setErrorMessage("getChangeMountDataInfo():"
					+ " monitorId=" + monitorInfo.getMonitorId()
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ": The number of data is insufficient. (change mount)"
					+ " required count=" + dataCount.toString()
					+ ", data count=" + Integer.toString(collectList.size()));
			rtn.setNotEnoughFlg(true);
		} else {
			try {
				average = OperatorCommonUtil.getAverage(collectList);
				standardDeviation = OperatorCommonUtil.getStandardDeviation(collectList);
			} catch (HinemosArithmeticException e) {
				m_log.warn("getChangeMountDataInfo():"
						+ " monitorId=" + monitorInfo.getMonitorId()
						+ ", facilityId=" + facilityId
						+ ", displayName=" + displayName
						+ ", itemName=" + itemName
						+ " : " + e.getMessage(), e);
			} catch (HinemosIllegalArgumentException e) {
				rtn.setErrorMessage("getChangeMountDataInfo():"
						+ " monitorId=" + monitorInfo.getMonitorId()
						+ ", facilityId=" + facilityId
						+ ", displayName=" + displayName
						+ ", itemName=" + itemName
						+ " : " + e.getMessage());
			}
		}
		rtn.setAverage(average);
		rtn.setStandardDeviation(standardDeviation);
		m_log.debug("getChangeMountDataInfo end : "
				+ "monitorId=" + monitorInfo.getMonitorId()
				+ ", facilityId=" + facilityId
				+ ", displayName=" + displayName
				+ ", itemName=" + itemName);

		// デバッグログ出力
		if (m_log.isDebugEnabled()
				&& rtn.getErrorMessage() != null && !rtn.getErrorMessage().isEmpty()
				&& !monitorInfo.getChangeFlg() && monitorInfo.getCollectorFlg()) {
			m_log.debug(rtn.getErrorMessage());
		}
		return rtn;
	}

	/**
	 * 変化量情報を格納する
	 */
	public static class MonitorChangeMountDataInfo {
		// 平均値
		private Double average;
		// 標準偏差値
		private Double standardDeviation;
		// エラーメッセージ
		private String errorMessage;
		// データ不足フラグ（true:不足）
		private boolean notEnoughFlg = false;
		// 値リスト
		private String valuesStr = "";

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
		public String getErrorMessage() {
			return errorMessage;
		}
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
		public boolean isNotEnoughFlg() {
			return notEnoughFlg;
		}
		public void setNotEnoughFlg(boolean notEnoughFlg) {
			this.notEnoughFlg = notEnoughFlg;
		}
		public String getValuesStr() {
			return valuesStr;
		}
		public void setValuesStr(String valuesStr) {
			this.valuesStr = valuesStr;
		}
	}
}
