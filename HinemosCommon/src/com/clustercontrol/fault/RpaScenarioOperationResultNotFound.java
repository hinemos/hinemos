/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPAシナリオ実績が存在しない場合に利用するException
 * @version 7.0.0
 */
public class RpaScenarioOperationResultNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -9013422102069465300L;

	/**
	 * RpaScenarioOperationResultNotFoundExceptionコンストラクタ
	 */
	public RpaScenarioOperationResultNotFound() {
		super();
	}

	/**
	 * RpaScenarioOperationResultNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioOperationResultNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * RpaScenarioOperationResultNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public RpaScenarioOperationResultNotFound(String messages) {
		super(messages);
	}

	/**
	 * RpaScenarioOperationResultNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public RpaScenarioOperationResultNotFound(Throwable e) {
		super(e);
	}

}
