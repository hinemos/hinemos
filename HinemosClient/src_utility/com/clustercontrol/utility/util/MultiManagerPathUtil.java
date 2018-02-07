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
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;

public class MultiManagerPathUtil {
	private static Log logger = LogFactory.getLog(MultiManagerPathUtil.class);

	public static String getDirectoryPath(String directoryType){
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		String baseDir = null;

		if(directoryType.equals(SettingToolsXMLPreferencePage.KEY_XML)){
			baseDir = store.getString(SettingToolsXMLPreferencePage.KEY_XML);
			if (baseDir.compareTo("") == 0){
				baseDir = SettingToolsXMLPreferencePage.VALUE_XML;
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
			if(directoryType.equals(SettingToolsXMLPreferencePage.KEY_DIFF_XML)){
				baseDir = store.getString(SettingToolsXMLPreferencePage.KEY_DIFF_XML);
				if (baseDir.compareTo("") == 0){
					baseDir = SettingToolsXMLPreferencePage.VALUE_DIFF_XML;
				}
				if(baseDir != null){
					return baseDir + ClientPathUtil.getInstance().getTempPath(baseDir);
				}
			}

		return null;
	}

	public static String getDirectoryPathTemporary(String directoryType){
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		String baseDir = null;

		if (directoryType.equals(SettingToolsXMLPreferencePage.KEY_XML)) {
			baseDir = store.getString(SettingToolsXMLPreferencePage.KEY_XML);
			if (baseDir.compareTo("") == 0) {
				baseDir = SettingToolsXMLPreferencePage.VALUE_XML;
			}
			if (baseDir != null) {
				String path = baseDir + File.separator + getManagerAddress();
				checkAndCreateDirectory(path);
				return path;
			}
		} else {
			if (directoryType.equals(SettingToolsXMLPreferencePage.KEY_DIFF_XML)) {
				baseDir = store.getString(SettingToolsXMLPreferencePage.KEY_DIFF_XML);
				if (baseDir.compareTo("") == 0){
					baseDir = SettingToolsXMLPreferencePage.VALUE_DIFF_XML;
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
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		String xmlFileName = store.getString(xmlDefaultName);
		if(xmlFileName.compareTo("") == 0) {
			xmlFileName = xmlDefaultName;
		}
		return xmlFileName;
	}

	private static String getManagerAddress(){
		if (ClusterControlPlugin.getDefault().getCurrentManagerName() == null) {return null;}
		try {
			URL url = new URL(EndpointManager.get(ClusterControlPlugin.getDefault().getCurrentManagerName()).getUrlListStr());
			return url.getHost();
		} catch (MalformedURLException e) {
			// TODO
		}
		return null;
	}

	public static String getPreference(String defaultPreference){
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		String preference = store.getString(defaultPreference);
		if(preference.compareTo("") == 0) {
			preference = defaultPreference;
		}
		return preference;
	}
}
