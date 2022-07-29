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

import org.openapitools.client.model.TransferInfoResponse.IntervalEnum;

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
	
	public static List<IntervalEnum> getRegularList() {
		return Arrays.asList(
				IntervalEnum.BATCH_HOUR_1,IntervalEnum.BATCH_HOUR_3,
				IntervalEnum.BATCH_HOUR_6,IntervalEnum.BATCH_HOUR_12,
				IntervalEnum.BATCH_HOUR_24);
	}
	
	public static List<IntervalEnum> getLagList() {
		return Arrays.asList(
				IntervalEnum.DELAY_DAY_10, IntervalEnum.DELAY_DAY_20,
				IntervalEnum.DELAY_DAY_30,IntervalEnum.DELAY_DAY_60,
				IntervalEnum.DELAY_DAY_90);
	}
	
	/**
	 * typeからStringへ変換する
	 * @param num
	 * @return
	 */
	public static String typeToString(IntervalEnum type){
		String str = null;
		if (type == null) {
			return STRING_REALTIME;
		}
		
		switch (type) {
			case BATCH_HOUR_1:
				str = STRING_HOUR_1;
				break;
			case BATCH_HOUR_3:
				str = STRING_HOUR_3;
				break;
			case BATCH_HOUR_6:
				str = STRING_HOUR_6;
				break;
			case BATCH_HOUR_12:
				str = STRING_HOUR_12;
				break;
			case BATCH_HOUR_24:
				str = STRING_HOUR_24;
				break;
			case DELAY_DAY_10:
				str = STRING_DAYS_10;
				break;
			case DELAY_DAY_20:
				str = STRING_DAYS_20;
				break;
			case DELAY_DAY_30:
				str = STRING_DAYS_30;
				break;
			case DELAY_DAY_60:
				str = STRING_DAYS_60;
				break;
			case DELAY_DAY_90:
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
	public static IntervalEnum stringToType(String str){
		IntervalEnum ret = null;
		
		if (str == null) 
			str = "";
		
		switch(str){
			case STRING_HOUR_1:
				ret = IntervalEnum.BATCH_HOUR_1;
				break;
			case STRING_HOUR_3:
				ret = IntervalEnum.BATCH_HOUR_3;
				break;
			case STRING_HOUR_6:
				ret = IntervalEnum.BATCH_HOUR_6;
				break;
			case STRING_HOUR_12:
				ret = IntervalEnum.BATCH_HOUR_12;
				break;
			case STRING_HOUR_24:
				ret = IntervalEnum.BATCH_HOUR_24;
				break;
			case STRING_DAYS_10:
				ret = IntervalEnum.DELAY_DAY_10;
				break;
			case STRING_DAYS_20:
				ret = IntervalEnum.DELAY_DAY_20;
				break;
			case STRING_DAYS_30:
				ret = IntervalEnum.DELAY_DAY_30;
				break;
			case STRING_DAYS_60:
				ret = IntervalEnum.DELAY_DAY_60;
				break;
			case STRING_DAYS_90:
				ret = IntervalEnum.DELAY_DAY_90;
				break;
			default:
				break;
		}
		return ret;
	}
}
