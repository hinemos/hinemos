/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.factory;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityExistsException;

import com.clustercontrol.binary.model.BinaryPatternInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoPK;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * バイナリ監視情報をマネージャで変更するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 * 
 */
abstract public class ModifyMonitorBinary extends ModifyMonitor {

	/**
	 * バイナリ監視の判定情報を作成し、監視情報に設定します。
	 * 
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean
	 */
	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// バイナリ検索条件情報を設定.
			List<BinaryPatternInfo> binaryList = m_monitorInfo.getBinaryPatternInfo();
			if (binaryList != null) {
				for (int index = 0; index < binaryList.size(); index++) {
					BinaryPatternInfo value = binaryList.get(index);
					value.setMonitorId(m_monitorInfo.getMonitorId());
					value.setOrderNo(index + 1);
					em.persist(value);
					value.relateToMonitorInfo(m_monitorInfo);
				}
			}
			return true;
		}
	}

	/**
	 * 監視情報よりバイナリ監視の判定情報を取得し、変更します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を削除します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。</li>
	 * </ol>
	 * 
	 * @throws MonitorNotFound
	 * @throws EntityExistsException
	 * @throws InvalidRole
	 * @see MonitorInfoBean
	 * @see MonitorStringValueInfoBean
	 */
	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			MonitorInfo monitorInfo = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

			// バイナリ監視判定情報を設定.
			List<BinaryPatternInfo> binaryList = m_monitorInfo.getBinaryPatternInfo();
			if (binaryList == null) {
				return true;
			}

			// バイナリ検索条件の変更.
			List<MonitorStringValueInfoPK> binaryEntityPkList = new ArrayList<MonitorStringValueInfoPK>();
			int orderNo = 0;
			for (BinaryPatternInfo value : binaryList) {
				if (value != null) {
					BinaryPatternInfo entity = null;
					MonitorStringValueInfoPK entityPk = new MonitorStringValueInfoPK(m_monitorInfo.getMonitorId(),
							Integer.valueOf(++orderNo));
					try {
						entity = QueryUtil.getBinaryPatternInfoPK(entityPk);
					} catch (MonitorNotFound e) {
						entity = new BinaryPatternInfo(entityPk);
						em.persist(entity);
						entity.relateToMonitorInfo(monitorInfo);
					}
					entity.setDescription(value.getDescription());
					entity.setGrepString(value.getGrepString());
					entity.setEncoding(value.getEncoding());
					entity.setProcessType(value.getProcessType());
					entity.setPriority(value.getPriority());
					entity.setMessage(value.getMessage());
					entity.setValidFlg(value.getValidFlg());
					binaryEntityPkList.add(entityPk);
				}
			}

			// 不要なMonitorStringValueInfoEntityを削除
			monitorInfo.deleteBinaryPatternInfoEntities(binaryEntityPkList);

			return true;
		}
	}

}
