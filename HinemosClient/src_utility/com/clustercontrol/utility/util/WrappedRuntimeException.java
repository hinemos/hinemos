/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

/**
 * 処理の継続が難しい例外が発生した場合、この例外にラップしてスローする。<br>
 * 
 * @version 6.1.0
 * @since 2.0.0
 * 
 * 
 */
public final class WrappedRuntimeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 263126017413001522L;

	/**
	 * Construct a WrappedRuntimeException from a checked exception.
	 * 
	 * @param e
	 *            Primary checked exception
	 */
	public WrappedRuntimeException(Exception e) {
		super(e);
	}

	/**
	 * Constructor WrappedRuntimeException
	 * 
	 * 
	 * @param msg
	 *            Exception information.
	 * @param e
	 *            Primary checked exception
	 */
	public WrappedRuntimeException(String msg, Exception e) {
		super(msg, e);
	}
}
