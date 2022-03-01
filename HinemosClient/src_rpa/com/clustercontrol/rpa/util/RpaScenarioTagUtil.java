/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openapitools.client.model.RpaScenarioTagResponse;

/**
 * RPAシナリオタグ用ユーティリティクラス
 */
public class RpaScenarioTagUtil {

	/**
	 * タグの階層を表示した文字列を返します。
	 * 階層=「：」、区切=「,」
	 * 
	 */
	public String getJoinTagLayer(List<RpaScenarioTagResponse> selectTagList, Map<String,String> tagNameMap, Map<String,String> tagPathMap) {
		ArrayList<String> tagTextList = new ArrayList<>();
		for (RpaScenarioTagResponse tag : selectTagList){
			tagTextList.add(getTagLayer(tag, tagNameMap, tagPathMap));
		}
		
		// タグ階層を:、タグ区切りを,で表現
		return String.join(",", tagTextList);
	}

	public String getTagLayer(RpaScenarioTagResponse tag, Map<String,String> tagNameMap, Map<String,String> tagPathMap) {
		String tagText = "";
		String tagPathOrigin = tagPathMap.get(tag.getTagId());
		
		// タグ階層は、親タグの場合は空白、子タグの場合は\親タグ...を想定
		if (tagPathOrigin == null){
			return tagText;
		}
		
		String[] tagPathArray = tagPathOrigin.split("\\\\");
		if (tagPathArray.length > 1){
			for (String tagPath : tagPathArray){
				if (tagPath.isEmpty()){
					continue;
				}
				
				if (tagText.isEmpty()){
					// 第一階層は必ず元となる親タグになる
					tagText = tagNameMap.get(tagPath);
				} else {
					// 第二階層以降は:で繋げる
					tagText = tagText + ":" + tagNameMap.get(tagPath);
				}
			}
			// 最後にタグ自身を:で繋げる
			tagText = tagText + ":" + tagNameMap.get(tag.getTagId());
		} else {
			// 親タグが設定されていない場合
			tagText = tagNameMap.get(tag.getTagId());
		}
		
		return tagText;
	}
}
