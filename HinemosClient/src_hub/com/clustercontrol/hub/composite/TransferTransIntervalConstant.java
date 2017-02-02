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

/**
 * 収集蓄積[転送]機能の転送間隔に関する定数クラス
 *
 */
public class TransferTransIntervalConstant {

	/** リアルタイム */
	public static final int TYPE_REALTIME = 0;

	/** 1時間 */
	public static final int TYPE_HOUR_1 = 1;

	/** 3時間 */
	public static final int TYPE_HOUR_3 = 3;

	/** 6時間 */
	public static final int TYPE_HOUR_6 = 6;

	/** 12時間 */
	public static final int TYPE_HOUR_12 = 12;

	/** 24時間 */
	public static final int TYPE_HOUR_24 = 24;
	
	/** 10日保存後に転送 */
	public static final int TYPE_DAYS_10 = 10;
	
	/** 20日保存後に転送 */
	public static final int TYPE_DAYS_20 = 20;
	
	/** 30日保存後に転送 */
	public static final int TYPE_DAYS_30 = 30;

	/** 60日保存後に転送 */
	public static final int TYPE_DAYS_60 = 60;

	/** 90日保存後に転送 */
	public static final int TYPE_DAYS_90 = 90;
	
	/** リアルタイムで転送 */
	public static final String STRING_REALTIME = "hub.log.transfer.trans.interval.realtime";
	
	public static final String STRING_HOUR = "hub.log.transfer.trans.interval.hour.";
	
	/** 1時間 */
	public static final String STRING_HOUR_1 = STRING_HOUR + TYPE_HOUR_1;

	/** 3時間 */
	public static final String STRING_HOUR_3 = STRING_HOUR + TYPE_HOUR_3;

	/** 6時間 */
	public static final String STRING_HOUR_6 = STRING_HOUR + TYPE_HOUR_6;

	/** 12時間 */
	public static final String STRING_HOUR_12 = STRING_HOUR + TYPE_HOUR_12;

	/** 24時間 */
	public static final String STRING_HOUR_24 = STRING_HOUR + TYPE_HOUR_24;

	/**  */
	public static final String STRING_DAYS = "hub.log.transfer.trans.interval.days.";
	
	/** 10日保存後に転送 */
	public static final String STRING_DAYS_10 = STRING_DAYS + TYPE_DAYS_10;
	
	/** 20日保存後に転送 */
	public static final String STRING_DAYS_20 = STRING_DAYS + TYPE_DAYS_20;
	
	/** 30日保存後に転送 */
	public static final String STRING_DAYS_30 = STRING_DAYS + TYPE_DAYS_30;
	
	/** 60日保存後に転送 */
	public static final String STRING_DAYS_60 = STRING_DAYS + TYPE_DAYS_60;
	
	/** 90日保存後に転送 */
	public static final String STRING_DAYS_90 = STRING_DAYS + TYPE_DAYS_90;
	
	public static List<Integer> getRegularList() {
		return Arrays.asList(TYPE_HOUR_1,TYPE_HOUR_3,TYPE_HOUR_6,TYPE_HOUR_12,TYPE_HOUR_24);
	}
	
	public static List<Integer> getLagList() {
		return Arrays.asList(TYPE_DAYS_10, TYPE_DAYS_20,TYPE_DAYS_30,TYPE_DAYS_60,TYPE_DAYS_90);
	}
	
	/**
	 * typeからStringへ変換する
	 * @param num
	 * @return
	 */
	public static String typeToString(int num){
		String str = null;
		switch(num){
			case TYPE_REALTIME:
				str = STRING_REALTIME;
				break;
			case TYPE_HOUR_1:
				str = STRING_HOUR_1;
				break;
			case TYPE_HOUR_3:
				str = STRING_HOUR_3;
				break;
			case TYPE_HOUR_6:
				str = STRING_HOUR_6;
				break;
			case TYPE_HOUR_12:
				str = STRING_HOUR_12;
				break;
			case TYPE_HOUR_24:
				str = STRING_HOUR_24;
				break;
			case TYPE_DAYS_10:
				str = STRING_DAYS_10;
				break;
			case TYPE_DAYS_20:
				str = STRING_DAYS_20;
				break;
			case TYPE_DAYS_30:
				str = STRING_DAYS_30;
				break;
			case TYPE_DAYS_60:
				str = STRING_DAYS_60;
				break;
			case TYPE_DAYS_90:
				str = STRING_DAYS_90;
				break;
			default:
				str = "";
		}
		return str;
	}
	
	/**
	 * Stringからtypeへ変換する
	 * @param str
	 * @return
	 */
	public static int stringToType(String str){
		int num = 0;
		
		if (str == null) 
			str = "";
		
		switch(str){
			case STRING_REALTIME:
				num = TYPE_REALTIME;
				break;
			case STRING_HOUR_1:
				num = TYPE_HOUR_1;
				break;
			case STRING_HOUR_3:
				num = TYPE_HOUR_3;
				break;
			case STRING_HOUR_6:
				num = TYPE_HOUR_6;
				break;
			case STRING_HOUR_12:
				num = TYPE_HOUR_12;
				break;
			case STRING_HOUR_24:
				num = TYPE_HOUR_24;
				break;
			case STRING_DAYS_10:
				num = TYPE_DAYS_10;
				break;
			case STRING_DAYS_20:
				num = TYPE_DAYS_20;
				break;
			case STRING_DAYS_30:
				num = TYPE_DAYS_30;
				break;
			case STRING_DAYS_60:
				num = TYPE_DAYS_60;
				break;
			case STRING_DAYS_90:
				num = TYPE_DAYS_90;
				break;
			default:
				num = 0;
		}
		return num;
	}
}
