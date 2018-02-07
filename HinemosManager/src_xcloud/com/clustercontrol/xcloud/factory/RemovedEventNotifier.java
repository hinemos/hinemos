/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.PluginException;

public class RemovedEventNotifier<T> implements AutoCloseable {
	private String type;
	private Class<T> clazz;
	private T object;
	private boolean completed;
	
	public RemovedEventNotifier(Class<T> clazz, String type, T object) throws CloudManagerException {
		this.type = type;
		this.clazz = clazz;
		this.object = object;
		try {
			CloudManager.singleton().getObjMonitor().firePreRemovedEvent(clazz, type, object);
		}
		catch (CloudManagerException e) {
			throw e;
		}
		catch (PluginException e) {
			throw new CloudManagerException(e);
		}
	}
	
	public void completed() {
		completed = true;
	}
	
	@Override
	public void close() throws CloudManagerException {
		if (!completed) return;
		
		try {
			CloudManager.singleton().getObjMonitor().firePostRemovedEvent(clazz, type, object);
		}
		catch (CloudManagerException e) {
			throw e;
		}
		catch (PluginException e) {
			throw new CloudManagerException(e);
		}
	}
}
