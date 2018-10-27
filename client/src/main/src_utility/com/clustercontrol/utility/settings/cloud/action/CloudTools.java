/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.cloud.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.xcloud.model.cloud.EndpointManager;
import com.clustercontrol.xcloud.model.cloud.HinemosManager;
import com.clustercontrol.xcloud.model.cloud.ICloudScopes;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class CloudTools {
	/* ロガー */
	protected static Logger log = Logger.getLogger(CloudTools.class);
	
	public static <T> T getEndpoint(Class<T> clazz) {
		EndpointManager endpointManager = new EndpointManager(UtilityManagerUtil.getCurrentManagerName());
		return endpointManager.getEndpoint(clazz);
	}
	
	private static List<IHinemosManager> getHinemosManagers(){
		Map<EndpointUnit, IHinemosManager> hinemosManagers = new HashMap<>();
		List<EndpointUnit> newEndpoints = com.clustercontrol.util.EndpointManager.getActiveManagerList();
		Set<EndpointUnit> oldEndpoints = hinemosManagers.keySet();
		
		CollectionComparator.compareCollection(newEndpoints, oldEndpoints, new CollectionComparator.Comparator<EndpointUnit, EndpointUnit>() {
			@Override
			public boolean match(EndpointUnit o1, EndpointUnit o2) {return o1 == o2;}
			@Override
			public void afterO1(EndpointUnit o1) {hinemosManagers.put(o1, new HinemosManager(o1.getManagerName(), o1.getUrlListStr()));}
			@Override
			public void afterO2(EndpointUnit o2) {hinemosManagers.remove(o2);}
		});
		
		return new ArrayList<>(hinemosManagers.values());
	}
	
	public static IHinemosManager getHinemosManager(String managerName){
		// マネージャ名を直接指定する場合、対象マネージャの存在のみ確認
		for(IHinemosManager manager: getHinemosManagers()){
			if(managerName.equals(manager.getManagerName())){
				return manager;
			}
		}
		return null;
	}
	
	
	public static List<com.clustercontrol.xcloud.model.cloud.ICloudScope> getCloudScopeList(){
		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> roots = new ArrayList<>();
		
		List<ICloudScopes> cloudScopeRoots = new ArrayList<>();
		
		
		for (IHinemosManager manager: getHinemosManagers()) {
				manager.update();
				cloudScopeRoots.add(manager.getCloudScopes());
		}
		
		for (ICloudScopes cloudScopes: cloudScopeRoots) {
			roots.addAll(Arrays.asList(cloudScopes.getCloudScopes()));
		}
		
		Collections.sort(roots, new Comparator<com.clustercontrol.xcloud.model.cloud.ICloudScope>() {
			@Override
			public int compare(com.clustercontrol.xcloud.model.cloud.ICloudScope o1, com.clustercontrol.xcloud.model.cloud.ICloudScope o2) {
				int compare = o1.getCloudScopes().getHinemosManager().getManagerName().compareTo(o2.getCloudScopes().getHinemosManager().getManagerName());
				if (compare == 0) {
					return o1.getId().compareTo(o2.getId());
				}
				return compare;
			}
		});
		return roots;
	}
	
	public static List<String> getValidPlatfomIdList() {
		List<String> validList = new ArrayList<>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + ".cloudOption");
		
		for (IExtension ex: point.getExtensions()) {
			for (IConfigurationElement element: ex.getConfigurationElements()) {
				String id = element.getAttribute("platformId");
				log.debug("Lookup Extension : " + ex.getExtensionPointUniqueIdentifier() + ", platform ID : " + id);
				if (id == null)
					continue;
				
				validList.add(id);
			}
		}
		return validList;
	}
}
