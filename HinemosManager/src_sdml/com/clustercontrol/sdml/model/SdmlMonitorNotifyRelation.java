/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import com.clustercontrol.notify.model.NotifyRelationInfo;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * The persistent class for the cc_sdml_monitor_notify_relation database table.
 *
 */
@Entity
@Table(name="cc_sdml_monitor_notify_relation", schema="setting")
@Cacheable(true)
public class SdmlMonitorNotifyRelation implements Serializable {
	private static final long serialVersionUID = 1L;

	private SdmlMonitorNotifyRelationPK id;
	private String notifyGroupId;

	private List<NotifyRelationInfo> notifyRelationList;
	private SdmlControlSettingInfo sdmlControlSettingInfo;

	public SdmlMonitorNotifyRelation() {
	}

	public SdmlMonitorNotifyRelation(SdmlMonitorNotifyRelationPK id) {
		this.setId(id);
	}
	public SdmlMonitorNotifyRelation(String applicationId, String sdmlMonitorTypeId) {
		this(new SdmlMonitorNotifyRelationPK(applicationId, sdmlMonitorTypeId));
	}

	@XmlTransient
	@EmbeddedId
	public SdmlMonitorNotifyRelationPK getId() {
		if (id == null) {
			id = new SdmlMonitorNotifyRelationPK();
		}
		return this.id;
	}
	public void setId(SdmlMonitorNotifyRelationPK id) {
		this.id = id;
	}

	@Transient
	public String getApplicationId() {
		return getId().getApplicationId();
	}
	public void setApplicationId(String applicationId) {
		getId().setApplicationId(applicationId);
	}

	@Transient
	public String getSdmlMonitorTypeId() {
		return getId().getSdmlMonitorTypeId();
	}
	public void setMsetSdmlMonitorTypeIdoduleId(String sdmlMonitorTypeId) {
		getId().setSdmlMonitorTypeId(sdmlMonitorTypeId);
	}

	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return notifyGroupId;
	}
	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	@Transient
	public List<NotifyRelationInfo> getNotifyRelationList() {
		return this.notifyRelationList;
	}
	public void setNotifyRelationList(List<NotifyRelationInfo> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	// bi-directional many-to-one association to SdmlControlSettingInfo
	@XmlTransient
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "application_id", insertable = false, updatable = false)
	public SdmlControlSettingInfo getSdmlControlSettingInfo() {
		return this.sdmlControlSettingInfo;
	}
	@Deprecated
	public void setSdmlControlSettingInfo(SdmlControlSettingInfo sdmlControlSettingInfo) {
		this.sdmlControlSettingInfo = sdmlControlSettingInfo;
	}

	/**
	 * SdmlControlSettingInfoオブジェクト参照設定<BR>
	 * 
	 * SdmlControlSettingInfo設定時はSetterに代わりこちらを使用すること。
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
	public void relateToSdmlControlSettingInfo(SdmlControlSettingInfo sdmlControlSettingInfo) {
		this.setSdmlControlSettingInfo(sdmlControlSettingInfo);
		if (sdmlControlSettingInfo != null) {
			List<SdmlMonitorNotifyRelation> list = sdmlControlSettingInfo.getSdmlMonitorNotifyRelationList();
			if (list == null) {
				list = new ArrayList<SdmlMonitorNotifyRelation>();
			} else {
				for (SdmlMonitorNotifyRelation entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			sdmlControlSettingInfo.setSdmlMonitorNotifyRelationList(list);
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

		// SdmlControlSettingInfo
		if (this.sdmlControlSettingInfo != null) {
			List<SdmlMonitorNotifyRelation> list = this.sdmlControlSettingInfo.getSdmlMonitorNotifyRelationList();
			if (list != null) {
				Iterator<SdmlMonitorNotifyRelation> iter = list.iterator();
				while (iter.hasNext()) {
					SdmlMonitorNotifyRelation entity = iter.next();
					if (entity.getId().equals(this.getId())) {
						iter.remove();
						break;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SdmlMonitorNotifyRelation [");
		sb.append("id = " + id.toString());
		sb.append(", notifyGroupId = " + notifyGroupId);
		sb.append("]");
		return sb.toString();
	}
}
