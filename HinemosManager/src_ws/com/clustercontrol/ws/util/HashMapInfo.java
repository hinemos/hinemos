/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.util;

import java.util.HashMap;
import java.util.TreeMap;

import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.performance.util.code.CollectorItemTreeItem;

/*
 * JAXBではWebMethodの引数、戻り値にはHashMapが使えない。
 * メンバ変数として定義されている場合は、独自クラスとしてクライアントから利用できるため、
 * メンバ変数にHashMapを持つクラスを用意する。
 */
public class HashMapInfo {
	private HashMap<String, CollectorItemTreeItem> map2 = new HashMap<>();
	private HashMap<Integer, ArrayListInfo> map3 = new HashMap<>();
	private HashMap<String, Integer> map4 = new HashMap<>();
	private HashMap<String, ArrayListInfo>map5 = new HashMap<>();
	private TreeMap<String, String>map6 = new TreeMap<>();
	private HashMap<String, ArrayListInfo> map7 = new HashMap<>();
	private HashMap<RunInstructionInfo, MonitorInfo> map8 = new HashMap<>();
	private HashMap<String, CollectKeyInfo> map9 = new HashMap<>();

	public HashMapInfo(){}

	// MonitorSettingEndpoint
	public HashMap<String, CollectorItemTreeItem> getMap2() {
		return map2;
	}
	public void setMap2(HashMap<String, CollectorItemTreeItem> map2) {
		this.map2 = map2;
	}

	// CollectEndpoint
	public HashMap<Integer, ArrayListInfo> getMap3() {
		return map3;
	}
	public void setMap3(HashMap<Integer, ArrayListInfo> map3) {
		this.map3 = map3;
	}

	// CollectEndpoint
	public HashMap<String, Integer> getMap4() {
		return map4;
	}
	public void setMap4(HashMap<String, Integer> map4) {
		this.map4 = map4;
	}

	// CollectEndpoint
	public HashMap<String, ArrayListInfo> getMap5() {
		return map5;
	}
	public void setMap5(HashMap<String, ArrayListInfo> map5) {
		this.map5 = map5;
	}

	// CollectEndpoint
	public TreeMap<String, String> getMap6() {
		return map6;
	}
	public void setMap6(TreeMap<String, String> map6) {
		this.map6 = map6;
	}
	
	// CollectEndpoint
	public HashMap<String, ArrayListInfo> getMap7() {
		return map7;
	}
	public void setMap7(HashMap<String, ArrayListInfo> map7) {
		this.map7 = map7;
	}

	// AgentEndpoint
	public HashMap<RunInstructionInfo, MonitorInfo> getMap8() {
		return map8;
	}
	public void setMap8(HashMap<RunInstructionInfo, MonitorInfo> map8) {
		this.map8 = map8;
	}

	// CollectEndpoint
	public HashMap<String, CollectKeyInfo> getMap9() {
		return map9;
	}
	public void setMap9(HashMap<String, CollectKeyInfo> map9) {
		this.map9 = map9;
	}
	
}
