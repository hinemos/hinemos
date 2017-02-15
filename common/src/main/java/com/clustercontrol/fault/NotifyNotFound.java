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
 * facilityIDが存在しない場合に利用するException
 * @version 3.2.0
 */
public class NotifyNotFound extends HinemosException {

	private static final long serialVersionUID = -5140106939019160410L;

	private String m_notifyId = null;

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 */
	public NotifyNotFound() {
		super();
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public NotifyNotFound(String messages) {
		super(messages);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public NotifyNotFound(Throwable e) {
		super(e);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NotifyNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getNotifyId() {
		return m_notifyId;
	}

	public void setNotifyId(String notifyId) {
		m_notifyId = notifyId;
	}

}
