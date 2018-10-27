/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.util.LinkedHashMap;
import java.util.Set;

import com.clustercontrol.xcloud.InternalManagerError;

public class ParamHolder {
	public static class Builder {
		private LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();

		public void addParam(String paramName, Object param) {
			params.put(paramName, param);
		}
		
		public ParamHolder build() {
			return new ParamHolder(params);
		}
	}
	
	private LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
	
	public ParamHolder(LinkedHashMap<String, Object> params) {
		this.params = params;
	}
	
	public Object getParam(String paramName) {
		Object param =  params.get(paramName);
		if (param == null)
			throw new InternalManagerError("");
		return param;
	}

	@SuppressWarnings("unchecked")
	public <T> T getParam(String paramName, Class<T> clazz) {
		return (T)params.get(paramName);
	}

	public Object[] getParams() {
		return params.values().toArray();
	}

	public int getParamNum() {
		return params.size();
	}

	public String[] getParamNames() {
		Set<String> keys = params.keySet();
		return keys.toArray(new String[keys.size()]);
	}
}
