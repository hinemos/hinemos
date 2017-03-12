/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

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
