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
public class FacilityDuplicate extends HinemosException {

	private static final long serialVersionUID = -5612525221540116629L;
	private String m_facilityId = null;

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 */
	public FacilityDuplicate() {
		super();
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public FacilityDuplicate(String messages) {
		super(messages);
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public FacilityDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public FacilityDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getFacilityId() {
		return m_facilityId;
	}

	public void setFacilityId(String facilityId) {
		m_facilityId = facilityId;
	}
}
