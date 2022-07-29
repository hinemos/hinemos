/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * ログインコマンド終了待ちをキャンセルする際に使用するクラス<br>
 * Windows版マネージャでタスクトレイプログラムからログインを行う際にログインコマンドの終了待ちをキャンセルする際に使用します。
 */
public class LoginTaskAbort {
	/** ログイン先のIPアドレス */
	private String ipAddress;

	public LoginTaskAbort() {
	}

	public LoginTaskAbort(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

}
