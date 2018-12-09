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
public class FacilityNotFound extends HinemosException {

	private static final long serialVersionUID = -2799194595006299333L;
	private String m_facilityId = null;

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 */
	public FacilityNotFound() {
		super();
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public FacilityNotFound(String messages) {
		super(messages);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public FacilityNotFound(Throwable e) {
		super(e);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public FacilityNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getFacilityId() {
		return m_facilityId;
	}

	public void setFacilityId(String facilityId) {
		m_facilityId = facilityId;
	}

}
