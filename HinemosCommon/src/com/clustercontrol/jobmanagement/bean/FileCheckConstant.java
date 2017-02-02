/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
}