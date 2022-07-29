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
import com.clustercontrol.xcloud.model.CloudModelException;

public class CloudModelContentProviderExtension {
	private static final String pointId = "cloudModelContentProvider";
	private static final String elementName = "cloudModelContentProvider";
	private static final String providerClassAttributeName = "providerClass";
	private static final String platformIdAttributeName = "platformId";
	
	private Map<String, ICloudModelContentProvider> providerMap = new HashMap<>();
	
	private static CloudModelContentProviderExtension singleton;
	
	private static final Log logger = LogFactory.getLog(CloudModelContentProviderExtension.class);
	
	private CloudModelContentProviderExtension() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + "." + pointId);
		for (IExtension ex: point.getExtensions()) {
			for (IConfigurationElement element: ex.getConfigurationElements()) {
				// 要素名が該当するpluginInfoのIdだった場合、ExtensionTypeの情報を取得
				if(element.getName().equals(elementName)){
					try {
						String platformId = element.getAttribute(platformIdAttributeName);
						ICloudModelContentProvider provider = (ICloudModelContentProvider)element.createExecutableExtension(providerClassAttributeName);
						
						providerMap.put(platformId, provider);
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public static ICloudModelContentProvider getModelContentProvider(String platformId) {
		if (singleton == null) {
			singleton = new CloudModelContentProviderExtension();
		}
		ICloudModelContentProvider provider = singleton.providerMap.get(platformId);
		if (provider == null) {
			throw new CloudModelException("Not found a provider. platformid=" + platformId);
		}
		return provider;
	}
}
