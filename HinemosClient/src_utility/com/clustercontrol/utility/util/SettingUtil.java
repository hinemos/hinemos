/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openapitools.client.model.GetImportUnitNumberResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;

//設定関連のUtilクラス
public class SettingUtil {
	
	/**
	 * インポート対象機能毎の一括インポート単位数を取得
	 * 
	 * @param functionIdlist インポート対象機能のIDリスト
	 * @return インポート対象機能毎の一括インポート単位数(MAP)
	 * @throws HinemosUnknown 
	 * @throws RestConnectFailed 
	 * @throws InvalidRole 
	 * @throws InvalidUserPass 
	 */
	public static Map<String,Integer> getImportUnitNumList(List<String> functionIdlist) throws InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown {

		Map<String,Integer> resultMap = new HashMap<String,Integer>();
		String functionIds = functionIdlist.stream().collect(Collectors.joining(","));
		UtilityRestClientWrapper wrapper = UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		List<GetImportUnitNumberResponse> resDtoList = wrapper.getImportUnitNumber(functionIds);
		if(resDtoList !=null){
			for (GetImportUnitNumberResponse rec : resDtoList) {
				resultMap.put(rec.getFunctionId(), rec.getImportUnitNumber());
			}
		}
		return resultMap;
		
	}
}
