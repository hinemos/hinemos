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

package com.clustercontrol.snmptrap.composite;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.snmptrap.action.GetTrapDefineTableDefine;
import com.clustercontrol.ws.monitor.TrapValueInfo;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class TrapDefineTableLabelProvider extends CommonTableLabelProvider<TrapValueInfo> {

	public TrapDefineTableLabelProvider(ITableItemCompositeDefine<TrapValueInfo> define) {
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

		if (element instanceof TrapValueInfo) {
			TrapValueInfo info = (TrapValueInfo) element;
			if (columnIndex == GetTrapDefineTableDefine.MIB) {
				return info.getMib();
			} else if (columnIndex == GetTrapDefineTableDefine.TRAP_NAME) {
				return info.getUei();
			} else if (columnIndex == GetTrapDefineTableDefine.VERSION) {
				return SnmpVersionConstant.typeToString(info.getVersion());
			} else if (columnIndex == GetTrapDefineTableDefine.TRAP_OID) {
				return info.getTrapOid();
			} else if (columnIndex == GetTrapDefineTableDefine.GENERIC_ID) {
				if(info.getVersion() == SnmpVersionConstant.TYPE_V1 && info.getGenericId() != null){
					return info.getGenericId().toString();
				}
			} else if (columnIndex == GetTrapDefineTableDefine.SPECIFIC_ID) {
				if(info.getVersion() == SnmpVersionConstant.TYPE_V1 && info.getSpecificId() != null){
					return info.getSpecificId().toString();
				}
			} else if (columnIndex == GetTrapDefineTableDefine.VALID_FLG) {
				return ValidMessage.typeToString(info.isValidFlg());
			} else if (columnIndex == GetTrapDefineTableDefine.VARIABLE) {
				return ValidMessage.typeToString(info.isProcessingVarbindSpecified());
			} else if (columnIndex == GetTrapDefineTableDefine.MESSAGE) {
				return info.getLogmsg();
			}
		}
		return "";
	}
}
