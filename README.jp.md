## Hinemos

<p align="center">
	<img alt="download" src="https://img.shields.io/github/downloads/hinemos/hinemos/total.svg"/>
	<img alt="license" src="https://img.shields.io/badge/license-GPL-blue.svg"/>
	<a href=https://twitter.com/Hinemos_INFO>
		<img alt="twitter" src="https://img.shields.io/twitter/follow/Hinemos_INFO.svg?style=social&label=Follow&maxAge=2592000"/>
	</a>
</p>

![Hinemos-logo](http://www.hinemos.info/files/images/HinemosLogo.png)

Hinemosは大規模、複雑化するITシステムの「監視」や「ジョブ」といった「運用業務の自動化」を実現し、オープンソースソフトウェアが持つコストメリットを最大限に活用できる統合運用管理ソフトウェアです。

[README(English)](README.md) | [Hinemosポータル](http://www.hinemos.info/) | [パッケージダウンロード](https://github.com/hinemos/hinemos/releases/tag/v7.2.0#packages_720)

## インストール

Hinemosはコマンドひとつでインストールできます。

- マネージャのインストール
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-manager-7.2.0-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-manager-7.2.0-1.el9.x86_64.rpm```
  - RHEL 10.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-manager-7.2.0-1.el10.x86_64.rpm```


- Webクライアントのインストール
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-web-7.2.0-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-web-7.2.0-1.el9.x86_64.rpm```
  - RHEL 10.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-web-7.2.0-1.el10.x86_64.rpm```

詳細は[Hinemos ver.7.2 基本機能マニュアル](https://github.com/hinemos/hinemos/releases/download/v7.2.0/ja_Base_Linux_7.2_rev1.pdf)をご覧下さい。


## ver.7.2新機能

- Hinemos AIエージェント
    - #7221478 Hinemos AIエージェント

- 電話通知
    - #7221306 電話通知

- ジョブ実行依存階層出力ツール
    - #7221843 ジョブ実行依存階層出力ツール

## ver.7.2機能改善

- メンテナンス
  - #7221629 Hinemos_manager_summaryでカスタムトラップ監視の内部キュー数とサマリ情報を取得できるようにする
  - #7221565 Hinemosプロパティのフィルタ機能追加
  - #7221450 環境サマリ情報を取得するスクリプトで、cc_collect_data_stringテーブルの内容が出力できるように改善する
  - #7213165 メンテナンススクリプトによって、カスタムトラップ監視の受信に関する設定を変更できるようにする

- セルフチェック
  - #7221627 セルフチェックにカスタムトラップ監視の内部キュー数チェックを追加する

- 監視
  - #7221739 ログファイル監視で時間区切りで監視を実施できるようにする
  - #7221635 JMX監視の監視項目にカスタムトラップ監視の内部キュー数チェックを追加する
  - #7221605 ログファイル監視で時間区切りで監視を実施できるようにする
  - #7221529 監視履歴[イベント]ビューのイベントから対象ノードのプロパティを確認できるよう改善
  - #7219073 ログファイル監視において、監視と収集がともに無効な場合のログ読み込みを抑止し、エージェントのIO負荷を軽減する。

- ジョブ
  - #7221715 PostgreSQLのshared_buffersメモリを拡張する
  - #7221593 ジョブの設定可能なシステムジョブ変数、および通知機能の置換文字列にジョブユニットIDとジョブID、ジョブ名の項目を追加する
  - #7221564 複数のジョブ同時実行制御キューを同時削除できるよう改善
  - #7221061 待ち条件に先行ジョブとジョブ変数（判定対象の条件関係：AND）で設定時、先行ジョブの終了後にジョブ変数の待ち条件判定を行えるようにする

- インストーラー
  - #7219416 Linux版エージェント(Ubuntu)でSysVinitにしか対応していない操作(エージェント複製スクリプト・インストール)をsystemdに対応させる

- コンポーネント
  - #7212780 【商用UNIX】Solaris11.4以降の/usr/ucb/psコマンドに関する仕様変更に対応する

- ミッションクリティカル
  - #7221052 【HA】Cluster Controller間のサーバ情報の暗号化キーがサーバー間で同一でなかった場合、StandbyサーバがシャットダウンしMasterサーバから通知が発生するように改善する
  - #7219437 【HA】Hinemos Manager (JavaVM)ヘルスチェック機構で利用している接続確認でOSで設定されているプロキシを参照しないようにする
  - #7218402 【HA】Hinemos Manager (JavaVM)ヘルスチェック機構のタイムアウトを制御できるように改善する
  - #7215157 【HA】STANDBYサーバ起動時のPostgreSQLの同期状態の確認回数を可変できるようにする

- Utility
  - #7221638 【Utility】JMX監視の監視項目にカスタムトラップ監視の内部キュー数チェックを追加する

- クラウドVM管理
  - #7221717 【AWS】【GCP】クラウドサービス監視マスタに登録されたサービスの削除手段を提供する
  - #7221461 【クラウド管理】クラウドログ監視について、監視対象のログストリームが多い状態で、新規ログストリームを検知した場合の監視実行時間を改善する
  - #7221358 【AWS】AWS Service Health Dashboardで提供されるRSSの「AWS WAF」「AWS IoT Device Management」「AWS Resource Groups」のURLを最新化する
  - #7221319 【AWS】クラウドサービス監視で、RSSのインシデントに「Service impact」で配信されるケースに対応する
  - #7221152 【AWS】HinemosマネージャからAWSエンドポイントへの接続する際に、HTTPSプロキシ経由で接続できるように改善する
  - #7221124 【クラウド/VM管理】リソース制御ジョブで指定するコンピュートIDにジョブ変数を利用可能にする
  - #7220836 【AWS】AWS SDK for Java 2対応
  - #7220330 【AWS】IAMロール認証を使用するクラウドスコープを監視対象としたクラウドログ監視について、AWSの認証情報の取得（AssumeRole）回数を軽減する
  - #7218587 【Azure】Azureで使用しているAzure REST APIのバージョンを更新する

- コマンドラインツール
  - #7221337 【コマンドラインツール】ユーザが追加したプラットフォームマスタをrepository_deleteCollectPlatformMaster.pyで削除できるようにする

- バージョンアップツール
  - #7221781 【バージョンアップツール】クラウドサービス監視で、RSSのインシデントに「Service impact」で配信されるケースに対応する

- インシデント管理連携ツール
  - #7221559 【インシデント管理連携ツール】ServiceNow連携でOAuth 2.0認証とパスワード等の暗号化に対応する
  - #7219956 【インシデント管理連携ツール】インシデント管理連携ツールをPython3.9以降へ対応させる

- その他
  - #7221643 スケジューラ実行時の内部処理の改善を行う
  - #7219211 スケジューラに遅延が生じ、スケジュールされた機能が実行されなかった場合の通知を制御できるようにする

詳細は[リリースノート](https://github.com/hinemos/hinemos/releases)をご覧下さい。

## ドキュメント

- Hinemos ver.7.2 基本機能マニュアル ([ja_Base_Linux_7.2_rev1.pdf](https://github.com/hinemos/hinemos/releases/download/v7.2.0/ja_Base_Linux_7.2_rev1.pdf) )

## ライセンス

GNU General Public License (GPL)
