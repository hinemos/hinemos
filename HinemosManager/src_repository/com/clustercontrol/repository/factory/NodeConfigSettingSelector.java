/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfoPK;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.repository.util.QueryUtil;

/**
 * 対象構成情報の取得処理を実装したクラス<BR>
 */
public class NodeConfigSettingSelector {

	private static Log m_log = LogFactory.getLog(NodeConfigSettingSelector.class);

	/**
	 * 引数で指定された対象構成情報を返します。
	 * 
	 * @param settingId settingID
	 * @return 対象構成情報
	 * @throws NodeConfigSettingNotFound
	 */
	public NodeConfigSettingInfo getNodeConfigSettingInfo(String settingId)
			throws NodeConfigSettingNotFound {

		NodeConfigSettingInfo bean = null;
		try
		{
			// 対象構成情報を取得
			bean = QueryUtil.getNodeConfigSettingInfoPK(settingId, ObjectPrivilegeMode.READ);

			// 通知情報を設定する
			bean.setNotifyRelationList(
					NotifyRelationCache.getNotifyList(bean.getNotifyGroupId()));

			for (NodeConfigSettingItemInfo itemInfo : bean.getNodeConfigSettingItemList()) {
				itemInfo = QueryUtil.getNodeConfigSettingItemInfoPK(new NodeConfigSettingItemInfoPK(settingId, itemInfo.getSettingItemId()));
			}

		} catch (NodeConfigSettingNotFound e) {
			m_log.info("getNodeConfigSettingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return bean;
	}

	/**
	 * 引数で指定されたファシリティIDの対象構成情報を返します。
	 * 対象ファシリティの管理対象フラグが無効の場合はnullを返します。
	 * 
	 * @param facilityId ファシリティID
	 * @return 対象構成情報リスト
	 * @throws NodeConfigSettingNotFound
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 */
	public List<NodeConfigSettingInfo> getNodeConfigSettingListByFacilityId(String facilityId)
			throws NodeConfigSettingNotFound, InvalidRole, FacilityNotFound {

		List<NodeConfigSettingInfo> list = null;
		List<String> scopeList = null;
		try
		{
			// 対象ファシリティの管理対象フラグが有効な場合のみスコープを取得する。
			if (FacilityTreeCache.getFacilityInfo(facilityId).getValid()){
				// ファシリティIDが所属するスコープを取得する
				scopeList = FacilitySelector.getNodeScopeIdList(facilityId);
			}
			// スコープに該当する対象構成情報を取得する
			list = QueryUtil.getNodeConfigSettingListByFacilityIdsAndValid(scopeList, ObjectPrivilegeMode.READ);

			if (list != null && list.size() > 0) {
				for (NodeConfigSettingInfo info : list) {
					// 通知情報を設定する
					info.setNotifyRelationList(
							NotifyRelationCache.getNotifyList(info.getNotifyGroupId()));

					// 対象項目を設定する
					for (NodeConfigSettingItemInfo itemInfo : info.getNodeConfigSettingItemList()) {
						itemInfo = QueryUtil.getNodeConfigSettingItemInfoPK(new NodeConfigSettingItemInfoPK(
								info.getSettingId(), itemInfo.getSettingItemId()));
					}
				}
			}
		} catch (NodeConfigSettingNotFound e) {
			m_log.info("getNodeConfigSettingListByFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return list;
	}

	/**
	 * 対象構成情報設定を返す
	 * 対象構成情報設定に設定されたスコープにファシリティIDが含まれない場合はnull
	 * 対象構成情報設定が無効の場合はnull(パラメータ管理)
	 * 
	 * @param settingId 対象構成情報設定ID
	 * @param facilityId ファシリティID
	 * @param ignoreValid 有効フラグ無視(即時実行の場合など)
	 * @return 対象構成情報設定
	 * @throws NodeConfigSettingNotFound
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 */
	public NodeConfigSettingInfo getNodeConfigSettingInfoByFacilityId(String settingId, String facilityId, boolean ignoreValid)
			throws NodeConfigSettingNotFound, InvalidRole, FacilityNotFound {

		// ファシリティIDが所属するスコープを取得する
		List<String> scopeList = FacilitySelector.getNodeScopeIdList(facilityId);

		// 対象構成情報を取得する
		NodeConfigSettingInfo nodeConfigSettingInfo 
			= QueryUtil.getNodeConfigSettingInfoPK(settingId, ObjectPrivilegeMode.READ);
		if (scopeList != null 
				&& scopeList.contains(nodeConfigSettingInfo.getFacilityId())
				&& (ignoreValid || (!ignoreValid && nodeConfigSettingInfo.getValidFlg()))) {
			// 通知情報を設定する
			nodeConfigSettingInfo.setNotifyRelationList(
					NotifyRelationCache.getNotifyList(nodeConfigSettingInfo.getNotifyGroupId()));
			return nodeConfigSettingInfo;
		}
		return null;
	}

	/**
	 * 対象構成情報一覧を取得する
	 * @return 対象構成情報一覧
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<NodeConfigSettingInfo> getAllNodeConfigSettingList() throws InvalidRole, HinemosUnknown {
		List<NodeConfigSettingInfo> list = QueryUtil.getAllNodeConfigSettingList();
		for (NodeConfigSettingInfo info: list) {
			// 通知情報を設定する
			info.setNotifyRelationList(
				new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));
		}
		return list;
	}
}
