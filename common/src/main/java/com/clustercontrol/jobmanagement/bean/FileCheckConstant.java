/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ファイルチェックの種別定数クラス<BR>
 * 
 * @version 4.1.0
 * @since 4.1.0
 */
public class FileCheckConstant {
	/** 作成の場合 */
	public static final int TYPE_CREATE = 0;
	/** 削除の場合 */
	public static final int TYPE_DELETE = 1;
	/** 変更の場合 */
	public static final int TYPE_MODIFY = 2;
	/** 変更 - タイムスタンプの場合 */
	public static final int TYPE_MODIFY_TIMESTAMP = 0;
	/** 変更 - ファイルサイズの場合 */
	public static final int TYPE_MODIFY_FILESIZE = 1;

	private FileCheckConstant() {
		throw new IllegalStateException("ConstClass");
	}
}