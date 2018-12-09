/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 標準出力・標準エラー出力をlog4jに代替出力するクラス<br/>
 */
public class StdOutErrLog {

	public static final Log log = LogFactory.getLog(StdOutErrLog.class);

	/**
	 * 標準出力・標準エラー出力の出力先をlog4jにセットする。<br/>
	 */
	public static void initialize() {
		System.setOut(new PrintStream(new LoggerStream(log)));
		System.setErr(new PrintStream(new LoggerStream(log)));
	}
}
