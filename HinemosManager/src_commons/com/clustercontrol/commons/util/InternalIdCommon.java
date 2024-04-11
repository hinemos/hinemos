/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.InternalIdAbstract;
import com.clustercontrol.util.MessageConstant;

/**
 * INTERNALイベントの以下の情報を格納するクラス
 * 
 * ※各項目のコメント欄は最新でない可能性があります。参考程度にして、詳細はプロパティファイルを参照してください。
 * ※項目名は[プラグインID]_[監視設定ID(固定"SYS")]_[連番]にすること。
 *   連番は同プラグインID内の最大値にし、間の空いている数値は使用しないこと。
 * ※InternalIdAbstractを実装するクラス間でINTERNAL_IDは重複しない値を設定すること。
 * 
 */
public enum InternalIdCommon implements InternalIdAbstract {
	// Hinemosマネージャが起動しました。 [{0}]
	MNG_SYS_001(PriorityConstant.TYPE_INFO, HinemosModuleConstant.HINEMOS_MANAGER_MONITOR, MessageConstant.MESSAGE_SYS_001_MNG),
	// Hinemosマネージャが停止しました。 [{0}]
	MNG_SYS_002(PriorityConstant.TYPE_INFO, HinemosModuleConstant.HINEMOS_MANAGER_MONITOR, MessageConstant.MESSAGE_SYS_002_MNG),
	// データベースの更新エラーが発生しました。詳しい情報に関しては、お手数ですが、ご契約のサポート窓口へお問い合わせください。
	MNG_SYS_024(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.HINEMOS_MANAGER_MONITOR, MessageConstant.MESSAGE_SYS_024_MNG),
	// データベースの更新エラーが発生しました。既にその情報は登録されています。
	MNG_SYS_025(PriorityConstant.TYPE_INFO, HinemosModuleConstant.HINEMOS_MANAGER_MONITOR, MessageConstant.MESSAGE_SYS_025_MNG),

	// データベースが利用できません。定常的に出力される場合、同時に実行される処理量（監視など）が過多であるため、設定量を削減してください。
	SYS_SFC_SYS_001(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_001_SYS_SFC),
	// ファイルシステム（{0}）の使用量（{1} [%]）が閾値（{2} [%]）を超えました。メンテナンス運用（ログファイル除去、メンテナンス機能およびスクリプト）を実施し、不必要となった情報を取り除いてください。
	SYS_SFC_SYS_002(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_002_SYS_SFC),
	// 利用可能なメモリ容量（{0}[MByte]）が閾値（{1} [MByte]）を下回りました。定常的に出力される場合、Hinemosマネージャの再起動を推奨します。また、登録サーバ数や各処理の設定量（監視など）が過剰なため、設定量を削減してください。
	SYS_SFC_SYS_003(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_003_SYS_SFC),
	// スケジューラ（{0}:{1}:{2} - 次回実行予定 {3}）に遅延（{4} [sec]以上）が発生しています。連続して出力される場合、定期的に実行される各処理（監視など）が正しく動作していない可能性があるため、Hinemosマネージャの再起動を推奨します。
	SYS_SFC_SYS_004(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_004_SYS_SFC),
	// メモリのスワップアウト（{0} [Blocks]）が発生しました。サーバのリソース状況を確認し、Hinemosマネージャのパフォーマンスをご確認ください。
	SYS_SFC_SYS_005(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_005_SYS_SFC),
	// 内部DB内のテーブル（{0}）が非常に多く蓄積（{1} [mbyte], {2} [rows] > 閾値 {3} {4}）されています。パフォーマンス低下の要因となるため、メンテナンス運用（メンテナンス機能およびスクリプト）を実施してください。
	SYS_SFC_SYS_006(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_006_SYS_SFC),
	// 実行中のジョブセッション数が多く({0} > 閾値 {1})存在します。パフォーマンス低下の要因となるため、不要に実行中のままとなっているジョブセッションを停止(「変更済み」または「終了」)させてください。
	SYS_SFC_SYS_007(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_007_SYS_SFC),
	// Hinemosマネージャ(tcp:{0})に対するリクエストが多く(処理待ちのリクエスト数 {1} > 閾値 {2})、処理遅延が生じています。定常的に出力される場合、同時に実行される処理量（監視など）が過多であるため、設定量を削減してください。
	SYS_SFC_SYS_008(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_008_SYS_SFC),
	// Hinemosマネージャに送信されたsyslogが多く(処理待ちのsyslog数 {0} > 閾値 {1})、処理遅延が生じています。監視対象から大量のsyslogが送信されていないかを確認してください。
	SYS_SFC_SYS_009(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_009_SYS_SFC),
	// Hinemosマネージャに送信されたsnmptrapが多く(処理待ちのsnmptrap数 {0} > 閾値 {1})、処理遅延が生じています。監視対象から大量のsnmptrapが送信されていないかを確認してください。
	SYS_SFC_SYS_010(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_010_SYS_SFC),
	// 非同期処理の蓄積量が多く(処理待ちの非同期処理数 {0} > 閾値 {1})、処理遅延が生じています。通知やジョブが非常に多く実行されていないかをご確認ください。
	SYS_SFC_SYS_011(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_011_SYS_SFC),
	// 内部ロジック(スレッドID {0}, スレッド名 {1}, クラス名 {2}, 開始日時 {3})で{4} [sec]以上の時間を要しています。
	SYS_SFC_SYS_012(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_012_SYS_SFC),
	// データベーストランザクションが長い間完了していないプロセスが存在します。パフォーマンス低下の要因となるため、Hinemosマネージャの再起動を推奨します。
	SYS_SFC_SYS_013(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_013_SYS_SFC),
	// 評価版のHinemosを利用中です。利用期限：{0}、評価版キーファイル名：{1}
	SYS_SFC_SYS_014(PriorityConstant.TYPE_INFO, HinemosModuleConstant.SYSYTEM_SELFCHECK,  MessageConstant.MESSAGE_SYS_014_SYS_SFC),
	// 評価版のクラウド管理・VM管理機能を利用中です。利用期限：{0}、評価版キーファイル名：{1}
	SYS_SFC_SYS_015(PriorityConstant.TYPE_INFO, HinemosModuleConstant.SYSYTEM_SELFCHECK,  MessageConstant.MESSAGE_SYS_015_SYS_SFC),
	// 評価版のHinemosが利用期限切れになっています。Hinemosマネージャを再起動するとHinemosが使用できなくなります。評価版キーファイル名：{0}
	SYS_SFC_SYS_016(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK,  MessageConstant.MESSAGE_SYS_016_SYS_SFC),
	// 評価版のクラウド管理・VM管理機能が利用期限切れになっています。Hinemosマネージャを再起動すると、クラウド管理・VM管理機能が使用できなくなります。評価版キーファイル名：{0}
	SYS_SFC_SYS_017(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK,  MessageConstant.MESSAGE_SYS_017_SYS_SFC),
	// Windows版マネージャではスワップアウト発生のセルフチェックを利用できません。
	SYS_SFC_SYS_018(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_018_SYS_SFC),
	// データベース接続数が多く({0} > 閾値 {1})存在します。パフォーマンス低下の要因となるため、Hinemosマネージャの再起動を推奨します。
	SYS_SFC_SYS_021(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_021_SYS_SFC),
	// 同時実行制御キュー({0})のサイズが警告閾値({1})を超えました。
	SYS_SFC_SYS_024(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_JOBQUEUE_SIZE),
	// 同時実行制御キュー({0})のジョブ実行状況が {1} [sec]以上変化していません。
	SYS_SFC_SYS_025(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SYS_JOBQUEUE_DEADLOCK),
	//スケジューラ（{0}:{1}:{2} - 次回実行予定 {3}）の実行予定時刻からの経過時間が起動失敗と判定する閾値を超えたため、実行されませんでした。
	SYS_SFC_SYS_026(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.SYSYTEM_SELFCHECK, MessageConstant.MESSAGE_SCHEDULER_SKIP_BY_DELAY),

	// 通知の取得に失敗しました。(NotifyId\={0})
	PLT_NTF_SYS_004(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_004_NOTIFY),
	// 通知一覧の取得に失敗しました。
	PLT_NTF_SYS_006(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_006_NOTIFY),
	// 通知に失敗しました。(NotifyId\={0})
	PLT_NTF_SYS_007(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_007_NOTIFY),
	// 通知に失敗しました。ジョブ通知の通知先のジョブ定義が存在しません。(NotifyId\={0},MonitorId\={1},JobunitId\={2},JobunitId\={3})
	PLT_NTF_SYS_008(null, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_008_NOTIFY),
	// 通知に失敗しました。環境構築通知の通知先の環境構築設定が存在しません。(NotifyId\={0},MonitorId\={1},InfraManagementId\={2})
	PLT_NTF_SYS_009(null, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_009_NOTIFY),
	// 通知に失敗しました。(NotifyId\={0})
	PLT_NTF_SYS_010(null, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_010_NOTIFY),
	// 利用可能なすべてのSMTPサーバで送信に失敗しました。(NotifyId\={0})
	PLT_NTF_SYS_011(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_ALL_FAILURE),
	// {0} から停止していたSMTPサーバ {1} への送信が復旧しました。
	PLT_NTF_SYS_012(PriorityConstant.TYPE_INFO, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_RESTART),
	// 監視の通知メールの送信に失敗しました。(NotifyID={0}, FacilityID={1}, MonitorID={2})
	PLT_NTF_SYS_013(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_MONITOR_FAILURE),
	// ジョブの通知メールの送信に失敗しました。(NotifyID={0}, SessionID={1}, JobUnitID={2}, JobID={3})
	PLT_NTF_SYS_014(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_JOB_FAILURE),
	// メンテナンスの通知メールの送信に失敗しました。(NotifyID={0}, MaintenanceID={1})
	PLT_NTF_SYS_015(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_MAINTENANCE_FAILURE),
	// 環境構築の通知メールの送信に失敗しました。(NotifyID={0}, FacilityID={1}, InfraManagementID={2})
	PLT_NTF_SYS_016(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_INFRA_FAILURE),
	// 構成情報取得の通知メールの送信に失敗しました。(NotifyID={0}, FacilityID={1}, SettingID={2})
	PLT_NTF_SYS_017(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_NODE_CONFIG_SETTING_FAILURE),
	// レポーティング通知メールの送信に失敗しました。(NotifyID={0}, ScheduleID={1})
	PLT_NTF_SYS_018(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_REPORTING_FAILURE),
	// ジョブ連携メッセージの送信に成功しました(通知ID={0},ジョブ連携メッセージID={1},ファシリティID={2},送信日時={3})
	PLT_NTF_SYS_019(PriorityConstant.TYPE_INFO, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_019_NOTIFY),
	// ジョブ連携メッセージの送信に失敗しました(通知ID={0},ジョブ連携メッセージID={1},ファシリティID={2},送信日時={3})
	PLT_NTF_SYS_020(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_020_NOTIFY),
	// RESTアクセス設定の取得に失敗しました。(NotifyId\={0},RestAccessId\={1})
	PLT_NTF_SYS_021(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_026_NOTIFY),
	// 通知に失敗しました。指定されたURLへのHTTPアクセスが異常終了しました。(NotifyId\={0},RestAccessId\={1},url\={2})
	PLT_NTF_SYS_022(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_027_NOTIFY),
	// 通知に失敗しました。設定の内容に問題があります。(NotifyId\={0},RestAccessId\={1})
	PLT_NTF_SYS_023(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_028_NOTIFY),

	/**
	 * クラウド通知の通知失敗メッセージ
	 */
	// 監視のクラウド通知に失敗しました。(NotifyID={0}, FacilityID={1}, MonitorID={2})
	PLT_NTF_SYS_024(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_MONITOR_FAILURE),
	// ジョブのクラウド通知に失敗しました。(NotifyID={0}, SessionID={1}, JobUnitID={2}, JobID={3})
	PLT_NTF_SYS_025(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_JOB_FAILURE),
	// メンテナンスのクラウド通知に失敗しました。(NotifyID={0}, MaintenanceID={1})
	PLT_NTF_SYS_026(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_MAINTENANCE_FAILURE),
	// 環境構築のクラウド通知に失敗しました。(NotifyID={0}, FacilityID={1}, InfraManagementID={2})
	PLT_NTF_SYS_027(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_INFRA_FAILURE),
	// 構成情報取得のクラウド通知に失敗しました。(NotifyID={0}, FacilityID={1}, SettingID={2})
	PLT_NTF_SYS_028(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_NODE_CONFIG_SETTING_FAILURE),
	// レポーティングのクラウド通知に失敗しました。(NotifyID={0}, ScheduleID={1})
	PLT_NTF_SYS_029(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_REPORTING_FAILURE),
	// 送信処理待ちのクラウド通知の量が閾値を超えたため、閾値量を下回るまで以降のクラウド通知は破棄されます。(閾値={0})
	PLT_NTF_SYS_030(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_MSG_DROP_START),
	// 送信処理待ちのクラウド通知の量が閾値以下になったため、クラウド通知の受付を再開します。(閾値={0})
	PLT_NTF_SYS_031(PriorityConstant.TYPE_INFO, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_MSG_DROP_END),

	// SDMLの通知メールの送信に失敗しました。(NotifyID={0}, ApplicationID={1})
	PLT_NTF_SYS_032(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_MAIL_SEND_SDML_FAILURE),
	// SDMLのクラウド通知に失敗しました。(NotifyID={0}, ScheduleID={1})
	PLT_NTF_SYS_033(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_SDML_FAILURE),

	// RPAシナリオ実績作成のクラウド通知に失敗しました。(NotifyID={0}, CreateSettingID={1})
	PLT_NTF_SYS_034(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_RPA_SCENARIO_CREATE_FAILURE),
	// RPAシナリオ実績更新のクラウド通知に失敗しました。(NotifyID={0}, createSettingID={1}, scenarioIdentifyString={2})
	PLT_NTF_SYS_035(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_CLOUD_NOTIFY_RPA_SCENARIO_UPDATE_FAILURE),

	// メールテンプレートの取得に失敗しました。(MailTemplateID\={0})
	PLT_MIL_TMP_SYS_004(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE, MessageConstant.MESSAGE_SYS_004_MAILTEMP),

	// ソースIP[{0}]に対して有効な自動登録の設定が存在しません。手動で登録を行うか、自動登録の設定を見直してください。
	PLT_REP_AREG_SYS_001(PriorityConstant.TYPE_INFO, HinemosModuleConstant.PLATFORM_REPSITORY_AUTO_REGISTER, MessageConstant.MESSAGE_AUTO_REGISTER_NODE_NONE),
	// ノード自動登録に失敗しました。({0}\:{1})
	PLT_REP_AREG_SYS_002(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_REPSITORY_AUTO_REGISTER, MessageConstant.MESSAGE_AUTO_REGISTER_NODE_FAILED),

	// repository.autoassign.scopeidsに存在しないスコープID({0})が指定されています。(ファシリティID: {1})
	AGENT_SYS_001(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_REPSITORY_SCOPE, MessageConstant.MESSAGE_AGENT_AUTO_ASSIGN_TO_INVALID_SCOPE),
	
	// スコープ情報の取得に失敗しました。 (FacilityId\={0})
	MON_SYS_001(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_001_MON),
	// 監視情報の取得に失敗しました。 (MonitorTypeId\={0}, MonitorId\={1})
	MON_SYS_010(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_010_MON),
	// 監視情報一覧の取得に失敗しました。
	MON_SYS_011(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_011_MON),
	// 監視の実行に失敗しました。 (MonitorTypeId\={0}, MonitorId\={1})
	MON_SYS_012(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_012_MON),
	// 監視の実行に失敗しました。 (MonitorTypeId\={0}, FacilityId\={1})
	MON_SYS_013(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_013_MON),

	// Fpingが応答を返しませんでした。 (監視項目ID\={0})
	MON_PNG_N_SYS_001(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.MONITOR_PING, MessageConstant.MESSAGE_SYS_001_MON_PNG),

	// SNMP TRAP の通知に失敗しました。 (trapOid\={0}, genericId\={1}, specificId\={2})
	MON_SNMP_TRP_SYS_009(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.MONITOR_SNMPTRAP, MessageConstant.MESSAGE_SYS_009_TRAP),

	// 差分値を計算する為のキャッシュ数が最大値を超えました。（件数={0},最大数={1}）
	MON_CUSTOMTRAP_N_SYS_022(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.MONITOR_CUSTOMTRAP_N, MessageConstant.MESSAGE_SYS_022_CUSTOM_TRAP_NUM_OVER),

	// ログ件数収集を正常終了しました。 (監視項目ID\={0})
	MON_LOGCOUNT_N_SYS_002(PriorityConstant.TYPE_INFO, HinemosModuleConstant.MONITOR_LOGCOUNT, MessageConstant.MESSAGE_SYS_002_MON_LOGCOUNT),
	// ログ件数収集を異常終了しました。 (監視項目ID\={0})
	MON_LOGCOUNT_N_SYS_003(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.MONITOR_LOGCOUNT, MessageConstant.MESSAGE_SYS_003_MON_LOGCOUNT),

	// {0}が異常終了しました。実行ユーザ={1} 起動日時={2} 結果は監視[イベントの詳細]ダイアログのイベント操作履歴で確認してください。
	MON_EVT_SYS_001(PriorityConstant.TYPE_INFO, HinemosModuleConstant.MONITOR_EVENT, MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_FAILURE_NOTIFY),
	
	//{0}[{1}]でクラウドログ監視[{2}]が開始されました
	MON_CLOUDLOG_S_SYS_001(PriorityConstant.TYPE_INFO, HinemosModuleConstant.MONITOR_CLOUD_LOG, MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_STARTED),
	//クラウドログ監視[{0}]の実行エージェントが変更されました。\n[変更前:{1}[{2}] 変更後:{3}[{4}]
	MON_CLOUDLOG_S_SYS_002(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR_CLOUD_LOG, MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_AGENT_SCOPE_CHANGED),
	
	// JMX監視スレッド数が上限に達しました。（現在値={0}, 上限値={1}）
	MON_JMX_N_SYS_001(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.MONITOR_JMX, MessageConstant.MESSAGE_JMX_THREADS_COUNT_EXCEEDS_MAX),

	// JMX監視スレッド数が上限の{2}%に達しました。（現在値={0}, 上限値={1}）
	MON_JMX_N_SYS_002(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR_JMX, MessageConstant.MESSAGE_JMX_THREADS_COUNT_EXCEEDS_WARN_THRESHOLD),

	// 自動採番の文字列データIDが最大値の半分まで達しました。
	HUB_TRF_SYS_001(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.HUB_TRANSFER, MessageConstant.MESSAGE_HUB_COLLECT_NUMBERING_OVER_INTERMEDIATE),
	// データの転送に失敗しました。(転送設定ID={0})
	HUB_TRF_SYS_002(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.HUB_TRANSFER, MessageConstant.MESSAGE_HUB_DATA_TRANSFER_FAILED),

	// ジョブの履歴情報の作成に失敗しました。(JobID\={0})
	JOB_SYS_003(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_003_JOB),
	// ジョブの開始[即時]に失敗しました。(SessionID\={0}, JobID\={1}, FacilityID\={2})
	JOB_SYS_007(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_007_JOB),
	// ジョブの開始[中断解除]に失敗しました。(SessionID\={0}, JobID\={1})
	JOB_SYS_008(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_008_JOB),
	// ジョブの開始[保留解除]に失敗しました。(SessionID\={0}, JobID\={1})
	JOB_SYS_009(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_009_JOB),
	// ジョブの開始[スキップ解除]に失敗しました。(SessionID\={0}, JobID\={1})
	JOB_SYS_010(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_010_JOB),
	// ジョブの停止[コマンド]に失敗しました。(SessionID\={0}, JobID\={1}, FacilityID\={2})
	JOB_SYS_011(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_011_JOB),
	// ジョブの停止[中断]に失敗しました。(SessionID\={0}, JobID\={1})
	JOB_SYS_012(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_012_JOB),
	// ジョブの停止[保留]に失敗しました。(SessionID\={0}, JobID\={1})
	JOB_SYS_013(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_013_JOB),
	// ジョブの停止[スキップ]に失敗しました。(SessionID\={0}, JobID\={1})
	JOB_SYS_014(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_014_JOB),
	// ジョブの停止[状態変更]に失敗しました。(SessionID\={0}, JobID\={1}, FacilityID\={2})
	JOB_SYS_015(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_015_JOB),
	// ジョブのスケジュール実行に失敗しました。(JobID\={0}, ScheduleInfo\={1})
	JOB_SYS_016(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_016_JOB),
	// ジョブのファイルチェック実行に失敗しました。(JobID\={0}, ScheduleInfo\={1})
	JOB_SYS_017(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_017_JOB),
	// ジョブの停止[強制停止]に失敗しました。(SessionID\={0}, JobID\={1}, FacilityID\={2})
	JOB_SYS_018(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_018_JOB),
	// 承認要求結果メールの送信先アドレスが設定されていません。(SessionID\={0}, JobID\={1})
	JOB_SYS_019(PriorityConstant.TYPE_INFO, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_019_JOB),
	// 承認要求/結果メールの送信に失敗しました。(SessionID\={0}, JobID\={1})
	JOB_SYS_020(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_020_JOB),
	// 事前生成ジョブセッション削除を正常終了しました。 (実行契機ID\={0})
	JOB_SYS_022(PriorityConstant.TYPE_INFO, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_022_JOB),
	// 事前生成ジョブセッション削除を異常終了しました。 (実行契機ID\={0})
	JOB_SYS_023(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_023_JOB),
	// ジョブセッションの事前生成が完了しました。(実行契機ID\={0}, 作成したセッションの親ジョブID\={1}({2}))
	JOB_SYS_024(PriorityConstant.TYPE_INFO, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_024_JOB),
	// ジョブセッションの事前生成に失敗しました。(実行契機ID\={0}, 作成したセッションの親ジョブID\={1}({2}))
	JOB_SYS_025(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_025_JOB),
	// 利用可能なすべてのSMTPサーバで送信に失敗しました。(NotifyId\={0})
	JOB_SYS_026(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_MAIL_SEND_ALL_FAILURE_JOB),
	// {0} から停止していたSMTPサーバ {1} への送信が復旧しました。
	JOB_SYS_027(PriorityConstant.TYPE_INFO, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_MAIL_SEND_RESTART_JOB),
	// ジョブの開始[強制実行]に失敗しました。(SessionID\={0}, JobID\={1})
	JOB_SYS_028(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_JOB_OPERATION_FAILED_FORCE_RUN),
	// ジョブの実行に失敗しました。(JobID\={0})
	JOB_SYS_029(null, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_FAILED_TO_START_JOB),
	// ジョブのジョブ連携受信実行に失敗しました。(JobID\={0}, JobKickInfo\={1})
	JOB_SYS_030(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_030_JOB),
	// 実行対象外のバージョンのエージェントに対してジョブが実行されたため起動に失敗しました。\n\
	// 該当のジョブを実行するには指定バージョン以上のエージェントを利用する必要があります。(セッションID\={0}, ジョブID\={1}, ファシリティID\={2}, 指定バージョン\={3})
	JOB_SYS_031(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_JOB_NOT_SUPPORTED_AGENT_VERSION),
	// ジョブID（jobid）の待ち条件「ジョブ（リターンコード）」に複数ノードが割り当てられたスコープを実行対象とするジョブが設定されています。（対象ジョブ:jobid、セッションID:sessionid)
	// 待ち条件「ジョブ（リターンコード）」は対象ジョブの実行対象がノード、または１ノードのみ割り当てられたスコープの場合のみ動作します
	JOB_SYS_032(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_WAIT_CONDITION_CANNOT_MULTI_NODE),
	// スクリーンショットの取得に失敗しました。(SessionID\={0}, JobID\={1}, FacilityID\={2})
	JOB_SYS_033(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_JOB_SCREENSHOT_FAILED),
	// ジョブ連携メッセージの手動送信に成功しました(ジョブ連携メッセージID={0},ファシリティID={1},送信日時={2})
	JOB_SYS_034(PriorityConstant.TYPE_INFO, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_034_JOB),
	// ジョブ連携メッセージの手動送信に失敗しました(ジョブ連携メッセージID={0},ファシリティID={1},送信日時={2})
	JOB_SYS_035(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_035_JOB),

	// 同時実行制御キューのサイズが上限値を超えたため、ジョブが異常終了しました。(キューID={0},セッションID={1},ジョブID={2})
	JOB_QUEUE_SYS_001(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB_QUEUE, MessageConstant.MESSAGE_JOBQUEUE_EXCEEDED),

	// ファシリティID({0})は自動デバイスサーチが有効化されています。自動デバイスサーチの無効化を推奨します。
	NODE_CONFIG_SETTING_SYS_001(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.NODE_CONFIG_SETTING, MessageConstant.MESSAGE_PLEASE_SET_NODE_CONFIG_AUTO_DEVICE_OFF),
	// 構成情報の取得に失敗しました。
	NODE_CONFIG_SETTING_SYS_002(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.NODE_CONFIG_SETTING, MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_INFO_FAILURE_MSG),
	// Hinemosプロパティの設定が誤っているため、デフォルト値が適用されました。
	NODE_CONFIG_SETTING_SYS_003(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.NODE_CONFIG_SETTING, MessageConstant.MESSAGE_INVALID_PROPERTY),
	// 一定時間が経過したため構成情報の即時取得がキャンセルされました。対象のノードでHinemosエージェントが起動しているかご確認ください。
	NODE_CONFIG_SETTING_SYS_004(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.NODE_CONFIG_SETTING, MessageConstant.MESSAGE_FAILED_TO_GET_NODE_CONFIG_RUN),

	// レポート作成に必要なJarファイルが配置されていません (スケジュールID:{0})
	REPORTING_SYS_001(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.REPORTING, MessageConstant.MESSAGE_REPORTING_JARFILES_NOT_FOUND),
	// 同時実行数の上限を超えているため、レポートの作成が行われませんでした (スケジュールID:{0})
	REPORTING_SYS_002(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.REPORTING, MessageConstant.MESSAGE_REPORTING_MULTIPLICITY_EXCEEDED),

	// Hinemosプロパティ「{0}」の設定値「{1}」に誤りがあります。ロール「{2}」がテナント「{3}」と「{4}」で重複して指定されています。この設定値は無視され、テナントロールが指定されていないものとみなされます。
	MULTI_TENANT_SYS_001(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MULTI_TENANT, MessageConstant.MESSAGE_MULTI_TENANT_INVALID_ROLES_SETTING),
	// Hinemosプロパティ「{0}」の設定値「{1}」に誤りがあります。不正な形式のCIDR「{2}」が含まれています。この設定値は無視され、アドレスグループが指定されていないものとみなされます。
	MULTI_TENANT_SYS_002(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MULTI_TENANT, MessageConstant.MESSAGE_MULTI_TENANT_INVALID_ADDRESS_GROUP_SETTING),
	// Hinemosプロパティ「{0}」の設定値「{1}」に誤りがあります。ファシリティIDに使用できない文字が含まれています。この設定値は無視され、プレフィックスが指定されていないものとみなされます。
	MULTI_TENANT_SYS_003(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MULTI_TENANT, MessageConstant.MESSAGE_MULTI_TENANT_INVALID_FACILITY_ID_SETTING),

	// メンテナンスID[{0}]が終了(終了状態：失敗)しました
	MAINTENANCE_SYS_001(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.SYSYTEM_MAINTENANCE, MessageConstant.MESSAGE_MAINTENANCE_STOPPED_FAILED),
	// メンテナンスID[{0}]がタイムアウト時間 {1} 秒を超過し、一部の履歴が削除されませんでした。(削除対象期間: {2}, 削除済み期間: {3})
	MAINTENANCE_SYS_002(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_MAINTENANCE, MessageConstant.MESSAGE_MAINTENANCE_HISTORY_DELETION_TIMED_OUT),
	// メンテナンスID[{0}]がタイムアウト時間 {1} 秒を超過し、一部の履歴が削除されませんでした。(削除対象履歴: {2}, 削除対象期間: {3}, 削除済み期間: {4})
	MAINTENANCE_SYS_003(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.SYSYTEM_MAINTENANCE, MessageConstant.MESSAGE_MAINTENANCE_NODE_HISTORY_DELETION_TIMED_OUT),

	// コマンド通知テンプレートの取得に失敗しました。(CommandTemplateId\={0})
	PLT_CMD_TMP_SYS_004(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_004_CMDTEMP),
	
	// 自動検知でノードが登録されました。登録件数={0}件
	RPA_SYS_001(PriorityConstant.TYPE_INFO,HinemosModuleConstant.RPA, MessageConstant.MESSAGE_RPA_AUTO_DETECT_ADD_NODE),
	// 自動検知でノードが変更されました。ファシリティID={0}
	RPA_SYS_002(PriorityConstant.TYPE_INFO,HinemosModuleConstant.RPA, MessageConstant.MESSAGE_RPA_AUTO_DETECT_CHANGE_NODE),
	// 自動検知でノードが削除されました。削除件数={0}件
	RPA_SYS_003(PriorityConstant.TYPE_INFO,HinemosModuleConstant.RPA, MessageConstant.MESSAGE_RPA_AUTO_DETECT_DELETE_NODE),
	// RPA管理ツールからの自動検知に失敗しました。RPAスコープID={0}
	RPA_SYS_004(PriorityConstant.TYPE_INFO,HinemosModuleConstant.RPA, MessageConstant.MESSAGE_RPA_AUTO_DETECT_ACCESS_FAILED),
	
	;

	// 重要度
	private Integer priority;
	// プラグインID
	private String pluginId;
	// メッセージ定数
	private MessageConstant messageConstant;

	/**
	 * コンストラクタ
	 * 
	 * @param priority 重要度
	 * @param pluginId プラグインID
	 * @param messageConstant メッセージ定数
	 */
	private InternalIdCommon(Integer priority, String pluginId, MessageConstant messageConstant) {
		this.priority = priority;
		this.pluginId = pluginId;
		this.messageConstant = messageConstant;
	}

	@Override
	public Integer getPriority() {
		return priority;
	}

	@Override
	public String getPluginId() {
		return pluginId;
	}

	@Override
	public String getMessage(String... args) {
		return messageConstant.getMessage(args);
	}

	@Override
	public String getInternalId() {
		return name();
	}
}
