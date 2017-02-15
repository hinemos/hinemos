/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.fault;

/**
 * calendarIDが重複している場合に利用するException
 * @version 3.2.0
 */
public class CalendarDuplicate extends HinemosException {

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
