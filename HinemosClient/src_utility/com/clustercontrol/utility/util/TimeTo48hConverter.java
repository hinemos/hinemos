/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.TimezoneUtil;
/**
 * 
 * epochを表示形式HH:MM:SSに対応させるクラス
 * @version 6.1.0
 *
 */
public class TimeTo48hConverter {
	
	/*ロガー*/
	private static Log log = LogFactory.getLog(TimeTo48hConverter.class);
    private static long utcOffset;
    
    static {
    	utcOffset = TimezoneUtil.getTimeZoneOffset();
    }
	/**
	 * epochを表示形式HH:MM:SSに変換。
	 * なお、6.0以降の仕様に対応するため、マイナスの時刻も変換可能
	 * 
	 * @param dateValue
	 * @return -HH:MM:SS or HH:MM:SS
	 */
	public static String dateTo48hms(long epoch){

		StringBuilder dateValue = new StringBuilder(); ;
		long epoch_abs;
		epoch = epoch + utcOffset ;
		if(epoch < 0){
			epoch_abs = -epoch;
			dateValue.append("-");
		}else{
			epoch_abs = epoch;
		}
		epoch_abs = epoch_abs / 1000; //秒単位に変換
		
		// 時
		dateValue.append(String.format("%02d",( epoch_abs / ( 60 * 60 )) ));
		// 分
		dateValue.append( ":" + String.format("%02d",((epoch_abs / 60) % 60)));
		// 秒
		dateValue.append( ":" + String.format("%02d",(epoch_abs % 60)));
		return dateValue.toString();
	}
	/**
	 * 表示形式を48:00まで対応
	 * 上記以上のDateは強制的に48:00(1970/01/03 00:00:00)に変換される
	 * 
	 * @param dateValue
	 * @return "" or 00:00 - 48:00
	 */
	public static String dateTo48hm(Date dateValue){
		String strDate = "";
		//表示項目設定
		String strHour24 = "1970/01/02 00:00:00";
		String strHour48 = "1970/01/03 00:00:00";
		Date date24 = null;
		Date date48 = null;
		long hour24 = 0;
		long hour48 = 0;
		try {
			SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			date24 = sdfYmd.parse(strHour24);
			date48 = sdfYmd.parse(strHour48);
			hour24 = date24.getTime();
			hour48 = date48.getTime();
		} catch (ParseException e) {
			log.error(e);
		}

		SimpleDateFormat sdfHH = new SimpleDateFormat("HH");
		SimpleDateFormat sdfMmSs = new SimpleDateFormat("mm");
		if(dateValue != null){
			/*
			1970/01/03 00:00:00を超える場合は、
			強制的に1970/01/03 00:00:00に変換（DB登録時も）
			 */
			if(hour48 < dateValue.getTime()){
				dateValue.setTime(hour48);
			}
			if(hour48 == dateValue.getTime()){
				String strHH = sdfHH.format(dateValue);
				Integer hh = Integer.parseInt(strHH);
				hh = hh + 48;
				strDate = String.valueOf(hh) + ":" + sdfMmSs.format(dateValue);
			}else if(hour24 <= dateValue.getTime()){
				String strHH = sdfHH.format(dateValue);
				Integer hh = Integer.parseInt(strHH);
				hh = hh + 24;
				strDate = String.valueOf(hh) + ":" + sdfMmSs.format(dateValue);
			}else {
				//開始時間
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				strDate = sdf.format(dateValue);
			}
		}
		return strDate;
	}
}
