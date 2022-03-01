/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

public class JobTreeItemResponseP1 {
	/** ジョブ情報 */
	private JobInfoResponseP1 data;

	/** 子のジョブツリーアイテムのリスト */
	private ArrayList<JobTreeItemResponseP1> children = new ArrayList<JobTreeItemResponseP1>();

	public JobTreeItemResponseP1() {
	}


	public JobInfoResponseP1 getData() {
		return data;
	}

	public void setData(JobInfoResponseP1 data) {
		this.data = data;
	}

	public ArrayList<JobTreeItemResponseP1> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<JobTreeItemResponseP1> children) {
		this.children = children;
	}

}
