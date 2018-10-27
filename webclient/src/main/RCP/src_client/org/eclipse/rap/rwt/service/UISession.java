/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.rwt.service;

import javax.servlet.http.HttpSession;

/**
 * UISession for RCP (Dummy class)
 * 
 * @since 5.0.0
 */
public class UISession {

	/* Create an unique HttpSession */
	private final static HttpSession httpSession = new DummyHttpSession();

	public static HttpSession getHttpSession(){
		return httpSession;
	}
	
	public String getId() {
		return "RichClient";
	}
}
