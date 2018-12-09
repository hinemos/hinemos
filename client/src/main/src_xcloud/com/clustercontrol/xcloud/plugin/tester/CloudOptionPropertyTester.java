/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.tester;

import java.util.Optional;

import org.eclipse.core.expressions.PropertyTester;

import com.clustercontrol.xcloud.extensions.CloudOptionCommandExtension;
import com.clustercontrol.xcloud.extensions.CloudOptionHandlerExtension;
import com.clustercontrol.xcloud.extensions.CloudOptionTesterExtension;
import com.clustercontrol.xcloud.extensions.ICloudOptionHandler;
import com.clustercontrol.xcloud.extensions.ICloudOptionTester;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;

public class CloudOptionPropertyTester extends PropertyTester {
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		Optional<Boolean> resultOpt = Optional.empty();
		switch(property) {
		case "handler":
			{
				ICloudScope cloudScope = null;
				try {
					cloudScope = (ICloudScope)((IElement)receiver).getAdapter(ICloudScope.class);
				} catch (NullPointerException e) {
				}
				
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
				resultOpt = Optional.of(handler != null);
			}
			break;
		case "owner":
			{
				resultOpt = Optional.of(((IElement)receiver).getOwner() != null);
			}
			break;
		}
		
		Optional<Boolean> resourceOpt = resourceTest(receiver, property, args, expectedValue);
		resultOpt = resultOpt.isPresent() ? (resourceOpt.isPresent() ? Optional.of(resultOpt.get() && resourceOpt.get()): resultOpt): resourceOpt;
		
		Optional<Boolean>  pluginOpt = pluginTest(receiver, property, args, expectedValue);
		resultOpt = resultOpt.isPresent() ? (pluginOpt.isPresent() ? Optional.of(resultOpt.get() && pluginOpt.get()): resultOpt): pluginOpt;
		
		return resultOpt.orElse(false);
	}

	protected Optional<Boolean> resourceTest(Object receiver, String property, Object[] args, Object expectedValue) {
		return Optional.empty();
	}
	
	protected Optional<Boolean> pluginTest(Object receiver, String property, Object[] args, Object expectedValue) {
		ICloudScope cloudScope = null;
		try {
			cloudScope = (ICloudScope)((IElement)receiver).getAdapter(ICloudScope.class);
		} catch (NullPointerException e) {
		}
		if (cloudScope == null)
			return Optional.empty();
		ICloudOptionTester tester = CloudOptionTesterExtension.getCloudOptionTester(cloudScope.getPlatformId());
		if (tester == null)
			return Optional.empty();
		return tester.pluginTest(receiver, property, args, expectedValue);
	}
}
