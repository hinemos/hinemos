/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ[実行契機]の種別定数クラス<BR>
 * 
 * @version 4.1.0
 * @since 4.1.0
 */
public class JobKickConstant {
	/** スケジュールの場合 */
	public static final int TYPE_SCHEDULE= 0;

	/** ファイルチェックの場合 */
	public static final int TYPE_FILECHECK = 1;

	/** マニュアル実行の場合 */
	public static final int TYPE_MANUAL = 2;

	private JobKickConstant() {
		throw new IllegalStateException("ConstClass");
	}
}