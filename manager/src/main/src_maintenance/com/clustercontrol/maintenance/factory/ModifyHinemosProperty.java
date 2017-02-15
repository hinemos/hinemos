/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.factory;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;


/**
 * 共通設定情報を更新するためのクラスです。
 *
 * @version 4.0.0
 * @since 2.2.0
 *
 */
public class ModifyHinemosProperty {

	private static Log m_log = LogFactory.getLog(ModifyHinemosProperty.class);

	/**
	 * 共通設定情報を追加します。
	 *
	 * @param info 共通設定情報
	 * @param loginUser ログインユーザー名
	 * @return
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public boolean addHinemosProperty(HinemosPropertyInfo info, String loginUser)
			throws EntityExistsException, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = new JpaTransactionManager();

		// Entityクラスのインスタンス生成
		try {
			// 重複チェック
			jtm.checkEntityExists(HinemosPropertyInfo.class, info.getKey());
			jtm.getEntityManager().persist(info);

			
			long now = HinemosTime.currentTimeMillis();
			
			info.setCreateUserId(loginUser);
			info.setCreateDatetime(now);
			info.setModifyUserId(loginUser);
			info.setModifyDatetime(now);
		} catch (EntityExistsException e){
			m_log.info("addHinemosProperty() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		return true;
	}
	
	/**
	 * 共通設定情報を変更します。
	 *
	 * @param info 共通設定情報
	 * @param loginUser ログインユーザー名
	 * @return
	 * @throws HinemosPropertyNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean modifyHinemosProperty(HinemosPropertyInfo info, String loginUser)
			throws HinemosPropertyNotFound, NotifyNotFound, InvalidRole, HinemosUnknown {

		//共通設定情報を取得
		HinemosPropertyInfo entity = QueryUtil.getHinemosPropertyInfoPK(info.getKey());

		//共通設定情報を更新
		entity.setDescription(info.getDescription());
		entity.setKey(info.getKey());
		entity.setValueString(info.getValueString());
		entity.setValueNumeric(info.getValueNumeric());
		entity.setValueBoolean(info.getValueBoolean());
		entity.setDescription(info.getDescription());
		entity.setModifyUserId(loginUser);
		entity.setModifyDatetime(HinemosTime.currentTimeMillis());

		return true;
	}
	
	/**
	 * 共通設定情報を削除します。
	 * 
	 * @param key
	 * @return
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteHinemosProperty(String key)
			throws HinemosPropertyNotFound, InvalidRole, HinemosUnknown {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 削除対象を検索
		HinemosPropertyInfo entity = QueryUtil.getHinemosPropertyInfoPK(key);

		//共通設定情報の削除
		em.remove(entity);

		return true;
	}
}
