/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * The primary key class for the cc_rest_access_send_http_header database table.
 * 
 */
@Embeddable
public class RestAccessAuthHttpHeaderPK  implements Serializable {
	private static final long serialVersionUID = 1L;

	private String restAccessId ;
	private Long headerOrderNo;

	public RestAccessAuthHttpHeaderPK() {
		super();
	}

	@Column(name="rest_access_id")
	public String getRestAccessId() {
		return restAccessId;
	}
	public void setRestAccessId(String restAccessId) {
		this.restAccessId = restAccessId;
	}

	@Column(name="header_order_no")
	public Long getHeaderOrderNo() {
		return headerOrderNo;
	}
	public void setHeaderOrderNo(Long headerOrderNo) {
		this.headerOrderNo = headerOrderNo;
	}

	// Mapのキーとして利用するので、内容値によって同一性が検証できるようにhashCodeとequalsを上書き
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headerOrderNo == null) ? 0 : headerOrderNo.hashCode());
		result = prime * result + ((restAccessId == null) ? 0 : restAccessId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestAccessAuthHttpHeaderPK other = (RestAccessAuthHttpHeaderPK) obj;
		if (headerOrderNo == null) {
			if (other.headerOrderNo != null)
				return false;
		} else if (!headerOrderNo.equals(other.headerOrderNo))
			return false;
		if (restAccessId == null) {
			if (other.restAccessId != null)
				return false;
		} else if (!restAccessId.equals(other.restAccessId))
			return false;
		return true;
	}

}
