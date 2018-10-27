/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.scheduler.QuartzUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.plugin.util.scheduler.SchedulerException;
import com.clustercontrol.reporting.bean.QuartzConstant;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.session.ReportingControllerBean;
import com.clustercontrol.util.HinemosTime;

/**
 * スケジュール情報を操作するクラスです。
 *
 * @version 4.1.2
 *
 */
public class ModifySchedule {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifySchedule.class );

	/**
	 * スケジュール情報を基にQuartzにジョブを登録します。<BR>
	 * Quartzからは、{@link com.clustercontrol.reporting.session.ReportingControllerBean#scheduleRunReporting(String, String)} が呼び出されます。
	 * 
	 * @param info スケジュール情報
	 * @param user ユーザID
	 * @throws ParseException
	 * @throws SchedulerException
	 * @throws RemoteException
	 * 
	 * @see com.clustercontrol.reporting.bean.QuartzConstant
	 */
	public void addSchedule(ReportingInfo info, String user) throws HinemosUnknown {
		m_log.debug("addSchedule() : id=" + info.getReportScheduleId());

		//JobDetailに呼び出すメソッドの引数を設定
		//ジョブIDを設定
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = info.getReportScheduleId();
		jdArgsType[0] = String.class;
		//カレンダIDを設定
		jdArgs[1] = info.getCalendarId();
		jdArgsType[1] = String.class;

		SchedulerPlugin.scheduleCronJob(SchedulerType.DBMS, info.getReportScheduleId(), QuartzConstant.GROUP_NAME, HinemosTime.currentTimeMillis() + 15 * 1000,
				QuartzUtil.getCronString(info.getSchedule()), true, ReportingControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
		if (!info.getValidFlg().booleanValue()) {
			SchedulerPlugin.deleteJob(SchedulerType.DBMS, info.getReportScheduleId(), QuartzConstant.GROUP_NAME);
//			SchedulerPlugin.pauseJob(SchedulerType.DBMS, info.getReportScheduleId(), QuartzConstant.GROUP_NAME);
		}
	}

	/**
	 * スケジュール情報を基にQuartzに登録したジョブを削除します。
	 * 
	 * @param scheduleId スケジュールID
	 * @throws SchedulerException
	 * @throws RemoteException
	 * 
	 * @see com.clustercontrol.reporting.bean.QuartzConstant
	 * @see com.clustercontrol.quartzmanager.ejb.session.QuartzManager#deleteSchedule(java.lang.String, java.lang.String)
	 */
	public void deleteSchedule(String scheduleId) throws HinemosUnknown {
		m_log.debug("deleteSchedule() : id=" + scheduleId);

		SchedulerPlugin.deleteJob(SchedulerType.DBMS, scheduleId, QuartzConstant.GROUP_NAME);
	}
}
