/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

/**
 * HinemosエージェントのJVMのターゲットプロセッサ。
 */
public enum AgentJavaArch {
	/** Java情報がないため不明 */
	UNKNOWN,
	/** x86 32ビット */
	X86,
	/** x86 64ビット (amd64) */
	X64,
	/** Java情報はあるが未確定 */
	OTHERS;

	public static AgentJavaArch detect(AgentJavaInfo javaInfo) {
		if (javaInfo == null) {
			return UNKNOWN;
		}
		if ("64".equals(javaInfo.getSunArchDataModel())) {
			return X64;
		}
		if ("32".equals(javaInfo.getSunArchDataModel())) {
			return X86;
		}
		if (javaInfo.getOsArch().endsWith("64")) {
			return X64;
		}
		if (javaInfo.getOsArch().endsWith("86")) {
			return X86;
		}
		return OTHERS;
	}
}