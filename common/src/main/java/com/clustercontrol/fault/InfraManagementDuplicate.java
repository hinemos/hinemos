/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 環境構築機能情報が存在しない場合に利用するException
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementDuplicate extends HinemosException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String managementId;
	
	/**
	 * InfraManagementInfoNotFound コンストラクタ
	 */
	public InfraManagementDuplicate() {
		super();
	}

	/**
	 * InfraManagementInfoNotFound コンストラクタ
	 * @param messages
	 */
	public InfraManagementDuplicate(String messages) {
		super(messages);
	}

	/**
	 * InfraManagementInfoNotFound コンストラクタ
	 * @param e
	 */
	public InfraManagementDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * InfraManagementInfoNotFound コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InfraManagementDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}
}
