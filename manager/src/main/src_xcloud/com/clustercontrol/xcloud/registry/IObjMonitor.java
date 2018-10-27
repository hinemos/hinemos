/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.registry;

import com.clustercontrol.xcloud.PluginException;



public interface IObjMonitor {
	<T> void addObjectChangedListener(Class<T> clazz, String eventType, IObjectChangedListener<T> listener);
	<T> void removeObjectChangedListener(Class<T> clazz, String eventType, IObjectChangedListener<T> listener);

	<T> void firePreAddedEvent(Class<T> clazz, String eventType, T object) throws PluginException;
	<T> void firePostAddedEvent(Class<T> clazz, String eventType, T object) throws PluginException;
	<T> void firePreRemovedEvent(Class<T> clazz, String eventType, T object) throws PluginException;
	<T> void firePostRemovedEvent(Class<T> clazz, String eventType, T object) throws PluginException;
	<T> void firePreModifiedEvent(Class<T> clazz, String eventType, T object) throws PluginException;
	<T> void firePostModifiedEvent(Class<T> clazz, String eventType, T object) throws PluginException;
}
