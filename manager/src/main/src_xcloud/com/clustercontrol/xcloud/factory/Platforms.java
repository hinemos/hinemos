/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.List;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.model.CloudPlatformEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;

public class Platforms implements IPlatforms {
	public Platforms() {
	}

	@Override
	public CloudPlatformEntity getCloudPlatform(String cloudplatformId) throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		return em.find(CloudPlatformEntity.class, cloudplatformId, ObjectPrivilegeMode.READ);
	}

	@Override
	public List<CloudPlatformEntity> getAllCloudPlatforms() throws CloudManagerException {
		HinemosEntityManager em = Session.current().getEntityManager();
		return PersistenceUtil.findAll(em, CloudPlatformEntity.class);
	}
}
