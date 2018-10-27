/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;

/**
 * ジョブ実行履歴を管理するユーティリティクラス<BR>
 * 
 * ジョブ実行のステータスを管理します。<BR>
 * ジョブの実行前後処理としてハッシュテーブルに追加・削除を行います。
 * 
 */
public class RunHistoryUtil {

	//ロガー
	private static Log m_log = LogFactory.getLog(RunHistoryUtil.class);
	private static ConcurrentHashMap<String, RunInstructionInfo> runHistory 
		= new ConcurrentHashMap<>();

	/**
	 * 実行履歴を追加します。<BR>
	 * @param runInstructionInfo
	 */
	public static void addRunHistory(RunInstructionInfo runInstructionInfo) {
		runHistory.put(getKey(runInstructionInfo), runInstructionInfo);
	}

	/**
	 * 実行履歴を削除します。<BR>
	 * @param runInstructionInfo
	 */
	public static void delRunHistory(RunInstructionInfo runInstructionInfo) {
		runHistory.remove(getKey(runInstructionInfo));
	}

	/**
	 * 実行履歴を検索します。<BR>
	 * @param runInstructionInfo
	 * @return runInstructionInfo
	 */
	public static RunInstructionInfo findRunHistory(RunInstructionInfo runInstructionInfo) {
		return runHistory.get(getKey(runInstructionInfo));
	}

	/**
	 * 実行履歴を検索します。<BR>
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param facilityId
	 * @return runInstructionInfo
	 */
	public static RunInstructionInfo findRunHistory(
			String sessionId,
			String jobunitId,
			String jobId,
			String facilityId) {
		return runHistory.get(sessionId + "," + jobunitId + "," + jobId + "," + facilityId);
	}

	/**
	 * 実行履歴を検索します。<BR>
	 * @param sessionId
	 * @return runInstructionInfo
	 */
	public static List<RunInstructionInfo> findRunHistoryBySessionId(String sessionId) {
		List<RunInstructionInfo> runInstructionInfoList = new ArrayList<>();
		for (Map.Entry<String, RunInstructionInfo> entry : runHistory.entrySet()) {
			if (entry.getKey().startsWith(sessionId + ",")) {
				runInstructionInfoList.add(entry.getValue());
			}
		}
		m_log.debug("findRunHistoryBySessionId : size=" + runInstructionInfoList.size());
		return runInstructionInfoList;
	}

	/**
	 * ハッシュテーブルに格納するため実行履歴Keyを作成します。
	 * @param info
	 * @return
	 */
	protected static String getKey(RunInstructionInfo info) {
		return info.getSessionId() + "," + info.getJobunitId() + "," + info.getJobId() + "," + info.getFacilityId();
	}
}
