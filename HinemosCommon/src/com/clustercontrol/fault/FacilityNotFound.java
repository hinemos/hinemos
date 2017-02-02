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
