/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clustercontrol.xcloud.PluginException;

public class ObjMonitor implements IObjMonitor {
	private static class Key {
		private String type;
		private Class<?> clazz;
		
		public Key(String type, Class<?> clazz) {
			super();
			this.type = type;
			this.clazz = clazz;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
	}
	
	private Map<Key, List<IObjectChangedListener<?>>> listenersMap = new HashMap<>();
	
	@Override
	public <T> void addObjectChangedListener(Class<T> clazz, String type, IObjectChangedListener<T> listener) {
		List<IObjectChangedListener<?>> list = listenersMap.get(new Key(type, clazz));
		if (list == null) {
			list = new ArrayList<>();
			listenersMap.put(new Key(type, clazz), list);
		}
		listener.onInstall();
		list.add(listener);
	}
	
	@Override
	public <T> void removeObjectChangedListener(Class<T> clazz, String type, IObjectChangedListener<T> listener) {
		List<IObjectChangedListener<?>> list = listenersMap.get(new Key(type, clazz));
		if (list != null) {
			list.remove(listener);
			listener.onUninstall();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void firePostAddedEvent(Class<T> clazz, String type, T object) throws PluginException {
		List<IObjectChangedListener<?>> list = listenersMap.get(new Key(type, clazz));
		if (list == null) return;
		
		for (IObjectChangedListener<?> listener: list) {
			((IObjectChangedListener<T>)listener).postAdded(type, object);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void firePreRemovedEvent(Class<T> clazz, String type, T object) throws PluginException {
		List<IObjectChangedListener<?>> list = listenersMap.get(new Key(type, clazz));
		if (list == null) return;
		
		for (IObjectChangedListener<?> listener: list) {
			((IObjectChangedListener<T>)listener).preRemoved(type, object);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void firePostModifiedEvent(Class<T> clazz, String type, T object) throws PluginException {
		List<IObjectChangedListener<?>> list = listenersMap.get(new Key(type, clazz));
		if (list == null) return;
		
		for (IObjectChangedListener<?> listener: list) {
			((IObjectChangedListener<T>)listener).postModified(type, object);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void firePreAddedEvent(Class<T> clazz, String type, T object) throws PluginException {
		List<IObjectChangedListener<?>> list = listenersMap.get(new Key(type, clazz));
		if (list == null) return;
		
		for (IObjectChangedListener<?> listener: list) {
			((IObjectChangedListener<T>)listener).preAdded(type, object);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void firePreModifiedEvent(Class<T> clazz, String type, T object) throws PluginException {
		List<IObjectChangedListener<?>> list = listenersMap.get(new Key(type, clazz));
		if (list == null) return;
		
		for (IObjectChangedListener<?> listener: list) {
			((IObjectChangedListener<T>)listener).preModified(type, object);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void firePostRemovedEvent(Class<T> clazz, String type, T object)
			throws PluginException {
		List<IObjectChangedListener<?>> list = listenersMap.get(new Key(type, clazz));
		if (list == null) return;
		
		for (IObjectChangedListener<?> listener: list) {
			((IObjectChangedListener<T>)listener).postRemoved(type, object);
		}
	}
}
