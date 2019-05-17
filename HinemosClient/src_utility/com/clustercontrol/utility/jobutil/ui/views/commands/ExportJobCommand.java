/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.jobutil.ui.views.commands;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.jobutil.dialog.JobExportDialog;
import com.clustercontrol.utility.jobutil.util.JobConvert;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.UtilityEndpointWrapper;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.utility.HinemosUnknown_Exception;
import com.clustercontrol.ws.utility.InvalidRole_Exception;
import com.clustercontrol.ws.utility.InvalidUserPass_Exception;

/**
 * ジョブをエクスポートするダイアログを開くためのクライアント側アクションクラス<BR>
 *
 * @version 6.1.a
 * @since 6.1.a
 */
public class ExportJobCommand extends AbstractHandler implements IElementUpdater {
	/*ロガー*/
	protected Log log = LogFactory.getLog(getClass());

	/** アクションID */
	public static final String ID = ExportJobCommand.class.getName();
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// keyチェック
		try {
			UtilityEndpointWrapper wrapper = UtilityEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
			String version = wrapper.getVersion();
			if (version.length() > 7) {
				boolean result = Boolean.valueOf(version.substring(7, version.length()));
				if (!result) {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.expiration.term.invalid"));
				}
			}
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					e.getMessage());
			return null;
		} catch (Exception e) {
			// キーファイルを確認できませんでした。処理を終了します。
			// Key file not found. This process will be terminated.
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.expiration.term"));
			return null;
		}

		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if (null == this.window || !isEnabled())
			return null;

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		JobTreeItem selectJob;
		if (viewPart instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView) viewPart;
			JobFigure figure = (JobFigure) view.getCanvasComposite().getSelection();
			if (figure == null)
				return null;
			
			selectJob =  figure.getJobTreeItem();
		} else if (viewPart instanceof JobListView) {
			JobListView view = (JobListView) viewPart;
			selectJob = view.getSelectJobTreeItemList().get(0);
		} else if (viewPart instanceof JobTreeView) {
			JobTreeView view = (JobTreeView) viewPart;
			selectJob = view.getSelectJobTreeItem();
		} else {
			log.debug("execute: view " + viewPart.getTitle()); 
			return null;
		}

		//viewから選択されている部分を取り出す
		if (selectJob == null)
			return null;

		JobExportDialog dialog = new JobExportDialog(viewPart.getSite().getShell());
		dialog.setSelectJob(selectJob);
		
		if (dialog.open() != Dialog.OK)
			return null;
		
		//jobtreeitem convert to jobxml
		int ret = JobConvert.exportJobXML(JobConvert.convertJobMastersXML(selectJob, dialog.isScope(), dialog.isNotify()), dialog.getFileName());

		// 結果出力ダイアログ
		if (ret == SettingConstants.SUCCESS) {
			String message = Messages.getString("message.job.export.success") + "\n\n" + "JobID=" + selectJob.getData().getId();
			MessageDialog.openInformation(null, Messages.getString("info"), message);
		} else {
			String message = Messages.getString("message.job.export.fail") + "\n\n" + "JobID=" + selectJob.getData().getId();
			MessageDialog.openError(null, Messages.getString("warning"), message);
		}

		//Web版ダウンロード
		if (ClusterControlPlugin.isRAP()) {
			File file = new File(dialog.getFileName());
			if (file.exists()) {
				try {
					FileDownloader.openBrowser(window.getShell(), file.getAbsolutePath(), file.getName());
				} catch (Exception e) {
					MessageDialog.openError(null,
							Messages.getString("message.error"),
							Messages.getString("message.export.error2"));
				}
			}
			if (ClientPathUtil.getInstance().isBussy(dialog.getFileName())) {
				ClientPathUtil.getInstance().unlock(dialog.getFileName());
			}

			if(file != null) {
				if(!file.delete())
					log.warn(String.format("Fail to delete file. %s", file.getAbsolutePath()));
			}
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if (null == window)
			return;
		
		IWorkbenchPage page = window.getActivePage();
		if (null == page)
			return;

		IWorkbenchPart part = page.getActivePart();
		boolean editEnable = false;
		
		int jobType = -1;
		if (part instanceof JobListView) {
			// Enable button when 1 item is selected
			JobListView view = (JobListView)part;
			int size = view.getJobTreeComposite().getSelectItemList().size();
			if (size == 1)
				jobType = view.getDataType();
			
		} else if (part instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView) part;
			JobFigure figure = (JobFigure) view.getCanvasComposite().getSelection();
			if (figure != null)
				jobType = figure.getJobTreeItem().getData().getType();
			
		} else if (part instanceof JobTreeView) {
			JobTreeView view = (JobTreeView) part;
			int size = view.getSelectJobTreeItemList().size();
			if (size == 1)
				jobType = view.getDataType();
		}

		if (jobType == JobConstant.TYPE_JOBUNIT || jobType == JobConstant.TYPE_JOBNET ||
				jobType == JobConstant.TYPE_REFERJOBNET) {
			editEnable = true;
		}
		
		this.setBaseEnabled(editEnable);
	}
}
