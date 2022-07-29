package com.clustercontrol.rest.dto.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 改行コードの変換を行うシリアライザ<BR>
 * 本クラスをusingに指定したJsonSerializeアノテーションをString型フィールドに付与することで、JSONへの出力時に文字列内のCRLFがLFに変換される。
 */
public class ConvertNewlineCharacterSerializer extends JsonSerializer<String> {
	// CRLFをLFに変換してJSONに出力する。
	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value != null) {
			gen.writeString(value.replace("\r\n", "\n"));
		} else {
			gen.writeNull();
		}
	}
}
