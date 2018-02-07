/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud;

import com.clustercontrol.xcloud.common.CloudMessageConstant;

public class InternalManagerError extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8764716113090107366L;

	private String errorCode;

	public InternalManagerError() {
		this(CloudMessageConstant.UNEXPECTED_ERROR.getMessage("UNEXPECTED"));
	}

	public InternalManagerError(String message, Throwable cause) {
		this(message, "UNEXPECTED", cause);
	}

	public InternalManagerError(String message, String errorCode, Throwable cause) {
		super(message, cause);
		setErrorCode("UNEXPECTED");
	}

	public InternalManagerError(String message) {
		this(message, "UNEXPECTED", null);
	}

	public InternalManagerError(String message, String errorCode) {
		this(message, errorCode, null);
	}

	public InternalManagerError(Throwable cause) {
		this(cause instanceof PluginException ? ((PluginException)cause).getUndecoratedMessage():(cause.getMessage() == null ? cause.toString(): cause.getMessage()), cause instanceof PluginException ? ((PluginException)cause).getErrorCode():"UNEXPECTED", cause);
	}

	public InternalManagerError(String message, String errorCode, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		setErrorCode(errorCode);
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + " : ErrorCode=" + errorCode;
	}
}
