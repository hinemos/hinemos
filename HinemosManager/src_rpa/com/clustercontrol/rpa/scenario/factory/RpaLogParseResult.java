/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import com.clustercontrol.bean.PriorityConstant;

/**
 * RPAツールのログ解析結果を格納するクラス
 */
public class RpaLogParseResult {
	/**
	 * 出力時刻(long型)
	 */
	private long logTime;
	
	/**
	 * 重要度<BR>
	 * <BR>
	 * CRITICAL - 危険<BR>
	 * UNKNOWN - 不明<BR>
	 * WARNING - 警告<BR>
	 * INFO - 通知<BR>
	 * NONE - なし
	 */
	private Priority priority;
	
	public static enum Priority {
		CRITICAL(0),
		UNKNOWN(1),
		WARNING(2),
		INFO(3),
		NONE(4);
		
		/**
		 * @see PriorityConstant
		 */
		private final Integer code;
		
		private Priority(final Integer code){
			this.code = code;
		}
		
		public Integer getCode() {
			return code;
		}
	}
	
	/**
	 * メッセージ本文
	 */
	private String logMessage;
	
	
	/**
	 * シナリオ識別文字列
	 */
	private String identifyString;
	
	/**
	 * 出力時刻(long型)
	 */
	public long getLogTime() {
		return logTime;
	}

	/**
	 * 重要度<BR>
	 * <BR>
	 * CRITICAL - 危険<BR>
	 * WARNING - 警告<BR>
	 * INFO - 通知<BR>
	 * UNKNOWN - 不明<BR>
	 * NONE - なし
	 */
	public Priority getPriority() {
		return priority;
	}

	/**
	 * メッセージ本文
	 */
	public String getLogMessage() {
		if (logMessage == null) {
			return "";
		}
		return logMessage;
	}

	public void setLogTime(long logTime) {
		this.logTime = logTime;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	/**
	 * シナリオ識別文字列
	 */
	public String getIdentifyString() {
		if (identifyString == null) {
			return "";
		}
		return identifyString;
	}

	public void setIdentifyString(String identifyString) {
		this.identifyString = identifyString;
	}
}
