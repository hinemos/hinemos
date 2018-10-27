/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.infra.util.InfraManagementValidator;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.model.NotifyCommandInfo;
import com.clustercontrol.notify.model.NotifyEventInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.notify.model.NotifyStatusInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;

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
		// 実行コマンド
		String[] commands = new String[] { info.getInfoCommand(),
				info.getWarnCommand(), info.getCriticalCommand(),
				info.getUnknownCommand() };

		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (effectiveUsers[validFlgIndex] == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_EFFECTIVEUSER.getMessage());
			}
			CommonValidator.validateString("effective.user", effectiveUsers[validFlgIndex],
					true, 0, 64);

			if (isNullOrEmpty(commands[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMAND_NOTIFY.getMessage());
			}
			CommonValidator.validateString("command", commands[validFlgIndex], true, 1, 1024);
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
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOBID.getMessage());
			}
		}

		return true;
	}

	private static boolean validateLogInfo(NotifyLogEscalateInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		if (info.getEscalateFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
			// 固定スコープを選択している場合の確認
			if (info.getEscalateFacility() == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE_NOTIFY.getMessage());
			}

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
	 * @throws InvalidSetting
	 */
	public static void validateMailTemplateInfo(
			MailTemplateInfo mailTemplateInfo) throws InvalidSetting {
		// mailTemplateId
		CommonValidator.validateId(MessageConstant.MAIL_TEMPLATE_ID.getMessage(),
				mailTemplateInfo.getMailTemplateId(), 64);

		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				mailTemplateInfo.getDescription(), false, 0, 256);

		CommonValidator.validateString(MessageConstant.SUBJECT.getMessage(),
				mailTemplateInfo.getSubject(), true, 1, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(mailTemplateInfo.getOwnerRoleId(),
				true, mailTemplateInfo.getMailTemplateId(),
				HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE);

	}

	public static void validateNotifyInfo(NotifyInfo notifyInfo)
			throws InvalidSetting, InvalidRole  {
		// notifyId
		CommonValidator.validateId(MessageConstant.NOTIFY_ID.getMessage(),
				notifyInfo.getNotifyId(), 64);

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				notifyInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		CommonValidator
				.validateOwnerRoleId(notifyInfo.getOwnerRoleId(), true,
						notifyInfo.getNotifyId(),
						HinemosModuleConstant.PLATFORM_NOTIFY);
		
		// calendarId
		CommonValidator.validateCalenderId(notifyInfo.getCalendarId(), false, notifyInfo.getOwnerRoleId());
		
		// 再通知抑制期間
		if (notifyInfo.getRenotifyPeriod() != null) {
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

		default:
			break;
		}
		if (!result) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_INFO_ONE_OR_MORE.getMessage());
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
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (isNullOrEmpty(infraIds[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFRA_MANAGEMENT_ID.getMessage());
			}

			// 構築IDが指定されている場合、参照可能かどうかを確認する
			InfraManagementValidator.validateInfraManagementId(infraIds[validFlgIndex], notifyInfo.getOwnerRoleId());
		}

		return true;
	}

	private static boolean validateStatusInfo(NotifyStatusInfo info,
			NotifyInfo notifyInfo) {
		return !NotifyUtil.getValidFlgIndexes(info).isEmpty();
	}
}
