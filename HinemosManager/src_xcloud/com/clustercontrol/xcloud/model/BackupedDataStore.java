/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BackupedDataStore {
	public List<BackupedData> getBackupedData() {
		return new ArrayList<>(getBackedupDataMap().values());
	}
	
	public BackupedData getBackupedData(String name) {
		return getBackedupDataMap().get(name);
	}

	public void putBackupedData(String name, String value) {
		BackupedData property = getBackedupDataMap().get(name);
		if (property == null) {
			property = new BackupedData();
			property.setName(name);
			property.setValue(value);
			
			getBackedupDataMap().put(name, property);
		} else {
			property.setValue(value);
		}
	}
	
	public void removeBackupedData(String name) {
		getBackedupDataMap().remove(name);
	}
	
	protected abstract Map<String, BackupedData> getBackedupDataMap();
}
