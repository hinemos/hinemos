/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.util.Messages;

/**
 * 一つ上のスコープへ遷移するクライアント側アクションクラス<BR>
 * 
 * @since 1.0.0
 */
public class UpwardAction extends AbstractHandler {

	// ログ
	private static Log m_log = LogFactory.getLog( UpwardAction.class );

	/** アクションID */
	public static final String ID = OpenNodeMapAction.ActionIDBase + UpdateAction.class.getSimpleName();

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
		
		try {
			view.upward();
		} catch (Exception e) {
			m_log.warn("run(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage() + " " + e.getClass().getSimpleName());
		}
		
		return null;
	}
}
