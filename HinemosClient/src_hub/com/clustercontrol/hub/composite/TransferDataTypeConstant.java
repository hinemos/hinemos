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

import java.util.Arrays;
import java.util.List;

import com.clustercontrol.ws.hub.DataType;

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
	public static String typeToString(DataType type){
		
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
	public static DataType stringToType(String str){
		if (str == null) 
			str = "";
		
		switch(str){
		case STRING_JOB:
			return DataType.JOB;
		case STRING_EVENT:
			return DataType.EVENT;
		case STRING_NUMERIC:
			return DataType.NUMERIC;
		case STRING_STRING:
			return DataType.STRING;
		default:
			return null;
		}
	}
}
