/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyCloudInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.factory.CloudNotifyExecuter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * クラウド通知の通知処理クラス
 */
public class ExecCloudNotify implements Notifier {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(ExecCloudNotify.class);
	// メッセージのキー
	private static final String _KEYMESSAGE = "MESSAGE";
	private static final String _KEYORGMESSAGE = "ORG_MESSAGE";

	@Override
	public void notify(NotifyRequestMessage requestMessage) throws NotifyNotFound {
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() " + requestMessage);
		}
		OutputBasicInfo outputInfo = requestMessage.getOutputInfo();

		String notifyId = requestMessage.getNotifyId();
		executeCloudNotify(outputInfo, notifyId);
	}

	/**
	 * クラウド通知の通知処理 連携情報を対象プラットフォームの適切なキーを使用してマップに格納し、 クラウド管理機能側の実際の通知処理に受け渡すメソッド
	 * クラウドへの通知処理で何らかの例外が発生した場合、 ここでキャッチしてInternalを出力する
	 * 
	 * @param outputInfo
	 * @param notifyId
	 */
	private void executeCloudNotify(OutputBasicInfo outputInfo, String notifyId) {
		NotifyCloudInfo cloudInfo = null;

		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() " + outputInfo);
		}

		try {
			cloudInfo = QueryUtil.getNotifyCloudInfoPK(notifyId);
		} catch (NotifyNotFound e) {
			internalErrorNotify(outputInfo, notifyId, e.getMessage());
		}

		// 連携情報をMapに格納しクラウド管理機能に受け渡す
		ConcurrentHashMap<String, Object> requestMap = new ConcurrentHashMap<String, Object>();
		// 通知の出力日時を格納
		Long generationDate = outputInfo.getGenerationDate();
		// 出力日時がない場合は現在時刻を使用
		if (generationDate == null) {
			m_log.warn("executeCloudNotify(); generationDate is null. Use current time");
			generationDate = HinemosTime.currentTimeMillis();
		}
		requestMap.put(CloudConstant.notify_timestamp, generationDate);
		if (cloudInfo.getPlatformType() == CloudConstant.notify_aws_platform) {
			// aws
			requestMap.put(CloudConstant.notify_aws_eventBus,
					getEventBus(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_aws_detailType,
					getDetailType(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_aws_source, getSource(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_aws_detail,
					getJsonData(outputInfo, cloudInfo, outputInfo.getPriority()));
		} else if (cloudInfo.getPlatformType() == CloudConstant.notify_azure_platform) {
			// azure
			requestMap.put(CloudConstant.notify_azure_endpoint,
					getEventBus(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_azure_accessKey,
					getAccessKey(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_azure_subject,
					getDetailType(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_azure_eventType,
					getSource(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_azure_dataVersion,
					getDataVersion(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_azure_data,
					getJsonData(outputInfo, cloudInfo, outputInfo.getPriority()));
		} else if (cloudInfo.getPlatformType() == CloudConstant.notify_gcp_platform) {
			// GCP
			requestMap.put(CloudConstant.notify_gcp_projectId,
					getEventBus(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_gcp_topicId,
					getSource(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_gcp_message,
					getDetailType(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_gcp_orderingKey,
					getDataVersion(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_gcp_attribute,
					getJsonData(outputInfo, cloudInfo, outputInfo.getPriority()));
			requestMap.put(CloudConstant.notify_gcp_region_endpoint,
					cloudInfo.getFacilityId().substring(cloudInfo.getFacilityId().lastIndexOf('_') + 1));
		} else {
			// GCP,AWS, Azure以外が選択された場合（バリデーションされているので通常あり得ない）
			m_log.error(
					"executeCloudNotify(): Unsupported Platform selected. Notification failed. Selected facility id: "
							+ cloudInfo.getFacilityId());
			internalErrorNotify(outputInfo, notifyId,
					MessageConstant.MESSAGE_PLEASE_SELECT_PUBLIC_CLOUD.getMessage(""));
		}

		// クラウド管理側での通知処理呼び出し
		try {
			CloudNotifyExecuter.execNotify(cloudInfo.getFacilityId(), cloudInfo.getNotifyInfoEntity().getOwnerRoleId(),
					requestMap);
		} catch (Exception e) {
			// 通知失敗時にはINTERNAL
			internalErrorNotify(outputInfo, notifyId, e.getMessage());
		}

		// バースト時の対策として、一定時間のクールタイムを設ける（無効化も可能）
		// 注意点として通知スレッドはデフォルト8スレッドあるので、必ずしもここで指定した期間毎に
		// 通知が送信されるわけではない
		if (HinemosPropertyCommon.notify_cloud_send_interval.getIntegerValue() > 0) {
			try {
				m_log.debug("executeCloudNotify(): sleep start for : "
						+ HinemosPropertyCommon.notify_cloud_send_interval.getIntegerValue());
				Thread.sleep(HinemosPropertyCommon.notify_cloud_send_interval.getIntegerValue());
				m_log.debug("executeCloudNotify(): sleep end");
			} catch (NullPointerException | InterruptedException e) {
				m_log.error("executeCloudNotify():", e);
			}
		}
	}

	/**
	 * AWS：EventBus Azure：Endpoint GCP:ProjectId を通知変数を置換し取得するメソッド
	 * 
	 * @param outputInfo
	 * @param priority
	 * @return
	 */
	private String getEventBus(OutputBasicInfo outputInfo, NotifyCloudInfo cloudInfo, int priority) {
		String beforeReplace = "";

		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			beforeReplace = cloudInfo.getInfoEventBus();
			break;
		case PriorityConstant.TYPE_WARNING:
			beforeReplace = cloudInfo.getWarnEventBus();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			beforeReplace = cloudInfo.getCritEventBus();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			beforeReplace = cloudInfo.getUnkEventBus();
			break;
		default:
			m_log.error("getEventBus(): unknown priority: " + priority);
			break;
		}

		return getReplaceString(outputInfo, cloudInfo, beforeReplace);
	}

	/**
	 * Azure：AccessKey を通知変数を置換し取得するメソッド
	 * 
	 * @param outputInfo
	 * @param priority
	 * @return
	 */
	private String getAccessKey(OutputBasicInfo outputInfo, NotifyCloudInfo cloudInfo, int priority) {
		String beforeReplace = "";

		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			beforeReplace = cloudInfo.getInfoAccessKey();
			break;
		case PriorityConstant.TYPE_WARNING:
			beforeReplace = cloudInfo.getWarnAccessKey();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			beforeReplace = cloudInfo.getCritAccessKey();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			beforeReplace = cloudInfo.getUnkAccessKey();
			break;
		default:
			m_log.error("getAccessKey(): unknown priority: " + priority);
			break;
		}

		return getReplaceString(outputInfo, cloudInfo, beforeReplace);
	}

	/**
	 * AWS：DetailType Azure：Subject GCP:Message を通知変数を置換し取得するメソッド
	 * 
	 * @param outputInfo
	 * @param priority
	 * @return
	 */
	private String getDetailType(OutputBasicInfo outputInfo, NotifyCloudInfo cloudInfo, int priority) {
		String beforeReplace = "";

		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			beforeReplace = cloudInfo.getInfoDetailType();
			break;
		case PriorityConstant.TYPE_WARNING:
			beforeReplace = cloudInfo.getWarnDetailType();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			beforeReplace = cloudInfo.getCritDetailType();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			beforeReplace = cloudInfo.getUnkDetailType();
			break;
		default:
			m_log.error("getDetailType(): unknown priority: " + priority);
			break;
		}

		return getReplaceString(outputInfo, cloudInfo, beforeReplace);
	}

	/**
	 * AWS：Souece Azure：EventType GCP: TopicId を通知変数を置換し取得するメソッド
	 * 
	 * @param outputInfo
	 * @param priority
	 * @return
	 */
	private String getSource(OutputBasicInfo outputInfo, NotifyCloudInfo cloudInfo, int priority) {
		String beforeReplace = "";

		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			beforeReplace = cloudInfo.getInfoSource();
			break;
		case PriorityConstant.TYPE_WARNING:
			beforeReplace = cloudInfo.getWarnSource();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			beforeReplace = cloudInfo.getCritSource();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			beforeReplace = cloudInfo.getUnkSource();
			break;
		default:
			m_log.error("getSource(): unknown priority: " + priority);
			break;
		}

		return getReplaceString(outputInfo, cloudInfo, beforeReplace);
	}

	/**
	 * AWS：Detail Azure：Data GCP: Attribute を通知変数を置換し取得するメソッド
	 * 
	 * @param outputInfo
	 * @param priority
	 * @return
	 */
	private Map<String, String> getJsonData(OutputBasicInfo outputInfo, NotifyCloudInfo cloudInfo, int priority) {
		String beforeReplace = "";

		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			beforeReplace = cloudInfo.getInfoJsonData();
			break;
		case PriorityConstant.TYPE_WARNING:
			beforeReplace = cloudInfo.getWarnJsonData();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			beforeReplace = cloudInfo.getCritJsonData();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			beforeReplace = cloudInfo.getUnkJsonData();
			break;
		default:
			m_log.error("getJsonData(): unknown priority: " + priority);
			break;
		}

		// json to map
		ObjectMapper om = new ObjectMapper();
		Map<String, String> tmpMap = null;
		Map<String, String> replacedMap = new ConcurrentHashMap<String, String>();
		if (!beforeReplace.isEmpty()) {
			try {
				// キーがString、値がObjectのマップに読み込みます。
				tmpMap = om.readValue(beforeReplace, new TypeReference<ConcurrentHashMap<String, String>>() {
				});
			} catch (Exception e) {
				// mapへの変換に失敗した場合は空のマップを返す。
				m_log.warn("getJsonData(): Map Parse failed", e);
				return replacedMap;
			}
		} else {
			// ディテール/データが空の場合は空のマップを返す
			return replacedMap;
		}

		// map内で変換が必要なものを変換
		for (Entry<String, String> e : tmpMap.entrySet()) {
			replacedMap.put(getReplaceString(outputInfo, cloudInfo, e.getKey()),
					getReplaceString(outputInfo, cloudInfo, e.getValue()));
		}

		return replacedMap;
	}

	/**
	 * Azure：DataVersion GCP: OrderingKey を通知変数を置換し取得するメソッド
	 * 
	 * @param outputInfo
	 * @param priority
	 * @return
	 */
	private String getDataVersion(OutputBasicInfo outputInfo, NotifyCloudInfo cloudInfo, int priority) {
		String beforeReplace = "";

		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			beforeReplace = cloudInfo.getInfoDataVersion();
			break;
		case PriorityConstant.TYPE_WARNING:
			beforeReplace = cloudInfo.getWarnDataVersion();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			beforeReplace = cloudInfo.getCritDataVersion();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			beforeReplace = cloudInfo.getUnkDataVersion();
			break;
		default:
			m_log.error("getDataVersion(): unknown priority: " + priority);
			break;
		}

		return getReplaceString(outputInfo, cloudInfo, beforeReplace);
	}

	/**
	 * 通知変数の置換用メソッド
	 * 
	 * @param outputInfo
	 * @param replaceStr
	 * @return
	 */
	private String getReplaceString(OutputBasicInfo outputInfo, NotifyCloudInfo cloudInfo, String replaceStr) {
		// nullの場合は空文字に置き換え
		if (replaceStr == null) {
			replaceStr = "";
		}
		// 文字列を置換する
		try {
			int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
			ArrayList<String> inKeyList = StringBinder.getKeyList(replaceStr, maxReplaceWord);
			Map<String, String> param = NotifyUtil.createParameter(outputInfo, cloudInfo.getNotifyInfoEntity(),
					inKeyList);

			// MESSAGE or ORG_MESSAGEの場合最大長で切り取る
			// 最大値が0未満の場合は無制限（切り取りを行わない）
			int messageMax = HinemosPropertyCommon.notify_cloud_message_max_length.getIntegerValue();
			int orgMessageMax = HinemosPropertyCommon.notify_cloud_messageorg_max_length.getIntegerValue();
			// MESSAGE
			String orgLine = param.get(_KEYMESSAGE);
			if (orgLine != null && orgLine.length() >= messageMax && messageMax > 0) {
				String updateMessageOrg = orgLine.substring(0, messageMax);
				param.replace(_KEYMESSAGE, updateMessageOrg);
			}
			// ORG_MESSAGE
			orgLine = param.get(_KEYORGMESSAGE);
			if (orgLine != null && orgLine.length() >= orgMessageMax && orgMessageMax > 0) {
				String updateMessageOrg = orgLine.substring(0, orgMessageMax);
				param.replace(_KEYORGMESSAGE, updateMessageOrg);
			}

			StringBinder binder = new StringBinder(param);
			return binder.replace(replaceStr);
		} catch (Exception e) {
			// 例外が発生した場合は、置換前の文字列を返す
			m_log.warn("getReplaceString() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);

			return replaceStr;

		}
	}

	/**
	 * 通知失敗時の内部エラー通知を定義します
	 */
	public void internalErrorNotify(OutputBasicInfo source, String notifyId, String detailMsg) {

		/*
		 * 通知元のプラグインIDごとにメッセージを分ける
		 */
		// 通知元が監視の場合
		if (source.getPluginId().matches(HinemosModuleConstant.MONITOR + ".*")) {
			String[] args = { notifyId, source.getFacilityId(), source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_024, args, detailMsg);
		}
		// 通知元がジョブの場合
		else if (source.getPluginId().matches(HinemosModuleConstant.JOB + ".*")) {
			String[] args = { notifyId, source.getFacilityId(), source.getMonitorId(), source.getJobunitId(),
					source.getJobId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_025, args, detailMsg);
		}
		// 通知元がメンテナンスの場合
		else if (source.getPluginId().matches(HinemosModuleConstant.SYSYTEM_MAINTENANCE)) {
			String[] args = { notifyId, source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_026, args, detailMsg);
		}
		// 通知元が環境構築の場合
		else if (source.getPluginId().matches(HinemosModuleConstant.INFRA)) {
			String[] args = { notifyId, source.getFacilityId(), source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_027, args, detailMsg);
		}
		// 通知元が構成情報設定の場合
		else if (source.getPluginId().matches(HinemosModuleConstant.NODE_CONFIG_SETTING)) {
			String[] args = { notifyId, source.getFacilityId(), source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_028, args, detailMsg);
		}
		// 通知元がレポーティングの場合
		else if (source.getPluginId().matches(HinemosModuleConstant.REPORTING)) {
			String[] args = { notifyId, source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_029, args, detailMsg);
		}
		// 通知元がSDMLの場合
		else if (source.getPluginId().matches(HinemosModuleConstant.SDML_CONTROL)) {
			String[] args = { notifyId, source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_033, args, detailMsg);
		}
		// 通知元がRPAシナリオ実績作成の場合
		else if (source.getPluginId().matches(HinemosModuleConstant.RPA_SCENARIO_CREATE)) {
			String[] args = { notifyId, source.getMonitorId() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_034, args, detailMsg);
		}
		// 通知元がRPAシナリオ実績更新の場合
		else if (source.getPluginId().matches(HinemosModuleConstant.RPA_SCENARIO_CORRECT)) {
			String[] args = { notifyId, source.getMonitorId(), source.getSubKey() };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_035, args, detailMsg);
		}
		// 何らかの理由でプラグインIDがマッチしなかった場合はデフォルト
		else {
			String[] args = { notifyId, source.getFacilityId(), source.getMonitorId() };
			// 通知失敗メッセージを出力
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, detailMsg);
		}

	}
}
