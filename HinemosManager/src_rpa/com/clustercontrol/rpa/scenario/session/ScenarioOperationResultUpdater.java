/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.session;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.rpa.scenario.model.UpdateRpaScenarioOperationResultInfo;
import com.clustercontrol.rpa.session.RpaControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * シナリオ実績更新の非同期実行を行うクラス
 */
public class ScenarioOperationResultUpdater {
	private static Log m_log = LogFactory.getLog(ScenarioOperationResultUpdater.class);
	private static final String jobName = "OperationResultUpdate";
	private static final String jobGroupName = "RPA_MANAGEMENT";

	/** クラス全体ロック */
	private static final ReentrantLock lock = new ReentrantLock();

	
	/**
	 * 非同期実行のスケジューリング
	 */
	@SuppressWarnings("unchecked")
	public static void start() {
		try {
			String cronString = HinemosPropertyCommon.rpa_update_scenario_operation_result_interval.getStringValue();
			SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, ScenarioOperationResultUpdater.class.getName(), "execute", new Class[]{}, new Serializable[]{});
			m_log.debug("scheduled update with cron expression=" + cronString);
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
			// 起動に失敗した場合、cron 文字列を既定で再試行。
				String cronString = HinemosPropertyCommon.rpa_update_scenario_operation_result_interval.getBean().getDefaultStringValue();
				m_log.warn(String.format("invalid cron expression expression:%s = %s, so schedule auto-detection with default cron expression:%s",
						HinemosPropertyCommon.rpa_update_scenario_operation_result_interval.getKey(),
						HinemosPropertyCommon.rpa_update_scenario_operation_result_interval.getStringValue(),
						cronString
						));
				try {
					SchedulerPlugin.scheduleCronJob(SchedulerType.RAM_MONITOR, jobName, jobGroupName, HinemosTime.currentTimeMillis(), cronString, true, ScenarioOperationResultUpdater.class.getName(), "execute", new Class[]{}, new Serializable[]{});
				} catch (HinemosUnknown e1) {
					m_log.warn(e.getMessage(), e1);
				}
		}
	}
	
	/**
	 * シナリオ実績更新実行
	 */
	public void execute() throws HinemosUnknown, InvalidRole {
		// ロックを取得。
		if (!lock.tryLock()) {
			// 取得できない場合(シナリオ実績更新が実行中)は終了
			m_log.info("execute(): scenario operation result is updating.");
			return;
		}

		try {
			m_log.debug("execute(): start.");
			RpaControllerBean bean = new RpaControllerBean();
			List<UpdateRpaScenarioOperationResultInfo> updateInfoList = bean.getUpdateRpaScenarioOperationResultInfoList(); 
			if (updateInfoList.isEmpty()) {
				// 実行予定のシナリオ実績更新がない場合は終了
				m_log.debug("execute(): no updates are scheduled.");
				return;
			}
			m_log.info("execute(): start update scenario operation result");
			
			for (UpdateRpaScenarioOperationResultInfo updateInfo : updateInfoList) {
				try (JpaTransactionManager jtm = new JpaTransactionManager()) {
					m_log.debug("update(): start. updateInfo=" + updateInfo);
					
					// 更新(シナリオ設定の実行ノード情報を反映)
					bean.updateUpdateRpaScenarioOperationResults(updateInfo);
					
				} catch(Exception e) {
					// 失敗を通知
					if (updateInfo.getNotifyGroupId() != null) {
						NotifyControllerBean.notify(Arrays.asList(notifyUpdateFailed(updateInfo, e)));
					}
				} finally {
					// 更新情報を削除
					m_log.debug("delete updateInfo=" + updateInfo);
					bean.deleteUpdateRpaScenarioOperationResultInfo(updateInfo.getUpdateId());
				}
			}

			m_log.info("execute(): finish update scenario operation result");

		} finally {
			// ロック開放
			lock.unlock();
		}
	}
	
	/**
	 * 更新完了を通知
	 */
	public static OutputBasicInfo notify(UpdateRpaScenarioOperationResultInfo updateInfo, long numberOfUpdatedResult) {
		// 通知が設定されている場合、完了通知を行う。
		OutputBasicInfo output = new OutputBasicInfo();
		// メッセージ「シナリオ実績更新に成功しました。実績作成設定ID={0}, シナリオ識別子={1}, 対象期間={2}～{3}, 修正シナリオ実績={4}件」
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());

		String message = MessageConstant.MESSAGE_RPA_SCENARIO_OPERATION_RESULT_UPDATE_FINISH.getMessage(
				updateInfo.getScenarioOperationResultCreateSettingId(),
				updateInfo.getScenarioIdentifyString(),
				sdf.format(new Date(updateInfo.getFromDate())),
				sdf.format(new Date(updateInfo.getToDate())),
				String.valueOf(numberOfUpdatedResult)
				);
		int priority = PriorityConstant.TYPE_INFO;

		output.setGenerationDate(HinemosTime.currentTimeMillis());
		output.setPriority(priority);
		output.setPluginId(HinemosModuleConstant.RPA_SCENARIO_CORRECT);
		output.setMonitorId(updateInfo.getScenarioOperationResultCreateSettingId());
		output.setSubKey(updateInfo.getScenarioIdentifyString());
		output.setApplication(updateInfo.getNotifyApplication());
		output.setJoblinkMessageId(JobLinkMessageId.getId(
				NotifyTriggerType.RPA_SCENARIO_UPDATE, 
				HinemosModuleConstant.RPA_SCENARIO_CORRECT, 
				updateInfo.getScenarioOperationResultCreateSettingId()));
		output.setScopeText("");
		output.setFacilityId("");
		output.setMessage(message);
		output.setMessageOrg(message);

		// 通知設定
		output.setNotifyGroupId(updateInfo.getNotifyGroupId());
		
		return output;
	}
	
	public static OutputBasicInfo notifyUpdateFailed(UpdateRpaScenarioOperationResultInfo updateInfo, Exception e) {
		// 通知が設定されている場合、完了通知を行う。
		OutputBasicInfo output = new OutputBasicInfo();
		// メッセージ「シナリオ実績更新に失敗しました。実績作成設定ID={0}, シナリオ識別子={1}, 対象期間={2}～{3}」
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());

		String message = MessageConstant.MESSAGE_RPA_SCENARIO_OPERATION_RESULT_UPDATE_FAILED.getMessage(
				updateInfo.getScenarioOperationResultCreateSettingId(),
				updateInfo.getScenarioIdentifyString(),
				sdf.format(new Date(updateInfo.getFromDate())),
				sdf.format(new Date(updateInfo.getToDate()))
				);
		int priority = PriorityConstant.TYPE_WARNING;

		output.setGenerationDate(HinemosTime.currentTimeMillis());
		output.setPriority(priority);
		output.setPluginId(HinemosModuleConstant.RPA_SCENARIO_CORRECT);
		output.setMonitorId(updateInfo.getScenarioOperationResultCreateSettingId());
		output.setSubKey(updateInfo.getScenarioIdentifyString());
		output.setApplication(updateInfo.getNotifyApplication());

		output.setScopeText("");
		output.setFacilityId("");
		output.setMessage(message);
		output.setMessageOrg(e.getMessage());

		// 通知設定
		output.setNotifyGroupId(updateInfo.getNotifyGroupId());
		
		return output;
	}
}
