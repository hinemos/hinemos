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
	public static final int INDEX_EXECUTE_TIME = 12;
	
	/** Quartzからコールバックされるメソッドの引数の数
	 * 
	 * @see com.clustercontrol.jobmanagement.ejb.session.JobControllerBean#scheduleRunJob()
	 */
	public static final int ARGS_NUM = 13;


	/**
	 *  監視ジョブ用の設定
	 */
	/** グループ名 */
	public static final String GROUP_NAME_FOR_MONITORJOB = "MONITORJOB";
	/** メソッド名（監視） */
	public static final String METHOD_NAME_FOR_MONITORJOB = "runMonitorJob";

	/**
	 *  ジョブ連携待機ジョブ用の設定
	 */
	/** グループ名 */
	public static final String GROUP_NAME_FOR_JOBLINKRCVJOB = "JOBLINKRCVJOB";
	/** メソッド名（ジョブ連携待機ジョブ） */
	public static final String METHOD_NAME_FOR_JOBLINKRCVJOB = "runJobLinkRcvJob";

	/**
	 *  ジョブセッション事前生成用の設定
	 */
	/** グループ名 */
	public static final String GROUP_NAME_FOR_JOBPREMAKE = "JOBPREMAKE";

	/** メソッド名 */
	public static final String METHOD_NAME_FOR_JOBPREMAKE = "schedulePremakeJobSession";

	/** メソッド名(一度のみ実行) */
	public static final String METHOD_NAME_FOR_JOBPREMAKE_ONCE = "schedulePremakeJobSessionOnce";

	/** Quartzからコールバックされるメソッドの引数として、ジョブセッション事前生成情報を指定する位置 */
	public static final int INDEX_JOBKICK_ID_FOR_JOBPREMAKE = 0;
	public static final int INDEX_TRIGGER_TYPE_FOR_JOBPREMAKE = 1;
	public static final int INDEX_TRIGGER_INFO_FOR_JOBPREMAKE = 2;
	public static final int INDEX_PREMAKE_ELAPSED_TIME_FOR_JOBPREMAKE = 3;
	public static final int INDEX_EXECUTE_TIME_FOR_JOBPREMAKE = 4;

	/** Quartzからコールバックされるメソッドの引数の数 */
	public static final int ARGS_NUM_FOR_JOBPREMAKE = 5;

	/**
	 *  ジョブ連携受信実行契機用の設定
	 */
	/** グループ名 */
	public static final String GROUP_NAME_FOR_JOBLINKRCV = "JOBLINKRCV";
	/** メソッド名 */
	public static final String METHOD_NAME_FOR_JOBLINKRCV = "scheduleJobLinkRcv";
	/** 引数 */
	public static final int INDEX_JOBKICK_ID_FOR_JOBLINKRCV = 0;
	public static final int INDEX_EXECUTE_TIME_FOR_JOBLINKRCV = 1;
	public static final int INDEX_PREVIOUS_FIRE_TIME_FOR_JOBLINKRCV = 2;
	
	/** Quartzからコールバックされるメソッドの引数の数 */
	public static final int ARGS_NUM_FOR_JOBLINKRCV = 3;

}
