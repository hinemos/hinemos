/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.factory.AgentBroadcastAwakeTaskFactory.AgentBroadcastAwakeTask;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.factory.AgentMessageLoggerTaskFactory;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.factory.SearchNodeBySNMP;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.NodeConfigSettingControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.xcloud.bean.CloudConstant;

public class AgentConnectUtil {

	private static Log m_log = LogFactory.getLog( AgentConnectUtil.class );

	private static ILock _agentCacheLock;
	private static ILock _agentTopicCacheLock;
	private static ILock _agentAwakeRequestCacheLock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_agentCacheLock = lockManager.create(AgentConnectUtil.class.getName() + "-" + AbstractCacheManager.KEY_AGENT);
		_agentTopicCacheLock = lockManager.create(AgentConnectUtil.class.getName() + "-" + AbstractCacheManager.KEY_AGENT_TOPIC);
		_agentAwakeRequestCacheLock = lockManager.create(AgentConnectUtil.class.getName() + "-" + AbstractCacheManager.KEY_AGENT_AWAKE_REQUSET);
		
		init();
	}
	
	public static void init() {
		try {
			_agentCacheLock.writeLock();
			
			Map<String, AgentInfo> agentCache = getAgentCache();
			if (agentCache == null) {	// not null when clustered
				storeAgentCache(new HashMap<String, AgentInfo>());
			}
		} finally {
			_agentCacheLock.writeUnlock();
		}
		
		try {
			_agentTopicCacheLock.writeLock();
			
			Map<String, List<TopicInfo>> agentTopicCache = getAgentTopicCache();
			if (agentTopicCache == null) {	// not null when clustered
				storeAgentTopicCache(new HashMap<String, List<TopicInfo>>());
			}
		} finally {
			_agentTopicCacheLock.writeUnlock();
		}

		try {
			_agentAwakeRequestCacheLock.writeLock();
			
			LinkedHashSet<String> agentAwakeRequestCache = getAgentAwakeRequestCache();
			if (agentAwakeRequestCache == null) {	// not null when clustered
				storeAgentAwakeRequestCache(new LinkedHashSet<String>());
			}
		} finally {
			_agentAwakeRequestCacheLock.writeUnlock();
		}
	
	}
	
	// 接続しているエージェントのリスト
	@SuppressWarnings("unchecked")
	private static HashMap<String, AgentInfo> getAgentCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_AGENT);
		if (m_log.isTraceEnabled()) m_log.trace("get cache " + AbstractCacheManager.KEY_AGENT + " : " + cache);
		return cache == null ? null : (HashMap<String, AgentInfo>)cache;
	}
	
	private static void storeAgentCache(HashMap<String, AgentInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isTraceEnabled()) m_log.trace("store cache " + AbstractCacheManager.KEY_AGENT + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_AGENT, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, List<TopicInfo>> getAgentTopicCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_AGENT_TOPIC);
		if (m_log.isTraceEnabled()) m_log.trace("get cache " + AbstractCacheManager.KEY_AGENT_TOPIC + " : " + cache);
		return cache == null ? null : (HashMap<String, List<TopicInfo>>)cache;
	}
	
	private static void storeAgentTopicCache(HashMap<String, List<TopicInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isTraceEnabled()) m_log.trace("store cache " + AbstractCacheManager.KEY_AGENT_TOPIC + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_AGENT_TOPIC, newCache);
	}
	
	//awake依頼リスト
	@SuppressWarnings("unchecked")
	private static LinkedHashSet<String> getAgentAwakeRequestCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_AGENT_AWAKE_REQUSET);
		if (m_log.isTraceEnabled()) m_log.trace("get cache " + AbstractCacheManager.KEY_AGENT_AWAKE_REQUSET + " : " + cache);
		return cache == null ? null : (LinkedHashSet<String>)cache;
	}
	
	private static void storeAgentAwakeRequestCache(LinkedHashSet<String> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isTraceEnabled()) m_log.trace("store cache " + AbstractCacheManager.KEY_AGENT_AWAKE_REQUSET + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_AGENT_AWAKE_REQUSET, newCache);
	}

	/**
	 * [Topic] エージェントにTopicを送る場合はこのメソッドを利用する。
	 * リポジトリ機能やログファイル監視機能やコマンド監視機能などから呼ばれる。
	 */
	public static AgentInfo setTopic (String facilityId, TopicInfo info) {
		// 全ノードにTopicを送信
		// この場合、引数のfacilityIdはnullになる。
		if (facilityId == null) {
			for (String fid : getValidAgent()) {
				subSetTopic(fid, info);
				awakeAgent(fid);
			}
			return null;
		}

		// 特定のノードにTopicを送信
		// 存在しているノードか否か確認
		boolean flag = false;
		for (String fid : getValidAgent()) {
			if (fid.equals(facilityId)) {
				flag = true;
			}
		}
		if (flag) {
			// 対象のエージェントにTopicを送信してよいか判定する
			// 現時点ではジョブでの判定しか想定していないため特定ノードへのTopic送信時のみ判定する
			if (info.getSupportedAgentVersion() != null) {
				String agtVersion = null;
				try {
					_agentCacheLock.readLock();
					Map<String, AgentInfo> agentMap = getAgentCache();
					AgentInfo agtInfo = agentMap.get(facilityId);
					if (agtInfo != null) {
						agtVersion = agtInfo.getVersion();
					}
				} finally {
					_agentCacheLock.readUnlock();
				}
				if (!AgentVersionManager.checkVersion(agtVersion, info.getSupportedAgentVersion())) {
					m_log.info("setTopic : this Agent is not supported by this Topic. " + "Agent(facilityId="
							+ facilityId + ", version=" + agtVersion + "), " + "Topic([" + info.toString()
							+ "] supportedVersion=" + info.getSupportedAgentVersion() + ")");
					// 送信対象外の場合は後処理をしてTopicをセットせずに終了する
					AgentVersionManager.processAfterCheck(facilityId, info);
					return null;
				}
			}

			m_log.debug("setTopic : " + facilityId + ", " + info);
			subSetTopic(facilityId, info);
			awakeAgent(facilityId);
		} else {
			RunInstructionInfo runInfo = info.getRunInstructionInfo();
			String jobId = "";
			if (runInfo != null) {
				jobId = runInfo.getJobId();
			}
			m_log.info("setTopic(" + info.getFlag() + ", " + jobId + ") : " + facilityId + " is not valid");
		}
		
		try {
			_agentCacheLock.readLock();
			
			Map<String, AgentInfo> agentMap = getAgentCache();
			return agentMap.get(facilityId);
		} finally {
			_agentCacheLock.readUnlock();
		}
	}

	/**
	 * [Topic]
	 * @param facilityId
	 * @param info
	 */
	private static void subSetTopic(String facilityId, TopicInfo info) {
		List<TopicInfo> infoList = null;
		
		try {
			_agentTopicCacheLock.writeLock();
			
			HashMap<String, List<TopicInfo>> topicMap = getAgentTopicCache();
			infoList = topicMap.get(facilityId);
			if (infoList != null && infoList.contains(info)) {
				m_log.info("subSetTopic(): same topic. Maybe, agent timeout error occured. "
						+ "facilityId = " + facilityId);
				return;
			}
			
			if (infoList == null) {
				infoList = new ArrayList<TopicInfo>();
				topicMap.put(facilityId, infoList);
			}
			
			// Topicのリストに同一のものがある場合は、追加しない。
			if (infoList.contains(info)) {
				m_log.info("subSetTopic(): same topic. Maybe, agent timeout error occured. "
						+ "facilityId = " + facilityId);
			} else {
				infoList.add(info);
				if (infoList.size() > 10) {
					m_log.info("subSetTopic(): topicList is too large : size=" +
							infoList.size() + ", facilityId = " + facilityId);
				}
			}
			
			storeAgentTopicCache(topicMap);
		} finally {
			_agentTopicCacheLock.writeUnlock();
		}
		
		printRunInstructionInfo(facilityId, infoList);
	}

	/**
	 * 現在のTopicのリストから、エージェントアップデート指示を除去します。
	 * 
	 * @param facilityId 対象ノードのファシリティID。
	 */
	public static void removeAgentUpdateTopic(String facilityId) {
		try {
			_agentTopicCacheLock.writeLock();
			HashMap<String, List<TopicInfo>> topicMap = getAgentTopicCache();
			List<TopicInfo> topics = topicMap.get(facilityId);
			if (topics != null) {
				topics.removeIf(topic -> {
					if (topic.getAgentCommand() == AgentCommandConstant.UPDATE) {
						m_log.info("removeAgentUpdateTopic: removed. topic=" + topic);
						return true;
					}
					return false;
				});
				// HinemosのCacheManagerの仕様として、返された値(ここではmap)のステート変更が
				// キャッシュ側の値へ反映されることは保証されないため、新たな値としてstoreし直す必要がある。
				storeAgentTopicCache(topicMap);
			}
		} finally {
			_agentTopicCacheLock.writeUnlock();
		}
	}
	
	/**
	 * [Topic]
	 * ファシリティIDをキーにして、トピックのリストを取得
	 * @param facilityId
	 * @return
	 */
	public static ArrayList<TopicInfo> getTopic (String facilityId) {
		ArrayList<TopicInfo> ret = null;
		m_log.debug("getJobOrder : " + facilityId);
		
		try {
			_agentTopicCacheLock.writeLock();
			
			HashMap<String, List<TopicInfo>> topicMap = getAgentTopicCache();
			List<TopicInfo> tmpInfoList = topicMap.get(facilityId);
			
			if (tmpInfoList != null) {
				ret = new ArrayList<TopicInfo>();
				for (TopicInfo topicInfo : tmpInfoList) {
					if (topicInfo.isValid()) {
						ret.add(topicInfo);
					} else {
						m_log.info("topic is too old : " + topicInfo.toString());
					}
				}
				
				topicMap.put(facilityId, new ArrayList<TopicInfo>());
				
				storeAgentTopicCache(topicMap);
			}
		} finally {
			_agentTopicCacheLock.writeUnlock();
		}
		
		return ret;
	}

	/**
	 * [Topic] getTopicを実行するという命令を、UDPパケットで送信する。
	 * @param facilityId
	 */
	private static void awakeAgent(String facilityId) {
		String ipAddress = "";
		Integer port = 24005;
		m_log.debug("awakeAgent facilityId=" + facilityId);
		try {
			NodeInfo info = NodeProperty.getProperty(facilityId);
			ipAddress = info.getAvailableIpAddress();
			port = info.getAgentAwakePort();
		} catch (FacilityNotFound e) {
			m_log.debug(e.getMessage(), e);
			return;
		} catch (Exception e) {
			m_log.warn("awakeAgent facilityId=" + facilityId + " " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			return;
		}
		if (port < 1 || 65535 < port) {
			m_log.info("awakeAgent : invalid port " + port + "(" + facilityId + ")");
		}
		m_log.debug("awakeAgent ipaddress=" + ipAddress);

		final int BUFSIZE = 1;
		byte[] buf = new byte[BUFSIZE];
		buf[0] = 1;
		InetAddress sAddr;
		try {
			sAddr = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			m_log.warn("awakeAgent facilityId=" + facilityId + " " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			return;
		}
		DatagramPacket sendPacket = new DatagramPacket(buf, BUFSIZE, sAddr, port);
		DatagramSocket soc = null;
		try {
			soc = new DatagramSocket();
			soc.send(sendPacket);
		} catch (SocketException e) {
			m_log.warn("awakeAgent facilityId=" + facilityId + " " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} catch (IOException e) {
			m_log.warn("awakeAgent facilityId=" + facilityId + " " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			if (soc != null) {
				soc.close();
			}
		}
	}

	private static void printRunInstructionInfo (String facilityId, List<TopicInfo> topicInfoList) {
		if (!m_log.isDebugEnabled()) {
			return;
		}
		if (topicInfoList == null) {
			m_log.debug("printRunInstructionInfo JobOrder : " + facilityId + ", infoList is null");
			return;
		}
		StringBuilder str = new StringBuilder();
		for (TopicInfo topicInfo : topicInfoList) {
			if (topicInfo.getRunInstructionInfo() != null) {
				str.append(topicInfo.getRunInstructionInfo().getJobId()).append(", ")
						.append(topicInfo.getRunInstructionInfo().getSessionId()).append(", ");
			}
		}
		m_log.debug("printRunInstructionInfo JobOrder : " + facilityId + ", " + str);
	}


	/**
	 * エージェント一覧に追加。
	 * このメソッドを利用する場合は、agentInfoのfacilityIdに値をつめること。
	 */
	public static void putAgentMap (AgentInfo agentInfo) {
		String facilityId = agentInfo.getFacilityId();
		if (facilityId == null) {
			m_log.info("putAgentMap facilityId=null");
			return;
		}
		agentInfo.refreshLastLogin();
		
		AgentInfo cacheInfo = null;
		try {
			_agentCacheLock.readLock();
			
			Map<String, AgentInfo> agentMap = getAgentCache();
			cacheInfo = agentMap.get(facilityId);
		} finally {
			_agentCacheLock.readUnlock();
		}
		
		if (cacheInfo == null) {
			try {
				_agentCacheLock.writeLock();
				
				m_log.info("new agent appeared : " + agentInfo);
				if (!facilityId.equals(agentInfo.getFacilityId())){
					m_log.info("facilityId=" + facilityId + ", f2=" + agentInfo.getFacilityId());
				}
				
				HashMap<String, AgentInfo> agentMap = getAgentCache();
				agentMap.put(facilityId, agentInfo.clone());
				storeAgentCache(agentMap);
				
				return;
			} finally {
				_agentCacheLock.writeUnlock();
				
				// 再起動後のエージェントの可能性があるので、
				// 実行中のノード情報を異常停止にする
				new JobSessionNodeImpl().endNodeByAgent(facilityId, agentInfo, false);
			}
		}

		// マップに過去の自分が存在している場合は、lastLoginを更新。
		if (cacheInfo.getStartupTime() == agentInfo.getStartupTime()) {
			m_log.debug("refresh " + agentInfo);
			cacheInfo.refreshLastLogin();
			
			try {
				_agentCacheLock.writeLock();
				
				HashMap<String, AgentInfo> agentMap = getAgentCache();
				agentMap.put(facilityId, cacheInfo);
				storeAgentCache(agentMap);
			} finally {
				_agentCacheLock.writeUnlock();
			}
		} else {
			/*
			 * 「同一ファシリティIDのエージェントが存在します。」
			 * というメッセージを出力させる。
			 */
			if (cacheInfo.getInstanceId().equals(agentInfo.getInstanceId())) {
				m_log.warn("agents are duplicate : " + facilityId + "[" +
					cacheInfo.toString() + "->" + agentInfo.toString() + "]");
			} else {
				m_log.debug("agents are duplicate : " + facilityId + "[" +
					cacheInfo.toString() + "->" + agentInfo.toString() + "]");
			}
			
			try {
				_agentCacheLock.writeLock();
				
				HashMap<String, AgentInfo> agentMap = getAgentCache();
				agentMap.put(facilityId, agentInfo.clone());
				storeAgentCache(agentMap);
			} finally {
				_agentCacheLock.writeUnlock();
			}
			
			// 再起動後のエージェントの可能性があるので、
			// 実行中のノード情報を異常停止にする
			new JobSessionNodeImpl().endNodeByAgent(facilityId, agentInfo, false);
		}
	}

	/**
	 * 有効なエージェント一覧を取得する。
	 */
	public static ArrayList<String> getValidAgent() {
		try {
			_agentCacheLock.writeLock();
			
			HashMap<String, AgentInfo> agentMap = getAgentCache();
			Set<AgentInfo> removedSet = new HashSet<AgentInfo>();
			for (Entry<String, AgentInfo> entry : new HashSet<Entry<String, AgentInfo>>(agentMap.entrySet())) {
				if (! entry.getValue().isValid()) {
					removedSet.add(agentMap.remove(entry.getKey()));
					m_log.info("remove " + entry.getValue());
				}
			}
			if (removedSet.size() > 0) {
				storeAgentCache(agentMap);
			}
		} finally {
			_agentCacheLock.writeUnlock();
		}
		
		try {
			_agentCacheLock.readLock();
			
			Map<String, AgentInfo> agentMap = getAgentCache();
			
			ArrayList<String> list = new ArrayList<String>();
			for (String fid : agentMap.keySet()) {
				list.add(fid);
			}
			
			return list;
		} finally {
			_agentCacheLock.readUnlock();
		}
	}

	/**
	 * エージェントが有効かチェック
	 */
	public static boolean isValidAgent(String facilityId){
		boolean valid = false;
		
		try {
			_agentCacheLock.readLock();
			
			Map<String, AgentInfo> agentMap = getAgentCache();
			
			AgentInfo agentInfo = agentMap.get(facilityId);
			if (agentInfo != null) {
				valid = agentInfo.isValid();
			}
		} finally {
			_agentCacheLock.readUnlock();
		}
		
		if (! valid) {
			try {
				_agentCacheLock.writeLock();
				
				HashMap<String, AgentInfo> agentMap = getAgentCache();
				agentMap.remove(facilityId);
				storeAgentCache(agentMap);
			} finally {
				_agentCacheLock.writeUnlock();
			}
		}
		
		return valid;
	}

	/**
	 * AgentInfo#toStringを取得する。
	 */
	public static String getAgentString(String facilityId) {
		try {
			_agentCacheLock.readLock();
			
			Map<String, AgentInfo> agentMap = getAgentCache();
			AgentInfo agentInfo = agentMap.get(facilityId);
			if (agentInfo == null) {
				return null;
			}
			return agentInfo.toString();
		} finally {
			_agentCacheLock.readUnlock();
		}
	}
	
	public static AgentInfo getAgentInfo(String facilityId) {
		try {
			_agentCacheLock.readLock();
			
			Map<String, AgentInfo> agentMap = getAgentCache();
			return agentMap.get(facilityId);
		} finally {
			_agentCacheLock.readUnlock();
		}
	}

	public static ArrayList<AgentInfo> getAgentList() {
		ArrayList<String> facilityIdList = getValidAgent();
		
		try {
			_agentCacheLock.readLock();
			
			Map<String, AgentInfo> agentMap = getAgentCache();
			
			ArrayList<AgentInfo> ret = new ArrayList<AgentInfo>();
			for (String facilityId : facilityIdList) {
				ret.add(agentMap.get(facilityId));
			}
			return ret;
		} finally {
			_agentCacheLock.readUnlock();
		}
	}

	// このメソッドが呼ばれる契機
	// ・エージェント停止時、ノード削除時
	public static void deleteAgent(String facilityId, AgentInfo agentInfo) {
		try {
			_agentCacheLock.writeLock();
			
			HashMap<String, AgentInfo> agentMap = getAgentCache();
			agentMap.remove(facilityId);
			storeAgentCache(agentMap);
		} finally {
			_agentCacheLock.writeUnlock();
		}

		//実行中のノード情報を異常停止にする
		JobSessionNodeImpl nodeImple = new JobSessionNodeImpl();
		nodeImple.endNodeByAgent(facilityId, agentInfo, true);
	}

	//////////////////////////////////
	// インターナルメッセージ
	//////////////////////////////////
	public static void sendMessageLocal(OutputBasicInfo outputBasicInfo,
			ArrayList<String> facilityIdList)
					throws HinemosUnknown, FacilityNotFound {
		// 監視情報を取得
		ArrayList<String> facilityList = null;
		if (HinemosModuleConstant.NODE_CONFIG_SETTING.equals(outputBasicInfo.getPluginId())){
			try {
				NodeConfigSettingInfo entity = new NodeConfigSettingControllerBean().getNodeConfigSettingInfo(outputBasicInfo.getMonitorId());
				String nodeConfigFacilityId = entity.getFacilityId();
				facilityList = FacilitySelector.getFacilityIdList(nodeConfigFacilityId, entity.getOwnerRoleId(), 0, false, false);
			} catch (NodeConfigSettingNotFound | InvalidRole e) {
				m_log.warn(e.getMessage() + " (" + outputBasicInfo.getMonitorId() + ")", e);
			}
		} else if (HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(outputBasicInfo.getPluginId())){
			try {
				String monitorFacilityId="";
				MonitorInfo entity = QueryUtil.getMonitorInfoPK_NONE(outputBasicInfo.getMonitorId());
				for (MonitorPluginStringInfo sInfo: entity.getPluginCheckInfo().getMonitorPluginStringInfoList()){
					if(sInfo.getKey().equals(CloudConstant.cloudLog_targetScopeFacilityId)){
						monitorFacilityId=sInfo.getValue();
						break;
					}
				}
				facilityList = FacilitySelector.getFacilityIdList(monitorFacilityId, entity.getOwnerRoleId(), 0, false, false);
			} catch (MonitorNotFound e) {
				m_log.warn(e.getMessage() + " (" + outputBasicInfo.getMonitorId() + ")", e);
			}
			
		} else if (!"SYS".equals(outputBasicInfo.getMonitorId())) {
			try {
				MonitorInfo entity = QueryUtil.getMonitorInfoPK_NONE(outputBasicInfo.getMonitorId());
				String monitorFacilityId = entity.getFacilityId();
				facilityList = FacilitySelector.getFacilityIdList(monitorFacilityId, entity.getOwnerRoleId(), 0, false, false);
			} catch (MonitorNotFound e) {
				m_log.warn(e.getMessage() + " (" + outputBasicInfo.getMonitorId() + ")", e);
			}
		} else{
			m_log.trace("MonitorId is SYS, so facilityList is null.");
		}
		for (String facilityId : facilityIdList) {
			if (facilityList != null && !facilityList.contains(facilityId)) {
				m_log.debug("not match facilityId(" + facilityId + ")");
				continue;
			}
			String scopeText = "";
			try {
				NodeInfo nodeInfo = new RepositoryControllerBean().getNode(facilityId);
				scopeText = nodeInfo.getFacilityName();
			} catch (FacilityNotFound e) {
				throw e;
			}
			// 通知出力情報をディープコピーする（AsyncWorkerPlugin.addTaskのため）
			OutputBasicInfo clonedInfo = outputBasicInfo.clone();
			clonedInfo.setFacilityId(facilityId);
			clonedInfo.setScopeText(scopeText);
			AsyncWorkerPlugin.addTask(AgentMessageLoggerTaskFactory.class.getSimpleName(), clonedInfo, false);
		}
	}

	/**
	 * common.agent.discovery.pingport で記載されたポートに対しマネージャのIPを送信する。
	 *  エージェントとTCPセッションが確立できた場合、trueを返す。
	 *  ポート番号が無効な場合は何も実施せずにfalseを返す。
	 *
	 * @param facilityId
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static boolean sendManagerDiscoveryInfo(String facilityId)
			throws UnknownHostException, IOException {
		String managerIpAddr = "";
		boolean successFlag = true;
		String agentIpAddr = "";
		int pingPort;

		// 接続先ポートの決定（プロパティファイルから読み込み、正しくないポートレンジの場合には何も実施せず終了する
		pingPort = HinemosPropertyCommon.common_agent_discovery_pingport.getIntegerValue();
		if (pingPort < 1 || pingPort > 65535) {
			return false;
		}
		// エージェントが接続するためのマネージャのIPを調べる
		try {
			// DNS名接続の場合(agent.connection.dnsnameに文字列が指定) Hinemos ver 4.0.2以降対応
			managerIpAddr = HinemosPropertyCommon.agent_connection_dnsname.getStringValue();
			
			// IPアドレス接続の場合(agent.connection.dnsnameが未指定)
			if(managerIpAddr == null || "".equals(managerIpAddr)){
				managerIpAddr = HinemosPropertyCommon.agent_connection_ipaddres.getAgentConnectionIpaddres();
			}
		} catch (UnknownHostException e) {
			throw e;
		}

		Socket socket = null;
		InputStream is = null;
		try {
			String sendDataStr = "managerIp=" + managerIpAddr + ",agentFacilityId=" + facilityId;
			byte[] data = sendDataStr.getBytes();
			byte[] msg = new byte[data.length];
			agentIpAddr = NodeProperty.getProperty(facilityId)
					.getAvailableIpAddress();
			
			m_log.info("trying to establish connection to hinemos agent server at "
					+ agentIpAddr + ":" + pingPort);
			
			socket = new Socket(agentIpAddr, pingPort);

			m_log.info("established the connection to the hinemos agent server at "
					+ agentIpAddr);

			is = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			out.write(data);

			m_log.info("sent the message： " + new String(data));

			// エージェントからの返信を受信
			int totalBytesRcvd = 0;
			int bytesRcvd;

			while (totalBytesRcvd < data.length) {
				if ((bytesRcvd = is.read(msg, totalBytesRcvd, data.length
						- totalBytesRcvd)) == -1) {
					continue;
				}
				totalBytesRcvd += bytesRcvd;
			}
			m_log.info("received the message: " + new String(msg));
		} catch (Exception e) {
			successFlag = false;
			m_log.warn("facilityId: " + facilityId + ", " + e.getMessage());
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
		
		
		return successFlag;
	}

	/**
	 * setTopicをaweke流量制限を行いつつ実施する。
	 * 
	 * @param targetList 対象ノードのファシリティID一覧
	 * @param info 設定するトピック
	 */
	private static void setTopicForFlowControl(List<String> targetList, TopicInfo info){

		//引数が不正なら処理打ち切り
		if(targetList == null || info == null ){
			return;
		}

		m_log.debug("setTopicForFlowControl(): start.");

		// TopicInfoを対象ノードに設定
		for( String setId: targetList ){
			subSetTopic(setId,info);
		}
		
		//aweke(流量制限付き)のリクエストをキューに追加
		try{
			_agentAwakeRequestCacheLock.writeLock();//リクエストキュー編集開始
			LinkedHashSet<String> queue = getAgentAwakeRequestCache();
			for( String addId: targetList ){
				queue.add(addId);
			}
			storeAgentAwakeRequestCache(queue);
		}finally{
			_agentAwakeRequestCacheLock.writeUnlock();//リクエストキュー編集終了
		}

		// 流量制限による待ちが呼出元に波及しないよう、AsyncTaskにawakeさせる。
		// 複数同時実行だと流量制御が難しいので、実行中のタスクがあれば単一実行になるよう制御。
		if( !( isRunningAgentBroadcastAwakeTask() ) ){
			// postCommitからの呼び出し時、AsyncWorkerPlugin.addTask内のcallback中のpostCommit部分が動作しないので 別スレッド化
			Thread taskAddThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						m_log.debug("setTopicForFlowControl(): addTask AgentBroadcatAwakeTask.");
						AsyncWorkerPlugin.addTask(AsyncWorkerPlugin.AGENT_BROADCAST_AWAKE_TASK_FACTORY,  HinemosTime.currentTimeMillis(), false);
					} catch (Exception e) {
						m_log.warn("setTopicForFlowControl(): Failed to create a broadcast awake task." , e);
					}
				}
			}, "addTask-AgentBroadcatAwakeTask");
			taskAddThread.start();
			try {
				taskAddThread.join();
			} catch (InterruptedException e) {
				m_log.error("setTopicForFlowControl() : " + taskAddThread.getName()
						+ " was interrupted. messages : " + e.getMessage(), e);
			}
		}
		m_log.debug("setTopicForFlowControl(): end");
	}

	/**
	 * 
	 * AgentBroadcastAwakeTaskの実行中チェック
	 * 
	 */
	private static boolean isRunningAgentBroadcastAwakeTask(){

		boolean isRunning = false;
		boolean isTaskExist = false;

		try {
			_agentAwakeRequestCacheLock.writeLock();//リクエストキュー読込開始(読込み中の編集を避けるため、ロックを取得)

			//依頼数を確認
			LinkedHashSet<String> queue = getAgentAwakeRequestCache();
			int queueSize=queue.size();

			//ワーカー向けのキューにタスクがないかを確認
			Iterator<Runnable> it = AsyncWorkerPlugin.getTaskIterator(AsyncWorkerPlugin.AGENT_BROADCAST_AWAKE_TASK_FACTORY);
			while (it.hasNext()) {
				Runnable runnable = it.next();
				if (runnable instanceof AgentBroadcastAwakeTask) {
					isTaskExist =true;
					break;
				}
			}
			
			//依頼が残って タスクが存在するなら 実行中とみなす
			if( queueSize > 0 && isTaskExist ){
				isRunning = true;
			}

		} catch (HinemosUnknown e) {
			m_log.warn("isRunningAgentBroadcastAwakeTask(): HinemosUnknown .", e);
		}finally{
			_agentAwakeRequestCacheLock.writeUnlock();//リクエストキュー読込終了(ロック解除)
		}
			
		return isRunning;
	}
	
	/**
	 * リクエストキューで指定されたノードにawakeを送信(AgentBroadcastAwakeTaskから呼出される)
	 * 
	 * awakeの流量を制限するため、以下を実施する
	 * ・指定数連続で送信した場合 インターバルを置いて次を送信する。
	 * ・インターバル中に追加のリクエストを受け付ける
	 */
	public static void execAwakeForFlowControl(){
		m_log.debug("execAwakeForFlowControl(): start .") ;

		final int interval= HinemosPropertyCommon.agent_topic_setting_broadcast_awake_interval.getIntegerValue();
		final int amount = HinemosPropertyCommon.agent_topic_setting_broadcast_awake_amount.getIntegerValue();

		boolean isQueueNotEmpty = true;

		//キューが0件になるまでインターバルを置きつつ繰り返す
		while(isQueueNotEmpty){

			List<String> doneIdList = new ArrayList<String>();
			int awakeCount=0;
			int queueSize=0;
			
			try {
				_agentAwakeRequestCacheLock.writeLock();//リクエストキュー編集開始
	
				//キューからリクエストを取得してawakeを送信
				LinkedHashSet<String> queue = getAgentAwakeRequestCache();
				for( String target: queue ){
	
					awakeAgent(target);
					doneIdList.add(target);
	
					//既定数送信したら一旦処理を中断
					awakeCount++;
					if(awakeCount >= amount){
						break;
					}
	
				}
				
				//処理済みレコードをリクエストキューから削除
				for( String removeId: doneIdList ){
					queue.remove(removeId);
				}
				queueSize=queue.size();
				storeAgentAwakeRequestCache(queue);

			}finally{
				_agentAwakeRequestCacheLock.writeUnlock();//リクエストキュー編集終了
			}
				
			//キューが残っているならインターバルを置いてから再度実施
			//（インターバル中に追加リクエストを受付できるようキューのロックは解除）
			if(queueSize <= 0 ){
				isQueueNotEmpty=false;
			}else{
				try{
					if(m_log.isDebugEnabled()){
						m_log.debug("execAwakeForFlowControl(): sleep interval. queueSize=" + queueSize+ " ,awake interval=" +interval +" , awake amount="+amount) ;
					}
					Thread.sleep(interval);
				}catch( Exception e ){
					m_log.warn("execAwakeForFlowControl(): Exception occur in sleep .",e);
					break;
				}
			}
		}
		m_log.debug("execAwakeForFlowControl(): end .") ;
	}
	/**
	 * [Topic] エージェントに設定変更Topicをbroadcastする場合はこのメソッドを利用する。
	 * リポジトリ機能やログファイル監視機能やコマンド監視機能などから呼ばれる。
	 * awake送信の流量制限に対応している。
	 */
	public static void broadcastTopicFlowControl (TopicInfo info) {
		// 全ノードにTopicを送信
		if (info != null) {
			//awake流量制限向けの間隔指定がある場合は、流量制限付きの処理を実施する。
			if(HinemosPropertyCommon.agent_topic_setting_broadcast_awake_interval.getIntegerValue() > 0 ){
				setTopicForFlowControl(getValidAgent(),info);
			}else{
				setTopic(null,info);
			}
			return;
		}
		
	}

	/**
	 * AgentInfo から紐づくノードを解決して、そのファシリティIDのリストを取得します。
	 */
	public static ArrayList<String> getFacilityIds(AgentInfo agentInfo) throws HinemosUnknown {
		ArrayList<String> facilityIdList = new ArrayList<String>();

		if (agentInfo.getFacilityId() != null && !agentInfo.getFacilityId().equals("")) {
			/*
			 * agentInfoにfacilityIdが入っている場合。
			 */
			// 複数facilityId対応。
			// agentInfoの内容をカンマ(,)で分割する。
			StringTokenizer st = new StringTokenizer(agentInfo.getFacilityId(), ",");
			while (st.hasMoreTokens()) {
				String facilityId = st.nextToken();
				try {
					new RepositoryControllerBean().getNode(facilityId);
				} catch (FacilityNotFound e) {
					m_log.debug("node not found. facilityId=" + facilityId);
					continue;
				}
				facilityIdList.add(facilityId);
				m_log.debug("add facilityId=" + facilityId);
			}

		} else {
			/*
			 * agentInfoにfacilityIdが入っていない場合。
			 * この場合は、ホスト名とIPアドレスからファシリティIDを決める。
			 */
			try {
				for (String ipAddress : agentInfo.getIpAddress()) {
					String hostname = agentInfo.getHostname();
					hostname = SearchNodeBySNMP.getShortName(hostname);
					ArrayList<String> list = new RepositoryControllerBean().getFacilityIdList(hostname, ipAddress);
					if (list != null && list.size() != 0) {
						for (String facilityId : list) {
							m_log.debug("facilityId=" + facilityId + ", " + agentInfo.toString());
						}
						facilityIdList.addAll(list);
					}
				}
			} catch (Exception e) {
				m_log.warn(e, e);
				throw new HinemosUnknown("getFacilityId " + e.getMessage());
			}
		}
		return facilityIdList;
	}

    /**
     * AgentInfo から紐づくノードを解決して、そのファシリティIDのリストを取得します。
     * 紐づくノードが存在しない場合は {@link FacilityNotFound} を投げます。
     */
    public static ArrayList<String> getEffectiveFacilityIds(AgentInfo agentInfo) throws HinemosUnknown, FacilityNotFound {
        ArrayList<String> r = getFacilityIds(agentInfo);
        if (r.isEmpty()) {
            m_log.warn("getEffectiveFacilityIds: Could not resolve the agent into any node."
                    + " hostname=" + agentInfo.getHostname()
                    + ", IP address=" + agentInfo.getIpAddress());
            throw new FacilityNotFound("Could not resolve the agent into any node.");
        }
        return r;
    }

    /**
     * エージェントのバージョンを取得します。
     */
	public static String getAgentVersion(String facilityId){
		String agtVersion = null;
		try {
			_agentCacheLock.readLock();
			Map<String, AgentInfo> agentMap = getAgentCache();
			AgentInfo agtInfo = agentMap.get(facilityId);
			if (agtInfo != null) {
				agtVersion = agtInfo.getVersion();
			}
		} finally {
			_agentCacheLock.readUnlock();
		}
		return agtVersion;
	}
}
