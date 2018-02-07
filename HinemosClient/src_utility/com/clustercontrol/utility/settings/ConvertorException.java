/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings;

/**
 * 都合良く使える例外。
 * 
 * @version 6.0.0
 * @since 2.0.0
 * 
 * 
 */
public class ConvertorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4120076515538017951L;

	private int errorCode = -1;

	/**
	 * HinemosConveterException コンストラクタ
	 * @param
	 * @param
	 */
	ConvertorException(Exception exception, int errorCode) {
		super(exception);
		this.errorCode = errorCode;
	}
	/*
	public ConvertorException(Exception exception) {
		super(exception);
	}
	*/
	ConvertorException(int errorCode) {
		super("Error code is " + errorCode);
		this.errorCode = errorCode;
	}
	
	public ConvertorException(String message) {
		super(message);
	}
	
	public ConvertorException() {
		super();
	}

	public int getErrorCode() {
		return errorCode;
	}
}