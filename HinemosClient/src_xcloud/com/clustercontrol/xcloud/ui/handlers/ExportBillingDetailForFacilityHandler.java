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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.BillingResultResponse.TypeEnum;

import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.BillingMonitor;
import com.clustercontrol.xcloud.platform.PlatformDependent;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ExportBillingDetailForFacilityHandler extends AbstractHandler implements CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(ExportBillingDetailForFacilityHandler.class);
	
	public static final String ID = ExportBillingDetailForFacilityHandler.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final BillingMonitor billingMonitor = (BillingMonitor)selection.getFirstElement();
		
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
		try {
			String managerName = billingMonitor.getBillingMonitors().getHinemosManager().getManagerName();
			CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
			File handler = endpoint.downloadBillingDetailsByFacility(
					billingMonitor.getMonitorInfo().getFacilityId(),
					cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + 1);

			PlatformDependent.getPlatformDependent().downloadBillingDetail(
					HandlerUtil.getActiveShell(event),
					TypeEnum.FACILITY,
					billingMonitor.getMonitorInfo().getFacilityId(),
					cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + 1,
					handler);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			ControlUtil.openError(e, msgErrorFinishExportBillingDetailForFacility);
		}
		return null;
	}
}
