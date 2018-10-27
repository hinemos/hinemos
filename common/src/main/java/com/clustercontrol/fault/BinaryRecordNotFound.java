/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * バイナリ収集蓄積データのが存在しない例外
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryRecordNotFound extends HinemosException {
	private static final long serialVersionUID = -8657399129165860039L;

	/**
	 * コンストラクタ
	 */
	public BinaryRecordNotFound(String message) {
		super(message);
	}
}
