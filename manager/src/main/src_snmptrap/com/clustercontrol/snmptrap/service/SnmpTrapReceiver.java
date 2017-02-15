/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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