/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalCacheManager extends AbstractCacheManager {
	private static Log m_log = LogFactory.getLog(LocalCacheManager.class);

	// key, value
	private static final Map<Serializable, Serializable> _keyValueStore = new ConcurrentHashMap<>();
	// key.class, set<key>
	private static final Map<Class<?>, Set<Serializable>> _keySetStore = new ConcurrentHashMap<>();

	private static int store_counter = 0;
	private static int get_key_set_counter = 0;

	@Override
	public Serializable get(final Serializable key) {
		return _keyValueStore.get(key);
	}

	private static Object lock = new Object();
	@Override
	public Serializable store(final Serializable key, final Serializable value) {
		if (value == null) {
			throw new NullPointerException("value is null. *use remove to unset the cache*");
		}

		Set<Serializable> set = null;
		synchronized( lock ){
			set = _keySetStore.get(key.getClass());
			if (set == null) {
				set = new ConcurrentSkipListSet<Serializable> ();
				_keySetStore.put(key.getClass(), set);
			}
			if(!set.contains(key)){
				set.add(key);
			}
		}

		if( ++store_counter > 10000 ){
			m_log.info("store() : exceeded 10K! Current key size = " + _keySetStore.get(key.getClass()).size() + ", key = " + key.toString());
			store_counter = 0;
		}

		return _keyValueStore.put(key, value);
	}

	@Override
	public Serializable remove(final Serializable key) {
		Set<Serializable> set = _keySetStore.get(key.getClass());
		set.remove(key);

		return _keyValueStore.remove(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Set<T> getKeySet(Class<T> type) {
		Set<Serializable> set = _keySetStore.get(type);
		if (set == null) {
			set = new ConcurrentSkipListSet<>();
		}

		if( incrementGetKeySetCounter() > 10000 ){
			m_log.info("getKeySet() : exceeded 10K! Current key size = " + set.size() + ", key = " + type.getClass());
			resetGetKeySetCounter();
		}

		return (ConcurrentSkipListSet<T>)set;
	}
	
	private static int incrementGetKeySetCounter() {
		return ++get_key_set_counter;
	}
	
	private static void resetGetKeySetCounter() {
		get_key_set_counter = 0;
	}
}
