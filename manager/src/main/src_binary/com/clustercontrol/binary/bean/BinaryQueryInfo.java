/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

import com.clustercontrol.hub.bean.StringQueryInfo;

/**
 * バイナリ収集の検索条件を格納するBean.<br>
 * <br>
 * 文字列収集の検索条件をベースにバイナリ独自項目を本クラスで追加.
 *
 * @version 6.1.0
 * @since 6.1.0
 * @see com.clustercontrol.hub.bean.StringQueryInfo
 */
public class BinaryQueryInfo extends StringQueryInfo {

	private String textEncoding;

	/**
	 * 文字列エンコーディング取得
	 * 
	 * @return the textEncoding
	 */
	public String getTextEncoding() {
		return this.textEncoding;
	}

	/**
	 * 文字列エンコーディング設定
	 * 
	 * @param hexStr
	 *            the textEncoding to set
	 */
	public void setTextEncoding(String textEncoding) {
		this.textEncoding = textEncoding;
	}

	@Override
	public String toString() {
		return "BinaryQueryInfo [from=" + super.getFrom() + ", to=" + super.getTo() + ", monitorId="
				+ super.getMonitorId() + ", facilityId=" + super.getFacilityId() + ", keywords=" + super.getKeywords()
				+ ", operator=" + super.getOperator() + ", offset=" + super.getOffset() + ", size=" + super.getSize()
				+ ", needCount=" + super.isNeedCount() + ", textEncoding=" + this.textEncoding + "]";
	}
}
