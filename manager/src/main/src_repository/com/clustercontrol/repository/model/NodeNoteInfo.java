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
 * The persistent class for the cc_cfg_node_note database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_note", schema="setting")
@Cacheable(true)
public class NodeNoteInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeNoteInfoPK id;
	private String note = "";
	private NodeInfo nodeEntity;

	public NodeNoteInfo() {
	}

	public NodeNoteInfo(String facilityId, Integer noteId) {
		this(new NodeNoteInfoPK(facilityId, noteId));
	}
	public NodeNoteInfo(NodeNoteInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@EmbeddedId
	public NodeNoteInfoPK getId() {
		if (id == null)
			id = new NodeNoteInfoPK();
		return id;
	}
	public void setId(NodeNoteInfoPK id) {
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
	public Integer getNoteId() {
		return getId().getNoteId();
	}
	public void setNoteId(Integer noteId) {
		getId().setNoteId(noteId);
	}


	public String getNote() {
		return this.note;
	}

	public void setNote(String note) {
		this.note = note;
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
			List<NodeNoteInfo> list = nodeEntity.getNodeNoteInfo();
			if (list == null) {
				list = new ArrayList<NodeNoteInfo>();
			} else {
				for(NodeNoteInfo entity : list){
					if (entity.getFacilityId().equals(this.getFacilityId())
						&& entity.getNoteId().equals(this.getNoteId())
						) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeNoteInfo(list);
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
			List<NodeNoteInfo> list = this.nodeEntity.getNodeNoteInfo();
			if (list != null) {
				Iterator<NodeNoteInfo> iter = list.iterator();
				while(iter.hasNext()) {
					NodeNoteInfo entity = iter.next();
					if (entity.getFacilityId().equals(this.getFacilityId())
							&& entity.getNoteId().equals(this.getNoteId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	@Override
	public NodeNoteInfo clone() {
		try {
			NodeNoteInfo cloneInfo = (NodeNoteInfo)super.clone();
			cloneInfo.id = this.id.clone();
			cloneInfo.note = this.note;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}