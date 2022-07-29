/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.infra.bean.InfraParameterConstant;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.model.InfraManagementParamInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.util.RepositoryUtil;

/**
 * 環境構築変数ユーティリティクラス<BR>
 *
 * @version 6.1.0
 */
public class InfraParameterUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( InfraParameterUtil.class );

	/**
	 * ノードの基本情報をハッシュとして返す
	 * @param nodeInfo
	 * @return
	 */
	public static Map<String, String> createInfraParameter(String managementId) {
		Map<String, String> params = new HashMap<>();
		if (managementId == null || managementId.isEmpty()) {
			return params;
		}
		List<InfraManagementParamInfo> infraParamList
			= QueryUtil.getInfraManagementParamListFindByManagementId(managementId);
		for (InfraManagementParamInfo infraParam : infraParamList) {
			params.put(infraParam.getId().getParamId(), infraParam.getValue());
			m_log.debug("createInfraParameter() put : "
					+ "managementId=" + managementId
					+ ", paramId=" + infraParam.getId().getParamId()
					+ ", value=" + infraParam.getValue());
		}
		return params;
	}

	/**
	 * 置換用ののマップを作成する
	 * 優先順位は以下のとおり
	 *   1. 環境構築ファイル(#[FILE:ファイルID])
	 *   2. 環境構築変数
	 *   3. ノード変数
	 *   
	 * @param nodeInfo ノード情報
	 * @param paramMap 環境構築変数マップ
	 * @return 置換用のマップ
	 */
	public static HashMap<String, String> createBindMap(NodeInfo nodeInfo, Map<String, String> paramMap, ArrayList<String> inKeyList) {
		HashMap<String, String> map = new HashMap<>();
		JpaTransactionManager jtm = new JpaTransactionManager();
		try {
			// ノード変数
			Map<String, String> variable = RepositoryUtil.createNodeParameter(nodeInfo, inKeyList);
			map.putAll(variable);

			// 環境構築変数
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				String[] keyArgs = entry.getKey().split(InfraParameterConstant.PARAMETER_DELIMITER);
				if (keyArgs.length >= 2 && keyArgs[1].equals(nodeInfo.getFacilityId())) {
					map.put(keyArgs[0], entry.getValue());
				} else {
					map.put(entry.getKey(), entry.getValue());
				}
			}

			// ファイルID			
			List<InfraFileInfo> fileList = QueryUtil.getAllInfraFile();
			for (InfraFileInfo file : fileList) {
				String key = "FILE:" + file.getFileId();
				String value = file.getFileName();
				map.put(key, value);
				m_log.debug("createBindMap()  >>> param.put = : " + key  + "  value = " +  value);
			}
		} catch (Exception e) {
			m_log.warn("createBindMap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			jtm.close();
		}
		return map;
	}
}
