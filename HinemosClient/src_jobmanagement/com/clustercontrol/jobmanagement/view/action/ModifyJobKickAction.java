/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobKickResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.action.GetJobKickTableDefine;
import com.clustercontrol.jobmanagement.bean.JobKickTypeMessage;
import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.dialog.JobKickDialog;
import com.clustercontrol.jobmanagement.view.JobKickListView;

/**
 * ジョブ[実行契機]ビューの「変更」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class ModifyJobKickAction extends AbstractHandler implements IElementUpdater{
	/** ログ */
	private static Log m_log = LogFactory.getLog(ModifyJobKickAction.class);
	/** アクションID */
	public static final String ID = ModifyJobKickAction.class.getName();
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * ジョブ[実行契機]ビューの「変更」が押された場合に、スケジュールを作成します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[実行契機]ビューから選択されたスケジュール、</li>
	 * <li>または、ファイルチェックを取得します。</li>
	 * <li>ジョブ[実行契機の作成・変更]ダイアログを表示します。</li>
	 * <li>ジョブ[実行契機]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.dialog.ScheduleDialog
	 * @see com.clustercontrol.jobmanagement.dialog.FileCheckDialog
	 * @see com.clustercontrol.jobmanagement.view.JobKickListView
	 * @see com.clustercontrol.jobmanagement.composite.JobKickListComposite
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart =  this.window.getActivePage().getActivePart();

		if (viewPart instanceof JobKickListView) {
			JobKickListView  view = null;
			try {
				view = (JobKickListView) viewPart
						.getAdapter(JobKickListView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}

			if (view == null) {
				m_log.info("execute: view is null"); 
				return null;
			}

			JobKickListComposite composite = view.getComposite();

			ArrayList<?> item = composite.getSelectItem();
			if (item != null) {
				String id = view.getSelectedIdList().get(0);
				List<?> list = (ArrayList<?>)item.get(0);
				String managerName = (String) list.get(GetJobKickTableDefine.MANAGER_NAME);
				String type = (String) list.get(GetJobKickTableDefine.TYPE);
				JobKickResponse.TypeEnum TypeNum = JobKickResponse.TypeEnum
						.fromValue(JobKickTypeMessage.stringToTypeEnumValue(type));
				JobKickDialog dialog = new JobKickDialog(this.window.getShell(), managerName, id, TypeNum, PropertyDefineConstant.MODE_MODIFY);
				dialog.open();
			}
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
				if( part instanceof JobKickListView  ){
					// Enable button when 1 item is selected
					JobKickListView view = (JobKickListView)part;
					if(view.getSelectedNum() == 1) {
						editEnable = true;
					}
				}
				this.setBaseEnabled( editEnable );
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
