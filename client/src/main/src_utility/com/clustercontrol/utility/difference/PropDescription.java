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
 * 各プロパティ毎に付加された DTO 差分検出のための目印を保持。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class PropDescription {
	public PropDescription() {
	}
	/**
	 * プロパティ名。
	 */
	public String propName = "propName";

	/**
	 * プロパティに付加される目印のリスト。
	 */
	public AnnoSubstitute[] annotations = new AnnoSubstitute[0];
}