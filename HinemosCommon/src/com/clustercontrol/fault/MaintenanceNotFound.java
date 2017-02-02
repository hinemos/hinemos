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
 * メンテナンスが存在しない場合に利用するException
 * @version 3.2.0
 */
public class MaintenanceNotFound extends HinemosException {


	/**
	 *
	 */
	private String m_maintenanceId = null;
	private static final long serialVersionUID = -4838587154677989509L;

	/**
	 * MaintenanceNotFoundExceptionコンストラクタ
	 */
	public MaintenanceNotFound() {
		super();
	}

	/**
	 * MaintenanceNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public MaintenanceNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * MaintenanceNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public MaintenanceNotFound(String messages) {
		super(messages);
	}

	/**
	 * MaintenanceNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public MaintenanceNotFound(Throwable e) {
		super(e);
	}

	/**
	 * メンテナンスIDを返します。
	 * @return メンテナンスID
	 */
	public String getMaintenanceId() {
		return m_maintenanceId;
	}

	/**
	 * メンテナンスIDを設定します。
	 * @param maintenanceId メンテナンスID
	 */
	public void setMaintenanceId(String maintenanceId) {
		m_maintenanceId = maintenanceId;
	}




}
