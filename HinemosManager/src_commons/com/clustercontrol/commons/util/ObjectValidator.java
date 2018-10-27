/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

/**
 * Java標準の基本クラスに対する値の妥当性確認あるいは変換処理を実装したクラス<BR>
 */
public class ObjectValidator {

	/**
	 * オブジェクトがnullあるいは空文字列であるかどうかを確認する。<BR>
	 * 
	 * @param obj オブジェクト
	 * @return trueあるいはfalse
	 */
	public static boolean isEmptyString(Object obj) {
		/** メイン処理 */
		if (obj == null) {
			return true;
		} else if (obj instanceof String && ((String)obj).length() == 0) {
			return true;
		} else {
			return false;
		}
	}
}
