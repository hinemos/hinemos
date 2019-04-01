/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

/**
 * 構成情報検索処理で使用するユーティリティクラス
 * 
 * @version 6.2.0
 */
public class NodeConfigFilterUtil {

	/** ファイルID（構成情報CSVファイル用） */
	private static Integer fileIdSequeance = 0;

	/**
	 * パッケージ情報のバージョン比較
	 * 
	 * @param v1 比較対象バージョン
	 * @param v2 比較対象バージョン
	 * @return 1：v1 > v2, 0：v1 > v2, -1：v1 < v2 
	 */
	public static int compareVersion(String v1, String v2) {
		String[] v1Arr = v1.split("\\.");
		String[] v2Arr = v2.split("\\.");
		int maxlength = Math.min(v1Arr.length, v2Arr.length);

		for (int i = 0; i < maxlength; ++i) {
			int result = subCompareVersion(v1Arr[i], v2Arr[i]);
			if (result != 0) {
				return result;
			}
		}

		if (v1Arr.length > v2Arr.length) {
			return 1;
		} else if (v1Arr.length < v2Arr.length) {
			return -1;
		}
		return 0;
	}

	/**
	 * パッケージ情報のバージョン比較
	 * 
	 * @param v1Sub
	 * @param v2Sub
	 * @return
	 */
	private static int subCompareVersion(String v1Sub, String v2Sub) {
		boolean isDigit = true;
		for (int i = 0; i < v1Sub.length(); ++i) {
			if (!Character.isDigit(v1Sub.charAt(i))) {
				isDigit = false;
				break;
			}
		}
		if (isDigit) {
			for (int i = 0; i < v2Sub.length(); ++i) {
				if (!Character.isDigit(v2Sub.charAt(i))) {
					isDigit = false;
					break;
				}
			}
		}
		if (isDigit) {
			try {
				int v1SubInt = Integer.parseInt(v1Sub);
				int v2SubInt = Integer.parseInt(v2Sub);
				return Integer.compare(v1SubInt, v2SubInt);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return v1Sub.compareTo(v2Sub);
	}

	/**
	 * 構成情報ファイルの一時ファイルIDを返します。
	 * 
	 * @return ファイルID
	 */
	public static synchronized String getNewFileId() {
		// ファイルIDインクリメント
		++fileIdSequeance;
		return fileIdSequeance.toString();
	}
}
