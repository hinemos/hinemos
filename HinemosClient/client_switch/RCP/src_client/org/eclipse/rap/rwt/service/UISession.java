/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.rwt.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

/**
 * UISession for RCP (Dummy class)
 * 
 * @since 5.0.0
 */
public class UISession {

	/* Create an unique HttpSession */
	private final static HttpSession httpSession = new DummyHttpSession();
	
	private final Map<String, Object> attributes = new ConcurrentHashMap<>();

	public static HttpSession getHttpSession(){
		return httpSession;
	}
	
	public String getId() {
		return "RichClient";
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public boolean setAttribute(String name, Object value) {
		attributes.put(name, value);
		return true;
	}
}
