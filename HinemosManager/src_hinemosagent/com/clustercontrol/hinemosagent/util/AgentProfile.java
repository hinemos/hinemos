/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.hinemosagent.bean.AgentJavaArch;
import com.clustercontrol.hinemosagent.bean.AgentJavaInfo;
import com.clustercontrol.hinemosagent.bean.AgentJavaOs;
import com.clustercontrol.hinemosagent.bean.AgentLibMd5s;

/**
 * Hinemosエージェントから送られてきた、MD5やJavaの情報を管理します。
 * 
 * @since 6.2.0
 */
public class AgentProfile {
	// ver.6.2先行版のバージョン判定に用いるHinemosエージェントのライブラリファイル
	private static final String V62_SAMPLE_LIB_PATH = "HinemosAgent.jar";

	// logger
	private static final Log log = LogFactory.getLog(AgentProfile.class);

	// ライブラリファイルの一覧
	private AgentLibMd5s libMd5s;
	// Java情報 (nullの場合もある)
	private AgentJavaInfo javaInfo;
	// Javaの対応OS
	private AgentJavaOs javaOs;
	// Javaの対応アーキテクチャ(プロセッサ)
	private AgentJavaArch javaArch;

	// 外部依存動作をモックへ置換できるように分離
	private External external;
	static class External {
		
		String getAgentV62BetaMd5s() {
			return HinemosPropertyCommon.agent_v62beta_md5s.getStringValue();
		}
	}
	
	/**
	 * コンストラクタ。 
	 * 
	 * @param libMd5s ライブラリファイル、nullにはできません。
	 * @param javaInfo Java情報、v6.2正式版より前のバージョンではnullを指定します。
	 */
	public AgentProfile(AgentLibMd5s libMd5s, AgentJavaInfo javaInfo) {
		this(new External(), libMd5s, javaInfo);
	}

	AgentProfile(External external, AgentLibMd5s libMd5s, AgentJavaInfo javaInfo) {
		this.external = external;
		
		if (libMd5s == null) {
			log.error("ctor: libMd5s is null.");
			throw new IllegalArgumentException("libMd5s is null");
		}

		this.libMd5s = libMd5s;
		this.javaInfo = javaInfo;
		javaOs = AgentJavaOs.detect(javaInfo);
		javaArch = AgentJavaArch.detect(javaInfo);

		// OSもしくはアーキテクチャの判別ができなかった場合、ログからロジック修正を検討できるように、情報を吐いておく。
		if (javaOs == AgentJavaOs.OTHERS || javaArch == AgentJavaArch.OTHERS) {
			log.info(String.format("ctor: Failed to detect Java OS/Arch. OS=%s, Arch=%s, %s", javaOs.name(), javaArch.name(),
					javaInfo.toString()));
		}
	}

	/**
	 * ライブラリファイルの一覧を返します。
	 */
	public AgentLibMd5s getLibMd5s() {
		return libMd5s;
	}

	/**
	 * Java情報を返します。
	 * Java情報を送信しないver6.2先行版以前のエージェントの場合は、nullになります。
	 */
	public AgentJavaInfo getJavaInfo() {
		return javaInfo;
	}

	/**
	 * Java情報から判定した、JVMのOS種別を返します。
	 */
	public AgentJavaOs getJavaOs() {
		return javaOs;
	}

	/**
	 * Java情報から判定した、JVMのプロセッサ アーキテクチャを返します。
	 */
	public AgentJavaArch getJavaArch() {
		return javaArch;
	}

	/**
	 * このエージェントが「ver.6.1以前」かどうかを判定します。
	 * 
	 * @return ver.6.1以前であればtrue、そうでなければfalse。
	 */
	public boolean isV61Earlier() {
		return javaInfo == null && !isV62Beta();
	}
	
	/**
	 * このエージェントが「ver.6.2先行版」かどうかを判定します。
	 * 
	 * @return ver.6.2先行版であればtrue、そうでなければfalse。
	 */
	// TODO: ver.6.2先行版との接続が不要になった場合、本メソッド(あるいはクラス全体)は削除可能です。
	public boolean isV62Beta() {
		String md5 = libMd5s.getMd5(V62_SAMPLE_LIB_PATH);
		if (md5 == null) {
			log.info(String.format("isV62Beta: '%s' not exist.", V62_SAMPLE_LIB_PATH));
			return false;
		}
		log.debug("isV62Beta: MD5=" + md5);

		String[] sampleMd5s = external.getAgentV62BetaMd5s().split(",");
		for (String sampleMd5 : sampleMd5s) {
			if (md5.equalsIgnoreCase(sampleMd5.trim())) {
				return true;
			}
		}
		return false;
	}

}
