/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.bean;

/**
 * DHCPサポート機能におけるノード更新方法の定義
 * Agentプロパティ"dhcp.update.mode"で指定する。
 */
public enum DhcpUpdateMode {
	/** 同一ホスト名のノードにエージェントのIPアドレスを設定する。 */
	ip,
	/** 同一IPアドレスのノードにエージェントのホスト名を設定する。 */
	host,
	/** ノードを更新しない */	
	disable
	;
	
	public static DhcpUpdateMode fromValue(String name) {
		try {
			return DhcpUpdateMode.valueOf(name);			
		} catch (IllegalArgumentException e) {
			// 既定値はdisable
			return disable;
		}
	}
}
