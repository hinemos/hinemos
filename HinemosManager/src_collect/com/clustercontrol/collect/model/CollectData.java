package com.clustercontrol.collect.model;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_collect_data_raw database table.
 * 
 */
@XmlType(namespace = "http://collect.ws.clustercontrol.com")
@Entity
@Table(name="cc_collect_data_raw", schema="log")
public class CollectData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private CollectDataPK id;
	private Float value;
	private Long position;

	public CollectData() {
	}

	public CollectData(CollectDataPK pk) {
		this.setId(pk);
	}
	public CollectData(Integer collectorid, Long time) {
		this(new CollectDataPK(collectorid, time));
	}

	
	public CollectData(CollectDataPK pk,Float value){
		this.setId(pk);
		this.setValue(value);
	}
	
	@XmlTransient
	@EmbeddedId
	public CollectDataPK getId() {
		if (id == null)
			id = new CollectDataPK();
		return this.id;
	}

	public void setId(CollectDataPK id) {
		this.id = id;
	}

	@Transient
	public Integer getCollectorId() {
		return getId().getCollectorid();
	}
	public void setCollectorId(Integer collectorid) {
		getId().setCollectorid(collectorid);
	}

	@Transient
	public Long getTime(){
		return getId().getTime();
	}
	public void setTime(Long time){
		getId().setTime(time);
	}
	
	@Column(name="value")
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	
	@XmlTransient
	@Column(name="position", insertable=false)
	public Long getPosition(){
		return this.position;
	}
	public void setPosition(Long position){
		this.position = position;
	}
}
