/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.util.HinemosMessage;

public class ExceptionBody {
	private int status;
	private String message;
	private String exception;
	private List<String> stack;

	public ExceptionBody() {
	}

	public ExceptionBody(int status, Throwable exception) {
		super();
		this.status = status;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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
