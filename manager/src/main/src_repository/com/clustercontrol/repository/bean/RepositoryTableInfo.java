/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * リポジトリのpropertyクラスを生成するためのPOJOクラス。
 * OSのIDと名前をペアにして送受信するために利用する。
 *
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class RepositoryTableInfo implements Serializable{

	private static final long serialVersionUID = 431487728084790498L;
	private String id;
	private String name;

	public RepositoryTableInfo() {}

	public RepositoryTableInfo(String id, String name) {
		this.id = id;
		this.name = name;
	}


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
