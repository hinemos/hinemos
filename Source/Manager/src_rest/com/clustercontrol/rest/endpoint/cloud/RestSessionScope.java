/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;

/**
 * クラウド系APIのセッションを張るクラス
 * SessionScopeをラップしてロギングやログイン情報の設定を実施
 * @see com.clustercontrol.xcloud.Session
 */
public class RestSessionScope extends SessionScope {
	private static Log m_log = LogFactory.getLog(RestSessionScope.class);
	private final long start;

	public RestSessionScope(boolean isolate) {
		super(isolate);
		start = System.currentTimeMillis();
		updateSessionContext();
	}

	public static RestSessionScope open() {
		String resourceMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		m_log.debug("call " + resourceMethodName + "()");

		return new RestSessionScope(true);
	}

	public static RestSessionScope merge() {
		String resourceMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		m_log.info("call " + resourceMethodName + "()");

		return new RestSessionScope(false);
	}

	@Override
	public void close() {
		super.close();
		String resourceMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		long end = System.currentTimeMillis();
		m_log.info(String.format("called %s() : elapsedTime=%dms", resourceMethodName, (end - start)));
	}

	private void updateSessionContext() {
		if(HinemosCredential.ANONYMOUS_USER.equals(Session.current().getHinemosCredential().getUserId())) {
			String accountName = (String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			Session.current().setHinemosCredential(new HinemosCredential(accountName));
		}
	}

}
