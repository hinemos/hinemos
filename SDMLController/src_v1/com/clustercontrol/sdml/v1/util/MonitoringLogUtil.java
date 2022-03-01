/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.sdml.v1.constant.SdmlCollectStringTag;
import com.clustercontrol.sdml.v1.constant.SdmlMonitorTypeEnum;

public class MonitoringLogUtil {
	private static Log logger = LogFactory.getLog( MonitoringLogUtil.class );

	// SDML監視ログ書式
	private static final String FORMAT_LOG = "^(%s) (%s) (%s)";
	// SDML監視ログ書式（フィルタ用）
	private static final String FORMAT_LOG_FILTER = "^%s %s %s";
	// SDML監視ログ書式（フィルタ用）
	private static final String FORMAT_INT_GCC_FILTER = "^%s %s \\[JVM GC\\] GCName=%s, .*";

	// タイムスタンプ
	private static final String PTN_TIMESTAMP = "\\d{4}-\\d{2}-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2},\\d{3}[+-]\\d{2}\\:\\d{2}";
	// SDML監視種別ID
	private static final String PTN_MONITOR_TYPE = "[A-Z]{3}_[A-Z]{3}";
	// メッセージ部
	private static final String PTN_MESSAGE = ".*";
	// SDML監視ログ共通パターン
	private static final String LOG_PTN_COMMON = String.format(FORMAT_LOG,
			PTN_TIMESTAMP,
			PTN_MONITOR_TYPE,
			PTN_MESSAGE);


	private static final String FORMAT_PARAM1 = "%s %s";
	private static final String FORMAT_PARAM2 = "%s %s, %s";
	private static final String FORMAT_PARAM3 = "%s %s, %s, %s";
	
	// アプリケーションログ監視
	private static final String PTN_LOG_APP_LEVEL = "([a-zA-Z]+)";
	private static final String PTN_LOG_APP_MESSAGE = ".*";
	private static final String MSG_PTN_LOG_APP = String.format(FORMAT_PARAM1,
			PTN_LOG_APP_LEVEL,
			PTN_LOG_APP_MESSAGE);

	// プロセス内部監視共通
	private static final String PTN_INT_COMMON_ITEM_NAME = "\\[([^\\[\\]]*)\\]";

	// デッドロック監視
	private static final String PTN_INT_DLK_RESULT = ".*";
	private static final String MSG_PTN_INT_DLK = String.format(FORMAT_PARAM1,
			PTN_INT_COMMON_ITEM_NAME,
			PTN_INT_DLK_RESULT);

	// ヒープ未使用量監視
	private static final String PTN_INT_HPR_REMAINING = "Remaining=(\\d+)MB";
	private static final String PTN_INT_HPR_THRESHOLD = "Threshold=(\\d+)MB";
	private static final String MSG_PTN_INT_HPR = String.format(FORMAT_PARAM1,
			PTN_INT_COMMON_ITEM_NAME,
			PTN_INT_HPR_REMAINING);
	private static final String FORMAT_INT_HPR_THRESHOLD = String.format(FORMAT_PARAM2,
			PTN_INT_COMMON_ITEM_NAME,
			PTN_INT_HPR_REMAINING,
			PTN_INT_HPR_THRESHOLD);

	// GC発生頻度監視
	private static final String PTN_INT_GCC_NAME = "GCName=([a-zA-Z0-9 ]+)";
	private static final String PTN_INT_GCC_COUNT = "Count=(\\d+)";
	private static final String PTN_INT_GCC_THRESHOLD = "Threshold=(\\d+)";
	private static final String MSG_PTN_INT_GCC = String.format(FORMAT_PARAM2,
			PTN_INT_COMMON_ITEM_NAME,
			PTN_INT_GCC_NAME,
			PTN_INT_GCC_COUNT);
	private static final String MSG_PTN_INT_GCC_THRESHOLD = String.format(FORMAT_PARAM3,
			PTN_INT_COMMON_ITEM_NAME,
			PTN_INT_GCC_NAME,
			PTN_INT_GCC_COUNT,
			PTN_INT_GCC_THRESHOLD);

	// CPU使用率監視
	private static final String PTN_INT_CPU_PID = "PID=(\\d+)";
	private static final String PTN_INT_CPU_USAGE = "Usage=(\\d+)%";
	private static final String PTN_INT_CPU_THRESHOLD = "Threshold=(\\d+)%";
	private static final String MSG_PTN_INT_CPU = String.format(FORMAT_PARAM2,
			PTN_INT_COMMON_ITEM_NAME,
			PTN_INT_CPU_PID,
			PTN_INT_CPU_USAGE);
	private static final String MSG_PTN_INT_CPU_THRESHOLD = String.format(FORMAT_PARAM3,
			PTN_INT_COMMON_ITEM_NAME,
			PTN_INT_CPU_PID,
			PTN_INT_CPU_USAGE,
			PTN_INT_CPU_THRESHOLD);


	/**
	 * SDML監視ログから抽出したタグを返却する
	 * 
	 * @param logMessage
	 * @return
	 */
	public static List<StringSampleTag> parse(String logMessage) {
		List<StringSampleTag> rtn = new ArrayList<>();

		String sdmlMonitorTypeId = null;
		String message = null;

		Pattern pattern = Pattern.compile(LOG_PTN_COMMON);
		Matcher matcher = pattern.matcher(logMessage);
		if (matcher.matches()) {
			rtn.add(new StringSampleTag(SdmlCollectStringTag.TIMESTAMP_IN_LOG.name(),
					SdmlCollectStringTag.TIMESTAMP_IN_LOG.valueType(), matcher.group(1)));
			sdmlMonitorTypeId = matcher.group(2);
			message = matcher.group(3);
		} else {
			logger.debug("parse() : unmatch to monitoring log format. log=" + logMessage);
			return rtn;
		}
		
		SdmlMonitorTypeEnum sdmlMonitorType = SdmlMonitorTypeEnum.toEnum(sdmlMonitorTypeId);
		if (sdmlMonitorType == null) {
			logger.debug("parse() : unknown monitor type. monitorType=" + sdmlMonitorTypeId);
			return rtn;
		}
		Pattern mPattern = null;
		Matcher mMatcher = null;
		switch (sdmlMonitorType) {
		case LOG_APPLICATION:
			mPattern = Pattern.compile(MSG_PTN_LOG_APP);
			mMatcher = mPattern.matcher(message);
			if (mMatcher.matches()) {
				rtn.add(new StringSampleTag(SdmlCollectStringTag.LogLevel.name(),
						SdmlCollectStringTag.LogLevel.valueType(), mMatcher.group(1)));
			}
			break;
		case INTERNAL_DEADLOCK:
			mPattern = Pattern.compile(MSG_PTN_INT_DLK);
			mMatcher = mPattern.matcher(message);
			if (mMatcher.matches()) {
				rtn.add(new StringSampleTag(SdmlCollectStringTag.ItemName.name(),
						SdmlCollectStringTag.ItemName.valueType(), mMatcher.group(1)));
			}
			break;
		case INTERNAL_HEAP_REMAINING:
			mPattern = Pattern.compile(MSG_PTN_INT_HPR);
			mMatcher = mPattern.matcher(message);
			if (mMatcher.matches()) {
				rtn.add(new StringSampleTag(SdmlCollectStringTag.ItemName.name(),
						SdmlCollectStringTag.ItemName.valueType(), mMatcher.group(1)));
				rtn.add(new StringSampleTag(SdmlCollectStringTag.Remaining.name(),
						SdmlCollectStringTag.Remaining.valueType(), mMatcher.group(2)));
			} else {
				// 閾値ありのパターン
				mPattern = Pattern.compile(FORMAT_INT_HPR_THRESHOLD);
				mMatcher = mPattern.matcher(message);
				if (mMatcher.matches()) {
					rtn.add(new StringSampleTag(SdmlCollectStringTag.ItemName.name(),
							SdmlCollectStringTag.ItemName.valueType(), mMatcher.group(1)));
					rtn.add(new StringSampleTag(SdmlCollectStringTag.Remaining.name(),
							SdmlCollectStringTag.Remaining.valueType(), mMatcher.group(2)));
					rtn.add(new StringSampleTag(SdmlCollectStringTag.Threshold.name(),
							SdmlCollectStringTag.Threshold.valueType(), mMatcher.group(3)));
				}
			}
			break;
		case INTERNAL_GC_COUNT:
			mPattern = Pattern.compile(MSG_PTN_INT_GCC);
			mMatcher = mPattern.matcher(message);
			if (mMatcher.matches()) {
				rtn.add(new StringSampleTag(SdmlCollectStringTag.ItemName.name(),
						SdmlCollectStringTag.ItemName.valueType(), mMatcher.group(1)));
				rtn.add(new StringSampleTag(SdmlCollectStringTag.GCName.name(), SdmlCollectStringTag.GCName.valueType(),
						mMatcher.group(2)));
				rtn.add(new StringSampleTag(SdmlCollectStringTag.Count.name(), SdmlCollectStringTag.Count.valueType(),
						mMatcher.group(3)));
			} else {
				// 閾値ありのパターン
				mPattern = Pattern.compile(MSG_PTN_INT_GCC_THRESHOLD);
				mMatcher = mPattern.matcher(message);
				if (mMatcher.matches()) {
					rtn.add(new StringSampleTag(SdmlCollectStringTag.ItemName.name(),
							SdmlCollectStringTag.ItemName.valueType(), mMatcher.group(1)));
					rtn.add(new StringSampleTag(SdmlCollectStringTag.GCName.name(),
							SdmlCollectStringTag.GCName.valueType(), mMatcher.group(2)));
					rtn.add(new StringSampleTag(SdmlCollectStringTag.Count.name(),
							SdmlCollectStringTag.Count.valueType(), mMatcher.group(3)));
					rtn.add(new StringSampleTag(SdmlCollectStringTag.Threshold.name(),
							SdmlCollectStringTag.Threshold.valueType(), mMatcher.group(4)));
				}
			}
			break;
		case INTERNAL_CPU_USAGE:
			mPattern = Pattern.compile(MSG_PTN_INT_CPU);
			mMatcher = mPattern.matcher(message);
			if (mMatcher.matches()) {
				rtn.add(new StringSampleTag(SdmlCollectStringTag.ItemName.name(),
						SdmlCollectStringTag.ItemName.valueType(), mMatcher.group(1)));
				rtn.add(new StringSampleTag(SdmlCollectStringTag.PID.name(), SdmlCollectStringTag.PID.valueType(),
						mMatcher.group(2)));
				rtn.add(new StringSampleTag(SdmlCollectStringTag.Usage.name(), SdmlCollectStringTag.Usage.valueType(),
						mMatcher.group(3)));
			} else {
				// 閾値ありのパターン
				mPattern = Pattern.compile(MSG_PTN_INT_CPU_THRESHOLD);
				mMatcher = mPattern.matcher(message);
				if (mMatcher.matches()) {
					rtn.add(new StringSampleTag(SdmlCollectStringTag.ItemName.name(),
							SdmlCollectStringTag.ItemName.valueType(), mMatcher.group(1)));
					rtn.add(new StringSampleTag(SdmlCollectStringTag.PID.name(), SdmlCollectStringTag.PID.valueType(),
							mMatcher.group(2)));
					rtn.add(new StringSampleTag(SdmlCollectStringTag.Usage.name(),
							SdmlCollectStringTag.Usage.valueType(), mMatcher.group(3)));
					rtn.add(new StringSampleTag(SdmlCollectStringTag.Threshold.name(),
							SdmlCollectStringTag.Threshold.valueType(), mMatcher.group(4)));
				}
			}
			break;

		default:
			// 到達しない
			logger.error("parse() : invalid sdmlMonitorType");
		}
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (StringSampleTag tag : rtn) {
				sb.append("[key=" + tag.getKey());
				sb.append(", type=" + tag.getType());
				sb.append(", value=" + tag.getValue());
				sb.append("]");
			}
			logger.debug("parse() : finish. tags=" + sb.toString());
		}
		return rtn;
	}

	/**
	 * デッドロック監視 フィルタのパターンに設定する用の文字列
	 * 
	 * @return
	 */
	public static String getFilterPatternIntDeadlock() {
		return String.format(FORMAT_LOG_FILTER, PTN_TIMESTAMP, SdmlMonitorTypeEnum.INTERNAL_DEADLOCK.getId(),
				PTN_MESSAGE);
	}

	/**
	 * ヒープ未使用量監視 フィルタのパターンに設定する用の文字列
	 * 
	 * @return
	 */
	public static String getFilterPatternIntHeapRemaining() {
		return String.format(FORMAT_LOG_FILTER, PTN_TIMESTAMP, SdmlMonitorTypeEnum.INTERNAL_HEAP_REMAINING.getId(),
				PTN_MESSAGE);
	}

	/**
	 * GC発生頻度監視 フィルタのパターンに設定する用の文字列
	 * 
	 * @return
	 */
	public static String getFilterPatternIntGcCount(String gcName) {
		return String.format(FORMAT_INT_GCC_FILTER, PTN_TIMESTAMP, SdmlMonitorTypeEnum.INTERNAL_GC_COUNT.getId(),
				gcName);
	}

	/**
	 * CPU使用率監視 フィルタのパターンに設定する用の文字列
	 * 
	 * @return
	 */
	public static String getFilterPatternIntCpuUsage() {
		return String.format(FORMAT_LOG_FILTER, PTN_TIMESTAMP, SdmlMonitorTypeEnum.INTERNAL_CPU_USAGE.getId(),
				PTN_MESSAGE);
	}
}
