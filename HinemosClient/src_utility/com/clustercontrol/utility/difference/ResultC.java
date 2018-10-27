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
 * 項目毎比較結果。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class ResultC {
	public enum ResultType {
		only1,
		only2,
		diff,
		equal
	}
	
	/**
	 * 項目 ID。
	 */
	private final String id;

	/**
	 * 差分種別。
	 */
	private ResultType resultType;


	/**
	 * 属性毎比較結果。
	 */
	private List<ResultD> resultDs;
	
	public ResultC(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ResultType getResultType() {
		return resultType;
	}
	
	public void setResultType(ResultType resultType) {
		this.resultType = resultType;
	}
	
	public List<ResultD> getResultDs() {
		if (resultDs == null) {
			resultDs = new ArrayList<ResultD>();
		}
		
		return resultDs;
	}
}