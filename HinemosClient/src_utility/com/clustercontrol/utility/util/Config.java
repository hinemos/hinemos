/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.clustercontrol.version.util.VersionUtil;

/**
 * ツール設定情報を管理するクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 */
public class Config {
	// ツールの定義情報
	public static final String hinemosVersion = VersionUtil.getVersionMajor();
	public static final String toolVersion = VersionUtil.getVersionTool();
	public static final String generator = "java";
	public static final String client= "localhost";

	private static final Map<String, String> properties = new HashMap<String, String>();
	
	private Config() {

	}

	/**
	 * 特定の環境情報を取得する。
	 * 
	 * @param key
	 *            パラメータ名
	 * @return 設定ファイルに記述された値
	 */
	public static String getConfig(String key) {
		return properties.get(key);
	}

	/**
	 * 特定の環境情報を追加する。
	 * 
	 * @param key
	 * @param value
	 */
	public static void putConfig(String key, String value) {
		properties.put(key, value);
	}
	
	/**
	 * XMLに埋め込むバージョン情報を生成する。
	 */
	public static Hashtable<String, String> getVersion() {
		
		Hashtable<String, String> ver = new Hashtable<String, String>();
		
		ver.put("hinemosVersion" ,hinemosVersion);
		ver.put("toolVersion",toolVersion);
		
		//生成ツールの表示
		
		String gen = generator  + " " +  System.getProperties().get("java.version") + " - "
					+ System.getProperties().get("os.name")  +  " "
					+ System.getProperties().get("os.version") ;
		ver.put("generator",gen);
		
		
		//動作ホストの表示
		
		String clientHost;
		String clientAddress = "127.0.0.1";
		try {
			java.net.InetAddress myHost = java.net.InetAddress.getLocalHost();
			clientAddress = myHost.getHostAddress();
			clientHost = myHost.getHostName();
		} catch (UnknownHostException e) {
			clientHost=client;
			
		}
		
		ver.put("runtimeHost",clientHost + "(" + clientAddress + ")");
		
		//マネージャの取得
		String managerHost = Config.getConfig("Login.URL");
		ver.put("connectedManager",managerHost);
		
		
		return ver;
	}
}
