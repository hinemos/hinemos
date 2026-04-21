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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.startup.ui.StartUpPerspective;
import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;

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
	
	/**
	 * RPA無効
	 */
	public static final String TYPE_NORPA = "norpa";
	
	/**
	 * プリファレンスストア_有効オプションキー名
	 * @see com.clustercontrol.util.KeyCheck#TYPE_ENTERPRISE
	 */
	private static final String PRE_KEY_LAST_ACT_KEY = "lastActivityKeys";
	

	public static void enableActivities(IWorkbenchWindow window, Set<String> options) {
		if( null == window ) return;

		IWorkbenchActivitySupport activitySupport = window.getWorkbench().getActivitySupport();
		IActivityManager activityManager = activitySupport.getActivityManager();
		
		// Activities to enable
		boolean norpa = false;
		Set<String> ids = new HashSet<>();
		for(String suffix: options){
			// RPAの無効を指定されているか
			if (TYPE_NORPA.equals(suffix)) {
				norpa = true;
				continue;
			}
			
			String id = ACTIVITY_ID_PREFIX + suffix;
			IActivity activity = activityManager.getActivity(id);
			if(activity.isDefined()){
				ids.add(id);
			}else{
				if (!id.endsWith(ActivationKeyConstant.EVALUATION_SUFFIX) 
						&& !id.endsWith(ActivationKeyConstant.EVALUATION_EXPIRED_SUFFIX)) {
					m_log.warn("Unknown activity: " + id);
				}
			}
		}
		// エンタープライズ機能が有効で、RPAの無効が指定されていない場合、RPA関連を有効にする
		if (ids.stream().anyMatch(id->id.endsWith(TYPE_ENTERPRISE)) && !norpa) {
			String id = ACTIVITY_ID_PREFIX + "rpa";
			IActivity activity = activityManager.getActivity(id);
			if(activity.isDefined()){
				ids.add(id);
			}
		}
		
		// プリファレンスストアから前回ログイン時の有効オプションキー情報を取得する。
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		String enabledIds = store.getString(PRE_KEY_LAST_ACT_KEY);
		
		m_log.debug("Enabled activities: [" + enabledIds + "]");
		m_log.debug("Enable activities: " + ids);

		// Compare with existed and enable only needed
		activitySupport.setEnabledActivityIds(ids);

		// 有効化オプションをStringに変換 
		String idsStr = String.join(",", ids);
		
		if(!idsStr.equals(enabledIds)){
			// プリファレンスストアに有効化オプションを登録する。
			store.setValue(PRE_KEY_LAST_ACT_KEY, idsStr);

			// 不整合を防ぐために、開いているパースペクティブをすべて閉じる
			for (IPerspectiveDescriptor desc : window.getActivePage().getOpenPerspectives()) {
				if (StartUpPerspective.ID.equals(desc.getId())) {
					continue;
				}
				window.getActivePage().closePerspective(desc, false, false);
			}

			// デフォルトのStartUpページを開く
			IPerspectiveDescriptor perspectiveDesc = window.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(StartUpPerspective.ID);
			if(null != perspectiveDesc) {
				window.getActivePage().setPerspective(perspectiveDesc);
				window.getActivePage().resetPerspective();
			}
		} else {
			// 有効化オプションを更新しない場合、パースペクティブのリセットは行われないので現在開いているビューをチェックする
			// 現在開いているのが設定インポートエクスポートビューの場合、ビューを更新した後、プルダウンにマネージャを自動で選択させる
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IViewPart viewPart = page.findView(ImportExportExecView.ID);
				if (viewPart instanceof ImportExportExecView) {
					((ImportExportExecView) viewPart).updateForLogin();
					if (m_log.isDebugEnabled()) {
						m_log.debug("Refresh ImportExportExecView");
					}
				}
			}
		}
	}
}
