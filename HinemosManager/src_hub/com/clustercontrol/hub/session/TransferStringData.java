/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.session;

import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringKeyInfo;

/**
 * 文字列情報を纏めて、Transfer へ渡す際に使用する
 * 
 *
 */
public class TransferStringData {
	public final CollectStringKeyInfo key;
	public final CollectStringData data;
	
	public TransferStringData(CollectStringKeyInfo key, CollectStringData data) {
		this.key = key;
		this.data = data;
	}
}
