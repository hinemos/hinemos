/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 環境構築ファイルが大きすぎ例外
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraFileTooLarge extends HinemosException {
	private static final long serialVersionUID = -269733107374123972L;

	/**
	 * コンストラクタ
	 */
	public InfraFileTooLarge(String message) {
		super(message);
	}
}
