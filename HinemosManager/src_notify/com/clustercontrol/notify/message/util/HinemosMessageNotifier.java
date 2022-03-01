/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.message.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.message.model.HinemosMessageJsonModel;
import com.clustercontrol.notify.model.NotifyMessageInfo;
import com.clustercontrol.notify.util.DependDbNotifier;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.util.HinemosMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HinemosMessageNotifier implements DependDbNotifier {

	@Override
	public void notify(NotifyRequestMessage message) throws Exception {
		String messageJson = createJson(message.getNotifyId(), message.getOutputInfo());
		HinemosMessageManager.add(messageJson);
	}

	/**
	 * Hinemosメッセージ内のmessages[]の１要素となるJSON文字列を作成します。
	 * OutputBasicInfoの重要度に対応するルールベースが無効な場合、nullを返します。
	 * 
	 * @param notifyId メッセージ通知のnotifyId
	 * @param basicInfo メッセージ通知を発生させた通知情報
	 * @return
	 * @throws NotifyNotFound 
	 * @throws JsonProcessingException 
	 */
	private String createJson(String notifyId, OutputBasicInfo basicInfo) throws NotifyNotFound, JsonProcessingException {
		String formatString = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		String priority = PriorityConstant.typeToMessageCode(basicInfo.getPriority()).toLowerCase();
		String ruleBase = getRuleBase(notifyId, basicInfo.getPriority());
		Locale locale = NotifyUtil.getNotifyLocale();

		// JsonModel変換用のモデルクラス生成
		HinemosMessageJsonModel.MessageJsonModel jsonModel = new HinemosMessageJsonModel.MessageJsonModel();
		jsonModel.setMessageId(basicInfo.getNotifyUUID());
		jsonModel.setPriority(priority);
		jsonModel.setRulebase(ruleBase);
		jsonModel.setGeneratedTime(format.format(new Date(basicInfo.getGenerationDate())));
		jsonModel.setNotifyId(notifyId);
		jsonModel.setPluginId(basicInfo.getPluginId());
		jsonModel.setMonitorId(basicInfo.getMonitorId());
		jsonModel.setMonitorDetail(basicInfo.getSubKey());
		jsonModel.setFacilityId(basicInfo.getFacilityId());
		jsonModel.setScopeText(HinemosMessage.replace(basicInfo.getScopeText(), locale));
		jsonModel.setApplication(HinemosMessage.replace(basicInfo.getApplication(), locale));
		jsonModel.setMessage(HinemosMessage.replace(basicInfo.getMessage(), locale));
		jsonModel.setMessageOrg(HinemosMessage.replace(basicInfo.getMessageOrg(), locale));
		jsonModel.setUserItem01(basicInfo.getUserItem01());
		jsonModel.setUserItem02(basicInfo.getUserItem02());
		jsonModel.setUserItem03(basicInfo.getUserItem03());
		jsonModel.setUserItem04(basicInfo.getUserItem04());
		jsonModel.setUserItem05(basicInfo.getUserItem05());
		jsonModel.setUserItem06(basicInfo.getUserItem06());
		jsonModel.setUserItem07(basicInfo.getUserItem07());
		jsonModel.setUserItem08(basicInfo.getUserItem08());
		jsonModel.setUserItem09(basicInfo.getUserItem09());
		jsonModel.setUserItem10(basicInfo.getUserItem10());
		jsonModel.setUserItem11(basicInfo.getUserItem11());
		jsonModel.setUserItem12(basicInfo.getUserItem12());
		jsonModel.setUserItem13(basicInfo.getUserItem13());
		jsonModel.setUserItem14(basicInfo.getUserItem14());
		jsonModel.setUserItem15(basicInfo.getUserItem15());
		jsonModel.setUserItem16(basicInfo.getUserItem16());
		jsonModel.setUserItem17(basicInfo.getUserItem17());
		jsonModel.setUserItem18(basicInfo.getUserItem18());
		jsonModel.setUserItem19(basicInfo.getUserItem19());
		jsonModel.setUserItem20(basicInfo.getUserItem20());
		jsonModel.setUserItem21(basicInfo.getUserItem21());
		jsonModel.setUserItem22(basicInfo.getUserItem22());
		jsonModel.setUserItem23(basicInfo.getUserItem23());
		jsonModel.setUserItem24(basicInfo.getUserItem24());
		jsonModel.setUserItem25(basicInfo.getUserItem25());
		jsonModel.setUserItem26(basicInfo.getUserItem26());
		jsonModel.setUserItem27(basicInfo.getUserItem27());
		jsonModel.setUserItem28(basicInfo.getUserItem28());
		jsonModel.setUserItem29(basicInfo.getUserItem29());
		jsonModel.setUserItem30(basicInfo.getUserItem30());
		jsonModel.setUserItem31(basicInfo.getUserItem31());
		jsonModel.setUserItem32(basicInfo.getUserItem32());
		jsonModel.setUserItem33(basicInfo.getUserItem33());
		jsonModel.setUserItem34(basicInfo.getUserItem34());
		jsonModel.setUserItem35(basicInfo.getUserItem35());
		jsonModel.setUserItem36(basicInfo.getUserItem36());
		jsonModel.setUserItem37(basicInfo.getUserItem37());
		jsonModel.setUserItem38(basicInfo.getUserItem38());
		jsonModel.setUserItem39(basicInfo.getUserItem39());
		jsonModel.setUserItem40(basicInfo.getUserItem40());

		// モデルクラス → JSON文字列
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(jsonModel);
	}

	/**
	 * DBから重要度に合わせたルールベースを取得します。
	 * 
	 * @param notifyId
	 * @param priority
	 * @return
	 * @throws NotifyNotFound 
	 */
	private String getRuleBase(String notifyId, int priority) throws NotifyNotFound {
		NotifyMessageInfo info = QueryUtil.getNotifyMessageInfoPK(notifyId);
		String ruleBase = null;
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			ruleBase = info.getInfoRulebaseId();
			break;
		case PriorityConstant.TYPE_WARNING:
			ruleBase = info.getWarnRulebaseId();
			break;
		case PriorityConstant.TYPE_CRITICAL:
			ruleBase = info.getCriticalRulebaseId();
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			ruleBase = info.getUnknownRulebaseId();
			break;
		default:
			ruleBase = null;
		}
		return ruleBase;
	}
}
