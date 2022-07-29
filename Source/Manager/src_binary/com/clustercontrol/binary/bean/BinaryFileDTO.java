/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

/**
 * ファイル単位の監視結果情報.<br>
 * <br>
 * ファイル名等はBinaryCheckInfoを利用するのでこちらにはフィールドなし.
 */
public class BinaryFileDTO {

	// 取得元のファイルに関する情報.
	/** 最終更新日時 */
	private Long lastModTime = 0L;

	/** ファイルヘッダ(Base64エンコード済) */
	private String fileHeader = "";

	/** 各種タグ(tagName=tagValue;で連結). */
	private String tags = "";

	/** レコード数 */
	private int recordCount;

	// 各フィールドのsetterとgetter.
	/** 最終更新日時 */
	public Long getLastModTime() {
		return lastModTime;
	}

	/** 最終更新日時 */
	public void setLastModTime(Long lastModTime) {
		this.lastModTime = lastModTime;
	}

	/** ファイルヘッダ(Base64エンコード済) */
	public String getFileHeader() {
		return fileHeader;
	}

	/** ファイルヘッダ(Base64エンコード済) */
	public void setFileHeader(String fileHeader) {
		this.fileHeader = fileHeader;
	}

	/** 各種タグ(tagName=tagValue;で連結.) */
	public String getTags() {
		return tags;
	}

	/** 各種タグ(tagName=tagValue;で連結.) */
	public void setTags(String tags) {
		this.tags = tags;
	}

	/** レコード数 */
	public int getRecordCount() {
		return recordCount;
	}

	/** レコード数 */
	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

}
