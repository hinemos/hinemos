/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.viewer;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.calendar.action.GetCalendarDetailTableDefine;
import com.clustercontrol.calendar.bean.OperateConstant;
import com.clustercontrol.ws.calendar.CalendarDetailInfo;

/**
 * カレンダ情報設定ダイアログ内
 * カレンダ詳細情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 3.0.0
 * @since 2.1.0
 */
public class CalendarDetailListTableLabelProvider extends LabelProvider implements ITableLabelProvider {

	/**
	 * カラム文字列を返します。
	 * 
	 * @since 2.1.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof CalendarDetailInfo) {
			CalendarDetailInfo detailInfo = (CalendarDetailInfo) element;

			if (columnIndex == GetCalendarDetailTableDefine.RULE) {
				if(detailInfo.getMonth() != null){
					return Integer.toString(detailInfo.getMonth());
				}
			}
			else if (columnIndex == GetCalendarDetailTableDefine.TIME){
				if(detailInfo.getDayType() != null){
					return Integer.toString(detailInfo.getDayType());
				}
			}
			else if (columnIndex == GetCalendarDetailTableDefine.OPERATE_FLG){
				if(detailInfo.isOperateFlg() != null){
					return OperateConstant.typeToString(OperateConstant.booleanToType(detailInfo.isOperateFlg()));
				}
			}
			else if (columnIndex == GetCalendarDetailTableDefine.DESCRIPTION){
				if(detailInfo.getDescription() != null){
					return detailInfo.getDescription();
				}
			}
		}
		return "";
	}

	/**
	 * カラムイメージ(アイコン)を返します。
	 * 
	 * @since 2.1.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
