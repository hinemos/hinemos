/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobmapIconImage;
import com.clustercontrol.jobmanagement.model.JobmapIconImageEntity;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.util.QueryUtil;

/**
 * 
 * ジョブマップ用アイコンを検索するクラスです。
 *
 * @version 5.1.0
 */
public class SelectJobmap {
	private static Log m_log = LogFactory.getLog( SelectJobmap.class );

	/**
	 * filenameのジョブマップ用アイコンを検索します。
	 * 
	 * @param iconId アイコンID
	 * @return ジョブマップ用アイコン情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public JobmapIconImage getJobmapIconImage(String iconId) throws IconFileNotFound, ObjectPrivilege_InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		m_log.debug("getJobmapIconImage() iconId = " + iconId);

		JobmapIconImageEntity entity 
			= em.find(JobmapIconImageEntity.class, iconId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			IconFileNotFound e = new IconFileNotFound("JobmapIconImageEntity.findByPrimaryKey"
					+ ", iconId = " + iconId);
			m_log.info("getJobmapIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return createJobmapIconImage(entity);
	}

	/**
	 * ジョブマップ用アイコン一覧情報を取得します。
	 * 
	 * @return ジョブマップ用アイコン一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<JobmapIconImage> getJobmapIconImageList() throws IconFileNotFound {

		m_log.debug("getJobmapIconImageList()");

		ArrayList<JobmapIconImage> list = new ArrayList<JobmapIconImage>();
		// ジョブマップ用アイコン情報を取得する
		Collection<JobmapIconImageEntity> jobmapIconImageEntities = QueryUtil.getJobmapIconImageEntities();
		if (jobmapIconImageEntities == null) {
			IconFileNotFound e = new IconFileNotFound("JobmapIconImageEntity.findAll");
			m_log.info("getJobmapIconImageList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		for(JobmapIconImageEntity entity : jobmapIconImageEntities){
			list.add(createJobmapIconImage(entity));
		}
		return list;
	}

	/**
	 * ジョブマップ用アイコンID一覧情報を取得します。
	 * ただし、デフォルトアイコンIDを除く。
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return ジョブマップ用アイコンID一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<String> getJobmapIconImageIdExceptDefaultList(String ownerRoleId) throws IconFileNotFound, HinemosUnknown {

		m_log.debug("getJobmapIconImageIdList()");
		
		// デフォルトアイコンIDリストを取得する。
		List<String> defaultList =  new JobControllerBean().getJobmapIconIdDefaultList();

		ArrayList<String> list = new ArrayList<String>();
		// ジョブマップ用アイコン情報を取得する
		
		List<String> iconIds 
			= QueryUtil.getJobmapIconImageIdExceptDefaultList_OR(defaultList, ownerRoleId);
		if (iconIds == null) {
			IconFileNotFound e = new IconFileNotFound("JobmapIconImageEntity.findAllIconIdExceptDefault");
			m_log.info("getJobmapIconImageIconIdExceptDefaultList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		list.addAll(iconIds);
		return list;
	}

	/**
	 * Entityからジョブマップ用アイコン情報を作成します
	 * 
	 * @param entity ジョブマップ用アイコンEntity
	 * @return ジョブマップ用アイコン一覧情報
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private JobmapIconImage createJobmapIconImage(JobmapIconImageEntity entity) {
		JobmapIconImage jobmapIconImage = new JobmapIconImage();
		jobmapIconImage.setIconId(entity.getIconId());
		jobmapIconImage.setFiledata(entity.getFiledata());
		jobmapIconImage.setDescription(entity.getDescription());
		jobmapIconImage.setOwnerRoleId(entity.getOwnerRoleId());
		jobmapIconImage.setCreateUser(entity.getRegUser());
		jobmapIconImage.setCreateTime(entity.getRegDate());
		jobmapIconImage.setUpdateUser(entity.getUpdateUser());
		jobmapIconImage.setUpdateTime(entity.getUpdateDate());
		return jobmapIconImage;
	}
	
}
