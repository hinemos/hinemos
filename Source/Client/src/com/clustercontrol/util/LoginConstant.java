/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;


public final class LoginConstant {

	public static final String KEY_LOGIN_STATUS_NUM = "numOfLoginStatus";
	public static final String KEY_LOGIN_STATUS_UID = "LoginStatusUid";
	public static final String KEY_LOGIN_STATUS_URL = "LoginStatusUrl";
	public static final String KEY_LOGIN_STATUS_MANAGERNAME = "LoginStatusManagerName";
	public static final String KEY_LOGIN_STATUS_ACCESSPOINT = "LoginStatusAccessPoint";

	public static final String KEY_URL = "Url";
	public static final String KEY_URL_NUM = "numOfUrlHistory";

	public static final String VALUE_UID = "hinemos";
	public static final String VALUE_URL = "http://localhost:8080/HinemosWeb/";

	public static final String KEY_INTERVAL = "managerPollingInterval";
	public static final int VALUE_INTERVAL = 1; //マネージャへの疎通(Dummy)ポーリング周期（分）

	public static final String KEY_HTTP_REQUEST_TIMEOUT = "httpRequestTimeout";	// Utilityオプションからも使用されています。
	public static final int VALUE_HTTP_REQUEST_TIMEOUT = 60000; // ms

	public static final String KEY_PROXY_ENABLE = "proxyEnable";
	public static final boolean VALUE_PROXY_ENABLE = false;
	public static final String KEY_PROXY_HOST = "proxyHost";
	public static final String VALUE_PROXY_HOST = "";
	public static final String KEY_PROXY_PORT = "proxyPort";
	public static final int VALUE_PROXY_PORT = 8080;
	public static final String KEY_PROXY_USER = "proxyUser";
	public static final String VALUE_PROXY_USER = "";
	public static final String KEY_PROXY_PASSWORD = "proxyPassword";
	public static final String VALUE_PROXY_PASSWORD = "";
	
	/** Auto-login */
	public static final String ENV_HINEMOS_MANAGER_URL = "HINEMOS_MANAGER_URL";
	public static final String ENV_HINEMOS_MANAGER_USER = "HINEMOS_USER";
	public static final String ENV_HINEMOS_MANAGER_PASS = "HINEMOS_PASS";
	
	public static final String KEY_BASIC_AUTH = "BasicAuth";
	public static final String KEY_URL_LOGIN_URL = "LoginUrl";
	public static final String KEY_URL_UID = "Uid";
	public static final String KEY_URL_MANAGER_NAME = "ManagerName";

	public static final String URL_HINEMOS = "hinemos";
	public static final String URL_ACCOUNT = "account";
	public static final String URL_CALENDAR = "calendar";
	public static final String URL_JOB_HISTORY = "job_history";
	public static final String URL_JOB_SETTING = "job_setting";
	public static final String URL_STARTUP = "startup";
	public static final String URL_MAINTENANCE = "maintenance";
	public static final String URL_REPOSITORY = "repository";
	public static final String URL_COLLECT = "collect";
	public static final String URL_APPROVAL = "approval";
	public static final String URL_INFRA = "infra";
	public static final String URL_MONITOR_HISTORY = "monitor_history";
	public static final String URL_MONITOR_SETTING = "monitor_setting";
	public static final String URL_HUB = "hub";
	public static final String URL_XCLOUD_BILLING = "xcloud_billing";
	public static final String URL_XCLOUD_COMPUTE = "xcloud_compute";
	public static final String URL_XCLOUD_NETWORK = "xcloud_network";
	public static final String URL_XCLOUD_SERVICE = "xcloud_service";
	public static final String URL_XCLOUD_STORAGE = "xcloud_storage";
	public static final String URL_JOBMAP_EDITOR = "jobmap_editor";
	public static final String URL_JOBMAP_HISTORY = "jobmap_history";
	public static final String URL_NODEMAP = "nodemap";
	public static final String URL_SETTING_TOOLS = "setting_tools";
	public static final String URL_REPORTING = "reporting";
	public static final String URL_RPA_SETTING = "rpa_setting";
	public static final String URL_RPA_SCENARIO_OPERATION_RESULT = "rpa_scenario_operation_result";
	public static final String URL_MSG_FILTER = "msg_filter";
}
