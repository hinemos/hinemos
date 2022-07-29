/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view.action;

import java.util.List;
import java.util.Map;

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
import org.openapitools.client.model.RunCollectNodeConfigResponse;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.action.GetNodeConfigSettingListTableDefine;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.repository.view.NodeConfigSettingListView;
import com.clustercontrol.util.DateUtil;
import com.clustercontrol.util.DateUtil.TimeUnitSet;
import com.clustercontrol.util.Messages;

/**
 * 構成情報取得の即時実行を行うクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigRunAction extends AbstractHandler implements IElementUpdater {

	public static final String ID = NodeConfigRunAction.class.getName();

	// ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeConfigRunAction.class);
	private static final String DELIMITER = "() : ";

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
	 * アイコン押下時実行内容.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		this.viewPart = HandlerUtil.getActivePart(event);
		NodeConfigSettingListView configListView = null;
		try {
			configListView = (NodeConfigSettingListView) this.viewPart.getAdapter(NodeConfigSettingListView.class);
		} catch (Exception e) {
			m_log.info("execute " + e.getMessage());
			return null;
		}

		if (configListView == null) {
			m_log.info("execute: view is null");
			return null;
		}

		StructuredSelection selection = (StructuredSelection) configListView.getComposite().getTableViewer()
				.getSelection();
		List<?> list = (List<?>) selection.getFirstElement();
		if (list == null) {
			return null;
		}

		String managerName = (String) list.get(GetNodeConfigSettingListTableDefine.MANAGER_NAME);
		String settingId = (String) list.get(GetNodeConfigSettingListTableDefine.GET_CONFIG_ID);

		if (settingId == null) {
			return null;
		}
		RepositoryRestClientWrapper wrapper = null;
		String message = "";
		try {
			wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
		} catch (IllegalStateException e) {
			message = Messages.getString("message.accesscontrol.18");
			MessageDialog.openError(null, Messages.getString("message.error"), message);
			return null;
		}

		Long delay = null;
		try {
			RunCollectNodeConfigResponse response = wrapper.runCollectNodeConfig(settingId);
			delay = response.getLoadDistributionTime();
		} catch (FacilityNotFound e) {
			message = Messages.getString("message.node.config.9");
			MessageDialog.openWarning(null, Messages.getString("word.warn"), message);
			return null;
		}  catch (InvalidRole e) {
			message = Messages.getString("message.accesscontrol.16");
			MessageDialog.openError(null, Messages.getString("message.error"), message);
			return null;
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + e.getMessage(), e);
			message = Messages.getString("message.unexpected_error") + "\n" + e.getMessage();
			MessageDialog.openError(null, Messages.getString("message.error"), message);
			return null;
		}

		if (delay == null) {
			message = "'delay' returned from manager is null." + " for more information, see 'hinemos_manager.log'.";
			m_log.warn(methodName + DELIMITER + message);
			message = Messages.getString("message.unexpected_error") + "\n" + message;
			MessageDialog.openError(null, Messages.getString("message.error"), message);
			return null;
		}

		String[] args = null;
		TimeUnitSet maxDelayTime = DateUtil.getHumanicTime(delay.longValue());
		args = new String[] { managerName, maxDelayTime.getTime() + Messages.getString(maxDelayTime.getUnit()) };
		message = Messages.getString("message.node.config.2", args);
		args = new String[] { message };
		message = Messages.getString("message.node.config.1", args);
		MessageDialog.openInformation(null, Messages.getString("message.priority.info"), message);
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
