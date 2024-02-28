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

[README(English)](README.md) | [Hinemosポータル](http://www.hinemos.info/) | [パッケージダウンロード](https://github.com/hinemos/hinemos/releases/tag/v7.0.1#packages)

## インストール

Hinemosはコマンドひとつでインストールできます。

- マネージャのインストール
  - RHEL 7.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.1/hinemos-7.0-manager-7.0.1-1.el7.x86_64.rpm```
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.1/hinemos-7.0-manager-7.0.1-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.1/hinemos-7.0-manager-7.0.1-1.el9.x86_64.rpm```


- Webクライアントのインストール
  - RHEL 7.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.1/hinemos-7.0-web-7.0.1-1.el7.x86_64.rpm```
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.1/hinemos-7.0-web-7.0.1-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.1/hinemos-7.0-web-7.0.1-1.el9.x86_64.rpm```

詳細は[Hinemos ver.7.0 基本機能マニュアル](https://github.com/hinemos/hinemos/releases/download/v7.0.1/ja_Base_Linux_7.0_rev17.pdf)をご覧下さい。


## ver.7.0新機能

- 監視
    - #13586 SDML(Software Defined Monitoring and Logging)

- エンタープライズ
    - #13585 RPA管理機能

- ユーティリティツール
    - #13115 Grafanaプラグイン

## ver.7.0機能改善

- マネージャ、エージェント、クライアント
    - #15254、#15260、15187 JREでTLSが1.3がデフォルトとなる変更に伴い、Hinemosでの通信がTLS1.2で行われるように対応する

- クライアント・エージェント
    - #11218 フィルタ条件保存
    - #12671 エージェントヘルスチェック
    - #10237 オペレーションログへのログインセッションと操作を紐づけた記録
    - #12483 Windows版Hinemosエージェントのインストーラ改善

- リポジトリ
    - #11519 ノード情報にクラウド・仮想化管理の項目追加
    - #12342 ノード情報にRPA管理の項目追加
    - #12729 DHCPサポート機能
    - #12729 エージェント設定によるスコープ自動割当て
    - #11479 構成情報取得設定 ユーザ任意情報コマンド動作モード(repository.cmdb.command.mode) "auto"の場合の動作変更

- 通知
    - #11692 REST通知
    - #11611 通知結果にIDを追加
    - #11479 イベントカスタムコマンド コマンドの実行モード(monitor.event.customcmd.cmdN.mode) "auto"の場合の動作変更
    - #14716 通知の監視詳細、アプリケーションおよびスコープの内容を改善する

- メンテナンス
   - #12256 履歴削除にメンテナンス種別に「ジョブ連携メッセージ削除」追加
   - #11519 履歴削除にメンテナンス種別に「RPAシナリオ 実績削除」追加
   - #11319 Hinemosに登録されているデータをファイル出力するスクリプト追加

- セルフチェック
    - #10151 HinemosエージェントのINTERNALイベントをSyslog送信に対応
    - #7227 マネージャで生じたINTERNALイベントのメール送信件名指定

- 監視
    - #12427 ログ件数監視の仕様改善
    - #12774 カスタムトラップ監視のMC機能対応
    - #11714 文字列系監視の重要度変化に対する仕様改善
    - #12758 クラウドサービス監視の重要度変化に対する仕様改善
    - #11519 ログファイル監視 ディレクトリのノードプロパティ対応
    - #11479 カスタム監視 カスタム監視のコマンド動作に関するOSプラットフォーム定義(monitor.custom.command.mode) "auto"の場合の動作変更
    - #12149 HTTP監視(シナリオ)がリダイレクトに対応
    - #11524 Windowsサービス監視で使用するWinRMの認証方法にNTLM認証を追加

- ジョブ
    - #11318 ジョブセッション事前生成
    - #12256 ジョブ連携メッセージ
    - #12487 ファイルチェックジョブ
    - #12488 標準出力のファイル出力
    - #12639 戻り値の飛び番対応
    - #12486 複数待ち条件対応
    - #12486 待ち条件の判定ロジック修正
    - #12606 繰り返し実行
    - #11479 コマンドジョブ 起動コマンド動作モード(job.command.mode) "auto"の場合の動作変更

- クラウド管理
    - #12342 クラウドログ監視
    - #12482 クラウド通知
    - #12395 クラウドリソース制御ジョブのMC対応
    - #12130 ノードの管理対象フラグがコンピュートの操作に応じて変更されるよう改善

- ノードマップ
    - #11507 構成情報ダウンロードの内容にファシリティ名を追加

- その他改善
    - #11226 メール送信に使用するSMTPサーバを複数登録できるよう改善
    - #13144 メール送信の認証方法にOAuth2.0を追加


詳細は[リリースノート](https://github.com/hinemos/hinemos/releases/tag/v7.0.1)をご覧下さい。

## ドキュメント

- Hinemos ver.7.0 基本機能マニュアル ([ja_Base_Linux_7.0_rev13.pdf](https://github.com/hinemos/hinemos/releases/download/v7.0.1/ja_Base_Linux_7.0_rev17.pdf) )

## ライセンス

GNU General Public License (GPL)
