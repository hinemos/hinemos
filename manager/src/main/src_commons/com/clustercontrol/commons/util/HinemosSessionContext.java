/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class HinemosSessionContext {
	
	private static Log m_log = LogFactory.getLog( HinemosSessionContext.class );

	// ログインユーザID
	public final static String LOGIN_USER_ID = "loginUserId";
	// ADMINISTRATORSロール所属
	public final static String IS_ADMINISTRATOR = "isAdministrator";
	// オブジェクト権限チェック対象（更新対象、削除対象Entity情報）
	public final static String OBJECT_PRIVILEGE_TARGET_LIST = "objectPrivilegeTargetList";
	private static ThreadLocal<HinemosSessionContext> instance  = new ThreadLocal<HinemosSessionContext>() {
		@Override
		protected HinemosSessionContext initialValue()
		{
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
}

