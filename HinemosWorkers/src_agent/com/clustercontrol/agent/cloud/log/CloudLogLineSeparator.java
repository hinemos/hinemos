/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log;

import java.util.regex.Matcher;
import com.clustercontrol.agent.util.filemonitor.LineSeparator;

/**
 * LineSeparatorにクラウドログ監視用の便利メソッドを追加したクラス
 * 
 */
public class CloudLogLineSeparator extends LineSeparator {
	public CloudLogLineSeparator(String fileReturnCode, String startRegexString, String endRegexString) {
		super(fileReturnCode, startRegexString, endRegexString);
	}

	/**
	 * 先頭パターンの場合に行頭が一致なら-1を返す
	 * それ以外の挙動はLineSeparator.searchと同じ
	 * @param cbuf
	 * @return
	 */
	public int searchInitialMatch(CharSequence cbuf) {

		if (startRegexPattern != null) {
			return searchByStartRegexFirst(cbuf);
		}

		return search(cbuf);
	}

	// 行頭が一致した際には-1を返す
	private int searchByStartRegexFirst(CharSequence str) {
		Matcher matcher = startRegexPattern.matcher(str);
		if (matcher.find()) {
			int index = matcher.start();
			if (index > 0) {
				return index;
			}
		}
		return -1;
	}

}