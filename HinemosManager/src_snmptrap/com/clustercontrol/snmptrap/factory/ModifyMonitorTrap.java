/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.snmptrap.model.TrapCheckInfo;
import com.clustercontrol.snmptrap.model.TrapValueInfo;
import com.clustercontrol.snmptrap.model.TrapValueInfoPK;
import com.clustercontrol.snmptrap.model.VarBindPattern;
import com.clustercontrol.snmptrap.model.VarBindPatternPK;
import com.clustercontrol.snmptrap.util.CharsetUtil;
import com.clustercontrol.snmptrap.util.QueryUtil;

import jakarta.persistence.EntityExistsException;

/**
 * SNMPTRAP監視情報を変更するクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class ModifyMonitorTrap extends ModifyMonitor {

	private static Log m_log = LogFactory.getLog( ModifyMonitorTrap.class );
	/**
	 * SNMPトラップ監視設定の追加(追加時に文字コードをチェック)
	 */
	@Override
	protected boolean addMonitorInfo(String user) throws MonitorIdInvalid, MonitorNotFound, TriggerSchedulerException,
			EntityExistsException, HinemosUnknown, InvalidRole {
		CharsetUtil.checkCharset(m_monitorInfo);

		return super.addMonitorInfo(user);
	}

	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		m_log.debug("addCheckInfo() : start");

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// SNMPTRAP監視情報を登録
			TrapCheckInfo trap = m_monitorInfo.getTrapCheckInfo();
			trap.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(trap);
			
			if(m_log.isDebugEnabled()){
				m_log.debug("addCheckInfo() : " +
						" MonitorId = " + m_monitorInfo.getMonitorId() +
						",CommunityName = " + trap.getCommunityName() +
						",CommunityCheck = " + trap.getCommunityCheck() +
						",CharsetConvert = " + trap.getCharsetConvert() +
						",CharsetName = " + trap.getCharsetName() +
						",NotifyofReceivingUnspecifiedFlg = " + trap.getNotifyofReceivingUnspecifiedFlg() +
						",PriorityUnspecified = " + trap.getPriorityUnspecified() +
						",TrapValueInfos = " + trap.getTrapValueInfos()
						);
			}
			
			for (TrapValueInfo value: trap.getTrapValueInfos()) {
				value.setMonitorId(m_monitorInfo.getMonitorId());
				if (value.getVersion() == SnmpVersionConstant.TYPE_V2){
					value.setGenericId(Integer.valueOf(0));
					value.setSpecificId(Integer.valueOf(0));
				}
				if (!value.getTrapOid().startsWith("."))
					value.setTrapOid("." + value.getTrapOid());
				em.persist(value);
				value.relateToMonitorTrapInfoEntity(trap);
				
				if (value.getFormatVarBinds() == null) {
					value.setFormatVarBinds("");
				}
				
				for (int i = 0; i < value.getVarBindPatterns().size(); ++i) {
					VarBindPattern pattern = value.getVarBindPatterns().get(i);
					pattern.setMonitorId(m_monitorInfo.getMonitorId());
					pattern.setMib(value.getMib());
					pattern.setTrapOid(value.getTrapOid());
					pattern.setGenericId(value.getGenericId() == null ? Integer.valueOf(0): value.getGenericId());
					pattern.setSpecificId(value.getSpecificId() == null ? Integer.valueOf(0): value.getSpecificId());
					pattern.setOrderNo(i);
					em.persist(pattern);
					pattern.relateToMonitorTrapValueInfoEntity(value);
				}
			}

			m_log.debug("addCheckInfo() : end");
			return true;
		}
	}
	
	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {
		return true;
	}
	
	/**
	 * SNMPトラップ監視設定の変更(変更時に文字コードをチェック)
	 */
	@Override
	protected boolean modifyMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {
		CharsetUtil.checkCharset(m_monitorInfo);

		return super.modifyMonitorInfo(user);
	}

	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {
		m_log.debug("modifyCheckInfo() : start");

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// SNMPTRAP監視情報を取得
			TrapCheckInfo checkInfo = m_monitorInfo.getTrapCheckInfo();
			if(m_log.isDebugEnabled()){
				m_log.debug("modifyCheckInfo() : " +
						" MonitorId = " + m_monitorInfo.getMonitorId() +
						",CommunityName = " + checkInfo.getCommunityName() +
						",CommunityCheck = " + checkInfo.getCommunityCheck() +
						",CharsetConvert = " + checkInfo.getCharsetConvert() +
						",CharsetName = " + checkInfo.getCharsetName() +
						",NotifyofReceivingUnspecifiedFlg = " + checkInfo.getNotifyofReceivingUnspecifiedFlg() +
						",PriorityUnspecified = " + checkInfo.getPriorityUnspecified() +
						",TrapValueInfos = " + checkInfo.getTrapValueInfos()
						);
			}

			TrapCheckInfo trapInfoEntity = QueryUtil.getMonitorTrapInfoPK(m_monitorInfo.getMonitorId());

			trapInfoEntity.setMonitorId(m_monitorInfo.getMonitorId());
			trapInfoEntity.setCharsetConvert(checkInfo.getCharsetConvert());
			trapInfoEntity.setCharsetName(checkInfo.getCharsetName());
			trapInfoEntity.setCommunityCheck(checkInfo.getCommunityCheck());
			trapInfoEntity.setCommunityName(checkInfo.getCommunityName());
			trapInfoEntity.setNotifyofReceivingUnspecifiedFlg(checkInfo.getNotifyofReceivingUnspecifiedFlg());
			trapInfoEntity.setPriorityUnspecified(checkInfo.getPriorityUnspecified());

			List<TrapValueInfo> trapValueList = new ArrayList<TrapValueInfo>(checkInfo.getTrapValueInfos());

			List<TrapValueInfoPK> monitorTrapValueInfoEntityPkList = new ArrayList<TrapValueInfoPK>();
			for (TrapValueInfo trapValue : trapValueList) {
				if (trapValue != null) {
					if (!trapValue.getTrapOid().startsWith(".")) {
						trapValue.setTrapOid("." + trapValue.getTrapOid());
					}
					if (trapValue.getVersion() == SnmpVersionConstant.TYPE_V2){
						trapValue.setGenericId(Integer.valueOf(0));
						trapValue.setSpecificId(Integer.valueOf(0));
					}
					TrapValueInfo entity = null;
					TrapValueInfoPK entityPk = new TrapValueInfoPK(
							m_monitorInfo.getMonitorId(),
							trapValue.getMib(),
							trapValue.getTrapOid(), 
							trapValue.getGenericId() == null ? Integer.valueOf(0): trapValue.getGenericId(),
							trapValue.getSpecificId() == null ? Integer.valueOf(0): trapValue.getSpecificId());
					try {
						entity = QueryUtil.getMonitorTrapValueInfoPK(entityPk);
					} catch (MonitorNotFound e) {
						entity = new TrapValueInfo(entityPk);
						em.persist(entity);
						entity.relateToMonitorTrapInfoEntity(trapInfoEntity);
					}
					
					if (trapValue.getFormatVarBinds() == null) {
						entity.setFormatVarBinds("");
					} else {
						entity.setFormatVarBinds(trapValue.getFormatVarBinds());
					}
					
					entity.setDescription(trapValue.getDescription());
					entity.setLogmsg(trapValue.getLogmsg());
					entity.setPriorityAnyVarbind(trapValue.getPriorityAnyVarbind());
					entity.setProcessingVarbindSpecified(trapValue.getProcessingVarbindSpecified());
					entity.setUei(trapValue.getUei());
					entity.setValidFlg(trapValue.getValidFlg());
					entity.setVersion(trapValue.getVersion());
					
					List<VarBindPattern> varbindList = new ArrayList<VarBindPattern>(trapValue.getVarBindPatterns());
					List<VarBindPatternPK> monitorTrapVarbindPatternInfoEntityPkList = new ArrayList<VarBindPatternPK>();
					int orderNo = 0;
					for (VarBindPattern varbind : varbindList) {
						if (varbind != null) {
							VarBindPattern varbindEntity = null;
							VarBindPatternPK varbindEntityPk = new VarBindPatternPK(
									m_monitorInfo.getMonitorId(),
									trapValue.getMib(),
									trapValue.getTrapOid(), 
									trapValue.getGenericId() == null ? Integer.valueOf(0): trapValue.getGenericId(),
									trapValue.getSpecificId() == null ? Integer.valueOf(0): trapValue.getSpecificId(),
									Integer.valueOf(orderNo));
							try {
								varbindEntity = QueryUtil.getMonitorTrapVarbindPatternInfoPK(varbindEntityPk);
							} catch (MonitorNotFound e) {
								varbindEntity = new VarBindPattern(varbindEntityPk);
								em.persist(varbindEntity);
								varbindEntity.relateToMonitorTrapValueInfoEntity(entity);
							}
							orderNo++;
							
							varbindEntity.setCaseSensitivityFlg(varbind.getCaseSensitivityFlg());
							varbindEntity.setDescription(varbind.getDescription());
							varbindEntity.setPattern(varbind.getPattern());
							varbindEntity.setPriority(varbind.getPriority());
							varbindEntity.setProcessType(varbind.getProcessType());
							varbindEntity.setValidFlg(varbind.getValidFlg());
							monitorTrapVarbindPatternInfoEntityPkList.add(varbindEntityPk);
						}
					}
					entity.deleteMonitorTrapVarbindPatternInfoEntities(monitorTrapVarbindPatternInfoEntityPkList);
					
					monitorTrapValueInfoEntityPkList.add(entityPk);
				}
			}
			trapInfoEntity.deleteMonitorTrapValueInfoEntities(monitorTrapValueInfoEntityPkList);

			m_log.debug("modifyCheckInfo() : end");
			return true;
		}
	}
	
	@Override
	protected boolean deleteCheckInfo() {
		TrapCheckInfo trapInfoEntity = m_monitor.getTrapCheckInfo();
		trapInfoEntity.deleteMonitorTrapValueInfoEntities(Collections.emptyList());
		return true;
	}
	
	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return 0;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.NONE;
	}

	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {
		return true;
	}
}
