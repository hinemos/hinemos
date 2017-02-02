/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.bean;

import org.apache.log4j.net.SyslogAppender;

/**
 * syslogで用いるFacilityの定数クラス<BR>
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
public class SyslogFacilityConstant {

	/** kern（種別）。 */
	public static final int TYPE_KERN = SyslogAppender.LOG_KERN;

	/** user（種別）。 */
	public static final int TYPE_USER = SyslogAppender.LOG_USER;

	/** mail（種別）。 */
	public static final int TYPE_MAIL = SyslogAppender.LOG_MAIL;

	/** daemon（種別）。 */
	public static final int TYPE_DAEMON = SyslogAppender.LOG_DAEMON;

	/** auth（種別）。 */
	public static final int TYPE_AUTH = SyslogAppender.LOG_AUTH;

	/** syslog（種別）。 */
	public static final int TYPE_SYSLOG = SyslogAppender.LOG_SYSLOG;

	/** lpr（種別）。 */
	public static final int TYPE_LPR = SyslogAppender.LOG_LPR;

	/** news（種別）。 */
	public static final int TYPE_NEWS = SyslogAppender.LOG_NEWS;

	/** uucp（種別）。 */
	public static final int TYPE_UUCP = SyslogAppender.LOG_UUCP;

	/** cron（種別）。 */
	public static final int TYPE_CRON = SyslogAppender.LOG_CRON;

	/** authpriv（種別）。 */
	public static final int TYPE_AUTHPRIV = SyslogAppender.LOG_AUTHPRIV;

	/** ftp（種別）。 */
	public static final int TYPE_FTP = SyslogAppender.LOG_FTP;

	/** local0（種別）。 */
	public static final int TYPE_LOCAL0 = SyslogAppender.LOG_LOCAL0;

	/** local1（種別）。 */
	public static final int TYPE_LOCAL1 = SyslogAppender.LOG_LOCAL1;

	/** local2（種別）。 */
	public static final int TYPE_LOCAL2 = SyslogAppender.LOG_LOCAL2;

	/** local3（種別）。 */
	public static final int TYPE_LOCAL3 = SyslogAppender.LOG_LOCAL3;

	/** local4（種別）。 */
	public static final int TYPE_LOCAL4 = SyslogAppender.LOG_LOCAL4;

	/** local5（種別）。 */
	public static final int TYPE_LOCAL5 = SyslogAppender.LOG_LOCAL5;

	/** local6（種別）。 */
	public static final int TYPE_LOCAL6 = SyslogAppender.LOG_LOCAL6;

	/** local7（種別）。 */
	public static final int TYPE_LOCAL7 = SyslogAppender.LOG_LOCAL7;


	/** （文字列）。 */
	public static final String STRING_KERN = "kern";

	/** （文字列）。 */
	public static final String STRING_USER = "user";

	/** （文字列）。 */
	public static final String STRING_MAIL = "mail";

	/** （文字列）。 */
	public static final String STRING_DAEMON = "daemon";

	/** （文字列）。 */
	public static final String STRING_AUTH = "auth";

	/** （文字列）。 */
	public static final String STRING_SYSLOG = "syslog";

	/** （文字列）。 */
	public static final String STRING_LPR = "lpr";

	/** （文字列）。 */
	public static final String STRING_NEWS = "news";

	/** （文字列）。 */
	public static final String STRING_UUCP = "uucp";

	/** （文字列）。 */
	public static final String STRING_CRON = "cron";

	/** （文字列）。 */
	public static final String STRING_AUTHPRIV = "authpriv";

	/** （文字列）。 */
	public static final String STRING_FTP = "ftp";

	/** （文字列）。 */
	public static final String STRING_LOCAL0 = "local0";

	/** （文字列）。 */
	public static final String STRING_LOCAL1 = "local1";

	/** （文字列）。 */
	public static final String STRING_LOCAL2 = "local2";

	/** （文字列）。 */
	public static final String STRING_LOCAL3 = "local3";

	/** （文字列）。 */
	public static final String STRING_LOCAL4 = "local4";

	/** （文字列）。 */
	public static final String STRING_LOCAL5 = "local5";

	/** （文字列）。 */
	public static final String STRING_LOCAL6 = "local6";

	/** （文字列）。 */
	public static final String STRING_LOCAL7 = "local7";



	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == TYPE_KERN) {
			return STRING_KERN;
		} else if (type == TYPE_USER) {
			return STRING_USER;
		} else if (type == TYPE_MAIL) {
			return STRING_MAIL;
		} else if (type == TYPE_DAEMON) {
			return STRING_DAEMON;
		} else if (type == TYPE_AUTH) {
			return STRING_AUTH;
		} else if (type == TYPE_SYSLOG) {
			return STRING_SYSLOG;
		} else if (type == TYPE_LPR) {
			return STRING_LPR;
		} else if (type == TYPE_NEWS) {
			return STRING_NEWS;
		} else if (type == TYPE_UUCP) {
			return STRING_UUCP;
		} else if (type == TYPE_CRON) {
			return STRING_CRON;
		} else if (type == TYPE_AUTHPRIV) {
			return STRING_AUTHPRIV;
		} else if (type == TYPE_FTP) {
			return STRING_FTP;
		} else if (type == TYPE_LOCAL0) {
			return STRING_LOCAL0;
		} else if (type == TYPE_LOCAL1) {
			return STRING_LOCAL1;
		} else if (type == TYPE_LOCAL2) {
			return STRING_LOCAL2;
		} else if (type == TYPE_LOCAL3) {
			return STRING_LOCAL3;
		} else if (type == TYPE_LOCAL4) {
			return STRING_LOCAL4;
		} else if (type == TYPE_LOCAL5) {
			return STRING_LOCAL5;
		} else if (type == TYPE_LOCAL6) {
			return STRING_LOCAL6;
		} else if (type == TYPE_LOCAL7) {
			return STRING_LOCAL7;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_KERN)) {
			return TYPE_KERN;
		} else if (string.equals(STRING_USER)) {
			return TYPE_USER;
		} else if (string.equals(STRING_MAIL)) {
			return TYPE_MAIL;
		} else if (string.equals(STRING_DAEMON)) {
			return TYPE_DAEMON;
		} else if (string.equals(STRING_AUTH)) {
			return TYPE_AUTH;
		} else if (string.equals(STRING_SYSLOG)) {
			return TYPE_SYSLOG;
		} else if (string.equals(STRING_LPR)) {
			return TYPE_LPR;
		} else if (string.equals(STRING_NEWS)) {
			return TYPE_NEWS;
		} else if (string.equals(STRING_UUCP)) {
			return TYPE_UUCP;
		} else if (string.equals(STRING_CRON)) {
			return TYPE_CRON;
		} else if (string.equals(STRING_AUTHPRIV)) {
			return TYPE_AUTHPRIV;
		} else if (string.equals(STRING_FTP)) {
			return TYPE_FTP;
		} else if (string.equals(STRING_LOCAL0)) {
			return TYPE_LOCAL0;
		} else if (string.equals(STRING_LOCAL1)) {
			return TYPE_LOCAL1;
		} else if (string.equals(STRING_LOCAL2)) {
			return TYPE_LOCAL2;
		} else if (string.equals(STRING_LOCAL3)) {
			return TYPE_LOCAL3;
		} else if (string.equals(STRING_LOCAL4)) {
			return TYPE_LOCAL4;
		} else if (string.equals(STRING_LOCAL5)) {
			return TYPE_LOCAL5;
		} else if (string.equals(STRING_LOCAL6)) {
			return TYPE_LOCAL6;
		} else if (string.equals(STRING_LOCAL7)) {
			return TYPE_LOCAL7;
		}
		return -1;
	}
}