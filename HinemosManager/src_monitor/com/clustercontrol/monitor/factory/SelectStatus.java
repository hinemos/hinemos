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

package com.clustercontrol.monitor.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.bean.StatusDataInfo;
import com.clustercontrol.monitor.bean.StatusFilterInfo;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ステータス情報を検索するクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class SelectStatus {

	/**
	 * 引数で指定された条件に一致するステータス一覧情報を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたプロパティに格納された検索条件を、プロパティユーティリティ（{@link com.clustercontrol.util.PropertyUtil}）を使用して取得します。</li>
	 * <li>引数で指定されたファシリティ配下のファシリティと検索条件を基に、ステータス情報を取得します。</li>
	 * <li>１ステータス情報をテーブルのカラム順（{@link com.clustercontrol.monitor.bean.StatusTabelDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>１ステータス情報をStatusInfoDataとして、ステータス情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 * </li>
	 * </ol>
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param property 検索条件
	 * @return ステータス情報一覧（StatusInfoDataが格納されたList）
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.monitor.bean.StatusInfoData
	 * @see com.clustercontrol.util.PropertyUtil#getPropertyValue(com.clustercontrol.bean.Property, java.lang.String)
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.StatusInfoBean#ejbFindStatus(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String)
	 * @see com.clustercontrol.monitor.bean.StatusTabelDefine
	 */
	public ArrayList<StatusDataInfo> getStatusList(String facilityId, StatusFilterInfo filter)
			throws HinemosUnknown {
		ArrayList<StatusDataInfo> list = null;

		Integer[] priorityList = null;
		Long outputFromDate = null;
		Long outputToDate = null;
		Long generationFromDate = null;
		Long generationToDate = null;
		String monitorId = null;
		String monitorDetailId = null;
		int facilityType = 0;
		String application = null;
		String message = null;
		String ownerRoleId = null;

		String[] facilityIds = null;

		Collection<StatusInfoEntity> ct = null;

		if(filter != null){
			//重要度取得
			if (filter.getPriorityList() != null && filter.getPriorityList().length>0) {
				priorityList = filter.getPriorityList();
			}

			//更新日時（自）取得
			if(filter.getOutputDateFrom() != null){
				outputFromDate = filter.getOutputDateFrom();
				outputFromDate -= (outputFromDate % 1000);	//ミリ秒の桁を0にする
			}

			//更新日時（至）取得
			if(filter.getOutputDateTo() != null){
				outputToDate = filter.getOutputDateTo();
				outputToDate += (999 - (outputToDate % 1000));	//ミリ秒の桁を999にする
			}

			//出力日時（自）取得
			if(filter.getGenerationDateFrom() != null){
				generationFromDate = filter.getGenerationDateFrom();
				generationFromDate -= (generationFromDate % 1000);	//ミリ秒の桁を0にする
			}

			//出力日時（至）取得
			if(filter.getGenerationDateTo() != null){
				generationToDate = filter.getGenerationDateTo();
				generationToDate += (999 - (generationToDate % 1000));	//ミリ秒の桁を999にする
			}

			//監視項目ID取得
			if (!"".equals(filter.getMonitorId())) {
				monitorId = filter.getMonitorId();
			}

			//監視詳細取得
			if (!"".equals(filter.getMonitorDetailId())) {
				monitorDetailId = filter.getMonitorDetailId();
			}

			//対象ファシリティ種別取得
			if(filter.getFacilityType() != null){
				facilityType = filter.getFacilityType();
			}

			//アプリケーション取得
			if(!"".equals(filter.getApplication())){
				application = filter.getApplication();
			}

			//メッセージ取得
			if(!"".equals(filter.getMessage())){
				message = filter.getMessage();
			}

			//オーナーロールID取得
			if(!"".equals(filter.getOwnerRoleId())){
				ownerRoleId = filter.getOwnerRoleId();
			}
		}

		// 対象ファシリティのファシリティIDを取得
		int level = RepositoryControllerBean.ALL;
		if(FacilityTargetConstant.TYPE_BENEATH == facilityType){
			level = RepositoryControllerBean.ONE_LEVEL;
		}
		ArrayList<String> facilityIdList = new RepositoryControllerBean().getFacilityIdList(facilityId, level);

		if(facilityIdList != null && facilityIdList.size() > 0){
			// スコープの場合
			if (facilityId.equals(RoleSettingTreeConstant.ROOT_ID)) {
				facilityIdList.add("");
			}
			facilityIds = new String[facilityIdList.size()];
			facilityIdList.toArray(facilityIds);
		}
		else{
			// ノードの場合
			facilityIds = new String[1];
			facilityIds[0] = facilityId;
		}

		// ステータス情報一覧を、検索条件を指定して取得
		ct = QueryUtil.getStatusInfoByFilter(
				facilityIds,
				priorityList,
				outputFromDate,
				outputToDate,
				generationFromDate,
				generationToDate,
				monitorId,
				monitorDetailId,
				application,
				message,
				ownerRoleId);

		// 2次元配列に変換
		list = this.collectionToArray(facilityId, ct);

		return list;
	}


	/**
	 * 重要度が最高で受信日時が最新のステータス情報を返します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたファシリティ配下のファシリティを、指定されたファシリティのターゲットで取得します。</li>
	 * <li>取得したファシリティに属する重要度が最高 かつ 受信日時が最新のステータス情報を取得し返します。</li>
	 * </ol>
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param level 取得対象のファシリティのターゲット（配下全て／直下のみ）
	 * @param orderFlg 日付ソート
	 * @return ステータス情報のローカルコンポーネントインターフェース
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.StatusInfoBean#ejbFindHighPriorityStatus(String[], Timestamp, Timestamp, Timestamp, Timestamp, String, String)
	 */
	protected StatusInfoEntity getHighPriorityStatus(String facilityId, int level, boolean orderFlg)
			throws HinemosUnknown {

		StatusInfoEntity status = null;

		String[] facilityIds = null;
		if(level == MonitorControllerBean.ONLY){
			if(facilityId != null && !"".equals(facilityId)){
				facilityIds = new String[1];
				facilityIds[0] = facilityId;
			}
			else{
				return null;
			}
		}
		else{
			// 直下 または 配下すべてのファシリティIDを取得
			ArrayList<String> facilityIdList = new RepositoryControllerBean().getFacilityIdList(facilityId, level);

			if(facilityIdList != null && facilityIdList.size() > 0){
				// スコープの場合
				if(facilityId != null){
					facilityIdList.add(facilityId);
				}
				facilityIds = new String[facilityIdList.size()];
				facilityIdList.toArray(facilityIds);
			}
			else{
				if(facilityId != null){
					// ノードの場合
					facilityIds = new String[1];
					facilityIds[0] = facilityId;
				}
				else{
					// リポジトリが1件も登録されていない場合
					return null;
				}
			}
		}

		// ステータス情報一覧を取得
		List<StatusInfoEntity> ct = QueryUtil.getStatusInfoByHighPriorityFilter(
				facilityIds,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				orderFlg);

		Iterator<StatusInfoEntity> itr = ct.iterator();
		if(itr.hasNext()){
			status = itr.next();
		}

		return status;
	}

	/**
	 * DBより取得したステータス情報のリストを返します。
	 * １ステータス情報をStatusInfoDataインスタンスとし、ステータス情報一覧を保持するリスト（{@link ArrayList}）に格納します。<BR>
	 * 
	 * 
	 * @param parentFacilityId ルートファシリティID
	 * @param ct ステータス情報取得結果
	 * @return ステータス情報一覧（StatusDataInfoが格納されたList）
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.monitor.bean.StatusInfoData
	 * @see com.clustercontrol.monitor.bean.StatusTabelDefine
	 */
	private ArrayList<StatusDataInfo> collectionToArray(String parentFacilityId, Collection<StatusInfoEntity> ct) throws HinemosUnknown {

		ArrayList<StatusDataInfo> list = new ArrayList<StatusDataInfo>();
		for(StatusInfoEntity status : ct){

			// スコープの取得
			String facilityPath = new RepositoryControllerBean().getFacilityPath(status.getId().getFacilityId(), parentFacilityId);

			StatusDataInfo info = new StatusDataInfo();
			info.setPriority(status.getPriority());
			info.setPluginId(status.getId().getPluginId());
			info.setMonitorId(status.getId().getMonitorId());
			info.setMonitorDetailId(status.getId().getMonitorDetailId());
			info.setFacilityId(status.getId().getFacilityId());
			info.setFacilityPath(facilityPath);
			info.setApplication(status.getApplication());
			if (status.getOutputDate() != null) {
				info.setOutputDate(status.getOutputDate());
			}
			if (status.getGenerationDate() != null) {
				info.setGenerationDate(status.getGenerationDate());
			}
			info.setMessage(status.getMessage());
			info.setOwnerRoleId(status.getOwnerRoleId());

			list.add(info);

		}
		return list;
	}
}
