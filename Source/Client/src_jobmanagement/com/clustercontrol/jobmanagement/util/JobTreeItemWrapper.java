/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;

import org.openapitools.client.model.JobDetailInfoResponse;

public class JobTreeItemWrapper {

	/** ジョブ情報 */
	private JobInfoWrapper data;
	private JobDetailInfoResponse detail; 
	private JobTreeItemWrapper parent;

	/** 子のジョブツリーアイテムのリスト */
	private ArrayList<JobTreeItemWrapper> children = new ArrayList<JobTreeItemWrapper>();

	public JobTreeItemWrapper(){
		
	}

	public JobInfoWrapper getData() {
		return data;
	}

	public void setData(JobInfoWrapper data) {
		this.data = data;
	}

	public JobTreeItemWrapper getParent() {
		return parent;
	}

	public void setParent(JobTreeItemWrapper parent) {
		this.parent = parent;
	}

	public ArrayList<JobTreeItemWrapper> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<JobTreeItemWrapper> children) {
		this.children = children;
	}

	public JobDetailInfoResponse getDetail() {
		return detail;
	}

	public void setDetail(JobDetailInfoResponse detail) {
		this.detail = detail;
	}
}
