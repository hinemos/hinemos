/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.util.CloudUtil;

public class CloudCryptKey {
	static {
		String key = null;
		
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		String filePath = CloudUtil.createAbsoluteFilePath(CloudConstants.cryptkeyFileRelativePath);
		try {
			fileReader = new FileReader(filePath);
			bufferedReader = new BufferedReader(fileReader);
			key = bufferedReader.readLine();
		} catch (Exception e){
			Logger.getLogger(CloudCryptKey.class).warn("file not readable. (" + filePath + ") : " + e.getMessage(), e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
			}
			try {
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (IOException e) {
			}
		}
		
		cryptKey = key != null ? key: "hinemos";
	}
	
	public static final String cryptKey;
}
