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

package com.clustercontrol.monitor.action;

import java.util.Locale;

import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.util.StatusFilterPropertyUtil;

/**
 * ステータス情報フィルタ用プロパティを取得するクライアント側アクションクラス<BR>
 * 
 * マネージャにSessionBean経由でアクセスし、ステータス情報フィルタ用プロパティを取得します。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetStatusFilterProperty {

	/**
	 * マネージャにSessionBean経由でアクセスし、ステータス情報フィルタ用プロパティを取得します。
	 * 
	 * @return ステータス情報フィルタ用プロパティ
	 * 
	 * @see com.clustercontrol.monitor.ejb.session.MonitorController
	 * @see com.clustercontrol.monitor.ejb.session.MonitorControllerBean#getStatusFilterProperty(Locale)
	 */
	public Property getProperty() {
		return StatusFilterPropertyUtil.getProperty(Locale.getDefault());
	}
}
