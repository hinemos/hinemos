/*

 Copyright (C) 2011 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.MonitorStringUtil;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ログファイル監視<BR>
 */
public class LogfileMonitor {

	// Syslog転送用ロガー
	private LoggerSyslog m_syslog = null;

	// ロガー
	private static Log m_log = LogFactory.getLog(LogfileMonitor.class);

	/** ログファイルのオープンに失敗したときは、最初のみ「ファイルがありません」というinternalイベントを発生させる。 */
	private boolean m_initFlag = true;
	private long m_lastDataCheck = HinemosTime.currentTimeMillis(); // 最終詳細チェック（冒頭データ比較）実行時刻

	private ReadingStatusRoot.ReadingStatus status;
	private FileChannel fileChannel;
	
	private long m_unchanged_stats = 0; // ファイルチェック時に、ファイルに変更がなかった回数
	
	private MonitorInfoWrapper m_wrapper = null;
	
	/**
	 * コンストラクタ
	 * 
	 * @param monitorInfo 監視情報
	 * @param runInstructionInfo 指示情報
	 */
	public LogfileMonitor(MonitorInfoWrapper monitorInfo, ReadingStatusRoot.ReadingStatus status) {
		this.status = status;
		m_syslog = new LoggerSyslog();
		setMonitor(monitorInfo);
	}

	public void clean() {
		m_log.info("clean dir=" + m_wrapper.monitorInfo.getLogfileCheckInfo().getDirectory() +
				", filepath=" + status.rsFilePath);
		closeFile();
	}
	
	
	private String getFilePath() {
		return status.filePath.getPath();
	}
	
	/**
	 * スレッドの動作メソッド<BR>
	 * 
	 */
	public void run() {
		m_log.debug("monitor start.  logfile : " + getFilePath() + "  file encoding : " + System.getProperty("file.encoding"));
		
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
			
			if (status.prevSize == currentFilesize) {
				/** ログローテートを判定するフラグ */
				boolean logrotateFlag = false;			// ローテートフラグ
				
				int runInterval = LogfileMonitorManager.getRunInterval();
				// ファイルサイズがm_unchangedStatsPeriod秒間以上変わらなかったら、ファイル切り替わりチェック
				if ((++m_unchanged_stats * runInterval) < LogfileMonitorConfig.unchangedStatsPeriod) {
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
				} else if (currentFilesize > 0 && LogfileMonitorConfig.firstPartDataCheckPeriod > 0 &&
						(HinemosTime.currentTimeMillis() - m_lastDataCheck) > LogfileMonitorConfig.firstPartDataCheckPeriod){
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
					//  （１）の時点では最終ログ読み込み時からログが出力されていないためサイズ（filesize）は同一であったが、
					//  （２）の判定直前にログが出力された場合は、ローテートされたと誤検知するため、
					// 再度サイズ比較を行う。
					if (currentFilesize == fileChannel.size()) {
						m_log.info(getFilePath() + " : file changed");
						closeFile();

						status.rotate();

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

			if (status.prevSize < currentFilesize) {
				// デバッグログ
				m_log.debug("run() : " + getFilePath() +
						" filesize " + status.prevSize + " tmp_filesize " + currentFilesize);
				
				char[] cbuf = new char[1024];
				try (Reader fr = new InputStreamReader(Channels.newInputStream(fileChannel), m_wrapper.monitorInfo.getLogfileCheckInfo().getFileEncoding()) {
						@Override
						public void close() {
							// チャネルがクローズされないように修正。
						}
					}) {
					LineSeparator separator = new LineSeparator(m_wrapper.monitorInfo.getLogfileCheckInfo());
					
					int maxBytes = m_wrapper.monitorInfo.getLogfileCheckInfo().getMaxBytes() != null ? m_wrapper.monitorInfo.getLogfileCheckInfo().getMaxBytes(): LogfileMonitorConfig.logfilMessageLength;
					int maxLines = LogfileMonitorConfig.logfilMessageLine;
					
					long start = System.currentTimeMillis();
					boolean logFlag = true;
					while (true) {
						readSuccessFlg = false;
						
						int read = fr.read(cbuf);
						readSuccessFlg = true;
						if (read == -1)
							break;
						
						// 今回出力処理する分のバッファを作成
						// 前回の繰越分と今回ファイルから読み出したデータのうち
						// 最後の改行までのデータをマージする。
						String appendedBuf = new StringBuilder(status.carryover.length() + read).append(status.carryover).append(cbuf, 0, read).toString();
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
							status.carryover = appendedBuf;
							
							// 繰越データが非常に長い場合はバッファをカットする
							if(status.carryover.length() > maxBytes){
								String message = "run() : " + getFilePath() + " carryOverBuf size = " + status.carryover.length() + 
										". carryOverBuf is too long. it cut down .(see monitor.logfile.message.length)";
								if (logFlag) {
									m_log.info(message);
									logFlag = false;
								} else {
									m_log.debug(message);
								}
								
								status.carryover = status.carryover.substring(0, maxBytes);
							}
							
							// デバッグログ
							if (m_log.isDebugEnabled()) {
								m_log.debug("run() : " + getFilePath()
										+ " carryOverBuf size "
										+ status.carryover.length());
							}
						} else {
							status.carryover = "";
							logFlag = true;
						}
						
						for (String line : lines) {
							// 旧バージョンとの互換性のため、syslogでも飛ばせるようにする。
							if (m_syslog.isValid()) {
								// v3.2 mode
								String logPrefix = LogfileMonitorConfig.program + "(" + getFilePath() + "):";
								m_syslog.log(logPrefix + line);
							} else {
								// v4.0 mode
								MonitorStringUtil.patternMatch(LogfileMonitorConfig.formatLine(line, m_wrapper.monitorInfo), m_wrapper.monitorInfo, m_wrapper.runInstructionInfo, getFilePath());
							}
						}
						//Copytruncate方式の場合、読み込み中のPositionを更新中に
						//ローテート処理がはしるとPositionがリセットされず、ローテートしたとみなされなくなる。
						//そのため、再度、ファイルチャネルのサイズと前のファイルのサイズを比較し、
						//ファイルチャネルのサイズが小さい場合、ローテートしたとみなす。
						currentFilesize =  fileChannel.size();
						if (status.prevSize <= currentFilesize) {
							// ファイルの読込情報を保存
							status.position = fileChannel.position();
							status.prevSize = currentFilesize;
							if (status.prefix == null || status.prefix.isEmpty()) {
								status.prefix = new String(cbuf, 0, Math.min(read, LogfileMonitorConfig.firstPartDataCheckSize));
							} else if (status.prefix.length() < LogfileMonitorConfig.firstPartDataCheckSize) {
								status.prefix = status.prefix + new String(cbuf, 0, Math.min(read, LogfileMonitorConfig.firstPartDataCheckSize - status.prefix.length()));
							}
							status.store();
						} else {
							fileChannel.position(0);
							status.rotate();
						}
					}
					
					m_log.info(String.format("run() : elapsed=%d ms.", System.currentTimeMillis() - start));
				}

				if(m_log.isDebugEnabled()){
					m_log.debug("run() : " + getFilePath() + " filesize = " + status.prevSize + ", FilePointer = " + fileChannel.position());
				}
			} else if (status.prevSize > currentFilesize) {
				// 最初から読み込み
				m_log.info("run() : " + getFilePath() + " : file size becomes small");
				fileChannel.position(0);
				status.rotate();
			}
		} catch (IOException e) {
			if (readSuccessFlg) {
				m_log.error("run() : " + e.getMessage());
				String[] args = { getFilePath() };
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.4=ファイルの読み込みに失敗しました
				sendMessage(PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
						MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage());

				// エラーが発生したのでファイルクローズ
				closeFile();
			} else {
				m_log.warn("run() : " + e.getMessage());
				// 増加分読み込みの部分でエラーが起きた場合は、ファイルポインタを進める
				try {
					fileChannel.position(status.prevSize);
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
	 *            重要度
	 * @param app
	 *            アプリケーション
	 * @param msg
	 *            メッセージ
	 * @param msgOrg
	 *            オリジナルメッセージ
	 */
	private void sendMessage(int priority, String app, String msg, String msgOrg) {
		LogfileMonitorManager.sendMessage(getFilePath(), priority, app, msg, msgOrg, m_wrapper.monitorInfo.getMonitorId(), m_wrapper.runInstructionInfo);
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
		m_log.info("openFile : filename=" + status.rsFilePath.getName());
		
		closeFile();
		
		FileChannel fc = null;

		// ファイルオープン
		try {
			if (checkPrefix())
				status.rotate();
			
			fc = FileChannel.open(Paths.get(getFilePath()), StandardOpenOption.READ);
			
			long filesize = fc.size();
			if (filesize > LogfileMonitorConfig.fileMaxSize) {
				// ファイルサイズが大きい場合、監視管理へ通知
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.3=ファイルサイズが上限を超えました
				// message.log.agent.5=ファイルサイズ「{0} byte」
				String[] args1 = { getFilePath() };
				String[] args2 = { String.valueOf(filesize) };
				sendMessage(PriorityConstant.TYPE_INFO,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FILE_SIZE_EXCEEDED_UPPER_BOUND.getMessage(),
						MessageConstant.MESSAGE_LOG_FILE.getMessage(args1) + ", "
								+ MessageConstant.MESSAGE_LOG_FILE_SIZE_BYTE.getMessage(args2));
			}

			// ファイルポインタの設定
			// 初回openはinit=trueで途中から読む。
			// ローテーションの再openはinit=falseで最初から読む。
			fc.position(status.position);

			fileChannel = fc;
			
			return true;
		} catch (FileNotFoundException e) {
			m_log.info("openFile : " + e.getMessage());
			if (m_initFlag) {
				// 最初にファイルをチェックする場合、監視管理へ通知
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.2=ログファイルがありませんでした
				String[] args = { getFilePath() };
				sendMessage(PriorityConstant.TYPE_INFO,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FILE_NOT_FOUND.getMessage(),
						MessageConstant.MESSAGE_LOG_FILE.getMessage(args));
			}

			return false;
		} catch (SecurityException e) {
			m_log.info("openFile : " + e.getMessage());
			if (m_initFlag) {
				// 監視管理へ通知
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.4=ファイルの読み込みに失敗しました
				String[] args = { getFilePath() };
				sendMessage(PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
						MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage());
			}
			return false;
		} catch (IOException e) {
			m_log.info("openFile : " + e.getMessage());
			if (m_initFlag) {
				// 監視管理へ通知
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.4=ファイルの読み込みに失敗しました
				String[] args = { getFilePath() };
				sendMessage(PriorityConstant.TYPE_INFO,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
						MessageConstant.MESSAGE_LOG_FILE.getMessage(args));
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
	
	private boolean checkPrefix() throws IOException {
		boolean logrotateFlag = false;

		try {
			char[] newFirstPartOfFile = new char[LogfileMonitorConfig.firstPartDataCheckSize];
			Arrays.fill(newFirstPartOfFile, '\0');  // 全ての要素を0で初期化

			if (status.prefix.length() > 0) {
				// ファイル名指定で新たにオープンするファイル
				try (BufferedReader newFile = new BufferedReader(new InputStreamReader(new FileInputStream(getFilePath()), this.m_wrapper.monitorInfo.getLogfileCheckInfo().getFileEncoding()))) {
					if (newFile.read(newFirstPartOfFile) >= status.prefix.length()) {
						String firstPart = new String(newFirstPartOfFile, 0, status.prefix.length());
						
						if(m_log.isDebugEnabled()){
							try {
								m_log.debug("run() : " + getFilePath() + " newFirstPartOfFile : " + firstPart);
							} catch (Exception e) {
								m_log.error("run() : " + getFilePath() + " " + e.getMessage(), e);
							}
						}
						
						// ファイルの冒頭部分が異なれば別ファイルと判定
						if (!firstPart.equals(status.prefix)) {
							m_log.debug("run() : " + getFilePath() + " log rotation detected");
							logrotateFlag = true;
							m_log.debug("run() : m_logrotate set true .2");
						}
					}
				}
			}
		} catch (RuntimeException e) {
			m_log.error("run() : " + getFilePath() + " " + e.getMessage(), e);
		}
		
		return logrotateFlag;
	}
	
	public void setMonitor(MonitorInfoWrapper monitorInfo) {
		this.m_wrapper = monitorInfo;
	}
}