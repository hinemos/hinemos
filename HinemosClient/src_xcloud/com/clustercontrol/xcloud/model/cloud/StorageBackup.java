/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openapitools.client.model.StorageBackupEntryResponse;
import org.openapitools.client.model.StorageBackupInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class StorageBackup extends Element implements IStorageBackup {
	private List<StorageBackupEntry> entries;
	
	public StorageBackup(Storage storage) {
		this.setOwner(storage);
	}
	
	@Override
	public StorageBackupEntry[] getEntries() {
		if (entries == null) {
			return new StorageBackupEntry[]{};
		}
		return entries.toArray(new StorageBackupEntry[entries.size()]);
	}

	@Override
	public Storage getStorage() {
		return (Storage)getOwner();
	}
	
	@Override
	public StorageBackupEntry[] getEntriesWithInitializing() {
		if (entries == null)
			update();
		return getEntries();
	}
	
	@Override
	public void update() {
		try {
			Storage storage = getStorage();
			List<StorageBackupInfoResponse> backups = storage.getLocation().getWrapper().getStorageBackups(
					storage.getCloudScope().getId(),
					storage.getLocation().getId(),
					storage.getId());
			
			if (!backups.isEmpty()) {
				update(backups.get(0));
			} else {
				entries = Collections.emptyList();
			}
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
			throw new CloudModelException(e);
		}
	}
	
	public void update(StorageBackupInfoResponse storageBackup) {
		if (entries == null)
			entries = new ArrayList<>();
		
		CollectionComparator.compareCollection(entries, storageBackup.getEntries(), new CollectionComparator.Comparator<StorageBackupEntry, StorageBackupEntryResponse>() {
			@Override
			public boolean match(StorageBackupEntry o1, StorageBackupEntryResponse o2) {
				return o1.getId().equals(o2.getSnapshotId());
			}
			@Override
			public void matched(StorageBackupEntry o1, StorageBackupEntryResponse o2) {
				o1.update(o2);
			}
			@Override
			public void afterO1(StorageBackupEntry o1) {
				internalRemoveProperty(p.entries, o1, entries);
			}
			@Override
			public void afterO2(StorageBackupEntryResponse o2) {
				internalAddProperty(p.entries, StorageBackupEntry.convert(o2), entries);
			}
		});
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return getStorage().getAdapter(adapter);
	}
}
