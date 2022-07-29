/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobInfoResponse;
import org.openapitools.client.model.JobNodeDetailResponse;
import org.openapitools.client.model.JobOperationRequest;
import org.openapitools.client.model.JobRpaInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.jobmanagement.composite.NodeDetailComposite;
import com.clustercontrol.jobmanagement.util.JobOperationRequestWrapper;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.JobNodeDetailView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

public class TakeScreenshotJobNodeDetailAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	protected static Log m_log = LogFactory.getLog(TakeScreenshotJobNodeDetailAction.class);
	/** アクションID */
	public static final String ID = TakeScreenshotJobNodeDetailAction.class.getName();
	/** ビュー */
	private IWorkbenchPart viewPart;
	/** マネージャ名 */
	private String m_managerName = null;
	/** セッションID */
	private String m_sessionId = null;
	/** ジョブユニットID */
	private String m_jobunitId = null;
	/** ジョブID */
	private String m_jobId = null;
	/** ファシリティID */
	private String m_facilityId = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);
		JobNodeDetailView jobNodeDetailView = null;
		try {
			jobNodeDetailView = (JobNodeDetailView) viewPart.getAdapter(JobNodeDetailView.class);
		} catch (Exception e) {
			m_log.info("execute " + e.getMessage());
			return null;
		}

		if (jobNodeDetailView == null) {
			m_log.info("execute: job node detail view is null");
		} else {
			NodeDetailComposite nodeDetailComposite = jobNodeDetailView.getComposite();
			m_managerName = nodeDetailComposite.getManagerName();
			m_sessionId = nodeDetailComposite.getSessionId();
			m_jobunitId = nodeDetailComposite.getJobunitId();
			m_jobId = nodeDetailComposite.getJobId();
			m_facilityId = nodeDetailComposite.getFacilityId();
			// ダイアログ表示
			MessageDialog dialog = new MessageDialog(null, Messages.getString("confirmed"), null,
					Messages.getString("message.job.rpa.1"), MessageDialog.CONFIRM,
					new String[] { Messages.getString("ok"), Messages.getString("cancel") }, 0);
			if (dialog.open() == IDialogConstants.OK_ID) {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(m_managerName);
				JobOperationRequestWrapper requestSrc = new JobOperationRequestWrapper();
				requestSrc.setSessionId(m_sessionId);
				requestSrc.setJobunitId(m_jobunitId);
				requestSrc.setJobId(m_jobId);
				requestSrc.setFacilityId(m_facilityId);
				requestSrc.setControl(JobOperationRequest.ControlEnum.RPA_SCREENSHOT);
				try {
					JobOperationRequest request = new JobOperationRequest();
					RestClientBeanUtil.convertBean(requestSrc, request);
					wrapper.operationSessionNode(requestSrc.getSessionId(), requestSrc.getJobunitId(), requestSrc.getJobId(), requestSrc.getFacilityId(),
							request);
					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.job.rpa.2"));
				} catch (JobInfoNotFound | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
					MessageDialog.openError(null, Messages.getString("error"),
							Messages.getString("message.unexpected_error"));
				}

			}

		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if (null != window) {
			IWorkbenchPage page = window.getActivePage();
			if (null != page) {
				IWorkbenchPart part = page.getActivePart();
				boolean editEnable = false;
				if (part instanceof JobNodeDetailView) {
					JobNodeDetailView view = (JobNodeDetailView) part;
					// RPAシナリオジョブ種別が直接実行の場合のみ有効化する
					JobRpaInfoResponse.RpaJobTypeEnum rpaJobType = view.getComposite().getRpaJobType();
					JobNodeDetailResponse.StatusEnum status = view.getComposite().getStatus();
					if (view.getSelectedNum() > 0 && rpaJobType != null && rpaJobType == JobRpaInfoResponse.RpaJobTypeEnum.DIRECT
							&& status == JobNodeDetailResponse.StatusEnum.RUNNING) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
