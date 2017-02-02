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
 * maintenanceIDが重複している場合に利用するException
 * @version 3.2.0
 */
public class MaintenanceDuplicate extends HinemosException {

	private static final long serialVersionUID = -5612525221540116629L;
	private String m_maintenanceId = null;

	/**
	 * MaintenanceDuplicateコンストラクタ
	 */
	public MaintenanceDuplicate() {
		super();
	}

	/**
	 * MaintenanceDuplicateコンストラクタ
	 * @param messages
	 */
	public MaintenanceDuplicate(String messages) {
		super(messages);
	}

	/**
	 * MaintenanceDuplicateコンストラクタ
	 * @param e
	 */
	public MaintenanceDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * MaintenanceDuplicateコンストラクタ
	 * @param messages
	 * @param e
	 */
	public MaintenanceDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getMaintenanceId() {
		return m_maintenanceId;
	}

	public void setMaintenanceId(String maintenanceId) {
		m_maintenanceId = maintenanceId;
	}
}
