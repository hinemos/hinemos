/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.util.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.factory.OperateCollectCategoryMaster;
import com.clustercontrol.performance.factory.OperateCollectItemCodeMaster;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryMstData;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.monitor.model.CollectorCategoryCollectMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.performance.util.CollectorMasterCache;
import com.clustercontrol.performance.util.PollingDataManager;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * 収集項目コードの情報を生成するファクトリクラス
 * 
 * @version 4.0
 * @since 1.0
 */
public class CollectorItemCodeTable {
	private static Log m_log = LogFactory.getLog( CollectorItemCodeTable.class );
	private static final Map<String, CollectorItemTreeItem> m_codeTable;

	/**
	 * static field
	 */
	static {
		m_codeTable = new ConcurrentHashMap<String, CollectorItemTreeItem>(); // 収集項目コードがキー

		JpaTransactionManager jtm = new JpaTransactionManager();
		if (!jtm.isNestedEm()) {
			m_log.warn("refresh() : transactioin has not been begined.");
			jtm.close();
		} else {

			// カテゴリコードからカテゴリ情報を参照するためのテーブル
			HashMap<String, CollectorItemTreeItem> categoryTable =
					new HashMap<String, CollectorItemTreeItem>();

			try {
				// カテゴリ情報の読み込み
				Collection<CollectorCategoryMstData> cate = new OperateCollectCategoryMaster().findAll();
				
				for(CollectorCategoryMstData category : cate) {
					CollectorItemTreeItem categoryItem =
							new CollectorItemTreeItem(null, null, category, null, null);  // 親の要素はないためnull

					// カテゴリコードにマッピングするようにカテゴリ情報を登録
					categoryTable.put(category.getCategoryCode(), categoryItem);
				}
			} catch (Exception e) {
				m_log.warn("static() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			try {
				// 収集項目コードの読み込み
				Collection<CollectorItemCodeMstData> ctItemCodeMst
				= new OperateCollectItemCodeMaster().findAll();

				for (CollectorItemCodeMstData codeData : ctItemCodeMst) {
					String itemCode = codeData.getItemCode();

					if(itemCode == null){
						continue;
					}

					if(codeData.getCategoryCode() != null
							&& codeData.isDeviceSupport() != null && codeData.isGraphRange() != null){

						// カテゴリ名を調べます
						CollectorItemTreeItem categoryTreeItem = categoryTable.get(codeData.getCategoryCode());

						// 親のオブジェクトを取得（存在しない場合はnull）
						CollectorItemTreeItem parentItem = null;
						if (codeData.getParentItemCode() != null) {
							parentItem = m_codeTable.get(codeData.getParentItemCode());
						}

						// 親のコードが存在しない場合はカテゴリの直下の要素とする
						if(parentItem == null){
							parentItem = categoryTreeItem;
						}

						CollectorItemTreeItem ctItem =
								new CollectorItemTreeItem(parentItem, codeData, null, null, null);

						// 収集項目コードをキーとして登録
						m_codeTable.put(itemCode, ctItem);
					}
				}

			} catch (Exception e) {
				m_codeTable.clear();
				// エラー処理
				m_log.warn("CollectorItemCodeTable static field error . ", e);
			}
		}
	}

	/**
	 * 収集項目IDを探索するためのテンポラリ用の内部クラス
	 * 
	 */
	private static class PlatformIdAndSubPlatformId {
		private String m_platformId;
		private String m_subPlatformId;

		public PlatformIdAndSubPlatformId(String platformId, String subPlatformId){
			m_platformId = platformId;
			m_subPlatformId = subPlatformId;
		}

		public String getPlatformId(){
			return m_platformId;
		}

		public String getSubPlatformId(){
			return m_subPlatformId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((m_platformId == null) ? 0 : m_platformId.hashCode());
			result = prime
					* result
					+ ((m_subPlatformId == null) ? 0 : m_subPlatformId
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PlatformIdAndSubPlatformId other = (PlatformIdAndSubPlatformId) obj;
			if (m_platformId == null) {
				if (other.m_platformId != null)
					return false;
			} else if (!m_platformId.equals(other.m_platformId))
				return false;
			if (m_subPlatformId == null) {
				if (other.m_subPlatformId != null)
					return false;
			} else if (!m_subPlatformId.equals(other.m_subPlatformId))
				return false;
			return true;
		}
	}


	/**
	 * 指定したfacilityId配下の全てのノードで選択可能な収集項目IDの集合を返却する
	 * 
	 * @param facilityId
	 * @return
	 * @throws HinemosUnknown
	 */
	private static Set<CollectorItemCodeMstData> getEnableCodeSet(String facilityId) throws HinemosUnknown{
		m_log.debug("getEnableCodeSet() facilityId = " + facilityId);

		// 対象ファシリティIDに含まれる全ノードに対して選択可能な収集項目コードのセット（本関数で返すべき結果）
		Set<CollectorItemCodeMstData> enableItemCodeSetInEveryNode = null;

		// 対象ファシリティIDが空またはNULLなら何も選択できるものはない
		if(facilityId == null || "".equals(facilityId)){
			m_log.debug("getEnableCodeSet() codeSet is 0");
			return enableItemCodeSetInEveryNode;
		}

		try {
			// リポジトリ管理機能のSessionBean
			RepositoryControllerBean bean = new RepositoryControllerBean();

			////
			// 探査対象となるノードリストを作成
			////
			List<String> nodeList = null;
			if(bean.isNode(facilityId)){	// 対象ファシリティがノードの場合
				m_log.debug("getEnableCodeSet() facilityId is node");

				// 自分自身のみを登録
				nodeList = new ArrayList<String>();
				nodeList.add(facilityId);
			} else {	// 対象ファシリティがスコープの場合
				m_log.debug("getEnableCodeSet() facilityId is scope");

				// スコープにふくまれるノードのIDのリストを取得
				nodeList = bean.getNodeFacilityIdList(facilityId, null, RepositoryControllerBean.ALL, false, null);
			}

			////
			// 対象ファシリティ配下の全ノードが持つ全てのプラットフォームIDのセットを作る
			////
			Set<PlatformIdAndSubPlatformId> platformSet = new HashSet<PlatformIdAndSubPlatformId>();
			for(String nodeId : nodeList){
				m_log.debug("getEnableCodeSet() target node  = " + nodeId);
				NodeInfo node = bean.getNode(nodeId);

				platformSet.add(new PlatformIdAndSubPlatformId(node.getPlatformFamily(), node.getSubPlatformFamily()));
			}

			////
			// 各プラットフォームごとに、収集可能な収集項目IDを取得する
			////
			for(PlatformIdAndSubPlatformId platform : platformSet){

				if (m_log.isDebugEnabled()) {
					m_log.debug("getEnableCodeSet() " +
							"platformId = " + platform.getPlatformId() +
							", subPlatformId = " + platform.getSubPlatformId());
				}

				// プラットフォーム、サブプラットフォーム単位のカテゴリコード、収集方法を取得
				Collection<CollectorCategoryCollectMstEntity> collects;
				// プラットフォーム、サブプラットフォーム単位の収集方法、アイテムコード、算出方法を取得
				List<CollectorItemCalcMethodMstEntity> calsMethods;
				try {
					collects = QueryUtil.getCollectorCategoryCollectMstByPlatformIdSubPlatformId(
							platform.getPlatformId(),
							platform.getSubPlatformId());
					calsMethods = QueryUtil.getCollectorItemCalcMethodMstByPlatformIdSubPlatformId(
							platform.getPlatformId(),
							platform.getSubPlatformId());
					if (m_log.isDebugEnabled()) {
						m_log.debug("getEnableCodeSet() " + "platformId = " + platform.getPlatformId() +
								", subPlatformId = " + platform.getSubPlatformId() +
								", CollectorCategoryCollectMstEntity size = " + collects.size() +
								", CollectorItemCalcMethodMstEntity size = " + calsMethods.size());
					}
					// VM管理やクラウド管理などサブプラットフォームがある場合には、サブプラットフォームが空（つまり物理と同じ）項目を監視できるようにする
					// 以下、サブプラットフォームが空の場合のカテゴリ＋収集方法、収集方法＋アイテムコード＋算出方法 を出し、上記のものと足し合わせる
					if (!platform.getSubPlatformId().isEmpty()) {
						Collection<CollectorCategoryCollectMstEntity> physicalCollects =
								QueryUtil.getCollectorCategoryCollectMstByPlatformIdSubPlatformId(
										platform.getPlatformId(), "");
						Collection<CollectorItemCalcMethodMstEntity> physicalCalsMethods =
								QueryUtil.getCollectorItemCalcMethodMstByPlatformIdSubPlatformId(
										platform.getPlatformId(), "");
						collects.addAll(physicalCollects);
						calsMethods.addAll(physicalCalsMethods);
						if (m_log.isDebugEnabled()) {
							m_log.debug("getEnableCodeSet() " + "platformId = " + platform.getPlatformId() +
									", physical platform, " +
									", CollectorCategoryCollectMstEntity size = " + physicalCollects.size() +
									", CollectorItemCalcMethodMstEntity size = " + physicalCalsMethods.size());
						}
					}
				} catch (Exception e) {
					m_log.warn("getEnableCodeSet() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					return null;
				}

				// categoryCodeをキーにcollectMethodを取得するマップ
				HashMap<String, String> categoryMap = new HashMap<String, String>();
				for(CollectorCategoryCollectMstEntity collect : collects){
					m_log.debug("getEnableCodeSet() add map categoryCode = " + collect.getId().getCategoryCode() + ", collectMethod = " + collect.getCollectMethod());
					categoryMap.put(collect.getId().getCategoryCode(), collect.getCollectMethod());
				}

				// このプラットフォーム・サブプラットフォームにおいて収集可能な収集項目コードを格納するSet
				HashSet<CollectorItemCodeMstData> enableItemCodeSetByPlatform = new HashSet<CollectorItemCodeMstData>();

				// 計算方法管理テーブルの各要素ごとに、その要素がこのプラットフォーム・サブプラットフォームにおいて収集可能な項目かを調べる
				for(CollectorItemCalcMethodMstEntity calcBean : calsMethods){

					CollectorItemCodeMstData codeBean;
					try {
						m_log.debug("getEnableCodeSet() search itemCode = " + calcBean.getId().getItemCode());
						codeBean = CollectorMasterCache.getCategoryCodeMst(calcBean.getId().getItemCode());
						if (codeBean == null) {
							// ここは通らないはず
							m_log.warn("getEnableCodeSet() codeBean is null. id = " + calcBean.getId());
							return null;
						}
					} catch (Exception e) {
						m_log.warn("getEnableCodeSet() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						return null;
					}

					// カテゴリ別収集情報の収集方法と収集項目マスタの収集方法を比較し、同じならば
					// このプラットフォームで利用可能な収集項目として、Setに追加する
					if(categoryMap.get(codeBean.getCategoryCode()) != null &&
							categoryMap.get(codeBean.getCategoryCode()).equals(calcBean.getId().getCollectMethod())) {
						// 収集項目コードマスタ情報のDTOを作成
						CollectorItemCodeMstData codeData = new CollectorItemCodeMstData(
								codeBean.getItemCode(),
								codeBean.getCategoryCode(),
								codeBean.getParentItemCode(),
								codeBean.getItemName(),
								codeBean.getMeasure(),
								codeBean.isDeviceSupport(),
								codeBean.getDeviceType(),
								codeBean.isGraphRange()
								);
						m_log.debug("getEnableCodeSet() add itemCode = " + calcBean.getId().getItemCode());
						enableItemCodeSetByPlatform.add(codeData);
					}

				}
				// 各プラットフォームで出した収集可能項目について、全てのプラットフォームを通じてANDをとる
				// プラットフォームに関するループ初回で取得した収集可能項目（厳密にはループ中で初めて取得できた
				// 収集可能項目）は一旦無条件に結果として格納し、以降のプラットフォームで取れる収集可能項目は
				// その結果に対してANDをとる（set.retainAll）
				// （つまり重複した部分のみを残して、重ならない部分はこそぎ落としていく）
				if(enableItemCodeSetInEveryNode == null){
					m_log.debug("getEnableCodeSet() enableCodeSetInEveryNode is null");
					enableItemCodeSetInEveryNode = enableItemCodeSetByPlatform;
				}
				enableItemCodeSetInEveryNode.retainAll(enableItemCodeSetByPlatform);
				m_log.debug("getEnableCodeSet() enableCodeSetInEveryNode size = " + enableItemCodeSetInEveryNode.size());
			}

		} catch (FacilityNotFound e) {
			m_log.debug("getEnableCodeSet " + facilityId);
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("getEnableCodeSet() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		// 1つもない場合は、0サイズのSetを返却
		if(enableItemCodeSetInEveryNode == null){
			enableItemCodeSetInEveryNode = new HashSet<CollectorItemCodeMstData>();
		}
		return enableItemCodeSetInEveryNode;
	}

	/**
	 * 指定したfacilityIdで収集可能な収集項目のリストを作成する
	 * 
	 * @param facilityId
	 * @return
	 * @throws HinemosUnknown
	 */
	public static List<CollectorItemInfo> getAvailableCollectorItemList(String facilityId) throws HinemosUnknown {
		m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId);

		// null check
		if(facilityId == null || "".equals(facilityId)){
			return new ArrayList<CollectorItemInfo>();
		}

		// 全てのノードに含まれるデバイスのセット
		Set<NodeDeviceInfo> deviceSet = getDeviceSetContainsAllNodes(facilityId);
		m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId + ", deviceSet size = " + deviceSet.size());

		// 選択可能な収集項目コード情報のセット
		Set<CollectorItemCodeMstData> itemCodeSet = getEnableCodeSet(facilityId);
		m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId + ", itemCodeSet size = " + itemCodeSet.size());

		// 戻り値
		List<CollectorItemInfo> list = new ArrayList<CollectorItemInfo>();

		for(CollectorItemCodeMstData itemCode : itemCodeSet){
			m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId + ", itemCode = " + itemCode.getItemCode() + ", deviceSupport = " + itemCode.isDeviceSupport().booleanValue());

			CollectorItemInfo itemInfo = null;

			if (itemCode.isDeviceSupport().booleanValue()) {
				//デバイスサポートあり
				for(NodeDeviceInfo deviceInfo : deviceSet){
					if(itemCode.getDeviceType() != null && itemCode.getDeviceType().equals(deviceInfo.getDeviceType())){
						itemInfo = new CollectorItemInfo(null, itemCode.getItemCode(), deviceInfo.getDeviceDisplayName());//collectorId is null

						m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId + ", itemCode = " + itemCode.getItemCode() + ", deviceDisplayName = " + deviceInfo.getDeviceDisplayName());
						list.add(itemInfo);
					}
				}

				// ALL Deviceの追加
				m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId + ", itemCode = " + itemCode.getItemCode() + ", deviceDisplayName = " + PollingDataManager.ALL_DEVICE_NAME);
				itemInfo = new CollectorItemInfo(null, itemCode.getItemCode(), PollingDataManager.ALL_DEVICE_NAME);//collectorId is null
				list.add(itemInfo);
			
			} else {
				//デバイスサポートなし
				itemInfo = new CollectorItemInfo(null, itemCode.getItemCode(), "");//collectorId is null

				m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId + ", itemCode = " + itemCode.getItemCode());
				list.add(itemInfo);
			}
		}

		// Sort
		Collections.sort(list,new Comparator<CollectorItemInfo>() {
			@Override
			public int compare(CollectorItemInfo o1, CollectorItemInfo o2) {
				return o1.getItemCode().compareTo(o2.getItemCode());
			}
		});

		m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId + ", list size = " + list.size());
		return list;
	}

	/**
	 * 指定したfacilityId配下の全てのノードに含まれるデバイスの集合を返却する。
	 * 
	 * @param facilityId
	 * @return
	 */
	private static Set<NodeDeviceInfo> getDeviceSetContainsAllNodes(String facilityId){
		m_log.debug("getDeviceSetContainsAllNodes() facilityId = " + facilityId);

		// 戻り値
		Set<NodeDeviceInfo> deviceSet = new HashSet<NodeDeviceInfo>();

		// 対象ファシリティIDが空またはNULLなら
		if(facilityId == null || "".equals(facilityId)){
			return deviceSet;
		}

		// 対象のファシリティIDリスト
		// targetFacilityIdListはノードのみ含まれる。
		List<String> targetFacilityIdList = FacilitySelector.getNodeFacilityIdList(facilityId, null, RepositoryControllerBean.ALL, false, true);
		List<NodeDeviceInfo> checkTargetList = null;
		for (String targetFacilityId : targetFacilityIdList){

			// (ノード)targetFacilityIdのデバイス一覧を取得
			List<NodeDeviceInfo> checkList = new ArrayList<NodeDeviceInfo>();
			try {
				NodeInfo node = NodeProperty.getProperty(targetFacilityId);
				checkList.addAll(node.getNodeDeviceInfo());
				checkList.addAll(node.getNodeCpuInfo());
				checkList.addAll(node.getNodeMemoryInfo());
				checkList.addAll(node.getNodeNetworkInterfaceInfo());
				checkList.addAll(node.getNodeDiskInfo());
				checkList.addAll(node.getNodeFilesystemInfo());
			} catch (FacilityNotFound e) {
			}

			// 1ノード目は全部セット
			if(checkTargetList == null){
				checkTargetList = checkList;
			}
			// 2ノード目は同じものだけを残す
			else{
				List<NodeDeviceInfo> tmpList = new ArrayList<NodeDeviceInfo>();

				for(NodeDeviceInfo checkInfo : checkList){
					for(NodeDeviceInfo targetInfo : checkTargetList){

						// デバイスタイプと表示名が同じものがあるか？
						if(checkInfo.getDeviceType().equals(targetInfo.getDeviceType()) &&
								checkInfo.getDeviceDisplayName().equals(targetInfo.getDeviceDisplayName())){
							tmpList.add(checkInfo);
							m_log.debug("getDeviceSetContainsAllNodes() facilityId = " + facilityId + ", " +
									"add device deviceType = " + checkInfo.getDeviceType() + ", deviceDisplayName = " + checkInfo.getDeviceDisplayName());
							break;
						}
					}
				}
				checkTargetList = tmpList;
			}
		}

		if(checkTargetList != null){
			deviceSet.addAll(checkTargetList);
		}

		return deviceSet;
	}


	/**
	 * 収集項目コードとリポジトリ表示名から収集項目表示用文字列を生成し返します。
	 * 形式 ：　収集名[リポジトリ表示名]
	 * 
	 * @param itemCode 収集項目コード
	 * @param displayName リポジトリ表示名
	 * @return 収集項目表示
	 */
	public static String getFullItemName(String itemCode, String displayName){
		m_log.debug("getFullItemName() itemCode = " + itemCode + ", displayName = " + displayName);

		try {
			CollectorItemCodeMstData bean = CollectorMasterCache.getCategoryCodeMst(itemCode);
			String itemName = bean.getItemName();
			if(bean.isDeviceSupport().booleanValue()){
				itemName = itemName + "[" + displayName + "]";
			}

			m_log.debug("getFullItemName() itemCode = " + itemCode + ", displayName = " + displayName + " : itemName = " + itemName);
			return itemName;
		} catch (Exception e) {
			m_log.warn("getFullItemName() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		m_log.debug("getFullItemName() itemCode = " + itemCode + ", displayName = " + displayName + " : itemName = " + itemCode + " is not found.");
		return itemCode + " is not found.";
	}

	/**
	 * 収集項目コードをキーにした、CollectorItemTreeItemのHashMapを返します。
	 * @return
	 */
	public static Map<String, CollectorItemTreeItem> getItemCodeMap(){
		return m_codeTable;
	}

}