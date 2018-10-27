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
public class NotifyNotFound extends HinemosException {

	private static final long serialVersionUID = -5140106939019160410L;

	private String m_notifyId = null;

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 */
	public NotifyNotFound() {
		super();
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public NotifyNotFound(String messages) {
		super(messages);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public NotifyNotFound(Throwable e) {
		super(e);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NotifyNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getNotifyId() {
		return m_notifyId;
	}

	public void setNotifyId(String notifyId) {
		m_notifyId = notifyId;
	}

}
