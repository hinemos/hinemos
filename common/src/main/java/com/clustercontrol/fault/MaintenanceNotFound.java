/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
