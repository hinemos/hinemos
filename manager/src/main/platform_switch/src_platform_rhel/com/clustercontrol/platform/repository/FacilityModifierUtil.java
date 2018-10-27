/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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