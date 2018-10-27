/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.registry;

import com.clustercontrol.xcloud.PluginException;

public abstract class AbstractObjectChangedListener<T> implements IObjectChangedListener<T> {

	@Override
	public void postAdded(String eventName, T object) throws PluginException {
	}

	@Override
	public void preRemoved(String eventName, T object) throws PluginException {
	}

	@Override
	public void postModified(String eventName, T object) throws PluginException {
	}

	@Override
	public void postRemoved(String eventName, T object) throws PluginException {
	}

	@Override
	public void onInstall() {
	}

	@Override
	public void onUninstall() {
	}

	@Override
	public void preAdded(String type, T object) throws PluginException {
	}

	@Override
	public void preModified(String type, T object) throws PluginException {
	}
}
