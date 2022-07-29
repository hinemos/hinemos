/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPAシナリオ実績詳細が存在しない場合に利用するException
 * @version 7.0.0
 */
public class RpaScenarioOperationResultDetailNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 7628981191161171661L;

	/**
	 * RpaScenarioOperationResultDetailNotFoundExceptionコンストラクタ
	 */
	public RpaScenarioOperationResultDetailNotFound() {
		super();
	}

	/**
	 * RpaScenarioOperationResultDetailNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioOperationResultDetailNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * RpaScenarioOperationResultDetailNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public RpaScenarioOperationResultDetailNotFound(String messages) {
		super(messages);
	}

	/**
	 * RpaScenarioOperationResultDetailNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public RpaScenarioOperationResultDetailNotFound(Throwable e) {
		super(e);
	}

}
