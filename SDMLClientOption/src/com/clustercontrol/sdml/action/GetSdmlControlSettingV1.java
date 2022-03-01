/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.GetSdmlControlSettingListRequest;
import org.openapitools.client.model.SdmlControlSettingFilterInfoRequest;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;

import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.sdml.util.SdmlControlSettingFilterConstant;
import com.clustercontrol.sdml.util.SdmlControlSettingFilterPropertyUtil;
import com.clustercontrol.sdml.util.SdmlRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;

/**
 * SDML制御設定を取得するクライアント側アクションクラス
 *
 */
public class GetSdmlControlSettingV1 {
	private static Log logger = LogFactory.getLog(GetSdmlControlSettingV1.class);

	/**
	 * 指定したマネージャからSDML制御設定の一覧を取得します
	 * 
	 * @param managerName
	 * @return
	 */
	public Map<String, List<SdmlControlSettingInfoResponse>> getList(String managerName) {
		Map<String, List<SdmlControlSettingInfoResponse>> dispDataMap = new ConcurrentHashMap<>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		dispDataMap.put(managerName, getList(managerName, errorMsgs));

		// メッセージ表示
		if (0 < errorMsgs.size()) {
			UIManager.showMessageBox(errorMsgs, true);
		}
		return dispDataMap;
	}

	/**
	 * 接続している全てのマネージャからSDML制御設定の一覧を取得します
	 * 
	 * @return
	 */
	public Map<String, List<SdmlControlSettingInfoResponse>> getList() {
		Map<String, List<SdmlControlSettingInfoResponse>> dispDataMap = new ConcurrentHashMap<>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			dispDataMap.put(managerName, getList(managerName, errorMsgs));
		}

		// メッセージ表示
		if (0 < errorMsgs.size()) {
			UIManager.showMessageBox(errorMsgs, true);
		}
		return dispDataMap;
	}

	private List<SdmlControlSettingInfoResponse> getList(String managerName, Map<String, String> errorMsgs) {
		List<SdmlControlSettingInfoResponse> records = new ArrayList<>();

		try {
			SdmlRestClientWrapper wrapper = SdmlRestClientWrapper.getWrapper(managerName);
			records = wrapper.getSdmlControlSettingListV1(null);
		} catch (InvalidRole e) {
			errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			logger.warn("getList() : " + e.getMessage(), e);
			errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + ", "
					+ HinemosMessage.replace(e.getMessage()));
		}

		return records;
	}

	/**
	 * 指定したマネージャからアプリケーションIDの一覧を取得します
	 * 
	 * @param managerName
	 * @return
	 */
	public List<String> getApplicationIdList(String managerName) {
		List<String> rtn = new ArrayList<>();

		Map<String, List<SdmlControlSettingInfoResponse>> dispDataMap = getList(managerName);
		for (Map.Entry<String, List<SdmlControlSettingInfoResponse>> entrySet : dispDataMap.entrySet()) {
			List<SdmlControlSettingInfoResponse> list = entrySet.getValue();
			for (SdmlControlSettingInfoResponse info : list) {
				rtn.add(info.getApplicationId());
			}
		}

		return rtn;
	}

	/**
	 * 条件を指定してSDML制御設定の一覧を取得します
	 * 
	 * @param condition
	 * @return
	 */
	public Map<String, List<SdmlControlSettingInfoResponse>> getListWithCondition(Property condition) {
		Map<String, List<SdmlControlSettingInfoResponse>> dispDataMap = new ConcurrentHashMap<>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		// フィルタ条件のマネージャを取得
		String conditionManager = null;
		ArrayList<?> values = PropertyUtil.getPropertyValue(condition, SdmlControlSettingFilterConstant.MANAGER);
		if (values.get(0) instanceof String && ((String) values.get(0)).length() > 0) {
			conditionManager = (String) values.get(0);
		}

		Set<String> managerSet = null;
		if (conditionManager == null || conditionManager.equals("")) {
			// マネージャの指定がない場合は接続しているマネージャ全てが対象
			managerSet = RestConnectManager.getActiveManagerSet();
		} else {
			managerSet = new HashSet<String>();
			managerSet.add(conditionManager);
		}

		SdmlControlSettingFilterInfoRequest filter = SdmlControlSettingFilterPropertyUtil.property2dto(condition);

		for (String managerName : managerSet) {
			try {
				GetSdmlControlSettingListRequest info = new GetSdmlControlSettingListRequest();
				info.setSdmlControlSettingFilterInfo(filter);
				SdmlRestClientWrapper wrapper = SdmlRestClientWrapper.getWrapper(managerName);
				dispDataMap.put(managerName, wrapper.getSdmlControlSettingListByConditionV1(info));
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				logger.warn("getListWithCondition() : " + e.getMessage(), e);
				errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + ", "
						+ HinemosMessage.replace(e.getMessage()));
			}
		}

		// メッセージ表示
		if (0 < errorMsgs.size()) {
			UIManager.showMessageBox(errorMsgs, true);
		}
		return dispDataMap;
	}
}
