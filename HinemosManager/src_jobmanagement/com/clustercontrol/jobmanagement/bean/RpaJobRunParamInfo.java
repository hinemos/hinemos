/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * RPAシナリオジョブ（間接実行）の起動パラメータに関する情報を保持するクラス
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class RpaJobRunParamInfo implements Serializable, RequestDto, Comparable<RpaJobRunParamInfo> {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 1L;
	/** パラメータID */
	private Integer paramId;
	/** パラメータの値 */
	private String paramValue;

	/**
	 * @return パラメータIDを返します。
	 */
	public Integer getParamId() {
		return paramId;
	}

	/**
	 * @param paramId
	 *            パラメータIDを設定します。
	 */
	public void setParamId(Integer paramId) {
		this.paramId = paramId;
	}

	/**
	 * @return パラメータの値を返します。
	 */
	public String getParamValue() {
		return paramValue;
	}

	/**
	 * @param paramValue
	 *            パラメータの値を設定します。
	 */
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramId == null) ? 0 : paramId.hashCode());
		result = prime * result + ((paramValue == null) ? 0 : paramValue.hashCode());
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
		RpaJobRunParamInfo other = (RpaJobRunParamInfo) obj;
		if (paramId == null) {
			if (other.paramId != null)
				return false;
		} else if (!paramId.equals(other.paramId))
			return false;
		if (paramValue == null) {
			if (other.paramValue != null)
				return false;
		} else if (!paramValue.equals(other.paramValue))
			return false;
		return true;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	@Override
	public int compareTo(RpaJobRunParamInfo o) {
		return this.getParamId() - o.getParamId();
	}
}
