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
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.BillingResultResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.BillingMonitor;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView.DataHolder;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ShowBillingDetailForFacilityHandler extends AbstractHandler implements CloudStringConstants {
	public static final String ID = ShowBillingDetailForFacilityHandler.class.getName();
	
	private static final Log logger = LogFactory.getLog(ShowBillingDetailForFacilityHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final BillingMonitor billingMonitor = (BillingMonitor)selection.getFirstElement();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		
		BillingDetailsView view = (BillingDetailsView)HandlerUtil.getActiveSite(event).getPage().findView(BillingDetailsView.Id);
		
		if (view != null) {
			view.update(new BillingDetailsView.DataProvider() {
				@Override
				public DataHolder getData(final int year, final int month) {
					return new BillingDetailsView.DataHolder() {
						private BillingResultResponse result = null;
						
						@Override
						public BillingResultResponse getData() {
							try {
								if (result == null) {
									String managerName = billingMonitor.getBillingMonitors().getHinemosManager().getManagerName();
									CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
									result = endpoint.getBillingDetailsByFacility(billingMonitor.getMonitorInfo().getFacilityId(), year, month);
								}
								return result;
							} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
								logger.error(e.getMessage(), e);
								
								ControlUtil.openError(e, msgErrorFinishShowBillingDetail);
								throw new IllegalStateException(e);
							}
						}
						@Override
						public File getFile() {
							try {
								String managerName = billingMonitor.getBillingMonitors().getHinemosManager().getManagerName();
								CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
								return endpoint.downloadBillingDetailsByCloudScope(billingMonitor.getMonitorInfo().getFacilityId(), year, month);
							} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed |  HinemosUnknown e) {
								logger.error(e.getMessage(), e);
								
								ControlUtil.openError(e, msgErrorFinishShowBillingDetail);
								throw new IllegalStateException(e);
							}
						}
						@Override
						public int getYear() {
							return year;
						}
						@Override
						public int getMonth() {
							return month;
						}
					};
				}
			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
		}
		return null;
	}
}
