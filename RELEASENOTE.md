リリースバージョン:6.1.2
リリース日:2018/10/26

[パッケージダウンロードへ](#packages)

# ■新規機能

Hinemos ver.6.1.2で新機能の追加はありません。

# ■機能改善

Hinemos ver.6.1.2で改善された機能は以下の通りです。

- ログファイル監視でのファイル読込における繰越データ長を増やす
- Hinemosエージェント（Ubuntu版）の Javaバージョンチェック改善

# ■不具合改修

Hinemos ver.6.1.2で修正された不具合、または不具合の可能性は以下の通りです。

- バイナリ監視にてレコード内のタイムスタンプが通知に反映されない
- ログファイル監視のファイルエンコーディングの設定を変更すると、監視中のログファイルが再度先頭から監視対象となる場合がある
- バイナリ監視にて想定外のデータが監視条件に一致してしまう
- Windowsイベント監視を無効にしているにも関わらず、Windowsイベント監視失敗の通知が出ることがある
- ログファイル監視の監視対象ディレクトリを更新しても反映されない
- Hinemosエージェントの再起動後、稀にログファイル監視でログメッセージが検知できなくなる場合がある
- Hinemosマネージャを起動するタイミングによって、極めて稀にスケジュールしたジョブを大量に実行する場合がある
- Webクライアントの脆弱性対応
- SNMP監視(数値)で収集値が "-1" で収集されてしまう
- HTTP(シナリオ)監視設定をコピーして作成した後、コピー元またはコピー先のページに関わる内容の変更が正しく反映されない場合がある
- 拡張エージェントアップデート機能によるLinuxエージェント更新にて一部不具合がある
- Windowsイベント監視の対象ノードにおいてOS側へのメモリリークが発生する
- SLES12 SP3の環境へHinemosエージェントがインストールできない

# ■仕様変更

Hinemos ver.6.1.2で仕様の変更はありません。

# ■下位互換性

### バージョン6.1.1からの内部データベースの移行

- 6.1.1の内部データベースバックアップを6.1.2の内部データベースに適用することができます。

### 内部データベース変更点

- 内部データベースに変更はありません。

### コンポーネント間の相互互換性

- 6.1.2のマネージャ・クライアントには、6.1.0～6.1.1のマネージャ・クライアントとの互換性があります。
- 6.1.2のマネージャ・エージェントには、6.1.0～6.1.1のマネージャ・エージェントとの互換性があります。

### エージェント互換性

- Hinemos ver.6.1のマネージャとHinemos ver.6.0のエージェントは互換性があり、Hinemos ver.6.0相当の機能が利用可能です。
なお、Hinemos ver.6.1の新機能であるバイナリファイル監視、パケットキャプチャ監視は利用できません。

# ■パッケージ <a name="packages"/>

以下よりダウンロードできます。

https://github.com/hinemos/hinemos/releases/tag/v6.1.2

なお、Windows版マネージャ、RHEL6版マネージャと商用UNIXエージェントの入手方法についてはHinemosポータルサイト (http://www.hinemos.info/) のご相談・お見積依頼フォームにてお問い合わせください。

また、Android版HinemosエージェントはGoogle Playにて公開中です。
(http://www.hinemos.info/release/hinemosver600android)

# ■English Doc <a name="eng"/>

English manuals are also available here (uploaded on 7/6):

- Installation Manual ( [en_Install_Linux_6.1_rev1.pdf](https://github.com/hinemos/hinemos/releases/download/v6.1.1/en_Install_Linux_6.1_rev1.pdf) )
- Administrator's Guide ( [en_Admin_Linux_6.1_rev1.pdf](https://github.com/hinemos/hinemos/releases/download/v6.1.1/en_Admin_Linux_6.1_rev1.pdf) )
- User Manual ( [en_User_6.1_rev1.pdf](https://github.com/hinemos/hinemos/releases/download/v6.1.1/en_User_6.1_rev1.pdf) )
