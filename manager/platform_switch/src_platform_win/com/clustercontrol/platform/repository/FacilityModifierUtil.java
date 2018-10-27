/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.repository.util.QueryUtil;

/**
 * FacilityModifierクラスの環境差分（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class FacilityModifierUtil {
	
	private static Log logger = LogFactory.getLog( FacilityModifierUtil.class );
	
	/**
	 * 親のFacilityとのリレーションを削除する
	 * 次のメソッドで呼ばれる
	 * deleteNode
	 * deleteScope
	 * 
	 * @param facilityId
	 * @throws InvalidRole 
	 * @throws FacilityNotFound 
	 */
	public static void deleteFacilityRelation(String facilityId) throws FacilityNotFound {
		logger.debug("Windows : Delete facilities relation");
		List<FacilityInfo> parentFacilities = FacilityTreeCache.getParentFacilityInfo(facilityId);
		
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		
		if (parentFacilities != null) {
			for (FacilityInfo parentFacility : parentFacilities) {
				//最上位スコープ（_ROOT_）とのリレーションはないので削除不要
				if (!parentFacility.getFacilityId().equals(FacilityIdConstant.ROOT)) {
					
					/** メイン処理 */
					logger.debug("releasing nodes from a scope...");

					// 該当するファシリティインスタンスを取得し、存在するファシリティかチェックする
					FacilityInfo facility = QueryUtil.getFacilityPK_NONE(parentFacility.getFacilityId());
					if (!FacilityUtil.isScope(facility)) {
						FacilityNotFound e = new FacilityNotFound(
								"parent's facility is not a scope. (parentFacilityId = " + 
								parentFacility.getFacilityId() + ")");
						logger.info("deleteFacilityRelation() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
					FacilityRelationEntity relation = QueryUtil.getFacilityRelationPk(parentFacility.getFacilityId(), facilityId);
					em.remove(relation);
					logger.info("deleteFacilityRelation() successful in releaseing a node. (parentFacilityId = " + parentFacility.getFacilityId() + ", facilityId = " + facilityId + ")");
				}
				logger.info("deleteFacilityRelation() successful in releasing nodes from a scope.");
			}
		}
	}
}