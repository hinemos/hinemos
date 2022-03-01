/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;

import java.rmi.AccessException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.openapitools.client.model.ManagerInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.dialog.LoginAccount;
import com.clustercontrol.accesscontrol.dialog.LoginAccount.ACCESS_POINT;
import com.clustercontrol.accesscontrol.dialog.LoginDialog;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.fault.InvalidTimezone;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.filtersetting.util.FilterSettingManagerNameUpdater;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.msgfilter.extensions.IRestConnectMsgFilter;
import com.clustercontrol.msgfilter.extensions.RestConnectMsgFilterExtension;
import com.clustercontrol.ui.util.OptionUtil;

public class RestLoginManager {
	private static Log m_log = LogFactory.getLog( RestLoginManager.class );

	public static final String KEY_URL = LoginConstant.KEY_URL;
	public static final String KEY_URL_NUM = LoginConstant.KEY_URL_NUM;

	public static final String VALUE_UID = LoginConstant.VALUE_UID;
	public static final String VALUE_URL = LoginConstant.VALUE_URL;


	public static final String KEY_INTERVAL = LoginConstant.KEY_INTERVAL;
	public static final int VALUE_INTERVAL = LoginConstant.VALUE_INTERVAL; //マネージャへの疎通(Dummy)ポーリング周期（分）

	public static final String KEY_HTTP_REQUEST_TIMEOUT = LoginConstant.KEY_HTTP_REQUEST_TIMEOUT;	// Utilityオプションからも使用されています。
	public static final int VALUE_HTTP_REQUEST_TIMEOUT = LoginConstant.VALUE_HTTP_REQUEST_TIMEOUT; // ms

	public static final String KEY_PROXY_ENABLE = LoginConstant.KEY_PROXY_ENABLE;
	public static final boolean VALUE_PROXY_ENABLE = LoginConstant.VALUE_PROXY_ENABLE;
	public static final String KEY_PROXY_HOST = LoginConstant.KEY_PROXY_HOST;
	public static final String VALUE_PROXY_HOST = LoginConstant.VALUE_PROXY_HOST;
	public static final String KEY_PROXY_PORT = LoginConstant.KEY_PROXY_PORT;
	public static final int VALUE_PROXY_PORT = LoginConstant.VALUE_PROXY_PORT;
	public static final String KEY_PROXY_USER = LoginConstant.KEY_PROXY_USER;
	public static final String VALUE_PROXY_USER = LoginConstant.VALUE_PROXY_USER;
	public static final String KEY_PROXY_PASSWORD = LoginConstant.KEY_PROXY_PASSWORD;
	public static final String VALUE_PROXY_PASSWORD = LoginConstant.VALUE_PROXY_PASSWORD;
	
	/** Auto-login */
	public static final String ENV_HINEMOS_MANAGER_URL = LoginConstant.ENV_HINEMOS_MANAGER_URL;
	public static final String ENV_HINEMOS_MANAGER_USER = LoginConstant.ENV_HINEMOS_MANAGER_USER;
	public static final String ENV_HINEMOS_MANAGER_PASS = LoginConstant.ENV_HINEMOS_MANAGER_PASS;
	
	public static final String KEY_BASIC_AUTH = LoginConstant.KEY_BASIC_AUTH;
	public static final String KEY_URL_LOGIN_URL = LoginConstant.KEY_URL_LOGIN_URL;
	public static final String KEY_URL_UID = LoginConstant.KEY_URL_UID;
	public static final String KEY_URL_MANAGER_NAME = LoginConstant.KEY_URL_MANAGER_NAME;

	public static final String URL_HINEMOS = LoginConstant.URL_HINEMOS;
	public static final String URL_ACCOUNT = LoginConstant.URL_ACCOUNT;
	public static final String URL_CALENDAR = LoginConstant.URL_CALENDAR;
	public static final String URL_JOB_HISTORY = LoginConstant.URL_JOB_HISTORY;
	public static final String URL_JOB_SETTING = LoginConstant.URL_JOB_SETTING;
	public static final String URL_STARTUP = LoginConstant.URL_STARTUP;
	public static final String URL_MAINTENANCE = LoginConstant.URL_MAINTENANCE;
	public static final String URL_REPOSITORY = LoginConstant.URL_REPOSITORY;
	public static final String URL_COLLECT = LoginConstant.URL_COLLECT;
	public static final String URL_APPROVAL = LoginConstant.URL_APPROVAL;
	public static final String URL_INFRA = LoginConstant.URL_INFRA;
	public static final String URL_MONITOR_HISTORY = LoginConstant.URL_MONITOR_HISTORY;
	public static final String URL_MONITOR_SETTING = LoginConstant.URL_MONITOR_SETTING;
	public static final String URL_HUB = LoginConstant.URL_HUB;
	public static final String URL_XCLOUD_BILLING = LoginConstant.URL_XCLOUD_BILLING;
	public static final String URL_XCLOUD_COMPUTE = LoginConstant.URL_XCLOUD_COMPUTE;
	public static final String URL_XCLOUD_NETWORK = LoginConstant.URL_XCLOUD_NETWORK;
	public static final String URL_XCLOUD_SERVICE = LoginConstant.URL_XCLOUD_SERVICE;
	public static final String URL_XCLOUD_STORAGE = LoginConstant.URL_XCLOUD_STORAGE;
	public static final String URL_JOBMAP_EDITOR = LoginConstant.URL_JOBMAP_EDITOR;
	public static final String URL_JOBMAP_HISTORY = LoginConstant.URL_JOBMAP_HISTORY;
	public static final String URL_NODEMAP = LoginConstant.URL_NODEMAP;
	public static final String URL_SETTING_TOOLS = LoginConstant.URL_SETTING_TOOLS;
	public static final String URL_REPORTING = LoginConstant.URL_REPORTING;
	public static final String URL_RPA_SETTING = LoginConstant.URL_RPA_SETTING;
	public static final String URL_RPA_SCENARIO_OPERATION_RESULT = LoginConstant.URL_RPA_SCENARIO_OPERATION_RESULT;
	public static final String URL_MSG_FILTER = LoginConstant.URL_MSG_FILTER;

	// Count login attempts
	private int loginAttempts = 0;

	/**
	 * Session Singleton
	 */
	private static RestLoginManager getInstance(){
		return SingletonUtil.getSessionInstance( RestLoginManager.class );
	}
	
	private static ManagerInfoResponse connect(String managerName) throws Exception {
		synchronized (getInstance()) {
			RestConnectUnit restConnectUnit = RestConnectManager.get(managerName);
			try {
				ManagerInfoResponse managerInfo = restConnectUnit.connect();
				String url = restConnectUnit.getUrlListStr();
				IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
				// 接続先リストをプレファレンスに書き戻す
				// 今回の接続先URLがURL履歴に存在するかチェックする。
				int numOfUrlHistory = store.getInt(RestLoginManager.KEY_URL_NUM);
				boolean urlExist = false;
				for(int i=0; i<numOfUrlHistory; i++){
					String histUrl = store.getString(RestLoginManager.KEY_URL + "_" + i);
					if(url.equals(histUrl)){
						urlExist = true;
						break;
					}
				}
				//存在しない場合、URL履歴の末尾に追加
				if(!urlExist && !url.equals("")){
					store.setValue(RestLoginManager.KEY_URL + "_" + numOfUrlHistory ,url);
					numOfUrlHistory++;
					store.setValue(RestLoginManager.KEY_URL_NUM, numOfUrlHistory);
				}

				return managerInfo;
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
			RestConnectManager.logout(managerName);

			FilterPropertyUpdater.getInstance().updateFilterProperties();
			FilterSettingManagerNameUpdater.getInstance().updateFilterManagerNames();

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
		return 0 < RestConnectManager.sizeOfActive();
	}

	public static void updateStatusBar(){
		StringBuffer statusMsg = new StringBuffer();
		statusMsg.append(Messages.getString("connection.manager.name") + "(" + 
				RestConnectManager.sizeOfActive() + "/" + RestConnectManager.sizeOfAll() + ") : ");
		m_log.debug("updateStatusBar : " + statusMsg.toString());
		boolean firstFlag = true;
		for( RestConnectUnit endpointUnit : RestConnectManager.getActiveManagerList()){
			if (firstFlag) {
				firstFlag = false;
			} else {
				statusMsg.append(", ");
			}
			statusMsg.append(endpointUnit.getManagerName() + "(" + endpointUnit.getUserId() + ")");
		}
		IRestConnectMsgFilter restConnectMsgFilter = RestConnectMsgFilterExtension.getInstance().getRestConnectMsgFilter();
		if(restConnectMsgFilter != null) {
			int sizeofActive = 0;
			if(restConnectMsgFilter.isActive()){
				sizeofActive = 1;
			}
			int sizeOfAll = 0;
			if(restConnectMsgFilter.isSaved()){
				sizeOfAll = 1;
			}
			statusMsg.append("; ");
			statusMsg.append(Messages.getString("connection.filtermanager.name") + "(" + 
					sizeofActive + "/" + sizeOfAll + ") : ");
			if(restConnectMsgFilter.isSaved()){
				statusMsg.append(restConnectMsgFilter.getManagerName() + "(" + restConnectMsgFilter.getUserId() + ")");
			}
		}
		
		UIManager.updateLoginStatus(statusMsg.toString());
	}

	private static boolean addConnect( List<LoginAccount> loginList ) {
		LinkedHashMap<String, String> msgs = new LinkedHashMap<>();
		LinkedHashMap<String, String> evaluationMsgs = new LinkedHashMap<>();
		ArrayList<Status> statusList = new ArrayList<>();
		boolean connectFlag = false;
		boolean isError = false;
		Pattern p = Pattern.compile("(\\d{4})(\\d{2})[" + ActivationKeyConstant.EVALUATION_SUFFIX + "|" + ActivationKeyConstant.EVALUATION_EXPIRED_SUFFIX + "]");

		m_log.debug("addConnections loginList.size=" + loginList.size());

		for( LoginAccount account : loginList ){
			connectFlag = true;
			String managerName = account.getManagerName();
			try {
				if(account.getAccessPoint() == ACCESS_POINT.HINEMOS_MANAGER){
					RestConnectManager.add( account.getUserId(), account.getPassword(), managerName, account.getUrl() );
					if (!RestConnectManager.get(managerName).isActive()) {
						ManagerInfoResponse managerInfo = connect(managerName);
						
						//オプションのチェック
						evaluationMsgs.put(managerName, "");
						List<String> options = managerInfo.getOptions();
						//Collections.sort(options.toArray());
						for (String option : options) {
							// 評価版かのチェック
							if (option.endsWith(ActivationKeyConstant.EVALUATION_SUFFIX) 
									|| option.endsWith(ActivationKeyConstant.EVALUATION_EXPIRED_SUFFIX)) {
								Matcher m = p.matcher(option);
								if (m.find()) {
									int year = Integer.parseInt(m.group(1));
									int month = Integer.parseInt(m.group(2));
									int day = LocalDate.of(year, month, 1).lengthOfMonth();
									
									String expireDate = LocalDate.of(year, month, day)
											.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
									
									// 評価版の場合
									if (option.startsWith(ActivationKeyConstant.TYPE_ENTERPRISE)) {
										String evaluationMsg = evaluationMsgs.get(managerName);
										if (evaluationMsg.isEmpty()) {
											evaluationMsg = "\n" + Messages.getString("message.accesscontrol.69", new String[]{});
										}
										evaluationMsg += "\n" + Messages.getString("message.accesscontrol.67", new String[]{expireDate});
										evaluationMsgs.put(managerName, evaluationMsg);
									} else if (option.startsWith(ActivationKeyConstant.TYPE_XCLOUD)) {
										String evaluationMsg = evaluationMsgs.get(managerName);
										if (evaluationMsg.isEmpty()) {
											evaluationMsg = "\n" + Messages.getString("message.accesscontrol.69", new String[]{});
										}
										evaluationMsg += "\n" + Messages.getString("message.accesscontrol.68", new String[]{expireDate});
										evaluationMsgs.put(managerName, evaluationMsg);
									}
								}
							}
						}
						String msg = Messages.getString("message.accesscontrol.5") + evaluationMsgs.get(managerName);
						// ログイン成功ダイアログを生成
						msgs.put( managerName, msg);

						FilterPropertyUpdater.getInstance().updateFilterProperties();

						m_log.info("Login Success : userId = " + account.getUserId() + ", url = " + account.getUrl());
					} else {
						m_log.info("Login already : userId = " + account.getUserId() + ", url = " + account.getUrl());
					}
				} else {
					// フィルタマネージャへの接続
					IRestConnectMsgFilter restConnectMsgFilter = RestConnectMsgFilterExtension.getInstance().getRestConnectMsgFilter();
					restConnectMsgFilter.connect(account.getUserId(), account.getPassword(), managerName, account.getUrl());

					String msg = Messages.getString("message.accesscontrol.5");
					// ログイン成功ダイアログを生成
					msgs.put( managerName, msg);

					m_log.info("FM Login Success : userId = " + account.getUserId() + ", url = " + account.getUrl());
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
			} catch (InvalidUserPass e){
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
				if (e.getMessage() != null &&  e.getMessage().contains(
						MessageConstant.MESSAGE_FAILED_TO_COMMUNICATE_EXTERNAL_AUTH_SERVER.toString())) {
					// 外部認証に失敗した場合はその旨をメッセージ出力
					msgs.put(managerName, HinemosMessage.replace(e.getMessage()));
				} else {
					// 予期せぬエラーダイアログを生成
					Status status = new Status(
							IStatus.ERROR,
							ClusterControlPlugin.getPluginId(),
							IStatus.OK,
							managerName + " : " + Messages.getString("message.accesscontrol.23"),
							e);
					statusList.add(status);
					msgs.put( managerName, Messages.getString("message.accesscontrol.6") );
				}
				isError = true;
				m_log.info("Login Fail : userId = " + account.getUserId() + ", url = " + account.getUrl());
			}
		}

		if (0 < msgs.size()) {
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
		store.setValue(LoginConstant.KEY_LOGIN_STATUS_NUM, 0);

		// Add history
		int stateNum = 0;
		for( RestConnectUnit endpointUnit : RestConnectManager.getAllManagerList() ){
			m_log.debug("saveLoginState() : " + stateNum + ", " + endpointUnit.getManagerName());
			store.setValue(LoginConstant.KEY_LOGIN_STATUS_UID + "_" + stateNum, endpointUnit.getUserId());
			store.setValue(LoginConstant.KEY_LOGIN_STATUS_URL + "_" + stateNum, endpointUnit.getUrlListStr());
			store.setValue(LoginConstant.KEY_LOGIN_STATUS_MANAGERNAME + "_" + stateNum, endpointUnit.getManagerName());
			store.setValue(LoginConstant.KEY_LOGIN_STATUS_ACCESSPOINT + "_" + stateNum, ACCESS_POINT.HINEMOS_MANAGER.name());
			stateNum++;
		}
		IRestConnectMsgFilter restConnectMsgFilter = RestConnectMsgFilterExtension.getInstance().getRestConnectMsgFilter();
		if(restConnectMsgFilter != null && restConnectMsgFilter.isSaved()){
			m_log.debug("saveLoginState() filter manager: " + stateNum + ", " + restConnectMsgFilter.getManagerName());
			store.setValue(LoginConstant.KEY_LOGIN_STATUS_UID + "_" + stateNum, restConnectMsgFilter.getUserId());
			store.setValue(LoginConstant.KEY_LOGIN_STATUS_URL + "_" + stateNum, restConnectMsgFilter.getUrlListStr());
			store.setValue(LoginConstant.KEY_LOGIN_STATUS_MANAGERNAME + "_" + stateNum, restConnectMsgFilter.getManagerName());
			store.setValue(LoginConstant.KEY_LOGIN_STATUS_ACCESSPOINT + "_" + stateNum, ACCESS_POINT.FILTER_MANAGER.name());
			stateNum++;
		}
		m_log.info("Save login state " + stateNum);
		store.setValue(LoginConstant.KEY_LOGIN_STATUS_NUM, stateNum);
		
		
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

		Set<String> options = new HashSet<String>();
		boolean loggedMsgFilter = false;
		// Proceed connecting if not Close button pressed
		if( returnCode == IDialogConstants.OK_ID ){
			if( addConnect(dialog.getLoginList()) ){
				ClientSession.doCheck();

				// マネージャの死活監視開始
				IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
				int interval = store.getInt(KEY_INTERVAL);
				m_log.trace("RestLoginManager.getInterval() interval = " + interval);
				ClientSession.startChecktask( interval );

				// Save login info
				saveLoginState();
			}
			// Hinemosマネージャのオプションを取得する
			if(RestLoginManager.isLogin()){
				options = RestConnectManager.getAllOptions();
			}

			// メッセージフィルタクライアントがインストールされログイン状態の場合、オプションを有効化する。
			IRestConnectMsgFilter msgFilterManager = RestConnectMsgFilterExtension.getInstance().getRestConnectMsgFilter();
			loggedMsgFilter = msgFilterManager != null && msgFilterManager.isActive();
			if(loggedMsgFilter){
				options.add(IRestConnectMsgFilter.OPTION_NAME);
			}
		}

		// ログイン状態を更新
		updateStatusBar();

		// Close all if no connection left
		if( returnCode != IDialogConstants.OK_ID && !RestLoginManager.isLogin() ){
			m_log.info("login() : cancel, " + returnCode);
			return;
		}

		if (returnCode == IDialogConstants.OK_ID && RestLoginManager.isLogin() || loggedMsgFilter){
			// ログインに成功した場合のみ、オプション用UI contributionsの有・無効化を行う
			OptionUtil.enableActivities(window, options);
		}
	}

	public static void login(IWorkbenchWindow window) {
		login(new HashMap<String, String>(), window);
	}

	public static void setup() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		// Timeout設定 (ms)
		int httpRequestTimeout = store.getInt(KEY_HTTP_REQUEST_TIMEOUT);
		RestConnectManager.setHttpRequestTimeout( httpRequestTimeout );
		m_log.info("request.timeout=" + httpRequestTimeout);

		// Proxy設定
		if (store.getBoolean(KEY_PROXY_ENABLE) == true) {
			String proxyHost = store.getString(KEY_PROXY_HOST);
			int proxyPort = store.getInt(KEY_PROXY_PORT);
			String proxyUser = store.getString(KEY_PROXY_USER);
			String proxyPass = store.getString(KEY_PROXY_PASSWORD);
			RestConnectManager.setProxy(proxyHost, proxyPort);
			RestConnectManager.setProxyAuchenticator(proxyUser, proxyPass);
		} else {
			RestConnectManager.setProxy(null, 0);
			RestConnectManager.setProxyAuchenticator(null, null);
		}

		RestConnectManager.setSetupExecuteTime(System.currentTimeMillis());
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
					m_log.trace("RestLoginManager.forceLogout() Open Dialog");

					// ダイアログ表示
					String[] args = { managerName, new Timestamp(System.currentTimeMillis()).toString() };
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.force.disconnect",args));

					UIManager.closeInfoDiag(managerName);
				}else{
					m_log.trace("RestLoginManager.disconnect() Dialog is occupied");
				}
			}}
		);
	}

}
