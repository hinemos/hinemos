/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.preference;

import com.clustercontrol.utility.util.ClientPathUtil;

/**
 * 設定ページ定数<BR>
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public final class PreferencePageConstant {
	
	public static final String KEY_XML= "xmlDir";
	public static final String VALUE_XML= ClientPathUtil.getDefaultXMLPath();

	public static final String KEY_DIFF_XML = "xmlDiffDir";
	public static final String VALUE_DIFF_XML = ClientPathUtil.getDefaultXMLDiffPath();
	public static final String KEY_DIFF_MODE = "xmlDiffMode";
	public static final String VALUE_DIFF_MODE = "false";

	public static final String VALUE_INFRA= "infraFile";
	
	public static final String VALUE_JOBMAP_IMAGE_FOLDER= "jobmapImage";
	public static final String VALUE_BACKUP_FOLDER= "backup";
	public static final String VALUE_NODEMAP_BG_FOLDER= "nodemapBgImage";
	public static final String VALUE_NODEMAP_ICON_FOLDER= "nodemapIconImage";
	public static final String KEY_BACKUP_IMPORT = "backup.import";
	public static final String KEY_BACKUP_EXPORT = "backup.export";
	public static final String KEY_BACKUP_CLEAR = "backup.delete";
	public static final String DEFAULT_VALUE_BACKUP_IMPORT = Boolean.TRUE.toString();
	public static final String DEFAULT_VALUE_BACKUP_EXPORT = Boolean.TRUE.toString();
	public static final String DEFAULT_VALUE_BACKUP_CLEAR = Boolean.TRUE.toString();

}