/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class CloudConstants {
	public final static String time_format_string = "%04d-%02d-%02d %02d:00:00";
	public final static Pattern time_pattern = Pattern.compile("^[0-9]{4}+-[0-9]{2}+-[0-9]{2}+ [0-9]{2}+:[0-9]{2}+:[0-9]{2}+$");

	// 機能ID
	public static final String FT_CloudServiceBilling = "com.clustercontrol.cloud.base.function.CloudServiceBilling";
	
	// Hinemos のノードに指定するリソース種別
	public static final String NT_Resource = "Resource";
	public static final String NT_Entity = "Entity";
	public static final String NT_Server = "Server";
	
	// ルートスコープ名 ファシリティ ID
	public final static String publicRootId = "_PUBLIC_CLOUD";
	public final static String privateRootId = "_PRIVATE_CLOUD";

	// ルートスコープ名 リソース ID
	public final static String publicRootNameId = "publicCloudRootScopeName";
	public final static String privateRootNameId = "privateCloudRootScopeName";
	public final static String allNodeScopeNameId = "allNodeScopeName";
	
	// 拡張プロパティ名
	public final static String EPROP_CloudScope = "cloud_cloudScope";
	public final static String EPROP_Platform = "cloud_platform";
	public final static String EPROP_Location = "cloud_location";
	public final static String EPROP_Instance = "cloud_instance";
	public final static String EPROP_Entity = "cloud_entity";

	// ノードを追加した際のイベント
	public static final String Node_Instance = "Node_Instance";
	public static final String Node_Server = "Node_Server";
	public static final String Node_CloudScope = "Node_CloudScope";
	public static final String Scope_Public_Root = "Scope_Public_Root";
	public static final String Scope_Private_Root = "Scope_Private_Root";
	public static final String Scope_CloudScope = "Scope_CloudScope";
	public static final String Scope_Location = "Scope_Location";
	public static final String Scope_Folder = "Scope_Folder";
	public static final String Scope_All_Node = "Scope_AllNode";
	
	// イベント
	public static final String Event_CloudScope = "Event_CloudScope";
	public static final String Event_LoginUserAccount = "Event_LoginUserAccount";
	public static final String Event_LoginUser = "Event_LoginUser";
	public static final String Event_Storage = "Event_Storage";
	public static final String Event_Instance = "Event_Instance";
	public static final String Event_BillingAlarm = "Event_BillingAlarm";

	// クラウドのリソース階層に紐づく情報の種別
	public static final String HINEMOS_FACILITY = "hinemos_facility";
	public static final String HINEMOS_SCOPE = "hinemos_scope";
	public static final String HINEMOS_NODE = "hinemos_node";
	public static final String CLOUD_LOCATION = "cloud_location";
	public static final String CLOUD_INSTANCE = "cloud_instance";
	
	// クラウド管理オプション関連パス
	public static final String PATH_SCRIPTS = "bin";
	public static final String PATH_LIB = "lib/xcloud";
	public static final String PATH_ETC = "etc/xcloud";
	public static final String PATH_SBIN = "sbin/xcloud";
	public static final String PATH_VAR = "var/xcloud";
	
	// パスワードを暗号化するためのキー
	public static final String cryptkeyFileRelativePath = "etc/db_crypt.key";
	public static final String cryptkeyName = "cryptkey";

	// クラウドオプションのプラグインID
	public static final String PLUGIN_ID = "CLOUD";

	public static DateFormat createTimeFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
}
