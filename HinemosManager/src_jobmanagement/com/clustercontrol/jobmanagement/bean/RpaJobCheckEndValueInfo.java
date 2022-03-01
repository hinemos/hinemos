/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

/**
 * RPAシナリオジョブ（間接実行）の終了値判定条件に関する情報を保持するクラス
 */
public class RpaJobCheckEndValueInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;
	/** RPA管理ツール 終了状態ID */
	private Integer endStatusId;
	/** 終了値 */
	private Integer endValue;

	/**
	 * @return RPA管理ツール 終了状態IDを返します。
	 */
	public Integer getEndStatusId() {
		return endStatusId;
	}
	
	/**
	 * @param endStatusId
	 *            RPA管理ツール 終了状態IDを設定します。
	 */
	public void setEndStatusId(Integer endStatusId) {
		this.endStatusId = endStatusId;
	}
	
	/**
	 * @return 終了値を返します。
	 */
	public Integer getEndValue() {
		return endValue;
	}
	
	/**
	 * @param endValue
	 *            終了値を設定します。
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endStatusId == null) ? 0 : endStatusId.hashCode());
		result = prime * result + ((endValue == null) ? 0 : endValue.hashCode());
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
		RpaJobCheckEndValueInfo other = (RpaJobCheckEndValueInfo) obj;
		if (endStatusId == null) {
			if (other.endStatusId != null)
				return false;
		} else if (!endStatusId.equals(other.endStatusId))
			return false;
		if (endValue == null) {
			if (other.endValue != null)
				return false;
		} else if (!endValue.equals(other.endValue))
			return false;
		return true;
	}

}
