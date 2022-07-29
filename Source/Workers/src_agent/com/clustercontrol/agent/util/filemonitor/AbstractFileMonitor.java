/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util.filemonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.log.LoggerSyslog;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * ファイル監視<BR>
 */
public abstract class AbstractFileMonitor<T extends AbstractFileMonitorInfoWrapper> {

	/** Syslog転送用ロガー */
	private LoggerSyslog m_syslog = null;

	/** ロガー */
	private static Log m_log = LogFactory.getLog(AbstractFileMonitor.class);

	/** ログファイルのオープンに失敗したときは、最初のみ「ファイルがありません」というinternalイベントを発生させる。 */
	private boolean m_initFlag = true;

	/** 最終詳細チェック（冒頭データ比較）実行時刻 */
	private long m_lastDataCheck = HinemosTime.currentTimeMillis();

	/** ファイル読み込み状態用ステータス */
	protected AbstractReadingStatus<T> status;

	/** ファイルチャネル */
	private FileChannel fileChannel;

	/** ファイルチェック時に、ファイルに変更がなかった回数 */
	private long m_unchanged_stats = 0;

	/** Tラッパー */
	protected T m_wrapper = null;

	/** ファイル監視用設定群 */
	protected FileMonitorConfig fileMonitorConfig;

	/** ファイルモニタマネージャ */
	private AbstractFileMonitorManager<T> fileMonitorManager;

	/** クリーンフラグ */
	private boolean cleanFlag;


	/**
	 * 監視項目設定更新日時
	 * 設定変更チェック用
	 */
	private long monitorInfoUpdateDate;


	/**
	 * コンストラクタ
	 * 
	 * @param monitorInfo 監視情報
	 * @param runInstructionInfo 指示情報
	 * @param fileMonitorConfig ファイル監視用せてい群
	 */
	public AbstractFileMonitor(AbstractFileMonitorManager<T> fileMonitorManager, T monitorInfo, AbstractReadingStatus<T> status, FileMonitorConfig fileMonitorConfig) {
		this.fileMonitorManager = fileMonitorManager;
		this.status = status;
		m_syslog = new LoggerSyslog();
		this.fileMonitorConfig = fileMonitorConfig;
		this.monitorInfoUpdateDate = monitorInfo.getUpdateDate();
		setMonitor(monitorInfo);
	}

	/**
	 * クリーン
	 */
	public void clean() {
		m_log.info("clean dir=" + m_wrapper.getDirectory() +
				", filepath=" + status.getFilePath());
		cleanFlag = true;
		closeFile();
	}

	/**
	 * ファイルパスを返す。
	 * 
	 * @return ファイルパス
	 */
	protected String getFilePath() {
		return status.getFilePath().getPath();
	}

	/**
	 * スレッドの動作メソッド<BR>
	 */
	public void run() {
		try {
			Long managerHavingUpdateDate = fileMonitorManager.getMonitorInfoUpdateDate(m_wrapper.getId());
			m_log.debug("run() check updatedate. manager updateDate=" + managerHavingUpdateDate + ", monitorInfoUpdateDate=" + monitorInfoUpdateDate);
			if (managerHavingUpdateDate != null && managerHavingUpdateDate > monitorInfoUpdateDate) {
				// マネージャが保持している更新日時が新しい場合、監視設定が変更されているためこのタスクは処理せず終了
				m_log.info("run() update check is false, so return. managerHavingUpdateDate=" + managerHavingUpdateDate + ", monitorInfoUpdateDate=" + monitorInfoUpdateDate);
				return;
			}
			runSub();
		} finally {
			// タスク終了を設定
			m_log.debug("run() thread done. id=" + m_wrapper.getId() + ", filePath=" + status.getFilePath().getPath());
			fileMonitorManager.doneFileMonitor(m_wrapper.getId(), status.getFilePath().getPath());
		}
	}

	/**
	 * スレッドの動作メソッド<BR>
	 * 
	 */
	private void runSub() {
		m_log.debug("monitor start. logfile=" + getFilePath() + ", file encoding(System.getProperty)=" + System.getProperty("file.encoding"));
		
		if (!status.isInitialized())
			return;
		
		// ファイルオープン
		if (fileChannel == null) {
			// オープンできないと終了
			if (!openFile()) {
				return;
			}
		}
		
		boolean readSuccessFlg = true; // 増加分読み込み成功フラグ
		long currentFilesize = 0;
		try {
			currentFilesize = fileChannel.size(); // 現在監視しているファイルのサイズを取得・・・（１）
			
			if (status.getPrevSize() == currentFilesize) {
				/** ログローテートを判定するフラグ */
				boolean logrotateFlag = false;			// ローテートフラグ
				
				int runInterval = fileMonitorConfig.getRunInterval();
				// ファイルサイズがm_unchangedStatsPeriod秒間以上変わらなかったら、ファイル切り替わりチェック
				if ((++m_unchanged_stats * runInterval) < fileMonitorConfig.getUnchangedStatsPeriod()) {
					return;
				}
				m_log.debug("run() : " + getFilePath() + " check log rotation");
				
				// ログローテートされているかチェックする
				// 従来の判定ロジック：
				// 現在監視しているファイルのサイズと本来監視すべきファイルのサイズが異なっている場合、
				// mv方式によりローテートされたと判断する。
				File file = new File(getFilePath());
				if (fileChannel.size() != file.length()) { // ・・・（２）
					m_log.debug("run() : " + getFilePath() + " file size not match");
					logrotateFlag = true;
					
					m_log.debug("run() : m_logrotate set true .1");// rmしたとき、このルートでrotateしたと判断される！！
					m_log.debug("run() : m_fr.length()=" + fileChannel.size());
					m_log.debug("run() : file.length()=" + file.length());
				} else if (currentFilesize > 0 && fileMonitorConfig.getFirstPartDataCheckPeriod() > 0 &&
						(HinemosTime.currentTimeMillis() - m_lastDataCheck) > fileMonitorConfig.getFirstPartDataCheckPeriod()){
					m_log.debug("run() : " + getFilePath() + " check first part of file");
					
					// 追加された判定ロジック：
					// 現在監視しているファイルのサイズと本来監視すべきファイルのサイズが同じであっても、
					// mv方式によりローテートされた可能性がある。
					// ファイルの冒頭部分を確認することで、ローテートされたことを確認する。
					logrotateFlag = checkPrefix();
					
					// 最終詳細チェック（ファイルデータ比較）実行時刻を設定
					m_lastDataCheck = HinemosTime.currentTimeMillis();
				}

				// ログローテートされたと判定された場合
				if(logrotateFlag){
					// 再度ファイルサイズの増加を確認する。
					// ローテートされたと判定されたが、実は、ローテートされておらず、
					//	（１）の時点では最終ログ読み込み時からログが出力されていないためサイズ（filesize）は同一であったが、
					//	（２）の判定直前にログが出力された場合は、ローテートされたと誤検知するため、
					// 再度サイズ比較を行う。
					if (currentFilesize == fileChannel.size()) {
						m_log.info(getFilePath() + " : file changed");
						closeFile();

						status.rotate();

						//ReadingStatusファイルのローテーション直後、その１秒以内に監視処理を完了してしまうと ReadingStatusファイルの管理上
						//問題がおきるケースがあるので1秒waitする。
						//（trueファイルとfalseファイルの更新日付が秒単位で同一になり どちらが最新なのか不明となる）
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							m_log.warn("LogfileMonitor . InterruptedException " + e.getMessage());
						}

						// ファイルオープン
						if (!openFile()) {
							return;
						}
						
						currentFilesize = fileChannel.size();
					}

				}
				m_unchanged_stats = 0;
			}

			m_unchanged_stats = 0;
			// FIXME
			// ログファイルの読込処理の途中で再起動された場合（positionがprevsize未満）、
			// 再起動後に即処理が再開されることが望ましいが
			// 現状の実装では対象ファイルのサイズが変わるまで 行われない。
			// status.positon が読込みの開始条件として考慮されていない
			if (status.getPrevSize() < currentFilesize) {
				//ファイルサイズを確認（上限を超えた場合通知）
				checkFilesize(fileChannel);// 
				// デバッグログ
				m_log.debug("run() read start: " + getFilePath() + ",prevsize=" + status.getPrevSize() + ",currentFilesize=" + currentFilesize +",FilePointer=" + status.getPosition());
				
				char[] cbuf = new char[1024];
				
				// ディスク書き込み負荷軽減の対応
				boolean statusUpdateFlag = true;
				
				try (Reader fr = new InputStreamReader(Channels.newInputStream(fileChannel), m_wrapper.getFileEncoding()) {
						@Override
						public void close() {
							// チャネルがクローズされないように修正。
						}
					}) {
					LineSeparator separator = new LineSeparator(m_wrapper.getFileReturnCode(), m_wrapper.getStartRegexString(), m_wrapper.getEndRegexString());
					int maxLines = fileMonitorConfig.getFilMessageLine();
					long start = System.currentTimeMillis();
					boolean logFlag = true;
					while (true) {
						readSuccessFlg = false;
						int read = fr.read(cbuf);
						readSuccessFlg = true;
						if (read == -1) {
							break;
						}
						m_log.debug("run() : " + getFilePath() + " . fr.read is success . read=" + read + ",fileChannel.position()=" + fileChannel.position() );

						// 今回出力処理する分のバッファを作成
						// 前回の繰越分と今回ファイルから読み出したデータのうち
						// 最後の改行までのデータをマージする。
						String appendedBuf = new StringBuilder(status.getCarryover().length() + read).append(status.getCarryover()).append(cbuf, 0, read).toString();
						List<String> lines = new LinkedList<String>();
						while (lines.size() <= maxLines) {
							int pos = separator.search(appendedBuf);
							if (pos != -1) {
								lines.add(appendedBuf.substring(0, pos));
								appendedBuf = appendedBuf.substring(pos, appendedBuf.length());
							} else {
								break;
							}
						}
						
						if (!appendedBuf.isEmpty()) {
							status.setCarryOver(appendedBuf);
							
							// 繰越データが非常に長い場合（規定の繰越データ長超え）は繰越バッファをカットする
							// FIXME
							// 改行コード（2byte以上）が繰越バッファ＋読取バッファをまたぐ場合、考慮不足でログがロストするケースあり。
							// 現状は繰越バッファに余裕があるために、ほぼ発現しない。
							if(status.getCarryover().length() > fileMonitorConfig.getFileReadCarryOverLength()){
								String message = "run() : " + getFilePath() + " carryOverBuf size = " + status.getCarryover().length() + 
										". carryOverBuf is too long. it cut down .(see monitor.logfile.read.carryover.length)";
								if (logFlag) {
									m_log.info(message);
									logFlag = false;
								} else {
									m_log.debug(message);
								}
								
								// ディスク書き込み負荷軽減の対応
								statusUpdateFlag = false;
								
								status.setCarryOver(status.getCarryover().substring(0, fileMonitorConfig.getFileReadCarryOverLength()));
							}
							m_log.debug("run() : " + getFilePath() + " carryOverBuf size " + status.getCarryover().length());
						} else {
							status.setCarryOver("");
							logFlag = true;
						}
						
						for (String line : lines) {
							m_log.debug("run() line=" + line);
							// 旧バージョンとの互換性のため、syslogでも飛ばせるようにする。
							if (m_syslog.isValid()) {
								// v3.2 mode
								String logPrefix = fileMonitorConfig.getProgram() + "(" + getFilePath() + "):";
								m_syslog.log(logPrefix + line);
							} else {
								// v4.0 mode
								patternMatchAndSendManager(line);
							}
						}
						//Copytruncate方式の場合、読み込み中のPositionを更新中に
						//ローテート処理がはしるとPositionがリセットされず、ローテートしたとみなされなくなる。
						//そのため、再度、ファイルチャネルのサイズと前のファイルのサイズを比較し、
						//ファイルチャネルのサイズが小さい場合、ローテートしたとみなす。
						currentFilesize =  fileChannel.size();
						if (status.getPrevSize() <= currentFilesize) {
							//直近のポジションを控えておく（差分のバイトデータ取得用）
							long prePosition = status.getPosition();
							// ファイルの読込情報を保存
							// FIXME
							// 読込み途中にReadingStatusファイルに中間保存してるpositionが実際の処理状況と合致しない場合あり
							// 処理用データを取得しているInputStreamReader での読込み位置と 
							// positionを取得してるfileChannel側でのposition管理が合致していない模様
							status.setPosition(fileChannel.position());
							status.setPrevSize(currentFilesize);
							
							//prefixBinary は必要な場合（未設定or長さが最大に達していない）のみ更新
							if (status.getPrefixBinary() == null || status.getPrefixBinary().isEmpty()) {
								//ローテーション検出などにより 未設定なら先頭から今回の読込み部分までのPrefixを取得
								status.setPrefixBinary(getMonFileByteData(0,status.getPosition()));
								int setListMax =Math.min((int)status.getPosition(), fileMonitorConfig.getFirstPartDataCheckSize());
								if(status.getPrefixBinary().size() > setListMax ){
									status.setPrefixBinary(status.getPrefixBinary().subList(0, setListMax));
								}
								status.setPrefixBinString(BinaryUtil.listToString(status.getPrefixBinary(), 1));
								if(m_log.isTraceEnabled()){
									m_log.trace("run() : " + getFilePath() + " prefixBinary initial set .status.prefixBinary .size " + status.getPrefixBinary().size() + ", FilePointer = " + fileChannel.position());
								}
							} else if (status.getPrefixBinary().size() < fileMonitorConfig.getFirstPartDataCheckSize()) {
								//設定済みだが最大長で無いなら今回の読込み部分をPREFIXに継ぎ足し
								List<Byte> addPreFix = getMonFileByteData(prePosition,status.getPosition());
								int addListMax = Math.min(addPreFix.size(), fileMonitorConfig.getFirstPartDataCheckSize() - status.getPrefixBinary().size());
								if(addPreFix.size() > addListMax ){
									addPreFix = addPreFix.subList(0, addListMax);
								}
								status.getPrefixBinary().addAll(addPreFix);
								status.setPrefixBinString(BinaryUtil.listToString(status.getPrefixBinary(), 1));
								if(m_log.isTraceEnabled()){
									m_log.trace("run() : " + getFilePath() + " prefixBinary add set .status.prefixBinary .size " + status.getPrefixBinary().size() + ", FilePointer = " + fileChannel.position());
								}
							}
							
							// ディスク書き込み負荷軽減の対応 繰越データが非常に長い場合は	
							// status書き出しをスキップする（運用上の影響は無し）
							if(statusUpdateFlag){
								status.store();
							}
							
							if(m_log.isTraceEnabled()){
								m_log.trace("run() : " + getFilePath() + " . status is stored . status.prevSize=" + status.getPrevSize() + ",status.position=" + status.getPosition() + ", fileChannel.size() = " + fileChannel.size() );
							}
						} else {
							fileChannel.position(0);
							status.rotate();
							if(m_log.isTraceEnabled()){
								m_log.trace("run() : " + getFilePath() + " . status is rotate . status.prevSize=" + status.getPrevSize() + ",status.position=" + status.getPosition() );
							}
						}
					}
					m_log.info(String.format("run() :" + getFilePath() + " , MonitorId " + m_wrapper.getId() + " , elapsed=%d ms.", System.currentTimeMillis() - start));
				}
				
				// ディスク書き込み負荷軽減の対応
				if(!statusUpdateFlag){
					status.store();
				}

				m_log.debug("run() read end: " + getFilePath() + " filesize = " + status.getPrevSize() + ", FilePointer = " + fileChannel.position());
			} else if (status.getPrevSize() > currentFilesize) {
				// 最初から読み込み
				m_log.info("run() : " + getFilePath() + " : file size becomes small");
				fileChannel.position(0);
				status.rotate();
			}
		} catch (IOException e) {
			if (readSuccessFlg) {
				if (cleanFlag) {
					// clean()呼出しで意図的にfileChannelを閉じているため、エラーとしない
					// （Windowsの場合に例外が発生するため、対応）
					m_log.info("run() cleanFlag is true, so this is not error: " + e.getMessage());
				} else {
					m_log.error("run() catch IOException: " + e.getMessage());
					sendMessageByFileReadIOException(e);
				}
				// エラーが発生したのでファイルクローズ
				closeFile();
			} else {
				m_log.warn("run() : " + e.getMessage());
				// 増加分読み込みの部分でエラーが起きた場合は、ファイルポインタを進める
				try {
					fileChannel.position(status.getPrevSize());
				} catch (IOException e1) {
					m_log.error("run() set file-pointer error : " + e1.getMessage());
				}
			}
		}
	}

	/**
	 * 監視管理情報へ通知
	 * 
	 * @param priority
	 *			  重要度
	 * @param app
	 *			  アプリケーション
	 * @param msg
	 *			  メッセージ
	 * @param msgOrg
	 *			  オリジナルメッセージ
	 */
	protected void sendMessage(int priority, String app, String msg, String msgOrg) {
		fileMonitorManager.sendMessage(getFilePath(), priority, app, msg, msgOrg, m_wrapper);
	}

	/**
	 * 転送対象ログファイルクローズ
	 * 
	 */
	private void closeFile() {
		if (fileChannel != null) {
			try {
				fileChannel.close();
				fileChannel = null;
			} catch (IOException e) {
				m_log.debug("run() : " + e.getMessage());
			}
		}
	}

	/**
	 * 転送対象ログファイルオープン
	 */
	private boolean openFile() {
		m_log.info("openFile : filename=" + status.getFilePath().getName());
		
		closeFile();
		
		FileChannel fc = null;

		// ファイルオープン
		try {
			if (checkPrefix())
				status.rotate();
			
			fc = FileChannel.open(Paths.get(getFilePath()), StandardOpenOption.READ);
			
			// ファイルサイズを確認（上限を超えた場合通知）
			checkFilesize(fc);

			// ファイルポインタの設定
			// 初回openはinit=trueで途中から読む。
			// ローテーションの再openはinit=falseで最初から読む。
			fc.position(status.getPosition());

			fileChannel = fc;
			
			return true;
		} catch (FileNotFoundException e) {
			m_log.info("openFile : " + e.getMessage());
			if (m_initFlag) {
				// 監視管理へ通知
				sendMessageByFileOpenFileNotFoundException(e);
			}

			return false;
		} catch (SecurityException e) {
			m_log.info("openFile : " + e.getMessage());
			if (m_initFlag) {
				// 監視管理へ通知
				sendMessageByFileOpenSecurityException(e);
			}
			return false;
		} catch (IOException e) {
			m_log.info("openFile : " + e.getMessage());
			if (m_initFlag) {
				// 監視管理へ通知
				sendMessageByFileOpenIOException(e);
			}
			return false;
		} finally {
			// ファイルは開けたが、なんらかの例外が出力された場合に、ファイルをクローズ。
			if (fc != null && fileChannel == null) {
				try {
					fc.close();
				} catch (IOException e) {
					m_log.warn(e.getMessage(), e);
				}
			}
			m_initFlag = false;
		}
	}

	/**
	 * 監視対象ファイルから指定箇所のデータをList<byte>で取得.<br>
	 * 
	 * @return 取得できなかった場合は空のListを返却
	 * 
	 */
	private List<Byte> getMonFileByteData(long fromPos ,long toPos ) {
		if(m_log.isTraceEnabled()){
			m_log.trace( "getMonFileByteData() :start. fromPos="+fromPos +" toPos="+toPos);
		}
		List<Byte> partOfFile = new ArrayList<Byte>();
		long getLen =toPos - fromPos;
		if(getLen == 0 ){// 読込み不要なら、空のリストを返す
			return partOfFile;
		}

		//接続済みのチャネルを用いて監視対象ファイルの指定部分だけ読込む
		//（mv方式でのログローテートへの対応）
		long curPos = 0;
		try{
			curPos = fileChannel.position();
		} catch (IOException e) {
			//ポジションの変更が出来ない場合は 空のリストを返す
			m_log.warn("getMonFileByteData() :	fileChannel.position():"+ e.getMessage(), e);
			return partOfFile;
		}
		try{
			try {
				ByteBuffer buf = ByteBuffer.allocate((int)getLen);
				fileChannel.position(fromPos);
				int readed =fileChannel.read(buf);
				for (byte target : buf.array()) {
					partOfFile.add(target);
				} 
				if(getLen > readed && partOfFile.size() > readed &&	 readed > 0	 ){
					partOfFile = partOfFile.subList(0, readed);
				}
			} catch (IOException e) {
				m_log.warn("getMonFileByteData() :"+ e.getMessage(), e);
			}
			if(m_log.isTraceEnabled()){
				m_log.trace("getMonFileByteData() :partOfFile size = " + partOfFile.size());
			}
		//保管していたポジションに戻す
		}finally{
			try{
				fileChannel.position(curPos);
			} catch (IOException e) {
				m_log.warn("getMonFileByteData() :	fileChannel.position(arg) :"+ e.getMessage(), e);
			}
		}
		
		return partOfFile;
	}

	/**
	 * ファイル冒頭部分の確認
	 * @return
	 * @throws IOException
	 */
	private boolean checkPrefix() throws IOException {
		boolean logrotateFlag = false;

		try {
			if (status.getPrefixBinary().size() > 0 ) {
				List<Byte> newFileBinary = status.getCurrentPrefix();
				
				//前回に保存したPrefix長にListをそろえてから比較処理へ渡す(でないと比較がうまくできない)
				int prefixBinarySize = status.getPrefixBinary().size();
				List<Byte> newFirstPartOfFile = new ArrayList<Byte>(newFileBinary);
				if (newFirstPartOfFile.size() > prefixBinarySize){
					newFirstPartOfFile = newFirstPartOfFile.subList(0, prefixBinarySize);
				} 
				List<Byte> oldFirstPartOfFile = status.getPrefixBinary();
				// ログ出力.
				if (m_log.isDebugEnabled()) {
					try {
						String newFirstPartString = BinaryUtil.listToString(newFirstPartOfFile, 1);
						String preFirstPartString = BinaryUtil.listToString(oldFirstPartOfFile, 1);
						m_log.debug("checkPrefix() : " + getFilePath() + " newFirstPartOfFile : "+ newFirstPartString);
						m_log.debug("checkPrefix() : " + getFilePath() + " preFirstPartOfFile : "+ preFirstPartString);
					} catch (Exception e) {
						m_log.error("checkPrefix() : " + getFilePath() + " " + e.getMessage(),e);
					}
				}
				
				// readingStatus作成時点のファイルの先頭と現時点のファイルの先頭を比較.
				if (!BinaryUtil.equals(newFirstPartOfFile, oldFirstPartOfFile)) {
					// readingStatus作成時点と先頭が異なっているので、ローテーションされてると判定.
					m_log.debug("checkPrefix() : " +getFilePath()  + " log rotation detected");
					logrotateFlag = true;
					m_log.debug("checkPrefix() : m_logrotate set true .2");
				}
			}
		} catch (RuntimeException e) {
			m_log.error("checkPrefix() : " + getFilePath() + " " + e.getMessage(), e);
		}
		
		return logrotateFlag;
	}

	/**
	 * 対象ログファイルのサイズ確認
	 */
	private void checkFilesize(FileChannel fc) throws IOException{
		long filesize = fc.size();
		if (filesize > fileMonitorConfig.getFileMaxSize()) {
			// ファイルサイズが大きい場合、監視管理へ通知
			sendMessageByFileSizeOver(filesize);
		}
	}

	/**
	 * 監視情報を設定する
	 * @param monitorInfo 監視情報
	 */
	public void setMonitor(T monitorInfo) {
		this.m_wrapper = monitorInfo;
		this.monitorInfoUpdateDate = monitorInfo.getUpdateDate(); // monitorInfoの更新と合わせて時刻も更新
	}

	/**
	 * ファイルオープン失敗（FileNotFoundException）に伴いマネージャへメッセージを送信する
	 */
	protected abstract void sendMessageByFileOpenFileNotFoundException(FileNotFoundException e);

	/**
	 * ファイルオープン失敗（SecurityException）に伴いマネージャへメッセージを送信する
	 */
	protected abstract void sendMessageByFileOpenSecurityException(SecurityException e);

	/**
	 * ファイルオープン失敗（IOException）に伴いマネージャへメッセージを送信する
	 */
	protected abstract void sendMessageByFileOpenIOException(IOException e);

	/**
	 * ファイル読み込み失敗（IOException）に伴いマネージャへメッセージを送信する
	 */
	protected abstract void sendMessageByFileReadIOException(IOException e);

	/**
	 * ファイルサイズ超過に伴いマネージャへメッセージを送信する
	 */
	protected abstract void sendMessageByFileSizeOver(long fileSize);
	
	/**
	 * パターンマッチ処理を行い結果をマネージャに送信する
	 * 
	 * @param line 対象文字列
	 */
	protected abstract void patternMatchAndSendManager(String line);
}