/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.jobmap.dialog.JobMapImageDialog;
import com.clustercontrol.jobmap.view.JobMapImageListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.RestConnectManager;

/**
 * ジョブマップ用イメージファイル登録を行うクライアント側アクションクラス<BR>
 *
 * @version 6.0.a
 */
public class JobMapImageAddAction extends AbstractHandler{
	private static Log m_log = LogFactory.getLog(JobMapImageAddAction.class);
	
	public static final String ID = BaseAction.ActionIdBase + JobMapImageAddAction.class.getSimpleName();

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
		JobMapImageListView listView = null; 
		try { 
			listView = (JobMapImageListView) this.viewPart.getAdapter(JobMapImageListView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + HinemosMessage.replace(e.getMessage())); 
			return null; 
		}

		String managerName = RestConnectManager.getActiveManagerNameList().get(0);

		// ダイアログを生成
		JobMapImageDialog dialog = new JobMapImageDialog(this.viewPart.getSite().getShell(), managerName);
		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		dialog.open();

		listView.update();

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

}
