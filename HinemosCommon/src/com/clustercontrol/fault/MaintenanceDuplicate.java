/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
