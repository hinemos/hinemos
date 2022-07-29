/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.snmp;

import java.util.ArrayList;

import com.clustercontrol.utility.mib.MibType;
import com.clustercontrol.utility.mib.value.ObjectIdentifierValue;

/**
 * MIBのパース処理(Trap関連情報取得)における NOTIFICATION-TYPEを表すクラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class SnmpNotificationType extends MibType{
	
	/**
	 * NOTIFICATION-TYPE に設定されたDESCRIPTION
	 */
	private String typeDesc;

	/**
	 * NOTIFICATION-TYPE に設定されたOBJECTS
	 */
	private ArrayList<ObjectIdentifierValue> typeObjectsList;

	/**
	 * NOTIFICATION-TYPEを表すインスタンスを作成
	 *
	 * @param typeDesc TYPE内のDESCRIPTION
	 * @param typeObjects  TYPE内のOBJECTS（ObjectIdentifierValueのArrayList）
	 */
	public SnmpNotificationType( String typeDesc ,ArrayList<ObjectIdentifierValue> typeObjects) {
		super("NOTIFICATION-TYPE");
		this.typeDesc = typeDesc;
		this.typeObjectsList = typeObjects;
	}
	
	/**
	 * TYPEに設定されたDESCRIPTIONを返します。
	 *
	 * @return DESCRIPTION文字列
	 */
	public String getDescription() {
		return typeDesc;
	}

	/**
	 * TYPEに設定されたOBJECTSを返します。
	 *
	 * @return TYPE内のOBJECTS（ObjectIdentifierValueのArrayList）
	 */
	public ArrayList<ObjectIdentifierValue> getObjects() {
		return typeObjectsList;
	}
	
}
