/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.extensions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.clustercontrol.ClusterControlPlugin;

public class CloudOptionTesterExtension {
	public static final String pointId = "cloudOptionTester";
	public static final String elementName = "cloudOptionTester";
	
	public static final String platformIdAttributeName = "platformId";
	public static final String testerClassAttributeName = "testerClass";
	
	private Map<String, ICloudOptionTester> testerMap = new HashMap<>();
	
	private static CloudOptionTesterExtension singleton;
	
	private static final Log logger = LogFactory.getLog(CloudOptionTesterExtension.class);
	
	private CloudOptionTesterExtension() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + "." + pointId);
		for (IExtension ex: point.getExtensions()) {
			for (IConfigurationElement element: ex.getConfigurationElements()) {
				// 要素名が該当するpluginInfoのIdだった場合、ExtensionTypeの情報を取得
				if(element.getName().equals(elementName)){
					try {
						String platformId = element.getAttribute(platformIdAttributeName);
						testerMap.put(platformId, (ICloudOptionTester)element.createExecutableExtension(testerClassAttributeName));
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public static ICloudOptionTester getCloudOptionTester(String platformId) {
		if (singleton == null)
			singleton = new CloudOptionTesterExtension();
		return singleton.testerMap.get(platformId);
	}
}
