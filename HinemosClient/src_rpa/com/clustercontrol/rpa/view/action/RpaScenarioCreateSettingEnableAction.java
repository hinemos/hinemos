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
import org.openapitools.client.model.SetRpaScenarioOperationResultCreateSettingValidRequest;

import com.clustercontrol.rpa.action.GetRpaScenarioOperationResultCreateSettingListTableDefine;
import com.clustercontrol.rpa.composite.RpaScenarioOperationResultCreateSettingListComposite;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.view.RpaScenarioOperationResultCreateSettingView;
import com.clustercontrol.util.Messages;

/**
 * RPA設定[シナリオ実績作成設定]ビューの有効アクションクラス<BR>
 */
public class RpaScenarioCreateSettingEnableAction extends AbstractHandler implements IElementUpdater {

	/** ログ */
	private static Log log = LogFactory.getLog(RpaScenarioCreateSettingEnableAction.class);

	/** アクションID */
	public static final String ID = RpaScenarioCreateSettingEnableAction.class.getName();

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
				RpaScenarioOperationResultCreateSettingView view = null;
				try {
					view = (RpaScenarioOperationResultCreateSettingView) this.viewPart.getAdapter(RpaScenarioOperationResultCreateSettingView.class);
				} catch (Exception e) { 
					log.info("execute " + e.getMessage()); 
					return null; 
				}

				if (view == null) {
					log.info("execute: view is null"); 
					return null;
				}

				RpaScenarioOperationResultCreateSettingListComposite composite = (RpaScenarioOperationResultCreateSettingListComposite) view.getListComposite();
				StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

				List<?> list = (List<?>) selection.toList();
				List<String[]> argsList = new ArrayList<String[]>();
				List<String> idList = new ArrayList<>();
				if(list != null && list.size() > 0){
					for (Object obj : list) {
						List<?> objList = (List<?>)obj;
						String[] args = new String[2];
						args[0] = (String) objList.get(GetRpaScenarioOperationResultCreateSettingListTableDefine.MANAGER_NAME);
						args[1] = (String) objList.get(GetRpaScenarioOperationResultCreateSettingListTableDefine.SETTING_ID);
						argsList.add(args);
						idList.add(args[1]);
					}
				}

				// 選択アイテムがある場合に、有効化処理を呼び出す
				if(argsList.isEmpty() ) {
					return null;
				}

				// 確認ダイアログの表示
				if (!MessageDialog.openConfirm(
						null,
						Messages.getString("confirmed"),
						Messages.getString("message.rpa.scenario.create.setting.enable.confirm", new String[]{String.join("\n", idList)}))) {

					// OKが押されない場合は終了
					return null;
				}

				Map<String, List<String>> enableMap = null;

				for(String[] args : argsList) {
					String managerName = args[0];
					String scenarioId = args[1];
					if(enableMap == null) {
						enableMap = new ConcurrentHashMap<String, List<String>>();
					}
					if(enableMap.get(managerName) == null) {
						enableMap.put(managerName, new ArrayList<String>());
					}
					enableMap.get(managerName).add(scenarioId);
				}

				// findbugs対応 文字列の連結方式をStringBuilderを利用する方法に変更
				StringBuilder errMessage = new StringBuilder("");
				StringBuilder successManager = new StringBuilder("");
				for(Map.Entry<String, List<String>> map : enableMap.entrySet()) {
					try {
						if (!(successManager.length()==0)) {
							successManager.append("\n");
						}
						List<String> targetIdList = map.getValue();
						RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(map.getKey());
						// APIリクエストを作成
						SetRpaScenarioOperationResultCreateSettingValidRequest request = new SetRpaScenarioOperationResultCreateSettingValidRequest();
						request.setSettingIdList(targetIdList);
						request.setValidFlg(true);
						
						wrapper.setRpaScenarioOperationResultCreateSettingValid(request);
						successManager.append(String.join("\n", targetIdList));
					} catch(Exception e) {
						if (!(errMessage.length()==0)) {
							errMessage.append("\n");
						}
						errMessage.append(String.format("%s (%s)",e.getMessage(), map.getKey()));
					}
				}
				if (!(errMessage.length()==0)) {
					MessageDialog.openError(null, Messages.getString("failed"), errMessage.toString());
				}
				if (!(successManager.length()==0)) {
					MessageDialog.openInformation(null, Messages.getString("successful"), 
							Messages.getString("message.rpa.scenario.create.setting.enable.finish", new String[]{successManager.toString()}));
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
				if(part instanceof RpaScenarioOperationResultCreateSettingView){
					// Enable button when 1 item is selected
					RpaScenarioOperationResultCreateSettingView view = (RpaScenarioOperationResultCreateSettingView)part;

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
