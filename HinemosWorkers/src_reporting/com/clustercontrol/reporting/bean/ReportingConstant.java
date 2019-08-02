/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.bean;

/**
 * レポーティング機能の定数クラス
 * 
 * @version 5.0.c
 * @since 5.0.a
 */
public class ReportingConstant {
	
	// 定数
	public static final String OUTPUT_TYPE_ALL = "ALL";
	public static final String OUTPUT_TYPE_MONTH = "MONTH";
	public static final String OUTPUT_TYPE_WEEK = "WEEK";
	public static final String OUTPUT_TYPE_DAY = "DAY";
	
	public static final int TYPE_PDF = 0;
	public static final int TYPE_XLSX = 1;
	
	public static final int DS_PASS_TYPE_PARAM = 0;
	public static final int DS_PASS_TYPE_DIRECT = 1;
	
	public static final String STR_REP = "reporting";
	
	public static final String STR_DS = "DATASOURCE";
	/*
	 * プロパティキー
	 */
	// 非指定プロパティ
	public static final String JRXML_PATH_KEY_VALUE = "jrxml.path";  // 実際のプロパティでは指定せず、実ファイルを自動で取得
	
	// テンプレート共通プロパティ
	public static final String CHART_NUM_KEY_VALUE = "chart.num";
	public static final String INDEX_FLG_KEY_VALUE = "index.flg";
	public static final String TEMPLATE_CLASS_KEY_VALUE = "template.class";
	public static final String TEMPLATE_NAME_KEY_VALUE = "template.name";
	public static final String TITLE_MAIN_KEY_VALUE = "title.main";
	public static final String TITLE_COVER_KEY_VALUE = "title.cover";
	public static final String DATE_FORMAT_KEY_VALUE = "date.format";
	public static final String OUTPUT_PERIOD_TYPE_KEY_VALUE = "output.period.type";
	public static final String DATASOURCE_PASS_TYPE_KEY_VALUE = "datasource.pass.type";
	public static final String NODE_DEDUPLICATION = "node.deduplication.flg";
	public static final String NODE_SORT_KEY_VALUE = "node.sort";
	public static final String TYPE_FACILITY_NAME = "facility_name";
	public static final String TYPE_FACILITY_ID = "facility_id";
	public static final String NODE_SORT_DEFAULT = TYPE_FACILITY_ID;

	// グラフ表示系テンプレートプロパティ
	public static final String DATASOURCE_CLASS_KEY_VALUE = "datasource.class";
	
	// ジョブの実行状態
	public static final String STATUS_NORMAL_KEY_VALUE = "status.normal";
	public static final String STATUS_WARNING_KEY_VALUE = "status.warning";
	public static final String STATUS_ERROR_KEY_VALUE = "status.error";
	
	// 重要度
	public static final String CRIT_STR_KEY_VALUE = "priority.crit";
	public static final String WARN_STR_KEY_VALUE = "priority.warn";
	public static final String INFO_STR_KEY_VALUE = "priority.info";
	public static final String UNKNOWN_STR_KEY_VALUE = "priority.unknown";
	
	// 監視・ジョブ共通テンプレートプロパティ
	public static final String TOTAL_KEY_VALUE = "total";
	public static final String ITEM_NAME_FACILITYID = "item.name.facilityid";
	public static final String ITEM_NAME_MSG = "item.name.message";
	public static final String ITEM_NAME_OWNROLEID = "item.name.ownerroleid";
	public static final String ITEM_NAME_SCPTXT = "item.name.scopetext";
	
	// 監視系テンプレートプロパティ
	public static final String ITEM_NAME_APPLI = "item.name.application";
	public static final String ITEM_NAME_COMM = "item.name.comment";
	public static final String ITEM_NAME_COMMDATE = "item.name.commentdate";
	public static final String ITEM_NAME_COMMUSR = "item.name.commentuser";
	public static final String ITEM_NAME_CONFUSR = "item.name.confirmuser";
	public static final String ITEM_NAME_GENEDATE = "item.name.generationdate";
	public static final String ITEM_NAME_MSGID = "item.name.messageid";
	public static final String ITEM_NAME_MONDETID = "item.name.monitordetailid";
	public static final String ITEM_NAME_MONID = "item.name.monitorid";
	public static final String ITEM_NAME_OUTDATE = "item.name.outputdate";
	public static final String ITEM_NAME_PLGID = "item.name.pluginid";
	public static final String ITEM_NAME_PRI = "item.name.priority";

	// ジョブ系テンプレートプロパティ
	public static final String ITEM_NAME_ELPSEDTIME = "item.name.elpsedtime";
	public static final String ITEM_NAME_ENDDATE = "item.name.enddate";
	public static final String ITEM_NAME_ENDSTATUS = "item.name.endstatus";
	public static final String ITEM_NAME_ENDTIME = "item.name.endtime";
	public static final String ITEM_NAME_ENDVALUE = "item.name.endvalue";
	public static final String ITEM_NAME_JOBID = "item.name.jobid";
	public static final String ITEM_NAME_JOBUNITID = "item.name.jobunitid";
	public static final String ITEM_NAME_NODENAME = "item.name.nodename";
	public static final String ITEM_NAME_SCHEDULEDATE = "item.name.scheduledate";
	public static final String ITEM_NAME_SESSIONID = "item.name.sessionid";
	public static final String ITEM_NAME_STARTDATE = "item.name.startdate";
	public static final String ITEM_NAME_STARTJOBID = "item.name.startjobid";
	public static final String ITEM_NAME_STARTTIME = "item.name.starttime";
	public static final String ITEM_NAME_STATUS = "item.name.status";
	public static final String ITEM_NAME_TRIGGERINFO = "item.name.triggerinfo";
	public static final String ITEM_NAME_TRIGGERTYPE = "item.name.triggertype";
	public static final String ITEM_NAME_CONCURRENCY = "item.name.concurrency";
	public static final String TITLE_JOBQUEUE_CONCURRENCY = "title.jobqueue.concurrency";
}
