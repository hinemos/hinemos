/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.openapitools.client.model.CorrectExecNodeDetailRequest;
import org.openapitools.client.model.RpaScenarioExecNodeResponse;
import org.openapitools.client.model.RpaScenarioResponseP1;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.util.PropertyUtil;

/**
 * RPAシナリオ実行ノード訂正コンポジット作成用ユーティリティクラス
 */
public class RpaScenarioCorrectExecNodePropertyUtil {
	
	/**
	 * プロパティをRPAシナリオ実行ノードDTOに変換するメソッドです。
	 */
	public static List<CorrectExecNodeDetailRequest> property2dto(Property property, List<RpaScenarioExecNodeResponse> execNodeList){
		List<CorrectExecNodeDetailRequest> ret = new ArrayList<>();

		for (RpaScenarioExecNodeResponse execNode : execNodeList){
			ArrayList<?> values = PropertyUtil.getPropertyValue(property, execNode.getFacilityId());
			String scenarioId = (String) values.get(0);
			CorrectExecNodeDetailRequest detail = new CorrectExecNodeDetailRequest();
			detail.setFacilityId(execNode.getFacilityId());
			detail.setScenarioId(scenarioId);
			ret.add(detail);
		}

		return ret;
	}
	
	/**
	 * RPAシナリオ実行ノード訂正コンポジット用プロパティを取得します。<BR>
	 */
	public static Property getProperty(List<RpaScenarioExecNodeResponse> execNodeList, List<RpaScenarioResponseP1> scenarioList) {
		// 初期表示ツリーを構成。
		Property property = new Property(null, null, "");
		property.removeChildren();
		
		List<RpaScenarioResponseP1> sortedScenarioList = scenarioList.stream()
				.sorted(Comparator.comparing(RpaScenarioResponseP1::getScenarioId))
				.collect(Collectors.toList());
		for (RpaScenarioExecNodeResponse execNode : execNodeList){
			Property facilityId =
					new Property(execNode.getFacilityId()
							, String.format("%s (%s)", execNode.getFacilityName(), execNode.getFacilityId())
							, PropertyDefineConstant.EDITOR_SELECT);
			facilityId.setSelectValues(getRpaScenarioIdList(sortedScenarioList));
			facilityId.setValue(execNode.getScenarioId());
			
			//変更の可/不可を設定
			facilityId.setModify(PropertyDefineConstant.MODIFY_OK);
			
			property.addChildren(facilityId);
		}
		
		return property;
	}

	private static Object[][] getRpaScenarioIdList(List<RpaScenarioResponseP1> scenarioList) {
		Object retArray[][] = null;
		
		if(scenarioList != null && scenarioList.isEmpty() == false)
		{
			
			retArray = new Object[2][scenarioList.size()];
			for (int i = 0; i < scenarioList.size(); i++){
				RpaScenarioResponseP1 scenarioInfo = scenarioList.get(i);
				retArray[PropertyDefineConstant.SELECT_VALUE][i] = scenarioInfo.getScenarioId();
				retArray[PropertyDefineConstant.SELECT_DISP_TEXT][i] = String.format("%s (%s)"
						,scenarioInfo.getScenarioName()
						, scenarioInfo.getScenarioId());
			}
		}
		else{
			Object nullArray[][] = {
					{ "" },
					{ "" }
			};
			retArray = nullArray;
		}
		
		return retArray;
	}

}
