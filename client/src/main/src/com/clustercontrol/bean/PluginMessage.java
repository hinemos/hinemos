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

package com.clustercontrol.bean;

import com.clustercontrol.util.Messages;

/**
 * Hinemosの機能を定数として格納するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.2
 */
public class PluginMessage {

	/** リポジトリ */
	public static final String STRING_REPOSITORY = Messages.getString("repository");
	/** アクセス */
	public static final String STRING_ACCESSCONTROL = Messages.getString("accesscontrol");
	/** ジョブ管理 */
	public static final String STRING_JOBMANAGEMENT = Messages.getString("job.management");
	/** 性能管理 */
	public static final String STRING_PERFORMANCE = Messages.getString("performance");
	/** 監視設定 */
	public static final String STRING_MONITOR = Messages.getString("monitor.setting");
	/** システムログ監視 */
	public static final String STRING_SYSTEMLOG_MONITOR = Messages.getString("systemlog.monitor");
	/** Hinemosエージェント監視 */
	public static final String STRING_AGENT_MONITOR = Messages.getString("agent.monitor");
	/** HTTP監視 */
	public static final String STRING_HTTP_MONITOR = Messages.getString("http.monitor");
	/** プロセス監視 */
	public static final String STRING_PROCESS_MONITOR = Messages.getString("process.monitor");
	/** SQL監視 */
	public static final String STRING_SQL_MONITOR = Messages.getString("sql.monitor");
	/** SNMP監視 */
	public static final String STRING_SNMP_MONITOR = Messages.getString("snmp.monitor");
	/** PING監視 */
	public static final String STRING_PING_MONITOR = Messages.getString("ping.monitor");
	/** カレンダ */
	public static final String STRING_CALENDAR = Messages.getString("calendar");
	/** 通知 */
	public static final String STRING_NOTIFY = Messages.getString("notify");
	/** 重要度判定 */
	public static final String STRING_PRIORITY_JUDGMENT = Messages.getString("priority.judgment");
	/** ログ転送 */
	public static final String STRING_LOG_TRANSFER = Messages.getString("logtransfer");
	/** 障害検知 */
	public static final String STRING_TROUBLE_DETECTION = Messages.getString("trouble.detection");
	/** SNMPTRAP監視 */
	public static final String STRING_SNMPTRAP_MONITOR = Messages.getString("snmptrap.monitor");
	/** リソース監視 */
	public static final String STRING_PERFORMANCE_MONITOR = Messages.getString("performance.monitor");
	/** サービス・ポート監視 */
	public static final String STRING_PORT_MONITOR = Messages.getString("port.monitor");
	/** カスタム監視 */
	public static final String STRING_CUSTOM_MONITOR = Messages.getString("command.monitor");
	/** Windowsサービス監視 */
	public static final String STRING_WINSERVICE_MONITOR = Messages.getString("winservice.monitor");
	/** Windowsイベント監視 */
	public static final String STRING_WINEVENT_MONITOR = Messages.getString("winevent.monitor");
	/** カスタムトラップ監視 */
	public static final String STRING_CUSTOMTRAP_MONITOR = Messages.getString("customtrap.monitor");
	/** 環境構築 */
	public static final String STRING_INFRA = Messages.getString("infra");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == PluginConstant.TYPE_ACCESSCONTROL) {
			return STRING_ACCESSCONTROL;
		} else if (type == PluginConstant.TYPE_AGENT_MONITOR) {
			return STRING_AGENT_MONITOR;
		} else if (type == PluginConstant.TYPE_CALENDAR) {
			return STRING_CALENDAR;
		} else if (type == PluginConstant.TYPE_HTTP_MONITOR) {
			return STRING_HTTP_MONITOR;
		} else if (type == PluginConstant.TYPE_JOBMANAGEMENT) {
			return STRING_JOBMANAGEMENT;
		} else if (type == PluginConstant.TYPE_LOG_TRANSFER) {
			return STRING_LOG_TRANSFER;
		} else if (type == PluginConstant.TYPE_MONITOR) {
			return STRING_MONITOR;
		} else if (type == PluginConstant.TYPE_NOTIFY) {
			return STRING_NOTIFY;
		} else if (type == PluginConstant.TYPE_PERFORMANCE) {
			return STRING_PERFORMANCE;
		} else if (type == PluginConstant.TYPE_PING_MONITOR) {
			return STRING_PING_MONITOR;
		} else if (type == PluginConstant.TYPE_PRIORITY_JUDGMENT) {
			return STRING_PRIORITY_JUDGMENT;
		} else if (type == PluginConstant.TYPE_PROCESS_MONITOR) {
			return STRING_PROCESS_MONITOR;
		} else if (type == PluginConstant.TYPE_REPOSITORY) {
			return STRING_REPOSITORY;
		} else if (type == PluginConstant.TYPE_SNMP_MONITOR) {
			return STRING_SNMP_MONITOR;
		} else if (type == PluginConstant.TYPE_SNMPTRAP_MONITOR) {
			return STRING_SNMPTRAP_MONITOR;
		} else if (type == PluginConstant.TYPE_SQL_MONITOR) {
			return STRING_SQL_MONITOR;
		} else if (type == PluginConstant.TYPE_SYSTEMLOG_MONITOR) {
			return STRING_SYSTEMLOG_MONITOR;
		} else if (type == PluginConstant.TYPE_TROUBLE_DETECTION) {
			return STRING_TROUBLE_DETECTION;
		} else if (type == PluginConstant.TYPE_PERFORMANCE_MONITOR) {
			return STRING_PERFORMANCE_MONITOR;
		} else if (type == PluginConstant.TYPE_PORT_MONITOR) {
			return STRING_PORT_MONITOR;
		} else if (type == PluginConstant.TYPE_CUSTOM_MONITOR) {
			return STRING_CUSTOM_MONITOR;
		} else if (type == PluginConstant.TYPE_WINSERVICE_MONITOR) {
			return STRING_WINSERVICE_MONITOR;
		} else if (type == PluginConstant.TYPE_WINEVENT_MONITOR) {
			return STRING_WINEVENT_MONITOR;
		} else if (type == PluginConstant.TYPE_INFRA) {
			return STRING_INFRA;
		} else if (type == PluginConstant.TYPE_CUSTOMTRAP_MONITOR) {
			return STRING_CUSTOMTRAP_MONITOR;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_ACCESSCONTROL)) {
			return PluginConstant.TYPE_ACCESSCONTROL;
		} else if (string.equals(STRING_AGENT_MONITOR)) {
			return PluginConstant.TYPE_AGENT_MONITOR;
		} else if (string.equals(STRING_CALENDAR)) {
			return PluginConstant.TYPE_CALENDAR;
		} else if (string.equals(STRING_HTTP_MONITOR)) {
			return PluginConstant.TYPE_HTTP_MONITOR;
		} else if (string.equals(STRING_JOBMANAGEMENT)) {
			return PluginConstant.TYPE_JOBMANAGEMENT;
		} else if (string.equals(STRING_LOG_TRANSFER)) {
			return PluginConstant.TYPE_LOG_TRANSFER;
		} else if (string.equals(STRING_MONITOR)) {
			return PluginConstant.TYPE_MONITOR;
		} else if (string.equals(STRING_NOTIFY)) {
			return PluginConstant.TYPE_NOTIFY;
		} else if (string.equals(STRING_PERFORMANCE)) {
			return PluginConstant.TYPE_PERFORMANCE;
		} else if (string.equals(STRING_PING_MONITOR)) {
			return PluginConstant.TYPE_PING_MONITOR;
		} else if (string.equals(STRING_PRIORITY_JUDGMENT)) {
			return PluginConstant.TYPE_PRIORITY_JUDGMENT;
		} else if (string.equals(STRING_PROCESS_MONITOR)) {
			return PluginConstant.TYPE_PROCESS_MONITOR;
		} else if (string.equals(STRING_REPOSITORY)) {
			return PluginConstant.TYPE_REPOSITORY;
		} else if (string.equals(STRING_SNMP_MONITOR)) {
			return PluginConstant.TYPE_SNMP_MONITOR;
		} else if (string.equals(STRING_SNMPTRAP_MONITOR)) {
			return PluginConstant.TYPE_SNMPTRAP_MONITOR;
		} else if (string.equals(STRING_SQL_MONITOR)) {
			return PluginConstant.TYPE_SQL_MONITOR;
		} else if (string.equals(STRING_SYSTEMLOG_MONITOR)) {
			return PluginConstant.TYPE_SYSTEMLOG_MONITOR;
		} else if (string.equals(STRING_TROUBLE_DETECTION)) {
			return PluginConstant.TYPE_TROUBLE_DETECTION;
		} else if (string.equals(STRING_PERFORMANCE_MONITOR)) {
			return PluginConstant.TYPE_PERFORMANCE_MONITOR;
		} else if (string.equals(STRING_PORT_MONITOR)) {
			return PluginConstant.TYPE_PORT_MONITOR;
		} else if (string.equals(STRING_CUSTOM_MONITOR)) {
			return PluginConstant.TYPE_CUSTOM_MONITOR;
		} else if (string.equals(STRING_WINSERVICE_MONITOR)) {
			return PluginConstant.TYPE_WINSERVICE_MONITOR;
		} else if (string.equals(STRING_WINEVENT_MONITOR)) {
			return PluginConstant.TYPE_WINEVENT_MONITOR;
		} else if (string.equals(STRING_INFRA)) {
			return PluginConstant.TYPE_INFRA;
		} else if (string.equals(STRING_CUSTOMTRAP_MONITOR)) {
			return PluginConstant.TYPE_CUSTOMTRAP_MONITOR;
		}
		return -1;
	}
}
