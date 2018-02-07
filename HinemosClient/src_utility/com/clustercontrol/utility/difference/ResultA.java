/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.util.HashMap;
import java.util.Map;

/**
 * 全比較結果。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class ResultA {
	/**
	 * 機能毎比較結果。
	 */
	private Map<String, ResultB> resultBs;
	
	public ResultA() {
	}

	public void setResultBs(Map<String, ResultB> resultBs) {
		this.resultBs = resultBs;
	}

	public Map<String, ResultB> getResultBs() {
		if (resultBs == null) {
			resultBs = new HashMap<String, ResultB>();
		}
		
		return resultBs;
	}
}
