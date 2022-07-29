/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * バージョン情報管理用ユーティリティクラス
 * 
 * @since 4.0
 */
public class VersionUtil {

	/**
	 * バージョン番号を取得する。<BR>
	 * 
	 * @return バージョン番号
	 */
	public static String getVersion() {
		/** ローカル変数 */
		String homedir = null;
		String path = null;
		FileInputStream versionFile = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		String version = null;

		/** メイン処理 */
		homedir = System.getProperty("hinemos.manager.home.dir");
		path = homedir + File.separator + "_version";

		try {
			versionFile = new FileInputStream(path);
			isr = new InputStreamReader(versionFile);
			reader = new BufferedReader(isr);

			version = reader.readLine();
		} catch (IOException | RuntimeException e) {
			return "unknown";
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
				}
			}
			if (versionFile != null) {
				try {
					versionFile.close();
				} catch (IOException e) {
				}
			}
		}

		return version;
	}
}
