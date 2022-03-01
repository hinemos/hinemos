/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

/**
 * RPAツールのログを解析するクラスのインターフェース
 *
 */
public interface RpaLogParser {
	/**
	 * ログ本文をパースする。<BR>
	 * 出力時刻、重要度、メッセージはRpaParseResultに格納して返す。<BR>
	 * パースに失敗した場合はRpaParseExceptionをthrowする。
	 * 
	 * @param logText ログ本文
	 * @return RpaParseResult 解析結果(出力時刻、重要度、メッセージ本文)
	 * @throws RpaLogParseException
	 */
	public RpaLogParseResult parse(String logText) throws RpaLogParseException;
}
