/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_cfg_node_hostname database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_hostname", schema="setting")
@Cacheable(true)
public class NodeHostnameInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeHostnameInfoPK id;
	private String hostname;
	private NodeInfo nodeEntity;

	public NodeHostnameInfo() {
	}
	
	public NodeHostnameInfo(String facilityId, String hostname) {
		this(new NodeHostnameInfoPK(facilityId, hostname));
	}
	public NodeHostnameInfo(NodeHostnameInfoPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeHostnameInfoPK getId() {
		if (id == null)
			id = new NodeHostnameInfoPK();
		return id;
	}
	public void setId(NodeHostnameInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getFacilityId() {
		return getId().getFacilityId();
	}
	public void setFacilityId(String facilityId) {
		getId().setFacilityId(facilityId);
	}

	@Transient
	public String getHostname() {
		return getId().getHostname();
	}
	public void setHostname(String hostname) {
		getId().setHostname(hostname);
	}


	//bi-directional many-to-one association to nodeEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="facility_id", insertable=false, updatable=false)
	public NodeInfo getNodeEntity() {
		return this.nodeEntity;
	}

	@Deprecated
	public void setNodeEntity(NodeInfo nodeEntity) {
		this.nodeEntity = nodeEntity;
	}

	/**
	 * nodeEntityオブジェクト参照設定<BR>
	 * 
	 * nodeEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToNodeEntity(NodeInfo nodeEntity) {
		this.setNodeEntity(nodeEntity);
		if (nodeEntity != null) {
			List<NodeHostnameInfo> list = nodeEntity.getNodeHostnameInfo();
			if (list == null) {
				list = new ArrayList<NodeHostnameInfo>();
			} else {
				for(NodeHostnameInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeHostnameInfo(list);
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

		// nodeEntity
		if (this.nodeEntity != null) {
			List<NodeHostnameInfo> list = this.nodeEntity.getNodeHostnameInfo();
			if (list != null) {
				Iterator<NodeHostnameInfo> iter = list.iterator();
				while(iter.hasNext()) {
					NodeHostnameInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	@Override
	public NodeHostnameInfo clone() {
		try {
			NodeHostnameInfo cloneInfo = (NodeHostnameInfo)super.clone();
			cloneInfo.hostname = this.hostname;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
}