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
