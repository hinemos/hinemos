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

[README(English)](README.md) | [Hinemosポータル](http://www.hinemos.info/) | [パッケージダウンロード](https://github.com/hinemos/hinemos/releases/tag/v7.1.0#packages)

## インストール

Hinemosはコマンドひとつでインストールできます。

- マネージャのインストール
  - RHEL 7.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.0/hinemos-7.1-manager-7.1.0-1.el7.x86_64.rpm```
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.0/hinemos-7.1-manager-7.1.0-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.0/hinemos-7.1-manager-7.1.0-1.el9.x86_64.rpm```


- Webクライアントのインストール
  - RHEL 7.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.0/hinemos-7.1-web-7.1.0-1.el7.x86_64.rpm```
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.0/hinemos-7.1-web-7.1.0-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.0/hinemos-7.1-web-7.1.0-1.el9.x86_64.rpm```

詳細は[Hinemos ver.7.1 基本機能マニュアル](https://github.com/hinemos/hinemos/releases/download/v7.1.0/ja_Base_Linux_7.1_rev1.pdf)をご覧下さい。


## ver.7.1新機能

- Hinemos Migration Assistant（他製品からの移行支援ツール）
    - #19379 Hinemos Migration Assistant

## ver.7.1機能改善

- 全般
    - #17640 TLS1.3対応
    - #19151 Oracle Linux 9対応
    - #19155 Amazon Linux 2023対応

- ミッションクリティカル機能
    - #17619 OCI上でのFIPと第2のスプリットブレイン防止機構対応
    - #19530 Google Cloud上での第2のスプリットブレイン防止機構対応

- クラウド管理機能（2024年7月リリース予定）
    - #18239 クラウド管理機能Google Cloud対応
    - #18238 クラウド管理機能OCI対応

詳細は[リリースノート](https://github.com/hinemos/hinemos/releases)をご覧下さい。

## ドキュメント

- Hinemos ver.7.1 基本機能マニュアル ([ja_Base_Linux_7.1_rev1.pdf](https://github.com/hinemos/hinemos/releases/download/v7.1.0/ja_Base_Linux_7.1_rev1.pdf) )

## ライセンス

GNU General Public License (GPL)
