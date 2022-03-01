/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * ログイン処理が終了したこと表すクラス<br>
 * Windows版マネージャでタスクトレイプログラムからログインを行う際にログインコマンドの終了をマネージャに通知する際に使用します。
 */
public class LoginTaskEnd {
	/** ログイン先のIPアドレス */
	private String ipAddress;

	public LoginTaskEnd() {
	}

	public LoginTaskEnd(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

}
