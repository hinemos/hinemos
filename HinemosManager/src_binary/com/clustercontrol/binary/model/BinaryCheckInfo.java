/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * The persistent class for the cc_monitor_binary_info database table.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name = "cc_monitor_binary_info", schema = "setting")
@Cacheable(true)
public class BinaryCheckInfo extends MonitorCheckInfo implements Serializable {
	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	// ------DBと紐づく項目
	// 主キー.
	/** 監視ID. */
	private String monitorId = null;

	// 監視全体の情報.
	/** 監視対象ディレクトリ. */
	private String directory = null;
	/** 監視対象ファイル名(正規表現). */
	private String fileName = null;
	/** 収集方式. */
	private String collectType = null;

	// 収集方式増分のみの場合の情報.
	/** レコード分割方法. */
	private String cutType = null;
	/** タグ抽出タイプ(プリセット名). */
	private String tagType = null;

	// 収集方式増分のみ・レコード長指定の場合の情報.
	/** レコード長指定方法. */
	private String lengthType = null;
	/** タイムスタンプ有無. */
	private boolean haveTs = false;
	/** ファイル全体ヘッダサイズ */
	private long fileHeadSize = 0;

	// 収集方式増分のみ・固定長.
	/** 固定長のレコードサイズ */
	private int recordSize = 0;

	// 収集方式増分のみ・可変長。
	/** 可変長のレコードヘッダサイズ */
	private int recordHeadSize = 0;
	/** 可変長のレコードサイズの位置 */
	private int sizePosition = 0;
	/** 可変長のレコードサイズの表現バイト数 */
	private int sizeLength = 0;

	// 収集方式増分のみ・タイムスタンプあり.
	/** タイムスタンプ位置 */
	private int tsPosition = 0;
	/** タイムスタンプ種類(UNIX時間等) */
	private String tsType = null;

	// 可変長/タイムスタンプ表現バイナリ.
	/**
	 * リトルエンディアン <br>
	 * 例)16進数表記00 05 08に対して、実際に格納されているバイト配列が08 05 00の場合、true
	 */
	private boolean littleEndian = true;

	// ------Javaのみ.
	// 親.
	/** 監視情報. */
	private MonitorInfo monitorInfo = null;

	// 監視結果用フィールド.
	/** 監視対象ファイル名(監視結果として絶対パスを格納). */
	private String binaryfile = null;

	// その他.
	/** エラーメッセージ(プリセット取得時のエラーメッセージ等). */
	private String errMsg = null;

	// コンストラクタ.
	public BinaryCheckInfo() {
	}

	/** 監視ID. */
	@XmlTransient
	@Id
	@Column(name = "monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}

	/** 監視ID. */
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	/** 監視対象ディレクトリ. */
	@Column(name = "directory")
	public String getDirectory() {
		return directory;
	}

	/** 監視対象ディレクトリ. */
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/** 監視対象ファイル名(正規表現). */
	@Column(name = "file_name")
	public String getFileName() {
		return fileName;
	}

	/** 監視対象ファイル名(正規表現). */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/** 収集方式. */
	@Column(name = "collect_type")
	public String getCollectType() {
		return collectType;
	}

	/** 収集方式. */
	public void setCollectType(String collectType) {
		this.collectType = collectType;
	}

	/** レコード分割方法. */
	@Column(name = "cut_type")
	public String getCutType() {
		return cutType;
	}

	/** レコード分割方法. */
	public void setCutType(String cutType) {
		this.cutType = cutType;
	}

	/** タグ抽出タイプ(プリセット名). */
	@Column(name = "tag_type")
	public String getTagType() {
		return tagType;
	}

	/** タグ抽出タイプ(プリセット名). */
	public void setTagType(String tagType) {
		this.tagType = tagType;
	}

	/** レコード長指定方法. */
	@Column(name = "length_type")
	public String getLengthType() {
		return lengthType;
	}

	/** レコード長指定方法. */
	public void setLengthType(String lengthType) {
		this.lengthType = lengthType;
	}

	/** タイムスタンプ有無. */
	@Column(name = "have_ts")
	public boolean isHaveTs() {
		return haveTs;
	}

	/** タイムスタンプ有無. */
	public void setHaveTs(boolean haveTs) {
		this.haveTs = haveTs;
	}

	/** ファイル全体ヘッダサイズ */
	@Column(name = "file_head_size")
	public long getFileHeadSize() {
		return fileHeadSize;
	}

	/** ファイル全体ヘッダサイズ */
	public void setFileHeadSize(long fileHeadSize) {
		this.fileHeadSize = fileHeadSize;
	}

	/** 固定長のレコードサイズ */
	@Column(name = "record_size")
	public int getRecordSize() {
		return recordSize;
	}

	/** 固定長のレコードサイズ */
	public void setRecordSize(int recordSize) {
		this.recordSize = recordSize;
	}

	/** 可変長のレコードヘッダサイズ */
	@Column(name = "record_head_size")
	public int getRecordHeadSize() {
		return recordHeadSize;
	}

	/** 可変長のレコードヘッダサイズ */
	public void setRecordHeadSize(int recordHeadSize) {
		this.recordHeadSize = recordHeadSize;
	}

	/** 可変長のレコードサイズの位置 */
	@Column(name = "size_position")
	public int getSizePosition() {
		return sizePosition;
	}

	/** 可変長のレコードサイズの位置 */
	public void setSizePosition(int sizePosition) {
		this.sizePosition = sizePosition;
	}

	/** 可変長のレコードサイズの表現バイト数 */
	@Column(name = "size_length")
	public int getSizeLength() {
		return sizeLength;
	}

	/** 可変長のレコードサイズの表現バイト数 */
	public void setSizeLength(int sizeLength) {
		this.sizeLength = sizeLength;
	}

	/** タイムスタンプ位置 */
	@Column(name = "ts_postion")
	public int getTsPosition() {
		return tsPosition;
	}

	/** タイムスタンプ位置 */
	public void setTsPosition(int tsPosition) {
		this.tsPosition = tsPosition;
	}

	/** タイムスタンプ種類(UNIX時間等) */
	@Column(name = "ts_type")
	public String getTsType() {
		return tsType;
	}

	/** タイムスタンプ種類(UNIX時間等) */
	public void setTsType(String tsType) {
		this.tsType = tsType;
	}

	/** リトルエンディアン(可変長サイズ/タイムスタンプの表現バイナリ) */
	@Column(name = "little_endian")
	public boolean isLittleEndian() {
		return littleEndian;
	}

	/** リトルエンディアン(可変長サイズ/タイムスタンプの表現バイナリ) */
	public void setLittleEndian(boolean littleEndian) {
		this.littleEndian = littleEndian;
	}

	/** 監視対象ファイル名(監視結果として絶対パスを格納). */
	@Transient
	public String getBinaryfile() {
		return binaryfile;
	}

	/** 監視対象ファイル名(監視結果として絶対パスを格納). */
	public void setBinaryfile(String binaryfile) {
		this.binaryfile = binaryfile;
	}

	/** エラーメッセージ(プリセット取得時のエラーメッセージ等). */
	@Transient
	public String getErrMsg() {
		return errMsg;
	}

	/** エラーメッセージ(プリセット取得時のエラーメッセージ等). */
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	// bi-directional one-to-one association to MonitorInfo
	@XmlTransient
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@PrimaryKeyJoinColumn
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

	@Deprecated
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	/**
	 * MonitorInfoオブジェクト参照設定<BR>
	 * 
	 * MonitorInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void relateToMonitorInfo(MonitorInfo monitorInfo) {
		this.setMonitorInfo(monitorInfo);
		if (monitorInfo != null) {
			monitorInfo.setBinaryCheckInfo(this);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void unchain() {

		// MonitorInfo
		if (this.monitorInfo != null) {
			this.monitorInfo.setBinaryCheckInfo(null);
		}
	}

	@Override
	public String toString() {

		String delimiter = ", ";

		String checkInfo = "BinaryCheckInfo [";

		checkInfo = checkInfo + "monitorId=" + monitorId;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "directory=" + directory;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "fileName=" + fileName;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "collectType=" + collectType;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "cutType=" + cutType;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "tagType=" + tagType;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "lengthType=" + lengthType;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "haveTs=" + haveTs;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "fileHeadSize=" + fileHeadSize;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "recordSize=" + recordSize;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "recordHeadSize=" + recordHeadSize;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "sizePosition" + sizePosition;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "sizeLength=" + sizeLength;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "tsPosition=" + tsPosition;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "tsType=" + tsType;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "littleEndian=" + littleEndian;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "binaryfile=" + binaryfile;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "errMsg=" + errMsg;

		checkInfo = checkInfo + "]";

		return checkInfo;
	}
}