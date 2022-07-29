/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.sdml.action.GetSdmlControlSettingListTableDefine;
import com.clustercontrol.sdml.view.SdmlControlSettingListView;
import com.clustercontrol.util.Messages;

/**
 * SDML設定[制御設定]ビューにおける有効/無効のアクションを定義する抽象クラス
 *
 */
public abstract class SdmlSettingEnableBaseAction extends AbstractHandler implements IElementUpdater {
	private static Log logger = LogFactory.getLog(SdmlSettingEnableBaseAction.class);

	/** ビュー */
	protected IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);

		SdmlControlSettingListView view = null;
		try {
			view = (SdmlControlSettingListView) this.viewPart.getAdapter(SdmlControlSettingListView.class);
		} catch (Exception e) {
			logger.info("execute() : " + e.getMessage());
			return null;
		}
		if (view == null) {
			logger.info("execute() : view is null");
			return null;
		}

		StructuredSelection selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();

		Object[] objs = selection.toArray();

		// 1つも選択されていない場合
		if (objs.length == 0) {
			MessageDialog.openConfirm(null, Messages.getString("confirmed"), Messages
					.getString("message.sdml.control.required", new String[] { Messages.getString("application.id") }));
			return null;
		}

		// 1つ以上選択されている場合
		String action = getAction();
		String target = getTarget();
		String managerName = null;
		String applicationId = null;
		StringBuffer targetList = new StringBuffer();
		StringBuffer successList = new StringBuffer();
		StringBuffer failureList = new StringBuffer();

		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		for (int i = 0; i < objs.length; i++) {
			managerName = (String) ((ArrayList<?>) objs[i]).get(GetSdmlControlSettingListTableDefine.MANAGER_NAME);
			map.put(managerName, new ArrayList<String>());
		}
		for (int i = 0; i < objs.length; i++) {
			managerName = (String) ((ArrayList<?>) objs[i]).get(GetSdmlControlSettingListTableDefine.MANAGER_NAME);
			applicationId = (String) ((ArrayList<?>) objs[i]).get(GetSdmlControlSettingListTableDefine.APPLICATION_ID);

			map.get(managerName).add(applicationId);
			targetList.append(String.format("%s(%s)", applicationId, managerName));
			targetList.append("\n");
		}

		// 実行確認(NG→終了)
		if (!MessageDialog.openConfirm(null, Messages.getString("confirmed"), Messages.getString(
				"message.sdml.control.multi.confirm", new String[] { target, action, targetList.toString() }))) {
			return null;
		}

		boolean hasRole = true;
		// 実行
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			managerName = entry.getKey();
			try {
				// アクションを実行（子クラスで定義）
				action(managerName, entry.getValue());

				for (String targetId : entry.getValue()) {
					successList.append(String.format("%s(%s)", targetId, managerName));
					successList.append("\n");
				}
			} catch (InvalidRole e) {
				for (String targetId : entry.getValue()) {
					failureList.append(String.format("%s(%s)", targetId, managerName));
					failureList.append("\n");
				}
				logger.info("execute() : targetIds=" + entry.getValue().toString() + ", " + e.getClass().getSimpleName()
						+ ", " + e.getMessage());
				hasRole = false;
			} catch (Exception e) {
				for (String targetId : entry.getValue()) {
					failureList.append(String.format("%s(%s)", targetId, managerName));
					failureList.append("\n");
				}
				logger.warn("execute() : targetIds=" + entry.getValue().toString() + ", " + e.getClass().getSimpleName()
						+ ", " + e.getMessage(), e);
			}
		}

		if (!hasRole) {
			// 権限がない場合にはエラーメッセージを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		}

		// 成功ダイアログ
		if (successList.length() > 0) {
			MessageDialog.openInformation(null, Messages.getString("successful"), Messages.getString(
					"message.sdml.control.multi.success", new String[] { target, action, successList.toString() }));
		}

		// 失敗ダイアログ
		if (failureList.length() > 0) {
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString(
					"message.sdml.control.multi.failed", new String[] { target, action, failureList.toString() }));
		}

		// ビューを更新
		view.update();
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
				if (part instanceof SdmlControlSettingListView) {
					// Enable button when 1 item is selected
					SdmlControlSettingListView view = (SdmlControlSettingListView) part;

					if (view.getSelectedNum() > 0) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

	/**
	 * 実行するアクション名
	 */
	protected abstract String getAction();

	/**
	 * アクションの対象名
	 */
	protected abstract String getTarget();

	/**
	 * 実行するアクション
	 */
	protected abstract void action(String managerName, List<String> applicationIds) throws RestConnectFailed,
			HinemosUnknown, SdmlControlSettingNotFound, InvalidUserPass, InvalidRole, InvalidSetting;
}
