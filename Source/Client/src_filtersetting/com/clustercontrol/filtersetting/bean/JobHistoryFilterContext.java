/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.bean;

import java.util.Objects;

import org.openapitools.client.model.JobHistoryFilterBaseRequest;

/**
 * ジョブ実行履歴フィルタ条件の関連情報をセットで扱うためのクラスです。
 * フィルタ条件の入力を開始したときに生成され、複数のGUI部品間で共有・更新されます。
 */
public class JobHistoryFilterContext {

	private JobHistoryFilterBaseRequest filter;
	private String managerName;

	/**
	 * コンストラクタ。
	 * 
	 * @param filter
	 * 		フィルタ条件の初期値です。
	 * 		GUI操作により随時更新されます。
	 * @param managerName 
	 * 		フィルタ条件の対象となるマネージャの名前です。
	 * 		全ての接続マネージャの場合は null を指定します。
	 */
	public JobHistoryFilterContext(
			JobHistoryFilterBaseRequest filter,
			String managerName) {
		Objects.requireNonNull(filter, "filter");

		this.filter = filter;
		this.managerName = managerName;
	}

	/**
	 * フィルタ条件を返します。
	 */
	public JobHistoryFilterBaseRequest getFilter() {
		return filter;
	}

	/**
	 * フィルタ条件を設定します。
	 */
	public void setFilter(JobHistoryFilterBaseRequest filter) {
		Objects.requireNonNull(filter, "filter");

		this.filter = filter;
	}

	/**
	 * フィルタ条件の対象となるマネージャの名前を設定します。
	 * 全ての接続マネージャの場合は null を指定します。
	 */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	/**
	 * フィルタ条件の対象となるマネージャの名前を返します。
	 * 全ての接続マネージャの場合は null です。
	 */
	public String getManagerName() {
		return managerName;
	}

}
