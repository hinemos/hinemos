/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

import java.io.Serializable;
import java.util.List;

import com.clustercontrol.hub.bean.StringSampleTag;

// 既存ログファイル監視で利用している下記クラスを参照して作成.
// StringSampleクラス・StringSampleDataクラス
// ※バイナリデータを扱うため別クラスとして定義.
/**
 * 収集したバイナリデータを保持するクラス<BR>
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinarySample implements Serializable {

	/** シリアルバージョンUID(フィールド変更したらアップ) */
	private static final long serialVersionUID = 1L;

	// 以下フィールド.
	/** Agent収集・送信時刻 */
	private Long dateTime = null;
	/** 監視ID */
	private String monitorId = null;
	/** FacilityID */
	private String facilityId = null;
	/** 収集方式 */
	private String collectType = null;
	/** ファイルパス */
	private String targetName = null;
	/** ファイル内レコード位置 */
	private String filePosition = null;
	/** ファイルヘッダサイズ */
	private Long fileHeaderSize = Long.valueOf(0L);
	/** レコードキー(ファイル名＋同一ファイル内での順序を考慮した数値) */
	private String recKey = null;
	/** レコード時刻(ログバイナリに埋め込まれてるレコード毎タイムスタンプ) */
	private Long recordTime = Long.valueOf(0L);
	/** バイナリ値 */
	private List<Byte> value = null;
	/** タグ(文字列監視と共通の型) */
	private List<StringSampleTag> tagList = null;

	// 以下setterとgetter.
	/** Agent収集・送信時刻 */
	public Long getDateTime() {
		return dateTime;
	}

	/** Agent収集・送信時刻 */
	public void setDateTime(Long dateTime) {
		this.dateTime = dateTime;
	}

	/** 監視ID */
	public String getMonitorId() {
		return monitorId;
	}

	/** 監視ID */
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	/** FacilityID */
	public String getFacilityId() {
		return facilityId;
	}

	/** FacilityID */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/** 収集方式 */
	public String getCollectType() {
		return collectType;
	}

	/** 収集方式 */
	public void setCollectType(String collectType) {
		this.collectType = collectType;
	}

	/** ファイルパス */
	public String getTargetName() {
		return targetName;
	}

	/** ファイルパス */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	/** ファイル内レコード位置 */
	public String getFilePosition() {
		return filePosition;
	}

	/** ファイル内レコード位置 */
	public void setFilePosition(String filePosition) {
		this.filePosition = filePosition;
	}

	/** ファイルヘッダサイズ */
	public Long getFileHeaderSize() {
		return fileHeaderSize;
	}

	/** ファイルヘッダサイズ */
	public void setFileHeaderSize(Long fileHeaderSize) {
		this.fileHeaderSize = fileHeaderSize;
	}

	/** レコードキー(ファイル名＋同一ファイル内での順序を考慮した数値) */
	public String getRecKey() {
		return recKey;
	}

	/** レコードキー(ファイル名＋同一ファイル内での順序を考慮した数値) */
	public void setRecKey(String recKey) {
		this.recKey = recKey;
	}

	/** レコード時刻(ログバイナリに埋め込まれてるレコード毎タイムスタンプ) */
	public Long getRecordTime() {
		return recordTime;
	}

	/** レコード時刻(ログバイナリに埋め込まれてるレコード毎タイムスタンプ) */
	public void setRecordTime(Long recordTime) {
		this.recordTime = recordTime;
	}

	/** バイナリ値 */
	public List<Byte> getValue() {
		return value;
	}

	/** バイナリ値 */
	public void setValue(List<Byte> value) {
		this.value = value;
	}

	/** タグ(文字列監視と共通の型) */
	public List<StringSampleTag> getTagList() {
		return tagList;
	}

	/** タグ(文字列監視と共通の型) */
	public void setTagList(List<StringSampleTag> tagList) {
		this.tagList = tagList;
	}

}
