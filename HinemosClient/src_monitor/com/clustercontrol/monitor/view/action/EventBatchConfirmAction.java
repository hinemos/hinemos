/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.EventFilterBaseRequest;
import org.openapitools.client.model.EventFilterConditionRequest;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.ModifyBatchConfirmRequest;
import org.openapitools.client.model.ModifyBatchConfirmRequest.ConfirmTypeEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.filtersetting.bean.EventFilterContext;
import com.clustercontrol.filtersetting.util.EventFilterHelper;
import com.clustercontrol.monitor.dialog.EventBatchConfirmDialog;
import com.clustercontrol.monitor.util.MonitorResultRestClientWrapper;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 監視[一括確認]ダイアログによる確認の更新処理を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class EventBatchConfirmAction extends AbstractHandler {

	// ログ
	private static Log m_log = LogFactory.getLog( EventBatchConfirmAction.class );

	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/** アクションID */
	public static final String ID = EventBatchConfirmAction.class.getName();

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * 監視[一括確認]ダイアログで指定された条件に一致するイベント情報の確認を更新します。
	 * <p>
	 * <ol>
	 * <li>監視[一括確認]ダイアログを表示します。</li>
	 * <li>ダイアログで指定された更新条件を取得します。</li>
	 * <li>スコープツリーで選択されているアイテムより、ファシリティIDを取得します。</li>
	 * <li>ファシリティIDと更新条件に一致するイベント情報の確認を一括更新します。 </li>
	 * <li>監視[イベント]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.dialog.EventBatchConfirmDialog
	 * @see com.clustercontrol.monitor.view.EventView#update()
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		EventView view = null;
		try {
			view = (EventView) this.viewPart.getAdapter(EventView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		FacilityTreeItemResponse item = view.getScopeTreeComposite().getSelectItem();
		if( null == item || item.getData().getFacilityType() == FacilityTypeEnum.COMPOSITE ){
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.monitor.47"));
			return null;
		}

		String managerName;
		String facilityId;
		if( item.getData().getFacilityType() == FacilityTypeEnum.MANAGER ){
			facilityId = null;
			managerName = item.getData().getFacilityId();
		}else{
			facilityId = item.getData().getFacilityId();
			FacilityTreeItemResponse manager = ScopePropertyUtil.getManager(item);
			managerName = manager.getData().getFacilityId();
		}

		EventFilterContext context = new EventFilterContext(
				// 元のフィルタ設定は壊さないように複製する
				EventFilterHelper.duplicate(view.getFilter()),
				managerName,
				view.getSelectedScopeLabel(),
				view.getEventDspSetting());

		EventBatchConfirmDialog dialog = new EventBatchConfirmDialog(
				this.viewPart.getSite().getShell(),
				context);

		if (dialog.open() == IDialogConstants.OK_ID) {
			try {
				EventFilterBaseRequest filter = context.getFilter();

				if (filter.getFacilityId() == null) {
					// ダイアログでスコープが選択されていない場合は、ダイアログを開く前に選択していたスコープをセット
					filter.setFacilityId(facilityId);
				} else {
					// ダイアログでスコープが選択されている場合は、対象マネージャを更新
					managerName = context.getManagerName();
				}

				// 重要度の入力値チェック
				boolean prioritySelected = false;
				for (EventFilterConditionRequest cnd : filter.getConditions()) {
					if (Boolean.TRUE.equals(cnd.getPriorityInfo())
							|| Boolean.TRUE.equals(cnd.getPriorityWarning())
							|| Boolean.TRUE.equals(cnd.getPriorityCritical())
							|| Boolean.TRUE.equals(cnd.getPriorityUnknown())) {
						prioritySelected = true;
						break;
					}
				}
				if (!prioritySelected) {
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.60") + ", " + Messages.getString("message.notify.13"));
					return null;
				}

				MonitorResultRestClientWrapper wrapper = MonitorResultRestClientWrapper.getWrapper(managerName);
				ModifyBatchConfirmRequest modifyBatchConfirmRequest = new ModifyBatchConfirmRequest();
				modifyBatchConfirmRequest.setConfirmType(ConfirmTypeEnum.CONFIRMED);
				modifyBatchConfirmRequest.setFilter(filter);
				wrapper.modifyBatchConfirm(modifyBatchConfirmRequest);
				view.update(false);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (HinemosUnknown e) {
				MessageDialog.openError(null, Messages.getString("message"),
						Messages.getString("message.monitor.60") + ", " + HinemosMessage.replace(e.getMessage()));
			} catch (Exception e) {
				m_log.warn("run() modifyBatchConfirm, " + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		return null;
	}
}
