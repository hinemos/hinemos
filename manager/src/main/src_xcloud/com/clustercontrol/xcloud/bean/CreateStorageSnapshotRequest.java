/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;

public class CreateStorageSnapshotRequest extends Request {
	private String name;
	private String description;
	private String storageId;

	public CreateStorageSnapshotRequest() {
	}

	/**
	 * インスタンス Id を取得する。
	 * 
	 * @return インスタンス Id
	 */
	@ElementId("XCLOUD_CORE_STORAGE_ID")
	@Size(max = 64)
	@NotNull
	public String getStorageId() {
		return storageId;
	}
	/**
	 * インスタンス Id を指定する。
	 * 
	 * @param instanceId　インスタンス Id
	 */
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	/**
	 * イメージ名 を取得する。
	 * 
	 * @return インスタンス Id
	 */
	@ElementId("XCLOUD_CORE_STORAGESNAPSHOT_NAME")
	@Size(max = 128)
	@NotNull
	public String getName() {
		return name;
	}
	/**
	 * イメージ名 を指定する。
	 * 
	 * @param imageName イメージ名
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 説明 を取得する。
	 * 
	 * @return 説明
	 */
	@ElementId("XCLOUD_CORE_DESCRIPTION")
	@Size(max = 256)
	public String getDescription() {
		return description;
	}
	/**
	 * 説明 を指定する。
	 * 
	 * @param description 説明
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
