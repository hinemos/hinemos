/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.util.MessageConstant;

/**
 * Hinemos の入力チェックで使用する共通メソッド
 * @since 4.0
 */
public class CommonValidator {

	private static Log m_log = LogFactory.getLog( CommonValidator.class );

	/**
	 * 指定されたIDがHinemosのID規則にマッチするかを確認する。
	 * [a-z,A-Z,0-9,-,_,.,@]のみ許可する (Hinemos5.0で「.」と「@」を追加)
	 * 
	 * @param id
	 * @throws InvalidSetting
	 */
	public static void validateId(String name, String id, int maxSize) throws InvalidSetting{

		// null check
		if(id == null || "".equals(id)){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ID_IS_NULL.getMessage(name));
			m_log.info("validateId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// string check
		validateString(name, id, false, 1, maxSize);

		/** メイン処理 */
		if(!id.matches(PatternConstant.HINEMOS_ID_PATTERN)){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ID_ILLEGAL_CHARACTERS.getMessage(id, name));
			m_log.info("validateId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 指定されたIDがHinemosのID規則にマッチするかを確認する。
	 * [a-z,A-Z,0-9,-,_,.,@]のみ許可する (Hinemos5.0で「.」と「@」を追加)
	 * 空文字列、null チェック行わない。
	 * 
	 * @param id
	 * @throws InvalidSetting
	 */
	public static void validateIdWithoutCheckNullAndEmpty(String name, String id, int maxSize) throws InvalidSetting{
		// string check
		validateString(name, id, false, 1, maxSize);

		/** メイン処理 */
		if(!id.matches(PatternConstant.HINEMOS_ID_PATTERN)){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ID_ILLEGAL_CHARACTERS.getMessage(id, name));
			m_log.info("validateId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * 文字列の長さチェック
	 * 
	 * @param name
	 * @param str
	 * @param nullcheck
	 * @param minSize
	 * @param maxSize
	 * @throws InvalidSetting
	 */
	public static void validateString(String name, String str, boolean nullcheck, int minSize, int maxSize) throws InvalidSetting{
		if(str == null){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		else{
			int size = str.length();
			if(size < minSize){
				if(size == 0){
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
					m_log.info("validateString() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}else{
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_UNDER.getMessage(name, String.valueOf(minSize)));
					m_log.info("validateString() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			if(size > maxSize){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_OVER_LIMIT.getMessage(name, String.valueOf(maxSize)));
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * 数値の上限下限チェック
	 * @throws InvalidSetting
	 */
	public static void validateDouble(String name, Double i, double minSize, double maxSize) throws InvalidSetting {
		if (i == null || i < minSize || maxSize < i) {
			String[] args = {name,
					((new BigDecimal(minSize)).toBigInteger()).toString(),
					((new BigDecimal(maxSize)).toBigInteger()).toString()};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(args));
			m_log.info("validateDouble() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 数値の上限下限チェック
	 * @throws InvalidSetting
	 */
	public static void validateInt(String name, Integer i, int minSize, int maxSize) throws InvalidSetting {
		if (i == null || i < minSize || maxSize < i) {
			String[] args = {name, Integer.toString(minSize), Integer.toString(maxSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(args));
			m_log.info("validateInt() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 数値の上限下限チェック
	 * {@link #validateInt(String, Integer, int, int)}のnull許容版。
	 * 
	 * @throws InvalidSetting
	 */
	public static void validateNullableInt(String name, Integer i, int minSize, int maxSize) throws InvalidSetting {
		if (i == null) return;
		validateInt(name, i, minSize, maxSize);
	}

	/**
	 * Integerを表す文字列の上限下限チェック
	 * 
	 * @param name エラー時に表示する項目名。
	 * @param strValue Integerを表す文字列。
	 * @param required 必須かどうか。nullまたは空文字列を許容しない場合は true、許容してデフォルト値を返す場合は false。
	 * @param defaultValue デフォルト値。
	 * @param minValue 最小値。
	 * @param maxValue 最大値。
	 * @return 数値変換した値。
	 */
	public static int validateIntegerString(String name, String strValue, boolean required,
			int defaultValue, int minValue, int maxValue) throws InvalidSetting {
		String[] args = { name, Integer.toString(minValue), Integer.toString(maxValue) };

		if (strValue == null || strValue.trim().length() == 0) {
			if (required) {
				throw new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(args));
			} else {
				return defaultValue;
			}
		}

		int intValue;
		try {
			intValue = Integer.parseInt(strValue);
		} catch (NumberFormatException e) {
			throw new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(args));
		}
		if (intValue < minValue || intValue > maxValue) {
			throw new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(args));
		}
		return intValue;
	}

	/**
	 * 数値の上限下限チェック
	 * @throws InvalidSetting
	 */
	public static void validateLong(String name, Long i, long minSize, long maxSize) throws InvalidSetting {
		if (i == null || i < minSize || maxSize < i) {
			String[] args = {name, Long.toString(minSize), Long.toString(maxSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(args));
			m_log.info("validateLong() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * calendarIdが空文字やnullかどうか、また対象のカレンダ設定が存在するかを確認する
	 * 
	 * @param calendarId
	 * @param nullcheck
	 * @param ownerRoleId
	 * 
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	
	public static void validateCalenderId(String calendarId, boolean nullcheck, String ownerRoleId) throws InvalidSetting, InvalidRole {	
		if("".equals(calendarId)){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_CALENDAR_ID_IS_NULL.getMessage());
			m_log.info("validateCalenderId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		else if(calendarId == null){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_CALENDAR_ID_IS_NULL.getMessage());
				m_log.info("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return;
		}
		else{
			try {
				//対象のカレンダ設定を取得できない場合にExceptionエラーを発生させる。
				com.clustercontrol.calendar.util.QueryUtil.getCalInfoPK_OR(calendarId, ownerRoleId);
			} catch (CalendarNotFound e) {
				String[] args = {calendarId};
				m_log.warn("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw new InvalidSetting(MessageConstant.MESSAGE_CALENDAR_ID_NOT_EXIST.getMessage(args));
			} catch (InvalidRole e) {
				m_log.warn("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		return;
	}

	/**
	 * calendarIdが空文字やnullチェック
	 * calendarIdが権限に関わらず存在するか確認する
	 * 
	 * @param calendarId
	 * @param nullcheck
	 * 
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateCalenderId(String calendarId, boolean nullcheck) throws InvalidSetting, InvalidRole {	
		if("".equals(calendarId)){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_CALENDAR_ID_IS_NULL.getMessage());
			m_log.info("validateCalenderId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		else if(calendarId == null){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_CALENDAR_ID_IS_NULL.getMessage());
				m_log.info("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return;
		}
		else{
			try {
				// 対象のカレンダ設定を取得できない場合CalendarNotFound、
				// 参照権限が無い場合はInvalidRoleが発生
				com.clustercontrol.calendar.util.QueryUtil.getCalInfoPK(calendarId);
			} catch (CalendarNotFound e) {
				String[] args = {calendarId};
				m_log.warn("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw new InvalidSetting(MessageConstant.MESSAGE_CALENDAR_ID_NOT_EXIST.getMessage(args));
			} catch (InvalidRole e) {
				String[] args = {calendarId};
				m_log.warn("validateCalenderId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw new InvalidSetting(MessageConstant.MESSAGE_CALENDAR_NOT_REFERENCE_AUTHORITY_TO_CALENDAR_ID.getMessage(args));
			}
		}
		return;
	}
	
	
	/**
	 * notifyIdがnullまたは、対象の通知設定が存在するかを確認する
	 * 
	 * @param notifyId
	 * @param nullcheck
	 * @param ownerRoleId
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateNotifyId(String notifyId, boolean nullcheck, String ownerRoleId) throws InvalidSetting, InvalidRole {

		if(notifyId == null){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_NOTIFY_ID_IS_NULL.getMessage());
				m_log.info("validateNotifyId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return;
		}
		else{
			try {
				//通知ID存在チェック
				com.clustercontrol.notify.util.QueryUtil.getNotifyInfoPK(notifyId, ObjectPrivilegeMode.NONE);
				//アクセス権限チェック
				com.clustercontrol.notify.util.QueryUtil.getNotifyInfoPK_OR(notifyId, ownerRoleId);
			} catch (NotifyNotFound e) {
				String[] args = {notifyId};
				throw new InvalidSetting(MessageConstant.MESSAGE_NOTIFY_ID_NOT_EXIST.getMessage(args));
			}
		}
		return;
	}

	/**
	 * 指定されたジョブ連携送信設定IDに該当するジョブ連携送信設定が参照可能かを確認する
	 * 
	 * @param joblinkSendSettindId
	 * @param ownerRoleId
	 * 
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	
	public static void validateJoblinkSendSettingId(String joblinkSendSettindId, String ownerRoleId)
			throws InvalidSetting, InvalidRole {	
		if(joblinkSendSettindId == null || joblinkSendSettindId.isEmpty()){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_LINK_SEND_ID_IS_NULL.getMessage());
			m_log.info("validateJoblinkSendSettingId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		try {
			//対象のジョブ連携送信設定を取得できない場合にExceptionエラーを発生させる。
			com.clustercontrol.jobmanagement.util.QueryUtil.getJobLinkSendSettingPK_OR(joblinkSendSettindId, ownerRoleId);
		} catch (JobMasterNotFound e) {
			String[] args = {joblinkSendSettindId};
			m_log.warn("validateJoblinkSendSettingId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidSetting(MessageConstant.MESSAGE_JOB_LINK_SEND_ID_NOT_EXIST.getMessage(args));
		} catch (InvalidRole e) {
			throw e;
		}
		return;
	}

	/**
	 * schedule型のチェック
	 */
	public static void validateScheduleHour(Schedule schedule) throws InvalidSetting {
		validateSchedule(schedule);
		// 分だけでなく、時も必須。
		if (schedule.getHour() == null ||
				schedule.getHour() < 0 || 24 <= schedule.getHour()) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_HOUR.getMessage());
			m_log.info("validateScheduleHour() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * schedule型のチェック
	 */
	public static void validateSchedule(Schedule schedule) throws InvalidSetting {
		boolean emptyFlag = true;
		if (schedule.getType() == ScheduleConstant.TYPE_DAY) {
			if (schedule.getMonth() != null) {
				emptyFlag = false;
				if (schedule.getMonth() < 0 || 12 < schedule.getMonth()) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MONTH.getMessage());
					m_log.info("validateSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if (schedule.getDay() != null) {
				emptyFlag = false;
				if (schedule.getDay() < 0 || 31 < schedule.getDay()) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DAY.getMessage());
					m_log.info("validateSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else if (!emptyFlag){
				// 月を入力した場合は日も必須。
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DAY.getMessage());
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else if (schedule.getType() == ScheduleConstant.TYPE_WEEK) {
			if (schedule.getWeek() == null ||
					schedule.getWeek() < 0 || 7 < schedule.getWeek()) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WEEK.getMessage());
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			InvalidSetting e = new InvalidSetting("unknown schedule type");
			m_log.info("validateSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if (schedule.getHour() != null) {
			emptyFlag = false;
			if (schedule.getHour() < 0 || 24 < schedule.getHour()) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_HOUR.getMessage());
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else if (!emptyFlag){
			// 日を入力した場合は時間も必須。
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_HOUR.getMessage());
			m_log.info("validateSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if (schedule.getMinute() != null) {
			emptyFlag = false;
			if (schedule.getMinute() < 0 || 60 < schedule.getMinute()) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MIN.getMessage());
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			// 分は必須。
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MIN.getMessage());
			m_log.info("validateSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * ownerRoleIdがnullまたは、対象のロールが存在するかを確認する
	 * さらに、変更時には、オーナーロールIDが変更されていないかを確認する
	 * 
	 * @param ownerRoleId
	 * @param nullcheck
	 * @param objectType
	 * @throws InvalidSetting
	 */
	public static void validateOwnerRoleId(String ownerRoleId, boolean nullcheck, Object pk, String objectType) throws InvalidSetting {

		if(ownerRoleId == null){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_OWNERROLEID.getMessage());
				m_log.info("validateOwnerRoleId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return;
		}
		else{
			try {
				// 存在確認
				com.clustercontrol.accesscontrol.util.QueryUtil.getRolePK(ownerRoleId);

				// 変更時にオーナーロールIDが変わっていないか確認する
				RoleValidator.validateModifyOwnerRole(pk, objectType, ownerRoleId);
			} catch (RoleNotFound e) {
				throw new InvalidSetting(MessageConstant.MESSAGE_OWNERROLEID_NOT_EXIST.getMessage(new String[]{ownerRoleId}), e);
			}
		}
		return;
	}
	
	/**
	 * ownerRoleIdに指定されたロールが存在するかを確認する<BR>
	 * REST API向け
	 * 
	 * @param ownerRoleId
	 * @throws InvalidSetting
	 */
	public static void validateOwnerRoleIdExists(String ownerRoleId) throws InvalidSetting {
		try {
			// 存在確認
			com.clustercontrol.accesscontrol.util.QueryUtil.getRolePK(ownerRoleId);

		} catch (RoleNotFound e) {
			throw new InvalidSetting(MessageConstant.MESSAGE_OWNERROLEID_NOT_EXIST.getMessage(ownerRoleId), e);
		}
	}
	
	/**
	 * カレントユーザがオーナーロールに所属しているかチェックする
	 * この関数は、16051で追加され、REST APIの呼び出し直後に実施
	 * ownerRoleIdが指定されていない場合は、処理をスルーする
	 * 既存動作への影響が読めないので、明らかなオーナーロールの違反のみバリデーション
	 *
	 * @param ownerRoleId
	 * @throw InvalidRole
	 */
	public static void validateCurrentUserBelongRole(String ownerRoleId) throws InvalidRole {
		// オーナーロールが指定されていない場合、チェックしない
		if (ownerRoleId != null) {
			// カレントユーザが指定のオーナーロールに所属しているか確認
			String currentUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			
			if(!(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR) && 
					!UserRoleCache.getRoleIdList(currentUser).contains(ownerRoleId)) {
				String args[] = {currentUser, ownerRoleId};
				throw new InvalidRole(MessageConstant.MESSAGE_USER_DOES_NOT_BELONG_TO_ROLE.getMessage(args));
			}
		}
	}
	
	/**
	 * 正規表現がnullまたは、有効かを確認する
	 * 
	 * @param name
	 * @param regex
	 * @param nullcheck
	 * @throws InvalidSetting
	 */
	public static void validateRegex(String name, String regex, boolean nullcheck) throws InvalidSetting {
		if(regex == null){
			if(nullcheck){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
				m_log.info("validateRegex() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return;
		}
		else{
			try{
				Pattern.compile(regex);
			} catch (PatternSyntaxException e){
				String[] args = {name};
				throw new InvalidSetting(MessageConstant.MESSAGE_REGEX_INVALID.getMessage(args));
			}
		}
		return;
	}
	
	/**
	 * 重要度が正しいか判定する
	 * 
	 */
	public static void validatePriority(String name, Integer priority, boolean permitNonePriority) throws InvalidSetting {
		if (priority == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_PRIORITY.getMessage(name));
			m_log.info("validatePriority() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if (priority == PriorityConstant.TYPE_NONE) {
			if (permitNonePriority) {
				return;
			} else {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_PRIORITY.getMessage(name));
				m_log.info("validatePriority() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		if (priority != PriorityConstant.TYPE_CRITICAL &&
				priority != PriorityConstant.TYPE_WARNING &&
				priority != PriorityConstant.TYPE_INFO &&
				priority != PriorityConstant.TYPE_UNKNOWN) {
			if (permitNonePriority) {
				return;
			} else {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_PRIORITY.getMessage(name));
				m_log.info("validatePriority() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		return;
	}
	
	/** 
	 * 指定された数値系監視の収集値に関する値がHinemosの規則にマッチするかを確認する。 
	 * [,、<、>、?、!、|、＠]は許可しない  
	 *  
	 * @param value 
	 * @param isCollectSelected
	 * @throws InvalidSetting 
	 */ 
	public static void validateCollect(String name, String value, boolean isCollectSelected, int maxSize) throws InvalidSetting{

		// null check
		if(value == null || "".equals(value)){
			 if (isCollectSelected) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
				m_log.info("validateCollect() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			 }
		}else{
			// string check
			// 後続の処理でセパレータ等に利用されている文字が含まれていないかチェック
			// ＠ については 全角なので注意（性能グラフ表示でセパレータとして利用されている）
			validateString(name, value, false, 0, maxSize);
			String errorPattern = "!,<>?|＠";
			String errorStr = ".*[" + errorPattern + "].*";
	
			/** メイン処理 */
			if(value.matches(errorStr)){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_ILLEGAL_CHARACTERS.getMessage(name, errorPattern));
				m_log.info("validateCollect() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * nullであるかをバリデーションします。
	 * 
	 * @param value
	 * @param message
	 * @return
	 * @throws InvalidSetting
	 */
	public static void validateNull(String name, Object value) throws InvalidSetting {
		if (value == null) {
			String message = MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name);
			InvalidSetting e = new InvalidSetting(message);
			m_log.info("validateNull() : " + e.getClass().getSimpleName()
					+ ", " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * 確認状態が正しいか判定する
	 * 
	 */
	public static void validateConfirm(String name, Integer confirm) throws InvalidSetting {
		if (confirm == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
			m_log.info("validateConfirm() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		for (int i = 0; i < ConfirmConstant.CONFIRM_LIST.length; i++) {
			if (confirm == ConfirmConstant.CONFIRM_LIST[i]) {
				//有効な値セットされている場合
				return;
			}
		}
		
		InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_ILLEGAL_VALUE.getMessage(name, String.valueOf(confirm)));
		m_log.info("validateConfirm() : "
				+ e.getClass().getSimpleName() + ", " + e.getMessage());
		throw e;
		
	}

	/**
	 * 指定された集合のnotNullと件数が指定の範囲内かを確認する。
	 * 
	 * @param name 項目名
	 * @param target  Object[] List<Object> Map<String,Object> のいずれかのみを想定 
	 * @param minSize -1なら検査しない
	 * @param maxSize -1なら検査しない
	 * @throws InvalidSetting
	 */
	public static void validateCollectionType(String name, Object target, boolean notNull, int minSize, int maxSize) throws InvalidSetting{

		// null check
		if(notNull) {
			if( target == null ){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
				m_log.info("validateId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		
		if(target != null) {
			int size = -1;
			if(target.getClass().isArray() ){
				size = ((Object[])target).length;
			}else if(target instanceof List ){
				size = ((List<?>)target).size();
			}else if(target instanceof Map ){
				size = ((Map<?,?>)target).size();
			}

			//サイズチェック
			if( minSize != -1 && size < minSize){
				if(size == 0){
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
					m_log.info("validateString() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}else{
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_UNDER.getMessage(name, String.valueOf(minSize)));
					m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if( maxSize != -1 && size > maxSize ){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_OVER_LIMIT.getMessage(name, String.valueOf(maxSize)));
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * Longの上限下限チェック（チェックの一部スキップ可能）
	 * @param name 項目名
	 * @param target 対象数値 nullなら検査しない
	 * @param minSize 最低数 
	 * @param maxSize 最大数 
	 * @throws InvalidSetting
	 */
	public static void validateLongSkippable(String name, Long target, long minSize, long maxSize) throws InvalidSetting {
		//対象がNULLなら検査しない
		if (target == null){
			return;
		}
		// 最低数検査
		if( target < minSize ){
			String[] args = {name, Long.toString(minSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_NUM_LIMIT_LESS.getMessage(args));
			m_log.info("validateLongSkippable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// 最大値検査
		if( maxSize < target ){
			String[] args = {name, Long.toString(maxSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_NUM_LIMIT_OVER.getMessage(args));
			m_log.info("validateLongSkippable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Integerの上限下限チェック（チェックの一部スキップ可能）
	 * @param name 項目名
	 * @param target 対象数値 nullなら検査しない
	 * @param minSize 最低数 
	 * @param maxSize 最大数 
	 * @throws InvalidSetting
	 */
	public static void validateIntegerSkippable(String name, Integer target, int minSize, int maxSize) throws InvalidSetting {
		//対象がNULLなら検査しない
		if (target == null){
			return;
		}
		// 最低数検査
		if( target < minSize ){
			String[] args = {name, Integer.toString(minSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_NUM_LIMIT_LESS.getMessage(args));
			m_log.info("validateIntegerSkippable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// 最大値検査
		if( maxSize < target ){
			String[] args = {name, Integer.toString(maxSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_NUM_LIMIT_OVER.getMessage(args));
			m_log.info("validateIntegerSkippable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Doubleの上限下限チェック（チェックの一部スキップ可能）
	 * @param name 項目名
	 * @param target 対象数値 nullなら検査しない
	 * @param minSize 最低数
	 * @param maxSize 最大数
	 * @throws InvalidSetting
	 */
	public static void validateDoubleSkippable(String name, Double target, Double minSize, Double maxSize) throws InvalidSetting {
		//対象がNULLなら検査しない
		if (target == null){
			return;
		}
		// 最低数検査
		if( target < minSize ){
			String[] args = {name, Double.toString(minSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_NUM_LIMIT_LESS.getMessage(args));
			m_log.info("validateDoubleSkippable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// 最大値検査
		if( maxSize < target ){
			String[] args = {name, Double.toString(maxSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_NUM_LIMIT_OVER.getMessage(args));
			m_log.info("validateDoubleSkippable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Stringの文字数上限下限チェック（チェックの一部スキップ可能）
	 * @param name 項目名
	 * @param target 対象数値 nullなら検査しない
	 * @param minSize 最低字数 -1なら検査しない
	 * @param maxSize 最大字数 -1なら検査しない
	 * @throws InvalidSetting
	 */
	public static void validateStringLengthSkippable(String name, String target, int minSize, int maxSize) throws InvalidSetting {
		//対象がNULLなら検査しない
		if (target == null){
			return;
		}
		// 最低数が-1でないなら 最低数検査
		if( minSize != -1 && target.length() < minSize ){
			String[] args = {name, Long.toString(minSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_STR_LIMIT_LESS.getMessage(args));
			m_log.info("validateLongSkippable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// 最大数が-1でないなら 最大値検査
		if( maxSize != -1 && maxSize <  target.length()  ){
			String[] args = {name, Long.toString(maxSize)};
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_STR_LIMIT_OVER.getMessage(args));
			m_log.info("validateLongSkippable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * URLのフォーマットチェック
	 * @param name 項目名
	 * @param target
	 * @throws InvalidSetting
	 */
	public static void validateUrl(String name, String target) throws InvalidSetting {
		if (target == null){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
			m_log.info("validateUrl() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;		}
		
		String url = target;
		// format check
		if (url.length() > 0 && (!url.startsWith("http://") && !url.startsWith("https://"))) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INVALID_URL.getMessage(name));
			m_log.info("validateUrl() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		else if((url.startsWith("http://") && url.length() == 7) || (url.startsWith("https://") && url.length() == 8)){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INVALID_URL.getMessage(name));
			m_log.info("validateUrl() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}	
	}

	/**
	 * ジョブ変数名が[a-z,A-Z,0-9,-,_]にマッチするかを確認する。
	 * [@,.]は許可しない。
	 * 
	 * @param id
	 * @throws InvalidSetting
	 */
	public static void validateJobParamId(String name, String id, int maxSize) throws InvalidSetting {
		// null check
		if (id == null || "".equals(id)) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ID_IS_NULL.getMessage(name));
			m_log.info("validateJobParamId() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// string check
		validateString(name, id, false, 1, maxSize);
		String errorPattern = "＠.";

		/** メイン処理 */
		if (!id.matches("[a-zA-Z0-9_-]+")) {
			InvalidSetting e = new InvalidSetting(
					MessageConstant.MESSAGE_INPUT_ILLEGAL_CHARACTERS.getMessage(name, errorPattern));
			m_log.info("validateJobParamId() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * for debug
	 * @param args
	 */
	public static void main(String[] args) {

		String id;

		// OKのパターン(半角英数字、-(半角ハイフン)、_(半角アンダーバー))
		id = "Linux-_1";
		try{
			validateId("name", id , 64);
			System.out.println("id = " + id + " is OK");
		} catch (Exception e) {
			System.out.println("???");
			e.printStackTrace();
		}

		// NGのパターン
		id = "/?/";
		try{
			validateId("name", id , 64);
		} catch (Exception e) {
			System.out.println("id = " + id + " is NG");
		}

	}

}
