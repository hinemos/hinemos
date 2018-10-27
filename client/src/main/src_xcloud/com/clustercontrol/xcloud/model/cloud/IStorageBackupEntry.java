/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;

public interface IStorageBackupEntry extends IElement {
	interface p {
		static final PropertyId<ValueObserver<String>> id = new PropertyId<ValueObserver<String>>("id"){};
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<ValueObserver<String>> description = new PropertyId<ValueObserver<String>>("description"){};
		static final PropertyId<ValueObserver<String>> status = new PropertyId<ValueObserver<String>>("status"){};
		static final PropertyId<ValueObserver<String>> statusAsPlatform = new PropertyId<ValueObserver<String>>("statusAsPlatform"){};
		static final PropertyId<ValueObserver<Long>> createTime = new PropertyId<ValueObserver<Long>>("createTime"){};
		static final PropertyId<CollectionObserver<IBackupedDataEntry>> backupedDataEntries = new PropertyId<CollectionObserver<IBackupedDataEntry>>("backupedDataEntries", true){};
	}
	
	IStorageBackup getBackup();
	
	String getId();
	String getName();
	String getStatus();
	String getStatusAsPlatform();
	String getDescription();
	Long getCreateTime();
	
	IBackupedDataEntry[] getBackupedDataEntries();
	String getBackupedDataEntryValue(String name);
}
