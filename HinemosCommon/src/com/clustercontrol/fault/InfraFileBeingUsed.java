/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 環境構築ファイルが利用中の例外
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraFileBeingUsed extends HinemosUsed {
	
	private static final long serialVersionUID = -4207846085775579766L;

	/**
	 * コンストラクタ
	 */
	public InfraFileBeingUsed() {
		super();
	}
	/**
	 * コンストラクタ
	 */
	public InfraFileBeingUsed(String message) {
		super(message);
	}
}