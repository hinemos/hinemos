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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.GetNodeListRequest;
import org.openapitools.client.model.NodeConfigFilterInfoRequest;
import org.openapitools.client.model.NodeConfigFilterItemInfoRequest;

import com.clustercontrol.repository.bean.NodeConfigFilterDataType;
import com.clustercontrol.repository.bean.NodeConfigFilterItem;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

/**
 * ノードマップのUtilityクラス
 *
 */
public class NodemapUtil {
	private static Log m_log = LogFactory.getLog(NodemapUtil.class);
	/**
	 * 検索条件文字列を作成
	 *
	 * @param nodeFilterInfo 構成情報検索条件
	 * @return 検索条件文字列
	 */
	public static String createConditionString(GetNodeListRequest nodeFilterInfo){
		if (nodeFilterInfo == null) {
			return "";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(TimezoneUtil.getTimeZone());

		List<String> conditionList = new ArrayList<>();
		List<String> conditionDetailList = new ArrayList<>();
		// 対象日時
		if (nodeFilterInfo.getNodeConfigTargetDatetime() != null
				&& !nodeFilterInfo.getNodeConfigTargetDatetime().equals("")) {
			try {
				conditionList.add(String.format("%s=%s", Messages.getString("target.datetime"), 
						sdf.format(TimezoneUtil.getSimpleDateFormat().parse(nodeFilterInfo.getNodeConfigTargetDatetime()))));
			} catch (Exception e) {
				//findbugs対応 エラーは発生しない想定なので本来不要だが Exception無視と思われないようtraceログの出力を追加
				m_log.trace("createConditionString : exception occuered",e);
			}
		}
		if (nodeFilterInfo.getNodeConfigFilterList() != null
				&& nodeFilterInfo.getNodeConfigFilterList().size() > 0) {
			// 各種条件
			for (NodeConfigFilterInfoRequest nodeConfigFilterInfo : nodeFilterInfo.getNodeConfigFilterList()) {
				if (nodeConfigFilterInfo.getItemList() == null 
						|| nodeConfigFilterInfo.getItemList().size() == 0) {
					continue;
				}
				NodeConfigSettingItem settingItem = NodeConfigSettingItem.nameToType(nodeConfigFilterInfo.getNodeConfigSettingItemName().getValue());
				List<String> list = new ArrayList<>();
				for (NodeConfigFilterItemInfoRequest itemInfo : nodeConfigFilterInfo.getItemList()) {
					if (itemInfo.getItemName() == null || itemInfo.getMethod() == null || itemInfo.getMethod().isEmpty()) {
						continue;
					}
					NodeConfigFilterItem filterItem = NodeConfigFilterItem.valueOf(itemInfo.getItemName().getValue());
					Object objValue = "";
					if (filterItem.dataType() == NodeConfigFilterDataType.DATETIME) {
						if (itemInfo.getItemLongValue() == null) {
							continue;
						}
						objValue = sdf.format(new Date(itemInfo.getItemLongValue()));
					} else if (filterItem.dataType() == NodeConfigFilterDataType.INTEGER
						|| filterItem.dataType() == NodeConfigFilterDataType.INTEGER_ONLYEQUAL) {
						if (itemInfo.getItemIntegerValue() == null) {
							continue;
						}
						objValue = itemInfo.getItemIntegerValue();

					} else if (filterItem.dataType() == NodeConfigFilterDataType.STRING
						|| filterItem.dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL
						|| filterItem.dataType() == NodeConfigFilterDataType.STRING_VERSION) {
						if (itemInfo.getItemStringValue() == null || itemInfo.getItemStringValue().isEmpty()) {
							continue;
						}
						objValue = itemInfo.getItemStringValue();
					}
					list.add(String.format("%s%s%s", filterItem.displayName(), itemInfo.getMethod(), objValue));
				}
				if (list.size() > 0) {
					String existsString = "";
					if (nodeConfigFilterInfo.getExists()) {
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
				if (nodeFilterInfo.getNodeConfigFilterIsAnd()) {
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
