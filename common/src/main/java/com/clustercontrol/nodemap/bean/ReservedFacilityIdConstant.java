/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.bean;

/**
 * 予約されているファシリティID
 * @since 1.0.0
 */
public class ReservedFacilityIdConstant {
	// ルートファシリティID
	public static final String ROOT_SCOPE="_ROOT_";
	public static final String ROOT_SCOPE_TEXT="Root";

	public static final String REGISTEREFD_SCOPE="REGISTERED";
	public static final String REGISTERED_SCOPE_TEXT="Registered_Nodes";

	public static final String UNREGISTEREFD_SCOPE="UNREGISTERED";
	public static final String UNREGISTERED_SCOPE_TEXT="Unregistered_Node";

	public static final String INTERNAL_SCOPE="INTERNAL";
	public static final String INTERNAL_SCOPE_TEXT="Hinemos_Internal";

	public static final String[] reservedIds = {
		REGISTEREFD_SCOPE,
		UNREGISTEREFD_SCOPE,
		INTERNAL_SCOPE,
		ROOT_SCOPE
	};

	/**
	 * 指定のファシリティIDがシステム予約IDか否かを調べます
	 * @param id ファシリティID
	 * @return システム予約IDの場合はtrueを返す
	 */
	public static boolean isMember(String facilityId){
		for(int i=0; i<reservedIds.length; i++){
			if(facilityId.equals(reservedIds[i])){
				return true;
			}
		}
		return false;
	}

	private ReservedFacilityIdConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
