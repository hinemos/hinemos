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
import com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.ModifyCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.RoleRelation;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;

public interface ILoginUsers {
	CloudLoginUserEntity addAccount(AddCloudLoginUserRequest request) throws CloudManagerException;
	CloudLoginUserEntity addUser(AddCloudLoginUserRequest request) throws CloudManagerException;
	CloudLoginUserEntity modifyCloudLoginUser(ModifyCloudLoginUserRequest request) throws CloudManagerException;
	CloudLoginUserEntity modifyRoleRelation(String cloudScopeId, String loginUserId, List<RoleRelation> relations) throws CloudManagerException;
	void removeCloudLoginUser(String cloudScopeId, String loginUserId) throws CloudManagerException;

	CloudLoginUserEntity getCloudLoginUser(String cloudScopeId, String loginUserId) throws CloudManagerException;
	List<CloudLoginUserEntity> getCloudLoginUserByRole(String roleId) throws CloudManagerException;

	List<CloudLoginUserEntity> getAllCloudLoginUsers() throws CloudManagerException;
	List<CloudLoginUserEntity> getCloudLoginUserByCloudScope(String cloudScopeId) throws CloudManagerException;
	List<CloudLoginUserEntity> getCloudLoginUserByCloudScopeAndHinemosUser(String cloudScopeId, String hinemosUserId) throws CloudManagerException;

	CloudLoginUserEntity getPrimaryCloudLoginUserByCurrent(String cloudScopeId) throws CloudManagerException;
	CloudLoginUserEntity getPrimaryCloudLoginUser(String cloudScopeId, String hinemosUserId) throws CloudManagerException;
	List<String> getCloudLoginUserPriority(String cloudScopeId) throws CloudManagerException, InvalidRole;
	void modifyCloudLoginUserPriority(String cloudScopeId, List<String> cloudLoginUserIds) throws CloudManagerException;
}
