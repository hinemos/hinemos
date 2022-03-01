/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * facilityIDが重複している場合に利用するException
 * @version 3.2.0
 */
public class MonitorDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -2752389198258965994L;

	private String m_monitorId = null;

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 */
	public MonitorDuplicate() {
		super();
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public MonitorDuplicate(String messages) {
		super(messages);
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public MonitorDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public MonitorDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getMonitorId() {
		return m_monitorId;
	}

	public void setFfacilityId(String monitorId) {
		m_monitorId = monitorId;
	}
}
