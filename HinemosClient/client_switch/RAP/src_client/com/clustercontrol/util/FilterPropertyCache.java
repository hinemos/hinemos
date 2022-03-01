/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import org.eclipse.rap.rwt.RWT;

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
	public static final String MSG_FILTER_RULEFILE_FILTER_DIALOG_PROPERTY  = "msgFilterRulefileFilterDialogProperty";
	public static final String MSG_FILTER_RULEBASE_FILTER_DIALOG_PROPERTY  = "msgFilterRulebaseFilterDialogProperty";
	public static final String MSG_FILTER_RULEVAR_FILTER_DIALOG_PROPERTY  = "msgFilterRulevarFilterDialogProperty";
	public static final String MSG_FILTER_MESSAGEVIEW_FILTER_DIALOG_PROPERTY = "msgFilterMessageviewFilterDialogProperty";

	public void initFilterPropertyCache(String propertyName,Object property) {
		RWT.getUISession().setAttribute(propertyName, property);
	}

	public Object getFilterPropertyCache(String propertyName) {
		return RWT.getUISession().getAttribute(propertyName);
	}

}
