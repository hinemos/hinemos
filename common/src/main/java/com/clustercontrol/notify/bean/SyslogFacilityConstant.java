/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

	// 出所 （文字列）
	public enum SyslogFacility{
		// kern（種別）
		KERN(SyslogAppender.LOG_KERN),
		// user（種別）
		USER(SyslogAppender.LOG_USER),
		// mail（種別）
		MAIL(SyslogAppender.LOG_MAIL),
		// daemon（種別）
		DAEMON(SyslogAppender.LOG_DAEMON),
		// auth（種別）
		AUTH(SyslogAppender.LOG_AUTH),
		// syslog（種別）
		SYSLOG(SyslogAppender.LOG_SYSLOG),
		// lpr（種別）
		LPR(SyslogAppender.LOG_LPR),
		// news（種別）
		NEWS(SyslogAppender.LOG_NEWS),
		// uucp（種別）
		UUCP(SyslogAppender.LOG_UUCP),
		// cron（種別）
		CRON(SyslogAppender.LOG_CRON),
		// authpriv（種別）
		AUTHPRIV(SyslogAppender.LOG_AUTHPRIV),
		// ftp（種別）
		FTP(SyslogAppender.LOG_FTP),
		// local0（種別）
		LOCAL0(SyslogAppender.LOG_LOCAL0),
		// local1（種別）
		LOCAL1(SyslogAppender.LOG_LOCAL1),
		// local2（種別）
		LOCAL2(SyslogAppender.LOG_LOCAL2),
		// local3（種別）
		LOCAL3(SyslogAppender.LOG_LOCAL3),
		// local4（種別）
		LOCAL4(SyslogAppender.LOG_LOCAL4),
		// local5（種別）
		LOCAL5(SyslogAppender.LOG_LOCAL5),
		// local6（種別）
		LOCAL6(SyslogAppender.LOG_LOCAL6),
		// local7（種別）
		LOCAL7(SyslogAppender.LOG_LOCAL7);

		private int id;
		private SyslogFacility(int id) {
			this.id = id;
		}

		public int toInt() {
			return this.id;
		}

		public String toString() {
			return this.name().toLowerCase();
		}

		public static SyslogFacility fromInt(int facility) {
			for(SyslogFacility syslogFacility: SyslogFacility.values()) {
				if(syslogFacility.toInt() == facility) {
					return syslogFacility;
				}
			}
			throw new IllegalArgumentException("Unknown facility: " + facility);
		}
		public static SyslogFacility fromString(String facility) {
			for(SyslogFacility syslogFacility: SyslogFacility.values()) {
				if(syslogFacility.toString().equals(facility)) {
					return syslogFacility;
				}
			}
			throw new IllegalArgumentException("Unknown facility: " + facility);
		}
	}

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_KERN = SyslogFacility.KERN.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_USER = SyslogFacility.USER.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_MAIL = SyslogFacility.MAIL.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_DAEMON = SyslogFacility.DAEMON.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_AUTH = SyslogFacility.AUTH.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_SYSLOG = SyslogFacility.SYSLOG.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LPR = SyslogFacility.LPR.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_NEWS = SyslogFacility.NEWS.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_UUCP = SyslogFacility.UUCP.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_CRON = SyslogFacility.CRON.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_AUTHPRIV = SyslogFacility.AUTHPRIV.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_FTP = SyslogFacility.FTP.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LOCAL0 = SyslogFacility.LOCAL0.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LOCAL1 = SyslogFacility.LOCAL1.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LOCAL2 = SyslogFacility.LOCAL2.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LOCAL3 = SyslogFacility.LOCAL3.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LOCAL4 = SyslogFacility.LOCAL4.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LOCAL5 = SyslogFacility.LOCAL5.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LOCAL6 = SyslogFacility.LOCAL6.toInt();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final int TYPE_LOCAL7 = SyslogFacility.LOCAL7.toInt();


	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_KERN = SyslogFacility.KERN.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_USER = SyslogFacility.USER.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_MAIL = SyslogFacility.MAIL.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_DAEMON = SyslogFacility.DAEMON.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_AUTH = SyslogFacility.AUTH.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_SYSLOG = SyslogFacility.SYSLOG.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LPR = SyslogFacility.LPR.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_NEWS = SyslogFacility.NEWS.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_UUCP = SyslogFacility.UUCP.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_CRON = SyslogFacility.CRON.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_AUTHPRIV = SyslogFacility.AUTHPRIV.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_FTP = SyslogFacility.FTP.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LOCAL0 = SyslogFacility.LOCAL0.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LOCAL1 = SyslogFacility.LOCAL1.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LOCAL2 = SyslogFacility.LOCAL2.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LOCAL3 = SyslogFacility.LOCAL3.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LOCAL4 = SyslogFacility.LOCAL4.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LOCAL5 = SyslogFacility.LOCAL5.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LOCAL6 = SyslogFacility.LOCAL6.toString();

	/**
	 * @deprecated (Use enum instead)
	 */
	@Deprecated public static final String STRING_LOCAL7 = SyslogFacility.LOCAL7.toString();

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		try {
			return SyslogFacility.fromInt(type).toString();
		} catch(IllegalArgumentException e) {
			return "";
		}
	}

	/**
	 * 文字列から種別に変換します。
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		try {
			return SyslogFacility.fromString(string).toInt();
		} catch(IllegalArgumentException e) {
			return -1;
		}
	}

	private SyslogFacilityConstant() {
		throw new IllegalStateException("ConstClass");
	}
}