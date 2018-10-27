/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.clustercontrol.ui.util.OptionUtil;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.bean.FuncTreeItem;
import com.clustercontrol.utility.settings.ui.constant.CommandConstant;
import com.clustercontrol.utility.settings.ui.constant.XMLConstant;

/**
 * 
 * 機能ツリーを作ります。
 * 
 * @version 6.1.0
 * 
 */
public class BuildFunctionTreeAction {
	
	/** オブジェクト権限のFuncTree */
	private static FuncTreeItem funcTreeObjectPrivilege=null;
	
	/**
	 * 機能ツリーを作ります。
	 * @return
	 */
	static public FuncTreeItem buildTree(){
		
		// 大枠の作成
		FuncTreeItem funcTree = new FuncTreeItem();
		FuncInfo info = new FuncInfo("","","",CommandConstant.WEIGHT_OTHER,"",false,"");
		funcTree.setData(info);

		//
		// 大項目の作成
		//
		
		// 設定
		FuncTreeItem funcTreeSetting = new FuncTreeItem();
		info = new FuncInfo("",
				HinemosModuleConstant.STRING_SETTING,
				"",
				CommandConstant.WEIGHT_OTHER,
				"",
				false,
				"");
		funcTreeSetting.setData(info);
		funcTree.addChildren(funcTreeSetting);
		
		// マスタ
		FuncTreeItem funcTreeMaster = new FuncTreeItem();
		info = new FuncInfo("",
				HinemosModuleConstant.STRING_MASTER,
				"",
				CommandConstant.WEIGHT_OTHER,
				"",
				false,
				"");
		funcTreeMaster.setData(info);
		funcTree.addChildren(funcTreeMaster);

		// 設定ここから
		
		FuncTreeItem funcTreePlatform = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM,
				HinemosModuleConstant.STRING_PLATFORM,
				"",
				CommandConstant.WEIGHT_OTHER,
				"",
				false,
				"");
		funcTreePlatform.setData(info);

		FuncTreeItem funcTreeChild = new FuncTreeItem();
		
		//リポジトリ(ノード)
		//ノードのXMLファイルは複数存在するためArrayListでセット
		List<String> xmlFiles = new ArrayList<String>();
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_HOSTNAME);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_CPU);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_MEMORY);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_NETWORKINTERFACE);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_DISK);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_FS);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_DEVICE);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_VARIABLE);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_NOTE);
		List<String> objectTypes = new ArrayList<String>();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_REPOSITORY_NODE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE,
				xmlFiles,
				CommandConstant.WEIGHT_PLATFORM_REPOSITORY_NODE,
				CommandConstant.ACTION_PLATFORM_REPOSITORY_NODE,
				true,
				objectTypes);
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);

		//リポジトリ(スコープ)
		//スコープのXMLファイルは複数存在するためArrayListでセット
		xmlFiles = new ArrayList<String>();
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_SCOPE);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_SCOPE_NODE);
		objectTypes = new ArrayList<String>();
		objectTypes.add(HinemosModuleConstant.PLATFORM_REPOSITORY);
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_REPOSITORY_SCOPE,
				HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_SCOPE,
				xmlFiles,
				CommandConstant.WEIGHT_PLATFORM_REPOSITORY_SCOPE,
				CommandConstant.ACTION_PLATFORM_REPOSITORY_SCOPE,
				true,
				objectTypes);
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);

		//通知
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_NOTIFY,
				HinemosModuleConstant.STRING_PLATFORM_NOTIFY,
				XMLConstant.DEFAULT_XML_PLATFORM_NOTIFY,
				CommandConstant.WEIGHT_PLATFORM_NOTIFY,
				CommandConstant.ACTION_PLATFORM_NOTIFY,
				true,
				HinemosModuleConstant.PLATFORM_NOTIFY);
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);
		
		//メールテンプレート
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE,
				HinemosModuleConstant.STRING_PLATFORM_MAIL_TEMPLATE,
				XMLConstant.DEFAULT_XML_PLATFORM_MAIL_TEMPLATE,
				CommandConstant.WEIGHT_PLATFORM_MAIL_TEMPLATE,
				CommandConstant.ACTION_PLATFORM_MAIL_TEMPLATE,
				true,
				HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE);
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);
		
		//カレンダ
		//カレンダのXMLファイルは複数存在するためArrayListでセット
		xmlFiles = new ArrayList<String>();
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_CALENDAR);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_CALENDAR_PATTERN);
		objectTypes = new ArrayList<String>();
		objectTypes.add(HinemosModuleConstant.PLATFORM_CALENDAR);
		objectTypes.add(HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN);
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_CALENDAR,
				HinemosModuleConstant.STRING_PLATFORM_CALENDAR,
				xmlFiles,
				CommandConstant.WEIGHT_PLATFORM_CALENDAR,
				CommandConstant.ACTION_PLATFORM_CALENDAR,
				true,
				objectTypes);
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);

		//メンテナンス
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.SYSYTEM_MAINTENANCE,
				HinemosModuleConstant.STRING_SYSYTEM_MAINTENANCE,
				XMLConstant.DEFAULT_XML_SYSYTEM_MAINTENANCE,
				CommandConstant.WEIGHT_PLATFORM_MAINTENANCE,
				CommandConstant.ACTION_PLATFORM_MAINTENANCE,
				true,
				HinemosModuleConstant.SYSYTEM_MAINTENANCE);
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);
		
		//ログフォーマット
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_LOG_FORMAT,
				HinemosModuleConstant.STRING_PLATFORM_LOG_FORMAT,
				XMLConstant.DEFAULT_XML_PLATFORM_LOG_FORMAT,
				CommandConstant.WEIGHT_PLATFORM_LOG_FORMAT,
				CommandConstant.ACTION_PLATFORM_LOG_FORMAT,
				true,
				HinemosModuleConstant.PLATFORM_LOG_FORMAT);
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);
		
		//Hinemosプロパティ
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_HINEMOS_PROPERTY,
				HinemosModuleConstant.STRING_PLATFORM_HINEMOS_PROPERTY,
				XMLConstant.DEFAULT_XML_PLATFORM_HINEMOS_PROPERTY,
				CommandConstant.WEIGHT_PLATFORM_HINEMOS_PROPERTY,
				CommandConstant.ACTION_PLATFORM_HINEMOS_PROPERTY,
				true,
				"");
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);
		
		//アカウント機能（ユーザ・ロール）
		//XMLファイルは複数存在するためArrayListでセット
		xmlFiles = new ArrayList<String>();
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_USER);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_ROLE);
		xmlFiles.add(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_ROLE_USER);
		objectTypes = new ArrayList<String>();
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_ACCESS_USER_ROLE,
				HinemosModuleConstant.STRING_PLATFORM_ACCESS_USER_ROLE,
				xmlFiles,
				CommandConstant.WEIGHT_PLATFORM_ACCESS_USER_ROLE,
				CommandConstant.ACTION_PLATFORM_ACCESS_USER_ROLE,
				true,
				objectTypes);
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);
		
		//アカウント機能（システム権限）
		//XMLファイルは複数存在するためArrayListでセット
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_ACCESS_SYSTEM_PRIVILEGE,
				HinemosModuleConstant.STRING_PLATFORM_ACCESS_SYSTEM_PRIVILEGE ,
				XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_SYSTEM_PRIVILEGE,
				CommandConstant.WEIGHT_PLATFORM_ACCESS_SYSTEM_PRIVILEGE,
				CommandConstant.ACTION_PLATFORM_ACCESS_SYSTEM_PRIVILEGE,
				true,
				"");
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);
		
		//アカウント機能（オブジェクト権限）
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.PLATFORM_ACCESS_OBJECT_PRIVILEGE,
				HinemosModuleConstant.STRING_PLATFORM_ACCESS_OBJECT_PRIVILEGE,
				XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_OBJECT_PRIVILEGE,
				CommandConstant.WEIGHT_PLATFORM_ACCESS_OBJECT_PRIVILEGE,
				CommandConstant.ACTION_PLATFORM_ACCESS_OBJECT_PRIVILEGE,
				true,
				"");
		funcTreeChild.setData(info);
		funcTreePlatform.addChildren(funcTreeChild);
		funcTreeObjectPrivilege = funcTreeChild;

		// 共通機能のツリーをベースの子供としてつなぐ
		funcTreeSetting.addChildren(funcTreePlatform);

		// 監視管理機能
		FuncTreeItem funcTreeMonitor = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR,
				HinemosModuleConstant.STRING_MONITOR,
				"",
				CommandConstant.WEIGHT_OTHER,
				"",
				false,
				"");
		funcTreeMonitor.setData(info);
		
		//HTTP監視（シナリオ）
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_HTTP_SCENARIO,
				HinemosModuleConstant.STRING_MONITOR_HTTP_SCENARIO,
				XMLConstant.DEFAULT_XML_MONITOR_HTTP_SCENARIO,
				CommandConstant.WEIGHT_MONITOR_HTTP_SCENARIO,
				CommandConstant.ACTION_MONITOR_HTTP_SCENARIO,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//HTTP監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MONITOR_HTTP,
				HinemosModuleConstant.STRING_MONITOR_HTTP,
				XMLConstant.DEFAULT_XML_MONITOR_HTTP,
				CommandConstant.WEIGHT_MONITOR_HTTP,
				CommandConstant.ACTION_MONITOR_HTTP,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//Hinemosエージェント監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_AGENT,
				HinemosModuleConstant.STRING_MONITOR_AGENT,
				XMLConstant.DEFAULT_XML_MONITOR_AGENT,
				CommandConstant.WEIGHT_MONITOR_AGENT,
				CommandConstant.ACTION_MONITOR_AGENT,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//JMX監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_JMX,
				HinemosModuleConstant.STRING_MONITOR_JMX,
				XMLConstant.DEFAULT_XML_MONITOR_JMX,
				CommandConstant.WEIGHT_MONITOR_JMX,
				CommandConstant.ACTION_MONITOR_JMX,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//PING監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_PING,
				HinemosModuleConstant.STRING_MONITOR_PING,
				XMLConstant.DEFAULT_XML_MONITOR_PING,
				CommandConstant.WEIGHT_MONITOR_PING,
				CommandConstant.ACTION_MONITOR_PING,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//SNMPTRAP監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MONITOR_SNMPTRAP,
				HinemosModuleConstant.STRING_MONITOR_SNMPTRAP,
				XMLConstant.DEFAULT_XML_MONITOR_SNMPTRAP,
				CommandConstant.WEIGHT_MONITOR_SNMPTRAP,
				CommandConstant.ACTION_MONITOR_SNMPTRAP,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		funcTreeChild = new FuncTreeItem();
		
		//SNMP監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MONITOR_SNMP,
				HinemosModuleConstant.STRING_MONITOR_SNMP,
				XMLConstant.DEFAULT_XML_MONITOR_SNMP,
				CommandConstant.WEIGHT_MONITOR_SNMP,
				CommandConstant.ACTION_MONITOR_SNMP,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//SQL監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MONITOR_SQL,
				HinemosModuleConstant.STRING_MONITOR_SQL,
				XMLConstant.DEFAULT_XML_MONITOR_SQL,
				CommandConstant.WEIGHT_MONITOR_SQL,
				CommandConstant.ACTION_MONITOR_SQL,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// Windowsイベント監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_WINEVENT,
				HinemosModuleConstant.STRING_MONITOR_WINEVENT,
				XMLConstant.DEFAULT_XML_MONITOR_WINEVENT,
				CommandConstant.WEIGHT_MONITOR_WINEVENT,
				CommandConstant.ACTION_MONITOR_WINEVENT,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// Windowsサービス監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_WINSERVICE,
				HinemosModuleConstant.STRING_MONITOR_WINSERVICE,
				XMLConstant.DEFAULT_XML_MONITOR_WINSERVICE,
				CommandConstant.WEIGHT_MONITOR_WINSERVICE,
				CommandConstant.ACTION_MONITOR_WINSERVICE,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// カスタムトラップ監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MONITOR_CUSTOMTRAP,
				HinemosModuleConstant.STRING_MONITOR_CUSTOMTRAP,
				XMLConstant.DEFAULT_XML_MONITOR_CUSTOMTRAP,
				CommandConstant.WEIGHT_MONITOR_CUSTOMTRAP,
				CommandConstant.ACTION_MONITOR_CUSTOMTRAP,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// カスタム監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MONITOR_CUSTOM,
				HinemosModuleConstant.STRING_MONITOR_CUSTOM,
				XMLConstant.DEFAULT_XML_MONITOR_CUSTOM,
				CommandConstant.WEIGHT_MONITOR_CUSTOM,
				CommandConstant.ACTION_MONITOR_CUSTOM,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//サービス・ポート監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_PORT,
				HinemosModuleConstant.STRING_MONITOR_PORT,
				XMLConstant.DEFAULT_XML_MONITOR_PORT,
				CommandConstant.WEIGHT_MONITOR_PORT,
				CommandConstant.ACTION_MONITOR_PORT,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// システムログ監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_SYSTEMLOG,
				HinemosModuleConstant.STRING_MONITOR_SYSTEMLOG,
				XMLConstant.DEFAULT_XML_MONITOR_SYSTEMLOG,
				CommandConstant.WEIGHT_MONITOR_SYSTEMLOG,
				CommandConstant.ACTION_MONITOR_SYSTEMLOG,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// バイナリファイル監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_BINARYFILE_BIN,
				HinemosModuleConstant.STRING_MONITOR_BINARYFILE,
				XMLConstant.DEFAULT_XML_MONITOR_BINARYFILE,
				CommandConstant.WEIGHT_MONITOR_BINARYFILE,
				CommandConstant.ACTION_MONITOR_BINARYFILE,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// パケットキャプチャ監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_PCAP_BIN,
				HinemosModuleConstant.STRING_MONITOR_PCAP,
				XMLConstant.DEFAULT_XML_MONITOR_PCAP,
				CommandConstant.WEIGHT_MONITOR_PCAP,
				CommandConstant.ACTION_MONITOR_PCAP,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//プロセス監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_PROCESS,
				HinemosModuleConstant.STRING_MONITOR_PROCESS,
				XMLConstant.DEFAULT_XML_MONITOR_PROCESS,
				CommandConstant.WEIGHT_MONITOR_PROCESS,
				CommandConstant.ACTION_MONITOR_PROCESS,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		//リソース監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_PERFORMANCE,
				HinemosModuleConstant.STRING_MONITOR_PERFORMANCE,
				XMLConstant.DEFAULT_XML_MONITOR_PERFORMANCE,
				CommandConstant.WEIGHT_MONITOR_PERFORMANCE,
				CommandConstant.ACTION_MONITOR_PERFORMANCE,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// ログファイル監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_LOGFILE,
				HinemosModuleConstant.STRING_MONITOR_LOGFILE,
				XMLConstant.DEFAULT_XML_MONITOR_LOGFILE,
				CommandConstant.WEIGHT_MONITOR_LOGFILE,
				CommandConstant.ACTION_MONITOR_LOGFILE,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// ログ件数監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_LOGCOUNT,
				HinemosModuleConstant.STRING_MONITOR_LOGCOUNT,
				XMLConstant.DEFAULT_XML_MONITOR_LOGCOUNT,
				CommandConstant.WEIGHT_MONITOR_LOGCOUNT,
				CommandConstant.ACTION_MONITOR_LOGCOUNT,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// 収集値統合監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_INTEGRATION,
				HinemosModuleConstant.STRING_MONITOR_INTEGRATION,
				XMLConstant.DEFAULT_XML_MONITOR_INTEGRATION,
				CommandConstant.WEIGHT_MONITOR_INTEGRATION,
				CommandConstant.ACTION_MONITOR_INTEGRATION,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// 相関係数監視
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_CORRELATION,
				HinemosModuleConstant.STRING_MONITOR_CORRELATION,
				XMLConstant.DEFAULT_XML_MONITOR_CORRELATION,
				CommandConstant.WEIGHT_MONITOR_CORRELATION,
				CommandConstant.ACTION_MONITOR_CORRELATION,
				true,
				com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
		funcTreeChild.setData(info);
		funcTreeMonitor.addChildren(funcTreeChild);
		
		// 監視管理機能のツリーをベースの子供としてつなぐ
		funcTreeSetting.addChildren(funcTreeMonitor);

		FuncTreeItem funcTreeJob = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.JOB,
				HinemosModuleConstant.STRING_JOB,
				"",
				CommandConstant.WEIGHT_OTHER,
				"",
				false,
				"");
		funcTreeJob.setData(info);
		
		//ジョブ
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.JOB_MST,
				HinemosModuleConstant.STRING_JOB_MST,
				XMLConstant.DEFAULT_XML_JOB_MST,
				CommandConstant.WEIGHT_JOB_MST,
				CommandConstant.ACTION_JOB_MST,
				true,
				HinemosModuleConstant.JOB);
		funcTreeChild.setData(info);
		funcTreeJob.addChildren(funcTreeChild);

		//実行契機
		//実行契機のXMLファイルは複数存在するためArrayListでセット
		xmlFiles = new ArrayList<String>();
		xmlFiles.add(XMLConstant.DEFAULT_XML_JOB_SCHEDULE);
		xmlFiles.add(XMLConstant.DEFAULT_XML_JOB_FILECHECK);
		xmlFiles.add(XMLConstant.DEFAULT_XML_JOB_MANUAL);
		objectTypes = new ArrayList<String>();
		objectTypes.add(HinemosModuleConstant.JOB_KICK);
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.JOB_KICK,
				HinemosModuleConstant.STRING_JOB_KICK,
				xmlFiles,
				CommandConstant.WEIGHT_JOB_KICK,
				CommandConstant.ACTION_JOB_KICK,
				true,
				objectTypes);
		funcTreeChild.setData(info);
		funcTreeJob.addChildren(funcTreeChild);

		// 監視管理機能のツリーをベースの子供としてつなぐ
		funcTreeSetting.addChildren(funcTreeJob);
		
		// 収集蓄積ここから
		FuncTreeItem funcTreeHub = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.HUB,
				HinemosModuleConstant.STRING_HUB,
				"",
				CommandConstant.WEIGHT_OTHER,
				"",
				false,
				"");
		funcTreeHub.setData(info);
		
		// 転送設定
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.HUB_TRANSFER,
				HinemosModuleConstant.STRING_HUB_TRANSFER,
				XMLConstant.DEFAULT_XML_HUB_TRANSFER,
				CommandConstant.WEIGHT_HUB_TRANSFER,
				CommandConstant.ACTION_HUB_TRANSFER,
				true,
				HinemosModuleConstant.HUB_TRANSFER);
		funcTreeChild.setData(info);
		funcTreeHub.addChildren(funcTreeChild);

		// 収集蓄積のツリーをベースの子供としてつなぐ
		funcTreeSetting.addChildren(funcTreeHub);
		
		// 収集蓄積ここまで

		// 環境構築ここから
		FuncTreeItem funcTreeInfra = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.INFRA,
				HinemosModuleConstant.STRING_INFRA,
				"",
				CommandConstant.WEIGHT_OTHER,
				"",
				false,
				"");
		funcTreeInfra.setData(info);
		
		// 設定
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.INFRA_SETTING,
				HinemosModuleConstant.STRING_INFRA_SETTING,
				XMLConstant.DEFAULT_XML_INFRA_SETTING,
				CommandConstant.WEIGHT_INFRA_SETTING,
				CommandConstant.ACTION_INFRA_SETTING,
				true,
				HinemosModuleConstant.INFRA);
		funcTreeChild.setData(info);
		funcTreeInfra.addChildren(funcTreeChild);
		
		// ファイル
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.INFRA_FILE,
				HinemosModuleConstant.STRING_INFRA_FILE,
				XMLConstant.DEFAULT_XML_INFRA_FILE,
				CommandConstant.WEIGHT_INFRA_FILE,
				CommandConstant.ACTION_INFRA_FILE,
				true,
				HinemosModuleConstant.INFRA_FILE);
		funcTreeChild.setData(info);
		funcTreeInfra.addChildren(funcTreeChild);
		// 環境構築のツリーをベースの子供としてつなぐ
		funcTreeSetting.addChildren(funcTreeInfra);
		// 環境構築ここまで
		// 設定ここまで
		
		// マスタここから
		// プラットフォームマスタ
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MASTER_PLATFORM,
				HinemosModuleConstant.STRING_MASTER_PLATFORM,
				XMLConstant.DEFAULT_XML_MASTER_PLATFORM,
				CommandConstant.WEIGHT_MASTER_PLATFORM,
				CommandConstant.ACTION_MASTER_PLATFORM,
				true,
				"");
		funcTreeChild.setData(info);
		funcTreeMaster.addChildren(funcTreeChild);
		
		// 収集項目マスタ
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MASTER_COLLECT,
				HinemosModuleConstant.STRING_MASTER_COLLECT,
				XMLConstant.DEFAULT_XML_MASTER_COLLECT,
				CommandConstant.WEIGHT_MASTER_COLLECT,
				CommandConstant.ACTION_MASTER_COLLECT,
				true,
				"");
		funcTreeChild.setData(info);
		funcTreeMaster.addChildren(funcTreeChild);
		
		// JMXマスタ
		funcTreeChild = new FuncTreeItem();
		info = new FuncInfo(HinemosModuleConstant.MASTER_JMX,
				HinemosModuleConstant.STRING_MASTER_JMX,
				XMLConstant.DEFAULT_XML_MASTER_JMX,
				CommandConstant.WEIGHT_MASTER_JMX,
				CommandConstant.ACTION_MASTER_JMX,
				true,
				"");
		funcTreeChild.setData(info);
		funcTreeMaster.addChildren(funcTreeChild);
		// マスタここまで

		// 所持のOptionを取得
		Set<String> options = EndpointManager.getAllOptions();
		if(options.contains(OptionUtil.TYPE_ENTERPRISE)){
			// エンタプライズ
			FuncTreeItem funcTreeEnterprise = new FuncTreeItem();
			info = new FuncInfo("",
					HinemosModuleConstant.STRING_ENTERPRISE,
					"",
					CommandConstant.WEIGHT_OTHER,
					"",
					false,
					"");
			funcTreeEnterprise.setData(info);
			funcTree.addChildren(funcTreeEnterprise);
	
			// ノードマップ
			FuncTreeItem funcNodeMap = new FuncTreeItem();
			info = new FuncInfo("",
					HinemosModuleConstant.STRING_NODE_MAP,
					"",
					CommandConstant.WEIGHT_OTHER,
					"",
					false,
					"");
			funcNodeMap.setData(info);
			funcTreeEnterprise.addChildren(funcNodeMap);
			
			// ジョブマップ
			FuncTreeItem funcJobmap = new FuncTreeItem();
			info = new FuncInfo("",
					HinemosModuleConstant.STRING_JOB_MAP,
					"",
					CommandConstant.WEIGHT_OTHER,
					"",
					false,
					"");
			funcJobmap.setData(info);
			funcTreeEnterprise.addChildren(funcJobmap);
			
			// レポーティング
			FuncTreeItem funcReporting = new FuncTreeItem();
			info = new FuncInfo("",
					HinemosModuleConstant.STRING_REPORT,
					"",
					CommandConstant.WEIGHT_OTHER,
					"",
					false,
					"");
			funcReporting.setData(info);
			funcTreeEnterprise.addChildren(funcReporting);


			// ノードマップここから
			// ノードマップ
			funcTreeChild = new FuncTreeItem();
			info = new FuncInfo(HinemosModuleConstant.NODE_MAP_SETTING,
					HinemosModuleConstant.STRING_NODE_MAP_SETTING,
					XMLConstant.DEFAULT_XML_NODE_MAP_SETTING,
					CommandConstant.WEIGHT_NODE_MAP_SETTING,
					CommandConstant.ACTION_NODE_MAP_SETTING,
					true,
					"");
			funcTreeChild.setData(info);
			funcNodeMap.addChildren(funcTreeChild);
			// ノードマップ（イメージ）
			//ノードマップのXMLファイルは複数存在するためArrayListでセット
			xmlFiles = new ArrayList<String>();
			xmlFiles.add(XMLConstant.DEFAULT_XML_NODE_MAP_IMAGE);
			xmlFiles.add(XMLConstant.DEFAULT_XML_NODE_MAP_ICON);
			objectTypes = new ArrayList<String>();
			funcTreeChild = new FuncTreeItem();
			info = new FuncInfo(HinemosModuleConstant.NODE_MAP_IMAGE,
					HinemosModuleConstant.STRING_NODE_MAP_IMAGE,
					xmlFiles,
					CommandConstant.WEIGHT_NODE_MAP_IMAGE,
					CommandConstant.ACTION_NODE_MAP_IMAGE,
					true,
					objectTypes);
			funcTreeChild.setData(info);
			funcNodeMap.addChildren(funcTreeChild);
			// ノードマップここまで
			
			// レポーティングここから
			// スケジュール
			funcTreeChild = new FuncTreeItem();
			info = new FuncInfo(HinemosModuleConstant.REPORT_SCHEDULE,
					HinemosModuleConstant.STRING_REPORT_SCHEDULE,
					XMLConstant.DEFAULT_XML_REPORT_SCHEDULE,
					CommandConstant.WEIGHT_REPORT_SETTING,
					CommandConstant.ACTION_REPORT_SCHEDULE,
					true,
					"");
			funcTreeChild.setData(info);
			funcReporting.addChildren(funcTreeChild);
	
			// テンプレート
			funcTreeChild = new FuncTreeItem();
			info = new FuncInfo(HinemosModuleConstant.REPORT_TEMPLATE,
					HinemosModuleConstant.STRING_REPORT_TEMPLATE,
					XMLConstant.DEFAULT_XML_REPORT_TEMPLATE,
					CommandConstant.WEIGHT_REPORT_TEMPLATE,
					CommandConstant.ACTION_REPORT_TEMPLATE,
					true,
					"");
			funcTreeChild.setData(info);
			funcReporting.addChildren(funcTreeChild);
			// レポーティングここまで
			
			// ジョブマップここから
			// イメージ
			funcTreeChild = new FuncTreeItem();
			info = new FuncInfo(HinemosModuleConstant.JOB_MAP_IMAGE,
					HinemosModuleConstant.STRING_JOB_MAP_IMAGE,
					XMLConstant.DEFAULT_XML_JOBMAP_IMAGE,
					CommandConstant.WEIGHT_JOBMAP_IMAGE,
					CommandConstant.ACTION_JOBMAP_IMAGE,
					true,
					HinemosModuleConstant.JOBMAP_IMAGE_FILE);
			funcTreeChild.setData(info);
			funcJobmap.addChildren(funcTreeChild);
			
			// ジョブマップここまで
		}

		if(options.contains(OptionUtil.TYPE_XCLOUD)){
			// Cloud
			FuncTreeItem funcCloud = new FuncTreeItem();
			info = new FuncInfo("",
					HinemosModuleConstant.STRING_CLOUD,
					"",
					CommandConstant.WEIGHT_OTHER,
					"",
					false,
					"");
			funcCloud.setData(info);
			funcTree.addChildren(funcCloud);

			// CLOUDここから
			// CLOUD ログインユーザ
			funcTreeChild = new FuncTreeItem();
			info = new FuncInfo(HinemosModuleConstant.CLOUD_USER,
					HinemosModuleConstant.STRING_CLOUD_USER,
					XMLConstant.DEFAULT_XML_CLOUD_USER,
					CommandConstant.WEIGHT_CLOUD_USER,
					CommandConstant.ACTION_CLOUD_USER,
					true,
					"");
			funcTreeChild.setData(info);
			funcCloud.addChildren(funcTreeChild);
			
			// CLOUD サービス監視
			funcTreeChild = new FuncTreeItem();
			info = new FuncInfo(HinemosModuleConstant.CLOUD_MONITOR_SERVICE,
					HinemosModuleConstant.STRING_CLOUD_MONITOR_SERVICE,
					XMLConstant.DEFAULT_XML_CLOUD_MON_SERVICE,
					CommandConstant.WEIGHT_CLOUD_MON_SERVICE,
					CommandConstant.ACTION_CLOUD_MON_SERVICE,
					true,
					com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
			funcTreeChild.setData(info);
			funcCloud.addChildren(funcTreeChild);
			
			// CLOUD 課金監視
			funcTreeChild = new FuncTreeItem();
			info = new FuncInfo(HinemosModuleConstant.CLOUD_MONITOR_BILLING,
					HinemosModuleConstant.STRING_CLOUD_MONITOR_BILLING,
					XMLConstant.DEFAULT_XML_CLOUD_MON_BILLING,
					CommandConstant.WEIGHT_CLOUD_MON_BILLING,
					CommandConstant.ACTION_CLOUD_MON_BILLING,
					true,
					com.clustercontrol.bean.HinemosModuleConstant.MONITOR);
			funcTreeChild.setData(info);
			funcCloud.addChildren(funcTreeChild);
		}

		return funcTree;
	}

	public static FuncTreeItem getFuncTreeObjectPrivilege() {
		return funcTreeObjectPrivilege;
	}
}