/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ui.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

import com.clustercontrol.startup.ui.StartUpPerspective;

/**
 * Utilities for managing option UI contribution
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class OptionUtil {
	private static Log m_log = LogFactory.getLog(OptionUtil.class);

	private static final String ACTIVITY_ID_PREFIX = "com.clustercontrol.activities.enableOption.";


	/**
	 * エンタプライズオプションのキー名
	 * @see com.clustercontrol.util.KeyCheck#TYPE_ENTERPRISE
	 */
	public static final String TYPE_ENTERPRISE = "enterprise";
	
	/**
	 * クラウド・仮想化オプションのキー名
	 * @see com.clustercontrol.util.KeyCheck#TYPE_XCLOUD
	 */
	public static final String TYPE_XCLOUD = "xcloud";

	public static void enableActivities(IWorkbenchWindow window, Set<String> options) {
		if( null == window ) return;

		IWorkbenchActivitySupport activitySupport = window.getWorkbench().getActivitySupport();
		IActivityManager activityManager = activitySupport.getActivityManager();

		// Activities to enable
		Set<String> ids = new HashSet<>();
		for(String suffix: options){
			String id = ACTIVITY_ID_PREFIX + suffix;
			IActivity activity = activityManager.getActivity(id);
			if(activity.isDefined()){
				ids.add(id);
			}else{
				m_log.warn("Unknown activity: " + id);
			}
		}

		Set<String> enabledIds = getEnabledActivityIds(activityManager);
		m_log.debug("Enabled activities: " + enabledIds);
		m_log.debug("Enable activities: " + ids);
		// Compare with existed and enable only needed
		if(!ids.equals(enabledIds)){
			activitySupport.setEnabledActivityIds(ids);

			// 不整合を防ぐために、すべてのパースペクティブを一回閉じる
			window.getActivePage().closeAllPerspectives(false, false);

			// 代わりにStartUpページを開く
			IPerspectiveDescriptor perspectiveDesc = window.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(StartUpPerspective.ID);
			if(null != perspectiveDesc)
				window.getActivePage().setPerspective(perspectiveDesc);
		}
	}

	private static Set<String> getEnabledActivityIds(IActivityManager activityManager) {
		Set<String> ids = new HashSet<>();
		for(Object raw: activityManager.getEnabledActivityIds()){
			ids.add((String)raw);
		}
		return ids;
	}
}
