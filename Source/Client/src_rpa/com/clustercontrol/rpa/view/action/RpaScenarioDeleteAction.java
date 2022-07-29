/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

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

import com.clustercontrol.fault.InvalidRole;

import com.clustercontrol.rpa.action.GetRpaScenarioListTableDefine;
import com.clustercontrol.rpa.composite.RpaScenarioListComposite;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.view.RpaScenarioListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオ実績[シナリオ一覧]ビューの削除アクションクラス<BR>
 */
public class RpaScenarioDeleteAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioDeleteAction.class );

	/** アクションID */
	public static final String ID = RpaScenarioDeleteAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		RpaScenarioListView view = null;
		try {
			view = (RpaScenarioListView) this.viewPart.getAdapter(RpaScenarioListView.class);
		} catch (Exception e) { 
			log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			log.info("execute: view is null"); 
			return null;
		}

		RpaScenarioListComposite composite = (RpaScenarioListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		List<String[]> argsList = new ArrayList<String[]>();
		if(list != null && list.size() > 0){
			for (Object obj : list) {
				List<?> objList = (List<?>)obj;
				String[] args = new String[2];
				args[0] = (String) objList.get(GetRpaScenarioListTableDefine.MANAGER_NAME);
				args[1] = (String) objList.get(GetRpaScenarioListTableDefine.SCENARIO_ID);
				argsList.add(args);
			}
		}

		// 選択アイテムがある場合に、削除処理を呼び出す
		if(argsList.isEmpty() ) {
			return null;
		}
		// 削除を実行してよいかの確認ダイアログの表示
		String msg = null;
		String[] msgArgs = new String[2];
		if(argsList.isEmpty() == false) {
			if (argsList.size() == 1) {
				msgArgs[0] = argsList.get(0)[1] + "(" + argsList.get(0)[0] + ")";
				msg = "message.rpa.scenario.9";
			} else {
				msgArgs[0] = Integer.toString(argsList.size());
				msg = "message.rpa.scenario.10";
			}
		}

		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString(msg, msgArgs))) {

			// OKが押されない場合は終了
			return null;
		}

		Map<String, List<String>> deleteMap = null;

		for(String[] args : argsList) {
			String managerName = args[0];
			String scenarioId = args[1];
			if(deleteMap == null) {
				deleteMap = new ConcurrentHashMap<String, List<String>>();
			}
			if(deleteMap.get(managerName) == null) {
				deleteMap.put(managerName, new ArrayList<String>());
			}
			deleteMap.get(managerName).add(scenarioId);
		}

		String errMessage = "";
		int errCount = 0;
		int successCount = 0;
		for(Map.Entry<String, List<String>> map : deleteMap.entrySet()) {
			try {
				RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(map.getKey());
				wrapper.deleteRpaScenario(String.join(",", map.getValue()));
				successCount = successCount + map.getValue().size();
			} catch(InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return null;
			} catch(Exception e) {
				errCount = errCount + map.getValue().size();
				errMessage = HinemosMessage.replace(e.getMessage());
			}
		}
		String message = null;
		if (errCount > 0) {
			if (errCount == 1) {
				msgArgs[1] = errMessage;
				message = Messages.getString("message.rpa.scenario.11", msgArgs);
			} else {
				message = Messages.getString("message.rpa.scenario.12", new String[]{Integer.toString(errCount), errMessage});
			}
			MessageDialog.openError(null, Messages.getString("failed"), message);
		}
		if (successCount > 0) {
			if (successCount == 1) {
				message = Messages.getString("message.rpa.scenario.7", msgArgs);
			} else {
				message = Messages.getString("message.rpa.scenario.8", new String[]{Integer.toString(successCount)});
			}
			MessageDialog.openInformation(null, Messages.getString("successful"), message);
			view.update();
		}
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
				if(part instanceof RpaScenarioListView){
					// Enable button when 1 item is selected
					RpaScenarioListView view = (RpaScenarioListView)part;

					if(view.getSelectedNum() > 0) {
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
