/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.ping;

import java.util.HashSet;
import java.util.Iterator;

/**
 * ReachAddressFpingクラスの環境差分（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class FpingCommand {
	
	public static String[] getCommand(String fpingPath, HashSet<String> hosts, int sentCount, int sentInterval, int timeout, int bytes) {
		//コマンド実行する配列を初期化する。
		int length = 6 + hosts.size();
		String cmd[] = new String[length];

		cmd[0] = fpingPath;
		cmd[1] = "-C" + sentCount;
		cmd[2] = "-p" + sentInterval;
		cmd[3] = "-t" + timeout;
		cmd[4] = "-b" + bytes;
		cmd[5] = "-q" ;

		//コマンドを実行するために値を詰め替えます。
		Iterator<String> itr = hosts.iterator();
		int i = 0;
		while(itr.hasNext()) {
			cmd[i + 6] = itr.next();
			i++;
		}
		
		return cmd;
	}
}
