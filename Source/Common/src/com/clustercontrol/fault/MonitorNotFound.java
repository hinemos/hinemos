/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * facilityIDが存在しない場合に利用するException
 * @version 3.2.0
 */
public class MonitorNotFound extends HinemosNotFound {
	
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
