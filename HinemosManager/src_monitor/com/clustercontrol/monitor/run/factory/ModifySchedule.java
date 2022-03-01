/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.factory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.QuartzConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.session.MonitorRunManagementBean;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.util.HinemosTime;


/**
 * スケジュールを登録するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class ModifySchedule {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifySchedule.class );

	/**
	 * DBに登録されている全ての監視項目の設定をスケジューラに登録します。
	 * @throws TriggerSchedulerException
	 * 
	 * @since 4.0.0
	 */
	public void updateScheduleAll() throws HinemosUnknown{
		m_log.debug("updateScheduleAll()");

		JpaTransactionManager jtm = null;
		Throwable exception = null;
		Collection<MonitorInfo> entityList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			entityList = QueryUtil.getAllMonitorInfo();
			jtm.commit();
		} catch (Exception e) {
			String msg = "updateScheduleAll() " + e.getClass().getSimpleName() + ", " + e.getMessage();
			m_log.warn(msg, e);
			if (jtm != null)
				jtm.rollback();
			exception = e;
		} finally {
			if (jtm != null)
				jtm.close();
		}

		if (entityList != null) {
			for (MonitorInfo entity : entityList) {
				try {
					// 監視または収集フラグが有効な設定のみスケジュール登録
					if(entity.getMonitorFlg()
							|| entity.getCollectorFlg()
							|| entity.getPredictionFlg()
							|| entity.getChangeFlg()){
						updateSchedule(
								entity.getMonitorId(),
								entity.getMonitorTypeId(),
								entity.getMonitorType(),
								entity.getTriggerType(),
								entity.getRunInterval(),
								entity.getDelayTime(),
								true,
								true);
					}
				} catch (Exception e) {
					m_log.info("updateScheduleAll() scheduleJob : monitorId = " + entity.getMonitorId()
							+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
					// 次の設定を処理するため、throwはしない。
					exception = e;
				}
			}
		}

		if(exception != null){
			throw new HinemosUnknown("An error occurred while scheduling the trigger.", exception);
		}
	}

	/**
	 * スケジューラに監視情報のジョブを登録します。
	 * <p>
	 * <ol>
	 * <li>監視対象IDと監視項目IDを指定し、監視を実行するメソッドのジョブを作成します。</li>
	 * <li>呼びだすメソッドの引数として、下記項目をセットします。</li>
	 * <dl>
	 *  <dt>引数</dt>
	 *  <dd>監視項目ID</dd>
	 * </dl>
	 * <li>スケジューラにジョブとトリガを登録します。</li>
	 * </ol>
	 * 
	 * @param monitorId 監視項目ID
	 * @throws TriggerSchedulerException
	 * @since 2.0.0
	 */
	protected void updateSchedule(String monitorId) throws HinemosUnknown {
		m_log.debug("updateSchedule() : monitorId=" + monitorId);

		MonitorInfo entity = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			try {
				entity = QueryUtil.getMonitorInfoPK_NONE(monitorId);
			} catch (MonitorNotFound e) {
				String msg = "updateSchedule() found no scheduleJob : monitorId = " + monitorId;
				TriggerSchedulerException e1 = new TriggerSchedulerException(msg);
				throw new HinemosUnknown(msg, e1);
			} catch (Exception e) {
				String msg = "updateSchedule() scheduleJob : monitorId = " + monitorId + e.getClass().getSimpleName() + ", " + e.getMessage();
				m_log.warn(msg, e);
				throw new HinemosUnknown(msg, e);
			}
			
			final MonitorInfo entityCopy = entity;
			jtm.addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void postCommit() {
					try {
						updateSchedule(
								entityCopy.getMonitorId(),
								entityCopy.getMonitorTypeId(),
								entityCopy.getMonitorType(),
								entityCopy.getTriggerType(),
								entityCopy.getRunInterval(),
								entityCopy.getDelayTime(),
								(entityCopy.getMonitorFlg() || entityCopy.getCollectorFlg() || entityCopy.getChangeFlg() || entityCopy.getPredictionFlg()),
								false);
					} catch (HinemosUnknown e) {
						m_log.error(e);
					}
				}
			});
		}
	}

	private void updateSchedule(
			String monitorId,
			String monitorTypeId,
			Integer monitorType,
			String triggerType,
			Integer runInterval,
			Integer delayTime,
			Boolean scheduleFlag,
			boolean isInitManager) throws HinemosUnknown {

		TriggerType type = null;
		try {
			type = TriggerType.valueOf(triggerType);
		} catch (IllegalArgumentException e) {
			m_log.info("updateSchedule() Invalid TRIGGER_TYPE. monitorTypeId = " + monitorTypeId + ", + monitorId = " + monitorId);
			return;
		}

		switch (type) {
		case SIMPLE :
			m_log.debug("Schedule SimpleTrigger. monitorId = " + monitorId);

			switch (monitorTypeId) {
			case HinemosModuleConstant.MONITOR_PROCESS:
			case HinemosModuleConstant.MONITOR_PERFORMANCE:
				// プロセス監視・リソース監視などのノード集約型監視については、ノード単位でQuartz登録をするためここでは何も行なわない
				break;
			default:
				//JobDetailに呼び出すメソッドの引数を設定
				// 監視対象IDを設定
				Serializable[] jdArgs = new Serializable[3];
				@SuppressWarnings("unchecked")
				Class<? extends Serializable>[] jdArgsType = new Class[3];
				// 第1引数：監視タイプ
				jdArgsType[0] = String.class;
				jdArgs[0] = monitorTypeId;
				// 第2引数：監視項目ID
				jdArgsType[1] = String.class;
				jdArgs[1] = monitorId;
				// 第3引数:監視判定タイプ
				jdArgsType[2] = Integer.class;
				jdArgs[2] = monitorType;
				
				// SimpleTrigger でジョブをスケジューリング登録
				// 監視も収集も無効の場合、スケジューラには登録しない
				if (scheduleFlag) {
					SchedulerPlugin.scheduleSimpleJob(
							SchedulerType.RAM_MONITOR,
							monitorId,
							monitorTypeId,
							calcSimpleTriggerStartTime(runInterval, delayTime, isInitManager),
							runInterval,
							true,
							MonitorRunManagementBean.class.getName(),
							QuartzConstant.MONITOR_METHOD_MONITOR_AGGREGATED,
							jdArgsType,
							jdArgs);
				}
				break;
			}
			break;
		case CRON :
			// CRON定義の監視は存在しない
			throw new UnsupportedOperationException();
		case SIMPLE2 :
			// SIMPLE2定義の監視は存在しない
			throw new UnsupportedOperationException();
		case NONE :
			// スケジュール登録しない
			break;
		}
	}

	/**
	 * 引数で指定された監視情報をQuartzから削除します。
	 * 
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @throws TriggerSchedulerException
	 * 
	 * @see com.clustercontrol.monitor.run.bean.QuartzConstant
	 * @see com.clustercontrol.commons.util.QuartzUtil#getQuartzManager()
	 * @see com.clustercontrol.quartzmanager.ejb.session.QuartzManager#deleteSchedule(java.lang.String, java.lang.String)
	 */
	protected void deleteSchedule(final String monitorTypeId, final String monitorId) throws HinemosUnknown {
		m_log.debug("deleteSchedule() : type =" + monitorTypeId + ", id=" + monitorId);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void postCommit() {
					try {
						SchedulerPlugin.deleteJob(SchedulerType.RAM_MONITOR, monitorId, monitorTypeId);
					} catch (HinemosUnknown e) {
						m_log.error(e);
					}
				}
			});
		}
	}

//	/**
//	 * Cron形式のスケージュール定義を返します。
//	 * 
//	 * @param schedule スケジュールカレンダ
//	 * @return Cron形式スケジュール
//	 */
//	private String getCronString(int interval, int delayTime){
//		String cronString = null;
//		if(interval > 0 && interval < 3600){
//			int minute = interval / 60;
//
//			// Quartzのcron形式（例： 30 */1 * * * ? *）
//			cronString = delayTime + " */" + minute + " * * * ? *";
//		} else if(interval > 0 && interval >= 3600){
//			int hour = interval / 3600;
//
//			// Quartzのcron形式（例： 30 0 */1 * * ? *）
//			cronString = delayTime + " 0 */" + hour + " * * ? *";
//		}
//
//		m_log.debug("getCronString() interval = " + interval + ", delayTime = " + delayTime + ", cronString = " + cronString);
//		return cronString;
//	}

	/**
	 * スケジュール開始時刻を計算します
	 * @param intervalSec 実行間隔（秒）
	 * @param delayTimeSec 監視・ノード固有のディレイ（秒）
	 * @param isInitManager 起動直後の初期化のフラグ
	 * @return 開始時刻のエポックタイム（ミリ秒）
	 */
	public static long calcSimpleTriggerStartTime(int intervalSec, int delayTimeSec, boolean isInitManager){
		// 再起動時も常に同じタイミングでQuartzのTriggerが起動されるようにstartTimeを設定する
		long now = HinemosTime.currentTimeMillis() + 1000l; // pauseFlag が trueの場合、起動させないように実行開始時刻を少し遅らせる。
		if (isInitManager) {
			// 起動直後の場合はスケジューラ有効になるまでスケジューリングしないように実行開始時刻を遅らせる。
			now += HinemosPropertyCommon.common_scheduler_startup_delay.getIntegerValue() * 1000;
		}
		long intervalMilliSecond = intervalSec * 1000;

		// 1) 現在時刻の直前で、監視間隔の倍数となる時刻を求める
		//   例）22:32:05 に 5分間間隔の設定を追加する場合は、22:35:00
		long roundout = (now / intervalMilliSecond + 1) * intervalMilliSecond;

		// 2) 1)の時刻にDelayTimeを秒数として足したものをstartTimeとして設定する
		long startTimeMillis = roundout + delayTimeSec * 1000;

		// 3) もう一つ前の実行タイミングが（現在時刻+5秒）より後の場合は、そちらをstartTimeとする
		if((now + 5 * 1000l) < (startTimeMillis - intervalMilliSecond)){
			m_log.debug("reset time before : " + new Date(startTimeMillis));
			startTimeMillis = startTimeMillis - intervalMilliSecond;
			m_log.debug("reset time after : " + new Date(startTimeMillis));
		}

		return startTimeMillis;
	}
}
