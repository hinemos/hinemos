/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hinemosエージェントのライブラリファイル一覧。
 * 
 * @since 6.2.0
 */
public class AgentLibMd5s {
	
	private Map<String, String> map;

	/**
	 * 空のインスタンスを生成します。
	 */
	public AgentLibMd5s() {
		this.map = new ConcurrentHashMap<>();
	}

	/**
	 * keyがファイルパス、valueがMD5チェックサムであるMapから、インスタンスを生成します。
	 * 
	 * @param source もととなるMap。
	 */
	public AgentLibMd5s(Map<String, String> source) {
		this();
		for (Entry<String, String> entry : source.entrySet()) {
			setMd5(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * keyがファイルパス、valueがMD5チェックサムであるMapを返します。
	 * <p>
	 * 返したMapに対する変更が本インスタンスに反映されるかどうかは未定義ですので、行わないでください。
	 */
	public Map<String, String> asMap() {
		// 現実装では中身を返すだけ
		return map;
	}
	
	/**
	 * 指定されたファイルのMD5を設定します。
	 * 
	 * @param filepath ファイルパス。
	 */
	public void setMd5(String filepath, String md5) {
		map.put(normalize(filepath), md5);
	}
	
	/**
	 * 指定されたファイルのMD5を返します。
	 * 
	 * @param filepath ファイルパス。
	 * @return 保持しているならMD5チェックサム。保持していないならnull。
	 */
	public String getMd5(String filepath) {
		return map.get(normalize(filepath));
	}

	/**
	 * 格納しているファイルパスとMD5の組の数を返します。
	 */
	public int getSize() {
		return map.size();
	}
	
	// ファイルセパレータをマネージャ側の形式に統一する
	private static String normalize(String filepath) {
		return filepath.replace("/", File.separator).replace("\\", File.separator);
	}
}
