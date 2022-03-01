/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.Operation;
import org.eclipse.rap.rwt.internal.protocol.Operation.NotifyOperation;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.Workbench;

import com.clustercontrol.accesscontrol.ui.AccessManagementPerspective;
import com.clustercontrol.approval.ui.ApprovalPerspective;
import com.clustercontrol.calendar.ui.CalendarPerspective;
import com.clustercontrol.collect.CollectPerspective;
import com.clustercontrol.hub.ui.HubPerspective;
import com.clustercontrol.infra.ui.InfraManagementPerspective;
import com.clustercontrol.jobmanagement.ui.JobHistoryPerspective;
import com.clustercontrol.jobmanagement.ui.JobSettingPerspective;
import com.clustercontrol.jobmap.JobMapEditorPerspective;
import com.clustercontrol.jobmap.JobMapHistoryPerspective;
import com.clustercontrol.maintenance.ui.MaintenancePerspective;
import com.clustercontrol.monitor.ui.MonitorHistoryPerspective;
import com.clustercontrol.monitor.ui.MonitorSettingPerspective;
import com.clustercontrol.msgfilter.extensions.IRestConnectMsgFilter;
import com.clustercontrol.msgfilter.extensions.RestConnectMsgFilterExtension;
import com.clustercontrol.msgfilter.util.MsgFilterClientUtil;
import com.clustercontrol.nodemap.NodeMapPerspective;
import com.clustercontrol.reporting.ui.ReportingPerspective;
import com.clustercontrol.repository.RepositoryPerspective;
import com.clustercontrol.rpa.ui.RpaScenarioOperationResultPerspective;
import com.clustercontrol.rpa.ui.RpaSettingPerspective;
import com.clustercontrol.sdml.util.SdmlClientUtil;
import com.clustercontrol.startup.ui.StartUpPerspective;
import com.clustercontrol.ui.util.OptionUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestLoginManager;
import com.clustercontrol.utility.settings.ui.SettingToolsPerspective;
import com.clustercontrol.xcloud.ui.BillingPerspective;
import com.clustercontrol.xcloud.ui.ComputePerspective;
import com.clustercontrol.xcloud.ui.NetworkPerspective;
import com.clustercontrol.xcloud.ui.ServicePerspective;
import com.clustercontrol.xcloud.ui.StoragePerspective;

/**
 * 
 * WorkbenchWindowAdvisorクラスを継承するクラス<BR>
 * RCPのWorkbenchWindowの設定などを行います。
 * 
 * @version 5.0.0
 * @since 2.0.0
 */
public class ClusterControlWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	// URL取得値
	private String perspective;
	private Map<String, String> paramaters = new HashMap<>();
	

	public ClusterControlWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ClusterControlActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

		// Initialize window size according to browser size
		Rectangle bounds = Display.getCurrent().getBounds();
		Rectangle rect = ClusterControlPlugin.WINDOW_INIT_SIZE;
		if(bounds.width < rect.width ){
			rect.width = bounds.width;
		}
		if(bounds.height < rect.height ){
			rect.height = bounds.height;
		}
		configurer.setInitialSize(new Point(rect.width, rect.height));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowPerspectiveBar(true);

		if( ClusterControlPlugin.isRAP() ){
			// Remove the title bar and buttons
			configurer.setShellStyle(SWT.NONE);
			
			// ページ遷移前に確認ダイアログを出す
			if (ClusterControlPlugin.isExitConfirm()) {
				ExitConfirmation exitConfirmation = RWT.getClient().getService(ExitConfirmation.class);
				exitConfirmation.setMessage(Messages.getString("leave.webclient.confirm"));
			}
			// URLにパースペクティブの情報が含まれているか確認する
			getUrlInfo();
			
			// Basic認証が行われた場合はそのデータを取得する
			String rawAuth = RWT.getRequest().getHeader("Authorization"); // Basic aGluZW1vczpoaW5lbW9z
			if (rawAuth != null && !rawAuth.equals("") ) {
				String authBase64 = rawAuth.split("\\s", 2)[1]; // aGluZW1vczpoaW5lbW9z
				String authDecoded =new String(Base64.getDecoder().decode(authBase64));  // hinemos:hinemos
				String[] auth = authDecoded.split(":", 2);
				paramaters.put("user", auth[0]);
				paramaters.put("password", auth[1]);
			}
			
			// Add the following to prevent overflow auto-hiding on perspective bar
			IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
			prefStore.setDefault( IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, IWorkbenchPreferenceConstants.TOP_LEFT );
		}
	}

	@Override
	public void postWindowCreate(){
		super.postWindowCreate();

		// 起動時にログインダイアログを表示する。
		// パースペクティブが何もない時に必ずこのルートを通る。
		RestLoginManager.login(paramaters, getWindowConfigurer().getWindow());

		// URLに情報が入力されている場合。
		if (perspective != null) {

			// パースペクティブIDを取得します。
			String perspectiveId = getPerspectiveID(perspective);
			// パースペクティブIDが正しくない場合は処理しない。
			if (perspectiveId.length() != 0) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				// URLから該当のパースペクティブを開く。
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				IWorkbenchPage activePage = window.getActivePage();
				IPerspectiveDescriptor desc = window.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
				
				if (desc == null) {
					// corresponding perspective is not found.
					return;
				}
				
				try {
					if (activePage != null) {
						activePage.setPerspective(desc);
						
						IPerspectiveDescriptor[] openPerspectives = activePage.getOpenPerspectives();

						// 他のパースペクティブをすべて閉じる。
						for(int i = 0; i < openPerspectives.length; i++) {
							if ((openPerspectives[i].getId()).equals(perspectiveId)) continue;
							activePage.closePerspective(openPerspectives[i], false, false);
						}
					} else {
						IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
						window.openPage(perspectiveId, input);
					}
					
				} catch (WorkbenchException e) {
					// 何もしない
				}
			}
		}
	}

	@Override
	public void postWindowOpen(){
		super.postWindowOpen();

		// Web Client starts with maximized window
		if( ClusterControlPlugin.isRAP() ){
			IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
			configurer.getWindow().getShell().setMaximized(true);
		}
	}

	
	/**
	 * URLにパースペクティブ情報が入力されているか検証を行う。 
	 */
	private void getUrlInfo() {
		ClientMessage mes = ProtocolUtil.getClientMessage();
		if (mes != null) {
			List<Operation> list = mes.getAllOperationsFor("rwt.client.BrowserNavigation");
			for (Operation operation : list) {
				if( operation instanceof NotifyOperation ) {
					NotifyOperation notifyOperation = ( NotifyOperation )operation;
					String state = notifyOperation.getProperties().get("state").asString();
					if (state != null && !state.equals("")) {
						parseUrl(state);
					}
				}
			}
		}
	}
	
	private void parseUrl(String url) {
		if (url.indexOf("?") < 0) {
			// パラメータが存在しない
			perspective = url;
			return;
		}
		// http://webclientのIPアドレス/#パースペクティブ名?LoginUrl=xxx;yyy;zzzの形式で来ることを前提とする。
		String urls[] = url.split("\\?", 2);
		perspective = urls[0];
		String paramString = urls[1];
		
		String[] paramArray = paramString.split("&");
		for (String param : paramArray) {
			if (param.indexOf("=") > 0) {
				String[] p = param.split("=", 2);
				if (p[0].equals(RestLoginManager.KEY_BASIC_AUTH) ||
						p[0].equals(RestLoginManager.KEY_URL_LOGIN_URL) || 
						p[0].equals(RestLoginManager.KEY_URL_UID) ||
						p[0].equals(RestLoginManager.KEY_URL_MANAGER_NAME)) {
					paramaters.put(p[0], p[1]);
				}
			}
		}
	}

	/**
	 * 入力されたURL情報からパースペクティブのIDを取得する。
	 * @param urlInfo
	 * @return perspectiveId
	 */
	private String getPerspectiveID(String urlInfo) {

		String id = "";

		if (urlInfo.equals(RestLoginManager.URL_HINEMOS)) {
			id = ClusterControlPerspective.ID;
		} else if (urlInfo.equals(RestLoginManager.URL_ACCOUNT)) {
			id = AccessManagementPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_CALENDAR)) {
			id = CalendarPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_JOB_HISTORY)) {
			id = JobHistoryPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_JOB_SETTING)) {
			id = JobSettingPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_STARTUP)) {
			id = StartUpPerspective.ID;
		} else if (urlInfo.equals(RestLoginManager.URL_MAINTENANCE)) {
			id = MaintenancePerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_REPOSITORY)) {
			id = RepositoryPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_COLLECT)) {
			id = CollectPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_APPROVAL)) {
			id = ApprovalPerspective.ID;
		} else if (urlInfo.equals(RestLoginManager.URL_INFRA)) {
			id = InfraManagementPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_MONITOR_HISTORY)) {
			id = MonitorHistoryPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_MONITOR_SETTING)) {
			id = MonitorSettingPerspective.class.getName();
		} else if (urlInfo.equals(RestLoginManager.URL_HUB)) {
			id = HubPerspective.class.getName();
		} else if (SdmlClientUtil.isSdmlUrl(urlInfo)) {
			id = SdmlClientUtil.getPerspectiveId(urlInfo);
		} else {
			Set<String> options = RestConnectManager.getAllOptions();
			if (urlInfo.equals(RestLoginManager.URL_XCLOUD_BILLING) && options.contains(OptionUtil.TYPE_XCLOUD)) {
				id = BillingPerspective.class.getName();
			} else if (urlInfo.equals(RestLoginManager.URL_XCLOUD_COMPUTE) && options.contains(OptionUtil.TYPE_XCLOUD)) {
				id = ComputePerspective.class.getName();
			} else if (urlInfo.equals(RestLoginManager.URL_XCLOUD_NETWORK) && options.contains(OptionUtil.TYPE_XCLOUD)) {
				id = NetworkPerspective.class.getName();
			} else if (urlInfo.equals(RestLoginManager.URL_XCLOUD_SERVICE) && options.contains(OptionUtil.TYPE_XCLOUD)) {
				id = ServicePerspective.class.getName();
			} else if (urlInfo.equals(RestLoginManager.URL_XCLOUD_STORAGE) && options.contains(OptionUtil.TYPE_XCLOUD)) {
				id = StoragePerspective.class.getName();
			} else if (urlInfo.equals(RestLoginManager.URL_JOBMAP_EDITOR) && options.contains(OptionUtil.TYPE_ENTERPRISE)) {
				id = JobMapEditorPerspective.ID;
			} else if (urlInfo.equals(RestLoginManager.URL_JOBMAP_HISTORY) && options.contains(OptionUtil.TYPE_ENTERPRISE)) {
				id = JobMapHistoryPerspective.ID;
			} else if (urlInfo.equals(RestLoginManager.URL_NODEMAP) && options.contains(OptionUtil.TYPE_ENTERPRISE)) {
				id = NodeMapPerspective.ID;
			} else if (urlInfo.equals(RestLoginManager.URL_SETTING_TOOLS) && options.contains(OptionUtil.TYPE_ENTERPRISE)) {
				id = SettingToolsPerspective.ID;
			} else if (urlInfo.equals(RestLoginManager.URL_REPORTING) && options.contains(OptionUtil.TYPE_ENTERPRISE)) {
				id = ReportingPerspective.ID;
			} else if (urlInfo.equals(RestLoginManager.URL_RPA_SETTING) && options.contains(OptionUtil.TYPE_ENTERPRISE)) {
				id = RpaSettingPerspective.ID;
			} else if (urlInfo.equals(RestLoginManager.URL_RPA_SCENARIO_OPERATION_RESULT) && options.contains(OptionUtil.TYPE_ENTERPRISE)) {
				id = RpaScenarioOperationResultPerspective.ID;
			}

			IRestConnectMsgFilter msgFilterManager = RestConnectMsgFilterExtension.getInstance().getRestConnectMsgFilter();
			boolean loggedMsgFilter = msgFilterManager != null && msgFilterManager.isActive();
			if(urlInfo.equals(RestLoginManager.URL_MSG_FILTER) && loggedMsgFilter){
				id = MsgFilterClientUtil.getPerspectiveId();
			}
		}
		return id;
	}
}
