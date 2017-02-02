/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.action;

import java.util.Locale;

import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.util.JobKickFilterPropertyUtil;

/**
 * ジョブ実行契機情報フィルタ用プロパティを取得するクライアント側アクションクラス<BR>
 * 
 * @version 5.1.0
 */
public class GetJobKickFilterProperty {

	/**
	 * ジョブ実行契機情報フィルタ用プロパティを取得します。
	 * 
	 * @return ジョブ実行契機情報フィルタ用プロパティ
	 */
	public Property getProperty() {
		return JobKickFilterPropertyUtil.getProperty(Locale.getDefault());
	}
}
