/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.run.bean.QuartzConstant;
import com.clustercontrol.monitor.run.factory.ModifySchedule;
import com.clustercontrol.monitor.run.session.MonitorRunManagementBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;

/**
 * ノード単位に行なう監視（プロセス・リソース）のポーリング処理について、Quartzへの登録・削除を管理するクラス
 */
public class NodeMonitorPollerController {
	
	private static final Log log = LogFactory.getLog( NodeMonitorPollerController.class );
	
	/**
	 * 全ノードに対して、プロセス・リソース監視のQuartz設定を追加する。
	 * 
	 * 本メソッドは、Hinemos起動直後に1回のみ実行すること。
	 */
	public static void init() throws HinemosUnknown {
		for (final NodeInfo node : new RepositoryControllerBean().getNodeList()) {
				registNodeMonitorPoller(node.getFacilityId(), node.getNodeMonitorDelaySec(), true);
		}
		log.info("init() : regist all node to poller completed.");;
	}
	
	private static final String[] targetMonitorTypes =
			new String[] {HinemosModuleConstant.MONITOR_PROCESS, HinemosModuleConstant.MONITOR_PERFORMANCE};
	
	public static void registNodeMonitorPoller(String facilityId, int nodeMonitorDelaySec) {
		registNodeMonitorPoller(facilityId, nodeMonitorDelaySec, false);
	}
	
	/**
	 * 指定したノードに対するノード単位で行なう監視（プロセス・リソースなど）のスケジュールを、Quartzに登録する。
	 * 
	 * Hinemosにノードを追加した際や、Hinemosの初期化の際に呼び出す。
	 * 
	 * @param node 対象のノード
	 * @param isInitManager 起動直後の初期化のフラグ
	 */
	public static void registNodeMonitorPoller(String facilityId, int nodeMonitorDelaySec, boolean isInitManager) {

		// 各監視項目タイプごとにQuartz登録する（今のところ、プロセスと、リソース）
		for (final String monitorTypeId : targetMonitorTypes) {
			// Quartzからコールバックされる際に、コールバックメソッドに渡される引数を構築する
			final Serializable[] args = new Serializable[3];
			@SuppressWarnings("unchecked")
			final Class<? extends Serializable>[] argTypes = new Class[3];
			// 第1引数：監視タイプ
			argTypes[0] = String.class;
			args[0] = monitorTypeId;
			// 第2引数：FacilityId
			argTypes[1] = String.class;
			args[1] = facilityId;
			// 第3引数:監視判定タイプ
			argTypes[2] = Integer.class;
			args[2] = Integer.valueOf(1); // XXX nagatsumas 監視判定タイプを固定で「数値」としているが、実際には監視タイプIDに応じて監視判定タイプを変えるべき
			
			// Quartzに登録
			try {
				SchedulerPlugin.scheduleSimpleJob(
						SchedulerType.RAM,
						facilityId,
						monitorTypeId,
						ModifySchedule.calcSimpleTriggerStartTime(RunInterval.min(), nodeMonitorDelaySec % RunInterval.min(), isInitManager),
						RunInterval.min(),
						true, 
						MonitorRunManagementBean.class.getName(),
						QuartzConstant.MONITOR_METHOD_FACILITY_AGGREGATED,
						argTypes,
						args);
				if (log.isDebugEnabled())
					log.debug("registNodeMonitorPoller() : regist node to poller. facilityId = " + facilityId + ", monitorTypeId = " + monitorTypeId);
			} catch (HinemosUnknown e) {
				log.error("registNodeMonitorPoller() : failed to regist node to poller. facilityId = " + facilityId + ", monitorTypeId = " + monitorTypeId, e);
			}
		}
	}
	
	/**
	 * 指定したノードに対するノード単位で行なう監視（プロセス・リソースなど）のスケジュールを、Quartzから削除する。
	 * 主に、Hinemosからノードを削除したときに呼び出す。
	 * 
	 * @param facilityId ノードのfacilityId
	 */
	public static void unregistNodeMonitorPoller(String facilityId) {
		// 各監視項目タイプごとにQuartz登録する（今のところ、プロセスと、リソース）
		for (final String monitorTypeId : targetMonitorTypes) {
			try {
				SchedulerPlugin.deleteJob(SchedulerType.RAM, facilityId, monitorTypeId);
				if (log.isDebugEnabled())
					log.debug("unregistNodeMonitorPoller() : unregist node to poller. facilityId = " + facilityId + ", monitorTypeId = " + monitorTypeId);
			} catch (HinemosUnknown e) {
				log.warn("unregistNodeMonitorPoller() : failed to unregist node to poller. facilityId = " + facilityId + ", monitorTypeId = " + monitorTypeId, e);
			}
		}
	}
	
	/**
	 * 現在のタイミングで実行すべき監視間隔のSetを返す
	 * @param m_facilityId 監視対象のノード（ノードごとに固有の監視タイミングが存在するため）
	 * @return 現在実行するべき監視間隔
	 */
	public static Set<Integer> calcCurrentExecMonitorIntervals(NodeInfo node) {
		
		final long nowMillisec = HinemosTime.currentTimeMillis();
		final long nodeDelayMillisec = node.getNodeMonitorDelaySec() * 1000;
		
		final Set<Integer> returnIntervals = new HashSet<Integer>();
		
		for (final int intervalSec : RunInterval.intValues()) {
			final long intervalMillisec = intervalSec * 1000;
			// 対象監視間隔において、現在の直前に実行すべき（ノード固有のディレイ値を含まない）時間を求める
			// 例：60秒間隔で、現在時刻が 10:01:20 の場合、直前の実行時刻は10:01:00
			// now / interval の時点で小数点以下切捨てなので、そこでintervalを再度かけることで、直前のタイミングがわかる
			long prevExecMillisec = (nowMillisec / intervalMillisec) * intervalMillisec;
			// 続いてノード固有のディレイ時間も含め、直前に実行するべき時間を求める
			prevExecMillisec += (nodeDelayMillisec % intervalMillisec);
			if (nowMillisec < prevExecMillisec) {
				prevExecMillisec -= intervalMillisec;
			}
			
			// 直前の実行時刻 ≦ 現在時刻 ＜ 直前の実行時刻＋ポーラの最小実行間隔
			// の関係になっていれば、このタイミングで当該監視間隔の監視を実行するべき。
			if (nowMillisec < (prevExecMillisec + RunInterval.min() * 1000)) {
				returnIntervals.add(intervalSec);
			}
		}
		return returnIntervals;
	}
	
}
