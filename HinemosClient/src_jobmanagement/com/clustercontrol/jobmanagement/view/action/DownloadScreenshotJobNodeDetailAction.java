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
import org.openapitools.client.model.JobRpaInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.jobmanagement.composite.NodeDetailComposite;
import com.clustercontrol.jobmanagement.dialog.RpaScreenshotDownloadDialog;
import com.clustercontrol.jobmanagement.view.JobNodeDetailView;
import com.clustercontrol.util.Messages;

public class DownloadScreenshotJobNodeDetailAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	protected static Log m_log = LogFactory.getLog(DownloadScreenshotJobNodeDetailAction.class);
	/** アクションID */
	public static final String ID = DownloadScreenshotJobNodeDetailAction.class.getName();
	/** ウィンドウ */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
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
			RpaScreenshotDownloadDialog dialog = new RpaScreenshotDownloadDialog(this.window.getShell(),
					nodeDetailComposite);
			if (dialog.open() == IDialogConstants.OK_ID) {
				// RCPの場合のみダイアログ表示
				if (!ClusterControlPlugin.isRAP()) {
					MessageDialog.openInformation(null, Messages.getString("successful"), Messages.getString(
							"message.job.rpa.26", new String[] { dialog.getFileName(), Messages.getString("screenshot") }));
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
					JobInfoResponse.TypeEnum jobType = view.getComposite().getJobType();
					JobRpaInfoResponse.RpaJobTypeEnum rpaJobType = view.getComposite().getRpaJobType();
					if (view.getSelectedNum() > 0 && jobType != null && jobType == JobInfoResponse.TypeEnum.RPAJOB
						&&	rpaJobType != null && rpaJobType == JobRpaInfoResponse.RpaJobTypeEnum.DIRECT) {
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
