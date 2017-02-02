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

package com.clustercontrol.priority.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.priority.model.PriorityJudgmentInfo;
import com.clustercontrol.priority.util.QueryUtil;

/**
 * 重要度判定を検索するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class SelectPriorityJudgment {

	/**
	 * 重要度判定情報を取得します。<BR>
	 * 
	 * @return
	 * @throws MonitorNotFound
	 */
	public PriorityJudgmentInfo getPriorityJudgment(String judgmentId) throws MonitorNotFound {
		//重要度判定情報を検索し取得
		return QueryUtil.getPriorityInfoPK(judgmentId);
	}
}
