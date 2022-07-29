/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.io.File;

public class ClientPathUtil {
	private static ClientPathUtil instance = new ClientPathUtil();
	
	public static String getDefaultXMLPath(){
		return "";
	}
	
	public static String getDefaultXMLDiffPath(){
		return "";
	}
	
	public static ClientPathUtil getInstance(){
		return instance;
	}
	
	private ClientPathUtil() {}
	
	public String getTempPath(String parentPath){
		return "";
	}
	
	public boolean lock(String parentPath){
		return false;
	}
	
	public boolean unlock(String parentPath){
		return false;
	}
	
	public boolean isBussy(String parentPath){
		return false;
	}

	public void unlockAll(){}
}
