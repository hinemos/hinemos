/*

Copyright (C) 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.process.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.process.entity.MonitorProcessMethodMstPK;
import com.clustercontrol.process.entity.MonitorProcessPollingMstData;
import com.clustercontrol.process.entity.MonitorProcessPollingMstPK;
import com.clustercontrol.process.factory.ProcessMasterCache;
import com.clustercontrol.process.model.MonitorProcessMethodMstEntity;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ポーリングに関する情報をDBから取得するクラス<BR>
 *
 * @version 4.0.0
 * @since 3.1.0
 */
public class PollingDataManager {

	private static Log m_log = LogFactory.getLog( PollingDataManager.class );

	private String m_platformId = "";
	private String m_subPlatformId = "";

	/**
	 * コンストラクタ
	 * 
	 * @param ファシリティID
	 */
	public PollingDataManager(String facilityId){
		m_log.debug("PollingDataManager() facilityId = " + facilityId);

		try {

			// プラットフォームIDとサブプラットフォームIDを問い合わせる
			NodeInfo info = new RepositoryControllerBean().getNode(facilityId);

			m_platformId = info.getPlatformFamily();
			m_subPlatformId = info.getSubPlatformFamily();

			// サブプラットフォームIDのnullチェック
			if(m_subPlatformId == null)
				m_subPlatformId = "";

		} catch (FacilityNotFound e) {
			m_log.debug(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			m_log.debug(e.getMessage(), e);
		}
	}

	private static ConcurrentHashMap<MonitorProcessMethodMstPK, String> methodCache =
			new ConcurrentHashMap<MonitorProcessMethodMstPK, String> ();

	static {
		JpaTransactionManager jtm = new JpaTransactionManager();
		if (!jtm.isNestedEm()) {
			m_log.warn("refresh() : transactioin has not been begined.");
			jtm.close();
		} else {

			try {
				List<MonitorProcessMethodMstEntity> c
				= QueryUtil.getAllMonitorProcessMethodMst();
				for (MonitorProcessMethodMstEntity entity : c) {
					MonitorProcessMethodMstPK pk
					= new MonitorProcessMethodMstPK(
							entity.getId().getPlatformId(),
							entity.getId().getSubPlatformId());
					methodCache.put(pk, entity.getCollectMethod());
				}
			} catch (Exception e) {
				m_log.warn("static() create MonitorProcessMethodMst cache failed. : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}

	}

	/**
	 * 収集方法（SNMP,WBEMなど）の取得
	 * 
	 * @return 収集方法
	 */

	public String getCollectMethod(){

		String collectMethod = "";

		m_log.debug("getColledtMethod() m_platformId : " + m_platformId + ", m_subPlatformId : " + m_subPlatformId);
		// 収集方法を取得する
		MonitorProcessMethodMstPK processPk = new MonitorProcessMethodMstPK(
				m_platformId,
				m_subPlatformId);

		collectMethod = methodCache.get(processPk);
		if (collectMethod == null) {
			m_log.info("getCollectMethod() : collectMethod is null.");
		}

		return collectMethod;
	}

	/**
	 * ポーリング対象の取得
	 * 
	 * @return ポーリング対象のリスト
	 */
	public Set<String> getPollingTargets(String collectMethod){
		m_log.debug("getPollingTargets() collectMethod = " + collectMethod);

		Set<String> pollingTargets = new HashSet<String>();

		MonitorProcessPollingMstData data = null;
		data = ProcessMasterCache.getMonitorProcessPollingMst(new MonitorProcessPollingMstPK(
				collectMethod, m_platformId, m_subPlatformId,
				"name"));
		if (data != null) {
			pollingTargets.add(data.getPollingTarget());
		}

		data = ProcessMasterCache.getMonitorProcessPollingMst(new MonitorProcessPollingMstPK(
				collectMethod, m_platformId, m_subPlatformId,
				"param"));
		if (data != null) {
			pollingTargets.add(data.getPollingTarget());
		}

		data = ProcessMasterCache.getMonitorProcessPollingMst(new MonitorProcessPollingMstPK(
				collectMethod, m_platformId, m_subPlatformId,
				"path"));
		if (data != null) {
			pollingTargets.add(data.getPollingTarget());
		}
		if (pollingTargets.size() == 0) {
			m_log.info("pollingTargets.size == 0");
		}

		return pollingTargets;
	}


	/**
	 * インスタンス生成時に与えたファシリティIDのプラットフォームIDを取得する
	 * @return プラットフォームID
	 */
	public String getPlatformId() {
		return m_platformId;
	}

	/**
	 * インスタンス生成時に与えたファシリティIDのサブプラットフォームIDを取得する
	 * @return サブプラットフォームID
	 */
	public String getSubPlatformId() {
		return m_subPlatformId;
	}
}
