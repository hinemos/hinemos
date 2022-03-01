/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

public class RestCommonConverter {
	private static Log m_log = LogFactory.getLog(RestCommonConverter.class);

	/**
	 * 日時を表す文字列からシステム内時刻（HinemosTime）を表すLongへの変換を行う<BR>
	 * 
	 * フォーマットパターンは REST向けの指定値を使う。 タイムゾーンはHinemosTime向けの指定値を使う。<BR>
	 * 
	 * @param target
	 *            変換対象となる日時文字列
	 * @param itemName
	 *            項目名称（エラー発生時に利用）
	 * @return 変換結果（HinemosTime）
	 * @throws InvalidSetting
	 *             targetが不正なフォーマットの場合に発生
	 */
	public static Long convertDTStringToHinemosTime(String target, String itemName) throws InvalidSetting {
		// com.clustercontrol.util.DateUtil にて タイムゾーン関連のパラメータが考慮された場合は一部処理の統合を検討すること。
		SimpleDateFormat format = getRestDateTimeFormat();
		try {
			format.setTimeZone(HinemosTime.getTimeZone());
			Date parseDate = format.parse(target);
			long convertTime = parseDate.getTime();
			return convertTime;
		} catch (ParseException e) {
			// format.parseで指定されたフォーマットとsrcValの値があっていない場合に発生する。
			String message = MessageConstant.MESSAGE_PLEASE_INPUT_FORMAT.getMessage(itemName,format.toPattern());
			InvalidSetting ex = new InvalidSetting(message);
			m_log.info("convertDTStringToHinemosTime() : " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
			throw ex;
		}
	}

	/**
	 * システム内時刻（HinemosTime）を表すLongから日時を表す文字列への変換を行う<BR>
	 * 
	 * フォーマットパターンは REST向けの指定値を使う。 タイムゾーンはHinemosTime向けの指定値を使う。
	 * 
	 * @param target
	 *            変換対象となるLong
	 * @return 変換結果（文字列）
	 */
	public static String convertHinemosTimeToDTString(Long target) throws InvalidSetting {
		// com.clustercontrol.util.DateUtil にて タイムゾーン関連のパラメータが考慮された場合は一部処理の統合を検討すること。
		SimpleDateFormat format = getRestDateTimeFormat();
		format.setTimeZone(HinemosTime.getTimeZone());
		String destVal = format.format(new Date(target));
		return destVal;
	}
	
	// 日時フォーマットの取得
	// 優先順位は、RESTクライアント指定の書式(スレッドローカルに格納) > Hinemosプロパティ指定の書式
	public static SimpleDateFormat getRestDateTimeFormat() throws InvalidSetting {

		SimpleDateFormat format = HinemosSessionContext.getRestDateFormat();
		if (format != null) {
			return format;
		}

		String restDateFormat = null;
		try {
			restDateFormat = HinemosPropertyCommon.rest_datetime_format.getStringValue();
			format = new SimpleDateFormat(restDateFormat);
			format.setTimeZone(HinemosTime.getTimeZone());
			return format;
		} catch (IllegalArgumentException e) {
			String message = String.format("the argument 'rest.datetime.format' is invalid. format=[%s]",
					restDateFormat);
			m_log.info("getRestDateTimeFormat" + ":" + message);
			throw new InvalidSetting(message);
		}
	}

	
	
	/**
	 * 入力値(文字列)を名称が合致するEnum(EnumDto)へ変換する<BR>
	 * リソースメソッドにおけるQueryStringパラメータのEnum値変換向けメソッド<BR>
	 * 
	 * @param inputName
	 *            入力項目名称（エラー発生時に利用）
	 * @param inputValue
	 *            Enum変換したい入力値(Enumの列挙子と合致する文字列)
	 * @param enumValues
	 *            変換対象となるEnum のvalues()
	 * @return 変換結果
	 * @throws InvalidSetting
	 */
	public static <E extends Enum<E>> E convertEnum(String inputName, String inputValue, E[] enumValues) throws InvalidSetting {
		//inputValueと合致する列挙子を検索して、該当があれば返却
		for (E e : enumValues) {
			if (e.name().equals(inputValue)) {
				return e;
			}
		}
		//該当する列挙子がない場合 InvalidSettingを編集して投げる。
		StringBuilder sb = new StringBuilder();
		int i = 0;
		final int ENUM_IN_MESSAGE_MAX = 10;
		String delim = "";
		for (E e : enumValues) {
			if (++i > ENUM_IN_MESSAGE_MAX) {
				sb.append("...");
				break;
			}
			sb.append(delim).append(e.name());
			delim = ",";
		}

		InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_NON_EXISTENT_MEMBER.getMessage(inputName, sb.toString()));
		m_log.info("convertEnum() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
		throw e;
	}	

	/**
	 * 入力値(文字列)をIntegerへ変換する<BR>
	 * 
	 * @param inputName
	 *            入力項目名称（エラー発生時に利用）
	 * @param inputValue
	 *            変換したい入力値
	 * @param minValue
	 *            最小入力値（特になければnullとすること）
	 * @param maxValue
	 *            最大入力値（特になければnullとすること）
	 * @throws InvalidSetting
	 */
	public static Integer convertInteger(String inputName, String inputValue, boolean isNotNull, Integer minValue, Integer maxValue) throws InvalidSetting {
		//null check
		if (isNotNull) {
			CommonValidator.validateNull(inputName, inputValue);
		}else if (inputValue == null) {
			return null;
		}
		//変換
		if (minValue == null) {
			minValue = Integer.MIN_VALUE;
		}
		if (maxValue == null) {
			maxValue = Integer.MAX_VALUE;
		}
		Integer ret = null;
		try{
			ret = Integer.valueOf( inputValue );
		}catch(NumberFormatException e){
			String[] args ={ inputName, Integer.toString(minValue), Integer.toString(maxValue) };
			InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN_EXCLUDE_MINSIZE.getMessage(args));
			m_log.info("convertInteger() : " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
			throw ex;
		}
		//チェック
		CommonValidator.validateIntegerSkippable(inputName, ret, minValue, maxValue);
		
		return ret;
	}
}
