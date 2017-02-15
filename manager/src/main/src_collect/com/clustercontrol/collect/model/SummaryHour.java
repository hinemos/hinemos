package com.clustercontrol.collect.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The persistent class for the cc_collect_summary_hour database table.
 * 
 */
@XmlType(namespace = "http://collect.ws.clustercontrol.com")
@Entity
@Table(name="cc_collect_summary_hour", schema="log")
public class SummaryHour implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private static Log m_log = LogFactory.getLog(SummaryHour.class);
	
	private CollectDataPK id;
	private Float avg;
	private Float min;
	private Float max;
	private Integer count;

	public SummaryHour() {
	}

	public SummaryHour(CollectDataPK pk) {
		this.setId(pk);
	}
	public SummaryHour(Integer collectorid, Long time) {
		this(new CollectDataPK(collectorid, time));
	}

	
	public SummaryHour(CollectDataPK pk,Float avg, Float min,Float max,Integer count){
		this.setId(pk);
		this.setAvg(avg);
		this.setMin(min);
		this.setMax(max);
		this.setCount(count);
	}
	
	@XmlTransient
	@EmbeddedId
	public CollectDataPK getId() {
		if (id == null)
			id = new CollectDataPK();
		return this.id;
	}

	public void setId(CollectDataPK collectorid) {
		this.id = collectorid;
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
	
	@Column(name="avg")
	public Float getAvg() {
		return avg;
	}
	public void setAvg(Float avg) {
		this.avg = avg;
	}
	@Column(name="min")
	public Float getMin() {
		return min;
	}
	public void setMin(Float min) {
		this.min = min;
	}
	@Column(name="max")
	public Float getMax() {
		return max;
	}
	public void setMax(Float max) {
		this.max = max;
	}
	@Column(name="count")
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	
	@Override
	public SummaryHour clone() {
			SummaryHour summaryhour = null;
			try {
				summaryhour = (SummaryHour)super.clone();
			} catch (CloneNotSupportedException e) {
				m_log.debug("SummaryHour.clone() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			}
		return summaryhour;
	}
}	