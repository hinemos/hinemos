/*
 * Copyright (c) 2023 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.io.Serializable;
import java.util.List;

/**
 * ステータス一覧情報を保持するクラス<BR>
 */
public class ViewStatusListInfo implements Serializable {

	private static final long serialVersionUID = 5500435643773316178L;

	/**
	 * ステータス一覧
	 * （件数制限あり）
	 */
	private List<StatusDataInfo> statusList;
	
	/**
	 * 全てのステータス一覧を取得した場合のレコード数
	 */
	private int countAll;

	public List<StatusDataInfo> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<StatusDataInfo> statusList) {
		this.statusList = statusList;
	}

	public int getCountAll() {
		return countAll;
	}

	public void setCountAll(int countAll) {
		this.countAll = countAll;
	}
}
