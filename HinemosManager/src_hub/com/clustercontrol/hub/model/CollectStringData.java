/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.model;


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

/**
 * The persistent class for the cc_collect_data_raw database table.
 * 
 */
@XmlType(namespace = "http://hub.ws.clustercontrol.com")
@Entity
@Table(name="cc_collect_data_string", schema="log")
public class CollectStringData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private CollectStringDataPK id;
	private Long time;
	private String value;
	private String logformatId;
	
	// Serializableのために、ListではなくArrayListで定義する
	private List<CollectDataTag> tagList = new ArrayList<>();

	public CollectStringData() {
	}

	public CollectStringData(CollectStringDataPK pk) {
		this.setId(pk);
	}
	public CollectStringData(Long collectorid, Long time) {
		this(new CollectStringDataPK(collectorid, time));
	}

	
	public CollectStringData(CollectStringDataPK pk,Long time, String value){
		this.setId(pk);
		this.setTime(time);
		this.setValue(value);
	}
	
	@XmlTransient
	@EmbeddedId
	public CollectStringDataPK getId() {
		if (id == null)
			id = new CollectStringDataPK();
		return this.id;
	}
	public void setId(CollectStringDataPK id) {
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
	
	
	@Column(name="time")
	public Long getTime(){
		return this.time;
	}
	public void setTime(Long time){
		this.time = time;
	}
	
	@Column(name="value")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@OneToMany
	@JoinColumns(
		{@JoinColumn(name="collect_id", referencedColumnName="collect_id"),
		@JoinColumn(name="data_id", referencedColumnName="data_id")
	})
	public List<CollectDataTag> getTagList() {
		return tagList;
	}
	public void setTagList(List<CollectDataTag> tagList) {
		this.tagList = tagList;
	}
	
	@Column(name="log_format_id")
	public String getLogformatId() {
		return logformatId;
	}
	public void setLogformatId(String logformatId) {
		this.logformatId = logformatId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CollectStringData [id=" + id + ", time=" + time + ", value=" + value + ", logformatId=" + logformatId
				+ ", tagList=" + tagList + "]";
	}
}