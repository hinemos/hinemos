/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.clustercontrol.xcloud.extensions.ICloudOptionHandler;
import com.clustercontrol.xcloud.util.ControlUtil;

public abstract class AbstractCloudOptionHandler implements ICloudOptionHandler{
	
	private static final Log logger = LogFactory.getLog(AbstractCloudOptionHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			return internalExecute(event);
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			
			ControlUtil.openError(e, getErrorMessage());
		}
		afterCall();
		return null;
	}
	
	public abstract Object internalExecute(ExecutionEvent event) throws Exception;
	
	protected void afterCall() {
	};

	protected abstract String getErrorMessage();
}
