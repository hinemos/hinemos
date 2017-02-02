/*

Copyright (C) since 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
