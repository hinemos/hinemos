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

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.priority.util.PriorityJudgment;

/**
 * ジョブの終了状態を判定するクラスです。
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class EndJudgment extends PriorityJudgment {

	/**
	 * 終了状態を判定パターンに当てはめて判定します。
	 * 
	 * @param statusList 終了状態の配列
	 * @return 終了状態
	 */
	public static Integer judgment(ArrayList<Integer> statusList) {
		Integer priority = null;
		Integer endStatus = null;
		boolean normal = false;
		boolean abnormal = false;
		boolean warning = false;
		boolean unknown = false;

		for(Integer status : statusList) {
			if(status == EndStatusConstant.TYPE_NORMAL){
				normal = true;
			}else if(status == EndStatusConstant.TYPE_WARNING){
				warning = true;
			}else if(status == EndStatusConstant.TYPE_ABNORMAL){
				abnormal = true;
			}
		}

		// 重要度を算出
		if(normal && !warning && !abnormal && !unknown){
			// 　　　　    正常 | 警告 | 異常 | 不明
			// パターン 1: ○　 | ×　 | ×　 | ×
			priority = m_patternMap.get(PATTERN_1);
		}else if(normal && warning && !abnormal && !unknown){
			// 　　　　    正常 | 警告 | 異常 | 不明
			// パターン 3: ○　 | ○　 | ×　 | ×
			priority = m_patternMap.get(PATTERN_3);
		}else if(!normal && warning && !abnormal && !unknown){
			// 　　　　    正常 | 警告 | 異常 | 不明
			// パターン 5: ×　 | ○　 | ×　 | ×
			priority = m_patternMap.get(PATTERN_5);
		}else if(normal && !warning && abnormal && !unknown){
			// 　　　　    正常 | 警告 | 異常 | 不明
			// パターン 7: ○　 | ×　 | ○　 | ×
			priority = m_patternMap.get(PATTERN_7);
		}else if(normal && warning && abnormal && !unknown){
			// 　　　　    正常 | 警告 | 異常 | 不明
			// パターン 9: ○　 | ○　 | ○　 | ×
			priority = m_patternMap.get(PATTERN_9);
		}else if(!normal && warning && abnormal && !unknown){
			// 　　　　    正常 | 警告 | 異常 | 不明
			// パターン11: ×　 | ○　 | ○　 | ×
			priority = m_patternMap.get(PATTERN_11);
		}else if(!normal && !warning && abnormal && !unknown){
			// 　　　　    正常 | 警告 | 異常 | 不明
			// パターン13: ×　 | ×　 | ○　 | ×
			priority = m_patternMap.get(PATTERN_13);
		}else{
			return null;
		}

		// 重要度から終了状態に変換
		if(priority == PriorityConstant.TYPE_INFO){
			endStatus = EndStatusConstant.TYPE_NORMAL;
		}else if(priority == PriorityConstant.TYPE_WARNING){
			endStatus = EndStatusConstant.TYPE_WARNING;
		}else if(priority == PriorityConstant.TYPE_CRITICAL){
			endStatus = EndStatusConstant.TYPE_ABNORMAL;
		}
		return endStatus;
	}

}
