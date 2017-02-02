/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.util.QueryUtil;

/**
 * システム通知情報を検索するクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class SelectNotifyRelation {

	/**
	 * システム通知情報を返します。
	 * 
	 * @param notifyId 取得対象の通知ID
	 * @return システム通知情報
	 */
	public ArrayList<NotifyRelationInfo> getNotifyRelation(String notifyGroupId) {
		return new ArrayList<NotifyRelationInfo>(QueryUtil.getNotifyRelationInfoByNotifyGroupId(notifyGroupId));
	}

	/**
	 * 引数で指定した通知IDを利用している通知グループIDを取得する。
	 * 
	 * @param notifyId
	 * @return 通知グループIDのリスト
	 */
	public ArrayList<String> getNotifyGroupIdBaseOnNotifyId(String notifyId) {

		ArrayList<String> ret = new ArrayList<String>();

		List<NotifyRelationInfo> relations = QueryUtil.getNotifyRelationInfoByNotifyId(notifyId);

		Iterator<NotifyRelationInfo> itr = relations.iterator();

		while(itr.hasNext()){

			NotifyRelationInfo relation = itr.next();

			ret.add(relation.getId().getNotifyGroupId());

		}

		return ret;
	}

}
