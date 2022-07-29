/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_node_config_setting_item_info database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_config_setting_item_info", schema="setting")
@Cacheable(true)
public class NodeConfigSettingItemInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private NodeConfigSettingItemInfoPK id;
	private NodeConfigSettingInfo nodeConfigSettingInfo;

	public NodeConfigSettingItemInfo() {
	}
	
	public NodeConfigSettingItemInfo(String settingId, String settingItemId) {
		this(new NodeConfigSettingItemInfoPK(settingId, settingItemId));
	}
	public NodeConfigSettingItemInfo(NodeConfigSettingItemInfoPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeConfigSettingItemInfoPK getId() {
		if (id == null)
			id = new NodeConfigSettingItemInfoPK();
		return id;
	}
	public void setId(NodeConfigSettingItemInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getSettingId() {
		return getId().getSettingId();
	}
	public void setSettingId(String settingId) {
		getId().setSettingId(settingId);
	}

	@Transient
	public String getSettingItemId() {
		return getId().getSettingItemId();
	}
	public void setSettingItemId(String settingItemId) {
		getId().setSettingItemId(settingItemId);
	}
	
	//bi-directional many-to-one association to NodeConfigSettingInfo
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="setting_id", insertable=false, updatable=false)
	public NodeConfigSettingInfo getNodeConfigSettingInfo() {
		return this.nodeConfigSettingInfo;
	}

	@Deprecated
	public void setNodeConfigSettingInfo(NodeConfigSettingInfo nodeConfigSettingInfo) {
		this.nodeConfigSettingInfo = nodeConfigSettingInfo;
	}

	/**
	 * NodeConfigSettingInfoオブジェクト参照設定<BR>
	 * 
	 * NodeConfigSettingInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToNodeConfigSettingInfo(NodeConfigSettingInfo nodeConfigSettingInfo) {
		this.setNodeConfigSettingInfo(nodeConfigSettingInfo);
		if (nodeConfigSettingInfo != null) {
			List<NodeConfigSettingItemInfo> list = nodeConfigSettingInfo.getNodeConfigSettingItemList();
			if (list == null) {
				list = new ArrayList<>();
			} else {
				for(NodeConfigSettingItemInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeConfigSettingInfo.setNodeConfigSettingItemList(list);
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
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// NodeConfigSettingInfo
		if (this.nodeConfigSettingInfo != null) {
			List<NodeConfigSettingItemInfo> list = this.nodeConfigSettingInfo.getNodeConfigSettingItemList();
			if (list != null) {
				Iterator<NodeConfigSettingItemInfo> iter = list.iterator();
				while(iter.hasNext()) {
					NodeConfigSettingItemInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return "NodeConfigSettingItemInfo [id=" + id + ", nodeConfigSettingInfo=" + nodeConfigSettingInfo + "]";
	}
}