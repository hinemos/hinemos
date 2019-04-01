/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.factory.AgentRestartTaskFactory.AgentRestartTask;
import com.clustercontrol.hinemosagent.factory.AgentUpdateTaskFactory.AgentUpdateTask;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.hinemosagent.util.AgentLibraryManager;
import com.clustercontrol.hinemosagent.util.AgentProfile;
import com.clustercontrol.hinemosagent.util.AgentProfiles;
import com.clustercontrol.hinemosagent.util.AgentUpdateList;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.repository.bean.AgentStatusInfo;
import com.clustercontrol.repository.bean.AgentUpdateStatus;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.util.Singletons;

/**
 * クライアント表示用のエージェント一覧情報を集計します。
 */
public class AgentStatusCollector {
	
	private static final Log log = LogFactory.getLog(AgentStatusCollector.class);

	// 外部依存動作をモックへ置換できるように分離
	private External external;
	static class External {
		List<String> getOwnerRoles() throws HinemosUnknown {
			return new AccessControllerBean().getOwnerRoleIdList();
		}
		
		List<AgentInfo> getAgentInfos() {
			return AgentConnectUtil.getAgentList();
		}
		
		NodeInfo getNodeInfoMin(String facilityId) throws FacilityNotFound {
			return NodeProperty.getProperty(facilityId);
		}
		
		int getJobRunning(String facilityId) {
			return JobMultiplicityCache.getRunningMultiplicity(facilityId);
		}
		
		int getJobWaiting(String facilityId) {
			return JobMultiplicityCache.getWaitMultiplicity(facilityId);
		}
		
		Iterator<Runnable> getTaskIterator(String worker) throws HinemosUnknown {
			return AsyncWorkerPlugin.getTaskIterator(worker);
		}
	}

	public AgentStatusCollector() {
		this(new External());
	}
	
	AgentStatusCollector(External external) {
		this.external = external;
	}
	
	/**
	 * クライアント表示用のエージェント一覧情報を返します。
	 */
	public List<AgentStatusInfo> getAgentStatusList() throws HinemosUnknown {
		List<AgentStatusInfo> ret = new ArrayList<AgentStatusInfo>();
		List<String> ownerRoles = external.getOwnerRoles();
		Set<String> updatingNodes = listUpdatingNodes();
		Set<String> restartingNodes = listRestartingNodes();
		AgentProfiles agentProfiles = Singletons.get(AgentProfiles.class);
		AgentLibraryManager agentLibMgr = Singletons.get(AgentLibraryManager.class);
		AgentUpdateList agentUpdateList = Singletons.get(AgentUpdateList.class);
		
		// 最新判定のため、ライブラリ情報を更新する
		agentLibMgr.refresh();
		// 更新中エージェントリストに期限切れノードがある場合は解放する
		agentUpdateList.releaseExpired();

		for (AgentInfo agentInfo : external.getAgentInfos()) {
			// ファシリティID (取得できない場合は除外)
			String facilityId = agentInfo.getFacilityId();
			if (facilityId == null || facilityId.isEmpty()) {
				log.info(String.format("getAgentStatusList: FacilityId not set, hostname=%s, IPaddrs=%s",
						agentInfo.getHostname(), String.join(",", agentInfo.getIpAddress())));
				continue;
			}

			// ノード情報  (取得できない場合は除外)
			NodeInfo nodeInfoMin;
			try {
				nodeInfoMin = external.getNodeInfoMin(facilityId);
			} catch (FacilityNotFound e) {
				log.info("getAgentStatusList: NodeInfo not found, facilityId=" + facilityId);
				continue;
			}

			// オーナーロールチェック
			if (!ownerRoles.contains(nodeInfoMin.getOwnerRoleId())) {
				log.debug("getAgentStatusList: OwnerRole not satisfied, facilityId=" + facilityId);
				continue;
			}

			// 情報の詰め込み
			AgentStatusInfo agent = new AgentStatusInfo();
			agent.setFacilityId(facilityId);
			agent.setFacilityName(nodeInfoMin.getFacilityName());
			agent.setStartupTime(agentInfo.getStartupTime());
			agent.setLastLogin(agentInfo.getLastLogin());

			// ジョブ多重度
			agent.setMultiplicity(
					"run=" + external.getJobRunning(facilityId) + ",wait=" + external.getJobWaiting(facilityId));

			// アップデート状況
			if (agentUpdateList.isUpdating(facilityId) || updatingNodes.contains(facilityId)) {
				agent.setUpdateStatus(AgentUpdateStatus.UPDATING);
			} else if (restartingNodes.contains(facilityId)) {
				agent.setUpdateStatus(AgentUpdateStatus.RESTARTING);
			} else {
				AgentProfile prof = agentProfiles.getProfile(facilityId);
				if (prof == null) {
					agent.setUpdateStatus(AgentUpdateStatus.UNKNOWN);
				} else if (agentLibMgr.isLatest(prof)) {
					agent.setUpdateStatus(AgentUpdateStatus.DONE);
				} else if (prof.isV61Earlier()) {
					agent.setUpdateStatus(AgentUpdateStatus.UNSUPPORTED);
				} else {
					agent.setUpdateStatus(AgentUpdateStatus.NOT_YET);
				}
			}

			ret.add(agent);
		}
		return ret;
	}

	// 更新キューにあるノードのファシリティIDを取得する。
	private Set<String> listUpdatingNodes() {
		Set<String> ret = new HashSet<>();
		try {
			Iterator<Runnable> it = external.getTaskIterator(AsyncWorkerPlugin.AGENT_UPDATE_TASK_FACTORY);;
			while (it.hasNext()) {
				Runnable runnable = it.next();
				if (runnable instanceof AgentUpdateTask) {
					AgentUpdateTask task = (AgentUpdateTask) runnable;
					ret.add(task.getParameter().getFacilityId());
				} else {
					log.info("listUpdatingNodes: Unknown class=" + runnable.getClass());
				}
			}
		} catch (HinemosUnknown e) {
			log.warn("listUpdatingNodes: Failed to list nodes.", e);
		}
		log.info("listUpdatingNodes: Found " + ret.size() + " nodes.");
		return ret;
	}
	
	// 再起動キューにあるノードのファシリティIDを取得する。
	private Set<String> listRestartingNodes() {
		Set<String> ret = new HashSet<>();
		try {
			Iterator<Runnable> it = external.getTaskIterator(AsyncWorkerPlugin.AGENT_RESTART_TASK_FACTORY);
			while (it.hasNext()) {
				Runnable runnable = it.next();
				if (runnable instanceof AgentRestartTask) {
					AgentRestartTask task = (AgentRestartTask) runnable;
					ret.add(task.getParameter().getFacilityId());
				} else {
					log.info("listRestartingNodes: Unknown class=" + runnable.getClass());
				}
			}
		} catch (HinemosUnknown e) {
			log.warn("listRestartingNodes: Failed to list nodes.", e);
		}
		log.info("listRestartingNodes: Found " + ret.size() + " nodes.");
		return ret;
	}
	
}
