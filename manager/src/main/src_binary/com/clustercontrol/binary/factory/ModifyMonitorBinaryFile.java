/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.factory;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * バイナリファイル監視情報をマネージャで変更するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class ModifyMonitorBinaryFile extends ModifyMonitorBinary {

	private static Log m_log = LogFactory.getLog(ModifyMonitorBinaryFile.class);
	
	@Override
	protected boolean addMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, EntityExistsException, HinemosUnknown, InvalidRole {
		BinaryCheckInfo checkInfo = m_monitorInfo.getBinaryCheckInfo();
		
		// 増分の時間区切り監視以外は監視間隔を0にする
		if (!BinaryConstant.COLLECT_TYPE_ONLY_INCREMENTS.equals(checkInfo.getCollectType()) ||
				!BinaryConstant.CUT_TYPE_INTERVAL.equals(checkInfo.getCutType())) {
			m_monitorInfo.setRunInterval(0);
		}

		return super.addMonitorInfo(user);
	}

	/**
	 * バイナリファイル監視情報追加<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole {
		// バイナリ監視情報を取得.
		BinaryCheckInfo checkInfo = m_monitorInfo.getBinaryCheckInfo();
		checkInfo.setMonitorId(m_monitorInfo.getMonitorId());
		
		// バイナリ監視情報を追加
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.persist(checkInfo);
		}
		return true;
	}
	
	@Override
	protected boolean modifyMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {
		BinaryCheckInfo checkInfo = m_monitorInfo.getBinaryCheckInfo();
		
		// 増分の時間区切り監視以外は監視間隔を0にする
		if (!BinaryConstant.COLLECT_TYPE_ONLY_INCREMENTS.equals(checkInfo.getCollectType()) ||
				!BinaryConstant.CUT_TYPE_INTERVAL.equals(checkInfo.getCutType())) {
			m_monitorInfo.setRunInterval(0);
		}
		
		return super.modifyMonitorInfo(user);
	}

	/**
	 * バイナリファイル監視情報変更<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		// バイナリ監視情報を取得.
		BinaryCheckInfo oldEntity = m_monitorInfo.getBinaryCheckInfo();
		BinaryCheckInfo entity = QueryUtil.getBinaryCheckInfoPK(m_monitorInfo.getMonitorId());

		// バイナリ監視情報を変更.
		entity.setDirectory(oldEntity.getDirectory());
		entity.setFileName(oldEntity.getFileName());
		entity.setCollectType(oldEntity.getCollectType());
		entity.setCutType(oldEntity.getCutType());
		entity.setTagType(oldEntity.getTagType());
		entity.setLengthType(oldEntity.getLengthType());
		entity.setHaveTs(oldEntity.isHaveTs());
		entity.setFileHeadSize(oldEntity.getFileHeadSize());
		entity.setRecordSize(oldEntity.getRecordSize());
		entity.setRecordHeadSize(oldEntity.getRecordHeadSize());
		entity.setSizePosition(oldEntity.getSizePosition());
		entity.setSizeLength(oldEntity.getSizeLength());
		entity.setTsPosition(oldEntity.getTsPosition());
		entity.setTsType(oldEntity.getTsType());
		entity.setLittleEndian(oldEntity.isLittleEndian());

		// 変更されると収集形式が変わる情報についてログ出力.
		m_log.trace("modify() : entity.getDirectory = " + entity.getDirectory());
		m_log.trace("modify() : entity.getFileName = " + entity.getFileName());
		m_log.trace("modify() : entity.getCollectType = " + entity.getCollectType());
		m_log.trace("modify() : entity.getCutType = " + entity.getCutType());
		m_log.trace("modify() : entity.getTagType = " + entity.getTagType());
		m_log.trace("modify() : entity.getLengthType = " + entity.getLengthType());
		m_log.trace("modify() : entity.isHaveTs = " + entity.isHaveTs());
		m_log.trace("modify() : entity.getFileHeadSize = " + entity.getFileHeadSize());
		m_log.trace("modify() : entity.getRecordSize = " + entity.getRecordSize());
		m_log.trace("modify() : entity.getRecordHeadSize = " + entity.getRecordHeadSize());
		m_log.trace("modify() : entity.getSizePosition = " + entity.getSizePosition());
		m_log.trace("modify() : entity.getSizeLength = " + entity.getSizeLength());
		m_log.trace("modify() : entity.getTsPosition = " + entity.getTsPosition());
		m_log.trace("modify() : entity.getTsType = " + entity.getTsType());
		m_log.trace("modify() : entity.isLittleEndian = " + entity.isLittleEndian());

		return true;

	}

	/**
	 * バイナリファイル監視のスケジュール実行種別としてトリガー指定なしを設定.<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.NONE;
	}

	/**
	 * バイナリファイル監視のスケジュール実行遅延時間として0を設定.<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	protected int getDelayTime() {
		return 0;
	}

}
