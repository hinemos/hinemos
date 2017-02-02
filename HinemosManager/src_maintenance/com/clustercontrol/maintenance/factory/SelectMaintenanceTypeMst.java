/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.maintenance.model.MaintenanceTypeMst;
import com.clustercontrol.maintenance.util.QueryUtil;

/**
 * 
 * メンテナンス種別に関する情報を検索するためのクラスです。
 * 
 * @since	2.2.0
 * @version	2.2.0
 *
 */
public class SelectMaintenanceTypeMst {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectMaintenanceTypeMst.class );

	/**
	 * メンテナンス種別一覧を取得する
	 * @return
	 */
	public ArrayList<MaintenanceTypeMst> getMaintenanceTypeList() {
		m_log.debug("getMaintenanceTypeList() : start");

		// メンテナンス種別マスタ一覧を取得
		List<MaintenanceTypeMst> ct = QueryUtil.getAllMaintenanceTypeMst();

		return new ArrayList<>(ct);
	}
}
