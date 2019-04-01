/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

/**
 * HinemosエージェントのJVMのターゲットOS。
 */
public enum AgentJavaOs {
	/** Java情報がないため不明 */
	UNKNOWN,
	/** Linux (RHEL, CentOS) */
	LINUX,
	/** Windows */
	WINDOWS,
	/** Linux/Windows以外(Java情報はあるが未確定) */
	OTHERS;
	
	public static AgentJavaOs detect(AgentJavaInfo javaInfo) {
		if (javaInfo == null) {
			return UNKNOWN;
		}
		if (javaInfo.getOsName().toLowerCase().contains("windows")) {
			return WINDOWS;
		}
		if (javaInfo.getOsName().toLowerCase().contains("linux")) {
			return LINUX;
		}
		return OTHERS;
	}
}