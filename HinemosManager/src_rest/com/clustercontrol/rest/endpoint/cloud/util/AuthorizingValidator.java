/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.util;

import java.util.Locale;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class AuthorizingValidator {

	/**
	 * ログインユーザがADMINISTRATORSロールに属していることを確認する。
	 */
	public static void validateAdmin() throws CloudManagerException {
		if (
			!Boolean.TRUE.equals(HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR))
			) {
			throw ErrorCode.NEED_ADMINISTRATORS_ROLE.cloudManagerFault(Session.current().getHinemosCredential().getUserId());
		}
	}

	/**
	 * ファシリティIDが存在することを確認する。
	 */
	public static void validateFacility(String facilityId) throws CloudManagerException {
		FacilityTreeItem treeItem;
		try {
			treeItem = RepositoryControllerBeanWrapper.bean().getFacilityTree(null, Locale.getDefault());
		}
		catch (HinemosUnknown e1) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e1);
		}

		if (serarchFacility(treeItem, facilityId) == null)
			throw ErrorCode.FACILITY_NOT_FOUND.cloudManagerFault(facilityId);
	}

	private static FacilityTreeItem serarchFacility(FacilityTreeItem treeItem, String facilityId) {
		if (!treeItem.getData().isNotReferFlg() && treeItem.getData().getFacilityId().equals(facilityId)) return treeItem;
		
		for (FacilityTreeItem child: treeItem.getChildrenArray()) {
			FacilityTreeItem result = serarchFacility(child, facilityId);
			if (result != null)
				return result;
		}
		return null;
	}

	/**
	 * ロケーションが存在することを確認する。
	 */
	public static void validateLocation(String cloudScopeId, String locationId) throws CloudManagerException {
		CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScopeByCurrentHinemosUser(cloudScopeId);
		scope.getLocation(locationId);
	}

	/**
	 * クラウドアカウントが存在することを確認する。
	 */
	public static void validateLoginUser(String cloudScopeId, String cloudLoginUserId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("findCloudLoginUser_hinemosUser_scope", CloudLoginUserEntity.class);
		query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("loginUserId", cloudLoginUserId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
		query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
		
		try {
			query.getSingleResult();
		}
		catch (NoResultException e) {
			throw ErrorCode.LOGINUSER_NOT_FOUND.cloudManagerFault(cloudScopeId, cloudLoginUserId);
		}
	}

	/**
	 * ログインユーザがADMINISTRATORSロールに属していること、及びクラウドスコープが存在することを確認する。
	 */
	public static void validateScopeAdmin(String cloudScopeId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudScopeEntity> query = em.createNamedQuery("findCloudScopeByHinemosUserAsAdmin", CloudScopeEntity.class);
		query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
		query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
		
		try {
			query.getSingleResult();
		}
		catch (NoResultException e) {
			throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(cloudScopeId);
		}
	}

	/**
	 * 指定したオーナーロールのクラウドスコープが存在すること、及びクラウドスコープ内にロケーションが存在することを確認する。
	 */
	public static void validateScopeLocationRole(String cloudScopeId, String ownerRoleId, String locationId) throws CloudManagerException {
		CloudScopeEntity cloudScope = CloudManager.singleton().getCloudScopes().getCloudScopeByOwnerRole(cloudScopeId, ownerRoleId);
		LocationEntity location = cloudScope.getLocation(locationId);
		if (location == null)
			throw ErrorCode.LOCATION_NOT_FOUND.cloudManagerFault(cloudScope.getId(), locationId);
	}

	/**
	 * クラウドスコープ内にロケーションが存在することを確認する。
	 */
	public static void validateScopeLocation(String cloudScopeId, String locationId) throws CloudManagerException {
		CloudScopeEntity cloudScope = CloudManager.singleton().getCloudScopes().getCloudScopeByCurrentHinemosUser(cloudScopeId);
		LocationEntity location = cloudScope.getLocation(locationId);
		if (location == null)
			throw ErrorCode.LOCATION_NOT_FOUND.cloudManagerFault(cloudScope.getId(), locationId);
	}

	/**
	 * 指定したオーナーロールのクラウドスコープが存在することを確認する。
	 */
	public static void validateScopeRole(String cloudScopeId, String ownerRoleId) throws CloudManagerException {
		CloudManager.singleton().getCloudScopes().getCloudScopeByOwnerRole(cloudScopeId, ownerRoleId);
	}

	/**
	 * クラウドスコープが存在することを確認する。
	 */
	public static void validateScope(String cloudScopeId) throws CloudManagerException {
		CloudManager.singleton().getCloudScopes().getCloudScopeByCurrentHinemosUser(cloudScopeId);
	}

}
