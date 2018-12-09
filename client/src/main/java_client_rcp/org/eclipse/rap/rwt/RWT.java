/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.rwt;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.service.DummyHttpServletRequest;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.ui.services.IServiceLocator;

/**
 * RWT for RCP (Dummy class)
 * 
 * @version 5.1.0
 * @since 5.0.0
 */
public class RWT {

	public final static String MNEMONIC_ACTIVATOR = "dummy";

	/* Create an unique UISession */
	private final static UISession uiSession = new UISession();
	private final static HttpServletRequest servletRequest = new DummyHttpServletRequest();

	public static UISession getUISession(){
		return uiSession;
	}

	public static HttpServletRequest getRequest() {
		return servletRequest;
	}

	public static IServiceLocator getClient() {
		// RCPでは使用しない。
		return null;
	}
}
