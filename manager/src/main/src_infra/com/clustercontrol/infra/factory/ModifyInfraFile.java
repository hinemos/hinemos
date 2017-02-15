/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.factory;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.persistence.EntityExistsException;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileBeingUsed;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.util.InfraJdbcExecutor;
import com.clustercontrol.infra.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * 環境構築ファイルを更新
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModifyInfraFile {
	private static Logger m_log = Logger.getLogger( ModifyInfraFile.class );
	
	public void add(InfraFileInfo fileInfo, DataHandler fileContent,
			String userId) throws IOException, InfraFileTooLarge, EntityExistsException, HinemosUnknown {
		JpaTransactionManager jtm = new JpaTransactionManager();
		
		long now = HinemosTime.currentTimeMillis();
		InfraFileInfo entity = new InfraFileInfo(fileInfo.getFileId(), fileInfo.getFileName());
		jtm.checkEntityExists(InfraFileInfo.class, entity.getFileId());
		entity.setCreateDatetime(now);
		entity.setCreateUserId(userId);
		entity.setModifyDatetime(now);
		entity.setModifyUserId(userId);
		entity.setOwnerRoleId(fileInfo.getOwnerRoleId());
		
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(entity);
		jtm.flush();
		InfraJdbcExecutor.insertFileContent(fileInfo.getFileId(), fileContent);
	}
	
	
	/**
	 * ファイルを変更します。
	 * @throws IOException 
	 * @throws InfraFileTooLarge 
	 * @throws InfraFileNotFound 
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public void modify(InfraFileInfo fileInfo, DataHandler fileContent,
			String userId) throws IOException, InfraFileTooLarge, InfraFileNotFound, InvalidRole, HinemosUnknown {
		
		// ファイルを取得
		InfraFileInfo entity = null;
		try {
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			String fileId = fileInfo.getFileId();
			entity = em.find(InfraFileInfo.class, fileId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				InfraFileNotFound e = new InfraFileNotFound("InfraFileEntity.findByPrimaryKey, fileId = " + fileId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			entity.setModifyUserId(userId);
			entity.setModifyDatetime(HinemosTime.currentTimeMillis());
			entity.setFileName(fileInfo.getFileName());
			
			if (fileContent != null) {
				em.remove(entity.getInfraFileContentEntity());
				new JpaTransactionManager().flush();
				InfraJdbcExecutor.insertFileContent(fileInfo.getFileId(), fileContent);
			}

		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
	}
	
	/**
	 * ファイルを削除します。
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws InfraFileNotFound 
	 * @throws InfraFileBeingUsed 
	 */
	public void delete(String fileId) throws InvalidRole, HinemosUnknown, InfraFileNotFound, InfraFileBeingUsed {
		m_log.debug(String.format("delete() : fileId = %s", fileId));

		// ファイルを取得
		InfraFileInfo entity = null;
		try {
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			entity = em.find(InfraFileInfo.class, fileId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				InfraFileNotFound e = new InfraFileNotFound("InfraFileEntity.findByPrimaryKey, fileId = " + fileId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			if (QueryUtil.isInfraFileReferredByFileTransferModuleInfoEntity(fileId)) {
				InfraFileBeingUsed e = new InfraFileBeingUsed("InfraFile is used by FileTransferModuleInfoEntity, fileId = " + fileId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			em.remove(entity);
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
	}
}