/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.util;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestObjectMapperWrapper {

	private static Log m_log = LogFactory.getLog(RestObjectMapperWrapper.class);

	private static ObjectMapper mapper = new ObjectMapper();

	public static <T> T convertJsonToObject(String json, Class<T> clazz) throws InvalidSetting, HinemosUnknown {
		T obj = null;
		try {
			obj = (T) mapper.readValue(json, clazz);
		} catch (JsonParseException | JsonMappingException e) {
			m_log.warn(e.getMessage());
			throw new InvalidSetting(e.getMessage(), e);
		} catch (IOException e) {
			m_log.warn(e.getMessage());
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return obj;
	}
}