/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * calendarIDが重複している場合に利用するException
 * @version 3.2.0
 */
public class CalendarDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -5612525221540116629L;
	private String m_calendarId = null;

	/**
	 * CalendarDuplicateExceptionコンストラクタ
	 */
	public CalendarDuplicate() {
		super();
	}

	/**
	 * CalendarDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public CalendarDuplicate(String messages) {
		super(messages);
	}

	/**
	 * CalendarDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public CalendarDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * CalendarDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CalendarDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getCalendarId() {
		return m_calendarId;
	}

	public void setCalendarId(String calendarId) {
		m_calendarId = calendarId;
	}
}
