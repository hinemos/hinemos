/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository.factory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.util.QueryUtil;


/**
 * スコープ用プロパティを作成するクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScopeProperty {

	/**
	 * スコープ用プロパティを返します。
	 * 
	 * @param facilityId
	 * @param mode
	 * @return スコープ用プロパティ
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static ScopeInfo getProperty(String facilityId) throws FacilityNotFound, InvalidRole {

		ScopeInfo property = new ScopeInfo();

		if(facilityId != null && facilityId.compareTo("") != 0){

			//Facility取得
			FacilityInfo facility = QueryUtil.getFacilityPK(facilityId);

			//ファシリティID
			property.setFacilityId(facility.getFacilityId());
			//ファシリティ名
			property.setFacilityName(facility.getFacilityName());
			//説明
			if(facility.getDescription() != null && facility.getDescription().compareTo("") != 0){
				property.setDescription(facility.getDescription());
			}
			//アイコン
			property.setIconImage(facility.getIconImage());
			//オーナーロールID
			property.setOwnerRoleId(facility.getOwnerRoleId());
		}

		return property;
	}

	/**
	 * オブジェクト権限のチェックなしでスコープ用プロパティを返します。
	 * 
	 * @param facilityId
	 * @param mode
	 * @return スコープ用プロパティ
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static ScopeInfo getProperty_NONE(String facilityId) throws FacilityNotFound, InvalidRole {

		ScopeInfo property = new ScopeInfo();

		if(facilityId != null && facilityId.compareTo("") != 0){

			//Facility取得
			FacilityInfo facility = QueryUtil.getFacilityPK_NONE(facilityId);

			//ファシリティID
			property.setFacilityId(facility.getFacilityId());
			//ファシリティ名
			property.setFacilityName(facility.getFacilityName());
			//説明
			if(facility.getDescription() != null && facility.getDescription().compareTo("") != 0){
				property.setDescription(facility.getDescription());
			}
			//アイコン
			property.setIconImage(facility.getIconImage());
			//オーナーロールID
			property.setOwnerRoleId(facility.getOwnerRoleId());
		}

		return property;
	}

}
