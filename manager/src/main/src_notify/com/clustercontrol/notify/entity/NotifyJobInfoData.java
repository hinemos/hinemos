/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.entity;

/**
 * Data object for NotifyJobInfo.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class NotifyJobInfoData
extends java.lang.Object
implements java.io.Serializable
{
	private static final long serialVersionUID = 2596975805001362268L;

	private java.lang.String notifyId;
	private java.lang.Integer priority;
	private java.lang.Integer jobFailurePriority;
	private java.lang.String jobunitId;
	private java.lang.String jobId;
	private java.lang.Boolean validFlg;
	private java.lang.Integer jobExecFacilityFlg;
	private java.lang.String jobExecFacility;

	/* begin value object */

	/* end value object */

	public NotifyJobInfoData()
	{
	}

	public NotifyJobInfoData( java.lang.String notifyId,java.lang.Integer priority,java.lang.Integer jobFailurePriority,java.lang.String jobunitId,java.lang.String jobId,java.lang.Boolean validFlg,java.lang.Integer jobExecFacilityFlg,java.lang.String jobExecFacility )
	{
		setNotifyId(notifyId);
		setPriority(priority);
		setJobFailurePriority(jobFailurePriority);
		setJobunitId(jobunitId);
		setJobId(jobId);
		setValidFlg(validFlg);
		setJobExecFacilityFlg(jobExecFacilityFlg);
		setJobExecFacility(jobExecFacility);
	}

	public NotifyJobInfoData( NotifyJobInfoData otherData )
	{
		setNotifyId(otherData.getNotifyId());
		setPriority(otherData.getPriority());
		setJobFailurePriority(otherData.getJobFailurePriority());
		setJobunitId(otherData.getJobunitId());
		setJobId(otherData.getJobId());
		setValidFlg(otherData.getValidFlg());
		setJobExecFacilityFlg(otherData.getJobExecFacilityFlg());
		setJobExecFacility(otherData.getJobExecFacility());

	}

	public com.clustercontrol.notify.entity.NotifyJobInfoPK getPrimaryKey() {
		com.clustercontrol.notify.entity.NotifyJobInfoPK pk = new com.clustercontrol.notify.entity.NotifyJobInfoPK(this.getNotifyId(),this.getPriority());
		return pk;
	}

	public java.lang.String getNotifyId()
	{
		return this.notifyId;
	}
	public void setNotifyId( java.lang.String notifyId )
	{
		this.notifyId = notifyId;
	}

	public java.lang.Integer getPriority()
	{
		return this.priority;
	}
	public void setPriority( java.lang.Integer priority )
	{
		this.priority = priority;
	}

	public java.lang.Integer getJobFailurePriority()
	{
		return this.jobFailurePriority;
	}
	public void setJobFailurePriority( java.lang.Integer jobFailurePriority )
	{
		this.jobFailurePriority = jobFailurePriority;
	}

	public java.lang.String getJobunitId()
	{
		return this.jobunitId;
	}
	public void setJobunitId( java.lang.String jobunitId )
	{
		this.jobunitId = jobunitId;
	}

	public java.lang.String getJobId()
	{
		return this.jobId;
	}
	public void setJobId( java.lang.String jobId )
	{
		this.jobId = jobId;
	}

	public java.lang.Boolean getValidFlg()
	{
		return this.validFlg;
	}
	public void setValidFlg( java.lang.Boolean validFlg )
	{
		this.validFlg = validFlg;
	}

	public java.lang.Integer getJobExecFacilityFlg()
	{
		return this.jobExecFacilityFlg;
	}
	public void setJobExecFacilityFlg( java.lang.Integer jobExecFacilityFlg )
	{
		this.jobExecFacilityFlg = jobExecFacilityFlg;
	}

	public java.lang.String getJobExecFacility()
	{
		return this.jobExecFacility;
	}
	public void setJobExecFacility( java.lang.String jobExecFacility )
	{
		this.jobExecFacility = jobExecFacility;
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer("{");

		str.append("notifyId=" + getNotifyId() + " " + "priority=" + getPriority() + " " + "jobFailurePriority=" + getJobFailurePriority() + " " + "jobunitId=" + getJobunitId() + " " + "jobId=" + getJobId() + " " + "validFlg=" + getValidFlg() + " " + "jobExecFacilityFlg=" + getJobExecFacilityFlg() + " " + "jobExecFacility=" + getJobExecFacility());
		str.append('}');

		return(str.toString());
	}

	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof NotifyJobInfoData )
		{
			NotifyJobInfoData lTest = (NotifyJobInfoData) pOther;
			boolean lEquals = true;

			if( this.notifyId == null )
			{
				lEquals = lEquals && ( lTest.notifyId == null );
			}
			else
			{
				lEquals = lEquals && this.notifyId.equals( lTest.notifyId );
			}
			if( this.priority == null )
			{
				lEquals = lEquals && ( lTest.priority == null );
			}
			else
			{
				lEquals = lEquals && this.priority.equals( lTest.priority );
			}
			if( this.jobFailurePriority == null )
			{
				lEquals = lEquals && ( lTest.jobFailurePriority == null );
			}
			else
			{
				lEquals = lEquals && this.jobFailurePriority.equals( lTest.jobFailurePriority );
			}
			if( this.jobunitId == null )
			{
				lEquals = lEquals && ( lTest.jobunitId == null );
			}
			else
			{
				lEquals = lEquals && this.jobunitId.equals( lTest.jobunitId );
			}
			if( this.jobId == null )
			{
				lEquals = lEquals && ( lTest.jobId == null );
			}
			else
			{
				lEquals = lEquals && this.jobId.equals( lTest.jobId );
			}
			if( this.validFlg == null )
			{
				lEquals = lEquals && ( lTest.validFlg == null );
			}
			else
			{
				lEquals = lEquals && this.validFlg.equals( lTest.validFlg );
			}
			if( this.jobExecFacilityFlg == null )
			{
				lEquals = lEquals && ( lTest.jobExecFacilityFlg == null );
			}
			else
			{
				lEquals = lEquals && this.jobExecFacilityFlg.equals( lTest.jobExecFacilityFlg );
			}
			if( this.jobExecFacility == null )
			{
				lEquals = lEquals && ( lTest.jobExecFacility == null );
			}
			else
			{
				lEquals = lEquals && this.jobExecFacility.equals( lTest.jobExecFacility );
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

		result = 37*result + ((this.notifyId != null) ? this.notifyId.hashCode() : 0);

		result = 37*result + ((this.priority != null) ? this.priority.hashCode() : 0);

		result = 37*result + ((this.jobFailurePriority != null) ? this.jobFailurePriority.hashCode() : 0);

		result = 37*result + ((this.jobunitId != null) ? this.jobunitId.hashCode() : 0);

		result = 37*result + ((this.jobId != null) ? this.jobId.hashCode() : 0);

		result = 37*result + ((this.validFlg != null) ? this.validFlg.hashCode() : 0);

		result = 37*result + ((this.jobExecFacilityFlg != null) ? this.jobExecFacilityFlg.hashCode() : 0);

		result = 37*result + ((this.jobExecFacility != null) ? this.jobExecFacility.hashCode() : 0);

		return result;
	}

}
