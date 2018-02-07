/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.hub.conv;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class HubConv {

	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 *
	 *
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.hub.xml.Common versionCollectDto2Xml(Hashtable<String,String> ver){

		com.clustercontrol.utility.settings.hub.xml.Common com = new com.clustercontrol.utility.settings.hub.xml.Common();

		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		com.setGenerateDate(dateFormat.format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));

		return com;
	}
}
