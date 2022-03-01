/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISession;

/**
 * プロパティを保持するクラス<BR>
 *
 */
// TODO 改善#3448 RCPとRAPで実装を分ける必要はないはず。2つのjavaファイルに同じ定数定義があるのはよろしくない。
public class FilterPropertyCache {

	public static final String APPROVAL_FILTER_DIALOG_PROPERTY = "approvalFilterDialogProperty";
	public static final String JOBKICK_FILTER_DIALOG_PROPERTY  =  "jobKickFilterDialogProperty";
	public static final String JOBLINKMESSAGE_FILTER_DIALOG_PROPERTY  =  "jobLinkMessageFilterDialogProperty";
	public static final String PLAN_FILTER_DIALOG_PROPERTY     =     "planFilterDialogProperty";
	public static final String MONITOR_FILTER_DIALOG_PROPERTY  =  "monitorFilterDialogProperty";
	public static final String NODELIST_FIND_DIALOG_PROPERTY   =   "nodeListFindDialogProperty";
	public static final String NODE_FILTER_DIALOG_PROPERTY     =     "nodeFilterDialogProperty";
	public static final String SDML_CONTROL_SETTING_FILTER_DIALOG_PROPERTY = "sdmlControlSettingFilterDialogProperty";
	public static final String RPA_SCENARIO_FILTER_DIALOG_PROPERTY  = "rpaScenarioFilterDialogProperty";

	private Map<UISession, Object> filterPropertyCacheMap = new ConcurrentHashMap<>();

	public void initFilterPropertyCache(String propertyName,Object property) {
		filterPropertyCacheMap.put(RWT.getUISession(), property);
	}

	public Object getFilterPropertyCache(String propertyName) {
		return filterPropertyCacheMap.get(RWT.getUISession());
	}

}
