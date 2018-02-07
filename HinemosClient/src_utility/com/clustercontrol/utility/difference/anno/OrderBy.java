/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;


/**
 * 比較結果のソート順を指定するクラス。
 * ソートするカラムを指定するには、該当プロパティへ達するまでのパスを指定する。
 * エージェント監視の "説明" の場合、"MonitorAgent" の "Monitor" から、"Description" という流れでアクセスできる。
 * パスは、プロパティ名の連結で表現し、"説明" の場合は、"Monitor.Description" となる。
 * また、プロパティが配列で、配列の各要素のプロパティに関しても、ソートしたい場合は、配列であることを示すために、
 * "*" を使用する。
 * 文字列の比較情報格納する MonitorStringValueInfo を配列として保持する MonitorHttp を例にとると、
 * MonitorStringValueInfo 内のプロパティをソートする場合は、以下のように記述する。
 * "StringValue.*.OrderNo","StringValue.*.Pattern","StringValue.*.ProcessType"。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class OrderBy extends AnnoSubstitute {
	/**
	 * ソートする順番で、プロパティのパスを格納する。
	 */
	public String[] props;
}
