/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.clustercontrol.bean.DefaultLayoutSettingManager;
import com.clustercontrol.jobmanagement.preference.JobManagementPreferencePage;
import com.clustercontrol.monitor.plugin.IMonitorPlugin;
import com.clustercontrol.monitor.plugin.LoadMonitorPlugin;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.utility.settings.ui.dialog.ClientUtilityDialogService;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.xcloud.model.cloud.HinemosManager;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.CollectionComparator;

/**
 * 
 * Hinemosクライアントのメインプラグインクラス<BR>
 * 
 * ここでEclipse(RCP)との接続点です。
 */
public class ClusterControlPlugin extends AbstractUIPlugin {

	private static Log m_log = LogFactory.getLog( ClusterControlPlugin.class );

	// ----- static フィールド ----- //

	/** コンソールアイコンの取得キー */
	public static final String IMG_CONSOLE = "console";

	/** スコープアイコンの取得キー */
	public static final String IMG_SCOPE = "scope";

	/** 参照不可のスコープアイコンの取得キー */
	public static final String IMG_SCOPE_INVALID = "scope_invalid";

	/** ノードアイコンの取得キー */
	public static final String IMG_NODE = "node";

	/** 使用不可のノードアイコンの取得キー */
	public static final String IMG_NODE_INVALID = "node_invalid";

	/** ジョブユニットアイコンの取得キー */
	public static final String IMG_JOBUNIT = "jobunit";

	/** 参照できないジョブユニットアイコンの取得キー */
	public static final String IMG_JOBUNIT_UNREFERABLE = "jobunit_unreferable";

	/** ジョブネットアイコンの取得キー */
	public static final String IMG_JOBNET = "jobnet";

	/** ジョブアイコンの取得キー */
	public static final String IMG_JOB = "job";

	/** ファイル転送ジョブアイコンの取得キー */
	public static final String IMG_FILEJOB = "filejob";

	/** 参照ジョブアイコンの取得キー */
	public static final String IMG_REFERJOB = "referjob";

	/** 参照ジョブネットアイコンの取得キー */
	public static final String IMG_REFERJOBNET = "referjobnet";

	/** 承認ジョブアイコンの取得キー */
	public static final String IMG_APPROVALJOB = "approvaljob";

	/** 監視ジョブアイコンの取得キー */
	public static final String IMG_MONITORJOB = "monitorjob";

	/** ファイルチェックジョブアイコンの取得キー */
	public static final String IMG_FILECHECKJOB = "filecheckjob";

	/** ジョブ連携送信ジョブアイコンの取得キー */
	public static final String IMG_JOBLINKSENDJOB = "joblinksendjob";

	/** ジョブ連携待機ジョブアイコンの取得キー */
	public static final String IMG_JOBLINKRCVJOB = "joblinkrcvjob";

	/** リソース制御ジョブアイコンの取得キー */
	public static final String IMG_RESOURCEJOB = "resourcejob";

	/** RPAシナリオジョブアイコンの取得キー */
	public static final String IMG_RPAJOB = "rpajob";

	/** チェック有りアイコンの取得キー */
	public static final String IMG_CHECKED = "checked";

	/** チェックなしアイコンの取得キー */
	public static final String IMG_UNCHECKED = "unchecked";

	/** ラジオONアイコンの取得キー */
	public static final String IMG_RADIO_ON = "radio_on";

	/** ラジオOFFアイコンの取得キー */
	public static final String IMG_RADIO_OFF = "radio_off";
	
	/** 実行状態(白)アイコンの取得キー */
	public static final String IMG_STATUS_WHITE = "status_white";

	/** 実行状態(黄)アイコンの取得キー */
	public static final String IMG_STATUS_YELLOW = "status_yellow";

	/** 実行状態(青)アイコンの取得キー */
	public static final String IMG_STATUS_BLUE = "status_blue";

	/** 実行状態(赤)アイコンの取得キー */
	public static final String IMG_STATUS_RED = "status_red";

	/** 実行状態(緑)アイコンの取得キー */
	public static final String IMG_STATUS_GREEN = "status_green";

	/** 終了状態(正常)アイコンの取得キー */
	public static final String IMG_END_STATUS_NORMAL = "normal";

	/** 終了状態(警告)アイコンの取得キー */
	public static final String IMG_END_STATUS_WARNING = "warning";

	/** 終了状態(異常)アイコンの取得キー */
	public static final String IMG_END_STATUS_ABNORMAL = "abnormal";

	/** 予定(過去)アイコンの取得キー */
	public static final String IMG_SCHEDULE_PAST = "schedule_past";

	/** 予定(現在)アイコンの取得キー */
	public static final String IMG_SCHEDULE_NOW = "schedule_now";

	/** 予定(未来)アイコンの取得キー */
	public static final String IMG_SCHEDULE_FUTURE = "schedule_future";

	/** 編集モードアイコンの取得キー */
	public static final String IMG_JOB_EDIT_MODE = "job_edit_mode";

	/** ロール設定のルートアイコンの取得キー */
	public static final String IMG_ROLESETTING_ROOT = "rootSettingsRoot";

	/** ロール設定のロールアイコンの取得キー */
	public static final String IMG_ROLESETTING_ROLE = "rootSettingsRole";

	/** ロール設定のユーザアイコンの取得キー */
	public static final String IMG_ROLESETTING_USER = "rootSettingsUser";

	/** 検索アイコンの取得キー */
	public static final String IMG_FIND = "find";
	
	/*** JobMap用 ***/
	/** 予定(未来)アイコンの取得キー */
	public static final String IMG_WAIT = "wait";
	public static final String IMG_WAIT_DOUBLE = "wait_double";
	public static final String IMG_WAIT_CROSS_JOB = "wait_cross_job";
	public static final String IMG_COLLAPSE = "collapse";
	public static final String IMG_EXPAND = "expand";
	/** 参照アイコンの取得キー */
	public static final String IMG_REFER = "refer";
	/** ジョブマップ:キューアイコンの取得キー */
	public static final String IMG_QUEUE = "queue";
	/** 待ち条件群アイコンの取得キー */
	public static final String IMG_WAIT_GROUP = "wait_group";
	/** Initial window size and position */
	public static final Rectangle WINDOW_INIT_SIZE = new Rectangle( -1, -1, 1024, 768 );

	/** スコープの区切り文字（セパレータ） */
	private final static String DEFAULT_SEPARATOR = ">";

	/** デフォルトレイアウトファイル　ファイル名 */
	private final static String DEFAULT_LAYOUTFILE_NAME = "default_layout.xml";


	/** The shared instance */
	private static ClusterControlPlugin plugin;

	/** RAPかどうかを判別する */
	private static Boolean is_rap = null;

	// ----- instance メソッド ----- //

	//Resource bundle.
	private ResourceBundle resourceBundle;

	private String separator;

	private ILogListener listener;

	private DefaultLayoutSettingManager defaultLayoutManager;
	// ----- コンストラクタ ----- //

	/**
	 * The constructor.
	 */
	public ClusterControlPlugin() {
		super();
		m_log.info("Starting Hinemos Client...");

		// log4jを使うための登録処理
		listener = new Listener();
		Platform.addLogListener(listener);
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		// 定期的にリロードする処理を開始する
		String _configFileDir = System.getProperty("hinemos.web.conf.dir");
		
		String configFileLayoutDir = null;
		
		if (null != _configFileDir && !"".equals(_configFileDir)) {
			URI _configFilePath = new File(_configFileDir, "log4j2.properties").toURI();
			m_log.info("configFilePath = " + _configFilePath.getPath());
			context.setConfigLocation(_configFilePath);
			configFileLayoutDir = _configFileDir;
		} else {
			//isRapはこのタイミングでは使用できないため、hinemos.web.conf.dirがない場合、リッチクライアントと判断する
			configFileLayoutDir = System.getProperty("hinemos.rich.conf.dir");
		}
		
		defaultLayoutManager = new DefaultLayoutSettingManager(new File(configFileLayoutDir, DEFAULT_LAYOUTFILE_NAME));
		
		setDefault(this);
		try {
			resourceBundle = ResourceBundle
					.getBundle("com.clustercontrol.ClusterControlPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}

		// Systemプロパティ
		m_log.info("java.vm.version = " + System.getProperty("java.vm.version"));
		m_log.info("java.vm.vendor = " + System.getProperty("java.vm.vendor"));
		m_log.info("java.home = " + System.getProperty("java.home"));
		m_log.info("os.name = " + System.getProperty("os.name"));
		m_log.info("os.arch = " + System.getProperty("os.arch"));
		m_log.info("os.version = " + System.getProperty("os.version"));
		m_log.info("user.name = " + System.getProperty("user.name"));
		m_log.info("user.dir = " + System.getProperty("user.dir"));
		m_log.info("user.country = " + System.getProperty("user.country"));
		m_log.info("user.language = " + System.getProperty("user.language"));
		m_log.info("file.encoding = " + System.getProperty("file.encoding"));

		// 起動時刻
		long startDate = System.currentTimeMillis();
		m_log.info("start date = " + new Date(startDate) + "(" + startDate + ")");


		// 追加監視の情報をログへ出力　*ここでクラスを初期化しています。
		for(IMonitorPlugin pluginMonitor: LoadMonitorPlugin.getExtensionMonitorList()){
			m_log.info("Extension monitorPlugin " + pluginMonitor.getMonitorPluginId() + " plugged in.");
		}

		// UtilityのDialogServiceを注入
		UtilityDialogInjector.setService(new ClientUtilityDialogService());
		
		// 確認ダイアログを表示するかをユーザが変更できない設定の場合、
		// Preferenceの値を設定ファイルで指定された値に変更する
		// イベント履歴の確認状態変更
		String eventConfirmDialogEditable = System.getProperty("event.confirm.dialog.editable");
		if (eventConfirmDialogEditable != null && eventConfirmDialogEditable.equals("false")) {
			getPreferenceStore().setValue(MonitorPreferencePage.P_EVENT_CONFIRM_DIALOG_FLG,
					Boolean.valueOf(System.getProperty("event.confirm.dialog")));
		}
		m_log.info("event.confirm.dialog = " + System.getProperty("event.confirm.dialog"));
		m_log.info("event.confirm.dialog.editable = " + System.getProperty("event.confirm.dialog.editable"));
		// ジョブの開始/停止
		String jobOperationConfirmDialogEditable = System.getProperty("job.operation.confirm.dialog.editable");
		if (jobOperationConfirmDialogEditable != null && jobOperationConfirmDialogEditable.equals("false")) {
			getPreferenceStore().setValue(JobManagementPreferencePage.P_HISTORY_CONFIRM_DIALOG_FLG,
					Boolean.valueOf(System.getProperty("job.operation.confirm.dialog")));
		}
		m_log.info("job.operation.confirm.dialog = " + System.getProperty("job.operation.confirm.dialog"));
		m_log.info("job.operation.confirm.dialog.editable = " + System.getProperty("job.operation.confirm.dialog.editable"));
	}

	private static class Listener implements ILogListener {
		@Override
		public void logging(final IStatus status, final String plugin) {
			if (status.getSeverity() == IStatus.INFO) {
				if (status.getException() == null) {
					m_log.info(status.getMessage());
				} else {
					m_log.info(status.getMessage(), status.getException());
				}
			} else if (status.getSeverity() == IStatus.WARNING) {
				if (status.getException() == null) {
					m_log.warn(status.getMessage());
				} else {
					m_log.warn(status.getMessage(), status.getException());
				}
			} else {
				if (status.getException() == null) {
					m_log.error(status.getMessage());
				} else {
					m_log.error(status.getMessage(), status.getException());
				}
			}
		}
	}

	// ----- instance メソッド ----- //

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			super.stop(context);
		} catch (AssertionFailedException e) {
			// Webクライアント停止時にAssertionFailedExceptionが発生することがあるので、
			// ログ出力をしておく。
			if (context == null) {
				m_log.warn("context=" + null);
			} else {
				Bundle[] bb = context.getBundles();
				for (Bundle b : bb) {
					m_log.warn("context=" + b.getSymbolicName());
				}
			}
		}
	}

	public static void setDefault(ClusterControlPlugin p) {
		plugin = p;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static ClusterControlPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ClusterControlPlugin.getDefault()
				.getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Returns DefaultLayoutSettingManager
	 */
	public DefaultLayoutSettingManager getDefaultLayoutSettingManager() {
		return defaultLayoutManager;
	}
	
	/**
	 * プラグインクラスが保持するImageRegistryにイメージを登録します。
	 * 
	 * @param registry
	 *            ImageRegistryオブジェクト
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		this.registerImage(registry, IMG_CONSOLE, "console_view.gif");
		this.registerImage(registry, IMG_SCOPE, "scope.gif");
		this.registerImage(registry, IMG_SCOPE_INVALID, "scope_invalid.gif");
		this.registerImage(registry, IMG_NODE, "node.gif");
		this.registerImage(registry, IMG_NODE_INVALID, "node_invalid.gif");
		this.registerImage(registry, IMG_JOBUNIT, "job_unit.gif");
		this.registerImage(registry, IMG_JOBUNIT_UNREFERABLE, "job_unit_unreferable.gif");
		this.registerImage(registry, IMG_JOBNET, "job_net.gif");
		this.registerImage(registry, IMG_JOB, "job.gif");
		this.registerImage(registry, IMG_FILEJOB, "file_obj.gif");
		this.registerImage(registry, IMG_REFERJOB, "refer_job.gif");
		this.registerImage(registry, IMG_REFERJOBNET, "refer_job_net.gif");
		this.registerImage(registry, IMG_APPROVALJOB, "approval_job.gif");
		this.registerImage(registry, IMG_MONITORJOB, "monitor_job.gif");
		this.registerImage(registry, IMG_FILECHECKJOB, "filecheck_job.png");
		this.registerImage(registry, IMG_JOBLINKSENDJOB, "joblinksend_job.png");
		this.registerImage(registry, IMG_JOBLINKRCVJOB, "joblinkrcv_job.png");
		this.registerImage(registry, IMG_RESOURCEJOB, "resource_job.png");
		this.registerImage(registry, IMG_RPAJOB, "rpa_job.png");
		this.registerImage(registry, IMG_CHECKED, "checked.gif");
		this.registerImage(registry, IMG_UNCHECKED, "unchecked.gif");
		this.registerImage(registry, IMG_RADIO_ON, "radio_on.gif");
		this.registerImage(registry, IMG_RADIO_OFF, "radio_off.gif");
		this.registerImage(registry, IMG_STATUS_BLUE, "status_blue.gif");
		this.registerImage(registry, IMG_STATUS_GREEN, "status_green.gif");
		this.registerImage(registry, IMG_STATUS_RED, "status_red.gif");
		this.registerImage(registry, IMG_STATUS_WHITE, "status_white.gif");
		this.registerImage(registry, IMG_STATUS_YELLOW, "status_yellow.gif");
		this.registerImage(registry, IMG_END_STATUS_NORMAL, "normal.gif");
		this.registerImage(registry, IMG_END_STATUS_WARNING, "warning.gif");
		this.registerImage(registry, IMG_END_STATUS_ABNORMAL, "abnormal.gif");
		this.registerImage(registry, IMG_SCHEDULE_PAST, "schedule_g.gif");
		this.registerImage(registry, IMG_SCHEDULE_NOW, "schedule_b.gif");
		this.registerImage(registry, IMG_SCHEDULE_FUTURE, "schedule_w.gif");
		this.registerImage(registry, IMG_JOB_EDIT_MODE, "job_edit_mode.gif");
		this.registerImage(registry, IMG_FIND, "find.png");

		// RoleSettingTree
		this.registerImage(registry, IMG_ROLESETTING_ROOT, "node.gif");
		this.registerImage(registry, IMG_ROLESETTING_ROLE, "role.gif");
		this.registerImage(registry, IMG_ROLESETTING_USER, "user.gif");

		// For JobMap
		this.registerImage(registry, IMG_WAIT, "waiting.gif");
		this.registerImage(registry, IMG_WAIT_DOUBLE, "breakpoint_view.gif");
		this.registerImage(registry, IMG_WAIT_CROSS_JOB, "smartmode_co.gif");
		this.registerImage(registry, IMG_WAIT_GROUP, "waitgroup.png");
		this.registerImage(registry, IMG_COLLAPSE, "collapse.gif");
		this.registerImage(registry, IMG_EXPAND, "expand.gif");
		this.registerImage(registry, IMG_REFER, "refer_mark.gif");
		this.registerImage(registry, IMG_QUEUE, "queue.png");

		// For xCloud
		registerImage(registry, "user", "user.png");
		registerImage(registry, "account", "account.png");
		registerImage(registry, "aws-box", "aws-box.png");
		registerImage(registry, "checked", "enable.gif");
		registerImage(registry, "unchecked", "disable.gif");
		registerImage(registry, "radio-on", "radio_on.gif");
		registerImage(registry, "radio-off", "radio_off.gif");
		registerImage(registry, "running", "running.png");
		registerImage(registry, "stopped", "stopped.png");
		registerImage(registry, "suspended", "suspended.png");
		registerImage(registry, "changing", "changing.png");
		
		registerImage(registry, "running2", "poweron.png");
		registerImage(registry, "stopped2", "poweroff.png");
		registerImage(registry, "suspended2", "suspend.png");
		registerImage(registry, "terminated", "delete.png");
		registerImage(registry, "processing", "processing.png");
		
		registerImage(registry, "cloudscope", "cloudscope.png");
		registerImage(registry, "location", "location.png");
		registerImage(registry, "instance", "instance.png");
		registerImage(registry, "instance2", "instance2.png");
	}

	/**
	 * ImageRegistryにイメージを登録します。
	 * 
	 * @param registry
	 *            ImageRegistryオブジェクト
	 * @param key
	 *            取得キー
	 * @param fileName
	 *            イメージファイル名
	 */
	private void registerImage(ImageRegistry registry, String key,
			String fileName) {
		try {
			URL url = new URL(getDefault().getBundle().getEntry("/"), "icons/"
					+ fileName);
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			registry.put(key, desc);
		} catch (Exception e) {
			m_log.debug(e.getMessage(), e);
		}
	}

	/**
	 * @return Returns the separator.
	 */
	public String getSeparator() {
		if (separator == null || separator.compareTo("") == 0) {
			separator = DEFAULT_SEPARATOR;
		}
		return separator;
	}

	/**
	 * @param separator
	 *            The separator to set.
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * プラグインIDとして利用できる、固有のIDを返します。
	 * 
	 * @return プラグインID
	 * @see Bundle#getSymbolicName()
	 */
	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Check if this application is run in RAP
	 * @return Is RAP or not
	 */
	public static Boolean isRAP() {
		if( null == is_rap ){
			// getAppName() will return null in RAP
			is_rap = (null == Display.getAppName());
		}
		return is_rap;
	}
	
	/**
	 * 終了前に確認ダイアログを表示するかを返します
	 */
	public static Boolean isExitConfirm() {
		String exitConfrim = System.getProperty("exit.confirm");
		if (exitConfrim != null && exitConfrim.equals("false")) {
			return false;
		}
		return true;
	}

	/******************************** xCloud ********************************/
	private static String pluginPath;
	private Map<RestConnectUnit, IHinemosManager> hinemosManagers = new HashMap<>();
	public static String getPluginPath() {
		if (pluginPath == null) {
			// プラグインがインストールされているパスを取得。
			URL entry = ClusterControlPlugin.getDefault().getBundle().getEntry("/");
			try {
				String url = FileLocator.resolve(entry).toString();
				// URI クラスは、空白を拒否するので、"%20" へエンコード。
				url = url.replaceAll(" ", "%20");
				pluginPath = new File(new URI(url)).getAbsolutePath();
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new IllegalStateException(e);
			}
		}

		return pluginPath;
	}
	
	public List<IHinemosManager> getHinemosManagers(){
		List<RestConnectUnit> newEndpoints = RestConnectManager.getActiveManagerList();
		Set<RestConnectUnit> oldEndpoints = hinemosManagers.keySet();
		
		CollectionComparator.compareCollection(newEndpoints, oldEndpoints, new CollectionComparator.Comparator<RestConnectUnit, RestConnectUnit>() {
			public boolean match(RestConnectUnit o1, RestConnectUnit o2) {return o1 == o2;}
			public void afterO1(RestConnectUnit o1) {hinemosManagers.put(o1, new HinemosManager(o1.getManagerName(), o1.getUrlListStr()));}
			public void afterO2(RestConnectUnit o2) {hinemosManagers.remove(o2);}
		});
		
		return new ArrayList<>(hinemosManagers.values());
	}
	
	public IHinemosManager getHinemosManager(String managerName){
		// マネージャ名を直接指定する場合、対象マネージャの存在のみ確認
		for(IHinemosManager manager: getHinemosManagers()){
			if(managerName.equals(manager.getManagerName())){
				return manager;
			}
		}
		return null;
	}
}
