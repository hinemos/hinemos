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
import com.clustercontrol.xcloud.extensions.ICloudOptionHandler;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView.DataHolder;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ShowBillingDetailForCloudScopeHandler implements ICloudOptionHandler, CloudStringConstants {
	public static final String ID = ShowBillingDetailForCloudScopeHandler.class.getName();
	
	private static final Log logger = LogFactory.getLog(ShowBillingDetailForCloudScopeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final ICloudScope cloudScope = (ICloudScope)selection.getFirstElement();
		
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
									String managerName = cloudScope.getCloudScopes().getHinemosManager().getManagerName();
									CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
									result = endpoint.getBillingDetailsByCloudScope(cloudScope.getId(), year, month);
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
								String managerName = cloudScope.getCloudScopes().getHinemosManager().getManagerName();
								CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
								return endpoint.downloadBillingDetailsByCloudScope(cloudScope.getId(), year, month);
							} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
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
