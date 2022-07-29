/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.session;

import com.clustercontrol.fault.UsedFacility;

/**
 * ファシリティチェックを行うインターフェイスです。
 * <P>
 * SessionBean に実装して使用します。
 * 
 * @version 2.1.2
 * @since 2.1.2
 */
public interface CheckFacility {

	/**
	 * ファシリティIDが使用されているかチェックします。
	 * <P>
	 * 使用されていたら、UsedFacility がスローされる。
	 * 
	 * @param facilityId ファシリティ
	 * @throws UsedFacility
	 */
	public void isUseFacilityId(String facilityId) throws UsedFacility;
}
