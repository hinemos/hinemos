/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.service;


/**
 * joeSnmp や snmp4j などのライブラリーを隠蔽するために使用するインターフェース。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public interface SnmpTrapSession {
	void open();
	void close();
	void setListenAddress(String address, int port);
	void registReceiver(SnmpTrapReceiver receiver);
}