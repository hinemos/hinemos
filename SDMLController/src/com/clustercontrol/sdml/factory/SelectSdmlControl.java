/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.sdml.bean.SdmlControlSettingFilterInfo;
import com.clustercontrol.sdml.model.SdmlControlMonitorRelation;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelation;
import com.clustercontrol.sdml.util.QueryUtil;

/**
 * SDMLの自動制御に関する設定を検索するクラス
 *
 */
public class SelectSdmlControl {
	private static Log logger = LogFactory.getLog(SelectSdmlControl.class);

	/**
	 * 指定されたSDML制御設定を返します
	 * 
	 * @param applicationId
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 */
	public SdmlControlSettingInfo getSdmlControlSettingInfo(String applicationId)
			throws SdmlControlSettingNotFound, InvalidRole {
		SdmlControlSettingInfo bean = null;
		bean = QueryUtil.getSdmlControlSettingInfoPK(applicationId);

		// 関連する情報を設定する
		setRelationInfo(bean);
		return bean;
	}

	/**
	 * 関連する情報を設定する
	 * 
	 * @param info
	 */
	private void setRelationInfo(SdmlControlSettingInfo info) {
		// 制御設定の通知情報
		info.setNotifyRelationList(NotifyRelationCache.getNotifyList(info.getNotifyGroupId()));

		// 監視設定共通通知情報
		info.setAutoMonitorCommonNotifyRelationList(
				NotifyRelationCache.getNotifyList(info.getAutoMonitorCommonNotifyGroupId()));

		// 監視設定個別通知情報
		for (SdmlMonitorNotifyRelation relInfo : info.getSdmlMonitorNotifyRelationList()) {
			relInfo.setNotifyRelationList(NotifyRelationCache.getNotifyList(relInfo.getNotifyGroupId()));
		}
	}

	/**
	 * 指定されたSDML制御設定を返します
	 * 
	 * @param applicationId
	 * @param mode
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 */
	public SdmlControlSettingInfo getSdmlControlSettingInfo(String applicationId, ObjectPrivilegeMode mode)
			throws SdmlControlSettingNotFound, InvalidRole {
		SdmlControlSettingInfo bean = null;
		bean = QueryUtil.getSdmlControlSettingInfoPK(applicationId, mode);

		// 関連する情報を設定する
		setRelationInfo(bean);
		return bean;
	}

	/**
	 * SDML制御設定の一覧を返します
	 * 
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> getAllSdmlControlSettingInfoList() throws HinemosUnknown {
		List<SdmlControlSettingInfo> list = QueryUtil.getAllSdmlControlSettingInfo();
		for (SdmlControlSettingInfo info : list) {
			// 関連する情報を設定する
			setRelationInfo(info);
		}
		return list;
	}

	/**
	 * 指定されたオーナーロールIDのSDML制御設定の一覧を返します
	 * 
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> getAllSdmlControlSettingInfoList(String ownerRoleId) throws HinemosUnknown {
		List<SdmlControlSettingInfo> list = QueryUtil.getAllSdmlControlSettingInfo_OR(ownerRoleId);
		for (SdmlControlSettingInfo info : list) {
			// 関連する情報を設定する
			setRelationInfo(info);
		}
		return list;
	}

	/**
	 * 指定されたバージョンのSDML制御設定の一覧を返します
	 * 
	 * @param version
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> getSdmlControlSettingInfoListByVersion(String version) throws HinemosUnknown {
		List<SdmlControlSettingInfo> list = QueryUtil.getSdmlControlSettingInfoByVersion(version);
		for (SdmlControlSettingInfo info : list) {
			// 関連する情報を設定する
			setRelationInfo(info);
		}
		return list;
	}

	/**
	 * 指定されたバージョンとオーナーロールIDのSDML制御設定の一覧を返します
	 * 
	 * @param version
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> getSdmlControlSettingInfoListByVersion(String version, String ownerRoleId)
			throws HinemosUnknown {
		List<SdmlControlSettingInfo> list = QueryUtil.getSdmlControlSettingInfoByVersion_OR(version, ownerRoleId);
		for (SdmlControlSettingInfo info : list) {
			// 関連する情報を設定する
			setRelationInfo(info);
		}
		return list;
	}

	/**
	 * 指定した条件でフィルタされたSDML制御設定の一覧を返します
	 * 
	 * @param condition
	 * @param version
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> getSdmlControlSettingInfoList(SdmlControlSettingFilterInfo condition,
			String version) throws HinemosUnknown {
		List<SdmlControlSettingInfo> filterList = new ArrayList<>();
		// 条件未設定の場合は空のリストを返却する
		if (condition == null) {
			logger.debug("getSdmlControlSettingInfoList() : condition is null");
			return filterList;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("getSdmlControlSettingInfoList() : condition" +
					", applicationId = " + condition.getApplicationId() +
					", description = " + condition.getDescription() +
					", facilityId = " + condition.getFacilityId() +
					", validFlg = " + condition.getValidFlg() +
					", regUser = " + condition.getRegUser() +
					", regFromDate = " + condition.getRegFromDate() +
					", regToDate = " + condition.getRegToDate() +
					", updateUser = " + condition.getUpdateUser() +
					", updateFromDate = " + condition.getUpdateFromDate() +
					", updateToDate = " + condition.getUpdateToDate() +
					", ownerRoleId = " + condition.getOwnerRoleId() +
					", version = " + version);
		}

		// facilityId以外の条件でSDML制御設定情報を取得
		List<SdmlControlSettingInfo> entityList = QueryUtil.getSdmlControlSettingInfoByFilter(
				condition.getApplicationId(),
				condition.getDescription(),
				condition.getValidFlg(),
				condition.getRegUser(),
				condition.getRegFromDate(),
				condition.getRegToDate(),
				condition.getUpdateUser(),
				condition.getUpdateFromDate(),
				condition.getUpdateToDate(),
				condition.getOwnerRoleId(),
				version);

		// facilityIdのみJavaで抽出する。
		for (SdmlControlSettingInfo entity : entityList) {
			// facilityId
			if (condition.getFacilityId() != null && !"".equals(condition.getFacilityId())
					&& entity.getFacilityId() != null) {
				// FacilitySelector.getFacilityIdListの第一引数が登録ノード全ての場合は、空リストを返す。そのため、下記のifを追加。
				if (!ReservedFacilityIdConstant.ROOT_SCOPE.equals(entity.getFacilityId())) {
					ArrayList<String> searchIdList = FacilitySelector.getFacilityIdList(entity.getFacilityId(),
							entity.getOwnerRoleId(), RepositoryControllerBean.ALL, false, true);

					if (!searchIdList.contains(condition.getFacilityId())) {
						logger.debug("getSdmlControlSettingInfoList() continue : target = " + entity.getFacilityId()
								+ ", filter = " + condition.getFacilityId());
						continue;
					}
				}
			}

			logger.debug("getSdmlControlSettingInfoList() add display list : target = " + entity.getApplicationId());
			filterList.add(entity);
		}
		return filterList;
	}

	/**
	 * 指定されたアプリケーションIDとファシリティIDに対して作成された自動作成監視の関連情報を返します
	 * 
	 * @param applicationId
	 * @param facilityId
	 * @return
	 */
	public List<SdmlControlMonitorRelation> getSdmlControlMonitorRelation(String applicationId, String facilityId) {
		if (applicationId == null || applicationId.isEmpty() || facilityId == null || facilityId.isEmpty()) {
			return Collections.emptyList();
		}
		return QueryUtil.getSdmlControlMonitorRelationByApplicationIdAndFacilityId(applicationId, facilityId);
	}
}
