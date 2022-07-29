/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.BillingResultResponse.TypeEnum;

import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.platform.PlatformDependent;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

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
		String managerName = cloudScope.getCloudScopes().getHinemosManager().getManagerName();
		CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
		File handler = endpoint.downloadBillingDetailsByCloudScope(
				cloudScope.getId(),
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1);
		PlatformDependent.getPlatformDependent().downloadBillingDetail(
				HandlerUtil.getActiveShell(event),
				TypeEnum.CLOUDSCOPE,
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
