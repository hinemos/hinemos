/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.http.viewer;

import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.Variable;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class VariableTableLabelProvider extends CommonTableLabelProvider<Variable> {

	public VariableTableLabelProvider(ITableItemCompositeDefine<Variable> define) {
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

		if (element instanceof Variable) {
			Variable variable = (Variable) element;

			if (columnIndex == GetVariableTableDefine.NAME) {
				if(variable.getName() != null){
					return variable.getName();
				}
			} else if (columnIndex == GetVariableTableDefine.VALUE) {
				if (variable.getValue() != null) {
					return variable.getValue();
				}
			} else if (columnIndex == GetVariableTableDefine.MATCHING_WITH_RESPONSE) {
				if(variable.isMatchingWithResponseFlg()){
					return Messages.getString("monitor.http.scenario.page.obtain.from.current.page.valid");
				} else {
					return Messages.getString("monitor.http.scenario.page.obtain.from.current.page.invalid");
				}
			}
		}
		return "";
	}
}
