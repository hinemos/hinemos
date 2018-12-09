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


/**
 * The persistent class for the cc_cfg_node_variable database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_variable", schema="setting")
@Cacheable(true)
public class NodeVariableInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeVariableInfoPK id;
	private String nodeVariableValue			= "";
	private NodeInfo nodeEntity;

	public NodeVariableInfo() {
	}

	public NodeVariableInfo(String facilityId, String nodeVariableName) {
		this(new NodeVariableInfoPK(facilityId, nodeVariableName));
	}

	public NodeVariableInfo(NodeVariableInfoPK id) {
		this.id = id;
	}
	
	@EmbeddedId
	@XmlTransient
	public NodeVariableInfoPK getId() {
		if (id == null)
			id = new NodeVariableInfoPK();
		return this.id;
	}
	public void setId(NodeVariableInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getFacilityId() {
		return getId().getFacilityId();
	}
	public void setFacilityId(String facilityId) {
		this.getId().setFacilityId(facilityId);
	}

	@Transient
	public String getNodeVariableName() {
		return getId().getNodeVariableName();
	}
	public void setNodeVariableName(String nodeVariableName) {
		this.getId().setNodeVariableName(nodeVariableName);
	}

	@Column(name="node_variable_value")
	public String getNodeVariableValue() {
		return this.nodeVariableValue;
	}

	public void setNodeVariableValue(String nodeVariableValue) {
		this.nodeVariableValue = nodeVariableValue;
	}


	//bi-directional many-to-one association to NodeEntity
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
	 * NodeEntityオブジェクト参照設定<BR>
	 * 
	 * NodeEntity設定時はSetterに代わりこちらを使用すること。
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
			List<NodeVariableInfo> list = nodeEntity.getNodeVariableInfo();
			if (list == null) {
				list = new ArrayList<NodeVariableInfo>();
			} else {
				for(NodeVariableInfo entity : list){
					if (entity.getFacilityId().equals(this.getFacilityId()) &&
							entity.getNodeVariableName().equals(this.getNodeVariableName())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeVariableInfo(list);
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
		// NodeEntity
		if (this.nodeEntity != null) {
			List<NodeVariableInfo> list = this.nodeEntity.getNodeVariableInfo();
			if (list != null) {
				Iterator<NodeVariableInfo> iter = list.iterator();
				while(iter.hasNext()) {
					NodeVariableInfo entity = iter.next();
					if (entity.getFacilityId().equals(this.getFacilityId()) &&
							entity.getNodeVariableName().equals(this.getNodeVariableName())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	@Override
	public NodeVariableInfo clone() {
		try {
			NodeVariableInfo cloneInfo = (NodeVariableInfo)super.clone();
			cloneInfo.id = (NodeVariableInfoPK)id.clone();
			cloneInfo.nodeVariableValue = this.nodeVariableValue;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}