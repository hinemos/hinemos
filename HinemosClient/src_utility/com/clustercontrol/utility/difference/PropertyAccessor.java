/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.lang.reflect.Method;

/**
 * "Translate" や "TranslateOverrides" (2012/06/25 時点) を反映したプロパティ値を返すクラスのインターフェース。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public interface PropertyAccessor {
	/**
	 * ラップしているクラスを返す。
	 * 
	 * @return
	 */
	Class<?> getType();
	/**
	 * プロパティ値を取得する。
	 * 
	 * @param obj
	 * @param propGet
	 * @return
	 */
	PropValue getProperty(Object obj, Method propGet);
	/**
	 * プロパティ値を取得する。
	 * 
	 * @param obj
	 * @param propGet
	 * @return
	 */
	PropValue getProperty(Object obj, String propName);
}