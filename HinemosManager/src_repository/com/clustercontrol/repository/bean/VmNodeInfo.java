/*

Copyright (C) since 2006 NTT DATA Corporation

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

import com.clustercontrol.repository.model.NodeInfo;

/**
 * リポジトリ機能－VMノードの追加・編集を行うテーブルデータ用Bean。<BR>
 * 
 * @version 3.1.0
 * @since 3.1.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class VmNodeInfo implements Serializable {

	private static final long serialVersionUID = 3450889228353874813L;

	/** 未登録 **/
	public static final int UNREGISTERED = 0;

	/** 登録済み **/
	public static final int REGISTERED = 1;

	/** 登録成功 **/
	public static final int REGIST_SUCCESS = 2;

	/** 登録失敗 **/
	public static final int REGIST_FAIL = 3;


	/** 処理チェックボックス */
	private boolean selection = false;

	/** リポジトリ */
	private Integer registration = 0;

	/** 最新管理ノード */
	private String newManageNode = null;

	/** VMノードプロパティ */
	private NodeInfo nodeInfo = null;


	// Getter And Setter
	public boolean isSelection() {
		return selection;
	}

	public void setSelection(boolean selection) {
		this.selection = selection;
	}

	public Integer getRegistration() {
		return registration;
	}

	public void setRegistration(Integer registration) {
		this.registration = registration;
	}

	public String getNewManageNode() {
		return newManageNode;
	}

	public void setNewManageNode(String newManageNode) {
		this.newManageNode = newManageNode;
	}

	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

	public void setNodeInfo(NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

}