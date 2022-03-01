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
import org.openapitools.client.model.CalendarDetailInfoResponse;
import org.openapitools.client.model.CalendarDetailInfoResponse.DayTypeEnum;

import com.clustercontrol.calendar.action.GetCalendarDetailTableDefine;
import com.clustercontrol.calendar.bean.OperateConstant;

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

		if (element instanceof CalendarDetailInfoResponse) {
			CalendarDetailInfoResponse detailInfo = (CalendarDetailInfoResponse) element;

			if (columnIndex == GetCalendarDetailTableDefine.RULE) {
				if(detailInfo.getMonthNo() != null){
					return Integer.toString(detailInfo.getMonthNo());
				}
			}
			else if (columnIndex == GetCalendarDetailTableDefine.TIME){
				if(detailInfo.getDayType() != null){
					return Integer.toString(getDayTypeToInt(detailInfo.getDayType()));
				}
			}
			else if (columnIndex == GetCalendarDetailTableDefine.OPERATE_FLG){
				if(detailInfo.getExecuteFlg() != null){
					return OperateConstant.typeToString(OperateConstant.booleanToType(detailInfo.getExecuteFlg()));
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
	
	/**
	 *  Enum から数値を得る
	 */
	private int getDayTypeToInt(DayTypeEnum e) {
		switch (e) {
		case ALL_DAY:
			return 0;
		case DAY_OF_WEEK:
			return 1;
		case DAY:
			return 2;
		case CALENDAR_PATTERN:
			return 3;
		default:
			return 0;
		}
	}
}
