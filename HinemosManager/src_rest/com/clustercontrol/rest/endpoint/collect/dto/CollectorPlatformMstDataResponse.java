/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

public class CollectorPlatformMstDataResponse {
	private String platformId;
	private String platformName;
	private Short orderNo;

	public CollectorPlatformMstDataResponse()
	{
	}

	public String getPlatformId()
	{
		return this.platformId;
	}
	public void setPlatformId( String platformId )
	{
		this.platformId = platformId;
	}

	public String getPlatformName()
	{
		return this.platformName;
	}
	public void setPlatformName( String platformName )
	{
		this.platformName = platformName;
	}

	public Short getOrderNo()
	{
		return this.orderNo;
	}
	public void setOrderNo( Short orderNo )
	{
		this.orderNo = orderNo;
	}

}
