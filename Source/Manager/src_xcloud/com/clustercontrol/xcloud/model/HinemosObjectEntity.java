/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import com.clustercontrol.xcloud.persistence.ApplyCurrentTime;
import com.clustercontrol.xcloud.persistence.ApplyUserName;

@MappedSuperclass
public abstract class HinemosObjectEntity extends CloudObjectEntity {
	private Long regDate;
	private Long updateDate;
	private java.lang.String regUser;
	private java.lang.String updateUser;

	public HinemosObjectEntity()
	{
	}

	public HinemosObjectEntity( Long regDate,Long updateDate,String regUser,String updateUser )
	{
		setRegDate(regDate);
		setUpdateDate(updateDate);
		setRegUser(regUser);
		setUpdateUser(updateUser);
	}

	public HinemosObjectEntity( HinemosObjectEntity otherData )
	{
		setRegDate(otherData.getRegDate());
		setUpdateDate(otherData.getUpdateDate());
		setRegUser(otherData.getRegUser());
		setUpdateUser(otherData.getUpdateUser());
	}
	
	@Column(name="reg_date")
	@ApplyCurrentTime(onlyPersist=true)
	public Long getRegDate()
	{
		return this.regDate;
	}
	public void setRegDate( Long regDate )
	{
		this.regDate = regDate;
	}

	@Column(name="update_date")
	@ApplyCurrentTime
	public Long getUpdateDate()
	{
		return this.updateDate;
	}
	public void setUpdateDate( Long updateDate )
	{
		this.updateDate = updateDate;
	}

	@Column(name="reg_user")
	@ApplyUserName(onlyPersist=true)
	public java.lang.String getRegUser()
	{
		return this.regUser;
	}
	public void setRegUser( java.lang.String regUser )
	{
		this.regUser = regUser;
	}

	@Column(name="update_user")
	@ApplyUserName
	public java.lang.String getUpdateUser()
	{
		return this.updateUser;
	}
	public void setUpdateUser( java.lang.String updateUser )
	{
		this.updateUser = updateUser;
	}
}
