/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.util.List;

/**
 * 最新のノード構成情報を保持するBean.<br>
 * 
 * @since 6.2.0
 * @version 6.2.0
 * @param <T>
 *            任意の構成情報Entity
 */
public class LatestNodeConfigWrapper<T> {

	/** Hinemos最終収集日時 */
	private Long collected = null;

	/** 構成情報の最終更新日時 */
	private Long lastUpdated = null;

	/** 構成情報Entityのリスト */
	private List<T> configList = null;

	/** Hinemos最終収集日時 */
	public Long getCollected() {
		return collected;
	}

	/** Hinemos最終収集日時 */
	public void setCollected(Long collected) {
		this.collected = collected;
	}

	/** 構成情報の最終更新日時 */
	public Long getLastUpdated() {
		return lastUpdated;
	}

	/** 構成情報の最終更新日時 */
	public void setLastUpdated(Long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/** 構成情報Entityのリスト */
	public List<T> getConfigList() {
		return configList;
	}

	/** 構成情報Entityのリスト */
	public void setConfigList(List<T> configList) {
		this.configList = configList;
	}
}
