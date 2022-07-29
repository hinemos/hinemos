/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.scheduler.QuartzUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.bean.QuartzConstant;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.session.MaintenanceControllerBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.plugin.util.scheduler.SchedulerException;

/**
 * スケジュール情報を操作するクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ModifySchedule {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifySchedule.class );

	/**
	 * スケジュール情報を基にQuartzにジョブを登録します。<BR>
	 * Quartzからは、{@link com.clustercontrol.jobmanagement.session.JobControllerBean#scheduleRunJob(String, String)} が呼び出されます。
	 * 
	 * @param info スケジュール情報
	 * @param user ユーザID
	 * @throws ParseException
	 * @throws SchedulerException
	 * @throws RemoteException
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 */
	public void addSchedule(final MaintenanceInfo info, String user) throws HinemosUnknown {
		m_log.debug("addSchedule() : id=" + info.getMaintenanceId());
		
		//JobDetailに呼び出すメソッドの引数を設定
		//ジョブIDを設定
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = info.getMaintenanceId();
		jdArgsType[0] = String.class;
		//カレンダIDを設定
		jdArgs[1] = info.getCalendarId();
		jdArgsType[1] = String.class;
		
		try {
			if (info.getValidFlg().booleanValue()) {
				SchedulerPlugin.scheduleCronJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME), info.getMaintenanceId(), QuartzConstant.GROUP_NAME, HinemosTime.currentTimeMillis() + 15 * 1000,
						QuartzUtil.getCronString(info.getSchedule()), true, MaintenanceControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
			} else {
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME), info.getMaintenanceId(), QuartzConstant.GROUP_NAME);
			}
		} catch (HinemosUnknown e) {
			
		}
	}

	/**
	 * スケジュール情報を基にQuartzに登録したジョブを削除します。
	 * 
	 * @param jobkickId スケジュールID
	 * @throws SchedulerException
	 * @throws RemoteException
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 * @see com.clustercontrol.quartzmanager.ejb.session.QuartzManager#deleteSchedule(java.lang.String, java.lang.String)
	 */
	public void deleteSchedule(final String jobkickId) throws HinemosUnknown {
		m_log.debug("deleteSchedule() : id=" + jobkickId);

		try {
			SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME), jobkickId, QuartzConstant.GROUP_NAME);
		} catch (HinemosUnknown e) {
			m_log.error(e);
		}
	}
}
