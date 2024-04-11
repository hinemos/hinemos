/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.CloudLoginUserInfoResponse;
import org.openapitools.client.model.CloudPlatformInfoResponse;
import org.openapitools.client.model.HRepositoryResponse;
import org.openapitools.client.model.InstanceBackupResponse;
import org.openapitools.client.model.InstanceInfoResponse;
import org.openapitools.client.model.StorageBackupInfoResponse;
import org.openapitools.client.model.StorageInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.repository.CloudRepository;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class HinemosManager extends Element implements IHinemosManager {
	
	private static final Log logger = LogFactory.getLog(HinemosManager.class);
	
	private String managerName;
	private String url;
	
	private boolean Initialized;

	private ElementBaseModeWatch modelWatcher;
	
	private List<CloudPlatform> cloudPlatforms;

	private CloudScopes cloudScopes;
	private CloudRepository cloudRepository;
	private BillingMonitors billingAlarms;
	
	private boolean expirationDialog;

	public HinemosManager(String managerName, String url){
		this.url = url;
		this.managerName = managerName;
		this.cloudRepository = new CloudRepository(this);
		this.billingAlarms = new BillingMonitors(this);
	}

	@Override
	public CloudRestClientWrapper getWrapper() {
		return CloudRestClientWrapper.getWrapper(managerName);
	}

	@Override
	public String getAccountName() {
		return RestConnectManager.get(managerName).getUserId();
	}

	@Override
	public String getManagerName(){
		return managerName;
	}

	@Override
	public CloudRepository getCloudRepository() {
		if(cloudRepository == null){
			cloudRepository = new CloudRepository(this);
		}
		return cloudRepository;
	}

	@Override
	public CloudPlatform[] getCloudPlatforms() {
		if (cloudPlatforms == null)
			update();
		return cloudPlatforms.toArray(new CloudPlatform[cloudPlatforms.size()]);
	}

	@Override
	public ICloudPlatform getCloudPlatform(String cloudPlatformId) {
		for(CloudPlatform cloudPlatform: getCloudPlatforms()){
			if(cloudPlatform.getId().equals(cloudPlatformId)){
				return cloudPlatform;
			}
		}
		throw new CloudModelException(String.format("Not found platform of %s", cloudPlatformId));
	}

	@Override
	public void update() {
		long start1 = System.currentTimeMillis();
		
		try {
			CloudRestClientWrapper wrapper = getWrapper();
			
			// 起動キーチェック処理
			try {
				if(!expirationDialog) {
					expirationDialog = true;
					wrapper.checkPublish();
				}
			} catch (HinemosUnknown e) {
				//マルチマネージャ接続時にクラウド/ＶＭが有効になってないマネージャの混在がありえる。
				//バージョンが取得できない（endpoint通信で異常が出る）なら クラウド/ＶＭが有効になってないマネージャとみなす。
				//運用上は想定内の状況なので、警告ログのみ出力して処理は続行する。
				if(UrlNotFound.class.equals(e.getCause().getClass())) {
					logger.warn("update(): checkPublish() : UrlNotFound. managerName=" + managerName);
				} else {
					MessageDialog.openWarning(null, 
							Messages.getString("warning"), 
							e.getMessage());
				}
			} catch (Throwable e) {
				MessageDialog.openWarning(null, 
						Messages.getString("warning"), 
						e.getMessage());
			}

			long start2 = System.currentTimeMillis();
			HRepositoryResponse repository = wrapper.getRepository(null);
			logger.debug(String.format("CloudRestEndpoint.getRepository : elapsed time=%dms", System.currentTimeMillis() - start2));

			updateCloudPlatforms(repository.getPlatforms());

			updateCloudScopes(repository);
			
			updateCloudRepository(repository);
			
			{
				Map<String, List<CloudLoginUserInfoResponse>> userMap = new HashMap<>();
				for (CloudLoginUserInfoResponse user: repository.getLoginUsers()) {
					List<CloudLoginUserInfoResponse> users = userMap.get(user.getEntity().getCloudScopeId());
					if (users == null) {
						users = new ArrayList<>();
						userMap.put(user.getEntity().getCloudScopeId(), users);
					}
					users.add(user);
				}
				for (CloudScope scope: getCloudScopes().getCloudScopes()) {
					List<CloudLoginUserInfoResponse> users = userMap.get(scope.getId());
					if (users != null)
						scope.getLoginUsers().update(users);
				}
			}
			
			logger.debug(String.format("HinemosManager.update : elapsed time=%dms", System.currentTimeMillis() - start1));
			
			Initialized = true;
		} catch (RuntimeException e) {
			// findbugs対応 RuntimeExceptionのcatch を明示化
			throw new CloudModelException(e.getMessage(), e);
		} catch (Exception e) {
			throw new CloudModelException(e.getMessage(), e);
		}
	}
	
	public void updateCloudPlatforms(List<CloudPlatformInfoResponse> CloudPlatforms) {
		if (cloudPlatforms == null)
			cloudPlatforms = new ArrayList<>();

		CollectionComparator.compareCollection(cloudPlatforms, CloudPlatforms, new CollectionComparator.Comparator<CloudPlatform, CloudPlatformInfoResponse>() {
			public boolean match(CloudPlatform o1, CloudPlatformInfoResponse o2) {return o1.equalValues(o2);}
			public void matched(CloudPlatform o1, CloudPlatformInfoResponse o2) {o1.update(o2);}
			public void afterO1(CloudPlatform o1) {cloudPlatforms.remove(o1);}
			public void afterO2(CloudPlatformInfoResponse o2) {
				CloudPlatform newCloudPlatform = CloudPlatform.convert(HinemosManager.this, o2);
				cloudPlatforms.add(newCloudPlatform);
			}
		});
	}

	@Override
	public void updateLocation(final ILocation location) {
		CloudRestClientWrapper wrapper = getWrapper();
		
		HRepositoryResponse repository;
		try {
			repository = wrapper.updateLocationRepository(location.getCloudScope().getId(), location.getId());
		} catch (Exception e) {
			throw new CloudModelException(e.getMessage(), e);
		}
		
		ComputeResources resources = getCloudScopes().getCloudScope(location.getCloudScope().getId()).getLocation(location.getId()).getComputeResources();

		List<InstanceInfoResponse> instances = new ArrayList<>(repository.getInstances());
		Iterator<InstanceInfoResponse> iter = instances.iterator();
		while (iter.hasNext()) {
			InstanceInfoResponse instance = iter.next();
			if (!(instance.getCloudScopeId().equals(location.getCloudScope().getId()) && instance.getLocationId().equals(location.getId()))) {
				iter.remove();
			}
		}
		resources.updateInstances(instances);
		
		Map<String, InstanceBackupResponse> instanceBackupMap = new HashMap<>();
		for (InstanceBackupResponse backup: repository.getInstanceBackups()) {
			instanceBackupMap.put(backup.getInstanceId(), backup);
		}
		for (Instance i: resources.getInstances()) {
			i.getBackup().update(instanceBackupMap.get(i.getId()));
		}
		
		List<StorageInfoResponse> storages = new ArrayList<>(repository.getStorages());
		Iterator<StorageInfoResponse> storageIter = storages.iterator();
		while (storageIter.hasNext()) {
			StorageInfoResponse storage = storageIter.next();
			if (!(storage.getCloudScopeId().equals(location.getCloudScope().getId()) && storage.getLocationId().equals(location.getId()))) {
				iter.remove();
			}
		}
		resources.updateStorages(storages);
		
		Map<String, StorageBackupInfoResponse> storageBackupMap = new HashMap<>();
		for (StorageBackupInfoResponse backup: repository.getStorageBackups()) {
			storageBackupMap.put(backup.getStorageId(), backup);
		}
		for (Storage s: resources.getStorages()) {
			s.getBackup().update(storageBackupMap.get(s.getId()));
		}

		getCloudRepository().updateLocation(location, repository);
	}
	
	public void updateCloudScopes(HRepositoryResponse repository) {
		getCloudScopes().updateCloudScopes(repository);
	}
	
	public void updateLoginUsers() {
		for (CloudScope scope: getCloudScopes().getCloudScopes()) {
			scope.getLoginUsers().update();
		}
	}
	
	public void updateCloudRepository(HRepositoryResponse repository) {
		getCloudRepository().update(repository);
	}
	
	@Override
	public CloudScopes getCloudScopes() {
		if (cloudScopes == null)
			cloudScopes = new CloudScopes(this);
		return cloudScopes;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public ElementBaseModeWatch getModelWatch() {
		if (modelWatcher == null)
			modelWatcher = new ElementBaseModeWatch(this);
		return modelWatcher;
	}

	@Override
	public String toString() {
		return "HinemosManager [managerName=" + managerName + ", url=" + url
				+ "]";
	}

	@Override
	public IBillingMonitors getBillingAlarms() {
		return billingAlarms;
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IHinemosManager.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public boolean isInitialized() {
		return Initialized;
	}
}
