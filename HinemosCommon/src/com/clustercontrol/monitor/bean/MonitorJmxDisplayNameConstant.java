/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.util.ArrayList;

/**
 * JMX監視のdisplayNameに関する定数を格納するクラス<BR>
 * 
 * @version 6.2.2
 * @since 1.0.0
 */
public class MonitorJmxDisplayNameConstant {

	/** DISPLAY_NAME_DBMS */
	public static final String DISPLAY_NAME_DBMS = "DBMS";
	/** DISPLAY_NAME_DBMS-JOB */
	public static final String DISPLAY_NAME_DBMS_JOB = "DBMS-JOB";
	/** DISPLAY_NAME_DBMS-DEL */
	public static final String DISPLAY_NAME_DBMS_DEL = "DBMS-DEL";
	/** DISPLAY_NAME_DBMS-ETC */
	public static final String DISPLAY_NAME_DBMS_ETC = "DBMS-ETC";
	/** DISPLAY_NAME_RAM */
	public static final String DISPLAY_NAME_RAM = "RAM";
	/** DISPLAY_NAME_RAM-MONITOR */
	public static final String DISPLAY_NAME_RAM_MONITOR = "RAM-MON";
	/** DISPLAY_NAME_RAM-JOB */
	public static final String DISPLAY_NAME_RAM_JOB = "RAM-JOB";
	/** DBMSに紐づくDISPLAY_NAMEリスト */
	public static ArrayList<String> DISPLAY_NAME_DBMS_LIST = new ArrayList<String>();
	/** RAMに紐づくDISPLAY_NAMEリスト */
	public static ArrayList<String> DISPLAY_NAME_RAM_LIST = new ArrayList<String>();

	static {
		// DBMS
		DISPLAY_NAME_DBMS_LIST.add(DISPLAY_NAME_DBMS_JOB);
		DISPLAY_NAME_DBMS_LIST.add(DISPLAY_NAME_DBMS_DEL);
		DISPLAY_NAME_DBMS_LIST.add(DISPLAY_NAME_DBMS_ETC);
		// RAM
		DISPLAY_NAME_RAM_LIST.add(DISPLAY_NAME_RAM_MONITOR);
		DISPLAY_NAME_RAM_LIST.add(DISPLAY_NAME_RAM_JOB);
	}

	/**
	 * displayNameがJMX監視に紐づくものか判定します。
	 * 
	 * @param value
	 * @return true:紐づく, false:紐づかない
	 */
	public static boolean isJmxDisplayName(String value) {

		if (isDbmsInclude(value)
				|| isRamInclude(value)) {
			return true;
		}
		return false;
	}

	/**
	 * displayNameがDBMSに紐づくものか判定します。
	 * 
	 * @param value displayName
	 * @return true:dbms, false:dbms以外
	 */
	public static boolean isDbmsInclude(String value) {

		if (DISPLAY_NAME_DBMS_LIST.contains(value)) {
			return true;
		}
		return false;
	}

	/**
	 * displayNameがRAMに紐づくものか判定します。
	 * 
	 * @param value displayName
	 * @return true:ram, false:ram以外
	 */
	public static boolean isRamInclude(String value) {

		if (DISPLAY_NAME_RAM_LIST.contains(value)) {
			return true;
		}
		return false;
	}
	
	/**
	 * JMX監視のkey値からdisplayNameを求めます。
	 *  
	 * @return displayName
	 */
	public static String getJmxDisplayName(String jmxKey) {

		String displayName = "";
		if (jmxKey == null) {
			return displayName;
		}

		if (MonitorJmxKeyConstant.SCHEDULER_TYPE_DBMS_JOB.equals(jmxKey)) {
			displayName = MonitorJmxDisplayNameConstant.DISPLAY_NAME_DBMS_JOB;
		} else if (MonitorJmxKeyConstant.SCHEDULER_TYPE_DBMS_DEL.equals(jmxKey)) {
			displayName = MonitorJmxDisplayNameConstant.DISPLAY_NAME_DBMS_DEL;
		} else if (MonitorJmxKeyConstant.SCHEDULER_TYPE_DBMS_ETC.equals(jmxKey)) {
			displayName = MonitorJmxDisplayNameConstant.DISPLAY_NAME_DBMS_ETC;
		} else if (MonitorJmxKeyConstant.SCHEDULER_TYPE_RAM_MONITOR.equals(jmxKey)) {
			displayName = MonitorJmxDisplayNameConstant.DISPLAY_NAME_RAM_MONITOR;
		} else if (MonitorJmxKeyConstant.SCHEDULER_TYPE_RAM_JOB.equals(jmxKey)) {
			displayName = MonitorJmxDisplayNameConstant.DISPLAY_NAME_RAM_JOB;
		}

		return displayName;
	}
}