/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.value;

import java.math.BigInteger;

import com.clustercontrol.utility.mib.MibValue;

/**
 * MIBのパース処理(Trap関連情報取得)における OID値クラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class ObjectIdentifierValue extends MibValue{
	
	/**
	 * OIDの名称.
	 */
	private String oidName;
	
	/**
	 * 親となるOID
	 */
	private ObjectIdentifierValue oidParent;

	/**
	 * OIDの値.
	 */
	private BigInteger oidValue;
	
	/**
	 * OID値のインスタンスを作成します
	 *
	 * @param oidParent 親となるOID(rootのOIDのみnullを指定)
	 * @param oidName	OIDの名称
	 * @param oidValue	OIDの値
	 */
	public ObjectIdentifierValue(ObjectIdentifierValue oidParent,  String oidName ,  BigInteger oidValue  ) {
		super("Object Identifier");
		this.oidParent = oidParent;
		this.oidName = oidName;
		this.oidValue = oidValue;
	}

	/**
	 * OIDの名称を返します
	 * 
	 * @return 割付した値の名称
	*/
	public String getName() {
		return oidName;
	}

	/**
	 * OIDの値を返します
	 * @return OID
	 */
	public int getValue() {
		return oidValue.intValue();
	}

	/**
	 * OIDの値(親のOID含むツリー)を返します
	 * ex
	 *	 1.3.6.1.4.1 ...
	 * @return OIDの名称
	 */
	public String toString() {
		StringBuffer oidBuf = new StringBuffer();
		if( oidParent == null){
			oidBuf.append("");
		}else{
			oidBuf.append(oidParent.toString());
			oidBuf.append(".");
		}
		oidBuf.append(oidValue.toString());
		return oidBuf.toString();
	}

	/**
	 * OIDの名称と値(親のOID含むツリー)を返します
	 * ex
	 *	 iso(1).org(3).dod(6).internet(1).private(4).enterprises(1)....
	 * @return OIDの名称(親のOID含む)
	 */
	public String toDetailString() {
		StringBuffer detailBuf = new StringBuffer();
		if( oidParent == null){
			detailBuf.append("");
		}else{
			detailBuf.append(oidParent.toDetailString());
			detailBuf.append(".");
		}
		if(oidName == null  || oidName.equals("")  ){
			detailBuf.append(oidValue.toString() );
		}else{
			detailBuf.append(oidName);
			detailBuf.append("(" +oidValue.toString() + ")");
		}
		return detailBuf.toString();
	}

}
