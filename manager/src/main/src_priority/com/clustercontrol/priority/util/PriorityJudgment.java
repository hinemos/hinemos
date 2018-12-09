/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.priority.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.priority.factory.SelectPriorityJudgment;
import com.clustercontrol.priority.model.PriorityJudgmentInfo;

/**
 * 重要度判定のBeanクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class PriorityJudgment {
	protected static HashMap<String, Integer> m_patternMap = new HashMap<String, Integer>();
	public static final String PATTERN_1 = "1";
	public static final String PATTERN_2 = "2";
	public static final String PATTERN_3 = "3";
	public static final String PATTERN_4 = "4";
	public static final String PATTERN_5 = "5";
	public static final String PATTERN_6 = "6";
	public static final String PATTERN_7 = "7";
	public static final String PATTERN_8 = "8";
	public static final String PATTERN_9 = "9";
	public static final String PATTERN_10 = "10";
	public static final String PATTERN_11 = "11";
	public static final String PATTERN_12 = "12";
	public static final String PATTERN_13 = "13";
	public static final String PATTERN_14 = "14";
	public static final String PATTERN_15 = "15";

	static{
		setPattern();
	}

	/**
	 * 重要度をパターンに当てはめて、判定します。<BR>
	 * 
	 * @param priorityList 重要度の配列(int)
	 * @return
	 */
	public static Integer judgment(ArrayList<Integer> priorityList) {
		Integer priority = null;
		boolean normal = false;
		boolean critical = false;
		boolean warning = false;
		boolean unknown = false;

		for(int i =0; i < priorityList.size(); i++){
			Integer status = priorityList.get(i);

			if(status.intValue() == PriorityConstant.TYPE_INFO){
				normal = true;
			}
			else if(status.intValue() == PriorityConstant.TYPE_WARNING){
				warning = true;
			}
			else if(status.intValue() == PriorityConstant.TYPE_UNKNOWN){
				unknown = true;
			}
			else if(status.intValue() == PriorityConstant.TYPE_CRITICAL){
				critical = true;
			}
		}

		if(normal && !warning && !critical && !unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 1: ○　 | ×　 | ×　 | ×
			priority = m_patternMap.get(PATTERN_1);
		}
		else if(normal && !warning && !critical && unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 2: ○　 | ×　 | ×　 | ○
			priority = m_patternMap.get(PATTERN_2);
		}
		else if(normal && warning && !critical && !unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 3: ○　 | ○　 | ×　 | ×
			priority = m_patternMap.get(PATTERN_3);
		}
		else if(normal && warning && !critical && unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 4: ○　 | ○　 | ×　 | ○
			priority = m_patternMap.get(PATTERN_4);
		}
		else if(!normal && warning && !critical && !unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 5: ×　 | ○　 | ×　 | ×
			priority = m_patternMap.get(PATTERN_5);
		}
		else if(!normal && warning && !critical && unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 6: ×　 | ○　 | ×　 | ○
			priority = m_patternMap.get(PATTERN_6);
		}
		else if(normal && !warning && critical && !unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 7: ○　 | ×　 | ○　 | ×
			priority = m_patternMap.get(PATTERN_7);
		}
		else if(normal && !warning && critical && unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 8: ○　 | ×　 | ○　 | ○
			priority = m_patternMap.get(PATTERN_8);
		}
		else if(normal && warning && critical && !unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン 9: ○　 | ○　 | ○　 | ×
			priority = m_patternMap.get(PATTERN_9);
		}
		else if(normal && warning && critical && unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン10: ○　 | ○　 | ○　 | ○
			priority = m_patternMap.get(PATTERN_10);
		}
		else if(!normal && warning && critical && !unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン11: ×　 | ○　 | ○　 | ×
			priority = m_patternMap.get(PATTERN_11);
		}
		else if(!normal && warning && critical && unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン12: ×　 | ○　 | ○　 | ○
			priority = m_patternMap.get(PATTERN_12);
		}
		else if(!normal && !warning && critical && !unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン13: ×　 | ×　 | ○　 | ×
			priority = m_patternMap.get(PATTERN_13);
		}
		else if(!normal && !warning && critical && unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン14: ×　 | ×　 | ○　 | ○
			priority = m_patternMap.get(PATTERN_14);
		}
		else if(!normal && !warning && !critical && unknown){
			// 　　　　    情報 | 警告 | 危険 | 不明
			// パターン15: ×　 | ×　 | ×　 | ○
			priority = m_patternMap.get(PATTERN_15);
		}
		else{
			return null;
		}

		return priority;
	}

	/**
	 * パターンを設定します。<BR>
	 * 
	 *
	 */
	private static void setPattern() {
		// 重要度のパターンをハッシュに保持
		//
		// 情報＜警告＜不明＜危険
		//
		// 　　　　    情報 | 警告 | 危険 | 不明
		// パターン 1: ○　 | ×　 | ×　 | ×
		// パターン 2: ○　 | ×　 | ×　 | ○
		// パターン 3: ○　 | ○　 | ×　 | ×
		// パターン 4: ○　 | ○　 | ×　 | ○
		// パターン 5: ×　 | ○　 | ×　 | ×
		// パターン 6: ×　 | ○　 | ×　 | ○
		// パターン 7: ○　 | ×　 | ○　 | ×
		// パターン 8: ○　 | ×　 | ○　 | ○
		// パターン 9: ○　 | ○　 | ○　 | ×
		// パターン10: ○　 | ○　 | ○　 | ○
		// パターン11: ×　 | ○　 | ○　 | ×
		// パターン12: ×　 | ○　 | ○　 | ○
		// パターン13: ×　 | ×　 | ○　 | ×
		// パターン14: ×　 | ×　 | ○　 | ○
		// パターン15: ×　 | ×　 | ×　 | ○

		SelectPriorityJudgment select = new SelectPriorityJudgment();
		PriorityJudgmentInfo info = null;
		try {
			info = select.getPriorityJudgment("DEFAULT");
		} catch (MonitorNotFound e) {
			// 何もしない
		}

		if(info != null){
			m_patternMap.put(PATTERN_1, info.getPattern01());
			m_patternMap.put(PATTERN_2, info.getPattern02());
			m_patternMap.put(PATTERN_3, info.getPattern03());
			m_patternMap.put(PATTERN_4, info.getPattern04());
			m_patternMap.put(PATTERN_5, info.getPattern05());
			m_patternMap.put(PATTERN_6, info.getPattern06());
			m_patternMap.put(PATTERN_7, info.getPattern07());
			m_patternMap.put(PATTERN_8, info.getPattern08());
			m_patternMap.put(PATTERN_9, info.getPattern09());
			m_patternMap.put(PATTERN_10, info.getPattern10());
			m_patternMap.put(PATTERN_11, info.getPattern11());
			m_patternMap.put(PATTERN_12, info.getPattern12());
			m_patternMap.put(PATTERN_13, info.getPattern13());
			m_patternMap.put(PATTERN_14, info.getPattern14());
			m_patternMap.put(PATTERN_15, info.getPattern15());
		}
		else{
			m_patternMap.put(PATTERN_1, PriorityConstant.TYPE_INFO);
			m_patternMap.put(PATTERN_2, PriorityConstant.TYPE_UNKNOWN);
			m_patternMap.put(PATTERN_3, PriorityConstant.TYPE_WARNING);
			m_patternMap.put(PATTERN_4, PriorityConstant.TYPE_UNKNOWN);
			m_patternMap.put(PATTERN_5, PriorityConstant.TYPE_WARNING);
			m_patternMap.put(PATTERN_6, PriorityConstant.TYPE_UNKNOWN);
			m_patternMap.put(PATTERN_7, PriorityConstant.TYPE_CRITICAL);
			m_patternMap.put(PATTERN_8, PriorityConstant.TYPE_CRITICAL);
			m_patternMap.put(PATTERN_9, PriorityConstant.TYPE_CRITICAL);
			m_patternMap.put(PATTERN_10, PriorityConstant.TYPE_CRITICAL);
			m_patternMap.put(PATTERN_11, PriorityConstant.TYPE_CRITICAL);
			m_patternMap.put(PATTERN_12, PriorityConstant.TYPE_CRITICAL);
			m_patternMap.put(PATTERN_13, PriorityConstant.TYPE_CRITICAL);
			m_patternMap.put(PATTERN_14, PriorityConstant.TYPE_CRITICAL);
			m_patternMap.put(PATTERN_15, PriorityConstant.TYPE_UNKNOWN);
		}
	}
}
