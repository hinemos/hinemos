/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.preference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.infra.view.action.UseNodePropManagementAction;
import com.clustercontrol.infra.view.action.UseNodePropModuleAction;

/**
 * 環境構築設定機能のプリファレンスの初期化<BR>
 *
 * @version 6.1.0
 */
public class InfraInitializer extends AbstractPreferenceInitializer {

	private static final Log log = LogFactory.getLog( InfraInitializer.class );

	public static final int INFRA_MODULE_NODE_INPUT_TYPE_DEFAULT = 0; // 環境構築設定モジュールのログイン情報設定種別(デフォルト)
	public static final int INFRA_MANAGEMENT_NODE_INPUT_TYPE_DEFAULT = 0; // 環境構築設定モジュールのログイン情報設定種別(デフォルト)

	/**
	 * プリファレンスの初期化
	 */
	@Override
	public void initializeDefaultPreferences() {
		log.debug("initializeDefaultPreferences()");

		IPreferenceStore store = ClusterControlPlugin.getDefault()
				.getPreferenceStore();

		log.debug("initializeDefaultPreferences() " + UseNodePropManagementAction.P_INFRA_MANAGEMENT_NODE_INPUT_TYPE + "=" + INFRA_MANAGEMENT_NODE_INPUT_TYPE_DEFAULT);
		store.setDefault(UseNodePropManagementAction.P_INFRA_MANAGEMENT_NODE_INPUT_TYPE, INFRA_MANAGEMENT_NODE_INPUT_TYPE_DEFAULT);

		log.debug("initializeDefaultPreferences() " + UseNodePropModuleAction.P_INFRA_MODULE_NODE_INPUT_TYPE + "=" + INFRA_MODULE_NODE_INPUT_TYPE_DEFAULT);
		store.setDefault(UseNodePropModuleAction.P_INFRA_MODULE_NODE_INPUT_TYPE, INFRA_MODULE_NODE_INPUT_TYPE_DEFAULT);
	}

}
