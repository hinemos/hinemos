/*

Copyright (C) 2011 NTT DATA Corporation

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
 * カレンダ定義が不正な場合にthrowされる例外
 * @version 4.0
 */
@SuppressWarnings("serial")
public class CalendarInvalid extends HinemosException {

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
