/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.bean.ScopeDataInfo;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * スコープ情報を検索するクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class SelectScope {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectScope.class );

	/**
	 * スコープ情報一覧を返します。
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param statusFlag
	 * @param eventFlag
	 * @param orderFlg
	 * @return スコープ情報一覧（ScopeInfoDataが格納されたList）
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<ScopeDataInfo> getScopeList(String facilityId,
			boolean statusFlag, boolean eventFlag, boolean orderFlg) throws MonitorNotFound, InvalidRole, HinemosUnknown {

		ArrayList<ScopeDataInfo> list = new ArrayList<ScopeDataInfo>();

		SelectStatus selectStatus = new SelectStatus();
		SelectEvent selectEvent = new SelectEvent();

		int parentPriority = PriorityConstant.TYPE_NONE;
		String parentFacilityId = null;
		String parentFacilityPath = null;
		Date parentOutputDate = null;

		try
		{
			// 指定したファシリティの重要度が一番高い情報を取得
			if(facilityId != null && !"".equals(facilityId)){
				// 重要度が一番高いステータス情報を取得
				StatusInfoEntity status = null;
				m_log.debug("flag " + statusFlag + "," + eventFlag);
				if (statusFlag) {
					status = selectStatus.getHighPriorityStatus(facilityId, MonitorControllerBean.ONLY, orderFlg);
				}

				// 重要度が一番高いイベントログを取得
				EventLogEntity event = null;
				if (eventFlag) {
					event = selectEvent.getHighPriorityEvent(facilityId, MonitorControllerBean.ONLY, orderFlg);
				}

				// 重要度が一番高い情報を設定する
				ScopeDataInfo scopeInfo = this.getHighPriorityScope(status, event, facilityId, facilityId);
				if (scopeInfo != null) {
					parentPriority = scopeInfo.getPriority().intValue();
					parentFacilityId = scopeInfo.getFacilityId();
					parentFacilityPath = scopeInfo.getFacilityPath();
					if (scopeInfo.getOutputDate() != null) {
						parentOutputDate = new Date(scopeInfo.getOutputDate());
					}
				}
			}

			// 直下のファシリティIDを取得
			ArrayList<String> facilityIdList
			= new RepositoryControllerBean().getFacilityIdList(facilityId, MonitorControllerBean.ONE_LEVEL);

			String[] facilityIds = null;
			if(facilityIdList != null && facilityIdList.size() > 0){
				// スコープの場合
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


			boolean highPriorityFlg = false;
			for(int index=0; ((facilityIds!=null) && (index<facilityIds.length)); index++){

				// 直下のファシリティの場合
				if(!facilityIds[index].equals(facilityId)){

					// 重要度が一番高いステータス情報を取得
					StatusInfoEntity status = null;
					if (statusFlag) {
						status = selectStatus.getHighPriorityStatus(facilityIds[index], MonitorControllerBean.ALL, orderFlg);
					}
					// 重要度が一番高いイベントログを取得
					EventLogEntity event = null;
					if (eventFlag) {
						event = selectEvent.getHighPriorityEvent(facilityIds[index], MonitorControllerBean.ALL, orderFlg);
					}

					// 重要度が一番高い情報を設定する
					ScopeDataInfo scopeInfo = this.getHighPriorityScope(status, event, facilityIds[index], facilityId);

					if(scopeInfo != null){

						//そのfacilityTreeのソート順を読み出す。ArrayListに追加する。
						FacilityInfo facility = QueryUtil.getFacilityPK_NONE(facilityIds[index]);
						Integer sortValue = facility.getDisplaySortOrder();

						scopeInfo.setSortValue(sortValue);

						list.add(scopeInfo);

						// 最上位のファシリティの情報を設定する
						if(parentPriority > scopeInfo.getPriority().intValue()){
							highPriorityFlg = true;
						}
						else if(parentPriority == scopeInfo.getPriority().intValue()){
							if(parentOutputDate != null
									&& scopeInfo.getOutputDate() != null){
								if(parentOutputDate.before(new Date(scopeInfo.getOutputDate()))){
									highPriorityFlg = true;
								}
							}
							else{
								highPriorityFlg = true;
							}
						}

						if(highPriorityFlg){
							parentPriority = scopeInfo.getPriority().intValue();
							if (scopeInfo.getOutputDate() != null) {
								parentOutputDate = new Date(scopeInfo.getOutputDate());
							}

							if(parentFacilityId == null){
								if(facilityId != null && !"".equals(facilityId)){
									parentFacilityId = facilityId;
									parentFacilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, facilityId);
								}
							}
						}
					}
				}
				highPriorityFlg = false;
			}

			if(parentFacilityId != null){
				// 最上位のファシリティの情報を設定する
				// 最上位スコープの表示位置は一番上にするため、Sort_ORDERの最小値をである0をセット
				ScopeDataInfo scopeInfo = new ScopeDataInfo(
						parentFacilityId,
						parentFacilityPath,
						parentPriority,
						parentOutputDate==null?null:parentOutputDate.getTime(),
								0);
				list.add(scopeInfo);

			}
		} catch (FacilityNotFound e) {
			String[] args = {facilityId};
			AplLogger.put(InternalIdCommon.MON_SYS_001, args);
			throw new MonitorNotFound(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			String[] args = {facilityId};
			AplLogger.put(InternalIdCommon.MON_SYS_001, args);
			throw e;
		}
		return list;
	}

	/**
	 * 重要度が最高で受信日時が最新のスコープ情報を返します。<BR>
	 * 引数で指定されたステータス情報／イベント情報を比較し、
	 * 重要度が最高で受信日時が最新のログの情報を、テーブルのカラム順にリストにセットし返します。
	 * 
	 * @param status ステータス情報
	 * @param event イベント情報
	 * @param repository リポジトリ
	 * @param facilityId 親ファシリティID
	 * @return スコープ情報
	 * @throws HinemosUnknown
	 */
	private ScopeDataInfo getHighPriorityScope(StatusInfoEntity status,
			EventLogEntity event,
			String facilityId,
			String parentFacilityId) throws HinemosUnknown {

		ScopeDataInfo scopeInfo = new ScopeDataInfo();

		boolean statusInfoFlg = false;
		boolean eventLogFlg = false;

		// 重要度が一番高い情報を設定する
		if(status == null && event == null){
			return null;
		}
		else if(status != null && event == null){
			statusInfoFlg = true;
		}
		else if(status == null && event != null){
			eventLogFlg = true;
		}
		else if(status != null && event != null){
			if((status.getPriority()).intValue() < (event.getPriority()).intValue()){
				statusInfoFlg = true;
			}
			else if((status.getPriority()).intValue() > (event.getPriority()).intValue()){
				eventLogFlg = true;
			}
			else if((status.getPriority()).intValue() == (event.getPriority()).intValue()){
				// 重要度が等しい場合、更新日時は最新の情報を設定する
				if((status.getOutputDate()).compareTo(event.getId().getOutputDate()) > 0){
					statusInfoFlg = true;
				}
				else{
					eventLogFlg = true;
				}
			}
		}

		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, parentFacilityId);
		if(statusInfoFlg){
			scopeInfo.setPriority(status.getPriority());
			scopeInfo.setFacilityId(facilityId);
			scopeInfo.setFacilityPath(facilityPath);
			if (status.getOutputDate() != null) {
				scopeInfo.setOutputDate(status.getOutputDate());
			}
		}
		else if(eventLogFlg){
			scopeInfo.setPriority(event.getPriority());
			scopeInfo.setFacilityId(facilityId);
			scopeInfo.setFacilityPath(facilityPath);
			if (event.getId().getOutputDate() != null) {
				scopeInfo.setOutputDate(event.getId().getOutputDate());
			}
		}
		return scopeInfo;
	}
}
