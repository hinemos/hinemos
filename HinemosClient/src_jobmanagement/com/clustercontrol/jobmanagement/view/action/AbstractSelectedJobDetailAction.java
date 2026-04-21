/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.view.action;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobOperationPropResponse.AvailableOperationListEnum;
import org.openapitools.client.model.JobOperationRequest;
import org.openapitools.client.model.JobOperationRequest.ControlEnum;
import org.openapitools.client.model.JobOperationRequest.EndStatusEnum;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.composite.DetailComposite.JobDetailViewModel;
import com.clustercontrol.jobmanagement.composite.DetailComposite.JobElement;
import com.clustercontrol.jobmanagement.dialog.AbstractSelectControlDialog;
import com.clustercontrol.jobmanagement.dialog.AbstractSelectControlDialog.Item;
import com.clustercontrol.jobmanagement.dialog.AbstractSelectControlDialog.Root;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.JobDetailView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/*
 * ジョブ履歴[ジョブ詳細]ビューで選択されたジョブに対する開始および停止アクションの基底クラス
 */
public abstract class AbstractSelectedJobDetailAction extends AbstractHandler implements IElementUpdater {
	
	protected JobDetailView ownerView;
	protected Consumer<Set<JobElement>> selectionListener;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// ジョブ履歴[ジョブ詳細]ビューのイベントか判定
		IWorkbenchPart wp = HandlerUtil.getActivePart(event);
		JobDetailView view = (JobDetailView) wp.getAdapter(JobDetailView.class);
		if (view == null) {
			getLogger().info("The Event did not originate from a JobDetailView."); 
			return null;
		}
		
		// 選択されたジョブのリストを取得
		JobDetailViewModel model = view.getComposite().getJobDetailViewModel();
		Set<JobElement> selected = model.getSelectedElements();
		
		// 選択されたジョブを実行可能な操作ごとに分類する
		JobRestClientWrapper wrapper = getJobRestClientWrapper(view.getComposite().getManagerName());
		String sessionId = view.getComposite().getSessionId();
		String jobunitId = view.getComposite().getJobunitId();
		
		Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOpe = new LinkedHashMap<>();
		selected.stream().forEach(s->{
			Optional<List<AvailableOperationListEnum>> operations =
					Optional.ofNullable(getAvailableOperation(wrapper, sessionId, jobunitId, s.getItem().getData().getId()));
			operations.ifPresent(l->
					l.stream().forEach(o->jobMapByOpe.computeIfAbsent(o, k->new LinkedHashSet<>()).add(s))
				);
		});
		
		if (jobMapByOpe.isEmpty()) {
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.job.jobdetal.not_found_available_control"));
			return null;
		}
		
		// 実行する操作を選択
		AbstractSelectControlDialog dialog = createDialog(
				HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
				selected,
				jobMapByOpe
				);
		if (dialog.open() == IDialogConstants.OK_ID) {
			Root root = dialog.getModel();
			
			root.getOperation().ifPresent(o->{
				ControlEnum control = OperationMessage.stringToEnum(OperationMessage.enumToString(o));
				root.getItems().stream().forEach(c->recursiveExecuteJobOperation(c, wrapper, sessionId, jobunitId, control, root.getNewEndStatus().orElse(null), root.getNewEndValue().orElse(null)));
				});
		}
		return null;
	}
	
	protected JobRestClientWrapper getJobRestClientWrapper(String managerName) {
		try {
			return JobRestClientWrapper.getWrapper(managerName);
		} catch(Exception e) {
			getLogger().warn("getStartProperty(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			throw new InternalError(String.format("Fail to get JobRestClientWrapper. manager=%s", managerName));
		}
	}
	
	/*
	 * 指定されたジョブに対する実行可能な操作一覧を取得
	 */
	protected abstract List<AvailableOperationListEnum> getAvailableOperation(JobRestClientWrapper wrapper, String sessionId, String jobunitId, String jobId);
	
	
	/*
	 * 指定されたジョブおよびその子要素のジョブに対して、指定された制御を実行
	 */
	protected void recursiveExecuteJobOperation(Item item, JobRestClientWrapper wrapper, String sessionId, String jobunitId, ControlEnum control, EndStatusEnum endStatus, Integer endValue) {
		// ジョブが操作対象なら、指定された制御で実行
		if (item.isOperationTarget()) {
			executeJobOperation(wrapper, sessionId, jobunitId, item.getJobElement().getItem().getData().getId(), control, endStatus, endValue);
		}
		
		// 子要素に対して繰り返し実行
		item.getChildren().forEach(c->recursiveExecuteJobOperation(c, wrapper, sessionId, jobunitId, control, endStatus, endValue));
	}
	
	protected void executeJobOperation(JobRestClientWrapper wrapper, String sessionId, String jobunitId, String jobId, ControlEnum control, EndStatusEnum endStatus, Integer endValue) {
		// 指定された制御が最新の実施可能制御に含まれていないならスルーする
		try {
			List<AvailableOperationListEnum> ope = getAvailableOperation(wrapper, sessionId, jobunitId, jobId);
			Set<ControlEnum> set = ope.stream().map(o->OperationMessage.stringToEnum(OperationMessage.enumToString(o))).collect(Collectors.toSet());
			if (!set.contains(control)) {
				return; 
			}
		} catch(Exception e) {
			getLogger().warn(e.getMessage(), e);
			return;
		}
		
		try {
			JobOperationRequest request = new JobOperationRequest();
			request.setControl(control);
			request.setEndStatus(endStatus);
			request.setEndValue(endValue);
			
			wrapper.operationSessionJob(sessionId, jobunitId, jobId, request);
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			if(e.getCause() instanceof NullPointerException){
				// 終了値未入力時、エラーダイアログを表示する
				MessageDialog.openError(
						null,
						Messages.getString("message"),
						Messages.getString("message.job.21"));
			}
			else if(e.getCause() instanceof IllegalStateException){
				// 実行エラー時、エラーダイアログを表示する
				MessageDialog.openError(
						null,
						Messages.getString("message"),
						Messages.getString("message.job.36"));
			}
			else{
				// 実行エラー時、エラーダイアログを表示する
				MessageDialog.openError(
						null,
						Messages.getString("message"),
						Messages.getString("message.job.34") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
	}
	
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		// ジョブ履歴[ジョブ詳細]ビューで、ジョブが選択されているならこのアクションを有効にする
		IWorkbenchPartSite site = (IWorkbenchPartSite)element.getServiceLocator().getService(IWorkbenchPartSite.class);
		if (site.getPart() instanceof JobDetailView) {
			JobDetailView view = (JobDetailView)site.getPart();
			
			// 最初に紐づけられたJobDetailViewのインスタンスを保存
			// リスナーの削除時に利用するので
			if (ownerView == null) {
				ownerView = view;
			} else {
				if (ownerView != view) {
					getLogger().info("A differnt JobDetailView instance was provided instead of the existing one.");
					return;
				}
			}
			
			if (selectionListener == null) {
				selectionListener = e->{
						// JobDetailViewで選択項目の変更があったので、このアクションの更新を促して、自身の有効無を更新
						ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
						if (service == null) {
							return;
						}
						service.refreshElements(getCommandId(), null);
					};
			}
			
			// 最初に紐づけられたJobDetailViewで、項目の選択に変更があったタイミングをリッスン
			if (!view.getComposite().getJobDetailViewModel().getSelectionListener().contains(selectionListener)) {
				view.getComposite().getJobDetailViewModel().addSelectionListener(selectionListener);
			}
			
			// 選択項目があるなら有効にする
			setBaseEnabled(
				!view.getComposite().getJobDetailViewModel().getSelectedElements().isEmpty()
				);
		}
	}
	
	/*
	 * コマンドIDを返す
	 */
	protected abstract String getCommandId();
	
	/*
	 * 実施する制御の選択ダイアログを作成
	 */
	protected abstract AbstractSelectControlDialog createDialog(Shell shell, Set<JobElement> selected, Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOpe);
	
	/*
	 * ロガーを取得
	 */
	protected abstract Log getLogger();
	
	@Override
	public void dispose() {
		if (ownerView != null) {
			ownerView.getComposite().getJobDetailViewModel().removeSelectionListener(selectionListener);
		}
	}
}
