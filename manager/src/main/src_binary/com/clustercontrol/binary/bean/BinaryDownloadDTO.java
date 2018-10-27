/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

import com.clustercontrol.hub.model.CollectStringDataPK;

/**
 * バイナリダウンロード時の検索条件DTO.
 * 
 * @since 6.1.0
 * @version 6.1.0
 */
public class BinaryDownloadDTO {

	private BinaryQueryInfo queryInfo;
	private CollectStringDataPK primaryKey;
	private String recordKey;

	/**
	 * バイナリ検索条件取得.
	 */
	public BinaryQueryInfo getQueryInfo() {
		return queryInfo;
	}

	/**
	 * バイナリ検索条件設定.
	 */
	public void setQueryInfo(BinaryQueryInfo queryInfo) {
		this.queryInfo = queryInfo;
	}

	/**
	 * 主キー取得.
	 */
	public CollectStringDataPK getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * 主キー設定.
	 */
	public void setPrimaryKey(CollectStringDataPK primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * レコードキー(ファイル統合時のレコード順整列用)取得.
	 */
	public String getRecordKey() {
		return recordKey;
	}

	/**
	 * レコードキー(ファイル統合時のレコード順整列用)設定.
	 */
	public void setRecordKey(String recordKey) {
		this.recordKey = recordKey;
	}

}
