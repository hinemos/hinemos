/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The persistent class for the cc_collect_summary_month database table.
 * 
 */
@XmlType(namespace = "http://collect.ws.clustercontrol.com")
@Entity
@Table(name="cc_collect_summary_month", schema="log")
public class SummaryMonth implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private static Log m_log = LogFactory.getLog(SummaryMonth.class);
	
	private CollectDataPK id;
	private Float avg;
	private Float min;
	private Float max;
	private Integer count;
	private Float averageAvg;
	private Integer averageCount;
	private Float standardDeviationAvg;
	private Integer standardDeviationCount;

	public SummaryMonth() {
	}

	public SummaryMonth(CollectDataPK pk) {
		this.setId(pk);
	}
	public SummaryMonth(Integer collectorid, Long time) {
		this(new CollectDataPK(collectorid, time));
	}

	
	public SummaryMonth(CollectDataPK pk,Float avg, Float min, Float max, Integer count,
			Float averageAvg, Integer averageCount, Float standardDeviationAvg, Integer standardDeviationCount){
		this.setId(pk);
		this.setAvg(avg);
		this.setMin(min);
		this.setMax(max);
		this.setCount(count);
		this.setAverageAvg(averageAvg);
		this.setAverageCount(averageCount);
		this.setStandardDeviationAvg(standardDeviationAvg);
		this.setStandardDeviationCount(standardDeviationCount);
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
		if (avg == null) {
			return Float.NaN;
		} else {
			return avg;
		}
	}
	public void setAvg(Float avg) {
		this.avg = avg;
	}
	@Column(name="min")
	public Float getMin() {
		if (min == null) {
			return Float.NaN;
		} else {
			return min;
		}
	}
	public void setMin(Float min) {
		this.min = min;
	}
	@Column(name="max")
	public Float getMax() {
		if (max == null) {
			return Float.NaN;
		} else {
			return max;
		}
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
	@Column(name="average_avg")
	public Float getAverageAvg() {
		if (averageAvg == null) {
			return Float.NaN;
		} else {
			return averageAvg;
		}
	}
	public void setAverageAvg(Float averageAvg) {
		this.averageAvg = averageAvg;
	}
	@Column(name="average_count")
	public Integer getAverageCount() {
		return averageCount;
	}
	public void setAverageCount(Integer averageCount) {
		this.averageCount = averageCount;
	}
	@Column(name="standard_deviation_avg")
	public Float getStandardDeviationAvg() {
		if (standardDeviationAvg == null) {
			return Float.NaN;
		} else {
			return standardDeviationAvg;
		}
	}
	public void setStandardDeviationAvg(Float standardDeviationAvg) {
		this.standardDeviationAvg = standardDeviationAvg;
	}
	@Column(name="standard_deviation_count")
	public Integer getStandardDeviationCount() {
		return standardDeviationCount;
	}
	public void setStandardDeviationCount(Integer standardDeviationCount) {
		this.standardDeviationCount = standardDeviationCount;
	}
	
	@Override
	public SummaryMonth clone() {
			SummaryMonth summarymonth = null;
			try {
				summarymonth = (SummaryMonth)super.clone();
			} catch (CloneNotSupportedException e) {
				m_log.debug("SummaryMonth.clone() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			}
		return summarymonth;
	}
	
}