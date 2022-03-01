/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HinemosSessionContext {

	private static Log m_log = LogFactory.getLog(HinemosSessionContext.class);

	// ログインユーザID
	public final static String LOGIN_USER_ID = "loginUserId";
	// ADMINISTRATORSロール所属
	public final static String IS_ADMINISTRATOR = "isAdministrator";
	// オブジェクト権限チェック対象（更新対象、削除対象Entity情報）
	public final static String OBJECT_PRIVILEGE_TARGET_LIST = "objectPrivilegeTargetList";
	// 認証トークン
	public final static String AUTH_TOKEN = "authToken";
	// ロケール情報
	public final static String LOCALE_LIST = "localeList";
	// REST向けのDateFormat
	public final static String REST_DATETIME_FORMAT = "restDatetimeFomat";
	// Hinemosクライアントのバージョン情報
	public final static String REST_HINEMOS_CLIENT_VERSION = "restHinemosClientVersion";

	private static ThreadLocal<HinemosSessionContext> instance = new ThreadLocal<HinemosSessionContext>() {
		@Override
		protected HinemosSessionContext initialValue() {
			m_log.debug("initialValue");
			return null;
		}
	};

	HashMap<String, Object> properties;

	public static HinemosSessionContext instance() {
		HinemosSessionContext ctx = instance.get();
		if (ctx != null) {
			return ctx;
		}

		instance.set(new HinemosSessionContext());
		return instance.get();
	}

	public Object getProperty(String key) {
		Object value = null;
		if (properties != null
				&& key != null
				&& key.length() > 0) {
			value = properties.get(key);
		}
		return value;
	}

	public void setProperty(String key, Object value) {
		if (properties == null) {
			properties = new HashMap<String, Object> ();
		}
		if (key != null && key.length() > 0) {
			properties.put(key, value);
		}
		return;
	}

	public void clearProperties() {
		if (properties != null) {
			properties.clear();
		}
	}

	public static String getLoginUserId() {
		return (String) instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
	}

	public static boolean isAdministrator() {
		Boolean b = (Boolean) instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		return b != null && b.booleanValue();
	}
	
	public static String getAuthToken() {
		return (String) instance().getProperty(HinemosSessionContext.AUTH_TOKEN);
	}

	public static SimpleDateFormat getRestDateFormat() {
		return (SimpleDateFormat) instance().getProperty(HinemosSessionContext.REST_DATETIME_FORMAT);
	}

	@SuppressWarnings("unchecked")
	public static List<Locale> getLocaleList() {
		return (List<Locale>) instance().getProperty(HinemosSessionContext.LOCALE_LIST);
	}
}

