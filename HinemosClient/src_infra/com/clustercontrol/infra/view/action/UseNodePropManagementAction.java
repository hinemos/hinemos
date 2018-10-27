/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.view.action;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.infra.bean.InfraNodeInputConstant;
import com.clustercontrol.infra.bean.InfraNodeInputImageConstant;
import com.clustercontrol.infra.bean.InfraNodeInputMessage;

public class UseNodePropManagementAction extends AbstractHandler implements IElementUpdater {
	/** アクションID */
	public static final String ID = UseNodePropManagementAction.class.getName();

	private IWorkbenchWindow window;

	private static Object reserveLock = new Object();

	/** ログイン情報設定 */
	public static final String P_INFRA_MANAGEMENT_NODE_INPUT_TYPE = "infraManagementNodeInputType";


	public static int getNodeInputType() {
		return ClusterControlPlugin.getDefault().getPreferenceStore().getInt(P_INFRA_MANAGEMENT_NODE_INPUT_TYPE);
	}

	public static void setNodeInputType(int nodeInputType) {
		ClusterControlPlugin.getDefault().getPreferenceStore().setValue(P_INFRA_MANAGEMENT_NODE_INPUT_TYPE, nodeInputType);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		if (commandService != null) {
			synchronized (reserveLock){
				Integer nodeInputType = getNodeInputType();
				if (nodeInputType == InfraNodeInputConstant.TYPE_NODE_PARAM) {
					nodeInputType = InfraNodeInputConstant.TYPE_INFRA_PARAM;
				} else if (nodeInputType == InfraNodeInputConstant.TYPE_INFRA_PARAM) {
					nodeInputType = InfraNodeInputConstant.TYPE_DIALOG;
				} else if (nodeInputType == InfraNodeInputConstant.TYPE_DIALOG) {
					nodeInputType = InfraNodeInputConstant.TYPE_NODE_PARAM;
				}
				setNodeInputType(nodeInputType);
				commandService.refreshElements(ID, null);
			}
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		Integer nodeInputType = getNodeInputType();
		element.setTooltip(InfraNodeInputMessage.typeToString(nodeInputType));
		element.setText(InfraNodeInputMessage.typeToString(nodeInputType));
		element.setIcon(InfraNodeInputImageConstant.typeToImageDescriptor(nodeInputType));
	}
}