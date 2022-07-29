/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.viewer;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.openapitools.client.model.JobDetailInfoResponse;
import org.openapitools.client.model.JobHistoryResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobParameterInfoResponse;

import com.clustercontrol.bean.CheckBoxImageConstant;
import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.EndStatusImageConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.bean.JobImageConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.ProcessMessage;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.bean.YesNoMessage;
import com.clustercontrol.jobmanagement.JobMessage;
import com.clustercontrol.jobmanagement.bean.DecisionObjectMessage;
import com.clustercontrol.jobmanagement.bean.JobParamTypeMessage;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectMessage;
import com.clustercontrol.jobmanagement.bean.ScheduleOnOffImageConstant;
import com.clustercontrol.jobmanagement.bean.StatusImageConstant;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.monitor.bean.ConfirmMessage;
import com.clustercontrol.notify.util.NotifyTypeUtil;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.viewer.ICommonTableLabelProvider;

/**
 * CommonTableViewerクラス用のLabelProviderクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobTableTreeLabelProvider extends LabelProvider implements ICommonTableLabelProvider {
	private JobTableTreeViewer m_viewer;

	/**
	 * コンストラクタ
	 * 
	 * アイコンイメージを取得
	 * 
	 * @param viewer
	 * @since 1.0.0
	 */
	public JobTableTreeLabelProvider(JobTableTreeViewer viewer) {
		m_viewer = viewer;
	}

	/**
	 * カラム文字列取得処理
	 * 
	 * @since 1.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	@Override
	public String getColumnText(Object value, int columnIndex) {
		ArrayList<TableColumnInfo> tableColumnList = m_viewer.getTableColumnList();

		TableColumnInfo tableColumn = tableColumnList.get(columnIndex);

		if (value == null) {
			return "";
		}

		if (tableColumn.getType() == TableColumnInfo.JOB) {
			//データタイプが「ジョブ」の処理
			if(value instanceof JobInfoWrapper.TypeEnum){
				return JobMessage.typeEnumValueToString(((JobInfoWrapper.TypeEnum) value).getValue());
			}
			if(value instanceof JobHistoryResponse.JobTypeEnum){
				return JobMessage.typeEnumValueToString(((JobHistoryResponse.JobTypeEnum) value).getValue());
			}
			return JobMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.STATE) {
			//データタイプが「状態」の処理
			if (value instanceof JobHistoryResponse.StatusEnum) {
				return StatusMessage.typeEnumValueToString(((JobHistoryResponse.StatusEnum) value).getValue());
			}
			if (value instanceof JobDetailInfoResponse.StatusEnum) {
				return StatusMessage.typeEnumValueToString(((JobDetailInfoResponse.StatusEnum) value).getValue());
			}
			
			return StatusMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.PRIORITY) {
			//データタイプが「重要度」の処理
			return PriorityMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.VALID) {
			//データタイプが「有効/無効」の処理
			return ValidMessage.typeToString(((Boolean) value).booleanValue());
		} else if (tableColumn.getType() == TableColumnInfo.JUDGMENT_OBJECT) {
			//データタイプが「判定対象」の処理
			if( value instanceof JobObjectInfoResponse.TypeEnum ){
				return JudgmentObjectMessage.enumToString((JobObjectInfoResponse.TypeEnum) value);
			} else if ( value instanceof String ){
				// 待ち条件群タイトル
				return (String)value;
			} else {
				return JudgmentObjectMessage.typeToString(((Number) value).intValue());
			}
		} else if (tableColumn.getType() == TableColumnInfo.NOTIFY_TYPE) {
			//データタイプが「判定対象」の処理
			return NotifyTypeUtil.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.WAIT_RULE_VALUE) {
			//データタイプが「開始条件値」の処理
			Class<?> itemClass = value.getClass();

			if (itemClass == Date.class) {
				//表示形式を0時未満および24時(及び48時)超にも対応する
				return TimeStringConverter.formatTime((Date)value);
			} else if (itemClass == Long.class) {
				//表示形式を0時未満および24時(及び48時)超にも対応する
				return TimeStringConverter.formatTime(new Date(((Long)value).longValue()));
			} else if (itemClass == String.class) {
				return String.valueOf(value);
			} else if (itemClass.getSuperclass() == Number.class) {
				return ((Number) value).toString();
			}
		} else if (tableColumn.getType() == TableColumnInfo.CONFIRM) {
			//データタイプが「確認/未確認」の処理
			return ConfirmMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.WAIT_RULE) {
			//データタイプが「待ち条件」の処理
			return YesNoMessage.typeToString(((Boolean) value).booleanValue());
		} else if (tableColumn.getType() == TableColumnInfo.PROCESS) {
			//データタイプが「処理」の処理
			return ProcessMessage.typeToString(((Boolean) value).booleanValue());
		} else if (tableColumn.getType() == TableColumnInfo.END_STATUS) {
			//データタイプが「終了状態」の処理
			if(value instanceof JobHistoryResponse.EndStatusEnum){
				return EndStatusMessage.typeEnumValueToString(((JobHistoryResponse.EndStatusEnum) value).getValue() );
			}
			if(value instanceof JobDetailInfoResponse.EndStatusEnum){
				return EndStatusMessage.typeEnumValueToString(((JobDetailInfoResponse.EndStatusEnum) value).getValue() );
			}
			return EndStatusMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.CHECKBOX) {
			//データタイプが「チェックボックス」の処理
			return "";
		} else if (tableColumn.getType() == TableColumnInfo.DAY_OF_WEEK) {
			//データタイプが「曜日」の処理
			return DayOfWeekConstant.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.SCHEDULE_ON_OFF) {
			//データタイプが「予定」の処理
			return "";
		} else if (tableColumn.getType() == TableColumnInfo.JOB_PARAM_TYPE) {
			//データタイプが「ジョブパラメータ種別」の処理
			if(value instanceof JobParameterInfoResponse.TypeEnum){
				return JobParamTypeMessage.typeEnumToString((JobParameterInfoResponse.TypeEnum)value);
			}
			return JobParamTypeMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.DECISION_CONDITION) {
			//データタイプが「判定条件」の処理
			return DecisionObjectMessage.typeEnumToString((JobObjectInfoResponse.DecisionConditionEnum) value);
		} else {
			//上記以外のデータタイプの処理
			Class<?> itemClass = value.getClass();

			if (itemClass == String.class) {
				return String.valueOf(value);
			} else if (itemClass == Date.class) {
				return TimezoneUtil.getSimpleDateFormat().format((Date) value);
			} else if (itemClass == Time.class) {
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				formatter.setTimeZone(TimezoneUtil.getTimeZone());
				return formatter.format((Time) value);
			} else if (itemClass.getSuperclass() == Number.class) {
				return ((Number) value).toString();
			}
		}
		return "";
	}

	/**
	 * カラムイメージ(アイコン)取得処理
	 * 
	 * @since 1.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	@Override
	public Image getColumnImage(Object value, int columnIndex) {
		ArrayList<TableColumnInfo> tableColumnList = m_viewer.getTableColumnList();

		TableColumnInfo tableColumn = tableColumnList
				.get(columnIndex);

		if (value == null || value.equals("")) {
			return null;
		}

		if (tableColumn.getType() == TableColumnInfo.JOB) {
			//データタイプが「ジョブ」の処理
			if(value instanceof JobInfoWrapper.TypeEnum){
				return JobImageConstant.typeEnumValueToImage((((JobInfoWrapper.TypeEnum) value).getValue()));
			}
			if(value instanceof JobHistoryResponse.JobTypeEnum){
				return JobImageConstant.typeEnumValueToImage((((JobHistoryResponse.JobTypeEnum) value).getValue()));
			}
			return JobImageConstant.typeToImage(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.FACILITY) {
			//データタイプが「ファシリティ」の処理
			Pattern p = Pattern.compile(".*>");
			Matcher m = p.matcher((String) value);
			if (m.matches()) {
				return FacilityImageConstant
						.typeToImage(FacilityConstant.TYPE_SCOPE, true);
			} else {
				return FacilityImageConstant
						.typeToImage(FacilityConstant.TYPE_NODE, true);
			}
		} else if (tableColumn.getType() == TableColumnInfo.STATE) {
			//データタイプが「状態」の処理
			if(value instanceof JobHistoryResponse.StatusEnum){
				return StatusImageConstant.typeEnumValueToImage(((JobHistoryResponse.StatusEnum) value).getValue() );
			}
			if(value instanceof JobDetailInfoResponse.StatusEnum){
				return StatusImageConstant.typeEnumValueToImage(((JobDetailInfoResponse.StatusEnum) value).getValue() );
			}
			return StatusImageConstant.typeToImage(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.END_STATUS) {
			//データタイプが「終了状態」の処理
			if(value instanceof JobHistoryResponse.EndStatusEnum){
				return EndStatusImageConstant.typeEnumValueToImage(((JobHistoryResponse.EndStatusEnum) value).getValue() );
			}
			if(value instanceof JobDetailInfoResponse.EndStatusEnum){
				return EndStatusImageConstant.typeEnumValueToImage(((JobDetailInfoResponse.EndStatusEnum) value).getValue() );
			}
			return EndStatusImageConstant.typeToImage(((Number) value)
					.intValue());
		} else if (tableColumn.getType() == TableColumnInfo.CHECKBOX) {
			//データタイプが「チェックボックス」の処理
			return CheckBoxImageConstant.typeToImage(((Boolean) value));
		} else if (tableColumn.getType() == TableColumnInfo.SCHEDULE_ON_OFF) {
			//データタイプが「予定」の処理
			return ScheduleOnOffImageConstant.dateToImage(((Date) value));
		}

		return null;
	}

	/**
	 * カラムカラー取得処理
	 * 
	 * @since 1.0.0
	 * @see com.clustercontrol.viewer.ICommonTableLabelProvider#getColumnColor(java.lang.Object,
	 *      int)
	 */
	@Override
	public Color getColumnColor(Object element, int columnIndex) {
		return null;
	}
}
