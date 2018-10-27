/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.composite;

import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.ProcessMessage;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.snmptrap.action.GetVarBindPatternTableDefine;
import com.clustercontrol.ws.monitor.VarBindPattern;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class VarBindPatternTableLabelProvider extends CommonTableLabelProvider<VarBindPattern> {

	public VarBindPatternTableLabelProvider(ITableItemCompositeDefine<VarBindPattern> define) {
		super(define);
	}

	/**
	 * カラム文字列を返します。
	 * 
	 * @since 2.1.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof VarBindPattern) {
			VarBindPattern pattern = (VarBindPattern) element;
			if (columnIndex == GetVarBindPatternTableDefine.ORDER_NO) {
				return String.valueOf(indexOf(pattern) + 1);
			} else if (columnIndex == GetVarBindPatternTableDefine.PROCESS_TYPE) {
				return ProcessMessage.typeToString(pattern.isProcessType());
			} else if (columnIndex == GetVarBindPatternTableDefine.PRIORITY) {
				return PriorityMessage.typeToString(pattern.getPriority());
			} else if (columnIndex == GetVarBindPatternTableDefine.PATTERN_STRING) {
				if (pattern.getPattern() != null) {
					return pattern.getPattern();
				}
			} else if (columnIndex == GetVarBindPatternTableDefine.DESCRIPTION) {
				if (pattern.getDescription() != null) {
					return pattern.getDescription();
				}
			} else if (columnIndex == GetVarBindPatternTableDefine.VALID_FLG) {
				return ValidMessage.typeToString(pattern.isValidFlg());
			}
		}
		return "";
	}
}
