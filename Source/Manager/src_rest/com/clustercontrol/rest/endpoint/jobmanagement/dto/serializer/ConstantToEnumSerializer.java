/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.rest.dto.EnumDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateConstant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * 内部コード(Integer) <-> 列挙型の変換用デシリアライザ<BR>
 * 以下のアノテーションをInteger型フィールドに付与することで、JSONに列挙値(EnumDto)で出力することができる。
 * <ol>
 * <li> 本クラスをusingに指定したJsonerializeアノテーション
 * <li> 列挙値の定義(EnumDtoを実装したクラス)を指定したEnumerateConstantアノテーション
 * <ol>
 */
public class ConstantToEnumSerializer extends StdSerializer<Integer> implements ContextualSerializer {
	private static Log m_log = LogFactory.getLog(ConstantToEnumSerializer.class);

	private static final long serialVersionUID = 1L;
	/**
	 * 列挙値に対応する内部コードの定義クラス
	 */
	private Class<? extends EnumDto<Integer>> enumDtoClass;

	public ConstantToEnumSerializer() {
		super(Integer.class);
	}

	protected ConstantToEnumSerializer(Class<? extends EnumDto<Integer>> enumDtoClass) {
		super(Integer.class);
		this.enumDtoClass = enumDtoClass;
	}

	/**
	 * 内部コードから列挙値を取得
	 */
	@SuppressWarnings("unchecked")
	private EnumDto<Integer> getNameFromCode(Integer code) throws Exception {
		try {
			Method valueOf = enumDtoClass.getMethod("values");
			EnumDto<Integer>[] values = (EnumDto<Integer>[])valueOf.invoke(null, new Object[]{});
			for (EnumDto<Integer> value : values) {
				if (value.getCode().equals(code)) {
					return value;
				}
			}
			return null;
		} catch (NoSuchMethodException | SecurityException  | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// 想定外エラー
			m_log.warn(e.getMessage(),e);
			throw e;
		}
	}

	@Override
	public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		try {
			EnumDto<Integer> enumDto = getNameFromCode(value);
			if (enumDto != null) {
				gen.writeString(enumDto.toString());
			} else {
				gen.writeNull();
			}
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
			throw new IOException(e);
		}
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		EnumerateConstant annotation = property.getAnnotation(EnumerateConstant.class);
		if (annotation != null) {
			Class<? extends EnumDto<Integer>> enumDtoClass = annotation.enumDto();
			return new ConstantToEnumSerializer(enumDtoClass);
		}
		return this;
	}
}
