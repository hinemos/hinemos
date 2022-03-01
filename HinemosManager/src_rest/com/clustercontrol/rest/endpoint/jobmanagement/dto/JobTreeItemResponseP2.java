/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

public class JobTreeItemResponseP2 {

	/** ジョブ情報 */
	private JobInfoResponse data;

	/** 子のジョブツリーアイテムのリスト */
	private ArrayList<JobTreeItemResponseP2> children = new ArrayList<JobTreeItemResponseP2>();

	public JobTreeItemResponseP2() {
	}


	public JobInfoResponse getData() {
		return data;
	}

	public void setData(JobInfoResponse data) {
		this.data = data;
	}

	public ArrayList<JobTreeItemResponseP2> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<JobTreeItemResponseP2> children) {
		this.children = children;
	}


}
