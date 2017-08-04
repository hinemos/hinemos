/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

public class OsScopeInitializerPlugin implements HinemosPlugin {
	public static final Log log = LogFactory.getLog(OsScopeInitializerPlugin.class);
	private static Set<String> osScopeIdSet = new HashSet<String>();

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(Log4jReloadPlugin.class.getName());
		dependency.add(CacheInitializerPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		Set<String> builtinScopeFacilityIdSet = FacilityTreeAttributeConstant.getBuiltinScopeFacilityIdSet();
		Map<String, CollectorPlatformMstEntity> platformMap = new ConcurrentHashMap<String, CollectorPlatformMstEntity>();
		Set<String> platformIdSet = new HashSet<String>();
		Set<String> osFacilityIdSet = new HashSet<String>();
		String osParentFacilityId = FacilityTreeAttributeConstant.OS_PARENT_SCOPE;
		try {
			for (CollectorPlatformMstEntity platformMstEntity : QueryUtil.getAllCollectorPlatformMst()) {
				String platformId = platformMstEntity.getPlatformId();
				platformIdSet.add(platformId);
				platformMap.put(platformId, platformMstEntity);
			}

			for (FacilityInfo facilityEntity : QueryUtil.getChildFacilityEntity(osParentFacilityId)) {
				osFacilityIdSet.add(facilityEntity.getFacilityId());
			}
			builtinScopeFacilityIdSet.addAll(osFacilityIdSet);
			osScopeIdSet.addAll(osFacilityIdSet);
		} catch(Exception e) {
			log.error(e);
			return;
		}

		//setting.cc_collector_platform_mstにある、setting.cc_cfg_facilityにない
		//OSをOS別スコープに登録
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//osFacilityIdがplatformIdと同じ
			Set<String> facilityIdToAddSet = new HashSet<String>(platformIdSet);
			facilityIdToAddSet.removeAll(osFacilityIdSet);

			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

			long now = HinemosTime.currentTimeMillis();
			FacilityInfo osParentFacilityEntity = QueryUtil.getFacilityPK_NONE(osParentFacilityId);
			for (String facilityIdToAdd : facilityIdToAddSet) {
				CollectorPlatformMstEntity platformMstEntity = platformMap.get(facilityIdToAdd);

				ScopeInfo facilityEntityToAdd = new ScopeInfo(facilityIdToAdd);

				facilityEntityToAdd.setFacilityName(platformMstEntity.getPlatformName());
				facilityEntityToAdd.setDescription(platformMstEntity.getPlatformName());
				facilityEntityToAdd.setDisplaySortOrder(platformMstEntity.getOrderNo());

				facilityEntityToAdd.setFacilityType(osParentFacilityEntity.getFacilityType());
				facilityEntityToAdd.setIconImage(osParentFacilityEntity.getIconImage());
				facilityEntityToAdd.setValid(osParentFacilityEntity.getValid());
				facilityEntityToAdd.setOwnerRoleId(osParentFacilityEntity.getOwnerRoleId());
				facilityEntityToAdd.setCreateUserId(osParentFacilityEntity.getCreateUserId());
				facilityEntityToAdd.setCreateDatetime(now);
				facilityEntityToAdd.setModifyUserId(osParentFacilityEntity.getModifyUserId());
				facilityEntityToAdd.setModifyDatetime(now);
				
				facilityEntityToAdd.persistSelf(em);
				
				em.flush();

				log.info(String.format("The OS scope %s will be added.", facilityIdToAdd));
				FacilityRelationEntity relation = new FacilityRelationEntity(osParentFacilityId, facilityIdToAdd);
				em.persist(relation);
			}

			jtm.commit();

			if (!facilityIdToAddSet.isEmpty()) {
				builtinScopeFacilityIdSet.addAll(facilityIdToAddSet);
				osScopeIdSet.addAll(facilityIdToAddSet);
			}
		} catch (Exception e) {
			log.error(e);
			if (jtm != null) {
				jtm.rollback();
			}

			return;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		//setting.cc_collector_platform_mstにない、setting.cc_cfg_facilityにある
		//OSをOS別スコープから削除
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			Set<String> facilityIdToRemoveSet = new HashSet<String>(osFacilityIdSet);
			facilityIdToRemoveSet.removeAll(platformIdSet);
			for (String facilityIdToRemove : facilityIdToRemoveSet) {
				FacilityRelationEntity facilityRelationEntityToRemove = QueryUtil.getFacilityRelationPk(osParentFacilityId, facilityIdToRemove);
				em.remove(facilityRelationEntityToRemove);

				FacilityInfo facilityEntityToRemove = QueryUtil.getFacilityPK_NONE(facilityIdToRemove);
				log.info(String.format("The OS scope %s will be removed.", facilityIdToRemove));
				em.remove(facilityEntityToRemove);

			}

			jtm.commit();

			if (!facilityIdToRemoveSet.isEmpty()) {
				builtinScopeFacilityIdSet.removeAll(facilityIdToRemoveSet);
				osScopeIdSet.removeAll(facilityIdToRemoveSet);
			}
		} catch (Exception e) {
			log.error(e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void destroy() {
	}

	public static Set<String> getOsScopeIdSet() {
		return osScopeIdSet;
	}
}
