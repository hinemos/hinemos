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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class RestObjectMapperWrapper {

	private static Log m_log = LogFactory.getLog(RestObjectMapperWrapper.class);

	private static ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

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

	// configureでの設定だと実装非推奨の警告が出るため、ファクトリメソッドからのインスタンス生成とした。
	private static ObjectMapper insensitiveMapper = JsonMapper.builder()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES).build(); 

	/**
	 * 大文字小文字を無視してJSONをJavaオブジェクトに変換する。<BR>
	 * 参照系の出力値を更新系の入力値として使いまわした場合に設定値を認識できず<BR>
	 * 入力エラーとなる不具合の対応契機で追加。
	 */
	public static <T> T convertJsonToObjectInsensitive(String json, Class<T> clazz)
			throws InvalidSetting, HinemosUnknown {
		T obj = null;
		try {
			obj = (T) insensitiveMapper.readValue(json, clazz);
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
