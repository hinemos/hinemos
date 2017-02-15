/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
