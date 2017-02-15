/*

Copyright (C) 2006 NTT DATA Corporation

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
 * ジョブ実行時にエージェントにて実行する特殊コマンドの定数を定義するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.0.0
 */
public class CommandConstant {
	/** 公開鍵取得コマンド */
	public static final String GET_PUBLIC_KEY = "getPublicKey";
	/** 公開鍵設定コマンド */
	public static final String ADD_PUBLIC_KEY = "addPublicKey";
	/** 公開鍵削除コマンド */
	public static final String DELETE_PUBLIC_KEY = "deletePublicKey";
	/** ファイルリスト取得コマンド */
	public static final String GET_FILE_LIST = "getFileList";
	/** チェックサム取得コマンド */
	public static final String GET_CHECKSUM = "getCheckSum";
	/** チェックサムチェックコマンド */
	public static final String CHECK_CHECKSUM = "checkCheckSum";
	/** 実行履歴削除コマンド */
	public static final String DELETE_RUN_HISTORY = "deleteRunHistory";
	/** 監視ジョブコマンド */
	public static final String MONITOR = "monitor";

}
