/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.preference;

import java.util.Set;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.ui.util.OptionUtil;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ui.constant.XMLConstant;
import com.clustercontrol.utility.ui.settings.composite.UtilityDirectoryFieldEditor;
import com.clustercontrol.utility.util.IUtilityPreferenceStore;
import com.clustercontrol.utility.util.UtilityPreferenceStore;

/**
 * 設定ページクラス(XMLファイル名)<BR>
 * 
 * @version 6.1.0
 * @since 1.2.0
 * 
 * 
 */
public class SettingToolsXMLPreferencePage extends FieldEditorPreferencePage 
implements IWorkbenchPreferencePage {

	// TODO 6.2で全部PreferencePageConstantを参照
	public static final String KEY_XML = PreferencePageConstant.KEY_XML;
	public static final String VALUE_XML = PreferencePageConstant.VALUE_XML;
	private UtilityDirectoryFieldEditor XML = null;

	public static final String KEY_DIFF_XML = PreferencePageConstant.KEY_DIFF_XML;
	public static final String VALUE_DIFF_XML = PreferencePageConstant.VALUE_DIFF_XML;
	public static final String KEY_DIFF_MODE = PreferencePageConstant.KEY_DIFF_MODE;
	public static final String VALUE_DIFF_MODE = PreferencePageConstant.VALUE_DIFF_MODE;
	private UtilityDirectoryFieldEditor DIFF_XML = null;
	private RadioGroupFieldEditor diffMode = null;

	public static final String VALUE_INFRA = PreferencePageConstant.VALUE_INFRA;
	
	public static final String VALUE_JOBMAP_IMAGE_FOLDER = PreferencePageConstant.VALUE_JOBMAP_IMAGE_FOLDER;
	public static final String VALUE_BACKUP_FOLDER = PreferencePageConstant.VALUE_BACKUP_FOLDER;
	public static final String VALUE_NODEMAP_BG_FOLDER = PreferencePageConstant.VALUE_NODEMAP_BG_FOLDER;
	public static final String VALUE_NODEMAP_ICON_FOLDER = PreferencePageConstant.VALUE_NODEMAP_ICON_FOLDER;
	public static final String KEY_BACKUP_IMPORT = PreferencePageConstant.KEY_BACKUP_IMPORT;
	public static final String KEY_BACKUP_EXPORT = PreferencePageConstant.KEY_BACKUP_EXPORT;
	public static final String KEY_BACKUP_CLEAR = PreferencePageConstant.KEY_BACKUP_CLEAR;
	public static final String DEFAULT_VALUE_BACKUP_IMPORT = PreferencePageConstant.DEFAULT_VALUE_BACKUP_IMPORT;
	public static final String DEFAULT_VALUE_BACKUP_EXPORT = PreferencePageConstant.DEFAULT_VALUE_BACKUP_EXPORT;
	public static final String DEFAULT_VALUE_BACKUP_CLEAR = PreferencePageConstant.DEFAULT_VALUE_BACKUP_CLEAR;

	
	private StringFieldEditor INFRA = null;
	private StringFieldEditor nodemapIconDir = null;
	private StringFieldEditor jobmapImageDir = null;
	private StringFieldEditor backupDir = null;
	private StringFieldEditor node = null;

	private StringFieldEditor nodeHostname = null;
	private StringFieldEditor nodeCPU = null;
	private StringFieldEditor nodeMemory = null;
	private StringFieldEditor nodeNetworkInterface = null;
	private StringFieldEditor nodeDisk = null;
	private StringFieldEditor nodeFS = null;
	private StringFieldEditor nodeDevice = null;
	private StringFieldEditor nodeVariable = null;
	private StringFieldEditor nodeNote = null;

	private StringFieldEditor scope = null;
	private StringFieldEditor scopeNode = null;
	private StringFieldEditor notify = null;
	private StringFieldEditor mailTemplate = null;
	private StringFieldEditor logformat = null;

	private StringFieldEditor calender = null;
	private StringFieldEditor calenderPattern = null;
	private StringFieldEditor accessUser = null;
	private StringFieldEditor accessRole = null;
	private StringFieldEditor accessRoleUser = null;
	private StringFieldEditor accessSystemPrivilege = null;
	private StringFieldEditor accessObjectPrivilege = null;

	private StringFieldEditor agent = null;
	private StringFieldEditor http = null;
	private StringFieldEditor httpScenario = null;
	private StringFieldEditor perf = null;
	private StringFieldEditor ping = null;
	private StringFieldEditor port = null;
	private StringFieldEditor process = null;
	private StringFieldEditor snmp = null;
	private StringFieldEditor snmptrap = null;
	private StringFieldEditor sql = null;
	private StringFieldEditor jmx = null;

	private StringFieldEditor command = null;
	private StringFieldEditor systemlog = null;
	private StringFieldEditor logfile = null;
	private StringFieldEditor winservice = null;
	private StringFieldEditor winevent = null;
	private StringFieldEditor customtrap = null;
	
	private StringFieldEditor logcount = null;
	private StringFieldEditor binaryfile = null;
	private StringFieldEditor pcap = null;
	private StringFieldEditor integration = null;
	private StringFieldEditor correlation = null;
	
	private StringFieldEditor jobMaster = null;
	private StringFieldEditor jobSchedule = null;
	private StringFieldEditor jobFileCheck = null;
	private StringFieldEditor jobManual = null;
	
	private StringFieldEditor maintenance = null;
	private StringFieldEditor hinemosProperty = null;

	private StringFieldEditor masterPlatform = null;
	private StringFieldEditor masterCollect = null;
	private StringFieldEditor masterJmx = null;
	
	private StringFieldEditor hubTransfer = null;
	private StringFieldEditor infra = null;
	private StringFieldEditor infraFile = null;

	// ノードマップ
	private StringFieldEditor nodeMapSetting = null;
	private StringFieldEditor nodeMapBgImage = null;
	private StringFieldEditor nodeMapIcon = null;
	
	// レポートスケジュール
	private StringFieldEditor reportSchedule=null;
	// テンプレート
	private StringFieldEditor reportTemplate=null;
	
	// Jobmap
	private StringFieldEditor jobMapSetting = null;

	// AWS
	private StringFieldEditor awsUser = null;
	private StringFieldEditor awsMonService = null;
	private StringFieldEditor awsMonBilling = null;
	
	private BooleanFieldEditor backupImport=null;
	private BooleanFieldEditor backupExport=null;
	private BooleanFieldEditor backupDelete=null;
	
	public SettingToolsXMLPreferencePage() {
		super(GRID);
		setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
	}
	
	public static class Initializer extends AbstractPreferenceInitializer {
		@Override
		public void initializeDefaultPreferences() {
			IUtilityPreferenceStore store = UtilityPreferenceStore.get();
			SettingToolsXMLPreferenceInitializer.init(store);
		}
	}
	
	/**
	 * 設定フィールドを生成します。
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;

		// XML ディレクトリ
		Group groupXML = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupXML.setLayoutData(gridData);

		groupXML.setText(Messages.getString("perference.common"));

		XML= new UtilityDirectoryFieldEditor(KEY_XML, Messages.getString("perference.xml.directory"), groupXML) {
			@Override
			public void setValidateStrategy(int value) {
				// キーストロークでバリデーションが実施されるように変更。
				super.setValidateStrategy(VALIDATE_ON_KEY_STROKE);
			}};
		XML.setTextLimit(256);
		XML.setChangeButtonText(Messages.getString("perference.xml.button.browse"));
		this.addField(XML);

		// Diff XML ディレクトリ
		Group groupDiffXML = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupDiffXML.setLayoutData(gridData);
		
		groupDiffXML.setText(Messages.getString("perference.diff"));
		
		Composite compositeDiffXML = new Composite(groupDiffXML, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		compositeDiffXML.setLayoutData(gridData);
		
		DIFF_XML = new UtilityDirectoryFieldEditor(KEY_DIFF_XML, Messages.getString("perference.xml.directory"), compositeDiffXML) {
			@Override
			public void setValidateStrategy(int value) {
				// キーストロークでバリデーションが実施されるように変更。
				super.setValidateStrategy(VALIDATE_ON_KEY_STROKE);
			}};
		DIFF_XML.setTextLimit(256);
		DIFF_XML.setChangeButtonText(Messages.getString("perference.xml.button.browse"));
		this.addField(DIFF_XML);
		
		//差分比較出力方法
		diffMode = new RadioGroupFieldEditor(KEY_DIFF_MODE,
				Messages.getString("perference.diff.mode"),
				2,
				new String[][] {{Messages.getString("perference.diff.mode.only.difference"), "false"},
								{Messages.getString("perference.diff.mode.all"), "true"}},
				groupDiffXML,
				true);
		this.addField(diffMode);

		// 環境構築ベース ディレクトリ
		Group groupInfra = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupInfra.setLayoutData(gridData);
		groupInfra.setText(Messages.getString("perference.infra"));
		INFRA = new StringFieldEditor(VALUE_INFRA, Messages.getString("perference.infra.dir"), groupInfra);
		INFRA.setTextLimit(256);
		this.addField(INFRA);

		// NodeMapイメージファイルフォルダ
		Group groupNodeMapBgImageFolder = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNodeMapBgImageFolder.setLayoutData(gridData);
		groupNodeMapBgImageFolder.setText(Messages.getString("perference.nodemap"));
		
		nodemapIconDir = new StringFieldEditor(VALUE_NODEMAP_BG_FOLDER, Messages.getString("perference.nodemap.bg.dir"), groupNodeMapBgImageFolder);
		nodemapIconDir.setTextLimit(256);
		this.addField(nodemapIconDir);
		
		nodemapIconDir = new StringFieldEditor(VALUE_NODEMAP_ICON_FOLDER, Messages.getString("perference.nodemap.icon.dir"), groupNodeMapBgImageFolder);
		nodemapIconDir.setTextLimit(256);
		this.addField(nodemapIconDir);

		// Jobmapイメージファイルフォルダ
		Group groupImageFolder = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupImageFolder.setLayoutData(gridData);
		groupImageFolder.setText(Messages.getString("perference.jobmap"));
		jobmapImageDir = new StringFieldEditor(VALUE_JOBMAP_IMAGE_FOLDER, Messages.getString("perference.jobmap.dir"), groupImageFolder);
		jobmapImageDir.setTextLimit(256);
		this.addField(jobmapImageDir);

		// バックアップ方法
		Group groupBackup = new Group(parent, SWT.SHADOW_NONE);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupBackup.setLayoutData(gridData);
		groupBackup.setText(Messages.getString("perference.xml.backup"));
		
		backupImport = new BooleanFieldEditor(
				KEY_BACKUP_IMPORT,
				HinemosModuleConstant.STRING_BACKUP_IMPORT,
				groupBackup
				);
		this.addField(backupImport);
		
		backupExport = new BooleanFieldEditor(
				KEY_BACKUP_EXPORT,
				HinemosModuleConstant.STRING_BACKUP_EXPORT,
				groupBackup
				);
		this.addField(backupExport);
		
		backupDelete = new BooleanFieldEditor(
				KEY_BACKUP_CLEAR,
				HinemosModuleConstant.STRING_BACKUP_CLEAR,
				groupBackup
				);
		this.addField(backupDelete);
		
		backupDir = new StringFieldEditor(VALUE_BACKUP_FOLDER, Messages.getString("perference.backup.dir"), groupBackup);
		backupDir.setTextLimit(256);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		backupDir.getTextControl(groupBackup).setLayoutData(gridData);
		
		this.addField(backupDir);
		
		groupBackup.setLayout(new GridLayout(3, true));
		
		
		// XMLファイル
		Group groupXMLFile = new Group(parent, SWT.SHADOW_NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		groupXMLFile.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupXMLFile.setLayoutData(gridData);
		
		groupXMLFile.setText(Messages.getString("perference.xml.file"));
		
		// 共通
		Group group = new Group(groupXMLFile, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group.setLayoutData(gridData);

		group.setText(Messages.getString("platform"));

		// ノード
		node = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE ,
				group);
		node.setTextLimit(256);
		this.addField(node);

		// ノードホスト名
		nodeHostname = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_HOSTNAME,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_HOSTNAME ,
				group);
		nodeHostname.setTextLimit(256);
		this.addField(nodeHostname);

		// ノードCPU
		nodeCPU = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_CPU,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_CPU ,
				group);
		nodeCPU.setTextLimit(256);
		this.addField(nodeCPU);

		// ノードメモリ
		nodeMemory = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_MEMORY,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_MEMORY ,
				group);
		nodeMemory.setTextLimit(256);
		this.addField(nodeMemory);

		// ノードネットワークインタフェース
		nodeNetworkInterface = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_NETWORKINTERFACE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_NETWORKINTERFACE ,
				group);
		nodeNetworkInterface.setTextLimit(256);
		this.addField(nodeNetworkInterface);

		// ノードディスク
		nodeDisk = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_DISK,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_DISK,
				group);
		nodeDisk.setTextLimit(256);
		this.addField(nodeDisk);

		// ノードファイルシステム
		nodeFS = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_FS,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_FS ,
				group);
		nodeFS.setTextLimit(256);
		this.addField(nodeFS);

		// ノードデバイス
		nodeDevice = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_DEVICE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_DEVICE ,
				group);
		nodeDevice.setTextLimit(256);
		this.addField(nodeDevice);

		// ノード変数名
		nodeVariable = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_VARIABLE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_VARIABLE ,
				group);
		nodeVariable.setTextLimit(256);
		this.addField(nodeVariable);

		// ノード備考
		nodeNote = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_NOTE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_NOTE,
				group);
		nodeNote.setTextLimit(256);
		this.addField(nodeNote);

		//スコープ
		scope = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_SCOPE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_SCOPE ,
				group);
		scope.setTextLimit(256);
		this.addField(scope);

		//スコープノード
		scopeNode = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_SCOPE_NODE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_SCOPE_NODE,
				group);
		scopeNode.setTextLimit(256);
		this.addField(scopeNode);

		//通知
		notify = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_NOTIFY,
				HinemosModuleConstant.STRING_PLATFORM_NOTIFY,
				group);
		notify.setTextLimit(256);
		this.addField(notify);
		
		//メールテンプレート
		mailTemplate = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_MAIL_TEMPLATE,
				HinemosModuleConstant.STRING_PLATFORM_MAIL_TEMPLATE,
				group);
		mailTemplate.setTextLimit(256);
		this.addField(mailTemplate);
		
		// カレンダー
		calender = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_CALENDAR,
				HinemosModuleConstant.STRING_PLATFORM_CALENDAR ,
				group);
		calender.setTextLimit(256);
		this.addField(calender);
		
		// カレンダーパターン
		calenderPattern = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_CALENDAR_PATTERN,
				HinemosModuleConstant.STRING_PLATFORM_CALENDAR_PATTERN ,
				group);
		calenderPattern.setTextLimit(256);
		this.addField(calenderPattern);
		
		//メンテナンス
		maintenance = new StringFieldEditor(XMLConstant.DEFAULT_XML_SYSYTEM_MAINTENANCE,
				HinemosModuleConstant.STRING_SYSYTEM_MAINTENANCE,
				group);
		maintenance.setTextLimit(256);
		this.addField(maintenance);
		
		//ログフォーマット
		logformat = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_LOG_FORMAT,
				HinemosModuleConstant.STRING_PLATFORM_LOG_FORMAT,
				group);
		logformat.setTextLimit(256);
		this.addField(logformat);

		hinemosProperty = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_HINEMOS_PROPERTY,
				HinemosModuleConstant.STRING_PLATFORM_HINEMOS_PROPERTY,
				group);
		hinemosProperty.setTextLimit(256);
		this.addField(hinemosProperty);
		
		//アクセス(ユーザ)
		accessUser = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_USER,
				HinemosModuleConstant.STRING_PLATFORM_ACCESS_USER,
				group);
		accessUser.setTextLimit(256);
		this.addField(accessUser);

		//アクセス(ロール)
		accessRole = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_ROLE,
				HinemosModuleConstant.STRING_PLATFORM_ACCESS_ROLE,
				group);
		accessRole.setTextLimit(256);
		this.addField(accessRole);
		
		//アクセス(ロール内ユーザ)
		accessRoleUser = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_ROLE_USER,
				HinemosModuleConstant.STRING_PLATFORM_ACCESS_ROLE_USER,
				group);
		accessRoleUser.setTextLimit(256);
		this.addField(accessRoleUser);

		//アクセス(システム権限)
		accessSystemPrivilege = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_SYSTEM_PRIVILEGE,
				HinemosModuleConstant.STRING_PLATFORM_ACCESS_SYSTEM_PRIVILEGE,
				group);
		accessSystemPrivilege.setTextLimit(256);
		this.addField(accessSystemPrivilege);
		
		//アクセス(オブジェクト権限)
		accessObjectPrivilege = new StringFieldEditor(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_OBJECT_PRIVILEGE,
				HinemosModuleConstant.STRING_PLATFORM_ACCESS_OBJECT_PRIVILEGE,
				group);
		accessObjectPrivilege.setTextLimit(256);
		this.addField(accessObjectPrivilege);

		Group group2 = new Group(groupXMLFile, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group2.setLayoutData(gridData);

		group2.setText(Messages.getString("monitorsetting"));
		
		// HTTPシナリオ監視
		httpScenario = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_HTTP_SCENARIO,
				HinemosModuleConstant.STRING_MONITOR_HTTP_SCENARIO,
				group2);
		httpScenario.setTextLimit(256);
		this.addField(httpScenario);
		
		// HTTP監視
		http = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_HTTP,
				HinemosModuleConstant.STRING_MONITOR_HTTP,
				group2);
		http.setTextLimit(256);
		this.addField(http);
		
		// エージェント監視
		agent = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_AGENT,
				HinemosModuleConstant.STRING_MONITOR_AGENT,
				group2);
		agent.setTextLimit(256);
		this.addField(agent);
		
		//jmx監視
		jmx = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_JMX,
				HinemosModuleConstant.STRING_MONITOR_JMX,
				group2);
		jmx.setTextLimit(256);
		this.addField(jmx);
		
		// PING監視
		ping = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_PING,
				HinemosModuleConstant.STRING_MONITOR_PING,
				group2);
		ping.setTextLimit(256);
		this.addField(ping);
		
		//snmptrap監視
		snmptrap = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_SNMPTRAP,
				HinemosModuleConstant.STRING_MONITOR_SNMPTRAP,
				group2);
		snmptrap.setTextLimit(256);
		this.addField(snmptrap);
		
		//snmp監視
		snmp = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_SNMP,
				HinemosModuleConstant.STRING_MONITOR_SNMP,
				group2);
		snmp.setTextLimit(256);
		this.addField(snmp);
		
		//sql監視
		sql = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_SQL,
				HinemosModuleConstant.STRING_MONITOR_SQL,
				group2);
		sql.setTextLimit(256);
		this.addField(sql);
		
		//Windows イベント監視
		winevent = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_WINEVENT,
				HinemosModuleConstant.STRING_MONITOR_WINEVENT,
				group2);
		winevent.setTextLimit(256);
		this.addField(winevent);
		
		//Windows サービス監視
		winservice = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_WINSERVICE,
				HinemosModuleConstant.STRING_MONITOR_WINSERVICE,
				group2);
		winservice.setTextLimit(256);
		this.addField(winservice);
		
		//カスタムトラップ監視
		customtrap = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_CUSTOMTRAP,
				HinemosModuleConstant.STRING_MONITOR_CUSTOMTRAP,
				group2);
		customtrap.setTextLimit(256);
		this.addField(customtrap);
		
		//カスタム監視
		command = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_CUSTOM,
				HinemosModuleConstant.STRING_MONITOR_CUSTOM,
				group2);
		command.setTextLimit(256);
		this.addField(command);
		
		//port監視
		port = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_PORT,
				HinemosModuleConstant.STRING_MONITOR_PORT,
				group2);
		port.setTextLimit(256);
		this.addField(port);
		
		//システムログ監視
		systemlog = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_SYSTEMLOG,
				HinemosModuleConstant.STRING_MONITOR_SYSTEMLOG,
				group2);
		systemlog.setTextLimit(256);
		this.addField(systemlog);
		
		//バイナリファイル監視
		binaryfile = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_BINARYFILE,
				HinemosModuleConstant.STRING_MONITOR_BINARYFILE,
				group2);
		binaryfile.setTextLimit(256);
		this.addField(binaryfile);
		
		//パケットキャプチャ監視
		pcap = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_PCAP,
				HinemosModuleConstant.STRING_MONITOR_PCAP,
				group2);
		pcap.setTextLimit(256);
		this.addField(pcap);
		
		//process監視
		process = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_PROCESS,
				HinemosModuleConstant.STRING_MONITOR_PROCESS,
				group2);
		process.setTextLimit(256);
		this.addField(process);
		
		// リソース監視
		perf = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_PERFORMANCE,
				HinemosModuleConstant.STRING_MONITOR_PERFORMANCE,
				group2);
		perf.setTextLimit(256);
		this.addField(perf);
		
		//ログファイル監視
		logfile = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_LOGFILE,
				HinemosModuleConstant.STRING_MONITOR_LOGFILE,
				group2);
		logfile.setTextLimit(256);
		this.addField(logfile);
		
		//ログ件数監視
		logcount = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_LOGCOUNT,
				HinemosModuleConstant.STRING_MONITOR_LOGCOUNT,
				group2);
		logcount.setTextLimit(256);
		this.addField(logcount);
		
		//収集値統合監視
		integration = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_INTEGRATION,
				HinemosModuleConstant.STRING_MONITOR_INTEGRATION,
				group2);
		integration.setTextLimit(256);
		this.addField(integration);
		
		//相関係数監視
		correlation = new StringFieldEditor(XMLConstant.DEFAULT_XML_MONITOR_CORRELATION,
				HinemosModuleConstant.STRING_MONITOR_CORRELATION,
				group2);
		correlation.setTextLimit(256);
		this.addField(correlation);

		
		Group group3 = new Group(groupXMLFile, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group3.setLayoutData(gridData);

		group3.setText(HinemosModuleConstant.STRING_JOB);

		// ジョブマスター
		jobMaster = new StringFieldEditor(XMLConstant.DEFAULT_XML_JOB_MST,
				HinemosModuleConstant.STRING_JOB_MST,
				group3);
		jobMaster.setTextLimit(256);
		this.addField(jobMaster);

		// ジョブスケジュール
		jobSchedule = new StringFieldEditor(XMLConstant.DEFAULT_XML_JOB_SCHEDULE,
				HinemosModuleConstant.STRING_JOB_SCHEDULE,
				group3);
		jobSchedule.setTextLimit(256);
		this.addField(jobSchedule);
		
		// ジョブファイルチェック
		jobFileCheck = new StringFieldEditor(XMLConstant.DEFAULT_XML_JOB_FILECHECK,
				HinemosModuleConstant.STRING_JOB_FILECHECK,
				group3);
		jobFileCheck.setTextLimit(256);
		this.addField(jobFileCheck);

		// ジョブマニュアル
		jobManual = new StringFieldEditor(XMLConstant.DEFAULT_XML_JOB_MANUAL,
				HinemosModuleConstant.STRING_JOB_MANUAL,
				group3);
		jobManual.setTextLimit(256);
		this.addField(jobManual);
		
		// Hub設定
		Group groupHub = new Group(groupXMLFile, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupHub.setLayoutData(gridData);

		groupHub.setText(HinemosModuleConstant.STRING_HUB);

		// 転送設定
		hubTransfer = new StringFieldEditor(XMLConstant.DEFAULT_XML_HUB_TRANSFER,
				HinemosModuleConstant.STRING_HUB_TRANSFER,
				groupHub);
		hubTransfer.setTextLimit(256);
		this.addField(hubTransfer);

		Group group4 = new Group(groupXMLFile, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		group4.setLayoutData(gridData);

		group4.setText(Messages.getString("infra"));

		// 環境構築
		infra = new StringFieldEditor(XMLConstant.DEFAULT_XML_INFRA_SETTING,
				HinemosModuleConstant.STRING_INFRA_SETTING,
				group4);
		infra.setTextLimit(256);
		this.addField(infra);

		// 環境構築ファイル
		infraFile = new StringFieldEditor(XMLConstant.DEFAULT_XML_INFRA_FILE,
				HinemosModuleConstant.STRING_INFRA_FILE,
				group4);
		infraFile.setTextLimit(256);
		this.addField(infraFile);
		
		
		Group group5 = new Group(groupXMLFile, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		group5.setLayoutData(gridData);

		group5.setText(Messages.getString("master"));

		// プラットフォーム
		masterPlatform = new StringFieldEditor(XMLConstant.DEFAULT_XML_MASTER_PLATFORM,
				HinemosModuleConstant.STRING_MASTER_PLATFORM,
				group5);
		masterPlatform.setTextLimit(256);
		this.addField(masterPlatform);

		// 収集項目
		masterCollect = new StringFieldEditor(XMLConstant.DEFAULT_XML_MASTER_COLLECT,
				HinemosModuleConstant.STRING_MASTER_COLLECT,
				group5);
		masterCollect.setTextLimit(256);
		this.addField(masterCollect);

		// JMXマスター
		masterJmx = new StringFieldEditor(XMLConstant.DEFAULT_XML_MASTER_JMX,
				HinemosModuleConstant.STRING_MASTER_JMX,
				group5);
		masterJmx.setTextLimit(256);
		this.addField(masterJmx);
		
		Set<String> options = EndpointManager.getAllOptions();
		if (options.contains(OptionUtil.TYPE_ENTERPRISE)) {
			// ノードマップのグループ
			Group groupNode = new Group(groupXMLFile, SWT.SHADOW_NONE);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			groupNode.setLayoutData(gridData);
			groupNode.setText(Messages.getString("nodemap"));

			// ノードマップ
			nodeMapSetting = new StringFieldEditor(XMLConstant.DEFAULT_XML_NODE_MAP_SETTING,
					HinemosModuleConstant.STRING_NODE_MAP,
					groupNode);
			nodeMapSetting.setTextLimit(256);
			this.addField(nodeMapSetting);

			nodeMapBgImage = new StringFieldEditor(XMLConstant.DEFAULT_XML_NODE_MAP_IMAGE,
					HinemosModuleConstant.STRING_NODE_MAP_IMAGE_BG,
					groupNode);
			nodeMapBgImage.setTextLimit(256);
			this.addField(nodeMapBgImage);

			nodeMapIcon = new StringFieldEditor(XMLConstant.DEFAULT_XML_NODE_MAP_ICON,
					HinemosModuleConstant.STRING_NODE_MAP_IMAGE_ICON,
					groupNode);
			nodeMapIcon.setTextLimit(256);
			this.addField(nodeMapIcon);

			// ジョブマップのグループ
			Group groupJobmap = new Group(groupXMLFile, SWT.SHADOW_NONE);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			groupJobmap.setLayoutData(gridData);
			groupJobmap.setText(Messages.getString("jobmap"));

			// ジョブマップ
			jobMapSetting = new StringFieldEditor(XMLConstant.DEFAULT_XML_JOBMAP_IMAGE,
					HinemosModuleConstant.STRING_JOB_MAP_IMAGE,
					groupJobmap);
			jobMapSetting.setTextLimit(256);
			this.addField(jobMapSetting);

			// レポーティングのグループ
			Group groupReport = new Group(groupXMLFile, SWT.SHADOW_NONE);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 2;
			groupReport.setLayoutData(gridData);
			groupReport.setText(Messages.getString("report"));

			// レポーティング スケジュール
			reportSchedule = new StringFieldEditor(XMLConstant.DEFAULT_XML_REPORT_SCHEDULE,
					HinemosModuleConstant.STRING_REPORT_SCHEDULE,
					groupReport);
			reportSchedule.setTextLimit(256);
			this.addField(reportSchedule);

			// レポーティング テンプレート
			reportTemplate = new StringFieldEditor(XMLConstant.DEFAULT_XML_REPORT_TEMPLATE,
					HinemosModuleConstant.STRING_REPORT_TEMPLATE,
					groupReport);
			reportTemplate.setTextLimit(256);
			this.addField(reportTemplate);
		}

		// AWSのグループ
		if (options.contains(OptionUtil.TYPE_XCLOUD)) {
			Group groupAws = new Group(groupXMLFile, SWT.SHADOW_NONE);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 1;
			groupAws.setLayoutData(gridData);
			groupAws.setText(Messages.getString("cloud"));

			// AWS ユーザ
			awsUser = new StringFieldEditor(XMLConstant.DEFAULT_XML_CLOUD_USER,
					HinemosModuleConstant.STRING_CLOUD_USER,
					groupAws);
			awsUser.setTextLimit(256);
			this.addField(awsUser);


			// Cloud サービス監視
			awsMonService = new StringFieldEditor(XMLConstant.DEFAULT_XML_CLOUD_MON_SERVICE,
					HinemosModuleConstant.STRING_CLOUD_MONITOR_SERVICE,
					groupAws);
			awsMonService.setTextLimit(256);
			this.addField(awsMonService);

			// Cloud 課金監視
			awsMonBilling = new StringFieldEditor(XMLConstant.DEFAULT_XML_CLOUD_MON_BILLING,
					HinemosModuleConstant.STRING_CLOUD_MONITOR_BILLING,
					groupAws);
			awsMonBilling.setTextLimit(256);
			this.addField(awsMonBilling);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {

	}
	
	@Override
	protected void adjustGridLayout() {
//		super.adjustGridLayout();
		// この関数が動作すると、レイアウトが崩れるのでキャンセル。
		// 原因は、フィールドエディターをプリファレンスのコントロールでなくグループに張っているから。
		// FieldEditorPreferencePage の使用方法の範囲外になる。
	}
	
	/**
	 * プロパティファイルへ書き出す
	 * @param xml
	 */
	public static void writeXmlProperties(String xmlDirPass){
		//リソースストアからファイルパスを取得
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		
		//ディレクトリ無指定時のNullPointer例外回避のため
		if (xmlDirPass != null) {
			store.setValue(KEY_XML, xmlDirPass);
		}
	}
}