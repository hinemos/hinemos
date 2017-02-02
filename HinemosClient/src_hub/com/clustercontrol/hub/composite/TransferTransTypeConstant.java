/*

 Copyright (C) 2016 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.composite;

import com.clustercontrol.ws.hub.TransferType;

/**
 * 収集蓄積[転送]機能の転送種別に関する定数クラス
 *
 */
public class TransferTransTypeConstant {
	/** リアルタイムで転送 */
	public static final String STRING_REALTIME = "hub.log.transfer.trans.type.realtime";
	
	/** 一定間隔で転送 */
	public static final String STRING_REGULAR = "hub.log.transfer.trans.type.regular";
	
	/** 保存期間を経て転送 */
	public static final String STRING_LAG = "hub.log.transfer.trans.type.lag";
	
	/**
	 * typeからStringへ変換する
	 * @param num
	 * @return
	 */
	public static String typeToString(TransferType type){
		switch(type){
		case BATCH:
			return STRING_REGULAR;
		case DELAY:
			return STRING_LAG;
		case REALTIME:
			return STRING_REALTIME;
		default:
			return null;
		}
	}
	
	/**
	 * Stringからtypeへ変換する
	 * @param str
	 * @return
	 */
	public static TransferType stringToType(String str){
		if (str == null) 
			str = "";
		
		switch(str){
		case STRING_REALTIME:
			return TransferType.REALTIME;
		case STRING_REGULAR:
			return TransferType.BATCH;
		case STRING_LAG:
			return TransferType.DELAY;
		default:
			return null;
		}
	}
}
