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
public class NotifyDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -8622469950942483628L;

	private String m_notifyId = null;

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 */
	public NotifyDuplicate() {
		super();
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public NotifyDuplicate(String messages) {
		super(messages);
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public NotifyDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * FacilityDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NotifyDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getNotifyId() {
		return m_notifyId;
	}

	public void setNotifyId(String notifyId) {
		m_notifyId = notifyId;
	}
}
