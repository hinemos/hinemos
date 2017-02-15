/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.nodemap.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.BgFileNotFound;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.NodeMapNotFound;
import com.clustercontrol.nodemap.model.MapAssociationEntity;
import com.clustercontrol.nodemap.model.MapAssociationEntityPK;
import com.clustercontrol.nodemap.model.MapBgImageEntity;
import com.clustercontrol.nodemap.model.MapIconImageEntity;
import com.clustercontrol.nodemap.model.MapInfoEntity;
import com.clustercontrol.nodemap.model.MapPositionEntity;
import com.clustercontrol.nodemap.model.MapPositionEntityPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MapInfoEntity getMapInfoPK(String mapId) throws NodeMapNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MapInfoEntity entity = em.find(MapInfoEntity.class, mapId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NodeMapNotFound e = new NodeMapNotFound("MapInfoEntity.findByPrimaryKey"
					+ ", mapId = " + mapId);
			m_log.info("getMapInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MapIconImageEntity getMapIconImagePK(String filename) throws IconFileNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MapIconImageEntity entity = em.find(MapIconImageEntity.class, filename, ObjectPrivilegeMode.READ);
		if (entity == null) {
			IconFileNotFound e = new IconFileNotFound("MapIconImageEntity.findByPrimaryKey"
					+ ", filename = " + filename);
			m_log.info("getMapIconImagePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<MapIconImageEntity> getAllMapIconImage() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MapIconImageEntity> list
		= em.createNamedQuery("MapIconImageEntity.findAll", MapIconImageEntity.class)
		.getResultList();
		return list;
	}

	public static MapBgImageEntity getMapBgImagePK(String filename) throws BgFileNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MapBgImageEntity entity = em.find(MapBgImageEntity.class, filename, ObjectPrivilegeMode.READ);
		if (entity == null) {
			BgFileNotFound e = new BgFileNotFound("MapBgImageEntity.findByPrimaryKey"
					+ ", filename = " + filename);
			m_log.info("getMapBgImagePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<MapBgImageEntity> getAllMapBgImage() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MapBgImageEntity> list
		= em.createNamedQuery("MapBgImageEntity.findAll", MapBgImageEntity.class)
		.getResultList();
		return list;
	}

	public static MapPositionEntity getMapPositionPK(MapPositionEntityPK pk) throws NodeMapNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MapPositionEntity entity = em.find(MapPositionEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NodeMapNotFound e = new NodeMapNotFound("MapPositionEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMapPositionPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MapPositionEntity getMapPositionPK(String mapId, String elementId) throws NodeMapNotFound {
		return getMapPositionPK(new MapPositionEntityPK(mapId, elementId));
	}

	public static MapAssociationEntity getMapAssociationPK(MapAssociationEntityPK pk) throws NodeMapNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MapAssociationEntity entity = em.find(MapAssociationEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			NodeMapNotFound e = new NodeMapNotFound("MapAssociationEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMapAssociationPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MapAssociationEntity getMapAssociationPK(String mapId, String source, String target) throws NodeMapNotFound {
		return getMapAssociationPK(new MapAssociationEntityPK(mapId, source, target));
	}

	public static List<MapAssociationEntity> getMapAssociationByMapId(String mapId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MapAssociationEntity> list
		= em.createNamedQuery("MapAssociationEntity.findByMapId", MapAssociationEntity.class)
		.setParameter("mapId", mapId)
		.getResultList();
		return list;
	}
}
