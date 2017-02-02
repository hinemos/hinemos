/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
