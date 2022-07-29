/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.session;


import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.util.code.CollectorItemCodeTable;
import com.clustercontrol.performance.util.code.CollectorItemTreeItem;

/**
 *　性能管理機能の管理を行うコントローラクラス
 * クライアントからの Entity Bean へのアクセスは、このSession Bean を介して行います。
 * 
 * @version 4.0.0
 * @since 1.0.0
 *
 */
public class PerformanceControllerBean {

	//	ログ出力
	private static Log m_log = LogFactory.getLog(PerformanceControllerBean.class);

	/**
	 * 収集項目コードの一覧を取得します
	 * 
	 * @return 収集項目IDをキーとしCollectorItemTreeItemが格納されているHashMap
	 */
	public Map<String, CollectorItemTreeItem> getItemCodeMap() throws HinemosUnknown {
		m_log.debug("getItemCodeMap()");

		JpaTransactionManager jtm = null;
		Map<String, CollectorItemTreeItem> map = null;
		
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			map = CollectorItemCodeTable.getItemCodeMap();
			jtm.commit();
		} catch (Exception e){
			m_log.warn("getItemCodeMap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return map;
	}

	/**
	 * 指定のファシリティで収集可能な項目のリストを返します
	 * デバイス別の収集項目があり、ノード毎に登録されているデバイス情報が異なるため、
	 * 取得可能な収集項目はファシリティ毎に異なる。
	 * 
	 * @param facilityId ファシリティID
	 * @return 指定のファシリティで収集可能な項目のリスト
	 * @throws HinemosUnknown
	 */
	public List<CollectorItemInfo> getAvailableCollectorItemList(String facilityId) throws HinemosUnknown {
		m_log.debug("getAvailableCollectorItemList() facilityId = " + facilityId);

		JpaTransactionManager jtm = null;
		List<CollectorItemInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = CollectorItemCodeTable.getAvailableCollectorItemList(facilityId);
			jtm.commit();
		} catch (HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("getAvailableCollectorItemList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}




}