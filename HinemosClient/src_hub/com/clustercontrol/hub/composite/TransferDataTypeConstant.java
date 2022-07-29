/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.composite;

import java.util.Arrays;
import java.util.List;

import org.openapitools.client.model.TransferInfoResponse.DataTypeEnum;

/**
 * 収集蓄積[転送]機能の転送データ種別に関する定数クラス
 *
 */
public class TransferDataTypeConstant {
	/** ジョブ履歴データ */
	public static final int TYPE_JOB = 0;
	
	/** イベント通知履歴データ */
	public static final int TYPE_EVENT = 1;
	
	/** 収集 数値データ */
	public static final int TYPE_NUMERIC = 2;
	
	/** 収集 文字列データ */
	public static final int TYPE_STRING = 3;
	
	/** ジョブ履歴データ */
	public static final String STRING_JOB = "hub.log.transfer.data.type.job";
	
	/** イベント通知履歴データ */
	public static final String STRING_EVENT = "hub.log.transfer.data.type.event";
	
	/** 収集 数値データ */
	public static final String STRING_NUMERIC = "hub.log.transfer.data.type.numeric";
	
	/** 収集 文字列データ */
	public static final String STRING_STRING = "hub.log.transfer.data.type.string";
	
	public static List<Integer> getList() {
		return Arrays.asList(TYPE_JOB, TYPE_EVENT,TYPE_NUMERIC,TYPE_STRING);
	}
	
	/**
	 * typeからStringへ変換する
	 * @param num
	 * @return
	 */
	public static String typeToString(DataTypeEnum type){
		
		String str = null;
		
		switch(type){
		case EVENT:
			str = STRING_EVENT;
			break;
		case JOB:
			str = STRING_JOB;
			break;
		case NUMERIC:
			str = STRING_NUMERIC;
			break;
		case STRING:
			str = STRING_STRING;
			break;
		default:
			break;
		}
		return str;
	}
	
	/**
	 * Stringからtypeへ変換する
	 * @param str
	 * @return
	 */
	public static DataTypeEnum stringToType(String str){
		if (str == null) 
			str = "";
		
		switch(str){
		case STRING_JOB:
			return DataTypeEnum.JOB;
		case STRING_EVENT:
			return DataTypeEnum.EVENT;
		case STRING_NUMERIC:
			return DataTypeEnum.NUMERIC;
		case STRING_STRING:
			return DataTypeEnum.STRING;
		default:
			return null;
		}
	}
}
