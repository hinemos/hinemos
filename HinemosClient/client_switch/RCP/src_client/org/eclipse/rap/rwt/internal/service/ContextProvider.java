/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.rwt.internal.service;

/**
 * ContextProvider for RCP (Dummy class)
 * 
 * @since 5.0.0
 */
public class ContextProvider{
	private static ServiceContext context = new ServiceContext();

	public static ServiceContext getContext(){
		return context;
	}

	public static void setContext( ServiceContext context ){
		if( context == null ){
			throw new IllegalStateException("context is null.");
		}
	}

	public static void releaseContextHolder(){
		
	}
}
