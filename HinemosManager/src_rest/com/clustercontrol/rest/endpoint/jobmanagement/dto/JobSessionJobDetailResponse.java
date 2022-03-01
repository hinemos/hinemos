/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.List;

public class JobSessionJobDetailResponse {

	/** ジョブ詳細一覧情報 */
	private List<JobDetailInfoResponse> jobDetailList = new ArrayList<>();

	/** ノード詳細一覧情報 */
	private List<JobNodeDetailResponse> jobNodeDetailList = new ArrayList<>();

	/** ファイル転送一覧情報 */
	private List<JobForwardFileResponse> jobForwardFileList = new ArrayList<>();

	public List<JobDetailInfoResponse> getJobDetailList() {
		return jobDetailList;
	}

	public void setJobDetailList(List<JobDetailInfoResponse> jobDetailList) {
		this.jobDetailList = jobDetailList;
	}

	public List<JobNodeDetailResponse> getJobNodeDetailList() {
		return jobNodeDetailList;
	}

	public void setJobNodeDetailList(List<JobNodeDetailResponse> jobNodeDetailList) {
		this.jobNodeDetailList = jobNodeDetailList;
	}

	public List<JobForwardFileResponse> getJobForwardFileList() {
		return jobForwardFileList;
	}

	public void setJobForwardFileList(List<JobForwardFileResponse> jobForwardFileList) {
		this.jobForwardFileList = jobForwardFileList;
	}


	
}
