/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_cal_detail_info database table.
 * 
 */
@Embeddable
public class TemplateSetDetailInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String templateSetId;
	private Integer orderNo;

	public TemplateSetDetailInfoEntityPK() {
	}

	public TemplateSetDetailInfoEntityPK(String templateSetId, Integer orderNo) {
		this.setTemplateSetId(templateSetId);
		this.setOrderNo(orderNo);
	}

	@Column(name="template_set_id")
	public String getTemplateSetId() {
		return this.templateSetId;
	}
	public void setTemplateSetId(String templateSetId) {
		this.templateSetId = templateSetId;
	}

	@Column(name="order_no")
	public Integer getOrderNo(){
		return this.orderNo;
	}
	public void setOrderNo(Integer orderNo){
		this.orderNo = orderNo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TemplateSetDetailInfoEntityPK)) {
			return false;
		}
		TemplateSetDetailInfoEntityPK castOther = (TemplateSetDetailInfoEntityPK)other;
		return
				this.templateSetId.equals(castOther.templateSetId)
				&& this.orderNo.equals(castOther.orderNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.templateSetId.hashCode();
		hash = hash * prime + this.orderNo.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"templateSetId",
				"orderNo"
		};
		String[] values = {
				this.templateSetId,
				this.orderNo.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}