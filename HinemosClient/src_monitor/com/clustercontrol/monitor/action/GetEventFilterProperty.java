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
import com.clustercontrol.monitor.util.EventFilterPropertyUtil;

/**
 * イベント情報フィルタ用プロパティを取得するクライアント側アクションクラス<BR>
 * 
 * マネージャにSessionBean経由でアクセスし、イベント情報フィルタ用プロパティを取得します。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetEventFilterProperty {

	/**
	 * イベント情報フィルタ用プロパティを取得します。
	 * 
	 * @return イベント情報フィルタ用プロパティ
	 */
	public Property getProperty() {
		return EventFilterPropertyUtil.getProperty(Locale.getDefault());
	}
}
