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

package com.clustercontrol.util;

import java.util.ArrayList;

import com.clustercontrol.bean.Property;

/**
 * Hinemosが定義しているクラスに対する値の妥当性確認あるいは変換処理を実装したクラス<BR>
 */
public class HinemosObjectValidator {

	/**
	 * オブジェクトをPropertyクラスの配列に変換します。<BR>
	 * ArrayList<Property>インスタンスの場合は与えられたオブジェクトを返し、そうでない場合は空のArrayList<Property>インスタンスを返します。<BR>
	 * 
	 * @param obj オブジェクト
	 * @return
	 */
	public static ArrayList<Property> objectToArrayListProperty(Object obj) {
		if (!(obj instanceof ArrayList)) {
			return new ArrayList<Property>();
		}

		ArrayList<Property> properties = new ArrayList<Property>();
		for (Object element : (ArrayList<?>)obj) {
			if (!(element instanceof Property)) {
				return new ArrayList<Property>();
			}

			properties.add((Property)element);
		}
		return properties;
	}
}
