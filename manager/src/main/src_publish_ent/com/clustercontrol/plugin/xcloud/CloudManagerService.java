/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.xcloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebParam;

import org.apache.log4j.Logger;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.ws.xcloud.CloudEndpointImpl;
import com.clustercontrol.ws.xcloud.MethodRestriction;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntry;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.task.CloudTaskService;

public class CloudManagerService extends CloudPlugin {
	public final static String id = CloudManagerService.class.getName();

	/**
	 * CloudServiceのコンストラクタ
	 */
	public CloudManagerService() {
		super();
	}

	@Override
	public Set<String> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public void create() {
		Logger logger = Logger.getLogger(this.getClass());

		// メイン処理
		logger.info("creating " + this.getClass().getSimpleName() + "...");

		logger.info("successful in creating " + this.getClass().getSimpleName() + "...");
	}

	@Override
	public void activate() {
		Logger logger = Logger.getLogger(this.getClass());

		// Check if key exists
		if(!checkRequiredKeys()){
			logger.warn("KEY NOT FOUND! Unable to activate " + this.getClass().getName());
			return;
		}

		// メイン処理
		logger.info("activate " + this.getClass().getSimpleName() + "...");
		
		CloudManager.singleton().open();

		// クラウド管理プラグインの初期化
		final Map<String, CloudPlugin> pluginMap = new HashMap<>();
		List<CloudPlugin> pluginList = new ArrayList<>(getPluginMap().values());
		for (CloudPlugin plugin: getPluginMap().values()) {
			pluginMap.put(plugin.getClass().getName(), plugin);
		}
		
		Collections.sort(pluginList, new Comparator<CloudPlugin>() {
				public int compare(CloudPlugin o1, CloudPlugin o2) {
					if (checkNegative(o1, o2)) {
						return -1;
					}
					else if (checkPositive(o1, o2)) {
						return 1;
					}
					return 0;
				}

				private boolean checkNegative(CloudPlugin o1, CloudPlugin dependencyPlugin) {
					for (String dependency: dependencyPlugin.getDependency()) {
						if (!dependency.equals(o1.getClass().getName())) {
							CloudPlugin nextDependencyPlugin = pluginMap.get(dependency);
							if (checkNegative(o1, nextDependencyPlugin)) {
								return true;
							}
						}
						else {
							return true;
						}
					}
					return false;
				}
				
				private boolean checkPositive(CloudPlugin o1, CloudPlugin dependencyPlugin) {
					for (String dependency: o1.getDependency()) {
						if (!dependency.equals(dependencyPlugin.getClass().getName())) {
							CloudPlugin nextDependencyPlugin = pluginMap.get(dependency);
							if (checkNegative(nextDependencyPlugin, dependencyPlugin)) {
								return true;
							}
						}
						else {
							return true;
						}
					}
					return false;
				}
			});
		
		for (CloudPlugin cloudPlugin: pluginList) {
			cloudPlugin.initialize();
		}
		
		CloudTaskService.singleton().startService();
		
		logger.info("successful in activating " + this.getClass().getSimpleName() + "...");
	}
	@Override
	public void deactivate() {
		Logger logger = Logger.getLogger(this.getClass());

		// メイン処理
		logger.info("stopping " + this.getClass().getSimpleName() + "...");

		logger.info("successful in stopping " + this.getClass().getSimpleName() + "...");
	}

	@Override
	public void destroy() {
		Logger logger = Logger.getLogger(this.getClass());

		// メイン処理
		logger.info("destroying " + this.getClass().getSimpleName() + "...");

		logger.info("successful in destroying " + this.getClass().getSimpleName() + "...");
	}

	@Override
	public String getPluginId() {
		return id;
	}

	@Override
	public void initialize() {
		Logger logger = Logger.getLogger(this.getClass());
		logger.info("initializing " + this.getClass().getSimpleName() + "...");
		
		CloudEndpointImpl.restriction.config(Configuration.class);
		
		logger.info("successful in initializing " + this.getClass().getSimpleName() + "...");
	}
	
	private interface Configuration {
		// クラウド[コンピュート]ビュー : パワーオン - ジョブ(スコープ指定)
		@MethodRestriction.Enable
		String makePowerOnInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : パワーオフ - ジョブ(スコープ指定)
		@MethodRestriction.Enable
		String makePowerOffInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : 再起動- ジョブ(スコープ指定)
		@MethodRestriction.Enable
		String makeRebootInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		// クラウド[コンピュート]ビュー : サスペンド - ジョブ(スコープ指定)
		@MethodRestriction.Enable
		String makeSuspendInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		
		// クラウド[コンピュート]ビュー : スコープ割当ルール
		@MethodRestriction.Enable
		void registAutoAssigneNodePattern(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "AutoAssigneNodePatternEntries") List<AutoAssignNodePatternEntry> patterns) throws CloudManagerException, InvalidUserPass, InvalidRole;
		@MethodRestriction.Enable
		List<AutoAssignNodePatternEntry> getAutoAssigneNodePatterns(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;
		@MethodRestriction.Enable
		void clearAutoAssigneNodePattern(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;
	}
}