/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.bean;

import org.openapitools.client.model.PortCheckInfoResponse.ServiceIdEnum;

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
	public static String typeToString(ServiceIdEnum type) {
		switch(type){
		case TCP:
			return STRING_PROTOCOL_TCP;
		case FTP:
			return STRING_PROTOCOL_FTP;
		case SMTP:
			return STRING_PROTOCOL_SMTP;
		case SMTPS:
			return STRING_PROTOCOL_SMTPS;
		case POP3:
			return STRING_PROTOCOL_POP3;
		case POP3S:
			return STRING_PROTOCOL_POP3S;
		case IMAP:
			return STRING_PROTOCOL_IMAP;
		case IMAPS:
			return STRING_PROTOCOL_IMAPS;
		case NTP:
			return STRING_PROTOCOL_NTP;
		case DNS:
			return STRING_PROTOCOL_DNS;
		default:
			return "";
		}
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static ServiceIdEnum stringToType(String string) {
		if (STRING_PROTOCOL_TCP.equals(string)) {
			return ServiceIdEnum.TCP;
		} else if (STRING_PROTOCOL_FTP.equals(string)) {
			return ServiceIdEnum.FTP;
		} else if (STRING_PROTOCOL_SMTP.equals(string)) {
			return ServiceIdEnum.SMTP;
		} else if (STRING_PROTOCOL_SMTPS.equals(string)) {
			return ServiceIdEnum.SMTPS;
		} else if (STRING_PROTOCOL_POP3.equals(string)) {
			return ServiceIdEnum.POP3;
		} else if (STRING_PROTOCOL_POP3S.equals(string)) {
			return ServiceIdEnum.POP3S;
		} else if (STRING_PROTOCOL_IMAP.equals(string)) {
			return ServiceIdEnum.IMAP;
		} else if (STRING_PROTOCOL_IMAPS.equals(string)) {
			return ServiceIdEnum.IMAPS;
		} else if (STRING_PROTOCOL_NTP.equals(string)) {
			return ServiceIdEnum.NTP;
		} else if (STRING_PROTOCOL_DNS.equals(string)) {
			return ServiceIdEnum.DNS;
		} else {
			return null;
		}
	}
}
