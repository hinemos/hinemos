/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalLockManager implements ILockManager {
	
	private final Map<String, LocalLock> _lockMap = new HashMap<String, LocalLock>();
	
	public LocalLockManager() {	}
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.commons.util.ILockManager#create(java.lang.String)
	 */
	@Override
	public synchronized ILock create(String key) {
		LocalLock lock = _lockMap.get(key);
		if (lock == null) {
			lock = new LocalLock(key);
			_lockMap.put(key, lock);
		}
		return lock;
	}
	
	@Override
	public synchronized boolean delete(String key) {
		return _lockMap.remove(key) != null;
	}

	public static class LocalLock implements ILock {
		
		private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
		
		public LocalLock(String key) {
		}
		
		/* (non-Javadoc)
		 * @see com.clustercontrol.commons.util.ILock#readLock()
		 */
		@Override
		public void readLock() {
			_lock.readLock().lock();
		}
		
		/* (non-Javadoc)
		 * @see com.clustercontrol.commons.util.ILock#readUnlock()
		 */
		@Override
		public void readUnlock() {
			_lock.readLock().unlock();
		}
		
		/* (non-Javadoc)
		 * @see com.clustercontrol.commons.util.ILock#writeLock()
		 */
		@Override
		public void writeLock() {
			_lock.writeLock().lock();
		}
		
		/* (non-Javadoc)
		 * @see com.clustercontrol.commons.util.ILock#writeUnlock()
		 */
		@Override
		public void writeUnlock() {
			_lock.writeLock().unlock();
		}

	}
	
}
