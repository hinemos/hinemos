package com.clustercontrol.repository.entity;

import java.io.Serializable;

public class CollectorSubPlatformMstData implements Serializable {

	private static final long serialVersionUID = 6098201993403001779L;

	private String subPlatformId;
	private String subPlatformName;
	private String type;
	private Integer orderNo;

	public CollectorSubPlatformMstData() {
	}

	public CollectorSubPlatformMstData(String subPlatformId,
			String subPlatformName, String type, Integer orderNo) {
		setSubPlatformId(subPlatformId);
		setSubPlatformName(subPlatformName);
		setType(type);
		setOrderNo(orderNo);
	}

	public CollectorSubPlatformMstData(CollectorSubPlatformMstData otherData) {
		setSubPlatformId(otherData.getSubPlatformId());
		setSubPlatformName(otherData.getSubPlatformName());
		setType(otherData.getType());
		setOrderNo(otherData.getOrderNo());
	}

	public String getSubPlatformId() {
		return subPlatformId;
	}
	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}
	public String getSubPlatformName() {
		return subPlatformName;
	}
	public void setSubPlatformName(String subPlatformName) {
		this.subPlatformName = subPlatformName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append("subPlatformId=" + getSubPlatformId() + " " + "subPlatformName=" + getSubPlatformName() + " " + "type=" + getType() + " " + "orderNo" + getOrderNo());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof CollectorSubPlatformMstData )
		{
			CollectorSubPlatformMstData lTest = (CollectorSubPlatformMstData) pOther;
			boolean lEquals = true;

			if( this.subPlatformId == null )
			{
				lEquals = lEquals && ( lTest.subPlatformId == null );
			}
			else
			{
				lEquals = lEquals && this.subPlatformId.equals( lTest.subPlatformId );
			}
			if( this.subPlatformName == null )
			{
				lEquals = lEquals && ( lTest.subPlatformName == null );
			}
			else
			{
				lEquals = lEquals && this.subPlatformName.equals( lTest.subPlatformName );
			}
			if( this.type == null )
			{
				lEquals = lEquals && ( lTest.type == null );
			}
			else
			{
				lEquals = lEquals && this.type.equals( lTest.type );
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

		result = 37*result + ((this.subPlatformId != null) ? this.subPlatformId.hashCode() : 0);

		result = 37*result + ((this.subPlatformName != null) ? this.subPlatformName.hashCode() : 0);

		result = 37*result + ((this.type != null) ? this.type.hashCode() : 0);

		result = 37*result + ((this.orderNo != null) ? this.orderNo.hashCode() : 0);

		return result;
	}
}
