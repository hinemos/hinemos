/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.mbean;

import com.clustercontrol.fault.HinemosUnknown;

public interface ManagerMXBean {

	/**
	 * エージェントの管理情報を文字列で返す。<br/>
	 * @return エージェントの管理情報文字列
	 */
	public String getValidAgentStr();

	/**
	 * 管理しているスケジューラ情報を文字列で返す。
	 * @return スケジューラ情報文字列
	 * @throws HinemosUnknown
	 */
	public String getSchedulerInfoStr() throws HinemosUnknown;

	/**
	 * セルフチェック機能が最後に動作した日時文字列を返す。<br/>
	 * @return セルフチェック機能が最後に動作した日時文字列を
	 */
	public String getSelfCheckLastFireTimeStr();

	/**
	 * syslogの統計情報を返す。<br/>
	 * @return syslogの統計情報
	 */
	public String getSyslogStatistics();

	/**
	 * snmptrapの統計情報を返す。<br/>
	 * @return snmptrapの統計情報
	 */
	public String getSnmpTrapStatistics();

	/**
	 * 非同期タスクの蓄積数を返す。<br/>
	 * @return 非同期タスクの蓄積数
	 * @throws HinemosUnknown
	 */
	public String getAsyncWorkerStatistics() throws HinemosUnknown;

	/**
	 * 通知抑制の履歴情報（最終重要度および通知日時）をリセットする
	 * @return
	 * @throws HinemosUnknown
	 */
	public String resetNotificationLogger() throws HinemosUnknown;

	/**
	 * ジョブ多重度のキューの状態を出力する。
	 * @return
	 */
	public String getJobQueueStr();

	/**
	 * DBコネクションのプール情報を文字列で返す。<br/>
	 * @return DBコネクションのプール情報文字列
	 */
	public String getDBConnectionPoolInfoStr();

	/**
	 * 実行中のジョブセッションレコード数を取得
	 * @return 実行中のジョブセッションレコード数
	 */
	public long getJobRunSessionCount();
	
	/**
	 * snmptrapの処理待ち数を取得
	 * @return snmptrapの処理待ち数
	 */
	public int getSnmpTrapQueueCount();
	
	/**
	 * syslogの処理待ち数を取得
	 * @return syslogの処理待ち数
	 */
	public int getSyslogQueueCount();
	
	/**
	 * WSのQueueサイズを取得
	 * @return WSのQueueサイズ
	 */
	public int getWebServiceQueueCount();
	
	/**
	 * WS(ForAgent)のQueueサイズを取得
	 * @return WSのQueueサイズ
	 */
	public int getWebServiceForAgentQueueCount();
	
	/**
	 * WS(ForAgentHub)のQueueサイズを取得
	 * @return WSのQueueサイズ
	 */
	public int getWebServiceForAgentHubQueueCount();

	/**
	 * WS(ForAgentBinary)のQueueサイズを取得
	 * 
	 * @return WSのQueueサイズ
	 */
	public int getWebServiceForAgentBinaryQueueCount();
	
	/**
	 * テーブルの物理サイズ（Byte）を取得
	 * @return テーブルの物理サイズ（Byte）
	 */
	public TablePhysicalSizes getTablePhysicalSize();
	
	/**
	 * テーブルのレコード数を取得
	 * @param tableName テーブル名
	 * 
	 * @return テーブルのレコード数
	 */
	public long getTableRecordCount(String tableName);

	/**
	 * JPAのキャッシュを全て出力する。
	 * 現状、hinemos_manager.logに出力されてしまいます。
	 */
	public void printJpaCacheAll();

	/**
	 * リポジトリのキャッシュ情報を、hinemos_manager.logに出力する
	 */
	public void printFacilityTreeCacheAll() ;
	/**
	 * リポジトリのキャッシュ情報をリフレッシュする
	 */
	public void refreshFacilityTreeCache();

	/**
	 * データベースのコネクション数を返す</br>
	 * 
	 * @return データベースのコネクション数
	 */
	public int getDBConnectionCount();
	
	/**
	 * 非同期タスクの蓄積数を返す。<br/>
	 * 
	 * @return 非同期タスクの蓄積数
	 * @throws HinemosUnknown
	 */
	public AsyncTaskQueueCounts getAsyncTaskQueueCount() throws HinemosUnknown;

	/**
	 * データベースのロングトランザクションを返す。<br/>
	 * 
	 * @return 最も時間のかかっているクエリーの時間
	 */
	public double getDBLongTransactionTime();

	/**
	 * 利用可能なヒープ容量をMByte単位で取得する。<br/>
	 * 
	 * @return 利用可能なヒープ容量
	 */
	public int getJVMHeapSize();

	/**
	 * スケジューラ情報の遅延時間を返す。<br/>
	 * 
	 * @return 指定したスケジューラ種別の最も遅延している時間
	 * @throws HinemosUnknown
	 */
	public SchedulerDelayTimes getSchedulerDelayTime() throws HinemosUnknown;
	/*
	public void startHinemosSchedulerTest();
	public void startHinemosSchedulerStressTest();
	public void stopHinemosSchedulerTest();
	*/
	
	/**
	 * ノードのキャッシュ情報を初期化する
	 */
	public void initNodeCache();
	
	/**
	 * ジョブのキャッシュ情報を初期化する
	 */
	public void initJobCache();
}