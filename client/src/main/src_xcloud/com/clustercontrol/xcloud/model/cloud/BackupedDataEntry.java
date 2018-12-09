/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.xcloud.model.base.Element;

public class BackupedDataEntry extends Element implements IBackupedDataEntry {
	private String name = null;
	private String value = null;
	
	public BackupedDataEntry() {
	}
	
	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		internalSetProperty(p.name, name, ()->this.name, (s)->this.name=s);
	}

	@Override
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		internalSetProperty(p.value, value, ()->this.value, (s)->this.value=s);
	}
	
	protected void update(com.clustercontrol.ws.xcloud.BackupedDataEntry source) {
		setName(source.getName());
		setValue(source.getValue());
	}
	
	public static BackupedDataEntry convert(com.clustercontrol.ws.xcloud.BackupedDataEntry source) {
		BackupedDataEntry storage = new BackupedDataEntry();
		storage.update(source);
		return storage;
	}
}
