/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.msgconverter.RestAllTransrateTarget;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

public class RestLanguageConverter {

	private static Log m_log = LogFactory.getLog(RestLanguageConverter.class);
	private static List<Locale> availableLocaleList = Arrays.asList(NumberFormat.getAvailableLocales());

	/**
	 * RESTのResponse向けDTOに対して、専用アノテーションに基づいてメッセージのコード変換を行う。<BR>
	 * 
	 * @param object
	 *            対象DTO
	 * @throws InvalidSetting
	 */
	public static <T> T convertMessages(T object) {
		if (object == null) {
			return null;
		}
		List<Locale> localeList = HinemosSessionContext.getLocaleList();
		Locale targetLocale = getPrimaryLocale(localeList);

		// 通常のクラスなら自身を、集合系のクラスなら保持レコードを、変換対象として処理
		if (object.getClass().isArray()) {
			for (Object tagAryRec : (Object[]) object) {
				convertMessagesRecursive(tagAryRec, localeList, targetLocale);
			}
		} else if (object instanceof List) {
			for (Object tagListRec : (List<?>) object) {
				convertMessagesRecursive(tagListRec, localeList, targetLocale);
			}
		} else if (object instanceof Map) {
			for (Object tagValueRec : ((Map<?, ?>) object).values()) {
				convertMessagesRecursive(tagValueRec, localeList, targetLocale);
			}
		} else {
			object = convertMessagesRecursive(object, localeList, targetLocale);
		}
		return object;
	}

	// 対象オブジェクト内の各メンバーに対し、再帰的変換処理(メンバ内のメンバも掘り下げて対応)を実施
	private static <T> T convertMessagesRecursive(T target, List<Locale> localeList, Locale targetLocale) {

		if (target == null) {
			return null;
		}

		// 対象オブジェクト内の各メンバーに対し、オブジェクトの型別にて変換処理を実施
		List<Field> memberFields = getClassFields(target.getClass());
		for (Field memberField : memberFields) {
			// private なFieldでもアクセスできるようにする。
			memberField.setAccessible(true);
			Object memberInstance = null;

			// 変換定義取得
			RestAllTransrateTarget allTrans = memberField.getAnnotation(RestAllTransrateTarget.class);
			RestPartiallyTransrateTarget partTrans = memberField.getAnnotation(RestPartiallyTransrateTarget.class);
			boolean setTranAnnotation = false;
			if (allTrans != null || partTrans != null) {
				setTranAnnotation = true;
			}

			// 値取得
			try {
				memberInstance = (Object) memberField.get(target);
			} catch (Exception e) {
				if (m_log.isTraceEnabled()) {
					m_log.trace(
							"convertMessagesRecursive() : memberField access failed .  Exception =" + e.getMessage());
				}
			}
			if (memberInstance == null) {
				continue;
			}

			if (memberField.getType().isArray()) {
				// 配列の場合
				Class<?> type = memberField.getType().getComponentType();
				if (setTranAnnotation && type == String.class) {
					// String配列(変換対象)ならレコードを変換して詰め替えて
					String[] arrayMember = (String[]) memberInstance;
					int arrayNum = arrayMember.length;
					for (int recCount = 0; recCount < arrayNum; recCount++) {
						arrayMember[recCount] = transMessage(arrayMember[recCount], allTrans, partTrans, localeList,
								targetLocale);
					}
				} else if (isRecursiveTarget(type)) {
					// 再帰処理(内部メンバーを処理)対象の配列ならレコード毎に再帰処理
					for (Object tagAryRec : (Object[]) memberInstance) {
						convertMessagesRecursive(tagAryRec, localeList, targetLocale);
					}
				}
			} else if (List.class.isAssignableFrom(memberField.getType())) {
				// Listの場合
				List<?> worklist = (List<?>) memberInstance;
				if (worklist.isEmpty()) {
					continue;
				}
				Class<?> type = worklist.get(0).getClass();
				if (setTranAnnotation && type == String.class) {
					// StringのList(変換対象)ならレコードを変換して詰め替え
					@SuppressWarnings("unchecked")
					List<String> listMember = (List<String>) memberInstance;
					int listNum = listMember.size();
					for (int recCount = 0; recCount < listNum; recCount++) {
						listMember.set(recCount,
								transMessage(listMember.get(recCount), allTrans, partTrans, localeList, targetLocale));
					}
				} else if (isRecursiveTarget(type)) {
					// 再帰処理(内部メンバーを処理)対象のListならレコード毎に再帰処理
					for (Object tagListRec : (List<?>) memberInstance) {
						convertMessagesRecursive(tagListRec, localeList, targetLocale);
					}
				}
			} else if (Map.class.isAssignableFrom(memberField.getType())) {
				// Mapの場合
				// valueが再帰処理(内部メンバーを処理)対象のMapならレコード毎に再帰処理
				// (valueがStringの場合は変換の対象外)
				Map<?, ?> targetMap = (Map<?, ?>) memberInstance;
				for (Entry<?, ?> tagKeyRec : targetMap.entrySet() ) {
					Object mapValue = tagKeyRec.getValue();
					Class<?> type = mapValue.getClass();
					if (isRecursiveTarget(type)) {
						convertMessagesRecursive(mapValue, localeList, targetLocale);
					}
				}
			}else if (setTranAnnotation && memberField.getType() == String.class) {
				// Stringの場合 変換して詰め替え
				try {
					memberField.set(target,
							transMessage((String) memberInstance, allTrans, partTrans, localeList, targetLocale));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					m_log.error(e.getMessage());
				}
			} else if (isRecursiveTarget(memberField.getType())) {
				// 再帰処理(内部メンバーを処理)対象のメンバなら再帰処理
				convertMessagesRecursive(memberInstance, localeList, targetLocale);
			}
		}
		return target;
	}

	/**
	 *  再帰処理対象のクラスかどうか判定<BR>
	 * <BR>
	 */
	private static boolean isRecursiveTarget(Class<?> type) {
		// プリミティブ型とそのラッパー、Enum、集合系クラスは再帰処理 対象外とする
		if (type == String.class || type == Integer.class || type == Double.class || type == Long.class
				|| type == Boolean.class || type == Float.class || type == Short.class || type == Byte.class
				|| type == Character.class || type.isEnum() || type.isPrimitive()
				|| Collection.class.isAssignableFrom(type)) {
			return false;
		}
		return true;
	}

	// 最優先となるLocaleを取得
	private static Locale getPrimaryLocale(List<Locale> localeList) {
		for (Locale locale : localeList) {
			if (availableLocaleList.contains(locale)) {
				m_log.debug(locale.toString() + " is contained in availableLocaleList");
				return locale;
			}
		}
		return null;
	}

	// アノテーション設定に基づいてメッセージ変換を実施
	private static String transMessage(String target, RestAllTransrateTarget allTrans,
			RestPartiallyTransrateTarget partTrans, List<Locale> localeList, Locale primaryLocale) {
		String transWork = target;
		if (allTrans != null) {
			transWork = Messages.getString(transWork, localeList);
		}
		if (partTrans != null) {
			transWork = HinemosMessage.replace(transWork, primaryLocale);
		}
		return transWork;
	}

	/**
	 * クラス内のメンバ一覧を取得（スーパークラスのメンバ含む）<BR>
	 */
	private static List<Field> getClassFields(Class<?> targetClass) {
		Field[] tatgetFields = targetClass.getDeclaredFields();
		Class<?> superClass = targetClass.getSuperclass();
		if (superClass != null) {
			List<Field> superFields = getClassFields(superClass);
			superFields.addAll(Arrays.asList(tatgetFields));
			return superFields;
		} else {
			List<Field> ret = new ArrayList<Field>();
			ret.addAll(Arrays.asList(tatgetFields));
			return ret;
		}
	}
	
	/**
	 * REST応答向けの最優先となるLocaleを取得<BR>
	 * 
	 * エージェントからの呼び出し等の場合は対応可能なLocaleListは存在しないので
	 * nullを返却する。
	 * 
	 */
	public static Locale getPrimaryLocale() {
		List<Locale> localeList = HinemosSessionContext.getLocaleList();
		if(localeList != null ){
			Locale targetLocale = getPrimaryLocale(localeList);
			return targetLocale;
		}
		return null;
	}


}