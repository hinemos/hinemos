/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.jobutil.ui.views.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobInfoResponse.TypeEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.jobutil.dialog.JobImportDialog;
import com.clustercontrol.utility.jobutil.util.JobConvert;
import com.clustercontrol.utility.jobutil.util.JobStringUtil;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;


/**
 * ジョブをインポートするダイアログを開くためのクライアント側アクションクラス<BR>
 *
 * @version 6.1.a
 * @since 6.1.a
 */
public class ImportJobCommand extends AbstractHandler implements IElementUpdater {
	/*ロガー*/
	public Log log = LogFactory.getLog(getClass());

	/** アクションID */
	public static final String ID = ImportJobCommand.class.getName();
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
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		JobTreeItemWrapper selection;
		if (viewPart instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView) viewPart;
			JobFigure figure = (JobFigure) view.getCanvasComposite().getSelection();
			if (figure == null)
				return null;
			
			selection =  figure.getJobTreeItem();
		} else if (viewPart instanceof JobListView) {
			JobListView view = (JobListView) viewPart;
			selection = view.getSelectJobTreeItemList().get(0);
		} else if (viewPart instanceof JobTreeView) {
			JobTreeView view = (JobTreeView) viewPart;
			selection = view.getSelectJobTreeItem();
		} else {
			log.debug("execute: view " + viewPart.getTitle()); 
			return null;
		}

		// 対象マネージャを取得
		JobTreeItemWrapper mgrTree = JobTreeItemUtil.getManager(selection);
		String managerName = null;
		if(mgrTree == null) {
			managerName = selection.getChildren().get(0).getData().getId();
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

		JobImportDialog dialog = new JobImportDialog(viewPart.getSite().getShell());
		dialog.setSelectJob(selection);
		if (dialog.open() != Dialog.OK)
			return null;
		
		//jobxml convert to jobtreeitem
		List<JobTreeItemWrapper> importJobList = JobConvert.convertJobTreeItem(dialog.getFileName(), dialog.isScope(), dialog.isNotify());

		if (importJobList == null) {
			String errorMsg = Messages.getString("message.job.import.convert.fail") +
					"\n\n" +
					"FileName=" +
					dialog.getFileName();
			MessageDialog.openError(null, Messages.getString("warning"), errorMsg);
			return null;
		}


		Map<Integer, List<String>> ret = new HashMap<>();
		for (JobTreeItemWrapper importTopJob : importJobList) {
			// 選択項目とインポート項目の適正チェック
			String errMsg = JobConvert.preImportJobCheck(selection, importTopJob);
			if (!errMsg.isEmpty()) {
				List<String> preResultList = ret.get(JobStringUtil.ERROR_PRE_CHECK);
				if (preResultList == null)
					preResultList = new ArrayList<>();

				preResultList.add(importTopJob.getData().getId());
				ret.put(JobStringUtil.ERROR_PRE_CHECK, preResultList);
				continue;
			}

			// 編集モードに入る
			JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
			String jobunitId = "";
			if (importTopJob.getData().getType().equals(JobInfoWrapper.TypeEnum.JOBUNIT)) {
				editMode(managerName, importTopJob, false, JobInfoWrapper.TypeEnum.JOBUNIT);
				jobunitId = importTopJob.getData().getId();
			} else if (importTopJob.getData().getType().equals(JobInfoWrapper.TypeEnum.JOBNET)) {
				jobunitId = selection.getData().getJobunitId();
				editMode(managerName, JobConvert.getJobUnit(selection), true, JobInfoWrapper.TypeEnum.JOBNET);
			}
			
			// edit mode に変わっていなければとりやめる
			if (!editState.isLockedJobunitId(jobunitId))
				return null;

			// ジョブをインポートする
			Integer result = JobConvert.importJob(selection, importTopJob);
			List<String> retlist = ret.get(result);
			if (retlist == null)
				retlist = new ArrayList<>();

			retlist.add(importTopJob.getData().getId());
			ret.put(result, retlist);
		}

		if (viewPart instanceof JobMapEditorView || viewPart instanceof JobTreeView) {
			JobTreeView view = JobMapActionUtil.getJobTreeView();
			view.getJobMapTreeComposite().getTreeViewer().sort(selection);
			view.getJobMapTreeComposite().refresh(
					selection.getData().getType().equals(TypeEnum.MANAGER) ?
							selection :
							JobConvert.getJobUnit(selection));
			JobTreeItemWrapper parentJobUnit = null;
			if (selection.getData().getType() == JobInfoWrapper.TypeEnum.MANAGER) {
				if (importJobList.size() == 1) {
					// import対象がジョブユニット1件
					parentJobUnit = importJobList.get(0);
				}
			} else {
				//ジョブユニット、ジョブネットに対してのインポート
				parentJobUnit = JobConvert.getJobUnit(selection);
			}

			view.getJobMapTreeComposite().updateJobMapEditor(parentJobUnit);
		} else if (viewPart instanceof JobListView) {
			JobListView view = (JobListView) viewPart;
			view.getJobTreeComposite().getTreeViewer().sort(selection);
			view.getJobTreeComposite().refresh(
					selection.getData().getType().equals(TypeEnum.MANAGER) ?
							selection :
							JobConvert.getJobUnit(selection));
		}

		// 結果出力ダイアログ
		if (ret.get(SettingConstants.SUCCESS) != null && !ret.get(SettingConstants.SUCCESS).isEmpty()) {
			StringBuffer message = new StringBuffer(Messages.getString("message.job.import.success"));
			message.append("\n");
			for (String id : ret.get(SettingConstants.SUCCESS)) {
				message.append("\nJobID=");
				message.append(id);
			}
			MessageDialog.openInformation(null, Messages.getString("info"), message.toString());
			
			if (viewPart instanceof JobMapEditorView || viewPart instanceof JobTreeView) {
				JobTreeView view = JobMapActionUtil.getJobTreeView();
				if (view == null)
					return null;

				view.getJobMapTreeComposite().getTreeViewer().setSelection( new StructuredSelection(importJobList), true);
			} else if (viewPart instanceof JobListView) {
				JobListView view = (JobListView) viewPart;
				view.getJobTreeComposite().getTreeViewer().setSelection( new StructuredSelection(importJobList), true );
			}
		}
		
		if (ret.get(JobStringUtil.ERROR_PRE_CHECK) != null) {
			StringBuffer message = new StringBuffer(Messages.getString("message.job.import.select.not.appropriate"));
			message.append("\n");
			for (String id : ret.get(JobStringUtil.ERROR_PRE_CHECK)) {
				message.append("\nJobID=");
				message.append(id);
			}
			MessageDialog.openError(null, Messages.getString("warning"), message.toString());
		}
		if (ret.get(SettingConstants.ERROR_INPROCESS) != null) {
			StringBuffer errorMsg = new StringBuffer(Messages.getString("message.job.import.fail"));
			errorMsg.append("\n");
			for (String id : ret.get(SettingConstants.ERROR_INPROCESS)) {
				errorMsg.append("\nJobID=");
				errorMsg.append(id);
			}
			MessageDialog.openError(null, Messages.getString("warning"), errorMsg.toString());
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
		
		if(jobType == JobInfoWrapper.TypeEnum.MANAGER || jobType == JobInfoWrapper.TypeEnum.JOBUNIT ||
				jobType == JobInfoWrapper.TypeEnum.JOBNET) {
			editEnable = true;
		}
		this.setBaseEnabled(editEnable);
	}
	
	private void editMode(String managerName, JobTreeItemWrapper unitJob, boolean backupTree, JobInfoWrapper.TypeEnum jobType) {
		Integer editSession = null;
		JobEditState jobEditState = JobEditStateUtil.getJobEditState( managerName );

		if (jobType == JobInfoWrapper.TypeEnum.JOBNET && jobEditState.isLockedJobunitId(unitJob.getData().getId())) {
			editSession = jobEditState.getEditSession(unitJob.getData());
		} else {
			Long updateTime = jobEditState.getJobunitUpdateTime(unitJob.getData().getId());
			try {
				editSession = JobUtil.getEditLock(managerName, unitJob.getData().getId(), updateTime, false);
			} catch (OtherUserGetLock e) {
				String message = e.getMessage() + "\n"
						+ HinemosMessage.replace(MessageConstant.MESSAGE_WANT_TO_GET_LOCK.getMessage());
				if (MessageDialog.openQuestion(
						null,
						Messages.getString("confirmed"),
						message)) {
					try {
						editSession = JobUtil.getEditLock(managerName, unitJob.getData().getId(), updateTime, true);
					} catch (Exception e1) {
						log.error("run() : logical error");
					}
				} else {
					MessageDialog.openInformation(
							null,
							Messages.getString("info"),
							Messages.getString("message.job.import.cancel"));
				}
			}
		}
		if (editSession != null) {
			if (backupTree) {
				JobEditStateUtil.getJobEditState(managerName).addLockedJobunit(unitJob.getData(), JobTreeItemUtil.clone(unitJob, null), editSession);
			} else {
				JobEditStateUtil.getJobEditState(managerName).addLockedJobunit(unitJob.getData(), null, editSession);
			}
			
			JobEditStateUtil.getJobEditState(managerName).addEditedJobunit(unitJob);
		}
	}
}