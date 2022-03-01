/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.filtersetting.bean.StatusFilterBaseInfo;
import com.clustercontrol.monitor.bean.StatusDataInfo;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
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
	 * 
	 * @param filter 検索条件
	 * @return ステータス情報一覧（StatusInfoDataが格納されたList）
	 */
	public ArrayList<StatusDataInfo> getStatusList(StatusFilterBaseInfo filter) throws HinemosUnknown {

		// ステータス情報一覧を、検索条件を指定して取得
		Collection<StatusInfoEntity> ct = QueryUtil.getStatusInfoByFilter(filter);

		// 2次元配列に変換
		ArrayList<StatusDataInfo> list = collectionToArray(filter.getFacilityId(), ct);

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
