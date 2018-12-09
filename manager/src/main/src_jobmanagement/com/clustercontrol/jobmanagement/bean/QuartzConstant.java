/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * Quartz関連の定義を定数として定義するクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class QuartzConstant {

	/**
	 *  ジョブのグループ。<BR>
	 *  ジョブ管理のスケジュール実行を呼び出すQuartzのジョブのグループ名です。
	 */
	public static final String GROUP_NAME = "JOB";

	/**
	 *  Quartzから呼び出すJNDI名。<BR>
	 *  ジョブ管理のスケジュール実行を行う Session Bean のJNDI名です。
	 */
	public static final String JNDI_NAME = "JobController";

	/**
	 *  Quartzから呼び出すメソッド名。<BR>
	 *  ジョブ管理のスケジュール実行を行う Session Bean のメソッド名です。
	 */
	public static final String METHOD_NAME = "scheduleRunJob";

	/**
	 * 有効/無効<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String VALID_KEY = "valid";

	/**
	 * ジョブ名<BR>
	 * QuartzのJobDetailに格納する属性のキー
	 */
	public static final String JOB_NAME_KEY = "jobName";

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



	/** Quartzからコールバックされるメソッドの引数として、ジョブユニットIDを指定する位置 */
	public static final int INDEX_JOBUNIT_ID = 0;

	/** Quartzからコールバックされるメソッドの引数として、ジョブIDを指定する位置 */
	public static final int INDEX_JOB_ID = 1;

	/** Quartzからコールバックされるメソッドの引数として、カレンダIDを指定する位置 */
	public static final int INDEX_CALENDAR_ID = 2;

	/** Quartzからコールバックされるメソッドの引数として、実行契機情報を指定する位置 */
	public static final int INDEX_TRIGGER_TYPE = 3;
	public static final int INDEX_TRIGGER_INFO = 4;
	public static final int INDEX_TRIGGER_FILENAME = 5;
	public static final int INDEX_TRIGGER_DIRECTORY = 6;
	public static final int INDEX_TRIGGER_JOB_WAIT_TIME = 7;
	public static final int INDEX_TRIGGER_JOB_WAIT_MINUTE = 8;
	public static final int INDEX_TRIGGER_JOB_COMMAND = 9;
	public static final int INDEX_TRIGGER_JOB_COMMAND_TEXT = 10;
	public static final int INDEX_TRIGGER_JOBKICK_ID = 11;
	
	/** Quartzからコールバックされるメソッドの引数の数
	 * 
	 * @see com.clustercontrol.jobmanagement.ejb.session.JobControllerBean#scheduleRunJob()
	 */
	public static final int ARGS_NUM = 12;


	/**
	 *  監視ジョブ用の設定
	 */
	/** グループ名 */
	public static final String GROUP_NAME_FOR_MONITORJOB = "MONITORJOB";
	/** メソッド名（監視） */
	public static final String METHOD_NAME_FOR_MONITORJOB = "runMonitorJob";
}
