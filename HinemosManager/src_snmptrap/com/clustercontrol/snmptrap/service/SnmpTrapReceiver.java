/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.service;

import java.util.List;

import com.clustercontrol.snmptrap.bean.SnmpTrap;

/**
 * SNMP Trap を受信するリスナー。
 * このインターフェースの実装は、SnmpTrapSession へ渡される。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public interface SnmpTrapReceiver {
	void onReceived(List<SnmpTrap> receivedTrapList);
}