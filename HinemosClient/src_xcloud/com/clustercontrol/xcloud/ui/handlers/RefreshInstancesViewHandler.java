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
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.ui.views.InstancesView;
import com.clustercontrol.xcloud.util.ControlUtil;

public class RefreshInstancesViewHandler extends AbstractHandler implements CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(RefreshInstancesViewHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			InstancesView view = (InstancesView)HandlerUtil.getActiveSite(event).getPage().findView(InstancesView.Id);
			if (view != null)
				view.update();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			ControlUtil.openError(e, msgErrorFinishRefreshView);
		}
		return null;
	}
}
