/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.msgfilter.extensions;

public interface IRestConnectMsgFilter {

	String OPTION_NAME = "msgfilter";

	public void connect(String urlStr, String userId, String password, String managerName) throws Exception;

	public void disconnect();

	public void delete();

	public boolean isSaved();

	public boolean isActive();
	
	public String getUserId();

	public String getPassword();

	public String getUrlListStr();

	public String getManagerName();

	public int getStatus();
}
