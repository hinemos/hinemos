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
 * facilityIDが重複している場合に利用するException
 * @version 3.2.0
 */
public class NotifyDuplicate extends HinemosException {

	private static final long serialVersionUID = -8622469950942483628L;

	private String m_notifyId = null;

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 */
	public NotifyDuplicate() {
		super();
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public NotifyDuplicate(String messages) {
		super(messages);
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public NotifyDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NotifyDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getNotifyId() {
		return m_notifyId;
	}

	public void setNotifyId(String notifyId) {
		m_notifyId = notifyId;
	}
}
