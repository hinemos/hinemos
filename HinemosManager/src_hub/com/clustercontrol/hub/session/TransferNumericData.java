/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.session;

import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;

/**
 * 数値情報を纏めて、Transfer へ渡す際に使用する
 * 
 *
 */
public class TransferNumericData {
	public final CollectKeyInfo key;
	public final CollectData data;
	
	public TransferNumericData(CollectKeyInfo key, CollectData data) {
		this.key = key;
		this.data = data;
	}
}
