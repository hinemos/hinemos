/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform.repository;

/**
 * FacilityModifierクラスの環境差分（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class FacilityModifierUtil {
	
	/**
	 * 親のFacilityとのリレーションを削除する
	 * 次のメソッドで呼ばれる
	 * deleteNode
	 * deleteScope
	 * 
	 * @param facilityId
	 */
	public static void deleteFacilityRelation(String facilityId){
		//Linux版はPostgreSQLにて、リレーションを解決するためなにもしない
	}
}