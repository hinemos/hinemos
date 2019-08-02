/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
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
				throw new InvalidSetting(e.getMessage(), e);
			}
		}
		return;
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
	 * @throws InvalidSetting 
	 */ 
	public static void validateCollect(String name, String value, int maxSize) throws InvalidSetting{

		// null check
		if(value == null || "".equals(value)){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(name));
			m_log.info("validateCollect() : "
				+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// string check
		validateString(name, value, false, 1, maxSize);

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
