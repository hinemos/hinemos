/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.xcloud;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.util.KeyCheck;

public abstract class CloudPlugin implements HinemosPlugin {
	private static Map<String, CloudPlugin> pluginMap = Collections.synchronizedMap(new HashMap<String, CloudPlugin>());
	
	public CloudPlugin() {
		pluginMap.put(getPluginId(), this);
	}
	
	public abstract String getPluginId();
	// HinemosPlugin のcreate の呼び出しが依存関係の順番を考慮していない。
	// この関数は、activate の呼び出し前に依存関係を考慮して呼び出される。
	public abstract void initialize();
	
	public Map<String, CloudPlugin> getPluginMap() {
		synchronized (pluginMap) {
			return new HashMap<>(pluginMap);
		}
	}

	@Override
	public Set<String> getRequiredKeys() {
		return new HashSet<>(Arrays.asList(KeyCheck.TYPE_XCLOUD));
	}

	/**
	 * 必要なキーが存在するかどうかをチェックする
	 */
	protected boolean checkRequiredKeys() {
		for(String key: getRequiredKeys()){
			if(!KeyCheck.checkKey(key)){
				return false;
			}
		}
		return true;
	}
}
