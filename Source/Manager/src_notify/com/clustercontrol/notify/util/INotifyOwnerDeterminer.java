/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

/**
 * 監視やジョブの結果の通知とは異なり、設定に紐付かないタイプ（オプションの操作ログなど）の
 * イベント・ステータス通知におけるオーナを決定するためのインターフェース。<br>
 * 独自のイベント・ステータスを出力するオプションなどでは、本インターフェースの実装クラスを作成し、
 * 最終的に出力したいイベント・ステータスのPluginIDをキーとして、ObjectSharingServiceにそのクラスを登録する。
 * これにより、イベント・ステータス通知時に自動的に本インターフェースに定義されたメソッドがコールバックされ、
 * オーナロールが決定されることとなる。
 * 詳細は、  com.clustercontrol.notify.monitor.util.OwnerDeteminDispatcher を参照のこと。
 */
public interface INotifyOwnerDeterminer {

	/**
	 * 設定などに紐付かないイベント通知のオーナロールを決定する<br>
	 * 注1 : 致命的なものを除き、基本的に本関数では例外を返さないこと<br>
	 * 注2 : 戻り値がnull or 長さが0の場合、処理元ではオーナロールにINTERNALを割り当てる
	 * @param monitorId
	 * @param monitorDetailId
	 * @param pluginId
	 * @param facilityId
	 * @return　オーナロールの文字列
	 */
	public String getEventOwnerRoleId(String monitorId, String monitorDetailId, String pluginId, String facilityId);

	/**
	 * 設定などに紐付かないステータス通知のオーナロールを決定する<br>
	 * 注1 : 致命的なものを除き、基本的に本関数では例外を返さないこと<br>
	 * 注2 : 戻り値がnull or 長さが0の場合、処理元ではオーナロールにINTERNALを割り当てる
	 * @param monitorId
	 * @param monitorDetailId
	 * @param pluginId
	 * @param facilityId
	 * @return オーナロールの文字列
	 */
	public String getStatusOwnerRoleId(String monitorId, String monitorDetailId, String pluginId, String facilityId);
}
