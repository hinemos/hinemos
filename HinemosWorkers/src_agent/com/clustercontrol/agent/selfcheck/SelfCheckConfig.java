/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.selfcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.AgentProperties;

public class SelfCheckConfig {

	private static Log log = LogFactory.getLog(SelfCheckConfig.class);
	
	private static final String selfcheckIntervalKey = "selfcheck.interval";
	private static long selfcheckInterval = 150000;

	private static final String selfcheckAlertThresholdKey = "selfcheck.alert.threshold";
	private static int selfcheckAlertThreshold = 3;
	
	private static final String selfcheckAlertThresholdAfterFirstAlertKey = "selfcheck.alert.threshold.after.first.alert";
	private static int selfcheckAlertThresholdAfterFirstAlert = 1;
	
	private static final String selfcheckShutdownTimeoutKey = "selfcheck.shutdown.timeout";
	private static long selfcheckShutdownTimeout = 15000;
	
	private static final String selfcheckMonitoringJvmFreeheapKey = "selfcheck.monitoring.jvm.freeheap";
	private static boolean selfcheckMonitoringJvmFreeheap = true;
	
	private static final String selfcheckMonitoringJvmFreeheapThresholdKey = "selfcheck.monitoring.jvm.freeheap.threshold";
	private static int selfcheckMonitoringJvmFreeheapThreshold = 4;
	
	public static void init() {
		String si = AgentProperties.getProperty(selfcheckIntervalKey);
		if (si != null) {
			try {
				selfcheckInterval = Integer.parseInt(si);
			} catch (NumberFormatException e) {
				log.error(selfcheckIntervalKey, e);
			}
		}
		
		String sat = AgentProperties.getProperty(selfcheckAlertThresholdKey);
		if (sat != null) {
			try {
				selfcheckAlertThreshold = Integer.parseInt(sat);
			} catch (NumberFormatException e) {
				log.error(selfcheckAlertThresholdKey, e);
			}
		}
		
		String satafa = AgentProperties.getProperty(selfcheckAlertThresholdAfterFirstAlertKey);
		if (satafa != null) {
			try {
				selfcheckAlertThresholdAfterFirstAlert = Integer.parseInt(satafa);
			} catch (NumberFormatException e) {
				log.error(selfcheckAlertThresholdAfterFirstAlertKey, e);
			}
		}
		
		String hsst = AgentProperties.getProperty(selfcheckShutdownTimeoutKey);
		if (hsst != null) {
			try {
				selfcheckShutdownTimeout = Integer.parseInt(hsst);
			} catch (NumberFormatException e) {
				log.error(selfcheckShutdownTimeoutKey, e);
			}
		}
		
		selfcheckMonitoringJvmFreeheap = Boolean.valueOf(AgentProperties.getProperty(selfcheckMonitoringJvmFreeheapKey, "true"));
		
		String smjft = AgentProperties.getProperty(selfcheckMonitoringJvmFreeheapThresholdKey);
		if (smjft != null) {
			try {
				selfcheckMonitoringJvmFreeheapThreshold = Integer.parseInt(smjft);
			} catch (NumberFormatException e) {
				log.error(selfcheckMonitoringJvmFreeheapThresholdKey, e);
			}
		}

		log.info(selfcheckIntervalKey + " = " + selfcheckInterval);
		log.info(selfcheckAlertThresholdKey + " = " + selfcheckAlertThreshold);
		log.info(selfcheckAlertThresholdAfterFirstAlertKey + " = " + selfcheckAlertThresholdAfterFirstAlert);
		log.info(selfcheckShutdownTimeoutKey + " = " + selfcheckShutdownTimeout);
		log.info(selfcheckMonitoringJvmFreeheapKey + " = " + selfcheckMonitoringJvmFreeheap);
		log.info(selfcheckMonitoringJvmFreeheapThresholdKey + " = " + selfcheckMonitoringJvmFreeheapThreshold);
	}

	public static long getSelfcheckInterval() {
		return selfcheckInterval;
	}

	public static int getSelfcheckAlertThreshold() {
		return selfcheckAlertThreshold;
	}
	
	public static int getSelfcheckAlertThresholdAfterFirstAlert() {
		return selfcheckAlertThresholdAfterFirstAlert;
	}
	
	public static long getSelfcheckShutdownTimeout() {
		return selfcheckShutdownTimeout;
	}
	
	public static boolean getSelfcheckMonitoringJvmFreeheap() {
		return selfcheckMonitoringJvmFreeheap;
	}
	
	public static int getSelfcheckMonitoringJvmFreeheapThreshold() {
		return selfcheckMonitoringJvmFreeheapThreshold;
	}
}
