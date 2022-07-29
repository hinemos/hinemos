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
 * RPAシナリオジョブ（直接実行）の実行オプションに関する情報を保持するクラス
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class RpaJobOptionInfo implements Serializable, RequestDto, Comparable<RpaJobOptionInfo> {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 1L;
	/** 順序 */
	private Integer orderNo;
	/** オプション */
	private String option;
	/** 説明 */
	private String description;

	/**
	 * @return 順序を返します。
	 */
	public Integer getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo
	 *            順序を設定します。
	 */
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	/**
	 * @return オプションを返します。
	 */
	public String getOption() {
		return option;
	}

	/**
	 * @param option
	 *            オプションを設定します。
	 */
	public void setOption(String option) {
		this.option = option;
	}

	/**
	 * @return 説明を返します。
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            説明を設定します。
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((option == null) ? 0 : option.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		RpaJobOptionInfo other = (RpaJobOptionInfo) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (option == null) {
			if (other.option != null)
				return false;
		} else if (!option.equals(other.option))
			return false;
		return true;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	@Override
	public int compareTo(RpaJobOptionInfo o) {
		return this.getOrderNo() - o.getOrderNo();
	}
}
