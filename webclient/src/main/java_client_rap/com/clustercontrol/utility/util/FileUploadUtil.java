/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import org.eclipse.rap.rwt.SingletonUtil;

public class FileUploadUtil {
	private FileUploadUtil(){}
	
	public static FileUploadUtil getInstance(){
		return SingletonUtil.getSessionInstance(FileUploadUtil.class);
	}
	
	public boolean upload(String filePath){
		
		
		
		return false;
	}
	
	public boolean delete(String filePath){
		
		
		return false;
	}
}
