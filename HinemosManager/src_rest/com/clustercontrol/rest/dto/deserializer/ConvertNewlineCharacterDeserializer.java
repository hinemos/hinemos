package com.clustercontrol.rest.dto.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;



/**
 * 改行コードの変換を行うデシリアライザ<BR>
 * 本クラスをusingに指定したJsonDeserializeアノテーションをString型フィールドに付与することで、JSONからオブジェクト変換時に文字列内のCRLFがLFに変換される。
 */
public class ConvertNewlineCharacterDeserializer extends JsonDeserializer<String> {
	// CRLFをLFに変換する。
	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		if(p.getCurrentToken() == JsonToken.VALUE_STRING && p.getText() != null){
			return p.getText().replace("\r\n", "\n");
		}
		return null;
	}
}
