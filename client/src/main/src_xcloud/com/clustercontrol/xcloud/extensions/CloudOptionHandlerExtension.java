/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.extensions;

import java.util.Collections;
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

public class CloudOptionHandlerExtension {
	public static class CloudOptionHandlerHolder {
		private String platformId;
		private ICloudOptionHandler cloudOptionHandler;
		
		public CloudOptionHandlerHolder(String platformId, ICloudOptionHandler cloudOptionHandler) {
			this.platformId = platformId;
			this.cloudOptionHandler = cloudOptionHandler;
		}
		
		public String getPlatformId() {
			return platformId;
		}
		public ICloudOptionHandler getCloudOptionHandler() {
			return cloudOptionHandler;
		}
	}
	
	public static final String pointId = "cloudOptionHandler";
	public static final String elementName = "cloudOptionHandler";
	
	public static final String platformIdAttributeName = "platformId";
	public static final String cloudOptionCommandIdAttributeName = "commandId";
	public static final String handlerClassAttributeName = "handlerClass";
	
	private Map<String, Map<String, CloudOptionHandlerHolder>> commandMap = new HashMap<>();
	
	private static CloudOptionHandlerExtension singleton;
	
	private static final Log logger = LogFactory.getLog(CloudOptionHandlerExtension.class);
	
	private CloudOptionHandlerExtension() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + "." + pointId);
		for (IExtension ex: point.getExtensions()) {
			for (IConfigurationElement element: ex.getConfigurationElements()) {
				// 要素名が該当するpluginInfoのIdだった場合、ExtensionTypeの情報を取得
				if(element.getName().equals(elementName)){
					try {
						String platformId = element.getAttribute(platformIdAttributeName);
						String commandId = element.getAttribute(cloudOptionCommandIdAttributeName);
						ICloudOptionHandler handler = null;
						if (element.getAttribute(handlerClassAttributeName) != null)
							handler= (ICloudOptionHandler)element.createExecutableExtension(handlerClassAttributeName);
						
						Map<String, CloudOptionHandlerHolder> map = commandMap.get(commandId);
						if (map == null) {
							map = new HashMap<>();
							commandMap.put(commandId, map);
						}
						map.put(platformId, new CloudOptionHandlerHolder(platformId, handler));
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public static CloudOptionHandlerHolder getCloudOptionHandler(String platformId, String commandId) {
		if (singleton == null)
			singleton = new CloudOptionHandlerExtension();
		
		Map<String, CloudOptionHandlerHolder> map = singleton.commandMap.get(commandId);
		if (map == null)
			return null;
		
		return map.get(platformId);
	}
	
	public static Map<String, CloudOptionHandlerHolder> getCloudOptionHandlers(String commandId) {
		if (singleton == null)
			singleton = new CloudOptionHandlerExtension();
		
		Map<String, CloudOptionHandlerHolder> map = singleton.commandMap.get(commandId);
		if (map != null)
			return Collections.unmodifiableMap(map);
		
		return Collections.emptyMap();
	}
}
