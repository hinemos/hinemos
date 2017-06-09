/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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