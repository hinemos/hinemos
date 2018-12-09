/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

	private CommandConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
