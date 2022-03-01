/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform;

import java.io.File;

import com.clustercontrol.commons.bean.HinemosPropertyBean;
import com.clustercontrol.commons.util.HinemosPropertyAbstract;

/**
 * 環境差分のあるHinemosPropertyのデフォルト値を格納するクラス（Windows）<BR>
 *
 * @version 6.1.0
 * @since 6.0.0
 */
public enum HinemosPropertyDefault implements HinemosPropertyAbstract {
	home_dir(HinemosPropertyBean.string(System.getProperty("hinemos.manager.home.dir"))),
	// Windows only.
	data_dir(HinemosPropertyBean.string(System.getProperty("hinemos.manager.data.dir"))),
	// Windows only.
	user_home_dir(HinemosPropertyBean.string(System.getProperty("user.home"))),
	cloud_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/export/")),
	binary_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/export/")),
	infra_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "\\export\\")),
	infra_transfer_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/infra/")),
	internal_command_commandline(HinemosPropertyBean.string("cmd /c echo #[GENERATION_DATE] #[MESSAGE] >> C:\\temp\\test.txt")),
	internal_command_user(HinemosPropertyBean.string("")),
	monitor_event_customcmd_common_stdout_encode(HinemosPropertyBean.string("MS932")),
	monitor_ping_fping_path(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/sbin/fping.ps1")),
	performance_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/export/")),
	node_config_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/export/")),
	jobmap_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/export/")),
	job_rpa_screenshot_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/export/")),
	reporting_create_process_multiplicity_limit(HinemosPropertyBean.numeric(0L)),
	reporting_create_timeout(HinemosPropertyBean.numeric(1800L)),
	reporting_filename(HinemosPropertyBean.string("hinemos_report")),
	reporting_heap_size(HinemosPropertyBean.string("-Xms256m -Xmx256m -Xss256k")),
	reporting_output_path(HinemosPropertyBean.string(System.getProperty("hinemos.manager.data.dir") + File.separator + "report")),
	selfcheck_monitoring_db_validationquery(HinemosPropertyBean.string("SELECT 1")),
	ws_https_keystore_path(HinemosPropertyBean.string(user_home_dir.getBean().getDefaultStringValue() + "/keystore")),
	notify_command_charset(HinemosPropertyBean.string("MS932")),
	performance_export_manager_encode(HinemosPropertyBean.string("MS932")),
	// Windows only.
	windows_eventlog(HinemosPropertyBean.bool(true)),
	nodemap_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/export/")),
	rpa_scenario_operation_result_export_dir(HinemosPropertyBean.string(data_dir.getBean().getDefaultStringValue() + "/export/")),
	job_rpa_login_file_dir(HinemosPropertyBean.string(System.getProperty("hinemos.manager.data.dir") + File.separator + "rpa")),
	job_rpa_login_end_file_check_interval(HinemosPropertyBean.numeric(10000L));
	

	// Hinemosプロパティ情報
	private final HinemosPropertyBean bean;

	/**
	 * コンストラクタ
	 * @param bean Hinemosプロパティ情報
	 */
	private HinemosPropertyDefault(HinemosPropertyBean bean) {
		this.bean = bean;
	}

	/**
	 * Hinemosプロパティ情報取得
	 * 
	 * @return Hinemosプロパティ情報
	 */
	@Override
	public HinemosPropertyBean getBean() {
		return this.bean;
	}
}