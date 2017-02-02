/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.performance.action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.ws.monitor.CollectorItemTreeItem;
import com.clustercontrol.ws.monitor.HashMapInfo;
import com.clustercontrol.ws.monitor.HashMapInfo.Map2;
import com.clustercontrol.ws.monitor.HashMapInfo.Map2.Entry;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;

/**
 * 収集した性能情報を取得を行うアクションクラス
 *
 * @version 4.0.0
 * @since 1.0.0
 *
 */
public class RecordController {
	private static Log log = LogFactory.getLog(RecordController.class);

	private int errorCount = 1;

	/**
	 * コンストラクタ
	 */
	public RecordController() {
	}

	/**
	 * 収集項目コード情報を取得します。
	 *
	 * @param ファシリティID
	 * @return デバイス情報セット
	 */
	public Map<String, CollectorItemTreeItem> getItemCodeTreeMap(String managerName) {
		log.debug("getItemCodeTreeMap() : managerName=" + managerName);
		CollectorItemTreeItem treeItem = null;
		for (int i = 0; i <= this.errorCount; i++) {
			try {
				//TODO:見直しする
				//            	return bean.getItemCodeMap();
				MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
				HashMapInfo hashMapInfo = wrapper.getItemCodeMap();
				Map2 map2 = hashMapInfo.getMap2();
				Map<String, CollectorItemTreeItem> rtnMap = new ConcurrentHashMap<String, CollectorItemTreeItem>();
				for(Entry entry : map2.getEntry()) {
					log.trace("entry : key=" + entry.getKey());
					treeItem = entry.getValue();
					setTreeParent(treeItem);
					rtnMap.put(entry.getKey(), treeItem);
				}
				log.debug("getItemCodeTreeMap() : size=" + rtnMap.size());
				return rtnMap;
			} catch (InvalidRole_Exception e) {
				log.error("getItemCodeTreeMap()", e);
			} catch (Exception e){
				log.error("getItemCodeTreeMap()", e);
			}
		}

		return null;
	}

	private static void setTreeParent(CollectorItemTreeItem item) {
		List<CollectorItemTreeItem> children = item.getChildren();
		for (CollectorItemTreeItem child : children) {
			child.setParent(item);
			setTreeParent(child);
		}
	}

}
