/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.bean;

import org.openapitools.client.model.StatusFilterBaseRequest;

/**
 * ステータス通知結果フィルタ条件の関連情報をセットで扱うためのクラスです。
 * フィルタ条件の入力を開始したときに生成され、複数のGUI部品間で共有・更新されます。
 */
public class StatusFilterContext {

	private StatusFilterBaseRequest filter;
	private String managerName;
	private String scopeHintMessage;

	/**
	 * コンストラクタ。
	 * 
	 * @param filter
	 * 		フィルタ条件の初期値です。
	 * 		GUI操作により随時更新されます。
	 * @param managerName 
	 * 		フィルタ条件の対象となるマネージャの名前です。
	 * 		特定マネージャではない場合は null を指定します。
	 * @param scopeHintMessage
	 * 		スコープ入力欄へヒント表示する文字列です。
	 */
	public StatusFilterContext(
			StatusFilterBaseRequest filter,
			String managerName,
			String scopeHintMessage) {
		requireScopeConstraint(filter, managerName);

		this.filter = filter;
		this.managerName = managerName;
		this.scopeHintMessage = scopeHintMessage;
	}

	/**
	 * スコープ指定の制約 (フィルタ条件でスコープ指定する場合は、特定マネージャが選択されている必要がある) をチェックし、
	 * 違反している場合は例外を投げます。
	 */
	private void requireScopeConstraint(StatusFilterBaseRequest filter, String managerName) {
		if (filter.getFacilityId() != null && managerName == null) {
			throw new IllegalStateException("managerName must not null.");
		}
	}

	/**
	 * フィルタ条件を返します。
	 */
	public StatusFilterBaseRequest getFilter() {
		return filter;
	}

	/**
	 * フィルタ条件を設定します。<br/>
	 * フィルタ条件の対象となるマネージャの名前も、同時に設定する必要があります。
	 * 特定マネージャではない場合は、マネージャ名に null を指定します。
	 */
	public void setFilter(StatusFilterBaseRequest filter, String managerName) {
		requireScopeConstraint(filter, managerName);

		this.managerName = managerName;
		this.filter = filter;
	}

	/**
	 * フィルタ条件の対象となるマネージャの名前を設定します。
	 * 特定マネージャではない場合は null を指定します。
	 */
	public void setManagerName(String managerName) {
		requireScopeConstraint(filter, managerName);

		this.managerName = managerName;
	}

	/**
	 * フィルタ条件の対象となるマネージャの名前を返します。
	 * 特定マネージャではない場合は null です。
	 */
	public String getManagerName() {
		return managerName;
	}

	/**
	 * スコープ入力欄へヒント表示する文字列を返します。
	 */
	public String getScopeHintMessage() {
		return scopeHintMessage;
	}

}
