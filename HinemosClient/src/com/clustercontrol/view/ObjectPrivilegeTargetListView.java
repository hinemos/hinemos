/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
