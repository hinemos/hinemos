/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer;

import java.io.IOException;
import java.util.Date;

import com.clustercontrol.calendar.util.TimeStringConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * エポックミリ秒を 時刻（HH:mm:ss 24時以降対応）に変換するシリアライザ<BR>
 * 本クラスをusingに指定したJsonSerializeアノテーションをLong型フィールドに付与することで、JSONに文字列として書き出される。
 */
public class TimeLongToStringSerializer extends StdSerializer<Long> {

	private static final long serialVersionUID = 1L;

	protected TimeLongToStringSerializer() {
		super(Long.class);
	}

	@Override
	public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		// クライアントとマネージャのタイムゾーンは一致している前提の処理である。
		String strTime;
		if (value == null) {
			strTime = null;
		} else {
			strTime = TimeStringConverter.formatTime(new Date(value));
		}
		gen.writeString(strTime);
	}

}
