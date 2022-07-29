/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * ログインに使用するパラメータを保持するクラス
 */
public class LoginParameter {

	/** ログイン先のIPアドレス */
	private String ipAddress;
	/** ユーザID */
	private String userId;
	/** パスワード */
	private String password;
	/** ログイン画面の解像度(横) */
	private Integer width;
	/** ログイン画面の解像度(縦) */
	private Integer height;

	public LoginParameter() {
	}

	/**
	 * コンストラクタ
	 * 
	 * @param ipAddress
	 *            IPアドレス
	 * @param userId
	 *            ユーザID
	 * @param password
	 *            パスワード
	 * @param resolution
	 *            解像度
	 */
	public LoginParameter(String ipAddress, String userId, String password, String resolution) {
		this.ipAddress = ipAddress;
		this.userId = userId;
		this.password = password;
		this.width = Integer.parseInt(resolution.split("x")[0]);
		this.height = Integer.parseInt(resolution.split("x")[1]);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}
}
