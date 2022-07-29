/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.util;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.util.HinemosTime;

/**
 * 
 * レポート生成に失敗したファイル名を管理するクラス
 * 
 */
public class ReportFileFailureListManager {

	private static Log m_log = LogFactory.getLog(ReportFileFailureListManager.class);
	
	// 生成失敗一覧を保持するMap<レポート名, 生成日時>
	private static ConcurrentHashMap<String, Long> failureMap = new ConcurrentHashMap<>();
	
	public static void regist(String fileName) {
		failureMap.put(fileName, HinemosTime.currentTimeMillis());
	}

	public static boolean isFailed(String fileName) {
		boolean ret = failureMap.containsKey(fileName);

		delete();

		return ret;
	}

	private static void delete() {
		long now = HinemosTime.currentTimeMillis();
		long period = HinemosPropertyCommon.reporting_failure_retention_period.getIntegerValue();
		failureMap.values().removeIf(value -> value < (now - period));
		
		m_log.info("delete() failureMap=" + failureMap); 
	}
}
