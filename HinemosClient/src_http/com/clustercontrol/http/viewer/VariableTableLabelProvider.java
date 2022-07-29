/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.viewer;

import org.openapitools.client.model.VariableResponse;

import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.util.Messages;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class VariableTableLabelProvider extends CommonTableLabelProvider<VariableResponse> {

	public VariableTableLabelProvider(ITableItemCompositeDefine<VariableResponse> define) {
		super(define);
	}

	/**
	 * カラム文字列を返します。
	 * 
	 * @since 5.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof VariableResponse) {
			VariableResponse variable = (VariableResponse) element;

			if (columnIndex == GetVariableTableDefine.NAME) {
				if(variable.getName() != null){
					return variable.getName();
				}
			} else if (columnIndex == GetVariableTableDefine.VALUE) {
				if (variable.getValue() != null) {
					return variable.getValue();
				}
			} else if (columnIndex == GetVariableTableDefine.MATCHING_WITH_RESPONSE) {
				if(variable.getMatchingWithResponseFlg()){
					return Messages.getString("monitor.http.scenario.page.obtain.from.current.page.valid");
				} else {
					return Messages.getString("monitor.http.scenario.page.obtain.from.current.page.invalid");
				}
			}
		}
		return "";
	}
}
