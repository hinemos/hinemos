/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.factory;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoPK;
import com.clustercontrol.monitor.run.util.MonitorJudgementInfoCacheRefreshCallback;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * 文字列監視の判定情報を変更する抽象クラス<BR>
 * <p>
 * 文字列監視を行う各監視管理クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.1.0
 */
abstract public class ModifyMonitorStringValueType extends ModifyMonitor{

	/**
	 * 文字列監視の判定情報を作成し、監視情報に設定します。
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean
	 */
	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 文字列監視判定情報を設定
			List<MonitorStringValueInfo> list = m_monitorInfo.getStringValueInfo();
			if (list != null) {
				for(int index = 0; index < list.size(); index++){
					MonitorStringValueInfo value = list.get(index);
					value.setOrderNo(index + 1);
					em.persist(value);
					value.relateToMonitorInfo(m_monitorInfo);
				}
			}

			// 判定キャッシュを更新
			jtm.addCallback(new MonitorJudgementInfoCacheRefreshCallback(
					m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorType(), m_monitorInfo.getStringValueInfo(), null, null));

			return true;
		}
	}
	
	/**
	 * 監視情報より文字列監視の判定情報を取得し、変更します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を削除します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。</li>
	 * </ol>
	 * @throws MonitorNotFound
	 * @throws EntityExistsException
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean
	 */
	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

			// 文字列監視判定情報を設定
			List<MonitorStringValueInfo> valueList = m_monitorInfo.getStringValueInfo();
			if(valueList == null){
				return true;
			}

			List<MonitorStringValueInfoPK> monitorStringValueInfoEntityPkList = new ArrayList<MonitorStringValueInfoPK>();

			int orderNo = 0;
			for(MonitorStringValueInfo value : valueList){
				if(value != null){
					MonitorStringValueInfo entity = null;
					MonitorStringValueInfoPK entityPk = new MonitorStringValueInfoPK(
							m_monitorInfo.getMonitorId(),
							Integer.valueOf(++orderNo));
					try {
						entity = QueryUtil.getMonitorStringValueInfoPK(entityPk);
					} catch (MonitorNotFound e) {
						entity = new MonitorStringValueInfo(entityPk);
						em.persist(entity);
						entity.relateToMonitorInfo(monitorInfo);
					}
					entity.setCaseSensitivityFlg(value.getCaseSensitivityFlg());
					entity.setDescription(value.getDescription());
					entity.setMessage(value.getMessage());
					entity.setPattern(value.getPattern());
					entity.setPriority(value.getPriority());
					entity.setProcessType(value.getProcessType());
					entity.setValidFlg(value.getValidFlg());
					monitorStringValueInfoEntityPkList.add(entityPk);
				}
			}
			// 不要なMonitorStringValueInfoEntityを削除
			monitorInfo.deleteMonitorStringValueInfoEntities(monitorStringValueInfoEntityPkList);

			// 判定キャッシュを更新
			jtm.addCallback(new MonitorJudgementInfoCacheRefreshCallback(
					m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorType(), m_monitorInfo.getStringValueInfo(), null, null));

			return true;
		}
	}

}
