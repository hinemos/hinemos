/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;


/**
 * 環境構築変数情報に関する情報を定義するクラス<BR>
 *
 * @version 6.1.0
 */
public class InfraParameterConstant {

	// アクセス情報キー
	/** SSHユーザ */
	public static final String SSH_USER = "SSH_USER";
	/** SSHパスワード */
	public static final String SSH_PASSWORD = "SSH_PASSWORD";
	/** SSH秘密鍵ファイルパス */
	public static final String SSH_PRIVATE_KEY_FILEPATH = "SSH_PRIVATE_KEY_FILEPATH";
	/** SSH秘密鍵パスフレーズ */
	public static final String SSH_PRIVATE_KEY_PASSPHRASE = "SSH_PRIVATE_KEY_PASSPHRASE";
	/** WinRMユーザ */
	public static final String WINRM_USER = "WINRM_USER";
	/** WinRMパスワード */
	public static final String WINRM_PASSWORD = "WINRM_PASSWORD";

	/** 環境構築変数情報区切り文字 */
	public final static String PARAMETER_DELIMITER = ":";

	// アクセス情報
	public static final String ACCESS_INFO_LIST_PARAM[] = {
		SSH_USER,
		SSH_PASSWORD,
		SSH_PRIVATE_KEY_FILEPATH,
		SSH_PRIVATE_KEY_PASSPHRASE,
		WINRM_USER,
		WINRM_PASSWORD
	};

	private InfraParameterConstant() {
		throw new IllegalStateException("ConstClass");
	}
}