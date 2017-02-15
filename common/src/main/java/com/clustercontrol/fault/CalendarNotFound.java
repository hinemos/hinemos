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
 * カレンダが存在しない場合に利用するException
 * @version 3.2.0
 */
public class CalendarNotFound extends HinemosException {


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
