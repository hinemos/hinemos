/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.util.MessageConstant;

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

	// --- 結果を1列で判定する用（ジョブ変数などに利用される）
	/** 存在 */
	public static final int RESULT_EXIST = 1;
	/** 作成 */
	public static final int RESULT_CREATE = 2;
	/** 削除 */
	public static final int RESULT_DELETE = 3;
	/** タイムスタンプ変更 */
	public static final int RESULT_MODIFY_TIMESTAMP = 4;
	/** ファイルサイズ変更 */
	public static final int RESULT_MODIFY_FILESIZE = 5;

	/**
	 * 結果用の種別を文字列に変換します。
	 * 
	 * @param type
	 * @return
	 */
	public static String resultTypeToMessage(int type) {
		if (type == RESULT_EXIST) {
			return MessageConstant.EXISTS.getMessage();
		} else if (type == RESULT_CREATE) {
			return MessageConstant.CREATE.getMessage();
		} else if (type == RESULT_DELETE) {
			return MessageConstant.DELETE.getMessage();
		} else if (type == RESULT_MODIFY_TIMESTAMP) {
			return MessageConstant.TIMESTAMP_MODIFY.getMessage();
		} else if (type == RESULT_MODIFY_FILESIZE) {
			return MessageConstant.FILE_SIZE_MODIFY.getMessage();
		}
		return "";
	}
}