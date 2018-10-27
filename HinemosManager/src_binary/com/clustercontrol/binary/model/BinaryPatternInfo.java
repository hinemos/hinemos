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
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoPK;

/**
 * The persistent class for the cc_binary_pattern_info database table.<br>
 * <br>
 * バイナリ監視でパターン設定時に表示されるバイナリ検索条件を格納するEntity定義.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")

@Entity
@Table(name = "cc_binary_pattern_info", schema = "setting")
@Cacheable(true)
public class BinaryPatternInfo implements Serializable {
	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	// --------DBと紐づくカラム.
	// 全体.
	/** 主キー(MonitorStringValueInfoと共通). */
	private MonitorStringValueInfoPK id;

	// フィルタ条件に関する項目.
	/** 説明. */
	private String description;
	/** 検索文字列. */
	private String grepString;
	/** エンコード. */
	private String encoding;

	// 通知に関する項目.
	/** 条件に一致した場合に処理(true:する/false:しない). */
	private Boolean processType;
	/** 通知重要度. */
	private Integer priority;
	/** 通知メッセージ. */
	private String message;
	/** 通知有効フラグ. */
	private Boolean validFlg;

	// --------Javaのみ.
	// 親.
	/** 監視設定. */
	private MonitorInfo monitorInfo;

	// コンストラクタ.
	public BinaryPatternInfo() {
	}

	public BinaryPatternInfo(MonitorStringValueInfoPK pk) {
		this.setId(pk);
	}

	public BinaryPatternInfo(String monitorId, Integer orderNo) {
		this(new MonitorStringValueInfoPK(monitorId, orderNo));
	}

	// getterとsetter
	/** 主キー(MonitorStringValueInfoと共通). */
	@XmlTransient
	@EmbeddedId
	public MonitorStringValueInfoPK getId() {
		if (this.id == null)
			this.id = new MonitorStringValueInfoPK();
		return this.id;
	}

	/** 主キー(MonitorStringValueInfoと共通). */
	public void setId(MonitorStringValueInfoPK id) {
		this.id = id;
	}

	/** 監視ID. */
	@Transient
	public String getMonitorId() {
		return getId().getMonitorId();
	}

	/** 監視ID. */
	public void setMonitorId(String monitorId) {
		getId().setMonitorId(monitorId);
	}

	/** 判定優先順位. */
	@XmlTransient
	@Transient
	public Integer getOrderNo() {
		return getId().getOrderNo();
	}

	/** 判定優先順位. */
	public void setOrderNo(Integer orderNo) {
		getId().setOrderNo(orderNo);
	}

	/** 説明. */
	@Column(name = "description")
	public String getDescription() {
		return this.description;
	}

	/** 説明. */
	public void setDescription(String description) {
		this.description = description;
	}

	/** 検索文字列. */
	@Column(name = "grep_string")
	public String getGrepString() {
		return this.grepString;
	}

	/** 検索文字列. */
	public void setGrepString(String grepString) {
		this.grepString = grepString;
	}

	/** エンコード. */
	@Column(name = "encoding")
	public String getEncoding() {
		return this.encoding;
	}

	/** エンコード. */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/** 条件に一致した場合に処理(true:する/false:しない). */
	@Column(name = "process_type")
	public Boolean getProcessType() {
		return this.processType;
	}

	/** 条件に一致した場合に処理(true:する/false:しない). */
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}

	/** 通知重要度. */
	@Column(name = "priority")
	public Integer getPriority() {
		return this.priority;
	}

	/** 通知重要度. */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/** 通知メッセージ. */
	@Column(name = "message")
	public String getMessage() {
		return this.message;
	}

	/** 通知メッセージ. */
	public void setMessage(String message) {
		this.message = message;
	}

	/** 通知有効フラグ. */
	@Column(name = "valid_flg")
	public Boolean getValidFlg() {
		return this.validFlg;
	}

	/** 通知有効フラグ. */
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	// bi-directional many-to-one association to MonitorInfo
	@XmlTransient
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "monitor_id", insertable = false, updatable = false)
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

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
			List<BinaryPatternInfo> list = monitorInfo.getBinaryPatternInfo();
			if (list == null) {
				list = new ArrayList<BinaryPatternInfo>();
			} else {
				for (BinaryPatternInfo entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorInfo.setBinaryPatternInfo(list);
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
			List<BinaryPatternInfo> list = this.monitorInfo.getBinaryPatternInfo();
			if (list != null) {
				Iterator<BinaryPatternInfo> iter = list.iterator();
				while (iter.hasNext()) {
					BinaryPatternInfo entity = iter.next();
					if (entity.getId().equals(this.getId())) {
						iter.remove();
						break;
					}
				}
			}
		}
	}

}
