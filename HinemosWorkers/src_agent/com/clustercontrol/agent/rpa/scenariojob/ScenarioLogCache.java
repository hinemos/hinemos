/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.agent.job.RunHistoryUtil;

/**
 * RPAシナリオのログで最も終了値判定条件の優先度の高いログを保持するクラス
 */
public class ScenarioLogCache {
	/** ロガー */
	static private Log m_log = LogFactory.getLog(ScenarioLogCache.class);
	/** インスタンス */
	private static final Map<String, ScenarioLogCache> instances = new ConcurrentHashMap<>();
	/** ログの優先順位 */
	private Integer orderNo;
	/** 出力されたログ */
	private String logMessage;
	/** ログファイル名 */
	private String logfileName;

	/**
	 * コンストラクタ
	 */
	private ScenarioLogCache() {
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public String getLogfileName() {
		return logfileName;
	}

	/**
	 * ジョブ実行指示情報に対応するインスタンスを返します。
	 * 
	 * @return インスタンス
	 */
	public static ScenarioLogCache get(AgtRunInstructionInfoResponse runInstructionInfo) {
		synchronized (ScenarioLogCache.class) {
			String key = RunHistoryUtil.getKey(runInstructionInfo);
			if (instances.containsKey(key)) {
				m_log.info("get() : instance exists, key=" + key);
				return instances.get(key);
			}
			ScenarioLogCache instance = new ScenarioLogCache();
			instances.put(key, instance);
			m_log.info("get() : instance created, key=" + key);
			return instance;
		}
	}

	/**
	 * インスタンスを削除します。<br>
	 * ScenarioMonitorThreadから呼ばれます。
	 * 
	 * @param runInstructionInfo
	 *            ジョブ実行指示情報
	 */
	public static void remove(AgtRunInstructionInfoResponse runInstructionInfo) {
		// 不要になった情報を削除
		synchronized (ScenarioLogCache.class) {
			String key = RunHistoryUtil.getKey(runInstructionInfo);
			instances.remove(key);
			m_log.info("remove() : key=" + key);
		}
	}

	/**
	 * 条件にマッチしたログを優先度と共に格納します。<br>
	 * 既により優先度の高いログが格納されている場合は処理を行いません。<br>
	 * MonitorStringUtilから呼ばれます。
	 * 
	 * @param orderNo
	 *            優先順序
	 * @param logMessage
	 *            出力されたログ
	 * @param logfileName
	 *            ログファイル名
	 */
	public void put(Integer orderNo, String logMessage, String logfileName) {
		// ログファイル監視で条件にマッチしたログを記憶し終了値判定で使用する
		synchronized (this) {
			// 優先度の高いログの場合のみ更新
			m_log.debug("put() : orderNo=" + orderNo + ", logMessage=" + logMessage + ", logfileName=" + logfileName);
			if (this.orderNo == null || orderNo < this.orderNo) {
				this.orderNo = orderNo;
				this.logMessage = logMessage;
				this.logfileName = logfileName;
				m_log.info("put() : updated");
			}
		}
		m_log.info("put() : orderNo=" + orderNo);
	}
}
