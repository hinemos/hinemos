/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RpaScenarioCoefficientPatternが存在しない場合に利用するException
 */
public class RpaScenarioCoefficientPatternNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 2387815483607910087L;
	private String rpaToolEnvId = null;
	private String orderNo = null;

	/**
	 * RpaScenarioCoefficientPatternNotFoundExceptionコンストラクタ
	 */
	public RpaScenarioCoefficientPatternNotFound() {
		super();
	}

	/**
	 * RpaScenarioCoefficientPatternNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioCoefficientPatternNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * RpaScenarioCoefficientPatternNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public RpaScenarioCoefficientPatternNotFound(String messages) {
		super(messages);
	}

	/**
	 * RpaScenarioCoefficientPatternNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public RpaScenarioCoefficientPatternNotFound(Throwable e) {
		super(e);
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
