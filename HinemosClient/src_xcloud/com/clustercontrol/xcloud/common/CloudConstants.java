/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.common;

public interface CloudConstants {
	public static final String PRIVATE_CLOUD_SCOPE_ID = "_PRIVATE_CLOUD";
	public static final String PUBLIC_CLOUD_SCOPE_ID = "_PUBLIC_CLOUD";

	public static final String ACTION_CONSTANTS_GET_WINDOWS_PASSWORD_TARGET_PLATFORM = "WINDOWS";
	public static final String ACTION_CONSTANTS_GET_WINDOWS_PASSWORD_CONSTRAINT_FILE_TYPE = "*.pem";
	
	public static final String TREE_ID_PREFIX = "[";
	public static final String TREE_ID_SUFFIX = "]";
	
	public static final MessageManager bundle_messages = MessageManager.getInstance("messages_client");
	public static final MessageManager bundle_plugin = MessageManager.getInstance("plugin");
	
	public final static String backup_instanceName = "instanceName";
	public final static String backup_memo = "memo";
	public final static String backup_tags = "tags";
}
