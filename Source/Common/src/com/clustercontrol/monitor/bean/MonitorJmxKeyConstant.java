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
 * JMX監視のkeyに関する定数を格納するクラス<BR>
 * 
 * @version 6.2.2
 * @since 1.0.0
 */
public class MonitorJmxKeyConstant {

	/** スケジュール保持種別_DBMS */
	public static final String SCHEDULER_TYPE_DBMS = "`dbms`";
	/** スケジュール保持種別_DBMS-JOB */
	public static final String SCHEDULER_TYPE_DBMS_JOB = "`dbmsJob`";
	/** スケジュール保持種別_DBMS-DEL */
	public static final String SCHEDULER_TYPE_DBMS_DEL = "`dbmsDel`";
	/** スケジュール保持種別_DBMS-ETC */
	public static final String SCHEDULER_TYPE_DBMS_ETC = "`dbmsTrans`";
	/** スケジュール保持種別_RAM */
	public static final String SCHEDULER_TYPE_RAM = "`ram`";
	/** スケジュール保持種別_RAM-MONITOR */
	public static final String SCHEDULER_TYPE_RAM_MONITOR = "`ramMon`";
	/** スケジュール保持種別_RAM-JOB */
	public static final String SCHEDULER_TYPE_RAM_JOB = "`ramJob`";
	/** DBMSに紐づくスケジュール保持種別リスト */
	public static ArrayList<String> SCHEDULER_TYPE_DBMS_LIST = new ArrayList<String>();
	/** RAMに紐づくスケジュール保持種別リスト */
	public static ArrayList<String> SCHEDULER_TYPE_RAM_LIST = new ArrayList<String>();

	static {
		// DBMS
		SCHEDULER_TYPE_DBMS_LIST.add(SCHEDULER_TYPE_DBMS_JOB);
		SCHEDULER_TYPE_DBMS_LIST.add(SCHEDULER_TYPE_DBMS_DEL);
		SCHEDULER_TYPE_DBMS_LIST.add(SCHEDULER_TYPE_DBMS_ETC);
		// RAM
		SCHEDULER_TYPE_RAM_LIST.add(SCHEDULER_TYPE_RAM_MONITOR);
		SCHEDULER_TYPE_RAM_LIST.add(SCHEDULER_TYPE_RAM_JOB);
	}

	/**
	 * keyが`dbms`か判定します。
	 * 
	 * @param value スケジュール保持種別
	 * @return true:dbms, false:dbms以外
	 */
	public static boolean isDbms(String value) {

		if (SCHEDULER_TYPE_DBMS.equals(value)) {
			return true;
		}
		return false;
	}

	/**
	 * keyが`ram`か判定します。
	 * 
	 * @param value スケジュール保持種別
	 * @return true:ram, false:ram以外
	 */
	public static boolean isRam(String value) {

		if (SCHEDULER_TYPE_RAM.equals(value)) {
			return true;
		}
		return false;
	}
}