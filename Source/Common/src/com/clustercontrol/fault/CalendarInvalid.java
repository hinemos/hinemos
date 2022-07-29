/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * カレンダ定義が不正な場合にthrowされる例外
 * @version 4.0
 */
@SuppressWarnings("serial")
public class CalendarInvalid extends HinemosInvalid {

	public CalendarInvalid() {
		super();
	}

	public CalendarInvalid(String messages) {
		super(messages);
	}

	public CalendarInvalid(Throwable e) {
		super(e);
	}

	public CalendarInvalid(String messages, Throwable e) {
		super(messages, e);
	}
}
