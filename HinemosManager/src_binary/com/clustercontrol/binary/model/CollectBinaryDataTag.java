/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.hub.bean.ValueType;
import com.clustercontrol.hub.model.CollectDataTagPK;

//既存ログファイル収集で利用している"CollectDataTag"クラスを参照して作成.
// Cascade制約をつける場合は親テーブルを複数指定できないため、別クラスとして定義.
/**
 * The persistent class for the cc_collect_binary_data_tag database table.
 * 
 * @version 6.1.0
 * @since 6.1.0
 * @see com.clustercontrol.hub.model.CollectDataTag
 */
@XmlType(namespace = "http://hub.ws.clustercontrol.com")
@Entity
@Table(name="cc_collect_binary_data_tag", schema="log")
public class CollectBinaryDataTag implements Serializable{
	private static final long serialVersionUID = 1L;
//	private String key;
	private ValueType type;
	private String value;
	private CollectBinaryData collectBinaryData;
	
	private CollectDataTagPK id;

	public CollectBinaryDataTag() {
	}
	
	public CollectBinaryDataTag(CollectDataTagPK pk) {
		this.setId(pk);
	}
	public CollectBinaryDataTag(Long collectorid, Long time, String key) {
		this(new CollectDataTagPK(collectorid, time, key));
	}
	
	public CollectBinaryDataTag(CollectDataTagPK pk, ValueType type, String value){
		this.setId(pk);
		this.setType(type);
		this.setValue(value);
	}
	
	@XmlTransient
	@EmbeddedId
	public CollectDataTagPK getId() {
		if (id == null)
			id = new CollectDataTagPK();
		return this.id;
	}
	public void setId(CollectDataTagPK id) {
		this.id = id;
	}
	@Transient
	public Long getCollectId() {
		return getId().getCollectId();
	}
	public void setCollectId(Long collectId) {
		getId().setCollectId(collectId);
	}
	@Transient
	public Long getDataId() {
		return getId().getDataId();
	}
	public void setDataId(Long dataId) {
		getId().setDataId(dataId);
	}

	@Column(name="tag_key")
	public String getKey() {
		return getId().getKey();
	}
	public void setKey(String key) {
		getId().setKey(key);
	}

	@Column(name="type")
	public ValueType getType() {
		return type;
	}
	public void setType(ValueType type) {
		this.type = type;
	}

	@Column(name="tag_value")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	//bi-directional many-to-one association to CollectStringData
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="collect_id", referencedColumnName="collect_id", insertable=false, updatable=false),
		@JoinColumn(name="data_id", referencedColumnName="data_id", insertable=false, updatable=false)
	})
	public CollectBinaryData getCollectBinaryData() {
		return this.collectBinaryData;
	}

	@Deprecated
	public void setCollectBinaryData(CollectBinaryData collectBinaryData) {
		this.collectBinaryData = collectBinaryData;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CollectBinaryDataTag other = (CollectBinaryDataTag) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CollectBinaryDataTag [type=" + type + ", value=" + value + ", id=" + id + "]";
	}
}