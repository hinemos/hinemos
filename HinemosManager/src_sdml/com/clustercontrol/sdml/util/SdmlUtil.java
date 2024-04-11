/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.sdml.factory.SdmlVersionManager;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Singletons;

/**
 * SDML機能のマネージャにおけるユーティリティクラス
 *
 */
public class SdmlUtil {
	private static Log logger = LogFactory.getLog(SdmlUtil.class);

	/**
	 * 監視設定がSDMLで自動作成された監視設定か判定する
	 * 
	 * @param monitorInfo
	 * @return
	 */
	public static boolean isCreatedBySdml(MonitorInfo monitorInfo) {
		if (monitorInfo == null) {
			return false;
		}
		return monitorInfo.getSdmlMonitorTypeId() != null && !monitorInfo.getSdmlMonitorTypeId().equals("");
	}

	/**
	 * SDML監視種別IDを指定してプラグインIDを取得
	 * 
	 * @param sdmlMonitorTypeId
	 * @return
	 */
	public static String getPluginId(String sdmlMonitorTypeId) {
		return Singletons.get(SdmlMonitorTypeUtil.class).getPluginId(sdmlMonitorTypeId);
	}

	/**
	 * 通知情報のプラグインIDをSDML監視種別のIDに置換する
	 * 
	 * @param monitorInfo
	 * @param list
	 */
	public static void replacePluginId(MonitorInfo monitorInfo, List<OutputBasicInfo> list) {
		String pluginId = getPluginId(monitorInfo.getSdmlMonitorTypeId());
		for (OutputBasicInfo output : list) {
			output.setPluginId(pluginId);
		}
	}

	/**
	 * 指定したSDML監視種別IDがマスタに含まれているかどうか
	 * 
	 * @param sdmlMonitorTypeId
	 * @return
	 */
	public static boolean isValidSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		return Singletons.get(SdmlMonitorTypeUtil.class).isContained(sdmlMonitorTypeId);
	}

	/**
	 * 監視ログから収集用のタグを抽出する
	 * 
	 * @param monitorId
	 * @param message
	 * @return
	 */
	public static List<StringSampleTag> extractTagsFromMonitoringLog(String monitorId, String message) {
		return Singletons.get(SdmlVersionManager.class).extractTagsFromMonitoringLog(monitorId, message);
	}

	/**
	 * 監視設定の情報からSDMLが自動で抽出するタグを取得する
	 * 
	 * @param monitorId
	 * @param sdmlMonitorTypeId
	 * @return
	 */
	public static List<String> getSampleTagList(String monitorId, String sdmlMonitorTypeId) {
		return Singletons.get(SdmlVersionManager.class).getSampleTagList(monitorId, sdmlMonitorTypeId);
	}

	/**
	 * SDML制御設定に対する通知情報を作成する
	 * 
	 * @param controlSetting
	 * @param facilityId
	 * @param priority
	 * @param message
	 * @param messageOrg
	 * @return
	 * @throws HinemosUnknown
	 */
	public static OutputBasicInfo createOutputBasicInfo(SdmlControlSettingInfo controlSetting, String facilityId,
			int priority, String message, String messageOrg, NotifyTriggerType notifyTriggerType) throws HinemosUnknown {
		OutputBasicInfo output = new OutputBasicInfo();

		// 通知情報を設定
		output.setPriority(priority);
		output.setGenerationDate(HinemosTime.currentTimeMillis());
		output.setPluginId(HinemosModuleConstant.SDML_CONTROL);
		output.setMonitorId(controlSetting.getApplicationId());
		output.setFacilityId(facilityId);
		output.setScopeText(new RepositoryControllerBean().getFacilityPath(facilityId, controlSetting.getFacilityId()));
		output.setApplication(controlSetting.getApplication());
		output.setMessage(message);
		output.setMessageOrg(messageOrg);
		output.setNotifyGroupId(controlSetting.getNotifyGroupId());
		output.setJoblinkMessageId(JobLinkMessageId.getId(notifyTriggerType, HinemosModuleConstant.SDML_CONTROL, controlSetting.getApplicationId()));
		//7.0.0との互換性保持のため、Hinemosプロパティで出力するか制御
		boolean flg = HinemosPropertyCommon.notify_output_trigger_subkey_$.getBooleanValue(HinemosModuleConstant.SDML_CONTROL);
		if (flg) {
			output.setSubKey(notifyTriggerType.name());
		}

		return output;
	}

	/**
	 * 登録されている全てのアプリケーションIDの一覧を取得する<br>
	 * ※バージョンは考慮しない
	 * 
	 * @return
	 */
	public static List<String> getApplicationIdList() {
		List<String> rtn = new ArrayList<>();
		try {
			List<SdmlControlSettingInfo> list = QueryUtil.getAllSdmlControlSettingInfo();
			for (SdmlControlSettingInfo info : list) {
				rtn.add(info.getApplicationId());
			}
		} catch (Exception e) {
			logger.warn("getApplicationIdList() : " + e.getMessage(), e);
		}
		return rtn;
	}

	/**
	 * 指定したオーナーロールIDが設定されたSDML制御設定のアプリケーションIDの一覧を取得する<br>
	 * ※バージョンは考慮しない
	 * 
	 * @param ownerRoleId
	 * @return
	 */
	public static List<String> getApplicationIdListByOwnerRoleId(String ownerRoleId) {
		List<String> rtn = new ArrayList<>();
		try {
			List<SdmlControlSettingInfo> list = QueryUtil.getSdmlControlSettingInfoFindByOwnerRoleId_NONE(ownerRoleId);
			for (SdmlControlSettingInfo info : list) {
				rtn.add(info.getApplicationId());
			}
		} catch (Exception e) {
			logger.warn("getApplicationIdListByOwnerRoleId() : " + e.getMessage(), e);
		}
		return rtn;
	}
}
