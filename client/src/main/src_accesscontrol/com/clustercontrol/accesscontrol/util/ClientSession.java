/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.Date;

import javax.xml.ws.WebServiceException;

import com.sun.xml.internal.ws.client.ClientTransportException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.LoginManager;

/**
 * Client Session<BR>
 * 
 * @since 5.0.0
 */
public class ClientSession {
	// ログ
	private static Log m_log = LogFactory.getLog( ClientSession.class );

	/** ファシリティID更新用タイマー * */
	private SessionTimer m_timer = null;

	/** Whether error dialog is available or not */
	private boolean dialogFlag = true;

	/**
	 * コンストラクタ
	 */
	private ClientSession() {}

	/**
	 * Session Singleton
	 */
	private static ClientSession getInstance(){
		return SingletonUtil.getSessionInstance( ClientSession.class );
	}

	/**
	 * チェックタスクを開始する
	 * 
	 * @param inverval 間隔(分)
	 */
	public static void startChecktask(int interval) {
		m_log.trace("ClientSession.startChecktask() start : interval = " + interval);

		ClientSession clientSession = getInstance();
		// タイマーが存在しない場合のみ実行
		if( clientSession.m_timer == null && interval > 0 ){
			m_log.trace("ClientSession.startChecktask() setup task");

			clientSession.m_timer = new SessionTimer();
			clientSession.m_timer.start( interval );
		}
	}

	/**
	 * チェックタスクをリスタート
	 * 
	 * @param inverval 間隔(分)
	 */
	public static void restartChecktask(int interval) {
		m_log.trace("ClientSession.restartChecktask() start : interval = " + interval);

		stopChecktask();
		startChecktask(interval);
	}

	/**
	 * チェックタスクを停止する
	 */
	public static void stopChecktask() {
		m_log.trace("ClientSession.stopChecktask() start");

		ClientSession clientSession = getInstance();
		if( clientSession.m_timer != null ){
			// スケジュール削除
			clientSession.m_timer.cancel();

			// タイマー削除
			clientSession.m_timer = null;
		}
	}

	/**
	 * Check
	 */
	public static void doCheck() {
		m_log.trace("ClientSession.doCheck() start");

		// ログインチェック
		try {
			if( !LoginManager.isLogin() ){
				m_log.trace("ClientSession.doCheck() Not logged in yet. Skip.");
				return;
			}
	
			// リポジトリの最新更新時間を取得
			for( EndpointUnit endpointUnit : EndpointManager.getAllManagerList() ){
				String managerName = endpointUnit.getManagerName();
				m_log.trace("ClientSession.doCheck() Get last updated time from Manager " + managerName);
				Date lastUpdateManager = null;
				if (endpointUnit.isActive()) {
					try{
						RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
						lastUpdateManager = new Date(wrapper.getLastUpdate());
						m_log.trace("ClientSession.doCheck() lastUpdate(Manager) = " + lastUpdateManager);
					}catch (Exception e){
						// マネージャ停止時はスタックとレースを出さない。
						if(e instanceof ClientTransportException || e instanceof WebServiceException){
							m_log.warn("ClientSession.doCheck() Manager is dead ! , " +
									e.getClass().getName() + ", "+ e.getMessage());
						} else {
							// 想定外の例外
							m_log.warn("ClientSession.doCheck() Manager is dead !! , " +
									e.getClass().getName() + ", "+ e.getMessage(), e);
						}
						// ダイアログを表示する
						LoginManager.forceLogout(managerName);
					}
				}
	
				Date lastUpdateClient = FacilityTreeCache.getCacheDate(managerName);
				
				// リポジトリの最新更新時刻が異なる場合にキャッシュ更新
				if (lastUpdateManager == lastUpdateClient)
					continue;
				
				boolean update = false;
				if (lastUpdateClient == null) {
					update = true;
				} else {
					update = !lastUpdateClient.equals(lastUpdateManager);
				}
				
				if (update) {
					m_log.debug("ClientSession.doCheck() lastUpdate(Manager)=" + lastUpdateManager +
							", lastUpdate(Client)=" + lastUpdateClient +
							", " + managerName);
					// リポジトリツリーキャッシュを更新して画面を再描画する
					FacilityTreeCache.refresh( managerName, lastUpdateManager );
				}
			}
		} catch (RuntimeException e){
			m_log.warn("doCheck : " + e.getClass().getName() + ", message=" + e.getMessage(), e);
		}
	}

	// TODO 将来的に、1つのエラービュー/StatusLineにまとめる
	/**
	 * 同時に表示するオートアップデートのエラーダイアログを1個に制限
	 * @param flag
	 */
	private void setDialogFlag (boolean flag) {
		this.dialogFlag = flag;
	}

	public static void occupyDialog(){
		getInstance().setDialogFlag(false);
	}

	public static void freeDialog(){
		getInstance().setDialogFlag(true);
	}

	public static boolean isDialogFree(){
		return getInstance().dialogFlag;
	}
}
