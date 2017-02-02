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
