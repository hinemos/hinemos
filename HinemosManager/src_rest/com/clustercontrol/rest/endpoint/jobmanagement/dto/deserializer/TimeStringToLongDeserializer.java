/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer;

import java.io.IOException;
import java.util.Date;

import com.clustercontrol.calendar.util.TimeStringConverter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * 時刻（HH:mm:ss 24時以降対応）をエポックミリ秒に変換するデシリアライザ<BR>
 * 本クラスをusingに指定したJsonDeserializeアノテーションをLong型フィールドに付与することで、JSONの時刻文字列フィールドから変換できるようにする。
 */
public class TimeStringToLongDeserializer extends StdDeserializer<Long> {
	private static final long serialVersionUID = 1L;

	protected TimeStringToLongDeserializer() {
		super(Long.class);
	}

	@Override
	public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String time = p.getText();
		if (time == null) {
			return null;
		}

		//クライアントとマネージャのタイムゾーンは一致している前提の処理である。
		Long longTime;
		Date dateTime = null;
		try {
			dateTime = TimeStringConverter.parseTime(time);
		} catch (Exception e) {
			throw new IOException(e.getMessage(),e);
		}
		longTime = dateTime.getTime();
		return longTime;
	}
}
