/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.factory;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.hub.bean.QuartzConstant;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.util.scheduler.CronExpression;
import com.clustercontrol.plugin.util.scheduler.SchedulerException;
import com.clustercontrol.util.HinemosTime;

/**
 * スケジュール情報を操作するクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
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
	public void updateSchedule(final TransferInfo transfer, String user) throws HinemosUnknown {
		m_log.debug("addSchedule() : id=" + transfer.getTransferId());
		
		//JobDetailに呼び出すメソッドの引数を設定
		//ジョブIDを設定
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = transfer.getTransferId();
		jdArgsType[0] = String.class;
		//カレンダIDを設定
		jdArgs[1] = transfer.getCalendarId();
		jdArgsType[1] = String.class;
		
		m_log.debug(String.format("addSchedule() : id=%s, validflag=%s", transfer.getTransferId(), transfer.getTransType()));
		if (transfer.getValidFlg()) {
			// 同一トランザクションで、deleteJob してから、scheduleCronJob は正常に動作しない。
			// // ジョブの実行に失敗すると、編集によるジョブの再登録が失敗しつづける。
			// // したがって、一旦削除する。
			// SchedulerPlugin.deleteJob(SchedulerType.DBMS, transfer.getTransferId(), QuartzConstant.GROUP_NAME);
			
			switch(transfer.getTransType()) {
			case realtime:
				SchedulerPlugin.scheduleCronJob(
						SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME),
						transfer.getTransferId(),
						QuartzConstant.GROUP_NAME,
						HinemosTime.currentTimeMillis(),
						"*/30 * * * * ? *",
						true,
						HubControllerBean.class.getName(),
						QuartzConstant.METHOD_NAME,
						jdArgsType, jdArgs);
				break;
			case batch:
				{
					String baseTime = HinemosPropertyCommon.hub_transfer_batch_basetime.getStringValue();
					int intervalSec = transfer.getInterval() * 60 * 60;
					long nextTime = getBaseTime(baseTime, intervalSec);
					m_log.debug(String.format("addSchedule() : id=%s, nextTime=%d", transfer.getTransferId(), nextTime));
					
					SchedulerPlugin.scheduleSimpleJob(
							SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME),
							transfer.getTransferId(),
							QuartzConstant.GROUP_NAME,
							nextTime,
							intervalSec,
							true,
							HubControllerBean.class.getName(),
							QuartzConstant.METHOD_NAME,
							jdArgsType, jdArgs);
				}
				break;
			case delay:
				String cronString = HinemosPropertyCommon.hub_transfer_delay_interval.getStringValue();
				try {
					new CronExpression(cronString);
				} catch(Throwable e) {
					m_log.warn(HinemosPropertyCommon.hub_transfer_delay_interval.message_invalid(cronString));
					break;
				}
				
				SchedulerPlugin.scheduleCronJob(
						SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME),
						transfer.getTransferId(),
						QuartzConstant.GROUP_NAME,
						HinemosTime.currentTimeMillis() + 15 * 1000,
						cronString,
						true,
						HubControllerBean.class.getName(),
						QuartzConstant.METHOD_NAME,
						jdArgsType, jdArgs);
				break;
			}
		} else {
			SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME), transfer.getTransferId(), QuartzConstant.GROUP_NAME);
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
	
	public static long getBaseTime(String baseTimeStr, int intervalSec){
		m_log.debug("getBaseTime() : baseTimeStr=" + baseTimeStr + ", intervalSec=" + intervalSec);
		
		Pattern p = Pattern.compile("([0-2]\\d):([0-5]\\d)");
		int offset = 0;
		Matcher m = p.matcher(baseTimeStr);
		if (m.matches()) {
			int hour = Integer.parseInt(m.group(1));
			int minute = Integer.parseInt(m.group(2));
			if (0 > hour || hour >= 24 || 0 > minute || minute >= 60) {
				m_log.warn(HinemosPropertyCommon.hub_transfer_batch_basetime.message_invalid(baseTimeStr));
				hour = 0;
				minute = 0;
			}
			offset = (hour * 60 * 60 + minute * 60) * 1000;
		} else {
			m_log.warn(HinemosPropertyCommon.hub_transfer_batch_basetime.message_invalid(baseTimeStr));
		}
		
		long currentTime = HinemosTime.currentTimeMillis() + HinemosTime.getTimeZoneOffset();
		// この計算を開始した日で、経過した時間を算出
		long elapsedTime = currentTime % (24 * 60 * 60 * 1000);
		long elapsedDay = currentTime / (24 * 60 * 60 * 1000);
		
		int intervalMsec = intervalSec * 1000;
		
		long baseIntervalTime = (elapsedTime / intervalMsec) * intervalMsec;
		long elapsedOffset = offset % intervalMsec;
		
		long baseOffsetTime = 0;
		if(elapsedTime < baseIntervalTime + elapsedOffset){
			baseOffsetTime = baseIntervalTime + elapsedOffset;
		}else{
			baseOffsetTime = baseIntervalTime + elapsedOffset + intervalMsec;
		}
		m_log.debug(String.format("getBaseTime() : baseIntervalTime=%s, elapsedOffset=%d, baseOffsetTime=%d", baseIntervalTime, elapsedOffset, baseOffsetTime));
		
		long baseTime = baseOffsetTime + elapsedDay * (24 * 60 * 60 * 1000) - HinemosTime.getTimeZoneOffset();
		
		m_log.debug(String.format("getBaseTime() : currentTime=%d, elapsedDay=%d, elapsedTime=%d, offset=%d, baseTime=%d",
													currentTime, elapsedDay, elapsedTime, offset, baseTime));
		return baseTime;
	}
	
}
