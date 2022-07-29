/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.msgfilter.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.clustercontrol.ClusterControlPlugin;

public class MsgFilterPreferenceInitializerExtension {
	private IMsgFilterPreferenceInitializer initializer = null;

	private static final Log logger = LogFactory.getLog(MsgFilterPreferenceInitializerExtension.class);

	private static final MsgFilterPreferenceInitializerExtension INSTANCE = new MsgFilterPreferenceInitializerExtension();

	public static MsgFilterPreferenceInitializerExtension getInstance() {
		return INSTANCE;
	}

	private MsgFilterPreferenceInitializerExtension() {
		final String pointId = "msgFilterPreferenceInitializer";
		final String elementName = "msgFilterPreferenceInitializer";
		final String InitializerClassAttributeName = "msgFilterPreferenceInitializer";

		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry
				.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + "." + pointId);
		for (IExtension ex : point.getExtensions()) {
			for (IConfigurationElement element : ex.getConfigurationElements()) {
				// 要素名が該当するpluginInfoのIdだった場合、ExtensionTypeの情報を取得
				if (element.getName().equals(elementName)) {
					try {
						if (element.getAttribute(InitializerClassAttributeName) != null) {
							initializer = (IMsgFilterPreferenceInitializer) element
									.createExecutableExtension(InitializerClassAttributeName);
						}
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}
	}

	public void init() {
		try {
			if (initializer == null) {
				return;
			}
			initializer.init();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
