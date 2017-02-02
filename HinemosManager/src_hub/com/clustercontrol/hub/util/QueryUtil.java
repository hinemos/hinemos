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
package com.clustercontrol.hub.util;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.LogFormatNotFound;
import com.clustercontrol.fault.LogTransferNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.hub.model.CollectStringKeyInfoPK;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

public class QueryUtil {

	/** ログ出力のインスタンス。 */
	private static Logger m_log = Logger.getLogger( QueryUtil.class );


	/**
	 * ログフォーマット
	 * @param formatId
	 * @return
	 * @throws Exception
	 */
	public static LogFormat getLogFormatPK(String formatId) throws Exception {
		return getLogFormatPK(formatId, ObjectPrivilegeMode.READ);
	}

	/**
	 * ログフォーマット
	 * @param formatId
	 * @param mode
	 * @return
	 * @throws LogNotFound 
	 * @throws InvalidRole 
	 * @throws Exception
	 */
	public static LogFormat getLogFormatPK(String formatId, ObjectPrivilegeMode mode) throws LogFormatNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		LogFormat entity = null;

		try {
			entity = em.find(LogFormat.class, formatId, mode);
			if (entity == null) {
				LogFormatNotFound e = new LogFormatNotFound("LogFormat.findByPrimaryKey, formatId = " + formatId);
				m_log.info("getLogFormatPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getLogFormatPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}
	/**
	 * @return
	 */
	public static List<LogFormat> getLogFormatList() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<LogFormat> list = em.createNamedQuery("LogFormat.findAll", LogFormat.class).getResultList();
		return list;
	}

	/**
	 * @return
	 */
	public static List<LogFormat> getLogFormatList_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<LogFormat> list = em.createNamedQuery_OR("LogFormat.findAll", LogFormat.class, ownerRoleId).getResultList();
		return list;
	}

	public static boolean isLogFormatUsed(String logFormatId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		TypedQuery<MonitorInfo> query = em.createNamedQuery("MonitorInfo.findBylogFormatId", MonitorInfo.class);
		query.setParameter("logFormatId", logFormatId);
		query.setMaxResults(1);
		return !query.getResultList().isEmpty();
	}
	
	/**
	 * 収集 - 文字列のカウンタの最大値を取得する。
	 * @return
	 */
	public static Long getMaxId(){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Long maxid = null;
		try {
			maxid = em.createNamedQuery("CollectStringKeyInfo.findMaxId",Long.class).getSingleResult();
		} catch (NoResultException e) {
			m_log.debug("getMaxCollectStringKeyId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return maxid;
	}
	
	/**
	 * 収集 - 文字列のカウンタの最大値を取得する。
	 * @return
	 */
	public static Long getMaxCollectStringDataId(){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Long maxid = null;
		try {
			maxid = em.createNamedQuery("CollectStringData.findMaxDataId",Long.class).getSingleResult();
		} catch (NoResultException e) {
			m_log.debug("getMaxCollectStringDataId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return maxid;
	}

	/**
	 * 
	 * @param pk
	 * @return
	 */
	public static CollectStringKeyInfo getCollectStringKeyPK(CollectStringKeyInfoPK pk){
		m_log.debug("getCollectStringKeyPK() : " + pk.toString());
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectStringKeyInfo entity=null;
		entity = em.find(CollectStringKeyInfo.class, pk, ObjectPrivilegeMode.NONE);
		return entity;
	}
	
	/**
	 * 
	 * @param transferId
	 * @return
	 * @throws LogTransferNotFound
	 * @throws InvalidRole
	 */
	public static TransferInfo getTransferInfo(String transferId) throws LogTransferNotFound, InvalidRole {
		return getTransferInfo(transferId, ObjectPrivilegeMode.READ);
	}

	/**
	 * 
	 * @param transferId
	 * @param mode
	 * @return
	 * @throws LogTransferNotFound
	 * @throws InvalidRole
	 */
	public static TransferInfo getTransferInfo(String transferId, ObjectPrivilegeMode mode) throws LogTransferNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		TransferInfo entity = null;

		try {
			entity = em.find(TransferInfo.class, transferId, mode);
			if (entity == null) {
				LogTransferNotFound e = new LogTransferNotFound("TransferInfo.findByPrimaryKey, transferId = " + transferId);
				m_log.info("getTransferInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getTransferInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	/**
	 * 
	 * @return
	 */
	public static List<TransferInfo> getTransferInfoList() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<TransferInfo> list
			= em.createNamedQuery("TransferInfo.findAll", TransferInfo.class).getResultList();
		return list;
	}
	/**
	 * 
	 * @param ownerRoleId
	 * @return
	 */
	public static List<TransferInfo> getTransferInfoList_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<TransferInfo> list
			= em.createNamedQuery_OR("TransferInfo.findAll", TransferInfo.class, ownerRoleId)
			.getResultList();
		return list;
	}
}