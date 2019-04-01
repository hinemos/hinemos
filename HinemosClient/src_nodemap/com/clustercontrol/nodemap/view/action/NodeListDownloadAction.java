/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.nodemap.dialog.NodeListDownloadDialog;
import com.clustercontrol.nodemap.view.NodeListView;
import com.clustercontrol.util.Messages;

/**
 * 検索結果ノードの構成情報ダウンロード処理を行うアクションクラス<BR>
 *
 * @version 6.2.0
 */
public class NodeListDownloadAction extends AbstractHandler {

	public static final String ID = OpenNodeMapAction.ActionIDBase + NodeListDownloadAction.class.getSimpleName();

	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeListDownloadAction.class);

	//	 ----- instance フィールド ----- //
	/** ビュー */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		NodeListView view = (NodeListView) viewPart.getAdapter(NodeListView.class);

		@SuppressWarnings("unchecked")
		ArrayList<Object> nodeTableList = (ArrayList<Object>)view.getListComposite().getTableViewer().getInput();
		if (nodeTableList == null || nodeTableList.size() == 0) {
			// 検索結果ノードが存在しない場合は処理終了
			return null;
		}

		// ファシリティIDマップ (マネージャ名, ファシリティIDリスト)
		HashMap<String, List<String>> facilityIdMap = new HashMap<>();

		for (int i = 0; i < nodeTableList.size(); i++) {
			@SuppressWarnings("unchecked")
			ArrayList<Object> objList = (ArrayList<Object>)nodeTableList.get(i);
			if (objList == null) {
				continue;
			}
			if (!facilityIdMap.containsKey((String)objList.get(0))) {
				facilityIdMap.put((String)objList.get(0), new ArrayList<>());
			}
			facilityIdMap.get((String)objList.get(0)).add(0, (String)objList.get(1));
		}

		NodeListDownloadDialog dialog = new NodeListDownloadDialog(
				this.viewPart.getSite().getShell(), facilityIdMap, view.getNodeFilterInfo());

		// ダイアログにて出力が選択された場合、ファイルダウンロード
		int btnId = dialog.open();
		if( btnId == IDialogConstants.OK_ID ) {
			m_log.debug( dialog.getFilePath() + " exported" );
			if( !ClusterControlPlugin.isRAP() ){
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.monitor.45", new String[]{ dialog.getFileName(), Messages.getString("node.config")}));
			}
		} else if( btnId == IDialogConstants.CANCEL_ID ) {
			// Do nothing
		} else {
			MessageDialog.openError(
				null,
				Messages.getString("failed"),
				Messages.getString("message.monitor.46"));
		}
		return null;
	}
}
