/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.scenario.factory;

import java.io.Serializable;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.scenario.session.RpaAnalyzeExecutor;
import com.clustercontrol.rpa.util.RpaConstants;
import com.clustercontrol.util.HinemosTime;

public class ModifySchedule {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifySchedule.class );

	private static Random random = new Random();

	/**
	 * シナリオ実績作成設定設定を基にQuartzにログ解析タスクのスケジュールを行う<BR>
	 * 
	 * @param createSetting シナリオ実績作成設定
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 */
	public static void updateAnalyzeSchedule(RpaScenarioOperationResultCreateSetting createSetting) throws HinemosUnknown {
		m_log.debug("addSchedule() : id=" + createSetting.getScenarioOperationResultCreateSettingId());
		//JobDetailに呼び出すメソッドの引数を設定
		//監視項目IDを設定
		Serializable[] jdArgs = new Serializable[1];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[1];
		jdArgs[0] = createSetting.getScenarioOperationResultCreateSettingId();
		jdArgsType[0] = String.class;
		
		if (createSetting.getValidFlg()) {
			// スケジュールを登録
			SchedulerPlugin.scheduleSimpleJob(
					SchedulerPlugin.toSchedulerTypeForDBMS(RpaConstants.groupName),
					RpaConstants.scheduleIdHeader + createSetting.getScenarioOperationResultCreateSettingId(),
					RpaConstants.groupName,
					calcSimpleTriggerStartTime(createSetting.getScenarioOperationResultCreateSettingId(), createSetting.getInterval()),
					createSetting.getInterval(),
					true,
					RpaAnalyzeExecutor.class.getName(),
					RpaConstants.methodName,
					jdArgsType, jdArgs);
		} else {
			// 設定が無効な場合、スケジュールを削除
			deleteAnalyzeSchedule(createSetting.getScenarioOperationResultCreateSettingId());
		}
	}

	/**
	 * スケジュールを削除する。
	 * @param createSettingId
	 * @throws HinemosUnknown 
	 */
	public static void deleteAnalyzeSchedule(String createSettingId) throws HinemosUnknown {
		SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(RpaConstants.groupName), RpaConstants.scheduleIdHeader + createSettingId, RpaConstants.groupName);
	}
	
	/**
	 * スケジュールの開始時刻を生成
	 * 初回時刻 = 00:00を基準とした現在時刻以降の実行時刻 + ランダムなdelay
	 * @see com.clustercontrol.monitor.run.factory.ModifySchedule#calcSimpleTriggerStartTime
	 */
	private static long calcSimpleTriggerStartTime(String settingId, int intervalSec) {
		long now = HinemosTime.currentTimeMillis();
		long startTime = now;
		int intervalMilli = intervalSec * 1000;
		// 00:00を基準とし、現在時刻以降の実行時刻を求め、開始時刻とする。
		// 例: 現在時刻:03:23:20、実行間隔:5分→実行時刻:03:25:00
		startTime += intervalMilli;
		startTime -= startTime % intervalMilli;
		
		// ランダムに遅延させる。
		// 作成設定IDのハッシュ値からdelay生成
		random.setSeed(settingId.hashCode());
		startTime += random.nextInt(intervalMilli);
		
		// 遅延させた結果、1回前の実行時刻が現在時刻+5秒以降になった場合、そちらを開始時刻とする。
		if (startTime - intervalMilli > now + 5000) {
			startTime -= intervalMilli;
		}
		
		m_log.debug("startTime=" + startTime);
		return startTime;
	}
}

