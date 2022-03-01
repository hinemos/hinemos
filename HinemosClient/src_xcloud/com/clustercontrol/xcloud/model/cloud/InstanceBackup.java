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

import org.openapitools.client.model.InstanceBackupEntryResponse;
import org.openapitools.client.model.InstanceBackupResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class InstanceBackup extends Element implements IInstanceBackup {
	private List<InstanceBackupEntry> entries;
	
	public InstanceBackup(Instance instance) {
		this.setOwner(instance);
	}
	
	@Override
	public InstanceBackupEntry[] getEntries() {
		if (entries == null) {
			return new InstanceBackupEntry[]{};
		}
		return entries.toArray(new InstanceBackupEntry[entries.size()]);
	}

	@Override
	public Instance getInstance() {
		return (Instance)getOwner();
	}
	
	@Override
	public InstanceBackupEntry[] getEntriesWithInitializing() {
		if (entries == null)
			update();
		return getEntries();
	}
	
	@Override
	public void update() {
		try {
			Instance instance = getInstance();
			List<InstanceBackupResponse> backups = instance.getLocation().getWrapper().getInstanceBackups(
					instance.getCloudScope().getId(),
					instance.getLocation().getId(),
					instance.getId());
			
			if (!backups.isEmpty()) {
				update(backups.get(0));
			} else {
				entries = Collections.emptyList();
			}
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
			throw new CloudModelException(e);
		}
	}
	
	public void update(InstanceBackupResponse instanceBackup) {
		if (entries == null)
			entries = new ArrayList<>();
		
		CollectionComparator.compareCollection(entries, instanceBackup.getEntries(), new CollectionComparator.Comparator<InstanceBackupEntry, InstanceBackupEntryResponse>() {
			@Override
			public boolean match(InstanceBackupEntry o1, InstanceBackupEntryResponse o2) {
				return o1.getId().equals(o2.getId());
			}
			@Override
			public void matched(InstanceBackupEntry o1, InstanceBackupEntryResponse o2) {
				o1.update(o2);
			}
			@Override
			public void afterO1(InstanceBackupEntry o1) {
				internalRemoveProperty(p.entries, o1, entries);
			}
			@Override
			public void afterO2(InstanceBackupEntryResponse o2) {
				internalAddProperty(p.entries, InstanceBackupEntry.convert(o2), entries);
			}
		});
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return getInstance().getAdapter(adapter);
	}
}
