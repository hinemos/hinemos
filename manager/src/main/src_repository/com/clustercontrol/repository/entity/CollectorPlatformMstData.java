/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.entity;

public class CollectorPlatformMstData
extends java.lang.Object
implements java.io.Serializable
{
	private static final long serialVersionUID = -22367165938860497L;

	private java.lang.String platformId;
	private java.lang.String platformName;
	private java.lang.Short orderNo;

	/* begin value object */

	/* end value object */

	public CollectorPlatformMstData()
	{
	}

	public CollectorPlatformMstData( java.lang.String platformId,java.lang.String platformName,java.lang.Short orderNo )
	{
		setPlatformId(platformId);
		setPlatformName(platformName);
		setOrderNo(orderNo);
	}

	public CollectorPlatformMstData( CollectorPlatformMstData otherData )
	{
		setPlatformId(otherData.getPlatformId());
		setPlatformName(otherData.getPlatformName());
		setOrderNo(otherData.getOrderNo());

	}

	public java.lang.String getPrimaryKey() {
		return  getPlatformId();
	}

	public java.lang.String getPlatformId()
	{
		return this.platformId;
	}
	public void setPlatformId( java.lang.String platformId )
	{
		this.platformId = platformId;
	}

	public java.lang.String getPlatformName()
	{
		return this.platformName;
	}
	public void setPlatformName( java.lang.String platformName )
	{
		this.platformName = platformName;
	}

	public java.lang.Short getOrderNo()
	{
		return this.orderNo;
	}
	public void setOrderNo( java.lang.Short orderNo )
	{
		this.orderNo = orderNo;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append("platformId=" + getPlatformId() + " " + "platformName=" + getPlatformName() + " " + "orderNo=" + getOrderNo());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof CollectorPlatformMstData )
		{
			CollectorPlatformMstData lTest = (CollectorPlatformMstData) pOther;
			boolean lEquals = true;

			if( this.platformId == null )
			{
				lEquals = lEquals && ( lTest.platformId == null );
			}
			else
			{
				lEquals = lEquals && this.platformId.equals( lTest.platformId );
			}
			if( this.platformName == null )
			{
				lEquals = lEquals && ( lTest.platformName == null );
			}
			else
			{
				lEquals = lEquals && this.platformName.equals( lTest.platformName );
			}
			if( this.orderNo == null )
			{
				lEquals = lEquals && ( lTest.orderNo == null );
			}
			else
			{
				lEquals = lEquals && this.orderNo.equals( lTest.orderNo );
			}

			return lEquals;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int result = 17;

		result = 37*result + ((this.platformId != null) ? this.platformId.hashCode() : 0);

		result = 37*result + ((this.platformName != null) ? this.platformName.hashCode() : 0);

		result = 37*result + ((this.orderNo != null) ? this.orderNo.hashCode() : 0);

		return result;
	}

}
