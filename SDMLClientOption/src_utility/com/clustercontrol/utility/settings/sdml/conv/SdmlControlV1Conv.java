/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.sdml.conv;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;
import org.openapitools.client.model.SdmlMonitorNotifyRelationResponse;

import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.sdml.xml.AutoMonitorCommonNotifyId;
import com.clustercontrol.utility.settings.sdml.xml.MonitorNotifyRelationList;
import com.clustercontrol.utility.settings.sdml.xml.NotifyId;
import com.clustercontrol.utility.settings.sdml.xml.SdmlControlInfoV1;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

/**
 * SDML制御設定(v1)のJavaBeanとXML(Bean)のbindingとの変換を行うクラス
 * 
 */
public class SdmlControlV1Conv extends BaseConv {
	// --- スキーマタイプを定義
	@Override
	protected String getType() {
		return "K";
	}

	@Override
	protected String getVersion() {
		return "1";
	}

	@Override
	protected String getRevision() {
		return "1";
	}

	/**
	 * DTOのBeanからXMLのBeanに変換する。
	 * 
	 * @param info
	 * @return
	 */
	public SdmlControlInfoV1 getXmlInfo(SdmlControlSettingInfoResponse info) {
		SdmlControlInfoV1 ret = new SdmlControlInfoV1();

		// 情報のセット(主部分)
		ret.setApplicationId(info.getApplicationId());
		ret.setDescription(info.getDescription());
		ret.setOwnerRoleId(info.getOwnerRoleId());
		ret.setFacilityId(info.getFacilityId());
		ret.setScope(info.getScope());
		ret.setValidFlg(info.getValidFlg());

		ret.setControlLogDirectory(info.getControlLogDirectory());
		ret.setControlLogFilename(info.getControlLogFilename());
		ret.setControlLogCollectFlg(info.getControlLogCollectFlg());
		ret.setApplication(info.getApplication());

		ret.setAutoMonitorDeleteFlg(info.getAutoMonitorDeleteFlg());
		ret.setAutoMonitorCalendarId(info.getAutoMonitorCalendarId());

		ret.setEarlyStopThresholdSecond(info.getEarlyStopThresholdSecond());
		ret.setEarlyStopNotifyPriority(convertPriority(info.getEarlyStopNotifyPriority()));

		ret.setAutoCreateSuccessPriority(convertPriority(info.getAutoCreateSuccessPriority()));
		ret.setAutoEnableSuccessPriority(convertPriority(info.getAutoEnableSuccessPriority()));
		ret.setAutoDisableSuccessPriority(convertPriority(info.getAutoDisableSuccessPriority()));
		ret.setAutoUpdateSuccessPriority(convertPriority(info.getAutoUpdateSuccessPriority()));
		ret.setAutoControlFailedPriority(convertPriority(info.getAutoControlFailedPriority()));

		// 通知（基本情報）
		List<NotifyId> notifyIdList = new ArrayList<>();
		NotifyId notifyId = null;
		for (NotifyRelationInfoResponse relInfo : info.getNotifyRelationList()) {
			notifyId = new NotifyId();
			notifyId.setNotifyId(relInfo.getNotifyId());
			notifyId.setNotifyType(OpenApiEnumConverter.enumToInteger(relInfo.getNotifyType()));
			notifyIdList.add(notifyId);
		}
		ret.setNotifyId(notifyIdList.toArray(new NotifyId[0]));

		// 種別共通通知（自動作成監視）
		List<AutoMonitorCommonNotifyId> commonNotifyIdList = new ArrayList<>();
		AutoMonitorCommonNotifyId commonNotifyId = null;
		for (NotifyRelationInfoResponse relInfo : info.getAutoMonitorCommonNotifyRelationList()) {
			commonNotifyId = new AutoMonitorCommonNotifyId();
			commonNotifyId.setNotifyId(relInfo.getNotifyId());
			commonNotifyId.setNotifyType(OpenApiEnumConverter.enumToInteger(relInfo.getNotifyType()));
			commonNotifyIdList.add(commonNotifyId);
		}
		ret.setAutoMonitorCommonNotifyId(commonNotifyIdList.toArray(new AutoMonitorCommonNotifyId[0]));

		// 個別通知（自動作成監視）
		List<MonitorNotifyRelationList> relationList = new ArrayList<>();
		MonitorNotifyRelationList relation = null;
		for (SdmlMonitorNotifyRelationResponse relInfo : info.getSdmlMonitorNotifyRelationList()) {
			relation = new MonitorNotifyRelationList();
			relation.setSdmlMonitorTypeId(relInfo.getSdmlMonitorTypeId());

			List<NotifyId> nList = new ArrayList<>();
			NotifyId id = null;
			for (NotifyRelationInfoResponse r : relInfo.getNotifyRelationList()) {
				id = new NotifyId();
				id.setNotifyId(r.getNotifyId());
				id.setNotifyType(OpenApiEnumConverter.enumToInteger(r.getNotifyType()));
				nList.add(id);
			}
			relation.setNotifyId(nList.toArray(new NotifyId[0]));

			relationList.add(relation);
		}
		ret.setMonitorNotifyRelationList(relationList.toArray(new MonitorNotifyRelationList[0]));

		return ret;
	}

	/**
	 * XMLのBeanからDTOのBeanに変換する。
	 * 
	 * @param info
	 * @return
	 */
	public SdmlControlSettingInfoResponse getDTO(SdmlControlInfoV1 info) {
		SdmlControlSettingInfoResponse ret = new SdmlControlSettingInfoResponse();

		// 情報のセット(主部分)
		ret.setApplicationId(info.getApplicationId());
		ret.setDescription(info.getDescription());
		ret.setOwnerRoleId(info.getOwnerRoleId());
		ret.setFacilityId(info.getFacilityId());
		// ret.setScope(info.getScope()); // 無視
		ret.setValidFlg(info.getValidFlg());

		ret.setControlLogDirectory(info.getControlLogDirectory());
		ret.setControlLogFilename(info.getControlLogFilename());
		ret.setControlLogCollectFlg(info.getControlLogCollectFlg());
		ret.setApplication(info.getApplication());

		ret.setAutoMonitorDeleteFlg(info.getAutoMonitorDeleteFlg());
		ret.setAutoMonitorCalendarId(info.getAutoMonitorCalendarId());

		ret.setEarlyStopThresholdSecond(info.getEarlyStopThresholdSecond());
		ret.setEarlyStopNotifyPriority(convertPriority(info.getEarlyStopNotifyPriority(),
				SdmlControlSettingInfoResponse.EarlyStopNotifyPriorityEnum.class));

		ret.setAutoCreateSuccessPriority(convertPriority(info.getAutoCreateSuccessPriority(),
				SdmlControlSettingInfoResponse.AutoCreateSuccessPriorityEnum.class));
		ret.setAutoEnableSuccessPriority(convertPriority(info.getAutoEnableSuccessPriority(),
				SdmlControlSettingInfoResponse.AutoEnableSuccessPriorityEnum.class));
		ret.setAutoDisableSuccessPriority(convertPriority(info.getAutoDisableSuccessPriority(),
				SdmlControlSettingInfoResponse.AutoDisableSuccessPriorityEnum.class));
		ret.setAutoUpdateSuccessPriority(convertPriority(info.getAutoUpdateSuccessPriority(),
				SdmlControlSettingInfoResponse.AutoUpdateSuccessPriorityEnum.class));
		ret.setAutoControlFailedPriority(convertPriority(info.getAutoControlFailedPriority(),
				SdmlControlSettingInfoResponse.AutoControlFailedPriorityEnum.class));

		// 通知（基本情報）
		NotifyRelationInfoResponse notifyInfo = null;
		for (NotifyId notify : info.getNotifyId()) {
			notifyInfo = new NotifyRelationInfoResponse();
			notifyInfo.setNotifyId(notify.getNotifyId());
			ret.addNotifyRelationListItem(notifyInfo);
		}

		// 種別共通通知（自動作成監視）
		for (AutoMonitorCommonNotifyId notify : info.getAutoMonitorCommonNotifyId()) {
			notifyInfo = new NotifyRelationInfoResponse();
			notifyInfo.setNotifyId(notify.getNotifyId());
			ret.addAutoMonitorCommonNotifyRelationListItem(notifyInfo);
		}

		// 個別通知（自動作成監視）
		SdmlMonitorNotifyRelationResponse relInfo = null;
		for (MonitorNotifyRelationList relation : info.getMonitorNotifyRelationList()) {
			relInfo = new SdmlMonitorNotifyRelationResponse();
			relInfo.setSdmlMonitorTypeId(relation.getSdmlMonitorTypeId());

			for (NotifyId notify : relation.getNotifyId()) {
				notifyInfo = new NotifyRelationInfoResponse();
				notifyInfo.setNotifyId(notify.getNotifyId());
				relInfo.addNotifyRelationListItem(notifyInfo);
			}

			ret.addSdmlMonitorNotifyRelationListItem(relInfo);
		}

		return ret;
	}

	@SuppressWarnings("unchecked")
	private <T extends Enum<T>> Integer convertPriority(T value) {
		String str = PriorityMessage.enumToString(value, (Class<T>) value.getClass());
		return PriorityMessage.stringToType(str);
	}

	private <T extends Enum<T>> T convertPriority(int value, Class<T> enumType) {
		String str = PriorityMessage.typeToString(value);
		return PriorityMessage.stringToEnum(str, enumType);
	}
}
