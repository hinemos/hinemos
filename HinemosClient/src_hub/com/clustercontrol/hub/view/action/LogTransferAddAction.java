/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.view.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.hub.dialog.TransferInfoDialog;
import com.clustercontrol.hub.view.TransferView;
import com.clustercontrol.util.EndpointManager;

/**
 * 収集蓄積[転送]の作成・変更ダイアログによる、収集蓄積[転送]設定登録を行うクライアント側アクションクラス<BR>
 *
 */
public class LogTransferAddAction extends AbstractHandler{
	public static final String ID = LogTransferAddAction.class.getName();

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
		TransferView view = (TransferView) this.viewPart
				.getAdapter(TransferView.class);

		String managerName = EndpointManager.getActiveManagerNameList().get(0);


		// ダイアログを生成
		TransferInfoDialog dialog = new TransferInfoDialog(this.viewPart.getSite()
				.getShell(), managerName, null, PropertyDefineConstant.MODE_ADD);
		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		dialog.open();

		view.update();

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
