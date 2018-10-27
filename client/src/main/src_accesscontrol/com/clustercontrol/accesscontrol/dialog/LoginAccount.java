/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.dialog;

/**
 * 接続[ログイン]の入力項目
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class LoginAccount{
	public static final int STATUS_UNCONNECTED = 0;
	public static final int STATUS_CONNECTED = 1;

	private String userId;
	private String password;
	private String url;
	private String managerName = null;
	private int status = STATUS_UNCONNECTED;

	public LoginAccount( String userid, String password, String url, String managerName, int status ){
		this( userid, password, url, managerName );
		setStatus( status );
	}

	public LoginAccount( String userid, String password, String url, String managerName ){
		setUserId(userid);
		setPassword(password);
		setUrl(url);
		setManagerName(managerName);
	}

	/**
	* ユーザIDを返します。
	*/
	public String getUserId(){
		return userId;
	}

	public void setUserId( String userid ){
		this.userId = userid;
	}

	/**
	* パスワードを返します。
	*/
	public String getPassword(){
		return password;
	}

	public void setPassword( String password ){
		this.password = password;
	}

	/**
	* 接続先URLを返します。
	*/
	public String getUrl(){
		return url;
	}

	public void setUrl( String url ){
		this.url = url;
	}

	/**
	* 接続先名前を返します。
	*/
	public String getManagerName(){
		return managerName != null ? managerName : url;
	}

	public void setManagerName(String managerName ){
		this.managerName = managerName;
	}

	public int getStatus(){
		return status;
	}

	public void setStatus( int status ){
		this.status = status;
	}
}
