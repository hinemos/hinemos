/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.nodemap.util.NodeMapEndpointWrapper;
import com.clustercontrol.nodemap.util.SecondaryIdMap;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.nodemap.view.NodeMapView.Mode;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.nodemap.Association;

/**
 * ビューをコネクション編集可能モードに変更するクライアント側アクションクラス<BR>
 * @since 1.0.0
 */
public class GetL2ConnectionAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( GetL2ConnectionAction.class );
	
	/** アクションID */
	public static final String ID = OpenNodeMapAction.ActionIDBase + GetL2ConnectionAction.class.getSimpleName();

	/** ビュー */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		
		NodeMapView view = (NodeMapView) viewPart.getAdapter(NodeMapView.class);
		if (view.getMode() == Mode.FIXED_MODE || view.getMode() == Mode.LIST_MODE) {
			// 閲覧モード・リストモードの場合は自動結線しない
			MessageDialog.openInformation(null, Messages.getString("message"),
					com.clustercontrol.nodemap.messages.Messages.getString("auto.connection.info"));
			return null;
		}
		
		String secondaryId = view.getViewSite().getSecondaryId();
		String facilityId = SecondaryIdMap.getFacilityId(secondaryId);
		try {
			long start = System.currentTimeMillis();
			NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(view.getCanvasComposite().getManagerName());
			List<Association> list = wrapper.getL2ConnectionMap(facilityId);
			view.getController().autoAssociation(list);
			view.updateNotManagerAccess();
			long end = System.currentTimeMillis();
			m_log.debug("run() : " + (end - start) + "ms");
			MessageDialog.openInformation(null, Messages.getString("message"), com.clustercontrol.nodemap.messages.Messages.getString("auto.connection.success"));
		} catch (Exception e) {
			m_log.warn("run() getL2Connection, " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					com.clustercontrol.nodemap.messages.Messages.getString("auto.connection.fail") + ", " + e.getMessage() + " " + e.getClass().getSimpleName());
		}

		view.setFocus();
		
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof NodeMapView){
					// Enable button when 1 item is selected
					NodeMapView view = (NodeMapView)part;
					editEnable = view.isEditableMode();
				}

				this.setBaseEnabled(editEnable);
			}
		}
	}
}
