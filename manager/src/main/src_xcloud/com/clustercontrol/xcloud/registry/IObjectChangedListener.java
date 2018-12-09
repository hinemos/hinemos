/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.registry;

import com.clustercontrol.xcloud.PluginException;

public interface IObjectChangedListener<T> {
	void onInstall();
	void onUninstall();
	void preAdded(String type, T object) throws PluginException;
	void postAdded(String type, T object) throws PluginException;
	void preRemoved(String type, T object) throws PluginException;
	void postRemoved(String type, T object) throws PluginException;
	void preModified(String type, T object) throws PluginException;
	void postModified(String type, T object) throws PluginException;
}
