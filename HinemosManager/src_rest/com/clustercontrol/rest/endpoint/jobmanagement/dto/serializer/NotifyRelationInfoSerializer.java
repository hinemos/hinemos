/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer;

import java.io.IOException;

import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * NotifyRelationInfoの変換用シリアライザ<BR>
 * 本クラスをusingに指定したJsonSerializeアノテーションをNotifyRelationInfoのフィールドに付与することで、NotifyRelationInfoResponseのオブジェクトとしてJSONに出力する。
 * @see com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse
 */
public class NotifyRelationInfoSerializer extends StdSerializer<NotifyRelationInfo> {
	private static final long serialVersionUID = 1L;

	public NotifyRelationInfoSerializer() {
		super(NotifyRelationInfo.class);
	}

	@Override
	public void serialize(NotifyRelationInfo value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("notifyId", value.getNotifyId());
		gen.writeStringField("notifyType", getNotifyType(value.getNotifyType()));
		gen.writeEndObject();
		
	}
	
	private String getNotifyType(int code) {
		for (NotifyTypeEnum type : NotifyTypeEnum.values()) {
			if (type.getCode() == code) {
				return type.name();
			}
		}
		return null;
	}
}
