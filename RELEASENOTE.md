リリースバージョン:6.1.1
リリース日:2018/07/06

[パッケージダウンロードへ](#packages)

# ■新規機能

Hinemos ver.6.1.1で新機能の追加はありません。

# ■機能改善

Hinemos ver.6.1.1で改善された機能は以下の通りです。

- ファイル配布モジュール(WinRM)のメモリ使用量の改善
  
- エージェントへの接続失敗時のジョブ再実行処理の改善
  
- ジョブネットの終了判定動作の改善
  
- コマンド、メール、ログエスカレーション通知の処理性能改善
  
- 特定の形式のsyslogメッセージを受信すると、そのsyslogメッセージの送信元が"未登録ノード"と判定される
  
- Hinemosマネージャ起動時のジョブキャッシュ初期化処理の改善
  
- Windowsイベント監視のイベントログ取得処理の機能改善
  
- Windowsイベント監視のイベントログ解析処理(XMLパーサ)の改善
  
- 拡張エージェントアップデート機能
  
- ジョブ登録処理の性能改善
  
- Hinemosエージェントの情報取得処理の改善

# ■仕様変更

Hinemos ver.6.1.1で仕様の変更はありません。

# ■下位互換性

### バージョン6.1.0からの内部データベースの移行

- 6.1.0の内部データベースバックアップを6.1.1の内部データベースに適用することができます。

### 内部データベース変更点

- 内部データベースに変更はありません。

### コンポーネント間の相互互換性

- 6.1.1のマネージャ・クライアントには、6.1.0のマネージャ・クライアントとの互換性があります。
- 6.1.1のマネージャ・エージェントには、6.1.0のマネージャ・エージェントとの互換性があります。

### エージェント互換性

- Hinemos ver.6.1のマネージャとHinemos ver.6.0のエージェントは互換性があり、Hinemos ver.6.0相当の機能が利用可能です。
なお、Hinemos ver.6.1の新機能であるバイナリファイル監視、パケットキャプチャ監視は利用できません。

# ■パッケージ <a name="packages"/>

以下よりダウンロードできます。

https://github.com/hinemos/hinemos/releases/tag/v6.1.1

なお、Windows版マネージャ、RHEL6版マネージャと商用UNIXエージェントの入手方法についてはHinemosポータルサイト (http://www.hinemos.info/) のご相談・お見積依頼フォームにてお問い合わせください。

また、Android版HinemosエージェントはGoogle Playにて公開中です。
(http://www.hinemos.info/release/hinemosver600android)

# ■English Doc <a name="eng"/>

English manuals are also available here (uploaded on 7/6):

- Installation Manual ( [en_Install_Linux_6.1_rev1.pdf](https://github.com/hinemos/hinemos/releases/download/v6.1.1/en_Install_Linux_6.1_rev1.pdf) )
- Administrator's Guide ( [en_Admin_Linux_6.1_rev1.pdf](https://github.com/hinemos/hinemos/releases/download/v6.1.1/en_Admin_Linux_6.1_rev1.pdf) )
- User Manual ( [en_User_6.1_rev1.pdf](https://github.com/hinemos/hinemos/releases/download/v6.1.1/en_User_6.1_rev1.pdf) )
