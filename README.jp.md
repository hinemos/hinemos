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

[README(English)](README.md) | [Hinemosポータル](http://www.hinemos.info/) | [パッケージダウンロード](https://github.com/hinemos/hinemos/releases/tag/v6.2.2#packages)

## インストール

Hinemosはコマンドひとつでインストールできます。

- マネージャのインストール

```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v6.2.2/hinemos-6.2-manager-6.2.2-1.el7.x86_64.rpm```

- Webクライアントのインストール

```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v6.2.2/hinemos-6.2-web-6.2.2-1.el7.x86_64.rpm```

詳細は[インストールマニュアル](https://github.com/hinemos/hinemos/releases/download/v6.2.2/ja_Install_Linux_6.2_rev5.pdf)をご覧下さい。

## ver.6.2新機能

- ノードの構成情報を履歴管理・変更通知
	- 管理対象のパッケージやプログラムなどの情報を定期的に取得し履歴管理
	- 構成情報の変更を通知
	- 蓄積した情報に対する検索、CSV出力 (サブスクリプションで提供)

## ver.6.2機能改善

- エージェントアップデートの改善
	- 転送効率改善およびアップデートステータスの詳細化

- 同時実行制御キュー
	- 同時実行制御キューにより、ジョブやジョブネットをまたがった同時実行数を制御
	
- イベント情報の拡張
	- イベント情報に含める項目にユーザーの独自情報を追加可能

- イベント情報を使ったユーザ操作
	- イベント情報を使った操作（コマンド）を事前に定義しユーザ判断によって実行が可能

- 監視履歴画面の拡張
	- 監視履歴の各ビューに対して既定レイアウトのカスタマイズが可能

- バージョンアップツールの改善(サブスクリプションで提供)
	- Hinemos ver.4.1以降のHinemosの定義をHinemos ver.6.2の定義へ直接変換


詳細は[リリースノート](https://github.com/hinemos/hinemos/releases)をご覧下さい。

## ドキュメント

- インストールマニュアル ([ja_Install_Linux_6.2_rev5.pdf](https://github.com/hinemos/hinemos/releases/download/v6.2.2/ja_Install_Linux_6.2_rev5.pdf) )
- ユーザマニュアル ( [ja_User_6.2_rev4.pdf](https://github.com/hinemos/hinemos/releases/download/v6.2.2/ja_User_6.2_rev4.pdf) )
- 管理者ガイド ( [ja_Admin_Linux_6.2_rev3.pdf](https://github.com/hinemos/hinemos/releases/download/v6.2.2/ja_Admin_Linux_6.2_rev3.pdf) )

## ライセンス

GNU General Public License (GPL)
