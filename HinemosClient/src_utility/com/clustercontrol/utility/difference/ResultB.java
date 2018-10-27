/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.util.ArrayList;
import java.util.List;

/**
 * 機能毎比較結果。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class ResultB {
	/**
	 * 機能名。
	 */
	private final String funcName;
	
	private List<ResultC> resultCs;
	
	public ResultB(String funcName) {
		this.funcName = funcName;
	}

	public String getFuncName() {
		return funcName;
	}

	public List<ResultC> getResultCs() {
		if (resultCs == null) {
			resultCs = new ArrayList<ResultC>();
		}
		
		return resultCs;
	}
}
