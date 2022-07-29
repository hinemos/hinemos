/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

public class JobTreeItemResponseP4 {
	/** ジョブ情報 */
	private JobInfoResponse data;

	/** ジョブ詳細 */
	private JobDetailInfoResponse detail;

	/** 子のジョブツリーアイテムのリスト */
	private ArrayList<JobTreeItemResponseP4> children = new ArrayList<JobTreeItemResponseP4>();

	public JobTreeItemResponseP4() {
	}

	public JobInfoResponse getData() {
		return data;
	}

	public void setData(JobInfoResponse data) {
		this.data = data;
	}

	public JobDetailInfoResponse getDetail() {
		return detail;
	}

	public void setDetail(JobDetailInfoResponse detail) {
		this.detail = detail;
	}

	public ArrayList<JobTreeItemResponseP4> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<JobTreeItemResponseP4> children) {
		this.children = children;
	}

}
