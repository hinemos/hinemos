/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.util;

import java.util.List;

import com.clustercontrol.systemlog.bean.SyslogMessage;

/**
 * syslogを受信した際の処理を定義するクラス
 */
public interface SyslogHandler {

	/**
	 * SyslogReceiverがsyslogを受信する度に、そのsyslogを引数としてこのAPIが呼ばれる。
	 * なお、非同期で呼ばれないため、この処理が終了するまで次のsyslogは処理されない。
	 * @param syslogList SyslogReceiverが受信したsyslog
	 */
	public void syslogReceived(List<SyslogMessage> syslogList);

	/**
	 * SyslogReceiverのstartが呼ばれた際に呼ばれるAPI。
	 * syslogReceivedが呼ばれる前に初期化処理が必要な場合に実装する。
	 */
	public void start();

	/**
	 * SyslogReceiverのshutdownが呼ばれた際に呼ばれるAPI。
	 * 各種リソースの解放処理などで終了処理が必要な場合に実装する。
	 */
	public void shutdown();

}
