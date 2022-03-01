/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.util.HinemosMessage;

public class RecordRegistrationExceptionResponse {
	private String message;
	private String exception;
	private List<String> stack;

	public RecordRegistrationExceptionResponse() {
	}

	public RecordRegistrationExceptionResponse( Throwable exception) {
		Locale locale = RestLanguageConverter.getPrimaryLocale();
		if( locale == null ){
			// エージェント向けRESTAPI等による呼び出しの場合、PrimaryLocaleは存在しないのでマネージャ側のロケールを用いる
			this.message = HinemosMessage.replace(exception.getMessage());
		}else{
			this.message = HinemosMessage.replace(exception.getMessage(),locale);
		}
		this.exception = exception.getClass().getName();
		this.stack = convertStackTraceToList(exception);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public List<String> getStack() {
		return stack;
	}

	public void setStack(List<String> stack) {
		this.stack = stack;
	}

	private List<String> convertStackTraceToList(Throwable e) {
		List<String> ret = new ArrayList<>();
		for (StackTraceElement s : e.getStackTrace()) {
			ret.add(s.toString());
		}
		return ret;
	}

}
