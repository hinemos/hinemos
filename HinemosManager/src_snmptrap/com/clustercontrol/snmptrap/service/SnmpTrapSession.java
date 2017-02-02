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