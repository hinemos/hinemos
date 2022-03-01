/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.jobutil.ui.views.commands;

import java.io.File;
import java.text.ParseException;
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
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.jobutil.dialog.JobExportDialog;
import com.clustercontrol.utility.jobutil.util.JobConvert;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.job.xml.JobMasters;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

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

		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if (null == this.window || !isEnabled())
			return null;

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		JobTreeItemWrapper selectJob;
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
		if (selectJob == null){
			return null;
		}

		// 対象マネージャを取得
		String managerName = null;
		JobTreeItemWrapper mgrTree = JobTreeItemUtil.getManager(selectJob);
		if(mgrTree == null) {
			managerName = selectJob.getChildren().get(0).getData().getId();
		} else {
			managerName = mgrTree.getData().getId();
		}

		// 対象マネージャのPublishを確認
		try {
			UtilityRestClientWrapper wrapper = UtilityRestClientWrapper.getWrapper(managerName);
			boolean isPublish = wrapper.checkPublish().getPublish();
			if (!isPublish) {
				MessageDialog.openWarning(
						null,
						Messages.getString("warning"),
						Messages.getString("message.enterprise.required"));			}
		} catch (InvalidRole | InvalidUserPass e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					e.getMessage());
			return null;
		} catch (HinemosUnknown e) {
			if(UrlNotFound.class.equals(e.getCause().getClass())) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.enterprise.required"));
				return null;
			} else {
				MessageDialog.openInformation(null, Messages.getString("message"),
						e.getMessage());
				return null;
			}
		} catch (Exception e) {
			// キーファイルを確認できませんでした。処理を終了します。
			// Key file not found. This process will be terminated.
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.enterprise.required"));
			return null;
		}
		

		//viewから選択されている部分を取り出す
		JobExportDialog dialog = new JobExportDialog(viewPart.getSite().getShell());
		dialog.setSelectJob(selectJob);
		
		if (dialog.open() != Dialog.OK)
			return null;
		
		//jobtreeitem convert to jobxml
		JobMasters jobMasters = null;
		int ret = SettingConstants.ERROR_INPROCESS;
		try {
			jobMasters = JobConvert.convertJobMastersXML(selectJob, dialog.isScope(), dialog.isNotify());
			ret = JobConvert.exportJobXML(jobMasters, dialog.getFileName());
		} catch (NullPointerException e) {
			log.warn("convertJobMastersXML failed "+ e);
			
		} catch (ParseException e) {
			log.warn("convertJobMastersXML failed" + e);
		}

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
		
		JobInfoWrapper.TypeEnum jobType = null;
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

		if (jobType == JobInfoWrapper.TypeEnum.JOBUNIT || jobType == JobInfoWrapper.TypeEnum.JOBNET) {
			editEnable = true;
		}
		
		this.setBaseEnabled(editEnable);
	}
}
