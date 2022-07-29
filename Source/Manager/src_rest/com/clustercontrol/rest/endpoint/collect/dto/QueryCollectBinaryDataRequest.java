/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

public class QueryCollectBinaryDataRequest extends QueryCollectStringDataRequest {

	public QueryCollectBinaryDataRequest(){
	}

	private String textEncoding;

	public String getTextEncoding(){
		return this.textEncoding;
	}

	public void setgetTextEncoding(String textEncoding){
		this.textEncoding = textEncoding;
	}
}
