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
public class MonitorNotFound extends HinemosException {
	
	private static final long serialVersionUID = 6496146742364458888L;

	private String m_monitorId = null;

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 */
	public MonitorNotFound() {
		super();
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public MonitorNotFound(String messages) {
		super(messages);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public MonitorNotFound(Throwable e) {
		super(e);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public MonitorNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getMonitorId() {
		return m_monitorId;
	}

	public void setFacilityId(String monitorId) {
		m_monitorId = monitorId;
	}

}
