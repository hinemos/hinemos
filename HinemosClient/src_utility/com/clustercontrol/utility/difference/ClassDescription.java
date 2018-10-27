/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import com.clustercontrol.utility.difference.anno.AnnoSubstitute;


/**
 * 各クラス毎に付加された DTO 差分検出のための目印を保持。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class ClassDescription {
	public ClassDescription() {
	}
	/**
	 * クラス名。
	 */
	public Class<?> targetClass;
	/**
	 * クラスに付加される目印のリスト。
	 */
	public AnnoSubstitute[] annotations;
	/**
	 * クラスの所属し差分検出の際に参照する必要があるプロパティのリスト。
	 */
	public PropDescription[] props;
}