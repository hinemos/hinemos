/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.clustercontrol.repository.bean.NodeConfigFilterDataType;
import com.clustercontrol.repository.bean.NodeConfigFilterItem;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.ws.repository.NodeConfigFilterInfo;
import com.clustercontrol.ws.repository.NodeConfigFilterItemInfo;
import com.clustercontrol.ws.repository.NodeInfo;

/**
 * ノードマップのUtilityクラス
 *
 */
public class NodemapUtil {

	/**
	 * 検索条件文字列を作成
	 *
	 * @param nodeFilterInfo 構成情報検索条件
	 * @return 検索条件文字列
	 */
	public static String createConditionString(NodeInfo nodeFilterInfo){
		if (nodeFilterInfo == null) {
			return "";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(TimezoneUtil.getTimeZone());

		List<String> conditionList = new ArrayList<>();
		List<String> conditionDetailList = new ArrayList<>();
		// 対象日時
		if (nodeFilterInfo.getNodeConfigTargetDatetime() != null
				&& nodeFilterInfo.getNodeConfigTargetDatetime() != 0L) {
			conditionList.add(String.format("%s=%s", Messages.getString("target.datetime"), 
					sdf.format(new Date(nodeFilterInfo.getNodeConfigTargetDatetime()))));
		}
		if (nodeFilterInfo.getNodeConfigFilterList() != null
				&& nodeFilterInfo.getNodeConfigFilterList().size() > 0) {
			// 各種条件
			for (NodeConfigFilterInfo nodeConfigFilterInfo : nodeFilterInfo.getNodeConfigFilterList()) {
				if (nodeConfigFilterInfo.getItemList() == null 
						|| nodeConfigFilterInfo.getItemList().size() == 0) {
					continue;
				}
				NodeConfigSettingItem settingItem = NodeConfigSettingItem.nameToType(nodeConfigFilterInfo.getNodeConfigSettingItemName());
				List<String> list = new ArrayList<>();
				for (NodeConfigFilterItemInfo itemInfo : nodeConfigFilterInfo.getItemList()) {
					if (itemInfo.getItemName() == null || itemInfo.getItemName().isEmpty()
							|| itemInfo.getMethod() == null || itemInfo.getMethod().isEmpty()
							|| itemInfo.getItemValue() == null
							|| (itemInfo.getItemValue() instanceof String && ((String)itemInfo.getItemValue()).isEmpty())) {
						continue;
					}
					NodeConfigFilterItem filterItem = NodeConfigFilterItem.valueOf(itemInfo.getItemName());
					Object objValue = "";
					if (filterItem.dataType() == NodeConfigFilterDataType.DATETIME) {
						objValue = sdf.format(new Date((Long)itemInfo.getItemValue()));
					} else {
						objValue = itemInfo.getItemValue();
					}
					list.add(String.format("%s%s%s", filterItem.displayName(), itemInfo.getMethod(), objValue));
				}
				if (list.size() > 0) {
					String existsString = "";
					if (nodeConfigFilterInfo.isExists()) {
						existsString = Messages.getString("node.config.exits.condition.exists.output");
					} else{
						existsString = Messages.getString("node.config.exits.condition.notexists.output");
					}
					conditionDetailList.add(
						String.format("%s=%s(%s) %s",
								Messages.getString("search.target"),
								settingItem.displayName(),
								String.join(", ", list),
								existsString)
						);
				}
			}
			if (conditionDetailList.size() > 0) {
				// AND/OR
				String andOr = "";
				if (nodeFilterInfo.isNodeConfigFilterIsAnd()) {
					andOr = "AND";
				} else {
					andOr = "OR";
				}
				conditionList.add(String.format("%s=%s", Messages.getString("condition.between.objects"), andOr));
				conditionList.addAll(conditionDetailList);
			}
		}

		return String.join(", ", conditionList);
	}
}
