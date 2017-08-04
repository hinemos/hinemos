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

package com.clustercontrol.plugin.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.DbmsSchedulerNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.plugin.model.DbmsSchedulerEntity;
import com.clustercontrol.plugin.model.DbmsSchedulerEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static DbmsSchedulerEntity getDbmsSchedulerPK(DbmsSchedulerEntityPK pk, ObjectPrivilegeMode mode) throws DbmsSchedulerNotFound, InvalidRole {
		m_log.debug("getDbmsSchedulerPK() call.");
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		DbmsSchedulerEntity entity = null;
		try {
			entity = em.find(DbmsSchedulerEntity.class, pk, mode);
			if (entity == null) {
				DbmsSchedulerNotFound je = new DbmsSchedulerNotFound("DbmsSchedulerEntity.getDbmsSchedulerPK");
				m_log.debug("getDbmsSchedulerPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setJobId(pk.getJobId());
				je.setJobGroup(pk.getJobGroup());
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.debug("getDbmsSchedulerPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static DbmsSchedulerEntity getDbmsSchedulerPK(String jobId, String jobGroup, ObjectPrivilegeMode mode) throws DbmsSchedulerNotFound, InvalidRole {
		m_log.debug("getDbmsSchedulerPK() call:" + jobId);
		return getDbmsSchedulerPK(new DbmsSchedulerEntityPK(jobId, jobGroup), mode);
	}
	
	public static DbmsSchedulerEntity getDbmsSchedulerPK_NONE(String jobId, String jobGroup) throws DbmsSchedulerNotFound {
		m_log.debug("getDbmsSchedulerPK_NONE() call.");
		DbmsSchedulerEntity entity = null;
		try {
			entity = getDbmsSchedulerPK(new DbmsSchedulerEntityPK(jobId, jobGroup), ObjectPrivilegeMode.NONE);
		} catch (InvalidRole e) {
			// NONE（オブジェクト権限チェックなし）のため、ここは通らない。
		}
		return entity;
	}

	public static List<DbmsSchedulerEntity> getAllDbmsScheduler() {
		m_log.debug("getAllDbmsScheduler() call.");
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.createNamedQuery("DbmsSchedulerEntity.findAll", DbmsSchedulerEntity.class).getResultList();
	}
}
