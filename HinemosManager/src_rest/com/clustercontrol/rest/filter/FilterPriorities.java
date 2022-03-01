/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.filter;

/**
 * REST向けのjersey filter の優先順設定用
 *  
 */
public class FilterPriorities {
	private FilterPriorities() { }	
	public static final int INITIALIZE = 1000;
	public static final int HEADER_DETECTOR= 1500;
	public static final int AUTHORIZATION = 2000;
	public static final int HEADER_DECORATOR = 4000;
	public static final int INDIVIDUAL = 5000;
	public static final int TERMINATE = 9000;
}
