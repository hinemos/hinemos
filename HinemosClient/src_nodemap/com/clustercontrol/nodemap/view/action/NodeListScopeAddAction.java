/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openapitools.client.model.GetNodeListRequest;

import com.clustercontrol.nodemap.dialog.NodeListScopeCreateDialog;
import com.clustercontrol.nodemap.view.NodeListView;
import com.clustercontrol.repository.bean.NodeConfigSettingConstant;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

/**
 * ノードマップ[ノード一覧]に表示されたノードを含むスコープを作成するアクションクラス<BR>
 *
 * @version 6.2.0
 */
public class NodeListScopeAddAction extends AbstractHandler {

	public static final String ID = OpenNodeMapAction.ActionIDBase + NodeListScopeAddAction.class.getSimpleName();

	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeListDownloadAction.class);

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

		GetNodeListRequest nodeFilterInfo = view.getNodeFilterInfo();

		// スコープのファシリティID生成
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		sdf.setTimeZone(TimezoneUtil.getTimeZone());
		String scopeFacilityId = NodeConfigSettingConstant.NODE_CONFIG_NODE_PREFIX + sdf.format(HinemosTime.getDateInstance());

		HashMap<String, List<String>> facilityIdMap = new HashMap<>();

		for (Object obj : nodeTableList) {
			@SuppressWarnings("unchecked")
			ArrayList<Object> objList = (ArrayList<Object>)obj;
			if (objList == null) {
				continue;
			}
			if (!facilityIdMap.containsKey((String)objList.get(0))) {
				facilityIdMap.put((String)objList.get(0), new ArrayList<>());
			}
			facilityIdMap.get((String)objList.get(0)).add((String)objList.get(1));
		}

		// ダイアログを生成
		List<String> rollbackManagerNameList = new ArrayList<>();

		String[] mapKeys = facilityIdMap.keySet().toArray(new String[]{});
		Arrays.sort(mapKeys);

		for (String managerName : mapKeys) {
			if (facilityIdMap.get(managerName) == null || facilityIdMap.get(managerName).size() == 0) {
				continue;
			}
			try {
				NodeListScopeCreateDialog dialog = new NodeListScopeCreateDialog(
						this.viewPart.getSite().getShell(), managerName, scopeFacilityId, facilityIdMap.get(managerName), nodeFilterInfo);
				
				// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
				if( dialog.open() == IDialogConstants.OK_ID ){
					rollbackManagerNameList.add(managerName);
					view.reload();
				}
			} catch (Exception e) {
				m_log.warn("execute() : Failed to add scope. : " + e.getMessage());
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.repository.15") + ", " + HinemosMessage.replace(e.getMessage()));

				// エラー時は、全てのスコープ登録を元に戻す
				for (String rollbackManagerName : rollbackManagerNameList) {
					try {
						// スコープ削除処理
						RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(rollbackManagerName);
						wrapper.deleteScope(scopeFacilityId);
					} catch (Exception ex) {
						m_log.warn("execute() : Failed to delete scope. : " + e.getMessage());
					}
				}
				// 処理終了
				return null;
			}
		}
		return null;
	}
}
