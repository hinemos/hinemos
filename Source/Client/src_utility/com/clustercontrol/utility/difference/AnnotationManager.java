/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.lang.reflect.Method;

import com.clustercontrol.utility.difference.anno.AnnoSubstitute;

/**
 * DTO に関連づいた目印をロード、管理するオブジェクトのインターフェース。
 * 
 * @version 6.0.0
 * @since 2.0.0
 * 
 *
 */
interface AnnotationManager {
	/**
	 * 指定したクラスに紐づいた目印を取得する。
	 * 
	 * @param clazz
	 * @return
	 */
	AnnoSubstitute[] getClassAnno(Class<?> clazz);
	
	/**
	 * 指定したプロパティの get 関数に紐づいた目印を取得する。
	 * 
	 * @param clazz
	 * @return
	 */
	AnnoSubstitute[] getPropAnno(Method method);

	/**
	 * 指定したクラスと目印を関連付ける。
	 * 
	 * @param clazz
	 * @return
	 */
	void addClassAnnos(Class<?> clazz, AnnoSubstitute[] annos);

	/**
	 * 指定したプロパティの get 関数と目印を関連付ける。
	 * 
	 * @param clazz
	 * @return
	 */
	void addPropAnnos(Method method, AnnoSubstitute[] annos);
}