/*

 Copyright (C) 2011 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent.log;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.clustercontrol.agent.util.AgentProperties;

/**
 * syslogへのログ転送クラス<BR>
 * 
 * ログはlog4jを使ってsyslogに転送されます。<BR>
 * 
 */
public class LoggerSyslog {

	/** 外部のsyslog出力用ロガー（Log4J） */
	static Logger e_log = Logger.getLogger("hinemos.syslog.transfer");
	/** LoggerSyslogクラス用ロガー（Log4J） */
	static Logger m_log = Logger.getLogger( LoggerSyslog.class );

	/** メッセージの重要度 */
	private int m_priority = Priority.INFO_INT;

	/** syslog転送の有効有無(v3.2との互換性保持) */
	private boolean valid = false;

	/**
	 * コンストラクタ
	 */
	public LoggerSyslog() {

		// メッセージの重要度
		String priorityText = AgentProperties.getProperty("monitor.logfile.syslog.priority");
		m_log.info("monitor.logfile.syslog.priority = " + priorityText);

		if (priorityText == null || "".equals(priorityText)) {
			valid = false;
		} else {
			valid = true;
			m_priority =  Level.toLevel(priorityText, Level.INFO).toInt();
		}
	}

	/**
	 * ログを転送します。<BR>
	 * 
	 * ログはlog4jを使ってsyslogに転送されます。<BR>
	 * log4jのオブジェクトにログを送信します。
	 * 
	 * 
	 * @param message the message object to log.
	 */
	public void log(Object message) {
		m_log.trace("syslog-message(" + m_priority + ") : " + message);
		switch (m_priority) {
		case Priority.DEBUG_INT:
			e_log.debug(message);
			break;
		case Priority.INFO_INT:
			e_log.info(message);
			break;
		case Priority.WARN_INT:
			e_log.warn(message);
			break;
		case Priority.ERROR_INT:
			e_log.error(message);
			break;
		case Priority.FATAL_INT:
			e_log.fatal(message);
			break;

		default:
			break;
		}
	}

	public boolean isValid() {
		return valid;
	}

	public static void main(String[] args) {

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(args[0]));

			LoggerSyslog sys = new LoggerSyslog();
			sys.log("test");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}