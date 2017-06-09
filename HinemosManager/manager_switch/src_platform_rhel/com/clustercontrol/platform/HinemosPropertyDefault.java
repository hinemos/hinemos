/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform;

/**
 * 環境差分のあるHinemosPropertyのデフォルト値を格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class HinemosPropertyDefault {
	
	public static enum StringKey {
		HOME_DIR,
		DATA_DIR,
		USER_HOME_DIR,
		INFRA_EXPORT_DIR,
		INFRA_TRANSFER_DIR,
		INTERNAL_COMMAND_COMMANDLINE,
		INTERNAL_COMMAND_USER,
		MONITOR_PING_FPING_PATH,
		MONITOR_PING_FPING6_PATH,
		PERFORMANCE_EXPORT_DIR,
		SELFCHECK_MONITORING_DB_VALIDATIONQUERY,
		WS_HTTPS_KEYSTORE_PATH
	}
	
	public static enum BooleanKey {
		WINDOWS_EVENTLOG
	}
	
	private static final String HOME_DIR;
	private static final String DATA_DIR;		// Windows only.
	private static final String USER_HOME_DIR;	// Windows only.
	
	static {
		HOME_DIR = System.getProperty("hinemos.manager.home.dir");
		DATA_DIR = null;
		USER_HOME_DIR = null;
	}
	
	/** infra.export.dir */
	private static final String INFRA_EXPORT_DIR = HOME_DIR + "/var/export/";
	
	/** infra.transfer.dir */
	private static final String INFRA_TRANSFER_DIR = HOME_DIR + "/var/infra/";
	
	/** internal.command.commandline */
	private static final String INTERNAL_COMMAND_COMMANDLINE = "echo #[GENERATION_DATE] #[MESSAGE] >> /tmp/test.txt";
	
	/** internal.command.user */
	private static final String INTERNAL_COMMAND_USER = "root";
	
	/** monitor.ping.fping.path */
	private static final String MONITOR_PING_FPING_PATH = HOME_DIR + "/sbin/fping";
	
	/** monitor.ping.fping6.path */
	private static final String MONITOR_PING_FPING6_PATH = HOME_DIR + "/sbin/fping6";
	
	/** performance.export.dir */
	private static final String PERFORMANCE_EXPORT_DIR = HOME_DIR + "/var/export/";
	
	/** selfcheck.monitoring.db.validationquery */
	private static final String SELFCHECK_MONITORING_DB_VALIDATIONQUERY = "SELECT 1 FOR UPDATE";
	
	/** windows.eventlog (Windows only.) */
	private static final boolean WINDOWS_EVENTLOG = false;
	
	/** ws.https.keystore.path */
	private static final String WS_HTTPS_KEYSTORE_PATH = "/root/keystore";
	
	public static String getString(StringKey key) {
		String value = null;
		switch (key) {
			case HOME_DIR:
				value = HOME_DIR;
				break;
			case DATA_DIR:
				value = DATA_DIR;
				break;
			case USER_HOME_DIR:
				value = USER_HOME_DIR;
				break;
			case INFRA_EXPORT_DIR:
				value = INFRA_EXPORT_DIR;
				break;
			case INFRA_TRANSFER_DIR:
				value = INFRA_TRANSFER_DIR;
				break;
			case INTERNAL_COMMAND_COMMANDLINE:
				value = INTERNAL_COMMAND_COMMANDLINE;
				break;
			case INTERNAL_COMMAND_USER:
				value = INTERNAL_COMMAND_USER;
				break;
			case MONITOR_PING_FPING_PATH:
				value = MONITOR_PING_FPING_PATH;
				break;
			case MONITOR_PING_FPING6_PATH:
				value = MONITOR_PING_FPING6_PATH;
				break;
			case PERFORMANCE_EXPORT_DIR:
				value = PERFORMANCE_EXPORT_DIR;
				break;
			case SELFCHECK_MONITORING_DB_VALIDATIONQUERY:
				value = SELFCHECK_MONITORING_DB_VALIDATIONQUERY;
				break;
			case WS_HTTPS_KEYSTORE_PATH:
				value = WS_HTTPS_KEYSTORE_PATH;
				break;
			default:
				break;
		}
		return value;
	}
	
	public static boolean getBoolean(BooleanKey key) {
		boolean value = false;
		switch (key) {
			case WINDOWS_EVENTLOG:
				value = WINDOWS_EVENTLOG;
				break;
			default:
				break;
		}
		return value;
	}
}