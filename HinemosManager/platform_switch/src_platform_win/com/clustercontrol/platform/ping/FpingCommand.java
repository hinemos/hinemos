/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.ping;

import java.util.HashSet;

/**
 * 環境差分のある固定値を定数として格納するクラス（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class FpingCommand {

	public static String[] getCommand(String fpingPath, HashSet<String> hosts, int sentCount, int sentInterval, int timeout, int bytes) {
		
		//コマンド実行する配列を初期化する。
		String cmd[] = new String[] {
				"cmd",
				"/c",
				"powershell"
				+ " \""
				+ "&\'"+ fpingPath + "\'"
				+ " -C " + sentCount
				+ " -p " + sentInterval
				+ " -t " + timeout
				+ " -b " + bytes
				+ " -q "
				+ " -g " + String.join("`,", hosts)
				+ "\""
		};
		
		return cmd;
	}
}