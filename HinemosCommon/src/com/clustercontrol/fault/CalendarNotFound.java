/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * カレンダが存在しない場合に利用するException
 * @version 3.2.0
 */
public class CalendarNotFound extends HinemosNotFound {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3038092388739914913L;
	private String m_calendarId = null;

	/**
	 * CalendarNotFountExceptionコンストラクタ
	 */
	public CalendarNotFound() {
		super();
	}

	/**
	 * CalendarNotFountExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CalendarNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * CalendarNotFountExceptionコンストラクタ
	 * @param messages
	 */
	public CalendarNotFound(String messages) {
		super(messages);
	}

	/**
	 * CalendarNotFountExceptionコンストラクタ
	 * @param e
	 */
	public CalendarNotFound(Throwable e) {
		super(e);
	}

	/**
	 * カレンダIDを返します。
	 * @return カレンダID
	 */
	public String getCalendarId() {
		return m_calendarId;
	}

	/**
	 * カレンダIDを設定します。
	 * @param calendarId カレンダID
	 */
	public void setCalendarId(String calendarId) {
		m_calendarId = calendarId;
	}




}
