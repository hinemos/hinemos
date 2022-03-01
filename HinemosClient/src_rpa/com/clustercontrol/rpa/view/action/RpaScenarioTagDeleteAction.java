/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.view.RpaScenarioTagView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;

/**
 * RPA設定[シナリオタグ]ビューの削除アクションクラス
 */
public class RpaScenarioTagDeleteAction extends AbstractHandler implements IElementUpdater{

	// ログ
	private static Log log = LogFactory.getLog(RpaScenarioTagDeleteAction.class);

	/** アクションID */
	public static final String ID = RpaScenarioTagDeleteAction.class.getName();

	/** ビュー */
	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;
	
	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}
		
		this.viewPart = HandlerUtil.getActivePart(event);
		
		// 選択アイテムの取得
		RpaScenarioTagView view = (RpaScenarioTagView) this.viewPart
				.getAdapter(RpaScenarioTagView.class);
		if (view == null) {
			log.info("execute: view is null"); 
			return null;
		}
		Map<String, List<String>> map = view.getSelectedItem();
		
		String[] args = new String[2];
		String msg = null;

		int i = 0;
		String tagId = null;
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			for(String id : entry.getValue()) {
				tagId = id;
				i++;
			}
		}
		
		if (i > 0) {
			
			if(i == 1) {
				msg = "message.rpa.tag.9";
				args[0] = tagId;
			} else {
				msg = "message.rpa.tag.10";
				args[0] = Integer.toString(i);
			}
			
			Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
			StringBuffer messageArg = new StringBuffer();
			i = 0;
			List<String> deleteTagIdList = new ArrayList<String>();
			List<RpaScenarioTagResponse> result = new ArrayList<>();
			
			// 選択アイテムがある場合に、削除処理を呼び出す
			if (MessageDialog.openConfirm(null,
					Messages.getString("confirmed"),
					Messages.getString(msg, args))) {
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					String managerName = entry.getKey();
					RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
					
					if(i > 0) {
						messageArg.append(", ");
					}
					messageArg.append(managerName);
					
					// 親タグに設定されているタグを削除する場合の検索用にタグデータを取得
					List<RpaScenarioTagResponse> list = null;
					try {
						list = wrapper.getRpaScenarioTagList(null);
					} catch (Exception e) {
						if (e instanceof InvalidRole) {
							// 権限なし
							errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
						} else {
							String errMessage = HinemosMessage.replace(e.getMessage());
							errorMsgs.put(managerName, Messages.getString("message.rpa.tag.8") +
									", " + errMessage);
						}
					}
					
					Map<String,List<String>> tagPathMap = new HashMap<>();
					List<String> deleteChildTagIdList = new ArrayList<String>();
					
					// 削除処理
					try {
						for (String entryTagId : entry.getValue()) {
							deleteChildTagIdList.clear();
							for (RpaScenarioTagResponse tag : list) {
								List<String> tagPathList = Arrays.asList(tag.getTagPath().split("\\\\"));
								tagPathMap.put(tag.getTagId(), tagPathList);
							}
							for (Map.Entry<String, List<String>> tagEntry : tagPathMap.entrySet()) {
								if (tagEntry.getValue().contains(entryTagId)){
									deleteChildTagIdList.add(tagEntry.getKey());
								}
							}
							
							String[] deleteChildTagsArg = new String[1];
							deleteChildTagsArg[0] = entryTagId;
							// 子タグが存在しない場合
							if (deleteChildTagIdList.isEmpty() && !deleteTagIdList.contains(entryTagId)){
								deleteTagIdList.add(entryTagId);
								
								continue;
							} else if(deleteChildTagIdList.isEmpty()){
								continue;
							}
							
							// 子タグが存在する場合
							if (MessageDialog.openConfirm(null,
									Messages.getString("confirmed"),
									Messages.getString("message.rpa.tag.12", deleteChildTagsArg))
									&& !deleteTagIdList.contains(entryTagId)) {
								deleteTagIdList.add(entryTagId);
								for (String childTagId : deleteChildTagIdList){
									if(!deleteTagIdList.contains(childTagId)){
										deleteTagIdList.add(childTagId);
									}
								}
							}
						}
						
						if (! deleteTagIdList.isEmpty()){
							result = wrapper.deleteRpaScenarioTag(String.join(",", deleteTagIdList));
						}
					} catch (RpaScenarioTagNotFound | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
						if (e instanceof InvalidRole) {
							// 権限なし
							errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
						} else {
							String errMessage = HinemosMessage.replace(e.getMessage());
							errorMsgs.put(managerName, Messages.getString("message.rpa.tag.8") +
									", " + errMessage);
						}
					}
					i++;
				}
				
				//メッセージ表示
				if(errorMsgs.size() > 0 ){
					UIManager.showMessageBox(errorMsgs, true);
				} else if (!result.isEmpty()){
					args[0] = Integer.toString(result.size());
					args[1] = messageArg.toString();
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.rpa.tag.7", args));
				}
				// ビューを更新
				view.update();
			}
		}
		
		return null;
	}
	
	/**
	 * Dispose
	 */
	@Override
	public void dispose(){
		this.viewPart = null;
		this.window = null;
	}
	
	/**
	 * Update handler status
	 */
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				if( part instanceof RpaScenarioTagView ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 0 < ((RpaScenarioTagView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
