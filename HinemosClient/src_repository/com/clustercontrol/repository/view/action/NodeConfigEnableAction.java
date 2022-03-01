/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view.action;

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
import org.openapitools.client.model.SetStatusNodeConfigSettingRequest;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.action.GetNodeConfigSettingListTableDefine;
import com.clustercontrol.repository.composite.NodeConfigSettingInfoListComposite;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.repository.view.NodeConfigSettingListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * リポジトリ[構成情報収集]ビューの収集有効アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigEnableAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog(NodeConfigEnableAction.class);

	/** アクションID */
	public static final String ID = NodeConfigEnableAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * アクション実行
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		NodeConfigSettingListView view = null;
		try {
			view = (NodeConfigSettingListView) this.viewPart.getAdapter(NodeConfigSettingListView.class);
		} catch (Exception e) {
			m_log.info("execute " + e.getMessage());
			return null;
		}

		if (view == null) {
			m_log.info("execute: view is null");
			return null;
		}

		NodeConfigSettingInfoListComposite composite = (NodeConfigSettingInfoListComposite) view.getComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		Object[] objs = selection.toArray();

		// 1つも選択されていない場合
		String[] args = null;
		String message = "";
		if (objs.length == 0) {
			args = new String[] { Messages.getString("getconfig.id") };
			message = Messages.getString("message.common.1", args);
			MessageDialog.openConfirm(null, Messages.getString("confirmed"), message);
			return null;
		}

		// 1つ以上選択されている場合
		String managerName = null;
		String configId = null;
		StringBuffer targetList = new StringBuffer();
		StringBuffer successList = new StringBuffer();
		StringBuffer failureList = new StringBuffer();

		Map<String, List<String>> dataMap = new ConcurrentHashMap<String, List<String>>();
		for (int i = 0; i < objs.length; i++) {
			managerName = (String) ((ArrayList<?>) objs[i]).get(GetNodeConfigSettingListTableDefine.MANAGER_NAME);
			if (dataMap.get(managerName) == null) {
				dataMap.put(managerName, new ArrayList<String>());
			}
		}

		for (int i = 0; i < objs.length; i++) {
			managerName = (String) ((ArrayList<?>) objs[i]).get(GetNodeConfigSettingListTableDefine.MANAGER_NAME);
			configId = (String) ((ArrayList<?>) objs[i]).get(GetNodeConfigSettingListTableDefine.GET_CONFIG_ID);

			dataMap.get(managerName).add(configId);

			if (targetList.length() > 0) {
				targetList.append(", ");
			}
			targetList.append(configId);
		}

		// 実行確認(NG→終了)
		args = new String[] { targetList.toString() };
		if (!MessageDialog.openConfirm(null, Messages.getString("confirmed"),
				Messages.getString("message.node.config.3", args))) {
			return null;
		}

		boolean hasRole = true; // 設定権限を持っているかどうか
		// 実行
		for (Map.Entry<String, List<String>> map : dataMap.entrySet()) {
			String mgrName = map.getKey();
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(mgrName);

			for (String settingId : map.getValue()) {

				try {
					SetStatusNodeConfigSettingRequest requestDto = new SetStatusNodeConfigSettingRequest();
					List<String> settingIds = new ArrayList<>();
					settingIds.add(settingId);
					requestDto.setSettingId(settingIds);
					requestDto.setValidFlag(true);
					wrapper.setStatusNodeConfigSetting(requestDto);
					if (successList.length() > 0) {
						successList.append(", ");
					}
					successList.append(settingId + "(" + mgrName + ")");
				} catch (InvalidRole e) {
					if (failureList.length() > 0) {
						failureList.append(", ");
					}
					failureList.append(settingId + "(" + HinemosMessage.replace(e.getMessage()) + ")");
					m_log.warn("run() setStatusNodeConfigSetting settingId=" + settingId + ", "
							+ HinemosMessage.replace(e.getMessage()));
					hasRole = false;
				} catch (Exception e) {
					if (failureList.length() > 0) {
						failureList.append(", ");
					}
					failureList.append(settingId + "(" + HinemosMessage.replace(e.getMessage()) + ")");
					m_log.warn("run() setStatusNodeConfigSetting settingId=" + settingId + ", "
							+ HinemosMessage.replace(e.getMessage()));
				}
			}
		}

		if (!hasRole) {
			// 権限がない場合にはエラーメッセージを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		}

		// 成功ダイアログ
		if (successList.length() != 0) {
			args = new String[] { successList.toString() };
			MessageDialog.openInformation(null, Messages.getString("successful"),
					Messages.getString("message.node.config.4", args));
		}

		// 失敗ダイアログ
		if (failureList.length() != 0) {
			args = new String[] { failureList.toString() };
			MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.node.config.5", args));
		}

		// ビューコンポジット更新
		composite.update();

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
				if (part instanceof NodeConfigSettingListView) {
					// Enable button when 1 item is selected
					NodeConfigSettingListView view = (NodeConfigSettingListView) part;

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

}
