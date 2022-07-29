/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clustercontrol.util.InternalIdAbstract;
import com.clustercontrol.xcloud.common.ErrorCode;

public class CloudManagerException extends PluginException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3153406804815913446L;

	private InternalIdAbstract internalId = null;

	public CloudManagerException() {
		super();
	}

	public CloudManagerException(String message, String errorCode, Throwable cause) {
		super(message, errorCode, cause);
	}

	public CloudManagerException(String message, InternalIdAbstract internalId, Throwable cause) {
		super(message, internalId.getInternalId(), cause);
		this.internalId = internalId;
	}

	public CloudManagerException(String message, String errorCode) {
		super(message, errorCode);
	}

	public CloudManagerException(String message, InternalIdAbstract internalId) {
		super(message, internalId.getInternalId());
		this.internalId = internalId;
	}

	public CloudManagerException(Throwable cause) {
		super(cause);
	}
	
	public CloudManagerException(String message, PluginExceptionBean faultInfo) {
		super(message, faultInfo);
	}
	
	public CloudManagerException(String message) {
		super(excludeErrorCode(message), extractErrorCode(message));
	}

	public InternalIdAbstract getInternalId() {
		return internalId;
	}

	public static CloudManagerException cloudManagerFault(String message) {
		return new CloudManagerException(message, ErrorCode.UNEXPECTED.getMessage());
	}
	
	private static String extractErrorCode(String message){
		String errorCode = ErrorCode.UNEXPECTED.name();
		Pattern pat = Pattern.compile(" : ErrorCode=(\\S+)");
		Matcher matcher = pat.matcher(message);
		if( matcher.find() ){
			errorCode = matcher.group(1);
		}		
		return errorCode;
	}
	
	private static String excludeErrorCode(String message){
		return message.replaceAll(" : ErrorCode=\\S+", "");
	}
}
