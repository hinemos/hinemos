/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.rmi.AccessException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.CommunicationException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.dialog.LoginAccount;
import com.clustercontrol.accesscontrol.dialog.LoginDialog;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.ui.util.OptionUtil;
import com.clustercontrol.ws.access.InvalidUserPass_Exception;
import com.clustercontrol.fault.InvalidTimezone;

/**
 * ログインマネージャクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class LoginManager {
	private static Log m_log = LogFactory.getLog( LoginManager.class );

	public static final String KEY_LOGIN_STATUS_NUM = "numOfLoginStatus";
	public static final String KEY_LOGIN_STATUS_UID = "LoginStatusUid";
	public static final String KEY_LOGIN_STATUS_URL = "LoginStatusUrl";
	public static final String KEY_LOGIN_STATUS_MANAGERNAME = "LoginStatusManagerName";

	public static final String KEY_URL = "Url";
	public static final String KEY_URL_NUM = "numOfUrlHistory";

	public static final String VALUE_UID = "hinemos";
	public static final String VALUE_URL = "http://localhost:8080/HinemosWS/";


	public static final String KEY_INTERVAL = "managerPollingInterval";
	public static final int VALUE_INTERVAL = 1; //マネージャへの疎通(Dummy)ポーリング周期（分）

	public static final String KEY_HTTP_REQUEST_TIMEOUT = "httpRequestTimeout";	// Utilityオプションからも使用されています。
	public static final int VALUE_HTTP_REQUEST_TIMEOUT = 60000; // ms

	public static final String KEY_PROXY_ENABLE = "proxyEnable";
	public static final boolean VALUE_PROXY_ENABLE = false;
	public static final String KEY_PROXY_HOST = "proxyHost";
	public static final String VALUE_PROXY_HOST = "";
	public static final String KEY_PROXY_PORT = "proxyPort";
	public static final int VALUE_PROXY_PORT = 8080;
	public static final String KEY_PROXY_USER = "proxyUser";
	public static final String VALUE_PROXY_USER = "";
	public static final String KEY_PROXY_PASSWORD = "proxyPassword";
	public static final String VALUE_PROXY_PASSWORD = "";
	
	/** Auto-login */
	public static final String ENV_HINEMOS_MANAGER_URL = "HINEMOS_MANAGER_URL";
	public static final String ENV_HINEMOS_MANAGER_USER = "HINEMOS_USER";
	public static final String ENV_HINEMOS_MANAGER_PASS = "HINEMOS_PASS";
	
	public static final String KEY_BASIC_AUTH = "BasicAuth";
	public static final String KEY_URL_LOGIN_URL = "LoginUrl";
	public static final String KEY_URL_UID = "Uid";
	public static final String KEY_URL_MANAGER_NAME = "ManagerName";

	public static final String URL_HINEMOS = "hinemos";
	public static final String URL_ACCOUNT = "account";
	public static final String URL_CALENDAR = "calendar";
	public static final String URL_JOB_HISTORY = "job_history";
	public static final String URL_JOB_SETTING = "job_setting";
	public static final String URL_STARTUP = "startup";
	public static final String URL_MAINTENANCE = "maintenance";
	public static final String URL_REPOSITORY = "repository";
	public static final String URL_COLLECT = "collect";
	public static final String URL_APPROVAL = "approval";
	public static final String URL_INFRA = "infra";
	public static final String URL_MONITOR_HISTORY = "monitor_history";
	public static final String URL_MONITOR_SETTING = "monitor_setting";
	public static final String URL_HUB = "hub";

	// Count login attempts
	private int loginAttempts = 0;

	static {
		ClientProxyManager.init();
	}

	/**
	 * Session Singleton
	 */
	private static LoginManager getInstance(){
		return SingletonUtil.getSessionInstance( LoginManager.class );
	}
	
	private static void connect(String managerName) throws Exception {
		synchronized (getInstance()) {
			EndpointUnit endpointUnit = EndpointManager.get(managerName);
			try {
				endpointUnit.connect();
				String url = endpointUnit.getUrlListStr();
				IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
				// 接続先リストをプレファレンスに書き戻す
				// 今回の接続先URLがURL履歴に存在するかチェックする。
				int numOfUrlHistory = store.getInt(LoginManager.KEY_URL_NUM);
				boolean urlExist = false;
				for(int i=0; i<numOfUrlHistory; i++){
					String histUrl = store.getString(LoginManager.KEY_URL + "_" + i);
					if(url.equals(histUrl)){
						//TODO 存在する場合、対象のURLを履歴の先頭に移動させ、他のURLを一つずつ後方にずらす
						urlExist = true;
						break;
					}
				}
				//存在しない場合、URL履歴の末尾に追加
				if(!urlExist && !url.equals("")){
					store.setValue(LoginManager.KEY_URL + "_" + numOfUrlHistory ,url);
					numOfUrlHistory++;
					store.setValue(LoginManager.KEY_URL_NUM, numOfUrlHistory);
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}

	/**
	* ログアウト
	*
	* @throws NamingException
	*
	* @version 5.0.0
	* @since 5.0.0
	*/
	public static void disconnect( String managerName ) {
		synchronized (getInstance()) {
			try {
				JobEditStateUtil.release(managerName);
				FacilityTreeCache.removeCache(managerName);
			} catch (Exception e) {
				// ログアウト時の例外なのでログ出力だけにとどめる
				m_log.info(e.getMessage(), e);
			}
			EndpointManager.logout(managerName);

			updateStatusBar();

			// 接続マネージャ数が0になった場合は、クライアントが保持するタイムゾーンを開放する
			if( !isLogin() ){
				m_log.debug("disconnect : client's timezone is released.");
				TimezoneUtil.releaseTimeZone();
			}
		}
	}

	/**
	* Get loginAttempts
	*
	* @return loginAttempts
	*/
	public static int getLoginAttempts() {
		return getInstance().loginAttempts;
	}

	/**
	* ログインチェック
	*
	* 本メソッドはUtilityオプションからも使用されています。
	*
	* @return ログイン中か否か
	*/
	public static boolean isLogin() {
		return 0 < EndpointManager.sizeOfActive();
	}

	public static void updateStatusBar(){
		StringBuffer statusMsg = new StringBuffer();
		statusMsg.append(Messages.getString("connection.manager.name") + "(" + 
				EndpointManager.sizeOfActive() + "/" + EndpointManager.sizeOfAll() + ") : ");
		m_log.debug("updateStatusBar : " + statusMsg.toString());
		boolean firstFlag = true;
		for( EndpointUnit endpointUnit : EndpointManager.getActiveManagerList()){
			if (firstFlag) {
				firstFlag = false;
			} else {
				statusMsg.append(", ");
			}
			statusMsg.append(endpointUnit.getManagerName() + "(" + endpointUnit.getUserId() + ")");
		}
		UIManager.updateLoginStatus(statusMsg.toString());
	}

	private static boolean addConnect( List<LoginAccount> loginList ) {
		LinkedHashMap<String, String> msgs = new LinkedHashMap<>();
		ArrayList<Status> statusList = new ArrayList<>();
		boolean connectFlag = false;
		boolean isError = false;

		m_log.debug("addConnections loginList.size=" + loginList.size());

		for( LoginAccount account : loginList ){
			connectFlag = true;
			String managerName = account.getManagerName();
			try {
				EndpointManager.add( account.getUserId(), account.getPassword(), managerName, account.getUrl() );
				if (!EndpointManager.get(managerName).isActive()) {
					connect(managerName);
					// ログイン成功ダイアログを生成
					msgs.put( managerName, Messages.getString("message.accesscontrol.5"));
					m_log.info("Login Success : userId = " + account.getUserId() + ", url = " + account.getUrl());
				} else {
					m_log.info("Login already : userId = " + account.getUserId() + ", url = " + account.getUrl());
				}
			} catch (CommunicationException e) {
				// 接続失敗ダイアログを生成
				Status status = new Status(
						IStatus.ERROR,
						ClusterControlPlugin.getPluginId(),
						IStatus.OK,
						managerName + " : " + Messages.getString("message.accesscontrol.22"),
						e);
				statusList.add(status);
				msgs.put( managerName, Messages.getString("message.accesscontrol.21") );
				isError = true;
				m_log.info("Login Fail : userId = " + account.getUserId() + ", url = " + account.getUrl());
			} catch (AccessException e) {
				// ログイン失敗ダイアログを生成
				msgs.put( managerName, Messages.getString("message.accesscontrol.6") );
				isError = true;
				m_log.info("Login Fail : userId = " + account.getUserId() + ", url = " + account.getUrl());
			} catch (InvalidUserPass_Exception e){
				// ログイン失敗ダイアログを生成
				msgs.put( managerName, Messages.getString("message.accesscontrol.45") );
				isError = true;
				m_log.info("Login Fail : userId = " + account.getUserId() + ", url = " + account.getUrl());
			} catch (InvalidTimezone e){
				// ログイン失敗ダイアログを生成
				msgs.put( managerName, Messages.getString("message.accesscontrol.65") );
				isError = true;
				m_log.info("Login Fail : userId = " + account.getUserId() + ", url = " + account.getUrl());
			} catch (Exception e) {
				// 予期せぬエラーダイアログを生成
				Status status = new Status(
						IStatus.ERROR,
						ClusterControlPlugin.getPluginId(),
						IStatus.OK,
						managerName + " : " + Messages.getString("message.accesscontrol.23"),
						e);
				statusList.add(status);
				msgs.put( managerName, Messages.getString("message.accesscontrol.6") );
				isError = true;
				m_log.info("Login Fail : userId = " + account.getUserId() + ", url = " + account.getUrl());
			}
		}
		if( 0 < msgs.size() ){
			MultiStatus multiStatus = null;
			if( 0 < statusList.size() ){
				multiStatus = new MultiStatus(ClusterControlPlugin.getPluginId(), IStatus.ERROR, statusList.toArray( new Status[statusList.size()] ), Messages.getString( "message.accesscontrol.56" ), null);
			}
			UIManager.showMessageBox( msgs, isError, multiStatus );
		}

		return connectFlag;
	}

	// ログイン状態を保持する
	// 次回ログインダイアログを開いたときに利用する
	public static void saveLoginState() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		// Clear old records
		store.setValue(KEY_LOGIN_STATUS_NUM, 0);

		// Add history
		int stateNum = 0;
		for( EndpointUnit endpointUnit : EndpointManager.getAllManagerList() ){
			m_log.debug("saveLoginState() : " + stateNum + ", " + endpointUnit.getManagerName());
			store.setValue(LoginManager.KEY_LOGIN_STATUS_UID + "_" + stateNum, endpointUnit.getUserId());
			store.setValue(LoginManager.KEY_LOGIN_STATUS_URL + "_" + stateNum, endpointUnit.getUrlListStr());
			store.setValue(LoginManager.KEY_LOGIN_STATUS_MANAGERNAME + "_" + stateNum, endpointUnit.getManagerName());
			stateNum++;
		}
		m_log.info("Save login state " + stateNum);
		store.setValue(KEY_LOGIN_STATUS_NUM, stateNum);
		
		
		// ジョブツリーの更新
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = page.findView(JobListView.ID);
			if (viewPart instanceof JobListView) {
				JobListView jobListView = (JobListView)viewPart;
				m_log.info("login() job update");
				jobListView.update();
			}
		} catch (Exception e) {
			m_log.warn("login() job " + e.getMessage(), e);
		}
	}
	
	public static void login(Map<String, String> map, IWorkbenchWindow window) {
		setup();

		Shell shell = window.getShell();
		int returnCode = IDialogConstants.RETRY_ID;

		// Increase login attempt time
		getInstance().loginAttempts ++;

		//ログインダイアログ表示
		LoginDialog dialog = new LoginDialog(shell , map);

		if (map.containsKey(KEY_BASIC_AUTH) && map.get(KEY_BASIC_AUTH).equals("true")) {
			// ログイン省略
			returnCode = IDialogConstants.OK_ID;
		}

		// Reopen if RETRY_ID returned
		while ( returnCode == IDialogConstants.RETRY_ID ) {
			returnCode = dialog.open();
		}

		// Proceed connecting if not Close button pressed
		if( returnCode == IDialogConstants.OK_ID ){
			if( addConnect(dialog.getLoginList()) ){
				ClientSession.doCheck();

				// マネージャの死活監視開始
				IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
				int interval = store.getInt(KEY_INTERVAL);
				m_log.trace("LoginManager.getInterval() interval = " + interval);
				ClientSession.startChecktask( interval );

				// Save login info
				saveLoginState();
			}
		}

		// ログイン状態を更新
		updateStatusBar();

		// Close all if no connection left
		if( returnCode != IDialogConstants.OK_ID && !LoginManager.isLogin() ){
			m_log.info("login() : cancel, " + returnCode);
			return;
		}

		// ログインに成功した場合のみ、オプション用UI contributionsの有・無効化を行う
		OptionUtil.enableActivities(window, EndpointManager.getAllOptions());
	}

	public static void login(IWorkbenchWindow window) {
		login(new HashMap<String, String>(), window);
	}

	public static void setup() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		// Timeout設定 (ms)
		int httpRequestTimeout = store.getInt(KEY_HTTP_REQUEST_TIMEOUT);
		EndpointManager.setHttpRequestTimeout( httpRequestTimeout );
		m_log.info("request.timeout=" + httpRequestTimeout);

		// Proxy設定
		if (store.getBoolean(KEY_PROXY_ENABLE) == true) {
			String proxyHost = store.getString(KEY_PROXY_HOST);
			int proxyPort = store.getInt(KEY_PROXY_PORT);
			String proxyUser = store.getString(KEY_PROXY_USER);
			String proxyPass = store.getString(KEY_PROXY_PASSWORD);
			EndpointManager.setProxy(proxyHost, proxyPort);
			EndpointManager.setAuthenticator(proxyUser, proxyPass);
		} else {
			EndpointManager.setProxy(null, 0);
			EndpointManager.setAuthenticator(null, null);
		}
	}

	/**
	* マネージャ切断をダイアログ表示してログアウト処理を行う
	*/
	public static void forceLogout( final String managerName ){
		UIManager.checkAsyncExec(new Runnable(){
			@Override
			public void run() {
				// 切断処理
				disconnect(managerName);

				// ログアウトダイアログの表示
				if( UIManager.openInfoDiag(managerName) ){
					m_log.trace("LoginManager.forceLogout() Open Dialog");

					// ダイアログ表示
					String[] args = { managerName, new Timestamp(System.currentTimeMillis()).toString() };
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.force.disconnect",args));

					UIManager.closeInfoDiag(managerName);
				}else{
					m_log.trace("LoginManager.disconnect() Dialog is occupied");
				}
			}}
		);
	}
}
