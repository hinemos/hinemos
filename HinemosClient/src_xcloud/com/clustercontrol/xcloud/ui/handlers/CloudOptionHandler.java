/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;

public class CloudOptionHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CloudOptionSourceProvider.OptionHandlerHolder handler = CloudOptionSourceProvider.getActiveOptionHandlerToProvider();
		if (handler != null)
			return handler.getCloudOptionHandler().execute(event);
		return null;
	}
}
