/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

public class EnvUtil {

	/**
	 * 実行環境がWindowsか判定する。
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}

	// for debug
	public static void main(String[] args) {
		System.out.println(isWindows());
	}
}
