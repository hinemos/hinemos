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
