/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer;

import java.io.IOException;
import java.util.Locale;

import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.util.HinemosMessage;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * メッセージコードをクライアントのロケールに沿って変換するシリアライザ<BR>
 * 本クラスをusingに指定したJsonSerializeアノテーションをString型フィールドに付与することで、文字列内のメッセージコードが変換される。
 */
public class LanguageTranslateSerializer extends StdSerializer<String> {

	private static final long serialVersionUID = 1L;

	protected LanguageTranslateSerializer() {
		super(String.class);
	}

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		Locale primaryLocale = RestLanguageConverter.getPrimaryLocale();
		gen.writeString(HinemosMessage.replace(value, primaryLocale));
	}
}
