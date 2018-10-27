/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.utility.settings.ui.preference.PreferencePageConstant;

public class MultiManagerPathUtil {
	private static Log logger = LogFactory.getLog(MultiManagerPathUtil.class);

	public static String getDirectoryPath(String directoryType){
		IUtilityPreferenceStore store = UtilityPreferenceStore.get();

		String baseDir = null;

		if(directoryType.equals(PreferencePageConstant.KEY_XML)){
			baseDir = store.getString(PreferencePageConstant.KEY_XML);
			if (baseDir.compareTo("") == 0){
				baseDir = PreferencePageConstant.VALUE_XML;
			}
			if(baseDir != null){
				String path = null;
				if (getManagerAddress() == null) {
					path = baseDir;
				} else { 
					path = baseDir + File.separator + getManagerAddress();
				}
				return path + ClientPathUtil.getInstance().getTempPath(path);
			}
		} else
			if(directoryType.equals(PreferencePageConstant.KEY_DIFF_XML)){
				baseDir = store.getString(PreferencePageConstant.KEY_DIFF_XML);
				if (baseDir.compareTo("") == 0){
					baseDir = PreferencePageConstant.VALUE_DIFF_XML;
				}
				if(baseDir != null){
					return baseDir + ClientPathUtil.getInstance().getTempPath(baseDir);
				}
			}

		return null;
	}

	public static String getDirectoryPathTemporary(String directoryType){
		IUtilityPreferenceStore store = UtilityPreferenceStore.get();

		String baseDir = null;

		if (directoryType.equals(PreferencePageConstant.KEY_XML)) {
			baseDir = store.getString(PreferencePageConstant.KEY_XML);
			if (baseDir.compareTo("") == 0) {
				baseDir = PreferencePageConstant.VALUE_XML;
			}
			if (baseDir != null) {
				String path = baseDir + File.separator + getManagerAddress();
				checkAndCreateDirectory(path);
				return path;
			}
		} else {
			if (directoryType.equals(PreferencePageConstant.KEY_DIFF_XML)) {
				baseDir = store.getString(PreferencePageConstant.KEY_DIFF_XML);
				if (baseDir.compareTo("") == 0){
					baseDir = PreferencePageConstant.VALUE_DIFF_XML;
				}
				if(baseDir != null){
					checkAndCreateDirectory(baseDir);
					return baseDir;
				}
			}
		}

		return null;
	}

	private static void checkAndCreateDirectory(String path){
		File newdir = new File(path);
		if (!newdir.exists()){
			if (!newdir.mkdir())
				logger.warn(String.format("Fail to create Directory. %s", newdir.getAbsolutePath()));
		}
	}

	public static String getXMLFileName(String xmlDefaultName){
		IUtilityPreferenceStore store = UtilityPreferenceStore.get();
		String xmlFileName = store.getString(xmlDefaultName);
		if(xmlFileName.compareTo("") == 0) {
			xmlFileName = xmlDefaultName;
		}
		return xmlFileName;
	}

	private static String getManagerAddress(){
		if (UtilityManagerUtil.getCurrentManagerName() == null) {return null;}
		try {
			URL url = new URL(EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr());
			return url.getHost();
		} catch (MalformedURLException e) {
			// TODO
		}
		return null;
	}

	public static String getPreference(String defaultPreference){
		IUtilityPreferenceStore store = UtilityPreferenceStore.get();
		String preference = store.getString(defaultPreference);
		if(preference.compareTo("") == 0) {
			preference = defaultPreference;
		}
		return preference;
	}
}
