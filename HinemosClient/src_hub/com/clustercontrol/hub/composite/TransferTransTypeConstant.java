/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.composite;

import org.openapitools.client.model.TransferInfoResponse.TransTypeEnum;

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
	public static String typeToString(TransTypeEnum type){
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
	public static TransTypeEnum stringToType(String str){
		if (str == null) 
			str = "";
		
		switch(str){
		case STRING_REALTIME:
			return TransTypeEnum.REALTIME;
		case STRING_REGULAR:
			return TransTypeEnum.BATCH;
		case STRING_LAG:
			return TransTypeEnum.DELAY;
		default:
			return null;
		}
	}
}
