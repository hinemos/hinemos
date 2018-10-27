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

import com.clustercontrol.bean.CheckBoxImageConstant;
import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.EndStatusImageConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.bean.JobImageConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.ProcessMessage;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.bean.YesNoMessage;
import com.clustercontrol.jobmanagement.JobMessage;
import com.clustercontrol.jobmanagement.action.GetJobDetailTableDefine;
import com.clustercontrol.jobmanagement.bean.DecisionObjectMessage;
import com.clustercontrol.jobmanagement.bean.JobParamTypeMessage;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectMessage;
import com.clustercontrol.jobmanagement.bean.ScheduleOnOffImageConstant;
import com.clustercontrol.jobmanagement.bean.StatusImageConstant;
import com.clustercontrol.jobmanagement.util.TimeToANYhourConverter;
import com.clustercontrol.monitor.bean.ConfirmMessage;
import com.clustercontrol.notify.util.NotifyTypeUtil;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.viewer.ICommonTableLabelProvider;
import com.clustercontrol.ws.common.Schedule;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

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

	private Object getValue(JobTreeItem item, int columnIndex) {

		Object value = null;
		if (columnIndex == GetJobDetailTableDefine.TREE) {
			value = "";
		} else if (columnIndex == GetJobDetailTableDefine.STATUS) {
			value = item.getDetail().getStatus();
		} else if (columnIndex == GetJobDetailTableDefine.END_STATUS) {
			value = item.getDetail().getEndStatus();
		} else if (columnIndex == GetJobDetailTableDefine.END_VALUE) {
			value = item.getDetail().getEndValue();
		} else if (columnIndex == GetJobDetailTableDefine.JOB_ID) {
			value = item.getData().getId();
		} else if (columnIndex == GetJobDetailTableDefine.JOB_NAME) {
			value = item.getData().getName();
		} else if (columnIndex == GetJobDetailTableDefine.JOBUNIT_ID) {
			value = item.getData().getJobunitId();
		} else if (columnIndex == GetJobDetailTableDefine.JOB_TYPE) {
			value = item.getData().getType();
		} else if (columnIndex == GetJobDetailTableDefine.FACILITY_ID) {
			value = item.getDetail().getFacilityId();
		} else if (columnIndex == GetJobDetailTableDefine.SCOPE) {
			value = HinemosMessage.replace(item.getDetail().getScope());
		} else if (columnIndex == GetJobDetailTableDefine.WAIT_RULE_TIME) {
			value = item.getDetail().getWaitRuleTime();
		} else if (columnIndex == GetJobDetailTableDefine.START_RERUN_TIME) {
			if (item.getDetail().getStartDate() != null) {
				value = new Date(item.getDetail().getStartDate());
			} else {
				value = "";
			}
		} else if (columnIndex == GetJobDetailTableDefine.END_SUSPEND_TIME) {
			if (item.getDetail().getEndDate() != null) {
				value = new Date(item.getDetail().getEndDate());
			} else {
				value = "";
			}
		} else if (columnIndex == GetJobDetailTableDefine.SESSION_TIME) {
			value = TimeToANYhourConverter.toDiffTime(item.getDetail().getStartDate(), item.getDetail().getEndDate());
		} else if (columnIndex == GetJobDetailTableDefine.RUN_COUNT) {
			value = item.getDetail().getRunCount();
		} else {
			value = "";
		}
		return value;
	}
	/**
	 * カラム文字列取得処理
	 * 
	 * @since 1.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		ArrayList<TableColumnInfo> tableColumnList = m_viewer.getTableColumnList();

		JobTreeItem item = (JobTreeItem) element;
		Object value = getValue(item, columnIndex);


		TableColumnInfo tableColumn = tableColumnList.get(columnIndex);

		if (value == null) {
			return "";
		}

		if (tableColumn.getType() == TableColumnInfo.JOB) {
			//データタイプが「ジョブ」の処理
			return JobMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.STATE) {
			//データタイプが「状態」の処理
			return StatusMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.PRIORITY) {
			//データタイプが「重要度」の処理
			return PriorityMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.VALID) {
			//データタイプが「有効/無効」の処理
			return ValidMessage.typeToString(((Boolean) value).booleanValue());
		} else if (tableColumn.getType() == TableColumnInfo.JUDGMENT_OBJECT) {
			//データタイプが「判定対象」の処理
			return JudgmentObjectMessage.typeToString(((Number) value).intValue());
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
		} else if (tableColumn.getType() == TableColumnInfo.SCHEDULE) {
			//データタイプが「スケジュール」の処理
			Schedule schedule = (Schedule) value;

			String scheduleString = null;
			//データタイプが「スケジュール」の処理
			if (schedule.getType() == ScheduleConstant.TYPE_DAY) {
				if (schedule.getMonth() != null) {
					scheduleString = schedule.getMonth() +
							"/" + schedule.getDay() + " " +
							schedule.getHour() + ":" + schedule.getMinute();
				} else if (schedule.getDay() != null){
					scheduleString = schedule.getDay() +
							"'" + Messages.getString("monthday") + "'" +
							schedule.getHour() + ":" + schedule.getMinute();
				} else if (schedule.getHour() != null) {
					scheduleString = schedule.getHour() + ":" + schedule.getMinute();
				} else if (schedule.getMinute() != null) {
					scheduleString = schedule.getMinute() + "'" +
							Messages.getString("minute") + "'";
				}
			} else {
				scheduleString = DayOfWeekConstant.typeToString(schedule.getWeek()) +
						" " + schedule.getHour() + ":" + schedule.getMinute();
			}
			return scheduleString;
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
			return JobParamTypeMessage.typeToString(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.DECISION_CONDITION) {
			//データタイプが「判定条件」の処理
			return DecisionObjectMessage.typeToString(((Number) value).intValue());
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
	public Image getColumnImage(Object element, int columnIndex) {
		ArrayList<TableColumnInfo> tableColumnList = m_viewer.getTableColumnList();

		JobTreeItem item = (JobTreeItem) element;
		Object value = getValue(item, columnIndex);


		TableColumnInfo tableColumn = tableColumnList
				.get(columnIndex);

		if (value == null || value.equals("")) {
			return null;
		}

		if (tableColumn.getType() == TableColumnInfo.JOB) {
			//データタイプが「ジョブ」の処理
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
			return StatusImageConstant.typeToImage(((Number) value).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.END_STATUS) {
			//データタイプが「終了状態」の処理
			return EndStatusImageConstant.typeToImage(((Number) value)
					.intValue());
		} else if (tableColumn.getType() == TableColumnInfo.CHECKBOX) {
			//データタイプが「チェックボックス」の処理
			return CheckBoxImageConstant.typeToImage(((Boolean) value)
					.booleanValue());
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
