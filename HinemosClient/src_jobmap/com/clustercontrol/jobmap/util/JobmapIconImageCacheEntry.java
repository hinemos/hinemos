/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmap.util;

import org.openapitools.client.model.JobmapIconImageInfoResponse;

/**
 * ジョブマップ用のアイコンの画像キャッシュ管理でDTOと画像ファイルのbyte[]を格納するためのクラス。
 * 
 * @since 7.0.0
 **/
public class JobmapIconImageCacheEntry {

	private JobmapIconImageInfoResponse jobmapIconImage;
	private byte[] filedata;

	public JobmapIconImageCacheEntry() {
	}

	public JobmapIconImageInfoResponse getJobmapIconImage() {
		return jobmapIconImage;
	}

	public void setJobmapIconImage(JobmapIconImageInfoResponse jobmapIconImage) {
		this.jobmapIconImage = jobmapIconImage;
	}

	public byte[] getFiledata() {
		return filedata;
	}

	public void setFiledata(byte[] filedata) {
		this.filedata = filedata;
	}
}
