/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.bean.JobObjectGroupInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * JobObjectGroupInfoの変換用デシリアライザ<BR>
 * 本クラスをusingに指定したJsonDeserializeアノテーションをJobObjectGroupInfo(List)のフィールドに付与することで、通常のJSON変換に加え以下の変換を行う。
 * <ol>
 * <li> orderNoを付与する。
 * <li> isGroupを判定しsetする。
 * <ol>
 */
public class JobObjectGroupInfoDeserializer extends StdDeserializer<List<JobObjectGroupInfo>> {
	private static Log m_log = LogFactory.getLog( JobObjectGroupInfoDeserializer.class );
	
	protected JobObjectGroupInfoDeserializer(Class<?> vc) {
		super(vc);
	}
	
	public JobObjectGroupInfoDeserializer() {
		this(List.class);
	}


	private static final long serialVersionUID = 1L;


	@Override
	public List<JobObjectGroupInfo> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		List<JobObjectGroupInfo> retList = p.readValueAs(new TypeReference<List<JobObjectGroupInfo>>() {});
		for (int i = 0; i < retList.size(); i++) {
			JobObjectGroupInfo jobObjectGroupInfo = retList.get(i);
			jobObjectGroupInfo.setOrderNo(i);
			jobObjectGroupInfo.setIsGroup(jobObjectGroupInfo.getJobObjectList().size() > 1);
			m_log.trace("deserialized JobObjectGroupInfo=" + jobObjectGroupInfo);
		}
		
		return retList;
	}

}
