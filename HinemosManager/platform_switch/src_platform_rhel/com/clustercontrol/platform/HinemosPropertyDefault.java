/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform;

import com.clustercontrol.commons.bean.HinemosPropertyBean;
import com.clustercontrol.commons.util.HinemosPropertyAbstract;

/**
 * 環境差分のあるHinemosPropertyのデフォルト値を格納するクラス（rhel）<BR>
 *
 * @version 6.1.0
 * @since 6.0.0
 */
public enum HinemosPropertyDefault implements HinemosPropertyAbstract {
	home_dir(HinemosPropertyBean.string(System.getProperty("hinemos.manager.home.dir"))),
	// Windows only.
	data_dir(HinemosPropertyBean.string()),
	// Windows only.
	user_home_dir(HinemosPropertyBean.string()),
	cloud_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	binary_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	infra_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	infra_transfer_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/infra/")),
	internal_command_commandline(HinemosPropertyBean.string("echo #[GENERATION_DATE] #[MESSAGE] >> /tmp/test.txt")),
	internal_command_user(HinemosPropertyBean.string("root")),
	monitor_event_customcmd_common_stdout_encode(HinemosPropertyBean.string("UTF-8")),
	monitor_ping_fping_path(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/sbin/fping")),
	performance_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	node_config_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	jobmap_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	job_rpa_screenshot_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	reporting_create_process_multiplicity_limit(HinemosPropertyBean.numeric(0L)),
	reporting_create_timeout(HinemosPropertyBean.numeric(1800L)),
	reporting_filename(HinemosPropertyBean.string("hinemos_report")),
	reporting_heap_size(HinemosPropertyBean.string("-Xms256m -Xmx256m -Xss256k")),
	reporting_output_path(HinemosPropertyBean.string(System.getProperty("hinemos.manager.home.dir", "/opt/hinemos") + "/var/report")),
	selfcheck_monitoring_filesystem_usage_list(HinemosPropertyBean.string("/:50")),
	ws_https_keystore_path(HinemosPropertyBean.string("/root/keystore")),
	notify_command_charset(HinemosPropertyBean.string("UTF-8")),
	performance_export_manager_encode(HinemosPropertyBean.string("MS932")),
	rpa_scenario_operation_result_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	// Windows only.
	windows_eventlog(HinemosPropertyBean.bool(false)),
	nodemap_export_dir(HinemosPropertyBean.string(home_dir.getBean().getDefaultStringValue() + "/var/export/")),
	// Linux only
	job_rpa_login_command(HinemosPropertyBean.string("xfreerdp /v:%s /u:%s /p:'%s' /w:%d /h:%d /cert-tofu")), // 初回の証明書の警告で停止するのを防ぐため/cert-tofuを付ける
	job_rpa_login_command_user(HinemosPropertyBean.string("root")),
	job_rpa_login_command_home_dir(HinemosPropertyBean.string("/root")),
	job_rpa_login_display(HinemosPropertyBean.string(":99"));

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