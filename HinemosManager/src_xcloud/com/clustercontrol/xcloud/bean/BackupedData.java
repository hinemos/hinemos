/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

public class BackupedData {
	public static class BackupedDataEntry {
		private String name;
		private String value;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	private List<BackupedDataEntry> entries = new ArrayList<>();

	public List<BackupedDataEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<BackupedDataEntry> entries) {
		this.entries = entries;
	}
}
