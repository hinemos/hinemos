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
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.ui.views.AbstractCloudViewPart;
import com.clustercontrol.xcloud.util.ControlUtil;

public class RefreshViewHandler extends AbstractHandler implements CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(RefreshViewHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
			if (part instanceof AbstractCloudViewPart) {
				AbstractCloudViewPart view = ((AbstractCloudViewPart)part);
				view.update();
			}
		} catch (CloudModelException e) {
			Throwable twe = e;
			if (e.getCause() != null)
				twe = e.getCause();
			logger.error(twe.getMessage(), twe);
			
			ControlUtil.openError(twe, msgErrorFinishRefreshView);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			ControlUtil.openError(e, msgErrorFinishRefreshView);
		}
		return null;
	}
}
