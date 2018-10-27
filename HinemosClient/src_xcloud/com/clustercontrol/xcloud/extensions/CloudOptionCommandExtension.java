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

public class CloudOptionCommandExtension {
	public static final String pointId = "cloudOptionCommand";
	public static final String elementName = "cloudOptionCommand";
	
	public static final String idAttributeName = "id";
	public static final String defaultHandlerClassAttributeName = "defaultHandlerClass";
	
	private Map<String, ICloudOptionHandler> commandMap = new HashMap<>();
	
	private static CloudOptionCommandExtension singleton;
	
	private static final Log logger = LogFactory.getLog(CloudOptionCommandExtension.class);
	
	private CloudOptionCommandExtension() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + "." + pointId);
		for (IExtension ex: point.getExtensions()) {
			for (IConfigurationElement element: ex.getConfigurationElements()) {
				// 要素名が該当するpluginInfoのIdだった場合、ExtensionTypeの情報を取得
				if(element.getName().equals(elementName)){
					try {
						String commandId = element.getAttribute(idAttributeName);
						
						String handlerClassName = element.getAttribute(defaultHandlerClassAttributeName);
						if (handlerClassName != null) {
							ICloudOptionHandler handler = (ICloudOptionHandler)element.createExecutableExtension(defaultHandlerClassAttributeName);
							commandMap.put(commandId, handler);
						}
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public static ICloudOptionHandler getDefaultCloudOptionHandlers(String commandId) {
		if (singleton == null)
			singleton = new CloudOptionCommandExtension();
		return singleton.commandMap.get(commandId);
	}
}
