/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

public class NodeConfigCollectReferenceTimeUtil {

	// ログ出力関連.
	private static Log m_log = LogFactory.getLog(NodeConfigCollectReferenceTimeUtil.class);
	private static final String DELIMITER = "() : ";

	// 基準時刻(HH:mm 24時間表記).
	public static final String REFERENCE_TIME = "00:00";

	// 書式チェック済みの基準時刻
	private static String collectReferenceTime;

	// マネージャ起動時に書式チェックを実施
	static {
		String collectBaseTime = REFERENCE_TIME;
		try {
			collectBaseTime = HinemosPropertyCommon.repository_node_config_collect_reference_time.getStringValue();
			m_log.debug(
					"static" + DELIMITER + HinemosPropertyCommon.repository_node_config_collect_reference_time.name()
							+ "= " + collectBaseTime);
		} catch (Exception e) {
			m_log.warn("static" + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		// 書式の確認.
		Pattern p = Pattern.compile("([0-2]\\d):([0-5]\\d)");
		Matcher m = p.matcher(collectBaseTime);
		if (m.matches()) {
			int hour = Integer.parseInt(m.group(1));
			int minute = Integer.parseInt(m.group(2));
			// 時刻として正しいか.
			if (0 > hour || hour >= 24 || 0 > minute || minute >= 60) {
				m_log.warn(HinemosPropertyCommon.repository_node_config_collect_reference_time
						.message_invalid(collectBaseTime));
				putInternalLog(HinemosPropertyCommon.repository_node_config_collect_reference_time, collectBaseTime);
				collectBaseTime = REFERENCE_TIME;
			}
		} else {
			m_log.warn(HinemosPropertyCommon.repository_node_config_collect_reference_time
					.message_invalid(collectBaseTime));
			putInternalLog(HinemosPropertyCommon.repository_node_config_collect_reference_time, collectBaseTime);
			collectBaseTime = REFERENCE_TIME;
		}
		collectReferenceTime = collectBaseTime;
	}

	/**
	 * マネージャ起動時に書式チェック済みの収集基準時刻を取得.
	 */
	public static String getReferenceTime() {
		return collectReferenceTime;
	}

	/**
	 * Internalエラー出力.
	 */
	private static void putInternalLog(HinemosPropertyCommon property, String defaultValue) {
		String[] args = {};
		AplLogger.put(InternalIdCommon.NODE_CONFIG_SETTING_SYS_003, args, property.message_invalid(defaultValue));
	}

	/**
	 * 収集基準時刻範囲を取得.
	 */
	public static Long getLoadDstrbRange() {
		Long collectBaseMin = HinemosPropertyCommon.repository_node_config_collect_load_distribution_range
				.getNumericValue();
		return collectBaseMin;
	}

}
