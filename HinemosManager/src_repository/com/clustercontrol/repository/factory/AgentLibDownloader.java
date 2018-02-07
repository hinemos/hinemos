/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.repository.bean.AgentStatusInfo;

/**
 * マネージャが保持しているエージェントライブラリを管理するクラス<BR>
 */
public class AgentLibDownloader {

	private static Log m_log = LogFactory.getLog(AgentLibDownloader.class);

	/**
	 * マネージャが保持しているエージェントライブラリのリストを返す。
	 * @return
	 * @throws HinemosUnknown 
	 */
	public static HashMap<String, String> getAgentLibMap() throws HinemosUnknown {
		HashMap<String, String> fileMap = new HashMap<String, String>();

		String agentLibDir = System.getProperty("hinemos.manager.home.dir", "/opt/hinemos/") + "/lib/agent";
		File dir = new File(agentLibDir);
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					fileMap.put(file.getName(), getMD5(file.getAbsolutePath()));
				}
			}
		} else {
			m_log.info("files is null, agentLibDir=" + agentLibDir);
		}
		return fileMap;
	}

	/**
	 * エージェントの状態を返します。<BR>
	 * 
	 */
	public static ArrayList<AgentStatusInfo> getAgentStatusList() throws HinemosUnknown {
		m_log.debug("getAgentStatusList() ");

		// ユーザが所属するロールを取得する
		List<String> ownerRoleIdList = new AccessControllerBean().getOwnerRoleIdList();

		HashMap<String, String> managerMap = getAgentLibMap();
		ArrayList<AgentStatusInfo> ret = new ArrayList<AgentStatusInfo>();
		for (AgentInfo agentInfo : AgentConnectUtil.getAgentList()) {
			// ファシリティID
			String facilityId = agentInfo.getFacilityId();
			if (facilityId == null) {
				continue;
			}
			AgentStatusInfo agent = null;
			agent = new AgentStatusInfo();
			agent.setFacilityId(facilityId);

			// ファシリティ情報の取得
			String facilityName = "";
			try {
				// オーナーロールIDへの参照権限がない場合は一覧に含めない。
				String ownerRoleId = NodeProperty.getProperty(facilityId).getOwnerRoleId();
				if (!ownerRoleIdList.contains(ownerRoleId)) {
					m_log.debug("getAgentStatusList() : not ownerRole facilityId = " + facilityId);
					continue;
				}
				m_log.debug("getAgentStatusList() : ownerRole facilityId = " + facilityId);
				facilityName = NodeProperty.getProperty(facilityId).getFacilityName();
			} catch (FacilityNotFound e) {
				// ファシリティ情報が取得できない場合は、一覧に含めない
				m_log.info("getAgentStatusList() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				continue;
			}
			agent.setFacilityName(facilityName);

			// startup time
			agent.setStartupTime(agentInfo.getStartupTime());

			// last login
			agent.setLastLogin(agentInfo.getLastLogin());

			// multiplicity
			int runningMultiplicity = JobMultiplicityCache.getRunningMultiplicity(facilityId);
			int waitMultiplicity = JobMultiplicityCache.getWaitMultiplicity(facilityId);
			/*
			running,wait,maxの3つを並べると見にくいので、maxは表示しない。
			int maxMultiplicity = 0;
			try {
				maxMultiplicity = new RepositoryControllerBean().getNode(facilityId).getJobMultiplicity();
			} catch (FacilityNotFound e) {
				m_log.debug(e.getMessage(), e);
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			}
			 */
			agent.setMultiplicity(
					"run=" + runningMultiplicity +
					",wait=" + waitMultiplicity);

			// 最新チェック
			boolean flag = true;
			HashMap<String, String> agentMap = AgentConnectUtil.getAgentLibMd5(facilityId);
			for (Map.Entry<String, String> filenameEntry: managerMap.entrySet()) {
				String agentMd5 = agentMap.get(filenameEntry.getKey());
				String managerMd5 = filenameEntry.getValue();
				// マネージャが保持しているのに、エージェントが保持していない場合は、
				// エージェントが最新でない。
				if (agentMd5 == null) {
					m_log.info("getAgentStatusList() agentMd5 is null + [" + facilityId +
							"] filename=" + filenameEntry.getKey());
					flag = false;
					break;
				}
				if (!agentMd5.equals(managerMd5)) {
					m_log.debug("getAgentStatusList() agentMd5 differs from managerMd5 [" + facilityId +
							"] filename=" + filenameEntry.getKey() +
							", managerMd5=" + managerMd5 + ", agentMd5=" + agentMd5);
					flag = false;
					break;
				}
			}
			agent.setNewFlag(flag);

			ret.add(agent);
		}
		return ret;
	}


	/**
	 * MD5を取得する。
	 * @param filepath
	 * @return
	 * @throws HinemosUnknown 
	 */
	private static String getMD5(String filepath) throws HinemosUnknown {
		MessageDigest md = null;
		DigestInputStream inStream = null;
		byte[] digest = null;
		try {
			md = MessageDigest.getInstance("MD5");
			inStream = new DigestInputStream(
					new BufferedInputStream(new FileInputStream(filepath)), md);
			while (inStream.read() != -1) {}
			digest = md.digest();
		} catch (Exception e) {
			m_log.warn("getMD5() : filepath=" + filepath + ", " + e.getClass(), e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (Exception e) {
					m_log.warn("getMD5() : close " + e.getClass(), e);
				}
			}
			if (digest == null)
				throw new HinemosUnknown("MD5 digest is null");
		}
		return hashByte2MD5(digest);
	}

	private static String hashByte2MD5(byte []input) {
		StringBuilder ret = new StringBuilder();
		for (byte b : input) {
			if ((0xff & b) < 0x10) {
				ret.append("0" + Integer.toHexString((0xFF & b)));
			} else {
				ret.append(Integer.toHexString(0xFF & b));
			}
		}
		return ret.toString();
	}
}
