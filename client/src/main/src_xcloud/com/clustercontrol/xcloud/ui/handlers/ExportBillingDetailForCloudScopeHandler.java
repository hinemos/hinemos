/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.util.Calendar;
import java.util.Date;

import javax.activation.DataHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.TargetType;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.platform.PlatformDependent;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView;

public class ExportBillingDetailForCloudScopeHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	@Override
	public Object internalExecute(ExecutionEvent event) throws Exception {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final ICloudScope cloudScope = (ICloudScope)selection.getFirstElement();

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());

		// Set date according to what is showing in BillingDetailsView, otherwise just set as current month
		IViewPart view = HandlerUtil.getActiveSite(event).getPage().findView(BillingDetailsView.Id);
		if( null != view && view instanceof BillingDetailsView){
			BillingDetailsView.State state = ((BillingDetailsView)view).getCurrentState();
			if( null != state ){
				cal.set(Calendar.YEAR, state.year);
				cal.set(Calendar.MONTH, state.month-1);
			}
		}
		// TODO Have better let user export all data instead of only monthly
		DataHandler handler = cloudScope.getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class).downloadBillingDetailsByCloudScope(
				cloudScope.getId(),
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1);

		PlatformDependent.getPlatformDependent().downloadBillingDetail(
				HandlerUtil.getActiveShell(event),
				TargetType.CLOUD_SCOPE,
				cloudScope.getId(),
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1,
				handler);
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishExportBillingDetailForCloudScope;
	}
}
