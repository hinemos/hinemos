/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.packet;

import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;

public class PacketCaptureUtil {

	/**
	 * プロミスキャスモード取得.<br>
	 * <br>
	 * 監視設定で登録したプロミスキャスモードを元に enumを取得する. <br>
	 * <br>
	 * プロミスキャスモード：同一ネットワーク内を流れる全てのパケットを宛先にかかわらず受信可能なモード.<br>
	 * ⇔ノンプロミスキャスモード:自分宛のデータパケットのみ受信するモード.<br>
	 * 
	 * @param promiscuousMode
	 *            監視設定で登録したプロミスキャスモード
	 * 
	 * @return boolean値に応じたenum
	 * 
	 */
	public static PromiscuousMode getPromiscuousMode(boolean promiscuousMode) {
		if (promiscuousMode) {
			return PromiscuousMode.PROMISCUOUS;
		} else {
			return PromiscuousMode.NONPROMISCUOUS;
		}
	}

}
