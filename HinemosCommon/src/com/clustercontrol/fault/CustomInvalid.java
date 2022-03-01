/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * コマンド監視定義が不正な場合にthrowされる例外
 * @version 4.0
 */
@SuppressWarnings("serial")
public class CustomInvalid extends HinemosInvalid {

	public CustomInvalid() {
		super();
	}

	public CustomInvalid(String messages, Throwable e) {
		super(messages, e);
	}

	public CustomInvalid(String messages) {
		super(messages);
	}

	public CustomInvalid(Throwable e) {
		super(e);
	}

}
