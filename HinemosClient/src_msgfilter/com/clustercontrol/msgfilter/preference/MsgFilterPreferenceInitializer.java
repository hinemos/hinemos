/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.msgfilter.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.clustercontrol.msgfilter.extensions.MsgFilterPreferenceInitializerExtension;

public class MsgFilterPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		MsgFilterPreferenceInitializerExtension.getInstance().init();
	}
}
