/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.bean;


/**
 * Quartz関連の定義を定数として格納するクラスです。
 *
 * @version 2.2.0
 * @since 2.2.0
 */
public class QuartzConstant {

	/**
	 *  メンテナンスのグループ。<BR>
	 *  メンテナンス機能のスケジュール実行を呼び出すQuartzのメンテナンスのグループ名です。
	 */
	public static final String GROUP_NAME = "MAINTENANCE";

	/**
	 *  Quartzから呼び出すJNDI名。<BR>
	 *  メンテナンス機能のスケジュール実行を行う Session Bean のJNDI名です。
	 */
	public static final String JNDI_NAME = "MaintenanceController";

	/**
	 *  Quartzから呼び出すメソッド名。<BR>
	 *  メンテナンス機能のスケジュール実行を行う Session Bean のメソッド名です。
	 */
	public static final String METHOD_NAME = "scheduleRunMaintenance";

	/**
	 * 有効/無効<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String VALID_KEY = "valid";

	/**
	 * メンテナンス名<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String JOB_NAME_KEY = "maintenanceName";

	/**
	 * 作成日時<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String CREATE_DATE_KEY = "createDate";

	/**
	 * 最終更新日時<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String UPDATE_DATE_KEY = "updateDate";

	/**
	 * 新規作成ユーザ<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String CREATE_USER_KEY = "createUser";

	/**
	 * 最終更新ユーザ<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String UPDATE_USER_KEY = "updateUser";

	/**
	 * スケジュール<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String SCHEDULE_KEY = "schedule";
}
