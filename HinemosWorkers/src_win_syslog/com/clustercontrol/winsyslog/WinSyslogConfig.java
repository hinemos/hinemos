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
package com.clustercontrol.winsyslog;

import java.util.Properties;

import org.apache.log4j.Logger;

/*
 * syslog.confファイルに設定されているプロパティ値を
 * 取得するためのユーティリティクラス
 */
public enum WinSyslogConfig {
	// TCP 受信の実施可否
	receive_tcp("syslog.receive.tcp", PropertyType.bool, true),
	// UDP 受信の実施可否
	receive_udp("syslog.receive.udp", PropertyType.bool, true),
	// 待ち受けポート
	receive_port("syslog.receive.port", PropertyType.integer, 514),
	// 転送先
	send_targets("syslog.send.targets", PropertyType.string, "127.0.0.1:24514"),
	// TCP の読み込みのタイムアウト
	receive_tcp_read_timeout("syslog.receive.tcp.read.timeout", PropertyType.integer, 1000 * 10),
	// UDP の受信バッファー
	receive_udp_buffer("syslog.receive.udp.buffer", PropertyType.integer),
	// 分割に利用する文字コード（複数指定する場合は「,」で区切る。分割不要の場合は空文字とする。）
	split_code("syslog.split.code", PropertyType.string, "10"),
	// デバッグ時に使用する文字セット
	debug_charset("syslog.debug.charset", PropertyType.string, "utf-8"),
	// TCP の最大受信サイズ
	receive_tcp_read_max_size("syslog.receive.tcp.read.max_size", PropertyType.string, "4096"),
	// 最大ワーカースレッド数
	worker_thread_max_size("syslog.worker.thread.max_size", PropertyType.integer, 1000),
	// ワーカースレッドがシュリンクするまでのタイムアウト
	worker_thread_keepalive_timeout("syslog.worker.thread.keepalive.timeout", PropertyType.integer, 5000),
	;
	
	private static Logger logger = Logger.getLogger(WinSyslogConfig.class);
	
	public interface PropertyType<T> {
		PropertyType<Integer> integer = new PropertyType<Integer>(){
			@Override
			public Integer parse(String prop) {
				try {
					return Integer.parseInt(prop);
				} catch(NumberFormatException e) {
					throw new IllegalArgumentException(e);
				}
			}
			@Override
			public String toString() {
				return "integer";
			}
		};
		PropertyType<Boolean> bool = new PropertyType<Boolean>(){
			@Override
			public Boolean parse(String prop) {
				return Boolean.parseBoolean(prop);
			}
			@Override
			public String toString() {
				return "bool";
			}
		};
		PropertyType<String> string = new PropertyType<String>(){
			@Override
			public String parse(String prop) {
				return prop;
			}
			@Override
			public String toString() {
				return "string";
			}
		};
		
		T parse(String prop) throws IllegalArgumentException;
	}
	
	private static Properties properties = new Properties();
	
	public final String key;
	public final Object defaultValue;
	public final PropertyType<?> type;
	
	private <T> WinSyslogConfig(String id, PropertyType<T> type, T defaultValue) {
		this.key = id;
		this.type = type;
		this.defaultValue = defaultValue;
	}
	
	private <T> WinSyslogConfig(String id, PropertyType<T> type) {
		this.key = id;
		this.type = type;
		this.defaultValue = null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T value(){
		String prop = properties.getProperty(key);
		if (prop != null) {
			try {
				return (T)type.parse(prop);
			} catch(IllegalArgumentException e) {
				logger.debug(String.format("value() : %s can not be parsed. value=%s", key, prop));
				return (T)defaultValue;
			}
		} else {
			logger.debug(String.format("value() : %s is null", key));
			return (T)defaultValue;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T value(T defaultValue){
		String prop = properties.getProperty(key);
		if (prop != null) {
			try {
				return (T)type.parse(prop);
			} catch(IllegalArgumentException e) {
				logger.debug(String.format("value() : %s can not be parsed. value=%s", key, prop));
				return defaultValue;
			}
		} else {
			logger.debug(String.format("value() : %s is null", key));
			return defaultValue;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T defaultValue() {
		return (T)defaultValue;
	}
	
	@Override
	public String toString() {
		return String.format("%s=%s (%s)", key, properties.getProperty(key), defaultValue);
	}
	
	public static void setProperties(Properties properties) {
		WinSyslogConfig.properties = properties;
	}
}