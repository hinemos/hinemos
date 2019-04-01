/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * Long型から日付形式の文字列に変換するクラス
 *
 */
public class DateTimeStringConverter {
	/**
	 * Long型の時間をString型に変換する。
	 */
	public static String formatLongDate(Long dateValue){
		String dateString = "";
		if (dateValue == null) {
			return dateString;
		}
		
		SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
		
		try { 
			dateString = sdf.format(new Date(dateValue));
		} catch (Exception e) {
			//ignore
		}
		return dateString;
	}
	
}
