/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.rest.dto.EnumDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateString;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * 列挙型 <-> 内部コード(String)の変換用デシリアライザ<BR>
 * 以下のアノテーションをString型フィールドに付与することで、JSONに列挙値(EnumDto)で指定されたフィールドから内部コードに変換することができる。
 * <ol>
 * <li> 本クラスをusingに指定したJsonDeserializeアノテーション
 * <li> 列挙値の定義(EnumDtoを実装したクラス)を指定したEnumerateStringアノテーション
 * <ol>
 */
public class EnumToStringDeserializer extends StdDeserializer<String> implements ContextualDeserializer {

	private static Log m_log = LogFactory.getLog(EnumToStringDeserializer.class);

	private static final long serialVersionUID = 1L;
	/**
	 * 列挙値に対応する内部コードの定義クラス
	 */
	private Class<? extends EnumDto<String>> enumDtoClass;
	
	/**
	 * 列挙値から内部コードを取得
	 */
	@SuppressWarnings("unchecked")
	private String getCodeFromName(String name) throws Exception {
		try {
			Method valueOf = enumDtoClass.getMethod("valueOf", String.class);
			return ((EnumDto<String>) valueOf.invoke(null, name)).getCode();
		} catch (NoSuchMethodException | SecurityException  | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// 想定外エラー
			m_log.warn(e.getMessage(),e);
			throw e;
		}
	}

	public EnumToStringDeserializer() {
		super(String.class);
	}
	
	public EnumToStringDeserializer(Class<? extends EnumDto<String>> enumDtoClass) {
		super(String.class);
		this.enumDtoClass = enumDtoClass;
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		EnumerateString annotation = property.getAnnotation(EnumerateString.class);
		if (annotation != null) {
			Class<? extends EnumDto<String>> enumDtoClass = annotation.enumDto();
			return new EnumToStringDeserializer(enumDtoClass);
		}
		return this;
	}

	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String enumName = p.getText();
		if (enumName == null) {
			return null;
		}
		try {
			return getCodeFromName(enumName);
		} catch (Exception e) {
			// Enumへのパース失敗
			m_log.warn(e.getMessage(), e);
			throw new IOException(e);
		}
	}
}
