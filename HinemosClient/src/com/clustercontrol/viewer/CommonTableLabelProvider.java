/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.viewer;

import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.bean.CheckBoxImageConstant;
import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.EndStatusImageConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.bean.JobImageConstant;
import com.clustercontrol.bean.PerformanceStatusImageConstant;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.ProcessMessage;
import com.clustercontrol.bean.RadioButtonImageConstant;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.bean.YesNoMessage;
import com.clustercontrol.jobmanagement.JobMessage;
import com.clustercontrol.jobmanagement.bean.DecisionObjectMessage;
import com.clustercontrol.jobmanagement.bean.JobApprovalResultImageConstant;
import com.clustercontrol.jobmanagement.bean.JobApprovalResultMessage;
import com.clustercontrol.jobmanagement.bean.JobApprovalStatusImageConstant;
import com.clustercontrol.jobmanagement.bean.JobApprovalStatusMessage;
import com.clustercontrol.jobmanagement.bean.JobParamTypeMessage;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeMessage;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectMessage;
import com.clustercontrol.jobmanagement.bean.ScheduleOnOffImageConstant;
import com.clustercontrol.jobmanagement.bean.StatusImageConstant;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
import com.clustercontrol.monitor.bean.ConfirmMessage;
import com.clustercontrol.notify.util.NotifyTypeUtil;
import com.clustercontrol.performance.bean.PerformanceStatusConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.NodeConfigRunInterval;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.ws.common.Schedule;

/**
 * CommonTableViewerクラス用のLabelProviderクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class CommonTableLabelProvider extends LabelProvider implements ICommonTableLabelProvider {
	// ログ
	private static Log m_log = LogFactory.getLog( CommonTableLabelProvider.class );

	private CommonTableViewer m_viewer;

	/**
	 * コンストラクタ
	 *
	 * アイコンイメージを取得
	 *
	 * @param viewer
	 * @since 1.0.0
	 */
	public CommonTableLabelProvider(CommonTableViewer viewer) {
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
	public String getColumnText(Object element, int columnIndex) {
		ArrayList<TableColumnInfo> tableColumnList = m_viewer.getTableColumnList();

		ArrayList<?> list = (ArrayList<?>) element;
		if (list.size() <= columnIndex) {
			m_log.debug("Bad implements. IndexOutOfBoundsException."
					+ " list.size=" + list.size() +
					", columnIndex=" + columnIndex);
			return "";
		}
		Object item = list.get(columnIndex);

		TableColumnInfo tableColumn = tableColumnList.get(columnIndex);

		if (item == null || item.equals("")) {
			return "";
		}

		switch(tableColumn.getType()){
		case TableColumnInfo.JOB:
			//データタイプが「ジョブ」の処理
			return JobMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.STATE:
			//データタイプが「状態」の処理
			return StatusMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.PRIORITY:
			//データタイプが「重要度」の処理
			return PriorityMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.VALID:
			//データタイプが「有効/無効」の処理
			return ValidMessage.typeToString(((Boolean) item).booleanValue());
		case TableColumnInfo.RUN_INTERVAL:
			//データタイプが「間隔」の処理
			int runInterval = (Integer)item;
			if(0 == runInterval){
				return "-";
			}else{
				RunInterval interval = RunInterval.valueOf(runInterval);
				if(interval != null){
					return interval.toString();
				} else{
					return NodeConfigRunInterval.valueOf(runInterval).toString();
				}
			}
		case TableColumnInfo.JUDGMENT_OBJECT:
			//データタイプが「判定対象」の処理
			return JudgmentObjectMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.NOTIFY_TYPE:
			//データタイプが「判定対象」の処理
			return NotifyTypeUtil.typeToString(((Number) item).intValue());
		case TableColumnInfo.WAIT_RULE_VALUE:
			//データタイプが「開始条件値」の処理
			Class<?> itemClass = item.getClass();
			if (itemClass == Date.class) {
				//表示形式を0時未満および24時(及び48時)超にも対応する
				return TimeStringConverter.formatTime((Date) item);
			} else if (itemClass == String.class) {
				return String.valueOf(item);
			} else if (itemClass.getSuperclass() == Number.class) {
				return ((Number) item).toString();
			}
		case TableColumnInfo.SCHEDULE:
			//データタイプが「スケジュール」の処理
			Schedule schedule = (Schedule) item;
			String scheduleString = null;
			DecimalFormat format = new DecimalFormat("00");
			if (schedule.getType() == ScheduleConstant.TYPE_DAY) {
				if (schedule.getMonth() != null) {
					scheduleString = format.format(schedule.getMonth()) +
							"/" + format.format(schedule.getDay()) + " " +
							format.format(schedule.getHour()) + ":" +
							format.format(schedule.getMinute());
				} else if (schedule.getDay() != null){
					scheduleString = format.format(schedule.getDay()) +
							Messages.getString("monthday") + " " +
							format.format(schedule.getHour()) + ":" +
							format.format(schedule.getMinute());
				} else if (schedule.getHour() != null) {
					scheduleString = format.format(schedule.getHour()) + ":" +
							format.format(schedule.getMinute());
				} else if (schedule.getMinute() != null) {
					scheduleString = format.format(schedule.getMinute()) +
							Messages.getString("minute");
				}
			} else if (schedule.getType() == ScheduleConstant.TYPE_WEEK){
				if (schedule.getHour() != null) {
					scheduleString = DayOfWeekConstant.typeToString(schedule.getWeek()) +
							" " + format.format(schedule.getHour()) + ":" +
							format.format(schedule.getMinute());
				} else {
					scheduleString = DayOfWeekConstant.typeToString(schedule.getWeek()) +
							" " + format.format(schedule.getMinute()) + Messages.getString("minute");
				}
			} else {
				// ここは通らないはず。
				m_log.warn("CommonTableLabelProvider 165");
			}
			return scheduleString;
		case TableColumnInfo.CONFIRM:
			//データタイプが「確認/未確認」の処理
			return ConfirmMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.WAIT_RULE:
			//データタイプが「待ち条件」の処理
			return YesNoMessage.typeToString(((Boolean) item).booleanValue());
		case TableColumnInfo.PROCESS:
			//データタイプが「処理」の処理
			return ProcessMessage.typeToString(((Boolean) item).booleanValue());
		case TableColumnInfo.END_STATUS:
			//データタイプが「終了状態」の処理
			return EndStatusMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.CHECKBOX:
			//データタイプが「チェックボックス」の処理
			return "";
		case TableColumnInfo.DAY_OF_WEEK:
			//データタイプが「曜日」の処理
			return DayOfWeekConstant.typeToString(((Number) item).intValue());
		case TableColumnInfo.SCHEDULE_ON_OFF:
			//データタイプが「予定」の処理
			return "";
		case TableColumnInfo.JOB_PARAM_TYPE:
			//データタイプが「ジョブパラメータ種別」の処理
			return JobParamTypeMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.COLLECT_STATUS:
			//データタイプが「収集状態」の処理
			return PerformanceStatusConstant.typeToString(((Boolean) item).booleanValue());
		case TableColumnInfo.JOB_RUNTIME_PARAM_TYPE:
			//データタイプが「ランタイムジョブ変数パ種別」の処理
			return String.format("%s(%s)"
					, JobParamTypeMessage.STRING_RUNTIME
					, JobRuntimeParamTypeMessage.typeToString(((Number) item).intValue()));
		case TableColumnInfo.JOBMAP_ICON_IMAGE:
			//データタイプが「ジョブマップアイコンイメージ」の処理
			return "";
		case TableColumnInfo.APPROVAL_STATUS:
			//データタイプが「承認状態」の処理
			return JobApprovalStatusMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.APPROVAL_RESULT:
			//データタイプが「承認結果」の処理
			return JobApprovalResultMessage.typeToString(((Number) item).intValue());
		case TableColumnInfo.DECISION_CONDITION:
			//データタイプが「判定条件」の処理
			return DecisionObjectMessage.typeToString(((Number) item).intValue());
		default:
			//上記以外のデータタイプの処理
			Class<?> itemClass2 = item.getClass();

			if (itemClass2 == String.class) {
				return String.valueOf(item);
			} else if (itemClass2 == Date.class) {
				return TimezoneUtil.getSimpleDateFormat().format((Date) item);
			} else if (itemClass2 == Time.class) {
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				formatter.setTimeZone(TimezoneUtil.getTimeZone());
				return formatter.format((Time) item);
			} else if (itemClass2.getSuperclass() == Number.class) {
				return ((Number) item).toString();
			} else if (itemClass2.isEnum()) {
				return ((Enum<?>) item).toString();
			} else {
				return item.toString();
			}
		}
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

		ArrayList<?> list = (ArrayList<?>) element;
		if (list.size() <= columnIndex) {
			m_log.debug("Bad implements. IndexOutOfBoundsException");
			return null;
		}
		Object item = list.get(columnIndex);

		TableColumnInfo tableColumn = tableColumnList.get(columnIndex);

		if (item == null || item.equals("")) {
			return null;
		}

		if (tableColumn.getType() == TableColumnInfo.JOB) {
			//データタイプが「ジョブ」の処理
			return JobImageConstant.typeToImage(((Number) item).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.FACILITY) {
			//データタイプが「ファシリティ」の処理
			// TODO スコープとノードの判別方法は要検討！
			Pattern p = Pattern.compile(".*>");
			Matcher m = p.matcher((String) item);
			if (m.matches()) {
				return FacilityImageConstant
						.typeToImage(FacilityConstant.TYPE_SCOPE, true);
			} else {
				return FacilityImageConstant
						.typeToImage(FacilityConstant.TYPE_NODE, true);
			}

		} else if (tableColumn.getType() == TableColumnInfo.STATE) {
			//データタイプが「状態」の処理
			return StatusImageConstant.typeToImage(((Number) item).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.END_STATUS) {
			//データタイプが「終了状態」の処理
			return EndStatusImageConstant.typeToImage(((Number) item)
					.intValue());
		} else if (tableColumn.getType() == TableColumnInfo.CHECKBOX) {
			//データタイプが「チェックボックス」の処理
			return CheckBoxImageConstant.typeToImage(((Boolean) item)
					.booleanValue());
		} else if (tableColumn.getType() == TableColumnInfo.SCHEDULE_ON_OFF) {
			//データタイプが「予定」の処理
			return ScheduleOnOffImageConstant.dateToImage(new Date((Long)item));
		} else if (tableColumn.getType() == TableColumnInfo.COLLECT_STATUS) {
			//データタイプが「収集状態」の処理
			return PerformanceStatusImageConstant.typeToImage(((Boolean) item).booleanValue());
		} else if (tableColumn.getType() == TableColumnInfo.APPROVAL_STATUS) {
			//データタイプが「承認状態」の処理
			return JobApprovalStatusImageConstant.typeToImage(((Number) item).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.APPROVAL_RESULT) {
			//データタイプが「承認結果」の処理
			return JobApprovalResultImageConstant.typeToImage(((Number) item).intValue());
		} else if (tableColumn.getType() == TableColumnInfo.JOBMAP_ICON_IMAGE) {
			//データタイプが「ジョブマップアイコンイメージ」の処理
			JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
			return iconCache.loadByteGraphicImage(((byte[]) item));
		} else if (tableColumn.getType() == TableColumnInfo.RADIO_BUTTON) {
			//データタイプが「ラジオボタン」の処理
			return RadioButtonImageConstant.typeToImage(((Boolean) item)
					.booleanValue());
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
		ArrayList<TableColumnInfo> tableColumnList = m_viewer.getTableColumnList();

		ArrayList<?> list = (ArrayList<?>) element;
		if (list.size() <= columnIndex) {
			m_log.debug("Bad implements. IndexOutOfBoundsException");
			return null;
		}
		Object item = list.get(columnIndex);

		TableColumnInfo tableColumn = tableColumnList.get(columnIndex);

		if (item == null) {
			return null;
		}

		if (tableColumn.getType() == TableColumnInfo.PRIORITY) {
			//データタイプが「重要度」の処理
			return PriorityColorConstant.typeToColor(((Number) item).intValue());
		}

		return null;
	}
}
