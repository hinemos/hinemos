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

package com.clustercontrol.hub.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.LogTransferDuplicate;
import com.clustercontrol.fault.LogTransferNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.model.TransferDestProp;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * 収集蓄積[転送]設定を更新
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ModifyTransfer {
	private static Logger m_log = Logger.getLogger( ModifyTransfer.class );
	
	
	/**
	 * 収集蓄積[転送]設定を追加」します。
	 * 
	 * @param entity
	 * @param userId
	 * @throws LogTransferDuplicate
	 * @throws HinemosUnknown
	 */
	public void add(TransferInfo entity, String userId) throws LogTransferDuplicate, HinemosUnknown {
		try {
			JpaTransactionManager jtm = new JpaTransactionManager();
			
			long now = HinemosTime.currentTimeMillis();
			jtm.checkEntityExists(TransferInfo.class, entity.getTransferId());
			
			entity.setRegDate(now);
			entity.setRegUser(userId);
			entity.setUpdateDate(now);
			entity.setUpdateUser(userId);
			
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			em.persist(entity);
			
			jtm.flush();
		} catch (EntityExistsException e) {
			throw new LogTransferDuplicate(MessageConstant.MESSAGE_HUB_TRANSFER_DUPLICATION_ID.getMessage(entity.getTransferId()));
		}
	}
	
	/**
	 * 収集蓄積[転送]設定を変更します。
	 * 
	 * @param logTransfer
	 * @param userId
	 * @throws LogTransferNotFound
	 * @throws InvalidRole
	 */
	public void modify(TransferInfo logTransfer, String userId) throws LogTransferNotFound, InvalidRole {
		try {
			JpaTransactionManager jtm = new JpaTransactionManager();
			
			HinemosEntityManager em = jtm.getEntityManager();
			String transferId = logTransfer.getTransferId();
			TransferInfo entity = em.find(TransferInfo.class, transferId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				LogTransferNotFound e = new LogTransferNotFound("LogTransfer.findByPrimaryKey, transferId = " + transferId);
				m_log.info("modify() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			entity.setDescription(logTransfer.getDescription());
			entity.setDataType(logTransfer.getDataType());
			entity.setDestTypeId(logTransfer.getDestTypeId());
			entity.setTransType(logTransfer.getTransType());
			entity.setInterval(logTransfer.getInterval());
			
			entity.setCalendarId(logTransfer.getCalendarId());
			entity.setValidFlg(logTransfer.getValidFlg());
			
			entity.setUpdateUser(userId);
			entity.setUpdateDate(HinemosTime.currentTimeMillis());
			
			List<TransferDestProp> modifyInfoList = new ArrayList<TransferDestProp>(logTransfer.getDestProps());
			List<TransferDestProp> mstInfoList = new ArrayList<TransferDestProp>(entity.getDestProps());

			Iterator<TransferDestProp> modifyInfoIter = modifyInfoList.iterator();
			while (modifyInfoIter.hasNext()) {
				TransferDestProp modifyInfo = modifyInfoIter.next();
				Iterator<TransferDestProp> mstInfoIter = mstInfoList.iterator();
				while (mstInfoIter.hasNext()) {
					TransferDestProp mstInfo = mstInfoIter.next();
					if (modifyInfo.getName().equals(mstInfo.getName())) {
						mstInfo.setValue(modifyInfo.getValue());
						
						modifyInfoIter.remove();
						mstInfoIter.remove();
						break;
					}
				}
			}
			
			for (TransferDestProp prop: modifyInfoList) {
				TransferDestProp newProp = new TransferDestProp();
				newProp.setName(prop.getName());
				newProp.setValue(prop.getValue());
				entity.getDestProps().add(newProp);
			}
			
			for (TransferDestProp prop: mstInfoList) {
				entity.getDestProps().remove(prop);
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("modify() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
	}
	
	/**
	 * 収集蓄積[転送]設定を削除します。
	 * 
	 * @param logTransferId
	 * @throws LogTransferNotFound
	 * @throws LogTransferUsed
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void delete(String transferId) throws LogTransferNotFound, InvalidRole, HinemosUnknown {
		m_log.debug(String.format("delete() : transferId = %s", transferId));

		// ファイルを取得
		try {
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			TransferInfo entity = em.find(TransferInfo.class, transferId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				LogTransferNotFound e = new LogTransferNotFound("LogTransfer.findByPrimaryKey, transferId = " + transferId);
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