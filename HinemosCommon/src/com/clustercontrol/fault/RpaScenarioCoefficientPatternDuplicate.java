/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RpaScenarioCoefficientPatternが重複している場合に利用するException
 */
public class RpaScenarioCoefficientPatternDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = 2427308671054127843L;
	private String rpaToolEnvId = null;
	private String orderNo = null;

	/**
	 * RpaScenarioCoefficientPatternDuplicateコンストラクタ
	 */
	public RpaScenarioCoefficientPatternDuplicate() {
		super();
	}

	/**
	 * RpaScenarioCoefficientPatternDuplicateコンストラクタ
	 * @param messages
	 */
	public RpaScenarioCoefficientPatternDuplicate(String messages) {
		super(messages);
	}

	/**
	 * RpaScenarioCoefficientPatternDuplicateコンストラクタ
	 * @param e
	 */
	public RpaScenarioCoefficientPatternDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * RpaScenarioCoefficientPatternDuplicateコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioCoefficientPatternDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getRpaToolEnvId() {
		return rpaToolEnvId;
	}

	public void setRpaToolEnvId(String rpaToolEnvId) {
		this.rpaToolEnvId = rpaToolEnvId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

}
