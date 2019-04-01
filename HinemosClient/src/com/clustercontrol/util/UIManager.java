/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

public class UIManager {
	// ログ
	private static Log m_log = LogFactory.getLog(UIManager.class);

	private static final String STATUS_ITEM_ID_LOGIN = "com.clustercontrol.accesscontrol.statusbar.login";

	private Display display = null;
	private IStatusLineManager lineManager = null;

	/** ダイアログ表示制御用 */
	private ConcurrentHashMap<String, String> infoDiagOpened = new ConcurrentHashMap<>();

	/**
	 * Private constructor
	 */
	private UIManager() {}

	/**
	 * LoginManager singleton
	 */
	private static UIManager getInstance(){
		return SingletonUtil.getSessionInstance( UIManager.class );
	}

	public static boolean openInfoDiag(String managerName){
		UIManager uiManager = getInstance();
		String str = uiManager.infoDiagOpened.get(managerName);
		if( str != null ){
			// occurried
			return false;
		}else{
			// lock it
			uiManager.infoDiagOpened.put(managerName, managerName);
			return true;
		}
	}

	public static void closeInfoDiag(String managerName){
		UIManager uiManager = getInstance();
		uiManager.infoDiagOpened.remove(managerName);
	}

	private void init(){
		// ステータスバーへユーザIDを登録
		IWorkbench workbench = PlatformUI.getWorkbench();
		WorkbenchWindow workbenchWindow = (WorkbenchWindow)workbench.getActiveWorkbenchWindow();
		display = workbench.getDisplay();

		IActionBars bars = workbenchWindow.getActionBars();
		lineManager = bars.getStatusLineManager();
	}

	public static void updateLoginStatus( String statusMsg ){
		UIManager uiManager = getInstance();
		if( null == uiManager.display ){
			uiManager.init();
		}

		IContributionItem item;
		item = uiManager.lineManager.find(STATUS_ITEM_ID_LOGIN);
		if( null == statusMsg ){
			// Clear status bar
			if( null != item ){
				uiManager.lineManager.remove(item);
			}
		}else{
			StatusLineContributionItem statusLineItem;
			// Update status bar
			if(null != item) {
				statusLineItem = ((StatusLineContributionItem) item);
			}else{
				statusLineItem = new StatusLineContributionItem( STATUS_ITEM_ID_LOGIN, 200 );
				uiManager.lineManager.add(statusLineItem);
			}
			statusLineItem.setText( statusMsg );
		}

		uiManager.lineManager.update(true);
	}

	/**
	 * SessionSingleton.SessionCheckTaskからのダイアログ&ログアウト処理を受け付けるスレッド生成用メソッド
	 * @param r
	 * @return
	 */
	public static void checkAsyncExec(Runnable r){
		UIManager uiManager = getInstance();
		if( uiManager.display == null){
			m_log.trace("LoginManager.checkAsyncExec() m_dislpay is null");
			throw new InternalError("uiManager.display is null");
		}

		if(! uiManager.display.isDisposed()){
			m_log.trace("LoginManager.checkAsyncExec() is true");
			 uiManager.display.asyncExec(r);
		}else{
			m_log.trace("LoginManager.checkAsyncExec() is false");
		}
	}

	/**
	 *　ダイアログメッセージを表示します。
	 * @param msgs 表示メッセージ
	 * @param isError エラーレベルで表示する場合はtrue、それ以外はfalse
	 */
	public static void showMessageBox( Map<String, String> msgs, boolean isError ){
		showMessageBox( msgs, isError, null );
	}

	/**
	 *　ダイアログメッセージを表示します。
	 * @param msgs 表示メッセージ
	 * @param isError エラーレベルで表示する場合はtrue、それ以外はfalse
	 */
	public static void showMessageBox( Map<String, String> msgs, boolean isError, IStatus status ){
		StringBuffer msg = new StringBuffer();
		m_log.debug("showMessageBox msgs.size=" + msgs.size());

		for (Map.Entry<String, String> entry : msgs.entrySet()) {
			if (msg.length() > 0)
				msg.append("\n");
			msg.append(entry.getKey() + " : " + entry.getValue());
		}
		String messageStr = msg.toString();
		m_log.debug("msg=" + messageStr);
		if( null != status ){
			ErrorDialog.openError( null, Messages.getString( "error" ), messageStr, status );
		}else{
			MessageDialog.open( isError ?
					MessageDialog.ERROR :
						MessageDialog.INFORMATION, null, Messages.getString( "message" ), messageStr, SWT.NONE );
		}
	}
}
