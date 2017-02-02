/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

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
