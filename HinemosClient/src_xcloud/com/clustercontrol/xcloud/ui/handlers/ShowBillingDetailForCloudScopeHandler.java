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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.ws.xcloud.BillingResult;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.ICloudOptionHandler;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView;
import com.clustercontrol.xcloud.ui.views.BillingDetailsView.DataHolder;
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
						private BillingResult result = null;
						
						@Override
						public BillingResult getData() {
							try {
								if (result == null) {
									result = cloudScope.getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class).getBillingDetailsByCloudScope(cloudScope.getId(), year, month);
								}
								return result;
							} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
								logger.error(e.getMessage(), e);
								
								ControlUtil.openError(e, msgErrorFinishShowBillingDetail);
								throw new IllegalStateException(e);
							}
						}
						@Override
						public DataHandler getDataHandler() {
							try {
								return cloudScope.getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class).downloadBillingDetailsByCloudScope(cloudScope.getId(), year, month);
							} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
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
			HandlerUtil.getActiveSite(event).getPage().activate(view);
		}
		return null;
	}
}
