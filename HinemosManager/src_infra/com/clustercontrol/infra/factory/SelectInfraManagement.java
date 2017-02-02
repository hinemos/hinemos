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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.infra.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * 環境構築情報を取得する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SelectInfraManagement {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectInfraManagement.class );

	/**
	 * 環境構築情報を取得します。
	 * <p>
	 */
	public InfraManagementInfo get(String infraManagementId, ObjectPrivilegeMode mode) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		InfraManagementInfo info = QueryUtil.getInfraManagementInfoPK(infraManagementId, mode);

		// モジュール情報を取得する
		if (info.getModuleList() != null) {
			for (InfraModuleInfo<?> infraModuleInfo : info.getModuleList()) {
				infraModuleInfo = QueryUtil.getInfraModuleInfoPK(infraModuleInfo.getId());
			}
		}

		// 通知情報を設定する
		info.setNotifyRelationList(
			new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));
		
		// FIXME:
		Collections.sort(info.getModuleList(), new Comparator<InfraModuleInfo<?>>() {
			@Override
			public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
				return o1.getOrderNo().compareTo(o2.getOrderNo());
			}
		});
		
		return info;
	}
	
	public List<InfraManagementInfo> getList() throws InvalidRole, HinemosUnknown {
		List<InfraManagementInfo> list = QueryUtil.getAllInfraManagementInfo();
		for (InfraManagementInfo info: list) {
			// 通知情報を設定する
			info.setNotifyRelationList(
				new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));

			// FIXME:
			Collections.sort(info.getModuleList(), new Comparator<InfraModuleInfo<?>>() {
				@Override
				public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
					return o1.getOrderNo().compareTo(o2.getOrderNo());
				}
			});
		}
		return list;
	}

	public List<InfraManagementInfo> getListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.debug("getList() : start");

		List<InfraManagementInfo> list = QueryUtil.getAllInfraManagementInfoOrderByInfraManagementId_OR(ownerRoleId);
		for (InfraManagementInfo info: list) {
			// 通知情報を設定する
			info.setNotifyRelationList(
				new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));
			// FIXME:
			Collections.sort(info.getModuleList(), new Comparator<InfraModuleInfo<?>>() {
				@Override
				public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
					return o1.getOrderNo().compareTo(o2.getOrderNo());
				}
			});
		}
		return list;
	}
}
