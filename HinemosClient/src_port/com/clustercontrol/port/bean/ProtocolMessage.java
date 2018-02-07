/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.bean;

import com.clustercontrol.util.Messages;

public class ProtocolMessage {

	public static final String STRING_PROTOCOL_TCP = Messages.getString("protocol.tcp");
	public static final String STRING_PROTOCOL_FTP = Messages.getString("protocol.ftp");
	public static final String STRING_PROTOCOL_SMTP = Messages.getString("protocol.smtp");
	public static final String STRING_PROTOCOL_SMTPS = Messages.getString("protocol.smtps");
	public static final String STRING_PROTOCOL_POP3 = Messages.getString("protocol.pop3");
	public static final String STRING_PROTOCOL_POP3S = Messages.getString("protocol.pop3s");
	public static final String STRING_PROTOCOL_IMAP = Messages.getString("protocol.imap");
	public static final String STRING_PROTOCOL_IMAPS = Messages.getString("protocol.imaps");
	public static final String STRING_PROTOCOL_NTP = Messages.getString("protocol.ntp");
	public static final String STRING_PROTOCOL_DNS = Messages.getString("protocol.dns");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(String type) {
		if (type.equals(ProtocolConstant.TYPE_PROTOCOL_TCP)) {
			return STRING_PROTOCOL_TCP;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_FTP)) {
			return STRING_PROTOCOL_FTP;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_SMTP)) {
			return STRING_PROTOCOL_SMTP;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_SMTPS)) {
			return STRING_PROTOCOL_SMTPS;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_POP3)) {
			return STRING_PROTOCOL_POP3;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_POP3S)) {
			return STRING_PROTOCOL_POP3S;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_IMAP)) {
			return STRING_PROTOCOL_IMAP;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_IMAPS)) {
			return STRING_PROTOCOL_IMAPS;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_NTP)) {
			return STRING_PROTOCOL_NTP;
		} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_DNS)) {
			return STRING_PROTOCOL_DNS;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static String stringToType(String string) {
		if (string.equals(STRING_PROTOCOL_TCP)) {
			return ProtocolConstant.TYPE_PROTOCOL_TCP;
		} else if (string.equals(STRING_PROTOCOL_FTP)) {
			return ProtocolConstant.TYPE_PROTOCOL_FTP;
		} else if (string.equals(STRING_PROTOCOL_SMTP)) {
			return ProtocolConstant.TYPE_PROTOCOL_SMTP;
		} else if (string.equals(STRING_PROTOCOL_SMTPS)) {
			return ProtocolConstant.TYPE_PROTOCOL_SMTPS;
		} else if (string.equals(STRING_PROTOCOL_POP3)) {
			return ProtocolConstant.TYPE_PROTOCOL_POP3;
		} else if (string.equals(STRING_PROTOCOL_POP3S)) {
			return ProtocolConstant.TYPE_PROTOCOL_POP3S;
		} else if (string.equals(STRING_PROTOCOL_IMAP)) {
			return ProtocolConstant.TYPE_PROTOCOL_IMAP;
		} else if (string.equals(STRING_PROTOCOL_IMAPS)) {
			return ProtocolConstant.TYPE_PROTOCOL_IMAPS;
		} else if (string.equals(STRING_PROTOCOL_NTP)) {
			return ProtocolConstant.TYPE_PROTOCOL_NTP;
		} else if (string.equals(STRING_PROTOCOL_DNS)) {
			return ProtocolConstant.TYPE_PROTOCOL_DNS;
		}
		return "";
	}
}
