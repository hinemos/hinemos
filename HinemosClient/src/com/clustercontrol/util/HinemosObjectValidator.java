/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
