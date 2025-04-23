/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CommandTemplateNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.RestAccessNotFound;
import com.clustercontrol.fault.UsedCommandTemplate;
import com.clustercontrol.infra.util.InfraManagementValidator;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventUserExtensionItemInfo;
import com.clustercontrol.monitor.util.EventHinemosPropertyUtil;
import com.clustercontrol.notify.bean.CommandSettingTypeConstant;
import com.clustercontrol.notify.bean.EventNotifyInfo;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.bean.RenotifyTypeConstant;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.model.CommandTemplateInfo;
import com.clustercontrol.notify.model.NotifyCloudInfo;
import com.clustercontrol.notify.model.NotifyCommandInfo;
import com.clustercontrol.notify.model.NotifyEventInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfoDetail;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.notify.model.NotifyMessageInfo;
import com.clustercontrol.notify.model.NotifyRestInfo;
import com.clustercontrol.notify.model.NotifyStatusInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyJobTypeEnum;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;

/**
 * 通知の入力チェッククラス
 * 
 * @since 4.0
 */
public class NotifyValidator {

	private static Log m_log = LogFactory.getLog(NotifyValidator.class);

	private static int getPriorityCode(int i) {
		Integer[] priorities = new Integer[] { PriorityConstant.TYPE_INFO,
				PriorityConstant.TYPE_WARNING,
				PriorityConstant.TYPE_CRITICAL,
				PriorityConstant.TYPE_UNKNOWN };
		return priorities[i];
	}

	private static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

	private static void throwInvalidSetting(HinemosException e)
			throws InvalidSetting {
		m_log.info("validateNotifyInfo() : " + e.getClass().getSimpleName()
				+ ", " + e.getMessage());
		throw new InvalidSetting(e.getMessage(), e);
	}

	private static void throwInvalidSetting(String messageId)
			throws InvalidSetting {
		InvalidSetting e = new InvalidSetting(messageId);
		m_log.info("validateNotifyInfo() : " + e.getClass().getSimpleName()
				+ ", " + e.getMessage());
		throw e;
	}

	private static boolean validateCommandInfo(NotifyCommandInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		// 実効ユーザ
		String[] effectiveUsers = new String[] {
				info.getInfoEffectiveUser(),
				info.getWarnEffectiveUser(),
				info.getCriticalEffectiveUser(),
				info.getUnknownEffectiveUser()
		};

		// 実効ユーザの文字列長チェック(nullチェックなし)
		for(String effectiveUser:Arrays.asList(effectiveUsers)){
			CommonValidator.validateString(MessageConstant.EFFECTIVE_USER.getMessage(), effectiveUser,
					false, 0, 64);
		}
		
		// 実行コマンド
		String[] commands = new String[] { info.getInfoCommand(),
				info.getWarnCommand(), info.getCriticalCommand(),
				info.getUnknownCommand() };

		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			// 実効ユーザのnullチェック
			if (effectiveUsers[validFlgIndex] == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_EFFECTIVEUSER.getMessage());
			}

			if (isNullOrEmpty(commands[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMAND_NOTIFY.getMessage());
			}
			// コマンドテンプレートを利用する場合、存在しないテンプレートIDを指定していたら設定は登録しない
			if (info.getCommandSettingType().equals(CommandSettingTypeConstant.CHOICE_TEMPLATE)) {
				try {
					QueryUtil.getCommandTemplateInfoPK(commands[validFlgIndex]);
				} catch (CommandTemplateNotFound | InvalidRole e) {
					throwInvalidSetting(e);
				}
			} else {
				CommonValidator.validateString("command", commands[validFlgIndex], true, 1, 1024);
			}
		}
		
		// タイムアウト
		if(info.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIMEOUT.getMessage());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.TIME_OUT.getMessage(),
				info.getTimeout(), 1, 60 * 60 * 1000);

		return true;
	}

	private static boolean validateEventInfo(NotifyEventInfo info,
			NotifyInfo notifyInfo) {
		return !NotifyUtil.getValidFlgIndexes(info).isEmpty();
	}

	private static boolean validateJobInfo(NotifyJobInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		if (NotifyJobTypeEnum.DIRECT.getCode().equals(info.getNotifyJobType())) {
			if (info.getJobExecFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
				// 固定スコープを選択している場合の確認
				if (info.getJobExecFacility() == null) {
					throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE_NOTIFY.getMessage());
				}
				try {
					FacilityTreeCache.validateFacilityId(info.getJobExecFacility(),
							notifyInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					throwInvalidSetting(e);
				}
			}
			String[] jobIds = new String[] {
					info.getInfoJobId(),
					info.getWarnJobId(),
					info.getCriticalJobId(),
					info.getUnknownJobId()
			};
			for (int i = 0; i < validFlgIndexes.size(); i++) {
				int validFlgIndex = validFlgIndexes.get(i);
				if (isNullOrEmpty(jobIds[validFlgIndex])) {
					throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOBID.getMessage(
							Messages.getString(PriorityConstant.typeToMessageCode(getPriorityCode(validFlgIndex)))));
				}
			}
		} else if (NotifyJobTypeEnum.JOB_LINK_SEND.getCode().equals(info.getNotifyJobType())) {

			// ジョブ連携送信設定IDチェック
			CommonValidator.validateJoblinkSendSettingId(info.getJoblinkSendSettingId(), notifyInfo.getOwnerRoleId());

		}

		return true;
	}

	private static boolean validateLogInfo(NotifyLogEscalateInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		// 固定スコープを選択している場合の確認
		if (info.getEscalateFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
			if (info.getEscalateFacility() == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE_NOTIFY.getMessage());
			}
		}

		if (info.getEscalateFacility() != null){
			try {
				FacilityTreeCache.validateFacilityId(
						info.getEscalateFacility(),
						notifyInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throwInvalidSetting(e);
			}
		}

		Integer[] syslogFacilities = new Integer[] {
				info.getInfoSyslogFacility(), info.getWarnSyslogFacility(),
				info.getCriticalSyslogFacility(),
				info.getUnknownSyslogFacility() };
		Integer[] syslogPriorities = new Integer[] {
				info.getInfoSyslogPriority(), info.getWarnSyslogPriority(),
				info.getCriticalSyslogPriority(),
				info.getUnknownSyslogPriority() };
		String[] escalateMessages = new String[] {
				info.getInfoEscalateMessage(), info.getWarnEscalateMessage(),
				info.getCriticalEscalateMessage(),
				info.getUnknownEscalateMessage() };

		Locale locale = NotifyUtil.getNotifyLocale();
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (syslogFacilities[validFlgIndex] == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FACILIY.getMessage(Messages.getString(PriorityConstant.typeToMessageCode(getPriorityCode(validFlgIndex)), locale)));
			}
			if (syslogPriorities[validFlgIndex] == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_PRIORITY.getMessage(Messages.getString(PriorityConstant.typeToMessageCode(getPriorityCode(validFlgIndex)), locale)));
			}
			if (isNullOrEmpty(escalateMessages[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MESSAGE.getMessage(Messages.getString(PriorityConstant.typeToMessageCode(getPriorityCode(validFlgIndex)), locale)));
			}
			CommonValidator.validateString(MessageConstant.MESSAGE.getMessage(),
					escalateMessages[validFlgIndex], true, 1, 1024);

		}

		if (info.getEscalatePort() == null) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_PORT_NUM.getMessage());
		}
		CommonValidator.validateInt(MessageConstant.PORT_NUMBER.getMessage(),
				info.getEscalatePort(), 1, DataRangeConstant.PORT_NUMBER_MAX);

		return true;
	}

	private static boolean validateMailInfo(NotifyMailInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		// メールテンプレートの参照権限
		if (info.getMailTemplateId() != null) {
			try {
				// 存在確認
				com.clustercontrol.notify.mail.util.QueryUtil
						.getMailTemplateInfoPK(info.getMailTemplateId());
				// 権限確認
				com.clustercontrol.notify.mail.util.QueryUtil
						.getMailTemplateInfoPK_OR(info.getMailTemplateId(),
								notifyInfo.getOwnerRoleId());
			} catch (MailTemplateNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			} catch (InvalidRole e) {
				m_log.warn("validateNotifyInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		String[] mailAddresses = new String[] { info.getInfoMailAddress(),
				info.getWarnMailAddress(), info.getCriticalMailAddress(),
				info.getUnknownMailAddress() };
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);

			if (isNullOrEmpty(mailAddresses[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MAILADDR_CORRECT_FORMAT.getMessage());
			}

			CommonValidator.validateString(
					MessageConstant.EMAIL_ADDRESS_SSV.getMessage(), mailAddresses[validFlgIndex], true,
					1, 1024);
		}

		return true;
	}

	/**
	 * メールテンプレート情報の妥当性チェック
	 * 
	 * @param mailTemplateInfo
	 * @param addFlg
	 * @throws InvalidSetting
	 */
	public static void validateMailTemplateInfo(
			MailTemplateInfo mailTemplateInfo, boolean addFlg) throws InvalidSetting {
		// mailTemplateId
		CommonValidator.validateId(MessageConstant.MAIL_TEMPLATE_ID.getMessage(),
				mailTemplateInfo.getMailTemplateId(), 64);

		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				mailTemplateInfo.getDescription(), false, 0, 256);

		CommonValidator.validateString(MessageConstant.SUBJECT.getMessage(),
				mailTemplateInfo.getSubject(), true, 1, 256);

		// ownerRoleId
		if (addFlg) {
			CommonValidator.validateOwnerRoleId(mailTemplateInfo.getOwnerRoleId(), true,
					mailTemplateInfo.getMailTemplateId(), HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE);
		}

	}

	public static void validateNotifyInfo(NotifyInfo notifyInfo, boolean addFlg)
			throws InvalidSetting, InvalidRole  {
		// notifyId
		CommonValidator.validateId(MessageConstant.NOTIFY_ID.getMessage(),
				notifyInfo.getNotifyId(), 64);

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				notifyInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		if (addFlg) {
			CommonValidator.validateOwnerRoleId(notifyInfo.getOwnerRoleId(), true, notifyInfo.getNotifyId(),
					HinemosModuleConstant.PLATFORM_NOTIFY);
		}
		// calendarId
		CommonValidator.validateCalenderId(notifyInfo.getCalendarId(), false, notifyInfo.getOwnerRoleId());
		
		// 再通知抑制期間
		// 期間で抑制する場合は抑制期間の入力が必須
		if(notifyInfo.getRenotifyPeriod() != null || notifyInfo.getRenotifyType() == RenotifyTypeConstant.TYPE_PERIOD) {
			CommonValidator.validateInt(
					MessageConstant.SUPPRESS_BY_TIME_INTERVAL.getMessage(),
					notifyInfo.getRenotifyPeriod(), 1,
					DataRangeConstant.SMALLINT_HIGH);
		}
		// 初回通知するまでのカウント
		int maxInitialCount = HinemosPropertyCommon.notify_initial_count_max.getIntegerValue();
		CommonValidator.validateInt(MessageConstant.NOTIFY_INITIAL.getMessage(),
				notifyInfo.getInitialCount(), 1, maxInitialCount - 1);

		// コマンド通知
		boolean result = true;
		switch (notifyInfo.getNotifyType()) {
		case NotifyTypeConstant.TYPE_COMMAND:
			NotifyCommandInfo command = notifyInfo.getNotifyCommandInfo();
			result = validateCommandInfo(command, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_EVENT:
			NotifyEventInfo event = notifyInfo.getNotifyEventInfo();
			result = validateEventInfo(event, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_JOB:
			NotifyJobInfo job = notifyInfo.getNotifyJobInfo();
			result = validateJobInfo(job, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			NotifyLogEscalateInfo log = notifyInfo.getNotifyLogEscalateInfo();
			result = validateLogInfo(log, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_MAIL:
			NotifyMailInfo mail = notifyInfo.getNotifyMailInfo();
			result = validateMailInfo(mail, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_STATUS:
			NotifyStatusInfo status = notifyInfo.getNotifyStatusInfo();
			result = validateStatusInfo(status, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_INFRA:
			NotifyInfraInfo infra = notifyInfo.getNotifyInfraInfo();
			result = validateInfraInfo(infra, notifyInfo);
			break;
		case NotifyTypeConstant.TYPE_REST:
			NotifyRestInfo rest = notifyInfo.getNotifyRestInfo();
			result = validateRestInfo(rest, notifyInfo);
			break;
			
		case NotifyTypeConstant.TYPE_CLOUD:
			NotifyCloudInfo cloud = notifyInfo.getNotifyCloudInfo();
			result = validateCloudInfo(cloud, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_MESSAGE:
			NotifyMessageInfo message = notifyInfo.getNotifyMessageInfo();
			result = validateMessageInfo(message, notifyInfo);
			break;

		default:
			break;
		}
		if (!result) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_INFO_ONE_OR_MORE.getMessage());
		}
	}

	public static boolean validateEventNotify(EventNotifyInfo eventNotifyInfo, Map<Integer, EventUserExtensionItemInfo> userExtenstionItemInfoMap) throws InvalidSetting, InvalidRole, HinemosUnknown {
		
		CommonValidator.validateNull("eventNotifyInfo", eventNotifyInfo);
		CommonValidator.validateString(MessageConstant.MONITOR_ID.getMessage(), 
				eventNotifyInfo.getMonitorId(), true, 0, 64);
		CommonValidator.validateString(MessageConstant.MONITOR_DETAIL_ID.getMessage(), 
				eventNotifyInfo.getMonitorDetail(), true, 0, 1024);
		CommonValidator.validateString(MessageConstant.PLUGIN_ID.getMessage(), 
				eventNotifyInfo.getPluginId(), true, 0, 64);
		CommonValidator.validateString(MessageConstant.FACILITY_ID.getMessage(), 
				eventNotifyInfo.getFacilityId(), true, 0, 512);
		CommonValidator.validateString(MessageConstant.SCOPE.getMessage(), 
				eventNotifyInfo.getScopeText(), true, 0, 512);
		CommonValidator.validateString(MessageConstant.APPLICATION.getMessage(), 
				eventNotifyInfo.getApplication(), true, 0, 64);
		CommonValidator.validateLong(MessageConstant.GENERATION_TIME.getMessage(), 
				eventNotifyInfo.getGenerationDate(), 0L, Long.MAX_VALUE);
		CommonValidator.validatePriority(MessageConstant.PRIORITY.getMessage(), 
				eventNotifyInfo.getPriority(), false);
		CommonValidator.validateConfirm(MessageConstant.CONFIRMED.getMessage(),
				eventNotifyInfo.getConfirmFlg());
		if (eventNotifyInfo.getOwnerRoleId() != null) {
			
			ArrayList<String> ownerRoleIdList = new AccessControllerBean().getOwnerRoleIdList();
			
			if (ownerRoleIdList == null || !ownerRoleIdList.contains(eventNotifyInfo.getOwnerRoleId())) {
				throw new InvalidSetting("invalid ownerRole.");
			}
		}
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			validateUserExtensionItem(
					NotifyUtil.getUserItemValue(eventNotifyInfo, i), 
					userExtenstionItemInfoMap.get(i), i, false);
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param value チェック対象の値
	 * @param itemInfo ユーザ拡張イベント項目の設定
	 * @param i ユーザ拡張イベント項目のindex
	 * @param isValidate バリデーションするか　新規登録／更新の仕様を吸収するためのフラグ（新規登録の時、バリデーションあり、更新の時、バリデーションなし）
	 * @return
	 * @throws InvalidSetting
	 */
	public static boolean validateUserExtensionItem(String value, EventUserExtensionItemInfo itemInfo, Integer i, boolean isValidate) throws InvalidSetting {
		final int eventItemLength = 4096;
		
		if (value == null) {
			//nullはチェックしない（新規の場合はデフォルト値、更新時は元の値のまま）
			return true;
		}
		
		String itemName = EventHinemosPropertyUtil.getDisplayName(itemInfo.getDisplayName(), i);
		
		//桁数チェック
		if (value.length() > eventItemLength) {
			throwInvalidSetting(MessageConstant.MESSAGE_INPUT_OVER_LIMIT.getMessage(itemName, String.valueOf(eventItemLength)));
		}
		
		if (!isValidate) {
			//新規の場合は以降のチェックは行わない
			return true;
		}
		
		//必須チェック
		if (itemInfo.getModifyRequired() && "".equals(value)) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(itemName));
		}
		
		if ("".equals(value)) {
			//空白の場合、以降のチェックはしない
			return true;
		}
		
		String validationType = itemInfo.getModifyValidation();
		String validateFormat = itemInfo.getModifyFormat();
		
		if (validationType == null || "".equals(validationType)) {
			//形式チェックの指定がない時
			return true;
		}
		
		if (!EventHinemosPropertyConstant.isValidatonType(validationType)) {
			//形式チェックの指定が誤っている場合
			//ログを出力し、処理を継続
			m_log.info(String.format(
					"invalid validation type. monitor.event.useritem.item%02d.modify.validation=%s",
					i, validationType));
			return true;
		}
		
		if (validateFormat == null || "".equals(validateFormat)) {
			//バリデーションのフォーマットが指定されていないとき
			m_log.info(String.format(
					"validation format not input. monitor.event.useritem.item%02d.modify.format=%s",
					i, validationType));
			return true;
		}
		
		if (EventHinemosPropertyConstant.USER_ITEM_VALIDATION_TYPE_REGEXP.equals(validationType)) {
			//正規表現の場合
			validteUserItemRegExp(validateFormat, value, i, itemName);
			
		} else if (EventHinemosPropertyConstant.USER_ITEM_VALIDATION_TYPE_DATEFORMAT.equals(validationType)) {
			//日付書式の場合
			validateUserItemDateFormat(validateFormat, value, i, itemName);
		}
		return true;
	}
	
	private static void validteUserItemRegExp(String validateFormat, String value, int index, String itemName) throws InvalidSetting {
		Pattern pattern = null;
		
		try {
			pattern = Pattern.compile(validateFormat);
		} catch (PatternSyntaxException e) {
		}
		
		if (pattern == null) {
			//正規表現のパターンではないとき、ログを出力し、処理は行う
			m_log.info(String.format(
					"not regexp format. monitor.event.useritem.item%02d.modify.format=%s",
					index, validateFormat));
			return;
		}
		
		//正規表現のチェック
		if (!pattern.matcher(value).matches()) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_EVENT_USER_EXNTENSION_ITEM_CORRECT_FORMAT.getMessage(itemName, validateFormat));
		}
	}
	
	private static void validateUserItemDateFormat(String validateFormat, String value, int index, String itemName) throws InvalidSetting {
		SimpleDateFormat sdf = null;
		
		try {
			sdf = new SimpleDateFormat(validateFormat);
		} catch (IllegalArgumentException e) {
		}
		
		if (sdf == null) {
			//日付書式でなかった場合、ログのみ出力し、チェックはしない
			m_log.info(String.format(
					"not date format. monitor.event.useritem.item%02d.modify.format=%s",
					index, validateFormat));
			return;
		}
		
		try { 
			sdf.parse(value);
		} catch (ParseException e) {
			//日付として判断できなかった時
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_EVENT_USER_EXNTENSION_ITEM_CORRECT_FORMAT.getMessage(itemName, validateFormat));
		}
	}

	
	private static boolean validateInfraInfo(NotifyInfraInfo info, NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		if (info.getInfraExecFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
			// 固定スコープを選択している場合の確認
			if (info.getInfraExecFacility() == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE_NOTIFY.getMessage());
			}
			try {
				FacilityTreeCache.validateFacilityId(info.getInfraExecFacility(),
						notifyInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throwInvalidSetting(e);
			}
		}

		String[] infraIds = new String[] {
				info.getInfoInfraId(),
				info.getWarnInfraId(),
				info.getCriticalInfraId(),
				info.getUnknownInfraId()
		};
		
		// 入力されて構築IDについて参照権限に関わらず存在チェック
		for(String infraId:Arrays.asList(infraIds)){
			if(infraId != null){
				InfraManagementValidator.validateInfraManagementId(infraId, true);
			}
		}
		
		MessageConstant[] priorities = new MessageConstant[] {
				MessageConstant.INFO,
				MessageConstant.WARNING,
				MessageConstant.CRITICAL,
				MessageConstant.UNKNOWN
		};
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (isNullOrEmpty(infraIds[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFRA_MANAGEMENT_ID.getMessage(priorities[validFlgIndex].getMessage()));
			}

			// 構築IDが指定されている場合、参照可能かどうかを確認する
			InfraManagementValidator.validateInfraManagementId(infraIds[validFlgIndex], notifyInfo.getOwnerRoleId());
		}

		return true;
	}

	private static boolean validateRestInfo(NotifyRestInfo info, NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		String[] restAccessIds = new String[] {
				info.getInfoRestAccessId(),
				info.getWarnRestAccessId(),
				info.getCriticalRestAccessId(),
				info.getUnknownRestAccessId()
		};
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			// IDが指定されている場合、参照可能かどうかを確認する
			if(restAccessIds[i] == null ){
				continue;
			}
			try {
				com.clustercontrol.notify.restaccess.util.RestAccessQueryUtil.getRestAccessInfoPK(restAccessIds[i]);
			} catch (RestAccessNotFound e) {
				String[] args = {restAccessIds[i]};
				throwInvalidSetting(MessageConstant.MESSAGE_REST_ACCESS_ID_NOT_EXIST.getMessage(args));
			}
		}

		return true;
	}

	private static boolean validateStatusInfo(NotifyStatusInfo info,
			NotifyInfo notifyInfo) {
		return validateNotifyInfoDetail(info);
	}

	private static boolean validateMessageInfo(NotifyMessageInfo info, NotifyInfo notifyInfo) {
		return validateNotifyInfoDetail(info);
	}

	private static boolean validateNotifyInfoDetail(NotifyInfoDetail info) {
		return !NotifyUtil.getValidFlgIndexes(info).isEmpty();
	}

	public static void validateCommandTemplateInfo(CommandTemplateInfo templateInfo)
			throws InvalidSetting, InvalidRole  {
		// ownerRoleId
		CommonValidator.validateOwnerRoleId(templateInfo.getOwnerRoleId(), true, templateInfo.getCommandTemplateId(),
				HinemosModuleConstant.PLATFORM_NOTIFY);
	}

	public static void validateDeleteCommandTemplateInfo(String commandTemplateId)
			throws InvalidSetting, InvalidRole, UsedCommandTemplate, HinemosUnknown  {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// コマンド通知(コマンド通知テンプレートを利用)の取得
			List<NotifyCommandInfo> list = QueryUtil.getNotifyCommandInfoByCommandSettingType(CommandSettingTypeConstant.CHOICE_TEMPLATE);
			List<String> templateUsageIdList = new ArrayList<>();
			
			for (NotifyCommandInfo info : list) {
				if (commandTemplateId.equals(info.getInfoCommand())) {
					templateUsageIdList.add(info.getNotifyId());
				} else if (commandTemplateId.equals(info.getWarnCommand())) {
					templateUsageIdList.add(info.getNotifyId());
				} else if (commandTemplateId.equals(info.getCriticalCommand())) {
					templateUsageIdList.add(info.getNotifyId());
				} else if (commandTemplateId.equals(info.getUnknownCommand())) {
					templateUsageIdList.add(info.getNotifyId());
				}
			}
			// コマンド通知でコマンド通知テンプレートIDを参照している場合に例外発生
			if (!templateUsageIdList.isEmpty()) {
				String message = (MessageConstant.NOTIFY.getMessage() + " : " + String.join(",", templateUsageIdList));
				UsedCommandTemplate e = new UsedCommandTemplate(commandTemplateId, message);
				
				m_log.info("validateDeleteCommandTemplateInfo() : "
						+ e.getClass().getSimpleName() + ", " 
						+ commandTemplateId + ", " + e.getMessage());
				throw e;
			}

			jtm.commit();
		} catch (UsedCommandTemplate e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("validateDeleteCommandTemplateInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage());
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	private static boolean validateCloudInfo(NotifyCloudInfo cloudInfo, NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole{
		//スコープチェック
		if (cloudInfo.getFacilityId() == null || cloudInfo.getFacilityId().isEmpty()) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE_NOTIFY.getMessage());
		}

		// コマンドラインツール、Utilityを考慮し、
		// ここで厳密に指定されたFacilityIDがパブリッククラウドスコープ、
		// かつオーナーロールから参照可能であるかを確認しておく
		if (notifyInfo.getOwnerRoleId().equals(RoleIdConstant.ADMINISTRATORS)) {
			// ADMINISTRATORの場合は何もしない
		} else {
			try (SessionScope sessionScope = SessionScope.open()) {
				// cloudScopeIDの取得
				String prefixRemoved = cloudInfo.getFacilityId().replaceFirst("_[A-Z]+_[A-Z]+_", "");
				String cloudScopeId = prefixRemoved.substring(0, prefixRemoved.lastIndexOf("_"));
				boolean found = false;
				// オーナーロールに紐づくクラウドアカウントの取得
				List<CloudLoginUserEntity> userList = null;
				userList = CloudManager.singleton().getLoginUsers()
						.getCloudLoginUserByRole(notifyInfo.getOwnerRoleId());
				for (CloudLoginUserEntity e : userList) {
					if (cloudScopeId.equals(e.getCloudScopeId())) {
						if (e.getCloudScope().isPublic()) {
							found = true;
							break;
						}
					}
				}

				if (!found) {
					m_log.warn("validateCloudInfo(): CloudScope Not Found For FacilityID: "+cloudInfo.getFacilityId());
					throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_PUBLIC_CLOUD.getMessage());
				}

			} catch (RuntimeException e1) {
				// findbugs対応 catch (Exception e) に対して RuntimeException
				// のキャッチを明示化
				m_log.warn("validateCloudInfo():",e1);
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_PUBLIC_CLOUD.getMessage());
			} catch (Exception e1) {
				m_log.warn("validateCloudInfo():",e1);
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_PUBLIC_CLOUD.getMessage());
			}
		}
		
		// 通知フラグチェック
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(cloudInfo);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}
		
		//AWS, Azure以外は不正
		switch (cloudInfo.getPlatformType()){
		case CloudConstant.notify_aws_platform:
			//aws
			if(cloudInfo.getInfoValidFlg()){
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_SOURCE.getMessage(), cloudInfo.getInfoSource(), true,
						1, 256);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_DETAIL_TYPE.getMessage(), cloudInfo.getInfoDetailType(), true,
						1, 128);
			}
			if(cloudInfo.getWarnValidFlg()){
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_SOURCE.getMessage(), cloudInfo.getWarnSource(), true,
						1, 256);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_DETAIL_TYPE.getMessage(), cloudInfo.getWarnDetailType(), true,
						1, 128);
			}
			if(cloudInfo.getCriticalValidFlg()){
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_SOURCE.getMessage(), cloudInfo.getCritSource(), true,
						1, 256);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_DETAIL_TYPE.getMessage(), cloudInfo.getCritDetailType(), true,
						1, 128);
			}
			if(cloudInfo.getUnknownValidFlg()){
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_SOURCE.getMessage(), cloudInfo.getUnkSource(), true,
						1, 256);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_DETAIL_TYPE.getMessage(), cloudInfo.getUnkDetailType(), true,
						1, 128);
			}
			break;
		case CloudConstant.notify_azure_platform:
			//azure
			if(cloudInfo.getInfoValidFlg()){
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_ENDPOINT.getMessage(),
						cloudInfo.getInfoEventBus(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_ACCESS_KEY.getMessage(),
						cloudInfo.getInfoAccessKey(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_SUBJECT.getMessage(),
						cloudInfo.getInfoDetailType(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_EVENT_TYPE.getMessage(),
						cloudInfo.getInfoSource(), true, 1, 4096);
			}
			if(cloudInfo.getWarnValidFlg()){
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_ENDPOINT.getMessage(),
						cloudInfo.getWarnEventBus(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_ACCESS_KEY.getMessage(),
						cloudInfo.getWarnAccessKey(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_SUBJECT.getMessage(),
						cloudInfo.getWarnDetailType(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_EVENT_TYPE.getMessage(),
						cloudInfo.getWarnSource(), true, 1, 4096);
			}
			if(cloudInfo.getCriticalValidFlg()){
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_ENDPOINT.getMessage(),
						cloudInfo.getCritEventBus(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_ACCESS_KEY.getMessage(),
						cloudInfo.getCritAccessKey(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_SUBJECT.getMessage(),
						cloudInfo.getCritDetailType(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_EVENT_TYPE.getMessage(),
						cloudInfo.getCritSource(), true, 1, 4096);
			}
			if(cloudInfo.getUnknownValidFlg()){
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_ENDPOINT.getMessage(),
						cloudInfo.getUnkEventBus(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_ACCESS_KEY.getMessage(),
						cloudInfo.getUnkAccessKey(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_SUBJECT.getMessage(),
						cloudInfo.getUnkDetailType(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_EVENT_TYPE.getMessage(),
						cloudInfo.getUnkSource(), true, 1, 4096);
			}
			
			break;
		case CloudConstant.notify_gcp_platform:
			// gcp
			if (cloudInfo.getInfoValidFlg()) {
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_PROJECT_ID.getMessage(),
						cloudInfo.getInfoEventBus(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_TOPIC_ID.getMessage(),
						cloudInfo.getInfoSource(), true, 1, 4096);
				if ((cloudInfo.getInfoDetailType() == null || cloudInfo.getInfoDetailType().isEmpty())
						&& (cloudInfo.getInfoJsonData() == null || cloudInfo.getInfoJsonData().isEmpty())) {
					throwInvalidSetting(MessageConstant.XCLOUD_NOTIFY_MESSAGE_ATTRIBUTE.getMessage());
				}
			}
			if (cloudInfo.getWarnValidFlg()) {
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_PROJECT_ID.getMessage(),
						cloudInfo.getWarnEventBus(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_TOPIC_ID.getMessage(),
						cloudInfo.getWarnSource(), true, 1, 4096);
				if ((cloudInfo.getWarnDetailType() == null || cloudInfo.getWarnDetailType().isEmpty())
						&& (cloudInfo.getWarnJsonData() == null || cloudInfo.getWarnJsonData().isEmpty())) {
					throwInvalidSetting(MessageConstant.XCLOUD_NOTIFY_MESSAGE_ATTRIBUTE.getMessage());
				}
			}
			if (cloudInfo.getCriticalValidFlg()) {
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_PROJECT_ID.getMessage(),
						cloudInfo.getCritEventBus(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_TOPIC_ID.getMessage(),
						cloudInfo.getCritSource(), true, 1, 4096);
				if ((cloudInfo.getCritDetailType() == null || cloudInfo.getCritDetailType().isEmpty())
						&& (cloudInfo.getCritJsonData() == null || cloudInfo.getCritJsonData().isEmpty())) {
					throwInvalidSetting(MessageConstant.XCLOUD_NOTIFY_MESSAGE_ATTRIBUTE.getMessage());
				}
			}
			if (cloudInfo.getUnknownValidFlg()) {
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_PROJECT_ID.getMessage(),
						cloudInfo.getUnkEventBus(), true, 1, 4096);
				CommonValidator.validateString(MessageConstant.XCLOUD_NOTIFY_TOPIC_ID.getMessage(),
						cloudInfo.getUnkSource(), true, 1, 4096);
				if ((cloudInfo.getUnkDetailType() == null || cloudInfo.getUnkDetailType().isEmpty())
						&& (cloudInfo.getUnkJsonData() == null || cloudInfo.getUnkJsonData().isEmpty())) {
					throwInvalidSetting(MessageConstant.XCLOUD_NOTIFY_MESSAGE_ATTRIBUTE.getMessage());
				}
			}
			break;
			
		default:
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_PUBLIC_CLOUD.getMessage());
			
		}
					
		return true;
	}
	

}
