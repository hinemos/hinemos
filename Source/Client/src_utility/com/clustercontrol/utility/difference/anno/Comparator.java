/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;

import com.clustercontrol.utility.difference.ResultC;

/**
 * DiffComparator を実装したクラスをプロパティに関連付けるクラス。
 * この目印を指定した先のプロパティの比較は、DiffComparator を実装したクラスに委譲する。
 * ※現在の実装では、正常に機能しない。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class Comparator extends AnnoSubstitute {

	public interface DiffComparator {
		boolean compare(Object dto1, Object dto2, Class<?> targetClass, ResultC resultC);
	}
	
	/**
	 * 比較ロジックを実装したコンパレーターのクラス。
	 */
	public Class<?> compType;
}
