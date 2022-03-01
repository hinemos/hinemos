/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinaryTagConstant;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.util.BinaryUtil;

// 既存ログファイル収集で利用している"CollectStringData"クラスを参照して作成.
/**
 * The persistent class for the cc_collect_data_binary database table.
 * 
 * @version 6.1.0
 * @since 6.1.0
 * @see com.clustercontrol.hub.model.CollectStringData
 */
@XmlType(namespace = "http://hub.ws.clustercontrol.com")
@Entity
@Table(name = "cc_collect_data_binary", schema = "log")
public class CollectBinaryData implements Serializable {

	// フィールド.
	// default serial version id, required for serializable classes.
	/** シリアルバージョンUID(カラム変更時にバージョンアップ) */
	private static final long serialVersionUID = 1L;

	// カラム.
	/** 主キー(文字列収集と同様) */
	private CollectStringDataPK id;
	/** 収集方式. */
	private String collectType;
	/** ファイル内レコード位置(ファイル全体監視向け). */
	private String filePosition;
	/** ファイルキー(ファイル全体監視の同一ファイル識別用) */
	private String fileKey;
	/** レコードキー(同一時点の同一ファイルに対する監視でレコード順を特定するキー) */
	private String recordKey;
	/** ファイル全体ヘッダサイズ */
	private Long fileHeadSize;
	/** Agent収集・送信時刻もしくはログレコード時刻 */
	private Long time;
	/** 収集バイナリデータ本体 */
	private byte[] value;

	// Serializableのために、ListではなくArrayListで定義する
	/** タグ */
	private List<CollectBinaryDataTag> tagList = new ArrayList<CollectBinaryDataTag>();

	// コンストラクタ.
	/** 空データ作成. */
	public CollectBinaryData() {
	}

	/** PKのみ指定. */
	public CollectBinaryData(CollectStringDataPK pk) {
		this.setId(pk);
	}

	/** 収集IDと時間のみ指定. */
	public CollectBinaryData(Long collectorid, Long time) {
		this(new CollectStringDataPK(collectorid, time));
	}

	/** タグリスト以外. */
	public CollectBinaryData(CollectStringDataPK pk, Long time, byte[] value) {
		this.setId(pk);
		this.setTime(time);
		this.setValue(value);
	}

	/** データフル. */
	public CollectBinaryData(CollectStringDataPK pk, Long time, byte[] value, List<CollectBinaryDataTag> tagList) {
		this.setId(pk);
		this.setTime(time);
		this.setValue(value);
		this.setTagList(tagList);
	}

	// setterとgetter.
	/** 主キー(文字列収集と同様) */
	@XmlTransient
	@EmbeddedId
	public CollectStringDataPK getId() {
		if (id == null)
			id = new CollectStringDataPK();
		return this.id;
	}

	/** 主キー(文字列収集と同様) */
	public void setId(CollectStringDataPK id) {
		this.id = id;
	}

	/** 収集ID(主キーCollectStringDataPKクラス定義) */
	@Transient
	public Long getCollectId() {
		return getId().getCollectId();
	}

	/** 収集ID(主キーCollectStringDataPKクラス定義) */
	public void setCollectId(Long collectId) {
		getId().setCollectId(collectId);
	}

	/** データID(主キーCollectStringDataPKクラス定義) */
	@Transient
	public Long getDataId() {
		return getId().getDataId();
	}

	/** データID(主キーCollectStringDataPKクラス定義) */
	public void setDataId(Long dataId) {
		getId().setDataId(dataId);
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

	/** ファイル内レコード位置(ファイル全体監視向け). */
	@Column(name = "file_position")
	public String getFilePosition() {
		return filePosition;
	}

	/** ファイル内レコード位置(ファイル全体監視向け). */
	public void setFilePosition(String filePosition) {
		this.filePosition = filePosition;
	}

	/** ファイルキー(ファイル全体監視の同一ファイル識別用) */
	@Column(name = "file_key")
	public String getFileKey() {
		return fileKey;
	}

	/** ファイルキー(ファイル全体監視の同一ファイル識別用) */
	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}

	/** レコードキー(同一時点の同一ファイルに対する監視でレコード順を特定するキー) */
	@Column(name = "record_key")
	public String getRecordKey() {
		return recordKey;
	}

	/** レコードキー(同一時点の同一ファイルに対する監視でレコード順を特定するキー) */
	public void setRecordKey(String recordKey) {
		this.recordKey = recordKey;
	}

	/** ファイル全体ヘッダサイズ */
	@Column(name = "file_head_size")
	public Long getFileHeadSize() {
		return this.fileHeadSize;
	}

	/** ファイル全体ヘッダサイズ */
	public void setFileHeadSize(Long fileHeadSize) {
		this.fileHeadSize = fileHeadSize;
	}

	/** 収集時間 */
	@Column(name = "time")
	public Long getTime() {
		return this.time;
	}

	/** 収集時間 */
	public void setTime(Long time) {
		this.time = time;
	}

	/** 収集バイナリデータ本体 */
	@Column(name = "value")
	public byte[] getValue() {
		return value;
	}

	/** 収集バイナリデータ本体 */
	public void setValue(byte[] value) {
		this.value = value;
	}

	/** タグ */
	@OneToMany
	@JoinColumns({ @JoinColumn(name = "collect_id", referencedColumnName = "collect_id"),
			@JoinColumn(name = "data_id", referencedColumnName = "data_id") })
	public List<CollectBinaryDataTag> getTagList() {
		return tagList;
	}

	/** タグ */
	public void setTagList(List<CollectBinaryDataTag> tagList) {
		this.tagList = tagList;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * CollectBinaryData [field1=value, field2=value...]<br>
	 * <br>
	 * データ本体の文字列が不要な場合はtoShortString()を使うこと.<br>
	 */
	@Override
	public String toString() {

		String delimiter = ", ";

		// タグ文字列(tagkey=value,).
		StringBuilder tagSb = new StringBuilder();
		if (tagList != null && !tagList.isEmpty()) {
			boolean isTop = true;
			for (CollectBinaryDataTag tag : tagList) {
				if (!isTop) {
					tagSb.append(delimiter);
				}
				tagSb.append(tag.getKey());
				tagSb.append("=");
				tagSb.append(tag.getValue());
				isTop = false;
			}
		}
		// データ本体(16進数変換).
		String oxStr = BinaryConstant.HEX_PREFIX + BinaryUtil.listToString(BinaryUtil.arrayToList(value));

		// 出力文字列.
		String checkInfo = "CollectBinaryData [";

		checkInfo = checkInfo + "id=" + id;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "collectType=" + collectType;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "filePosition=" + filePosition;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "fileKey=" + fileKey;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "recordKey=" + recordKey;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "fileHeadSize=" + fileHeadSize;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "time=" + time;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "value=[" + oxStr + "]";
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "tagList=[" + tagSb.toString() + "]";

		checkInfo = checkInfo + "]";

		return checkInfo;
	}

	/**
	 * 簡略な文字列取得(ログ出力向け).<br>
	 * <br>
	 * CollectBinaryData(excerpt) [field1=value, field2=value...]<br>
	 * <br>
	 * ログ出力で見やすいようにデータ本体以外の情報を出力.<br>
	 * タグはファイル名のみ出力.<
	 */
	public String toShortString() {

		// タグ文字列(ファイル名のみ)
		StringBuilder tagSb = new StringBuilder();
		if (tagList != null && !tagList.isEmpty()) {
			for (CollectBinaryDataTag tag : tagList) {
				if (BinaryTagConstant.CommonTagName.FILE_NAME.equals(tag.getKey())) {
					tagSb.append(tag.getKey());
					tagSb.append("=");
					tagSb.append(tag.getValue());
					break;
				}
			}
		}

		String delimiter = ", ";

		// 出力文字列.
		String checkInfo = "CollectBinaryData(excerpt) [";

		checkInfo = checkInfo + "id=" + id;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "collectType=" + collectType;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "filePosition=" + filePosition;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "fileKey=" + fileKey;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "recordKey=" + recordKey;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "fileHeadSize=" + fileHeadSize;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "time=" + time;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "tagList=[" + tagSb.toString() + "]";

		checkInfo = checkInfo + "]";

		return checkInfo;
	}

}
