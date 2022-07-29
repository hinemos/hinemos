/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.util.OperatorChangeUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.run.bean.CollectMonitorDisplayNameConstant;
import com.clustercontrol.monitor.run.bean.CollectMonitorNotifyConstant;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.bean.MonitorPredictionMethod;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.util.MonitorOperatorChangeMountUtil.MonitorChangeMountDataInfo;
import com.clustercontrol.monitor.run.util.MonitorOperatorPredictionUtil.MonitorPredictionDataInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * 将来予測監視、変化点監視機能のユーティリティクラス<br/>
 * 
 * @since 6.1.0
 */
public class CollectMonitorManagerUtil {

	private static Log m_log = LogFactory.getLog(CollectMonitorManagerUtil.class);


	/**
	 * 将来予測監視、変化点監視に伴う計算処理、キャッシュ更新（将来予測）、通知作成を行う。
	 * 
	 * @param runMonitor					RunMonitor
	 * @param monitorInfo					監視設定情報
	 * @param facilityId					ファシリティID
	 * @param displayName					DisplayName
	 * @param itemName						ItemName
	 * @param targetDate					監視時点の日時
	 * @param latestValue					最新の取得値
	 * @param monitorChangeMountDataInfo	平均値、標準偏差値、収集データ
	 * @param monitorPredictionDataInfo		将来予測情報
	 * @return	判定結果情報
	 */
	public static CollectMonitorDataInfo calculateChangePredict(
			RunMonitor runMonitor,
			MonitorInfo monitorInfo,
			String facilityId, 
			String displayName,
			String itemName,
			Long targetDate,
			Double latestValue) {

		CollectMonitorDataInfo rtn = new CollectMonitorDataInfo();

		if (monitorInfo == null 
				|| facilityId == null 
				|| facilityId.isEmpty()
				|| targetDate == null
				|| !(monitorInfo.getCollectorFlg() || monitorInfo.getChangeFlg() || monitorInfo.getPredictionFlg())) {
			return rtn;
		}

		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}

		m_log.debug("getMonitorRunResultInfo start : "
				+ "monitorId=" + monitorInfo.getMonitorId()
				+ ", facilityId=" + facilityId
				+ ", displayName=" + displayName
				+ ", itemName=" + itemName
				+ ", targetDate=" + targetDate
				+ ", latestValue=" + latestValue);

		// キャッシュを更新する(キャッシュ更新可否はupdate()メソッドにて判定)
		MonitorCollectDataCache.update(monitorInfo.getMonitorId(), facilityId, 
				displayName, itemName, targetDate);

		//　キャッシュに収集値を設定する
		if (latestValue != null && monitorInfo.getCollectorFlg()) {
			// 収集ONの場合のみ収集値を設定する
			MonitorCollectDataCache.add(monitorInfo.getMonitorId(), facilityId,
					displayName, itemName, targetDate, latestValue.floatValue());
		} else {
			MonitorCollectDataCache.add(monitorInfo.getMonitorId(), facilityId,
					displayName, itemName, targetDate, null);
		}

		if (monitorInfo.getCollectorFlg() || monitorInfo.getChangeFlg()) {
			// 変化量計算
			MonitorChangeMountDataInfo changeMountDataInfo = MonitorOperatorChangeMountUtil.getChangeMountDataInfo(
					monitorInfo, facilityId, displayName, itemName, targetDate, latestValue);

			if (changeMountDataInfo != null) {
				// 戻り値への設定
				rtn.setAverage(changeMountDataInfo.getAverage());
				rtn.setStandardDeviation(changeMountDataInfo.getStandardDeviation());
			}
			if (monitorInfo.getChangeFlg() && !changeMountDataInfo.isNotEnoughFlg()) {
				// 通知情報の作成
				MonitorRunResultInfo resultInfo = new MonitorRunResultInfo();
				resultInfo.setMonitorNumericType(MonitorNumericType.TYPE_CHANGE);
				boolean isValueNotNull = (latestValue != null);
				resultInfo.setMonitorFlg(isValueNotNull);
				resultInfo.setCollectorResult(isValueNotNull);
				resultInfo.setAverage(changeMountDataInfo.getAverage());
				resultInfo.setStandardDeviation(changeMountDataInfo.getStandardDeviation());
				Integer checkResult = PriorityConstant.TYPE_UNKNOWN;
				if (latestValue != null && !latestValue.isNaN()) {
					checkResult = getChangeCheckResult(monitorInfo, latestValue, resultInfo.getAverage(), resultInfo.getStandardDeviation());
				}
				resultInfo.setCheckResult(checkResult);
				if (checkResult != PriorityConstant.TYPE_INFO 
						&& checkResult != PriorityConstant.TYPE_WARNING
						&& checkResult != PriorityConstant.TYPE_CRITICAL) {
					if (monitorInfo.getFailurePriority() != null) {
						resultInfo.setPriority(monitorInfo.getFailurePriority());
					} else {
						resultInfo.setPriority(checkResult);
					}
				} else { 
					resultInfo.setPriority(checkResult);
				}
				if (runMonitor != null) {
					resultInfo.setNotifyGroupId(getChangeNotifyGroupId(runMonitor.getNotifyGroupId()));
				} else {
					resultInfo.setNotifyGroupId(getChangeNotifyGroupId(monitorInfo.getNotifyGroupId()));
				}
				resultInfo.setProcessType(checkResult != -2);
				String latestValueStr = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_ANALYTICS.getMessage();
				if (latestValue != null && !latestValue.isNaN()) {
					latestValueStr = latestValue.toString();
				}
				String itemNameStr = monitorInfo.getItemName();
				if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PERFORMANCE)
						&& !displayName.equals("") && !monitorInfo.getItemName().endsWith("[" + displayName + "]")) {
					itemNameStr += "[" + displayName + "]";
				}
				String message = "";
				Double changeMount = OperatorChangeUtil.getChangeMount(
						latestValue, changeMountDataInfo.getAverage(), changeMountDataInfo.getStandardDeviation());
				if (changeMount == null) {
					message = String.format("%s : %s", MessageConstant.CHANGE_MOUNT.getMessage(), 
							MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_ANALYTICS.getMessage());
				} else {
					message = String.format("%s : %s", MessageConstant.CHANGE_MOUNT.getMessage(), changeMount);
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
				sdf.setTimeZone(HinemosTime.getTimeZone());
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("%s : %s", itemNameStr, latestValueStr) + "\n");
				String averageStr = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_ANALYTICS.getMessage();
				if (changeMountDataInfo.getAverage() != null && !changeMountDataInfo.getAverage().isNaN()) {
					averageStr = changeMountDataInfo.getAverage().toString();
				}
				sb.append(String.format("%s : %s", MessageConstant.CHANGE_AVERAGE.getMessage(), averageStr) + "\n");
				String standardDeviationStr = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_ANALYTICS.getMessage();
				if (changeMountDataInfo.getStandardDeviation() != null 
						&& !changeMountDataInfo.getStandardDeviation().isNaN()) {
					standardDeviationStr = changeMountDataInfo.getStandardDeviation().toString();
				}
				sb.append(String.format("%s : %s", MessageConstant.CHANGE_STANDARD_DEVIATION.getMessage(), standardDeviationStr) + "\n");
				sb.append(MessageConstant.CHANGE_COLLECTION_VALUES.getMessage());
				sb.append("(time, value) : \n");
				sb.append(changeMountDataInfo.getValuesStr());
				resultInfo.setMessage(message);
				resultInfo.setMessageOrg(message + "\n" + sb.toString());
				resultInfo.setValue(latestValue);
				resultInfo.setDisplayName(getChangeDisplayName(displayName));
				resultInfo.setApplication(monitorInfo.getChangeApplication());
				resultInfo.setItemName(itemName);
				// 共通処理
				resultInfo.setFacilityId(facilityId);
				resultInfo.setNodeDate(targetDate);
				// 監視ジョブからは使用されないため未設定
				resultInfo.setCurData(null);

				rtn.setChangeMonitorRunResultInfo(resultInfo);

				// エラーログ出力
				if (changeMountDataInfo.getErrorMessage() != null && !changeMountDataInfo.getErrorMessage().isEmpty()) {
					m_log.info(changeMountDataInfo.getErrorMessage());
				}
			}
		}

		if (monitorInfo.getCollectorFlg() || monitorInfo.getPredictionFlg()) {
			// 将来予測計算
			MonitorPredictionDataInfo predictionData = MonitorOperatorPredictionUtil.getPredictionInfo(
					monitorInfo, facilityId, displayName, itemName, targetDate);

			if (monitorInfo.getCollectorFlg() && predictionData != null) {
				// キャッシュへの将来予測情報登録
				MonitorCollectDataCache.setPredictionInfo(monitorInfo.getMonitorId(), facilityId, displayName, 
						itemName, monitorInfo.getPredictionMethod(), predictionData.getCoefficients());
			}

			if (monitorInfo.getPredictionFlg() && !predictionData.isNotEnoughFlg()) {
				// 通知情報の作成
				MonitorRunResultInfo resultInfo = new MonitorRunResultInfo();
				resultInfo.setMonitorNumericType(MonitorNumericType.TYPE_PREDICTION);
				boolean isValueNotNull = (predictionData.getValue() != null);
				resultInfo.setMonitorFlg(isValueNotNull);
				resultInfo.setCollectorResult(isValueNotNull);
				Integer checkResult = PriorityConstant.TYPE_UNKNOWN;
				if (runMonitor != null) {
					checkResult = runMonitor.getCheckResult(isValueNotNull, predictionData.getValue());
					resultInfo.setCheckResult(checkResult);
					if (checkResult == -2) {
						resultInfo.setPriority(PriorityConstant.TYPE_NONE);
					} else {
						resultInfo.setPriority(runMonitor.getPriority(checkResult));
					}
					resultInfo.setNotifyGroupId(getPredictionNotifyGroupId(runMonitor.getNotifyGroupId()));
				} else {
					resultInfo.setPriority(getPriority(monitorInfo, predictionData.getValue()));
					resultInfo.setNotifyGroupId(getPredictionNotifyGroupId(monitorInfo.getNotifyGroupId()));
				}
				resultInfo.setProcessType(checkResult != -2);
				String predictionValueStr = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_ANALYTICS.getMessage();
				if (predictionData.getValue() != null && !predictionData.getValue().isNaN()) {
					predictionValueStr = predictionData.getValue().toString();
				}
				String itemNameStr = monitorInfo.getItemName();
				if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PERFORMANCE)
						&& !displayName.equals("") && !monitorInfo.getItemName().endsWith("[" + displayName + "]")) {
					itemNameStr += "[" + displayName + "]";
				}
				// X分後をA日B時間C分後に変更する
				StringBuilder timeSb = new StringBuilder();
				int tmpTime = monitorInfo.getPredictionTarget();
				boolean appendFlg = false;
				if (tmpTime > 24 * 60) {
					// 1日以上
					timeSb.append(tmpTime / (24 * 60));
					timeSb.append(MessageConstant.DAY.getMessage());
					tmpTime = tmpTime % (24 * 60);
					appendFlg = true;
				}
				if (appendFlg || tmpTime > 60) {
					// 1時間以上
					timeSb.append(tmpTime / 60);
					timeSb.append(MessageConstant.HOUR_PERIOD.getMessage());
					tmpTime = tmpTime % 60;
				}
				timeSb.append(tmpTime);
				timeSb.append(MessageConstant.MINUTES_AFTER.getMessage());
				
				String message = String.format("%s(%s) : %s%n%s : %s", 
						itemNameStr,
									timeSb.toString(),
									predictionValueStr,
									MessageConstant.PREDICTION_METHOD.getMessage(),
									MonitorPredictionMethod.typeToMessage(monitorInfo.getPredictionMethod()));
				StringBuilder sb = new StringBuilder();
				sb.append(MessageConstant.PREDICTION_COLLECTION_VALUES.getMessage());
				sb.append("(time, value) : \n");
				sb.append(predictionData.getValuesStr());
				resultInfo.setMessage(message);
				resultInfo.setMessageOrg(message + "\n" + sb.toString());
				resultInfo.setValue(predictionData.getValue());
				resultInfo.setDisplayName(getPredictionDisplayName(displayName));
				resultInfo.setItemName(itemName);
				resultInfo.setApplication(monitorInfo.getPredictionApplication());
				// 共通処理
				resultInfo.setFacilityId(facilityId);
				resultInfo.setNodeDate(targetDate);
				// 監視ジョブからは使用されないため未設定
				resultInfo.setCurData(null);

				rtn.setPredictionMonitorRunResultInfo(resultInfo);

				// エラーログ出力
				if (predictionData.getErrorMessage() != null && !predictionData.getErrorMessage().isEmpty()) {
					m_log.info(predictionData.getErrorMessage());
				}
			}
		}
		m_log.debug("getMonitorRunResultInfo end : "
				+ "monitorId=" + monitorInfo.getMonitorId()
				+ ", facilityId=" + facilityId
				+ ", displayName=" + displayName
				+ ", itemName=" + itemName);
		return rtn;
	}

	/**
	 * 通知グループIDを返す(将来予測監視)
	 * @param notifyGroupId　通知グループID
	 * @return　通知グループID（将来予測用）
	 */
	public static String getPredictionNotifyGroupId(String notifyGroupId) {
		return CollectMonitorNotifyConstant.PREDICTION_NOTIFY_GROUPID_PREFIX + notifyGroupId;
	}
	/**
	 * 通知グループIDを返す(変化点監視)
	 * @param notifyGroupId　通知グループID
	 * @return　通知グループID（変化点用）
	 */
	public static String getChangeNotifyGroupId(String notifyGroupId) {
		return CollectMonitorNotifyConstant.CHANGE_NOTIFY_GROUPID_PREFIX + notifyGroupId;
	}
	/**
	 * ディスプレイ名を返す(将来予測監視)
	 * @param displayName　ディスプレイ名
	 * @return　ディスプレイ名（変化点用）
	 */
	public static String getPredictionDisplayName(String displayName) {
		if (displayName == null) {
			return CollectMonitorDisplayNameConstant.PREDICTION_MONITOR_DETAIL_PREFIX;
		} else {
			return CollectMonitorDisplayNameConstant.PREDICTION_MONITOR_DETAIL_PREFIX + displayName;
		}
	}
	/**
	 * ディスプレイ名を返す(変化点監視)
	 * @param displayName　ディスプレイ名
	 * @return　ディスプレイ名（変化点用）
	 */
	public static String getChangeDisplayName(String displayName) {
		if (displayName == null) {
			return CollectMonitorDisplayNameConstant.CHANGE_MONITOR_DETAIL_PREFIX;
		} else {
			return CollectMonitorDisplayNameConstant.CHANGE_MONITOR_DETAIL_PREFIX + displayName;
		}
	}

	/**
	 * 変化点監視の判定結果を返す
	 * 
	 * @param monitorInfo 監視設定
	 * @param value　判定対象の値
	 * @param collectList 判定で使用するデータ群
	 * @return checkResult
	 */
	private static Integer getChangeCheckResult(MonitorInfo monitorInfo, Double value, Double average, Double standardDeviation) {
		int rtn = PriorityConstant.TYPE_UNKNOWN;
		Double upper = 0D;
		Double lower = 0D;
		if (monitorInfo == null) {
			return rtn;
		}
		
		try {
			// 判定条件取得
			TreeMap<Integer, MonitorJudgementInfo> judgementInfoList
				= MonitorJudgementInfoCache.getMonitorJudgementMap(
				monitorInfo.getMonitorId(), MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_CHANGE.getType());

			if (standardDeviation == null) {
				// 標準偏差がnullの場合は不明のため、ここでは処理なし
			} else if (standardDeviation.doubleValue() == 0D) {
				// 標準偏差=0の場合
				// 算出された平均値及びキャッシュのvalueはfloatで管理されているので、比較はfloatで行う
				// TODO 本対応は収集値や平均値等がrealとしてDBに登録およびキャッシュで保持されるための対処
				// 改善チケット1135 の対応がされた場合は、doubleで比較が必要となる。
				if (average.floatValue() != value.floatValue()) {
					// 平均値と値が異なる場合は「危険」
					rtn  = PriorityConstant.TYPE_CRITICAL;
				} else {
					// 平均値と値が同じ場合は、値は0とし、範囲には標準偏差を掛けない。
					// 例） 「情報」の判定が「-1σ」以上「2σ」未満とした場合、
					//    値0が-1以上で2未満であるため、「情報である」と判定する。
					MonitorJudgementInfo judgementInfo = judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
					upper = judgementInfo.getThresholdUpperLimit();
					lower = judgementInfo.getThresholdLowerLimit();
					m_log.debug("getCheckResult(INFO) monitorId=" + monitorInfo.getMonitorId() + ", value=0D" + ", upper=" + upper + ", lower=" + lower);
					// 通知をチェック
					if (upper != null && lower != null) {
						if(0D >= lower && 0D < upper){
							rtn = PriorityConstant.TYPE_INFO;
						} else {
							// 警告の範囲チェック
							judgementInfo = judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
							upper = judgementInfo.getThresholdUpperLimit();
							lower = judgementInfo.getThresholdLowerLimit();
							m_log.debug("getCheckResult(WARN) monitorId=" + monitorInfo.getMonitorId() + ", value=0D" + ", upper=" + upper + ", lower=" + lower);
							if(0D >= lower && 0D < upper){
								rtn = PriorityConstant.TYPE_WARNING;
							}
							else{
								// 危険（通知・警告以外）
								rtn = PriorityConstant.TYPE_CRITICAL;
							}
						}
					}
				}
			} else {
				// 範囲情報の取得
				MonitorJudgementInfo judgementInfo = judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
				upper = OperatorChangeUtil.getStandardDeviation(average, standardDeviation, judgementInfo.getThresholdUpperLimit());
				lower = OperatorChangeUtil.getStandardDeviation(average, standardDeviation, judgementInfo.getThresholdLowerLimit());
				m_log.debug("getCheckResult(INFO) monitorId=" + monitorInfo.getMonitorId() + ", value=" + value + ", upper=" + upper + ", lower=" + lower);
				// 通知をチェック
				if (upper != null && lower != null) {
					if(value >= lower && value < upper){
						rtn = PriorityConstant.TYPE_INFO;
					} else {
						// 警告の範囲チェック
						judgementInfo = judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
						upper = OperatorChangeUtil.getStandardDeviation(average, standardDeviation, judgementInfo.getThresholdUpperLimit());
						lower = OperatorChangeUtil.getStandardDeviation(average, standardDeviation, judgementInfo.getThresholdLowerLimit());
						m_log.debug("getCheckResult(WARN) monitorId=" + monitorInfo.getMonitorId() + ", value=" + value + ", upper=" + upper + ", lower=" + lower);
						if(value >= lower && value < upper){
							rtn = PriorityConstant.TYPE_WARNING;
						}
						else{
							// 危険（通知・警告以外）
							rtn = PriorityConstant.TYPE_CRITICAL;
						}
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// 何もしない
		}
		return rtn;
	}

	/**
	 * 監視の判定結果を返す
	 * 
	 * @param monitorInfo 監視設定
	 * @param value　判定対象の値
	 * @param collectList 判定で使用するデータ群
	 * @return
	 */
	private static Integer getPriority(MonitorInfo monitorInfo, Double value) {
		int rtn = PriorityConstant.TYPE_UNKNOWN;

		if (monitorInfo == null || value == null) {
			return rtn;
		}

		TreeMap<Integer, MonitorJudgementInfo> judgementInfoList
			= MonitorJudgementInfoCache.getMonitorJudgementMap(
			monitorInfo.getMonitorId(), MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_BASIC.getType());

		// 範囲情報の取得
		MonitorJudgementInfo infoJudgementInfo = judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
		MonitorJudgementInfo warnJudgementInfo = judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
		
		// MAIN
		if (!Double.isNaN(value)) {
			// if numeric value is defined
			if (value >= infoJudgementInfo.getThresholdLowerLimit() 
					&& value < infoJudgementInfo.getThresholdUpperLimit()) {
				rtn = PriorityConstant.TYPE_INFO;
			} else if (value >= warnJudgementInfo.getThresholdLowerLimit()
						&& value < warnJudgementInfo.getThresholdUpperLimit()) {
				rtn = PriorityConstant.TYPE_WARNING;
			} else {
				rtn = PriorityConstant.TYPE_CRITICAL;
			}
		}
		return rtn;
	}

	/**
	 * 将来予測監視、変化量監視の通知情報、平均値、標準偏差を格納
	 */
	public static class CollectMonitorDataInfo {

		// 平均値
		private Double average;
		// 標準偏差
		private Double standardDeviation;
		// 通知結果（変化量監視）
		MonitorRunResultInfo changeMonitorRunResultInfo;
		// 通知結果（将来予測監視）
		MonitorRunResultInfo predictionMonitorRunResultInfo;

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
		public MonitorRunResultInfo getChangeMonitorRunResultInfo() {
			return changeMonitorRunResultInfo;
		}
		public void setChangeMonitorRunResultInfo(
				MonitorRunResultInfo changeMonitorRunResultInfo) {
			this.changeMonitorRunResultInfo = changeMonitorRunResultInfo;
		}
		public MonitorRunResultInfo getPredictionMonitorRunResultInfo() {
			return predictionMonitorRunResultInfo;
		}
		public void setPredictionMonitorRunResultInfo(
				MonitorRunResultInfo predictionMonitorRunResultInfo) {
			this.predictionMonitorRunResultInfo = predictionMonitorRunResultInfo;
		}
	}
}
