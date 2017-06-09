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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.factory.NotifyEventTaskFactory;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

public class AgentConnectUtil {

	private static Log m_log = LogFactory.getLog( AgentConnectUtil.class );

	private static ILock _agentCacheLock;
	private static ILock _agentLibMd5CacheLock;
	private static ILock _agentTopicCacheLock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_agentCacheLock = lockManager.create(AgentConnectUtil.class.getName() + "-" + AbstractCacheManager.KEY_AGENT);
		_agentLibMd5CacheLock = lockManager.create(AgentConnectUtil.class.getName() + "-" + AbstractCacheManager.KEY_AGENT_LIBMD5);
		_agentTopicCacheLock = lockManager.create(AgentConnectUtil.class.getName() + "-" + AbstractCacheManager.KEY_AGENT_TOPIC);
		
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
			_agentLibMd5CacheLock.writeLock();
			
			Map<String, Map<String, String>> agentLibMd5Cache = getAgentLibMd5Cache();
			if (agentLibMd5Cache == null) {	// not null when clustered
				storeAgentLibMd5Cache(new HashMap<String, Map<String, String>>());
			}
		} finally {
			_agentLibMd5CacheLock.writeUnlock();
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
	}
	
	// 接続しているエージェントのリスト
	@SuppressWarnings("unchecked")
	private static HashMap<String, AgentInfo> getAgentCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_AGENT);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_AGENT + " : " + cache);
		return cache == null ? null : (HashMap<String, AgentInfo>)cache;
	}
	
	private static void storeAgentCache(HashMap<String, AgentInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_AGENT + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_AGENT, newCache);
	}
	
	// 接続しているエージェントのライブラリのMD5
	// HashMap<facilityId, HashMap<filename, md5>>
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, String>> getAgentLibMd5Cache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_AGENT_LIBMD5);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_AGENT_LIBMD5 + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, String>>)cache;
	}
	
	private static void storeAgentLibMd5Cache(HashMap<String, Map<String, String>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_AGENT_LIBMD5 + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_AGENT_LIBMD5, newCache);
	}

	/**
	 * TODO
	 * このHashMapはejbを利用してテーブルに置き、session beanでアクセスする事。
	 * (src_agentに移動すること。)
	 * そうしないと、HashMapに入っていてエージェントに送る前にシャットダウンすると、
	 * データが損失してしまう。
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, List<TopicInfo>> getAgentTopicCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_AGENT_TOPIC);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_AGENT_TOPIC + " : " + cache);
		return cache == null ? null : (HashMap<String, List<TopicInfo>>)cache;
	}
	
	private static void storeAgentTopicCache(HashMap<String, List<TopicInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_AGENT_TOPIC + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_AGENT_TOPIC, newCache);
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
	// ・エージェント停止時
	public static void deleteAgent(String facilityId, AgentInfo agentInfo) {
		try {
			_agentCacheLock.writeLock();
			
			HashMap<String, AgentInfo> agentMap = getAgentCache();
			agentMap.remove(facilityId);
			storeAgentCache(agentMap);
		} finally {
			_agentCacheLock.writeUnlock();
		}
		
		try {
			_agentLibMd5CacheLock.writeLock();
			
			HashMap<String, Map<String, String>> agentLibMd5 = getAgentLibMd5Cache();
			agentLibMd5.remove(facilityId);
			storeAgentLibMd5Cache(agentLibMd5);
		} finally {
			_agentLibMd5CacheLock.writeUnlock();
		}
		
		//実行中のノード情報を異常停止にする
		JobSessionNodeImpl nodeImple = new JobSessionNodeImpl();
		nodeImple.endNodeByAgent(facilityId, agentInfo, true);
	}



	//////////////////////////////////
	// エージェント　リモートアップデート機能
	//////////////////////////////////
	public static HashMap<String, String> getAgentLibMd5(String facilityId) {
		try {
			_agentLibMd5CacheLock.readLock();
			
			Map<String, Map<String, String>> agentLibMd5 = getAgentLibMd5Cache();
			Map<String, String> map = agentLibMd5.get(facilityId);
			return map == null ? new HashMap<String, String>() : new HashMap<String, String>(map);
		} finally {
			_agentLibMd5CacheLock.readUnlock();
		}
	}

	/**
	 * AgentLibMd5にファシリティIDとMD5の組がセットされているかチェックする。
	 * セットされていない場合は、トピックを発行して、情報取得を依頼する。
	 * (その直後にsetAgentLibMd5がやってくるはず。)
	 * @param facilityId
	 */
	public static void checkAgentLibMd5(String facilityId) {
		try {
			_agentLibMd5CacheLock.readLock();
			
			Map<String, Map<String, String>> agentLibMd5 = getAgentLibMd5Cache();
			Map<String, String> map = agentLibMd5.get(facilityId);
			if (map != null) {
				return;
			}
		} finally {
			_agentLibMd5CacheLock.readUnlock();
		}
		
		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setNewFacilityFlag(true);
		AgentConnectUtil.setTopic(facilityId, topicInfo);
	}

	public static void setAngetLibMd5(String facilityId, HashMap<String, String> map) {
		try {
			_agentLibMd5CacheLock.writeLock();
			
			HashMap<String, Map<String, String>> agentLibMd5 = getAgentLibMd5Cache();
			agentLibMd5.put(facilityId, map);
			storeAgentLibMd5Cache(agentLibMd5);
		} finally {
			_agentLibMd5CacheLock.writeUnlock();
		}
	}

	//////////////////////////////////
	// インターナルメッセージ
	//////////////////////////////////
	public static void sendMessageLocal(OutputBasicInfo outputBasicInfo,
			ArrayList<String> facilityIdList)
					throws HinemosUnknown, FacilityNotFound {
		// 監視情報を取得
		ArrayList<String> facilityList = null;
		if (!"SYS".equals(outputBasicInfo.getMonitorId())) {
			try {
				MonitorInfo entity = QueryUtil.getMonitorInfoPK_NONE(outputBasicInfo.getMonitorId());
				String monitorFacilityId = entity.getFacilityId();
				facilityList = FacilitySelector.getFacilityIdList(monitorFacilityId, entity.getOwnerRoleId(), 0, false, false);
			} catch (MonitorNotFound e) {
				m_log.warn(e.getMessage() + " (" + outputBasicInfo.getMonitorId() + ")", e);
			}
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
			AsyncWorkerPlugin.addTask(NotifyEventTaskFactory.class.getSimpleName(), clonedInfo, false);
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
		pingPort = HinemosPropertyUtil.getHinemosPropertyNum("common.agent.discovery.pingport", Long.valueOf(24005)).intValue();
		if (pingPort < 1 || pingPort > 65535) {
			return false;
		}
		// エージェントが接続するためのマネージャのIPを調べる
		try {
			// DNS名接続の場合(agent.connection.dnsnameに文字列が指定) Hinemos ver 4.0.2以降対応
			managerIpAddr = HinemosPropertyUtil.getHinemosPropertyStr("agent.connection.dnsname","");
			
			// IPアドレス接続の場合(agent.connection.dnsnameが未指定)
			if(managerIpAddr == null || "".equals(managerIpAddr)){
				managerIpAddr = HinemosPropertyUtil.getHinemosPropertyStr(
						"agent.connection.ipaddres", InetAddress.getLocalHost()
						.getHostAddress());
			}
		} catch (UnknownHostException e) {
			throw e;
		}

		// FIXME 本関数内の処理では通信のタイムアウトなどを設けていないため、異常な応答などが返った場合に無限待ちとなる可能性がある
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
}
