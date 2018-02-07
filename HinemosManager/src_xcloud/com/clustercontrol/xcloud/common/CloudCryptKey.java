/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.common;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.util.CloudUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class CloudCryptKey {
	static {
		String key = null;
		
		try {
			String filePath = CloudUtil.createAbsoluteFilePath(CloudConstants.cryptkeyFileRelativePath);
			
			ObjectMapper om = new ObjectMapper();
			ObjectReader or = om.readerFor(new TypeReference<Map<String,String>>(){});
			Map<String, String> map = or.readValue(new FileReader(filePath));
			key = map.get(CloudConstants.cryptkeyName);
		} catch (IOException e) {
			Logger.getLogger(CloudCryptKey.class).warn(e.getMessage(), e);
		}
		
		cryptKey = key != null ? key: "hinemos";
	}
	
	public static final String cryptKey;
}
