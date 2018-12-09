/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 環境構築ファイルが存在しない例外
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraFileNotFound extends HinemosException {
	private static final long serialVersionUID = -8657399129165860039L;

	/**
	 * コンストラクタ
	 */
	public InfraFileNotFound(String message) {
		super(message);
	}
}
