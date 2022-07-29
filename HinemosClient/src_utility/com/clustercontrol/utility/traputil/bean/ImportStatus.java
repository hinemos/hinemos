/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.bean;

import com.clustercontrol.util.Messages;

/**
 * SnmpTrapユーティリティ インポート実行ステータス用enum<BR>
 * 
 * @version 6.1.0
 * @since 2.4.0
 */
public enum ImportStatus {
	
	
	TYPE_WAIT(Messages.getString("traputil.status.wait")),
	TYPE_REGISTERED(Messages.getString("traputil.status.registered")),
	TYPE_RUNNING(Messages.getString("traputil.status.running")),
	TYPE_SUCCESS(Messages.getString("traputil.status.success")),
	TYPE_ERROR(Messages.getString("traputil.status.error"));
	
	//表記用文字列
	private String name ;
	
	//コンストラクタ
	private ImportStatus(String name){
		this.name = name;
	}
	
	//toString()のオーバーライド
	@Override
	public String toString() {
		return this.name;
	}
}