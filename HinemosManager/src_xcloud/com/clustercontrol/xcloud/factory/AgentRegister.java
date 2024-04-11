/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Threading;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;

public class AgentRegister implements Runnable {
	private static final Logger logger = Logger.getLogger(AgentRegister.class);

	private String facilityId;
	private int count = 0;
	private int maxCount = HinemosPropertyCommon.xcloud_agent_connection_count.getIntegerValue();
	
	public AgentRegister(String facilityId) {
		this.facilityId = facilityId;
	}

	@Override
	public void run() {
		++count;
		logger.debug("Try to connect with " + facilityId + "'s agent - " + count + "/" + maxCount);

		boolean success = false;
		try {
			// ノードが削除されていないか確認。
			// sendManagerDiscoveryInfo は、FacilityNotFound　をスローしないので必要。
			RepositoryControllerBeanWrapper.bean().getNode(facilityId);
			success = AgentConnectUtil.sendManagerDiscoveryInfo(facilityId);
		} catch (FacilityNotFound e) {
			logger.warn(e.getMessage(), e);
			// 終了のために RuntimeException をスロー。
			throw new RuntimeException();
		} catch (java.net.ConnectException e) {
			logger.warn(e.getMessage());
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		if (success) {
			logger.info("Succeeded to connect to " + facilityId + "'s agent.");
			// 終了のために RuntimeException をスロー。
			throw new RuntimeException();
		}
		
		if (maxCount <= count) {
			logger.warn("Timed-out. Failed to connect with " + facilityId + "'s agent.");
			// 終了のために RuntimeException をスロー。
			throw new RuntimeException();
		}
	}

	public void asyncRegistAgent() {
		logger.info("Start to connect to " + facilityId + "'s agent...");
		Threading.scheduleWithFixedDelayForRegistAgent(this, 0,
				HinemosPropertyCommon.xcloud_agent_connection_interval.getIntegerValue(), TimeUnit.MILLISECONDS);
	}

	public void registAgent() {
		logger.info("Try to connect to " + facilityId + "'s agent...");

		try {
			for (;;) {
				run();
				Thread.sleep(HinemosPropertyCommon.xcloud_agent_connection_interval.getIntegerValue());
			}
		}catch (Exception e) {
			logger.debug(e.getMessage(), e);
		}
	}

	public Boolean findAgent() throws CloudManagerException {
		try {
			logger.info("Start to check to " + facilityId + "'s agent...");

			boolean result = false;
			int counter = 0;
			int maxCount = HinemosPropertyCommon.xcloud_agent_connection_count.getIntegerValue();
			long interval = HinemosPropertyCommon.xcloud_agent_connection_interval.getIntegerValue();
			while (counter < maxCount) {
				counter++;
				logger.debug("Try to find agent on " + facilityId + " - " + counter + "/" + maxCount);

				// ノードが削除されていないか確認。
				RepositoryControllerBeanWrapper.bean().getNode(facilityId);

				if (AgentConnectUtil.isValidAgent(facilityId)) {
					logger.info("Succeeded to find " + facilityId + "'s agent.");
					return true;
				}

				if (counter >= maxCount) {
					throw ErrorCode.AGENT_NOT_FOUND.cloudManagerFault();
				}
				Thread.sleep(interval);
			}
			return result;
		}
		catch (Exception e) {
			throw new CloudManagerException(e);
		}
	}

	public static void asyncRegistAgent(String facilityId) {
		new AgentRegister(facilityId).asyncRegistAgent();
	}

	public static void registAgent(String facilityId) {
		// 失敗か成功かなどの結果は、現時点では通知しない。
		new AgentRegister(facilityId).registAgent();
	}
	
	public static Boolean findAgent(String facilityId) throws CloudManagerException {
		return new AgentRegister(facilityId).findAgent();
	}
}
