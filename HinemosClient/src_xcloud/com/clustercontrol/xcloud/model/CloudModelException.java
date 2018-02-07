/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import com.clustercontrol.xcloud.common.CloudConstants;

public class CloudModelException extends RuntimeException {
	private static final long serialVersionUID = -5229536826531257547L;

	private String errorCode = ErrorCodeConstants.UNEXPECTED;
	
	public CloudModelException() {
		super(CloudConstants.bundle_messages.getString("message.unexpected_error"));
	}

	public CloudModelException(String message, Throwable cause) {
		super(message, cause);
	}

	public CloudModelException(String message) {
		super(message);
	}

	public CloudModelException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public CloudModelException(Throwable cause) {
		super(CloudConstants.bundle_messages.getString("message.unexpected_error"), cause);
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public String toString() {
		return super.toString() + " : ErrorCode=" + errorCode;
	}
}
