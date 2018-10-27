/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud;

import javax.xml.ws.WebFault;

import com.clustercontrol.xcloud.common.ErrorCode;

@WebFault
public class PluginException extends Exception {
	public static class PluginExceptionBean {
		private String errorCode = ErrorCode.UNEXPECTED.name();
		public PluginExceptionBean() {
		}
		public String getErrorCode() {
			return errorCode;
		}
		public void setErrorCode(String errorCode) {
			this.errorCode = errorCode;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7755504233825235753L;

	private PluginExceptionBean faultInfo;

	public PluginException() {
		this(ErrorCode.UNEXPECTED.getMessage());
	}

	public PluginException(String message, Throwable cause) {
		super(message, cause);
		this.faultInfo = new PluginExceptionBean();
	}

	public PluginException(String message, String errorCode, Throwable cause) {
		this(message, cause);
		setErrorCode(errorCode);
	}

	public PluginException(String message, PluginExceptionBean faultInfo) {
		super(message);
		this.faultInfo = faultInfo;
	}

	public PluginException(String message) {
		super(message);
		this.faultInfo = new PluginExceptionBean();
	}

	public PluginException(String message, String errorCode) {
		this(message);
		setErrorCode(errorCode);
	}

	public PluginException(Throwable cause) {
		this(getMessage(cause), getCode(cause), cause);
	}

	public void setErrorCode(String errorCode) {
		this.faultInfo.setErrorCode(errorCode);
	}

	public String getErrorCode() {
		return this.faultInfo.getErrorCode();
	}

	@Override
	public String getMessage() {
		return super.getMessage() + " : ErrorCode=" + this.faultInfo.getErrorCode();
	}

	public String getUndecoratedMessage() {
		return super.getMessage();
	}

	public PluginExceptionBean getFaultInfo() {
		return faultInfo;
	}

	public static String getMessage(Throwable cause) {
		return cause instanceof PluginException ? ((PluginException)cause).getUndecoratedMessage():(cause.getMessage() == null ? cause.toString(): cause.getMessage());
	}

	public static String getCode(Throwable cause) {
		return cause instanceof PluginException ? ((PluginException)cause).getErrorCode():ErrorCode.UNEXPECTED.name();
	}
}
