/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
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
}
