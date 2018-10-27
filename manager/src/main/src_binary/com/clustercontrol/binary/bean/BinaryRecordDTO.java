/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

import com.clustercontrol.binary.model.BinaryPatternInfo;

/**
 * バイナリレコードクラス.<br>
 * <br>
 * Agentで分割済のバイナリレコード毎の情報.<br>
 * ※soap通信でxml送信されるため各フィールドは文字列変換可能な型にすること.<br>
 */
public class BinaryRecordDTO {
	// レコード毎の情報.
	/** ファイル内レコード位置(任意バイナリファイル向け). */
	private String filePosition = "";
	/** レコード連番(キー生成用) */
	private String sequential = "";
	/** レコードキー(取り出し順考慮) */
	private String key = "";
	/** Base64エンコード文字列 */
	private String base64Str = "";
	/** 16進数文字列 */
	private String oxStr = "";
	/** タイムスタンプ(レコード毎) */
	private Long recTime = 0L;
	/** 各種タグ(tagName=tagValue;で連結). */
	private String tags = "";
	/** マッチしたバイナリ検索条件(フィルタなしはnull) */
	private BinaryPatternInfo matchBinaryProvision;

	// 各フィールドのsetterとgetter.
	/** レコード先頭識別フラグ */
	public String getFilePosition() {
		return filePosition;
	}

	/** レコード先頭識別フラグ */
	public void setFilePosition(String filePosition) {
		this.filePosition = filePosition;
	}

	/** レコード連番(キー生成用) */
	public String getSequential() {
		return sequential;
	}

	/** レコード連番(キー生成用) */
	public void setSequential(String sequential) {
		this.sequential = sequential;
	}

	/** レコードキー情報 */
	public String getKey() {
		return key;
	}

	/** レコードキー情報 */
	public void setKey(String key) {
		this.key = key;
	}

	/** Base64エンコード文字列 */
	public String getBase64Str() {
		return base64Str;
	}

	/** Base64エンコード文字列 */
	public void setBase64Str(String base64Str) {
		this.base64Str = base64Str;
	}

	/** 16進数文字列 */
	public String getOxStr() {
		return oxStr;
	}

	/** 16進数文字列 */
	public void setOxStr(String oxStr) {
		this.oxStr = oxStr;
	}

	/** レコード毎のタイムスタンプ */
	public Long getRecTime() {
		return recTime;
	}

	/** レコード毎のタイムスタンプ */
	public void setRecTime(Long recTime) {
		this.recTime = recTime;
	}

	/** 各種タグ(tagName=tagValue;で連結.) */
	public String getTags() {
		return tags;
	}

	/** 各種タグ(tagName=tagValue;で連結.) */
	public void setTags(String tags) {
		this.tags = tags;
	}

	/** マッチしたバイナリ検索条件(フィルタなしはnull) */
	public BinaryPatternInfo getMatchBinaryProvision() {
		return matchBinaryProvision;
	}

	/** マッチしたバイナリ検索条件(フィルタなしはnull) */
	public void setMatchBinaryProvision(BinaryPatternInfo matchBinaryProvision) {
		this.matchBinaryProvision = matchBinaryProvision;
	}

}
