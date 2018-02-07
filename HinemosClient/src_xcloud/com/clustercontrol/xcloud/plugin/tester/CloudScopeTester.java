/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.tester;

import java.util.regex.Pattern;

import org.eclipse.core.expressions.PropertyTester;

import com.clustercontrol.xcloud.extensions.CloudOptionCommandExtension;
import com.clustercontrol.xcloud.extensions.CloudOptionHandlerExtension;
import com.clustercontrol.xcloud.extensions.ICloudOptionHandler;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;

public class CloudScopeTester extends PropertyTester {
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		ICloudScope cloudScope = (ICloudScope)receiver;
		switch(property) {
		case "platformId":
			{
				String platformId = cloudScope.getPlatformId();
				if (platformId == null) {
					return expectedValue == null;
				} else {
					return expectedValue != null ? Pattern.matches((String)expectedValue, platformId): false;
				}
			}
		case "handler":
			{
				ICloudOptionHandler handler = null;
				if (cloudScope != null) {
					CloudOptionHandlerExtension.CloudOptionHandlerHolder handlerHolder = CloudOptionHandlerExtension.getCloudOptionHandler(cloudScope.getPlatformId(), expectedValue.toString());
					if (handlerHolder != null) {
						if (handlerHolder.getCloudOptionHandler() == null) {
							// 未サポートのコマンドと判断。
							CloudOptionSourceProvider.setActiveOptionHandlerToProvider(null);
							return false;
						} else {
							handler = handlerHolder.getCloudOptionHandler();
						}
					}
				}
				
				if (handler == null)
					handler = CloudOptionCommandExtension.getDefaultCloudOptionHandlers(expectedValue.toString());
				
				CloudOptionSourceProvider.setActiveOptionHandlerToProvider(new CloudOptionSourceProvider.OptionHandlerHolder(expectedValue.toString(), handler));
				return handler != null;
			}
		}
		return false;
	}
}
