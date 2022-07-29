/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.view;

import java.util.List;

import com.clustercontrol.accesscontrol.util.ObjectBean;


/**
 * オブジェクト権限設定対象一覧ビューInterface<BR>
 * 
 * @version 4.1.0
 */
public interface ObjectPrivilegeTargetListView {

	/**
	 * ビュー更新
	 */
	void update();

	/**
	 * 選択した項目を返す
	 * 
	 * @return 選択されたオブジェクト種別、オブジェクトIDのリスト
	 */
	List<ObjectBean> getSelectedObjectBeans();

	/**
	 * 選択した項目のオーナーロールIDを返す
	 * 
	 * @return 選択された項目のオーナーロールID
	 */
	String getSelectedOwnerRoleId();

}
