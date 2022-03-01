/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;
import org.openapitools.client.model.ConnectCheckResponse;

import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.rest.ApiException;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestLoginManager;

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
	private int interval = 0;

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
			clientSession.interval=interval;
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
			if( !RestLoginManager.isLogin() ){
				m_log.debug("ClientSession.doCheck() Not logged in yet. Skip.");
				return;
			}
			for( RestConnectUnit connectUnit : RestConnectManager.getAllManagerList() ){
				String managerName = connectUnit.getManagerName();
				m_log.debug("ClientSession.doCheck() Get last updated time from Manager " + managerName);
				Date lastUpdateManager = null;
				if (connectUnit.isActive()) {
					try{
						
						//connectCheckで接続チェック
						AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
						ConnectCheckResponse ret =wrapper.connectCheck();
						m_log.debug("ClientSession.doCheck() connectCheck = " + ret);

						//必要ならアクセストークンを更新
						ClientSession clientSession = getInstance();
						connectUnit.updateTokenIfNeeded(clientSession.interval);

						lastUpdateManager = new Date(RepositoryRestClientWrapper.getWrapper(managerName).getLastUpdate());

					}catch (Exception e){
						// マネージャ停止時はスタックとレースを出さない。
						if(e instanceof RestConnectFailed  || e instanceof ApiException){
							m_log.warn("ClientSession.doCheck() Manager is dead ! , " +
									e.getClass().getName() + ", "+ e.getMessage());
						} else {
							// 想定外の例外
							m_log.warn("ClientSession.doCheck() Manager is dead !! , " +
									e.getClass().getName() + ", "+ e.getMessage(), e);
						}
						// ダイアログを表示する
						RestLoginManager.forceLogout(managerName);
					}
				}

				Date lastUpdateClient = FacilityTreeCache.getCacheDate(managerName);
				if (lastUpdateManager != null && lastUpdateManager == lastUpdateClient) {
					//リポジトリの最新更新時刻が変化なしなら、次のマネージャの処理へ
					continue;
				}

				//リポジトリの最新更新時刻でキャッシュ更新
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
