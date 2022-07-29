/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlType;

/**
 * 構成情報収集を即時実行するための情報<BR>
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeConfigRunCollectInfo implements Serializable {
	// カラム構成変更したら更新すること.
	private static final long serialVersionUID = -4324117941296918253L;

	/** 負荷分散間隔 */
	private Long loadDistributionTime = null;

	/** 実行指示情報{@literal <Agent向け構成情報設定, 実行指示日時>} */
	private HashMap<NodeConfigSetting, Long> instructedInfoMap = new HashMap<NodeConfigSetting, Long>();

	/** 負荷分散間隔 */
	public Long getLoadDistributionTime() {
		return loadDistributionTime;
	}

	/** 負荷分散間隔 */
	public void setLoadDistributionTime(Long loadDistributionTime) {
		this.loadDistributionTime = loadDistributionTime;
	}

	/** 実行指示情報{@literal <Agent向け構成情報設定, 実行指示日時>} */
	public HashMap<NodeConfigSetting, Long> getInstructedInfoMap() {
		return instructedInfoMap;
	}

	/** 実行指示情報{@literal <Agent向け構成情報設定, 実行指示日時>} */
	public void setInstructedInfoMap(HashMap<NodeConfigSetting, Long> instructedInfoMap) {
		this.instructedInfoMap = instructedInfoMap;
	}

}
