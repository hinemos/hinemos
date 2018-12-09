/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.List;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.AddPrivateCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AddPublicCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntry;
import com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest;
import com.clustercontrol.xcloud.bean.ModifyCloudScopeRequest;
import com.clustercontrol.xcloud.bean.PlatformServiceCondition;
import com.clustercontrol.xcloud.model.AutoAssignNodePatternEntryEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.PrivateCloudScopeEntity;
import com.clustercontrol.xcloud.model.PublicCloudScopeEntity;

public interface ICloudScopes {
	PublicCloudScopeEntity addPublicCloudScope(AddPublicCloudScopeRequest request) throws CloudManagerException, InvalidRole;
	PrivateCloudScopeEntity addPrivateCloudScope(AddPrivateCloudScopeRequest request) throws CloudManagerException, InvalidRole;
	void removeCloudScope(String cloudScopeId) throws CloudManagerException, InvalidRole;
	
	CloudScopeEntity getCloudScope(String cloudScopeId) throws CloudManagerException;
	List<CloudScopeEntity> getAllCloudScopes() throws CloudManagerException;
	
	CloudScopeEntity modifyCloudScope(ModifyCloudScopeRequest request) throws CloudManagerException, InvalidRole;
	
	CloudScopeEntity getCloudScopeByHinemosUser(String cloudScopeId, String hinemosUserId) throws CloudManagerException;
	CloudScopeEntity getCloudScopeByOwnerRole(String cloudScopeId, String ownerRoleId) throws CloudManagerException;
	List<CloudScopeEntity> getCloudScopesByHinemosUser(String hinemosUserId) throws CloudManagerException;
	CloudScopeEntity getCloudScopeByCurrentHinemosUser(String cloudScopeId) throws CloudManagerException;
	List<CloudScopeEntity> getCloudScopesByCurrentHinemosUser() throws CloudManagerException;
	List<CloudScopeEntity> getCloudScopesByOwnerRole(String ownerRoleId) throws CloudManagerException;
	
	List<PlatformServiceCondition> getPlatformServiceConditions(String cloudScopeId) throws CloudManagerException;
	List<PlatformServiceCondition> getPlatformServiceConditions(String cloudScopeId, String locationId) throws CloudManagerException;
	
	List<AutoAssignNodePatternEntryEntity> registAutoAssigneNodePattern(String cloudScopeId, List<AutoAssignNodePatternEntry> patterns) throws CloudManagerException;
	List<AutoAssignNodePatternEntryEntity> getAutoAssigneNodePatterns(String cloudScopeId) throws CloudManagerException;
	void clearAutoAssigneNodePattern(String cloudScopeId) throws CloudManagerException;
	
	void modifyBillingSetting(ModifyBillingSettingRequest request) throws CloudManagerException, InvalidRole;
}
