/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.MultiManagerRunUtil;
import com.clustercontrol.ws.jobmanagement.JobInfo;

/**
 * IApplication実装クラス<BR>
 *
 * RCP起動時のエントリポイントとなるクラス
 * クライアントの表示に利用します。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class ClusterControl implements IApplication {

	private static Log m_log = LogFactory.getLog( ClusterControl.class );
	private static int m_max_user;
	private static ConcurrentHashMap<String, String> m_access_user_map = new ConcurrentHashMap<String, String>();
	
	static {
		int max_user = 8;
		try {
			max_user = Integer.parseInt(System.getProperty("maximum.access.users", "8"));
		} catch (NumberFormatException e) {
			m_log.info("System environment value \"maximum.access.users\" is not correct.");
		} finally {
			m_max_user = max_user;
			m_log.info("max_user = " + m_max_user);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		m_log.debug("run()");

		WorkbenchAdvisor advisor = new ClusterControlWorkbenchAdvisor();
		Display display = PlatformUI.createDisplay();
		
		// Activate mnemonic key binding for RAP
		if( ClusterControlPlugin.isRAP() ){
			display.setData( RWT.MNEMONIC_ACTIVATOR, "CTRL+ALT" );
		}

		String uiSessionId = null;
		String ipAddress = null;
		try {
			// アクセスしてきたユーザのUIThreadのIDとIPアドレスの組を保存する
			uiSessionId = RWT.getUISession().getId();
			ipAddress = RWT.getRequest().getRemoteAddr();
			m_access_user_map.put(uiSessionId, ipAddress);
			m_log.info("new user(IPAddress="+ipAddress+") access, user count = " + m_access_user_map.size());
			if (m_log.isDebugEnabled()) {
				// デバッグモードの場合は、アクセスしているユーザのリストを出力する
				m_log.debug("Access users are in the following list.");
				for (Map.Entry<String, String> id : m_access_user_map.entrySet()) {
					m_log.debug("UISessionId=" + id.getKey() + ", IPAddress=" + id.getValue());
				}
			}
			
			// アクセスユーザ数が制限を超えた場合は終了する
			if (m_access_user_map.size() > m_max_user) {
				String msg = Messages.getString("message.accesscontrol.63", new String[] {String.valueOf(m_max_user)});
				m_log.warn(msg);
				m_log.warn("Access users are in the following list.");
				for (Entry<String, String> entry : m_access_user_map.entrySet()) {
					m_log.warn("UISessionId=" + entry.getKey() + ", IPAddress=" + entry.getValue());
				}
				MessageDialog.openError(null, Messages.getString("message"), msg);
				return IApplication.EXIT_OK;
			}
			

			int ret = PlatformUI.createAndRunWorkbench(display, advisor);
			if (ret == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			} else {
				return IApplication.EXIT_OK;
			}
		} finally {
			try{
				// Stop the the timer task if started
				ClientSession.stopChecktask();
				
				// 各マネージャから情報を取得するためのスレッドを停止する
				MultiManagerRunUtil.getExecutorService().shutdown();
				
				// ロックしているジョブユニットがある場合
				for (String managerName : EndpointManager.getActiveManagerNameList()) {
					JobEditState state = JobEditStateUtil.getJobEditState(managerName);
					for (JobInfo jobunit : state.getLockedJobunitList()) {
						try {
							JobEndpointWrapper.getWrapper(managerName).releaseEditLock(state.getEditSession(jobunit));
							m_log.info("release job lock : jobunitId=" + jobunit.getJobunitId());
						} catch (Exception e) {
							m_log.warn("dispose() : " + e.getMessage());
						}
					}
				}
				
				display.dispose();
				
			}catch(Exception e){
				m_log.error( "dispose() : ", e );
			} finally {
				if (uiSessionId != null) {
					boolean isRemoveSuccess = m_access_user_map.remove(uiSessionId) != null;
					m_log.info("user(IPAddress=" +ipAddress+") exit, remove = " + isRemoveSuccess + ", user count = " + m_access_user_map.size());
				} else {
					m_log.warn("can't remove user.(uid is null), user count = " + m_access_user_map.size());
				}
			}
			display.dispose();
		}
	}

	@Override
	public void stop() {
	}
}
