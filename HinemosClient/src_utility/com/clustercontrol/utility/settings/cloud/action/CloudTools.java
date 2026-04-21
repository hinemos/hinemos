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
import org.openapitools.client.model.CheckPublishResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.cloud.HinemosManager;
import com.clustercontrol.xcloud.model.cloud.ICloudScopes;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class CloudTools {
	/* ロガー */
	protected static Logger log = Logger.getLogger(CloudTools.class);
	
	private static List<IHinemosManager> getHinemosManagers(){
		Map<RestConnectUnit, IHinemosManager> hinemosManagers = new HashMap<>();
		List<RestConnectUnit> newEndpoints = RestConnectManager.getActiveManagerList();
		Set<RestConnectUnit> oldEndpoints = hinemosManagers.keySet();
		
		CollectionComparator.compareCollection(newEndpoints, oldEndpoints, new CollectionComparator.Comparator<RestConnectUnit, RestConnectUnit>() {
			@Override
			public boolean match(RestConnectUnit o1, RestConnectUnit o2) {return o1 == o2;}
			@Override
			public void afterO1(RestConnectUnit o1) {hinemosManagers.put(o1, new HinemosManager(o1.getManagerName(), o1.getUrlListStr()));}
			@Override
			public void afterO2(RestConnectUnit o2) {hinemosManagers.remove(o2);}
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

	public static List<com.clustercontrol.xcloud.model.cloud.ICloudScope> getCurrentManagerCloudScopeList(){
		return getTargeManagerCloudScopeList(UtilityManagerUtil.getCurrentManagerName());
	}

	public static List<com.clustercontrol.xcloud.model.cloud.ICloudScope> getTargeManagerCloudScopeList(String targetManagerName){
		List<com.clustercontrol.xcloud.model.cloud.ICloudScope> roots = new ArrayList<>();
		
		List<ICloudScopes> cloudScopeRoots = new ArrayList<>();
		
		//該当のマネージャがなければ 空のリストを返却
		IHinemosManager targetManager = null;
		for (IHinemosManager manager: getHinemosManagers()) {
			if(targetManagerName.equals( manager.getManagerName())){
				targetManager = manager;
				break;
			}
		}
		if(targetManager == null){
			return roots;
		}
		
		//該当のマネージャについてクラウド・ＶＭ機能が有効でなければ 空のリストを返却
		CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper( targetManager.getManagerName());
		try{
			CheckPublishResponse res = endpoint.checkPublish();
			if( res.getPublish().booleanValue() ){
				targetManager.update();
				cloudScopeRoots.add(targetManager.getCloudScopes());
			}
		}catch( RestConnectFailed |HinemosUnknown| CloudManagerException| InvalidRole| InvalidUserPass e){
			return roots;
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

	/**
	 * 指定されたIDのクラウドスコープを取得します。
	 * 該当するクラウドスコープが見つからなければ例外を投げます。
	 * 
	 * @throws IllegalArgumentException
	 */
	public static com.clustercontrol.xcloud.model.cloud.ICloudScope getCloudScope(String cloudScopeId) {
		com.clustercontrol.xcloud.model.cloud.ICloudScope r = getCloudScopeOrNull(cloudScopeId);
		if (r == null) {
			throw new IllegalArgumentException("CloudScope '" + cloudScopeId + "' not found");
		}
		return r;
	}

	/**
	 * 指定されたIDのクラウドスコープを取得します。
	 * 該当するクラウドスコープが見つからなければ null を返します。
	 */
	public static com.clustercontrol.xcloud.model.cloud.ICloudScope getCloudScopeOrNull(String cloudScopeId) {
		for (com.clustercontrol.xcloud.model.cloud.ICloudScope iCloudScope : CloudTools.getCloudScopeList()){
			if (iCloudScope.getId().equals(cloudScopeId)) return iCloudScope;
		}
		return null;
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
	
	public static int checkCurrentManagerCloudPublish( Logger logger ) {
		return checkTargetManagerCloudPublish(UtilityManagerUtil.getCurrentManagerName(),logger);
	}

	public static int checkTargetManagerCloudPublish(String targetManName , Logger logger) {
		CheckPublishResponse res = null;
		try{
			CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(targetManName);
			res = endpoint.checkPublish();
			if (res == null || res.getPublish() == false) {
				// getPublishにて応答がtrueでないならマネージャ側でクラウド機能が有効化されていないと判断する。
				logger.error(Messages.getString("message.xcloud.required"));
				return SettingConstants.ERROR_INPROCESS;
			}
		}catch( Exception e){
			if(UrlNotFound.class.equals(e.getCause().getClass())) {
				// getPublishにてExceptionのCauseがUrlNotFoundはマネージャ側でクラウド機能が有効化されていないと判断する。
				logger.error(Messages.getString("message.xcloud.required"));
			}else{
				logger.error(Messages.getString("SettingTools.ExportFailed"),e);
			}
			return SettingConstants.ERROR_INPROCESS;
		}
		return SettingConstants.SUCCESS;
	}
	
}
