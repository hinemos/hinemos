/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.custom.bean.CommandExecuteDTO;
import com.clustercontrol.custom.bean.CommandVariableDTO;
import com.clustercontrol.custom.bean.CustomConstant;
import com.clustercontrol.custom.bean.Type;
import com.clustercontrol.custom.model.CustomCheckInfo;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * コマンド監視の特有設定に対する参照処理実装クラス
 * 
 * @version 6.0.0
 * @since 4.0.0
 */
public class SelectCustom extends SelectMonitor {

	private static Log m_log = LogFactory.getLog( SelectCustom.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(SelectCustom.class.getName());
		
		try {
			_lock.writeLock();
			
			ArrayList<MonitorInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				refreshCache();
			}
		} finally {
			_lock.writeUnlock();
			m_log.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : " + (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<MonitorInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_CUSTOM);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_CUSTOM + " : " + cache);
		return cache == null ? null : (ArrayList<MonitorInfo>)cache;
	}
	
	private static void storeCache(ArrayList<MonitorInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_CUSTOM + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_CUSTOM, newCache);
	}

	public static void refreshCache() {
		m_log.info("refresh cache");
		
		long startTime = HinemosTime.currentTimeMillis();
		try {
			_lock.writeLock();
			
			ArrayList<MonitorInfo> customCache = new SelectCustom().getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_CUSTOM_N);
			customCache.addAll(new SelectCustom().getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_CUSTOM_S));
			storeCache(customCache);
			m_log.info("refresh customCache " + (HinemosTime.currentTimeMillis() - startTime) +
					"ms. size=" + customCache.size());
		} catch (Exception e) {
			m_log.warn("failed refreshing cache.", e);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * 
	 * 要求されたエージェントが実行すべきコマンド実行情報の一覧を返す。<br/>
	 * @param requestedFacilityId 要求したエージェントのファシリティID
	 * @return コマンド実行情報の一覧
	 * @throws CustomInvalid コマンド監視設定
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<CommandExecuteDTO> getCommandExecuteDTO(String requestedFacilityId) throws CustomInvalid, InvalidRole, HinemosUnknown {
		// Local Variables
		CommandExecuteDTO dto = null;
		ArrayList<CommandExecuteDTO> dtos = new ArrayList<CommandExecuteDTO>();

		RepositoryControllerBean repositoryCtrl = null;

		CustomCheckInfo cmdInfo = null;
		CalendarInfo calendar = null;
		ArrayList<String> facilityIds = null;
		NodeInfo nodeInfo = null;

		List<CommandVariableDTO> variables = null;

		// MAIN
		if (requestedFacilityId == null) {
			HinemosUnknown e = new HinemosUnknown("facilityId is null. (facilityId = null)");
			m_log.info("getCommandExecuteDTO() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		String monitorId = null;
		try {
			
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			List<MonitorInfo> monitorList = getCache();
			
			repositoryCtrl = new RepositoryControllerBean();

			for (MonitorInfo info : monitorList) {
				monitorId = info.getMonitorId();
				if (!info.getMonitorFlg() && !info.getCollectorFlg()) {
					if (m_log.isDebugEnabled())
						m_log.debug("command monitor is not enabled. (monitorId = " + info.getMonitorId() + ", facilityId = " + requestedFacilityId + ")");
					continue;
				}

				cmdInfo = info.getCustomCheckInfo();
				calendar = new CalendarControllerBean().getCalendarFull(info.getCalendarId());

				if (cmdInfo.getCommandExecType() == CustomConstant.CommandExecType.INDIVIDUAL) {
					// コマンド監視の対象スコープに含まれる有効ノードとして、問い合わせたエージェントが含まれるかどうか
					facilityIds = repositoryCtrl.getNodeFacilityIdList(info.getFacilityId(), info.getOwnerRoleId(), RepositoryControllerBean.ALL, false, true);
					if (! facilityIds.contains(requestedFacilityId)) {
						if (m_log.isDebugEnabled())
							m_log.debug("CommandExcecuteDTO is required from not-contained facility. (monitorId = " + info.getMonitorId() + ", facilityId = " + requestedFacilityId + ")");
						continue;
					}
					facilityIds = new ArrayList<String>();
					facilityIds.add(requestedFacilityId);
				} else {
					// コマンド監視の指定ノードと問い合わせたエージェントが一致するかどうか
					if (! requestedFacilityId.equals(cmdInfo.getSelectedFacilityId())) {
						if (m_log.isDebugEnabled())
							m_log.debug("CommandExcecuteDTO is required from not-selected facility. (monitorId = " + info.getMonitorId() + ", facilityId = " + requestedFacilityId + ")");
						continue;
					}
					if (! repositoryCtrl.getNode(requestedFacilityId).getValid()) {
						if (m_log.isDebugEnabled()) {
							m_log.debug("requestedFacilityId is not enabled. (monitorId = " + info.getMonitorId() + ", facilityId = " + requestedFacilityId + ")");
						}
						continue;
					}
					facilityIds = repositoryCtrl.getNodeFacilityIdList(info.getFacilityId(), info.getOwnerRoleId(), RepositoryControllerBean.ALL, false, true);
				}

				variables = new ArrayList<CommandVariableDTO>();
				for (String facilityId : facilityIds) {
					if (m_log.isDebugEnabled())
						m_log.debug("facility variables are assigned to CommandExecuteDTO. (monitroId = " + info.getMonitorId() + ", facilityId = " + facilityId + ")");
					nodeInfo = repositoryCtrl.getNode(facilityId);
					Map<String, String> variable = RepositoryUtil.createNodeParameter(nodeInfo);
					variables.add(new CommandVariableDTO(facilityId, variable));
				}
				
				if (HinemosModuleConstant.MONITOR_CUSTOM_N.equals(info.getMonitorTypeId())) {
					dto = new CommandExecuteDTO(info.getMonitorId(), requestedFacilityId, cmdInfo.getSpecifyUser(), cmdInfo.getEffectiveUser(), cmdInfo.getCommand(), cmdInfo.getTimeout(), info.getRunInterval() * 1000, calendar, variables ,Type.NUMBER);
				}else if (HinemosModuleConstant.MONITOR_CUSTOM_S.equals(info.getMonitorTypeId())) {
					dto = new CommandExecuteDTO(info.getMonitorId(), requestedFacilityId, cmdInfo.getSpecifyUser(), cmdInfo.getEffectiveUser(), cmdInfo.getCommand(), cmdInfo.getTimeout(), info.getRunInterval() * 1000, calendar, variables, Type.STRING);
				}else{
					m_log.warn("monitor type Error " + info.getMonitorTypeId());
				}
				dtos.add(dto);

				m_log.debug("getCommandExecuteDTO() CommandExecuteDTO is retured to agent. (requestedFacilityId = " + requestedFacilityId + ", dto = " + dto + ")");
			}

		} catch (CalendarNotFound e) {
			m_log.info("getCommandExecuteDTO() configuration of custom is not valid. : "
					+ "facilityId=" + requestedFacilityId + ", monitorId=" + monitorId
					+ ", class=" + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CustomInvalid("configuration of custom is not valid.", e);
		} catch (FacilityNotFound e) {
			m_log.info("getCommandExecuteDTO() configuration of custom is not valid. : "
					+ "facilityId=" + requestedFacilityId + ", monitorId=" + monitorId
					+ ", class=" + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CustomInvalid("configuration of custom is not valid.", e);
		} catch (InvalidRole e) {
			m_log.info("getCommandExecuteDTO() configuration of custom is not valid. : "
					+ "facilityId=" + requestedFacilityId + ", monitorId=" + monitorId
					+ ", class=" + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (HinemosUnknown e) {
			m_log.info("getCommandExecuteDTO() unexpected internal failure occurred. : "
					+ "facilityId=" + requestedFacilityId + ", monitorId=" + monitorId
					+ ", class=" + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new HinemosUnknown("unexpected internal failure occurred.", e);
		} catch (Exception e) {
			m_log.warn("getCommandExecuteDTO() unexpected internal failure occurred. : "
					+ "facilityId=" + requestedFacilityId + ", monitorId=" + monitorId
					+ ", class=" + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown("unexpected internal failure occurred.", e);
		}

		return dtos;
	}

	/**
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * ※監視ジョブ用
	 * 
	 * 要求されたエージェントが実行すべきコマンド実行情報の一覧を返す。<br/>
	 * @param requestedFacilityId 要求したエージェントのファシリティID
	 * @return コマンド実行情報の一覧
	 * @throws CustomInvalid コマンド監視設定
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<CommandExecuteDTO> getCommandExecuteDTOForMonitorJob(String requestedFacilityId) throws CustomInvalid, InvalidRole, HinemosUnknown {
		// Local Variables
		CommandExecuteDTO dto = null;
		ArrayList<CommandExecuteDTO> dtos = new ArrayList<CommandExecuteDTO>();

		RepositoryControllerBean repositoryCtrl = null;

		CustomCheckInfo cmdInfo = null;
		ArrayList<String> facilityIds = null;
		NodeInfo nodeInfo = null;

		List<CommandVariableDTO> variables = null;

		if (requestedFacilityId == null) {
			HinemosUnknown e = new HinemosUnknown("facilityId is null. (facilityId = null)");
			m_log.info("getCommandExecuteDTO() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		try {
			// 監視対象の情報を取得
			for (String monitorTypeId : new String[]{HinemosModuleConstant.MONITOR_CUSTOM_N, HinemosModuleConstant.MONITOR_CUSTOM_S}) {
				Map<RunInstructionInfo, MonitorInfo> monitorMap = MonitorJobWorker.getMonitorJobMap(monitorTypeId);
				
				repositoryCtrl = new RepositoryControllerBean();
	
				for (Map.Entry<RunInstructionInfo, MonitorInfo> entry : monitorMap.entrySet()) {
	
					cmdInfo = entry.getValue().getCustomCheckInfo();
	
					// ジョブセッション情報の取得
					//セッションIDとジョブIDから、セッションジョブを取得
					JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(
							entry.getKey().getSessionId(), 
							entry.getKey().getJobunitId(), 
							entry.getKey().getJobId());
	
					if (cmdInfo.getCommandExecType() == CustomConstant.CommandExecType.INDIVIDUAL) {
						// コマンド監視の対象スコープに含まれる有効ノードとして、問い合わせたエージェントが含まれるかどうか
						facilityIds = repositoryCtrl.getNodeFacilityIdList(entry.getKey().getFacilityId(), sessionJob.getOwnerRoleId(), RepositoryControllerBean.ALL, false, true);
						if (! facilityIds.contains(requestedFacilityId)) {
							continue;
						}
						facilityIds = new ArrayList<String>();
						facilityIds.add(requestedFacilityId);
					} else {
						// コマンド監視の指定ノードと問い合わせたエージェントが一致するかどうか
						if (! requestedFacilityId.equals(cmdInfo.getSelectedFacilityId())) {
							continue;
						}
						if (! repositoryCtrl.getNode(requestedFacilityId).getValid()) {
							continue;
						}
						facilityIds = repositoryCtrl.getNodeFacilityIdList(entry.getKey().getFacilityId(), sessionJob.getOwnerRoleId(), RepositoryControllerBean.ALL, false, true);
					}
	
					variables = new ArrayList<CommandVariableDTO>();
					for (String facilityId : facilityIds) {
						nodeInfo = repositoryCtrl.getNode(facilityId);
						Map<String, String> variable = RepositoryUtil.createNodeParameter(nodeInfo);
						variables.add(new CommandVariableDTO(facilityId, variable));
					}
					
					if (HinemosModuleConstant.MONITOR_CUSTOM_N.equals(entry.getValue().getMonitorTypeId())) {
						dto = new CommandExecuteDTO(
								entry.getValue().getMonitorId(),
								requestedFacilityId,
								cmdInfo.getSpecifyUser(),
								cmdInfo.getEffectiveUser(),
								cmdInfo.getCommand(),
								cmdInfo.getTimeout(),
								RunInterval.TYPE_MIN_01.toSec() * 1000,
								null,
								variables,
								entry.getKey(),
								Type.NUMBER);
					}else if (HinemosModuleConstant.MONITOR_CUSTOM_S.equals(entry.getValue().getMonitorTypeId())) {
						dto = new CommandExecuteDTO(
								entry.getValue().getMonitorId(),
								requestedFacilityId,
								cmdInfo.getSpecifyUser(),
								cmdInfo.getEffectiveUser(),
								cmdInfo.getCommand(),
								cmdInfo.getTimeout(),
								RunInterval.TYPE_MIN_01.toSec() * 1000,
								null,
								variables,
								entry.getKey(),
								Type.STRING);
					}else{
						m_log.warn("monitor type Error " + cmdInfo.getMonitorTypeId());
					}
					
					
					dtos.add(dto);
	
					m_log.debug("getCommandExecuteDTO() CommandExecuteDTO is retured to agent. (requestedFacilityId = " + requestedFacilityId + ", dto = " + dto + ")");
				}
			}
		} catch (FacilityNotFound e) {
			m_log.info("getCommandExecuteDTO() configuration of custom is not valid. : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CustomInvalid("configuration of custom is not valid.", e);
		} catch (InvalidRole e) {
			m_log.info("getCommandExecuteDTO() configuration of custom is not valid. : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (HinemosUnknown e) {
			m_log.info("getCommandExecuteDTO() unexpected internal failure occurred. : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new HinemosUnknown("unexpected internal failure occurred.", e);
		} catch (Exception e) {
			m_log.warn("getCommandExecuteDTO() unexpected internal failure occurred. : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown("unexpected internal failure occurred.", e);
		}

		return dtos;
	}
}
