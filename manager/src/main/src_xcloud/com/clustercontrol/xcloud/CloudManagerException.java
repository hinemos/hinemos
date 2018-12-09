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
public class CloudManagerException extends PluginException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3153406804815913446L;
	
	public CloudManagerException() {
		super();
	}

	public CloudManagerException(String message, String errorCode, Throwable cause) {
		super(message, errorCode, cause);
	}

	public CloudManagerException(String message, String errorCode) {
		super(message, errorCode);
	}

	public CloudManagerException(Throwable cause) {
		super(cause);
	}
	
	public CloudManagerException(String message, PluginExceptionBean faultInfo) {
		super(message, faultInfo);
	}
	
	public static CloudManagerException cloudManagerFault(String message) {
		return new CloudManagerException(message, ErrorCode.UNEXPECTED.getMessage());
	}
}
