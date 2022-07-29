/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * システム内時刻（HinemosTime）を表すLongから日時を表す文字列への変換を行うシリアライザ<BR>
 * 本クラスをusingに指定したJsonSerializeアノテーションをLong型フィールドに付与することで、JSONに文字列として書き出される。
 */
public class DateLongToStringSerializer extends StdSerializer<Long> {
	private static Log m_log = LogFactory.getLog(DateLongToStringSerializer.class);

	private static final long serialVersionUID = 1L;

	protected DateLongToStringSerializer() {
		super(Long.class);
	}

	@Override
	public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		try {
			gen.writeString(RestCommonConverter.convertHinemosTimeToDTString(value));
		} catch (InvalidSetting e) {
			m_log.warn(e.getMessage(), e);
			throw new IOException(e);
		}
	}

}
