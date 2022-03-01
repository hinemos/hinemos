/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.composite;

import org.openapitools.client.model.TrapValueInfoResponse;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.snmptrap.action.GetTrapDefineTableDefine;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class TrapDefineTableLabelProvider extends CommonTableLabelProvider<TrapValueInfoResponse> {

	public TrapDefineTableLabelProvider(ITableItemCompositeDefine<TrapValueInfoResponse> define) {
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

		if (element instanceof TrapValueInfoResponse) {
			TrapValueInfoResponse info = (TrapValueInfoResponse) element;
			if (columnIndex == GetTrapDefineTableDefine.MIB) {
				return info.getMib();
			} else if (columnIndex == GetTrapDefineTableDefine.TRAP_NAME) {
				return info.getUei();
			} else if (columnIndex == GetTrapDefineTableDefine.VERSION) {
				if (info.getVersion() == TrapValueInfoResponse.VersionEnum.V1) {
					return SnmpVersionConstant.typeToString(SnmpVersionConstant.TYPE_V1);
				} else if (info.getVersion() == TrapValueInfoResponse.VersionEnum.V2C_V3) {
					return SnmpVersionConstant.typeToString(SnmpVersionConstant.TYPE_V2);
				}
			} else if (columnIndex == GetTrapDefineTableDefine.TRAP_OID) {
				return info.getTrapOid();
			} else if (columnIndex == GetTrapDefineTableDefine.GENERIC_ID) {
				if(info.getVersion() == TrapValueInfoResponse.VersionEnum.V1 && info.getGenericId() != null){
					return info.getGenericId().toString();
				}
			} else if (columnIndex == GetTrapDefineTableDefine.SPECIFIC_ID) {
				if(info.getVersion() == TrapValueInfoResponse.VersionEnum.V1 && info.getSpecificId() != null){
					return info.getSpecificId().toString();
				}
			} else if (columnIndex == GetTrapDefineTableDefine.VALID_FLG) {
				return ValidMessage.typeToString(info.getValidFlg());
			} else if (columnIndex == GetTrapDefineTableDefine.VARIABLE) {
				return ValidMessage.typeToString(info.getProcVarbindSpecified());
			} else if (columnIndex == GetTrapDefineTableDefine.MESSAGE) {
				return info.getLogmsg();
			}
		}
		return "";
	}
}
