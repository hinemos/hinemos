/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.bean;

import java.util.ArrayList;
import java.util.List;


/**
 * SNMPプロトコルの定義クラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SnmpProtocolConstant {
	/**  MD5 */
	public static final String  MD5 = "MD5";

	/**  SHA */
	public static final String  SHA = "SHA";

	/**  DES */
	public static final String  DES = "DES";

	/**  AES */
	public static final String  AES = "AES";

	// 認証プロトコル
	public static List<String> getAuthProtocol() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(MD5);
		list.add(SHA);
		return list;
	}
	
	// 暗号化プロトコル
	public static List<String> getPrivProtocol() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(DES);
		list.add(AES);
		return list;
	}
	
	
}