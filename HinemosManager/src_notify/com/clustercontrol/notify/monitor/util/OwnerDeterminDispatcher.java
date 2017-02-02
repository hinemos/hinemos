/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.monitor.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.notify.monitor.model.EventLogEntityPK;
import com.clustercontrol.notify.monitor.model.StatusInfoEntityPK;
import com.clustercontrol.notify.util.INotifyOwnerDeterminer;
import com.clustercontrol.commons.util.ObjectSharingService;

public class OwnerDeterminDispatcher {

	private static Log m_log = LogFactory.getLog(OwnerDeterminDispatcher.class);

	/**
	 * 設定に紐付かない、オプションの操作ログなどのイベント通知におけるオーナを決定する
	 * @param pk オーナロールを決めたいイベント通知のエンティティPKey
	 * @return 決定されたオーナロールの文字列
	 */
	public static String getOptionalEventOwner(EventLogEntityPK pk) {
		return getOptionalOwner(pk.getMonitorId(), pk.getMonitorDetailId(), pk.getPluginId(), pk.getFacilityId(), true);
	}

	/**
	 * 設定に紐付かない、オプションの操作ログなどのステータス通知におけるオーナを決定する
	 * @param pk オーナロールを決めたいステータス通知のエンティティPKey
	 * @return 決定されたオーナロールの文字列
	 */
	public static String getOptionalStatusOwner(StatusInfoEntityPK pk) {
		return getOptionalOwner(pk.getMonitorId(), pk.getMonitorDetailId(), pk.getPluginId(), pk.getFacilityId(), false);
	}

	/**
	 * 設定に紐付かない、オプションの操作ログなどのイベント・ステータス通知で、イベント・ステータスのオーナを決定するためのロジック<br>
	 * プラグインIDをキーにして、ObjectSharingに登録されたINotifyOwnerDeterminerのサブクラスのインスタンスを取得し、
	 * そのインスタンスにオーナを決定させる。
	 */
	public static String getOptionalOwner(String monitorId, String monitorDetailId, String pluginId, String facilityId, boolean isEvent) {
		INotifyOwnerDeterminer ownerDeterminer = null;
		try {
			ownerDeterminer = ObjectSharingService.objectRegistry().get(INotifyOwnerDeterminer.class, pluginId);
		} catch (InstantiationException e) {
			m_log.error("getOptionalEventOwner() : can't create INotifyOwnerDeteminer instance.", e);
		} catch (IllegalAccessException e) {
			m_log.error("getOptionalEventOwner() : can't create INotifyOwnerDeteminer instance.", e);
		}
		if (ownerDeterminer == null) {
			// ObjectRegistoryに登録していない通常のイベント・ステータスの場合、あるいは
			// 登録されているがObjectRegistoryから正常に取得できない場合、INTERNALとなる
			if (m_log.isDebugEnabled()) {
				m_log.debug("getOptionalEventOwner() : can't get INotifyOwnerDeterminer subclass. Use INTERNAL Role. PluginId = " + pluginId);
			}
			return RoleIdConstant.INTERNAL;
		} else {
			// ObjectRegistoryに登録されている種類のイベント・ステータスの場合、登録されたインスタンスからオーナロールを取得する
			// オーナロールが実在するものか否かのチェックは行わない。但し、取得したオーナロールがnull、もしくは空文字の場合には
			// オーナロールをINTERNALとして登録する
			if (m_log.isDebugEnabled()) {
				m_log.debug("getOptionalEventOwner() : INotifyOwnerDeterminer subClass is registered. PluginId = " + pluginId
						+ ", registered class = " + ownerDeterminer.getClass().getName());
			}
			String ownerRoleId = null;
			if (isEvent == true) {
				ownerRoleId = ownerDeterminer.getEventOwnerRoleId(monitorId, monitorDetailId, pluginId, facilityId);
			} else {
				ownerRoleId = ownerDeterminer.getStatusOwnerRoleId(monitorId, monitorDetailId, pluginId, facilityId);
			}
			if (ownerRoleId == null || ownerRoleId.length() == 0) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("getOptionalEventOwner() : INotifyOwnerDeterminer subClass returned null or empty Role. Use INTERNAL Role.");
				}
				ownerRoleId = RoleIdConstant.INTERNAL;
			}
			return ownerRoleId;
		}
	}
}
