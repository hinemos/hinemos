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
 * MIBのパース処理(Trap関連情報取得)における TRAP-TYPEを表すクラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class SnmpTrapType extends MibType{
	
	/**
	 * TRAP-TYPE に設定されたDESCRIPTION
	 */
	private String typeDesc;

	/**
	 * TRAP-TYPE に設定されたVARIABLES
	 */
	private ArrayList<ObjectIdentifierValue> typeVariables;

	/**
	 * TRAP-TYPE に設定されたENTERPRISE
	 */
	private ObjectIdentifierValue typeEnterprise;

	/**
	 * TRAP-TYPEを表すインスタンスを作成
	 *
	 * @param typeDesc TYPE内のDESCRIPTION
	 * @param typeVariables	 TYPE内のVARIABLES（ObjectIdentifierValueのArrayList）
	 * @param typeEnterprise TYPE内のENTERPRISE(ObjectIdentifierValue)
	 */
	public SnmpTrapType( String typeDesc ,ArrayList<ObjectIdentifierValue> typeVariables , ObjectIdentifierValue typeEnterprise ) {
		super("TRAP-TYPE");
		this.typeDesc = typeDesc;
		this.typeVariables = typeVariables;
		this.typeEnterprise = typeEnterprise;
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
	 * TYPEに設定されたVARIABLESを返します。
	 *
	 * @return VARIABLESのList（ObjectIdentifierValueのArrayList）
	 */
	public ArrayList<ObjectIdentifierValue> getVariables() {
		return typeVariables;
	}
	
	/**
	 * TYPEに設定されたENTERPRISEを返します。
	 *
	 * @return TYPE内のENTERPRISE(OID)
	 */
	public ObjectIdentifierValue getEnterprise() {
		return typeEnterprise;
	}
	
}
