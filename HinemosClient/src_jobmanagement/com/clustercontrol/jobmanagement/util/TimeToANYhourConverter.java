/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

public class TimeToANYhourConverter {
	// ログ
	private static Log m_log = LogFactory.getLog( TimeToANYhourConverter.class );

    /**
     * 開始時刻と終了時刻から処理時間を返す
     * 引数上のミリ秒以下は切り捨て。
     * 四捨五入だと、ナノ秒レベルでの考えをしなきゃいけない。
     *  + TimeUnit.MILLISECONDS.toSeconds(49999L)
     */
    public static String toDiffTime(Long startMillis, Long endMillis) {
    	if (startMillis == null || endMillis == null) {
            return "";
    	}

    	if (startMillis > endMillis) {
            return "";
    	}
    	long sessionFromTime = TimeUnit.MILLISECONDS.toSeconds(endMillis);
    	long sessionTOTime = TimeUnit.MILLISECONDS.toSeconds(startMillis);
    	long sessionTime = sessionFromTime - sessionTOTime;
    	long oneSeconds = 1L;
    	long oneMinutes = 60L;
    	long oneHours = 60L;

    	long basicSeconds = sessionTime/oneSeconds;
    	long sessionMinutes = basicSeconds / oneMinutes;
    	long sessionSeconds = basicSeconds%oneMinutes;
    	long sessionHours = sessionMinutes / oneHours;
    	long sessionMinutesRest = sessionMinutes%oneHours;
    	String diffTime = String.format("%1$02d", sessionHours) + ":" + String.format("%1$02d", sessionMinutesRest) + ":" + String.format("%1$02d", sessionSeconds);

        return diffTime;
    }

    public static boolean main() {
    	String a = "2009/04/19 12:23:01";
    	String b = "2009/04/22 12:23:00";
    	String c = "2009/05/20 12:23:00";
    	String d = "2010/04/20 12:23:00";
    	String e = "2009/04/19 12:24:00";
    	String f = "2009/04/19 13:23:00";
    	String g = "2009/04/19 12:24:01";
    	String h = "2009/04/19 13:23:01";
    	String i = "2009/04/19 12:23:00";
    	SimpleDateFormat DFYS = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	DFYS.setTimeZone(TimezoneUtil.getTimeZone());
    	Date ta = null;
    	Date tb = null;
    	Date tc = null;
    	Date td = null;
    	Date te = null;
    	Date tf = null;
    	Date tg = null;
    	Date th = null;
    	Date ti = null;
    	try{
    	    ta = DFYS.parse(a);
    	    tb = DFYS.parse(b);
    	    tc = DFYS.parse(c);
    	    td = DFYS.parse(d);
    	    te = DFYS.parse(e);
    	    tf = DFYS.parse(f);
    	    tg = DFYS.parse(g);
    	    th = DFYS.parse(h);
    	    ti = DFYS.parse(i);
    	}catch(java.text.ParseException z){
    		m_log.debug(z.getMessage(), z);
			throw new InternalError(z.getMessage());
    	}
    	long s = ta.getTime();
    	long e1 = tb.getTime();
    	long e2 = tc.getTime();
    	long e3 = td.getTime();
    	long e4 = te.getTime();
    	long e5 = tf.getTime();
    	long e6 = tg.getTime();
    	long e7 = th.getTime();
    	long e8 = ti.getTime();
    	m_log.info("from date = : " + a);
    	m_log.info("test case 1 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + b);
    	m_log.info("diffTime   = : " + toDiffTime(s, e1));
    	m_log.info("test case 2 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + c);
    	m_log.info("diffTime   = : " + toDiffTime(s, e2));
    	m_log.info("test case 3 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + d);
    	m_log.info("diffTime   = : " + toDiffTime(s, e3));
    	m_log.info("test case 4 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + e);
    	m_log.info("diffTime   = : " + toDiffTime(s, e4));
    	m_log.info("test case 5 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + f);
    	m_log.info("diffTime   = : " + toDiffTime(s, e5));
    	m_log.info("test case 6 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + g);
    	m_log.info("diffTime   = : " + toDiffTime(s, e6));
    	m_log.info("test case 7 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + h);
    	m_log.info("diffTime   = : " + toDiffTime(s, e7));
    	m_log.info("test case 8 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + a);
    	m_log.info("diffTime   = : " + toDiffTime(s, s));
    	m_log.info("test case 9 :>>>>>>>>>>>>");
    	m_log.info("to   date = : " + i);
    	m_log.info("diffTime   = : " + toDiffTime(s, e8));
    	return true;
    }
}
