/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.tasktray;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.bean.LoginParameter;
import com.clustercontrol.jobmanagement.rpa.bean.LoginTaskAbort;
import com.clustercontrol.jobmanagement.rpa.bean.LoginTaskEnd;
import com.clustercontrol.jobmanagement.rpa.util.CommandProxy;
import com.clustercontrol.jobmanagement.rpa.util.RoboFileManager;

/**
 * タスクトレイプログラムでmstscコマンドによる自動ログインを行うクラス<br>
 * RPAシナリオジョブから使用されます。
 * 
 * @see com.clustercontrol.jobmanagement.rpa.RpaJobLoginWorker
 * @see com.clustercontrol.platform.rpa.LoginExecutor
 */
public class LoginFileObserver {
	/** ロガー */
	private static final Log m_log = LogFactory.getLog(LoginFileObserver.class);
	/** マネージャとの連携用ファイル出力先ディレクトリ */
	private static final String loginFileDir = System.getProperty("datadir") + "\\rpa";
	/** ログイン指示ファイル確認スレッド名 */
	private static final String loginFileCheckThreadName = "LoginFileCheckThread";
	/** ログイン指示ファイル確認スレッドプール */
	private static ExecutorService fileCheckExecutor;
	/** ログインコマンド実行スレッド名 */
	private static final String loginThreadName = "LoginThread";
	/** ログインコマンド実行スレッドプール */
	private static ExecutorService loginExecutor;
	/** ログインコマンド実行スレッドプールサイズ */
	private static final int loginThreadSize;
	/** ログイン指示ファイル確認チェック間隔 */
	private static final int checkInterval;
	/** タスクトレイプログラムのプロパティ */
	private static Properties properties = new Properties();
	/** ログイン処理を中断する際に使用するFutureオブジェクトのキャッシュ */
	private static Map<String, Future<Void>> futureCache = new ConcurrentHashMap<>();

	static {
		String homeDir = System.getProperty("homedir");
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(homeDir + "\\etc\\hinemos_tasktray.properties");
			properties.load(stream);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}

		loginThreadSize = Integer.parseInt(properties.getProperty("login.thread.size", "10"));
		m_log.debug("static : loginThreadSize=" + loginThreadSize);
		checkInterval = Integer.parseInt(properties.getProperty("file.check.interval", "10000"));
		m_log.debug("static : checkInterval=" + checkInterval);

		fileCheckExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, loginFileCheckThreadName);
				thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						m_log.error("uncaughtException() : " + e.getMessage(), e);
					}
				});
				return thread;
			}
		});

		loginExecutor = Executors.newFixedThreadPool(loginThreadSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, loginThreadName);
				thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						m_log.error("uncaughtException() : " + e.getMessage(), e);
					}
				});
				return thread;
			}
		});
	}

	/**
	 * ログイン指示ファイルの生成を定期的に確認するクラス<br>
	 * ログイン指示ファイルがある場合、ログイン実行スレッドを起動します。
	 */
	static class FileCheckTask implements Runnable {

		@Override
		public void run() {
			m_log.info("run()");
			m_log.debug("run() : loginFileDir=" + loginFileDir + ", checkInterval=" + checkInterval);
			while (true) {
				try {
					Path dir = Paths.get(loginFileDir);
					if (!dir.toFile().exists()) {
						m_log.debug("run() : loginFileDir doesn't exist");
						continue;
					}
					Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							m_log.debug("visitFile() : file=" + file.toString());
							String fileName;
							Path fileNamePath = file.getFileName();
							if (fileNamePath != null) {
								fileName = fileNamePath.toString();
								m_log.debug("visitFile() : fileName=" + fileName);
							} else {
								m_log.warn("visitFile() : fileName is null");
								return super.visitFile(file, attrs);
							}

							String ipAddressDir;
							Path parent = file.getParent();
							if (parent != null) {
								ipAddressDir = parent.toString();
								m_log.debug("visitFile() : ipAddressDir=" + ipAddressDir);
							} else {
								m_log.warn("visitFile() : parent is null");
								return super.visitFile(file, attrs);
							}

							if (fileName.equals(RoboFileManager.getFileName(LoginParameter.class))) {
								// ログインを実行
								m_log.info("run() : execute login task");
								Future<Void> future = loginExecutor.submit(new LoginTask(ipAddressDir));
								futureCache.put(ipAddressDir, future);
							} else if (fileName.equals(RoboFileManager.getFileName(LoginTaskAbort.class))) {
								// ログインを中断
								m_log.info("run() : abort login task");
								Future<Void> future = futureCache.get(ipAddressDir);
								if (future != null) {
									future.cancel(true);
								}
							}
							return super.visitFile(file, attrs);
						}
					});
					m_log.debug("run() : sleep for " + checkInterval + " ms");
				} catch (IOException e) {
					m_log.error("run() : " + e.getMessage(), e);
				} finally {
					try {
						// ファイル出力先ディレクトリが存在しない等の場合もsleepが実行されるようにする
						Thread.sleep(checkInterval);
					} catch (InterruptedException e) {
						m_log.info("run() : thread interrupted", e);
					}
				}
			}
		}
	}

	/**
	 * mstscコマンドによる自動ログインを行います。<br>
	 * 以下の流れで自動ログインを行います。
	 * <ol>
	 * <li>認証情報の端末への一時保存</li>
	 * <li>mstscコマンドの実行</li>
	 * <li>保存した認証情報の削除</li>
	 * <ol>
	 *
	 */
	static class LoginTask implements Callable<Void> {
		/** マネージャとの連携用ファイル出力先ディレクトリ */
		private String loginFileDir;
		/** 連携用ファイル入出力用クラス */
		private RoboFileManager roboFileManager;
		/** ログイン情報 */
		private LoginParameter param;
		/** mstscコマンド */
		private String loginCommand;
		/** 認証情報一時保存コマンド */
		private String addKeyCommand;
		/** 認証情報削除コマンド */
		private String deleteKeyCommand;

		/**
		 * コンストラクタ
		 * 
		 * @param loginFileDir
		 *            マネージャとの連携用ファイル出力先ディレクトリ
		 */
		public LoginTask(String loginFileDir) {
			this.loginFileDir = loginFileDir;
			this.roboFileManager = new RoboFileManager(loginFileDir);
		}

		@Override
		public Void call() {
			m_log.info("run()");
			boolean interrupted = false; // Future#cancelではThread.currentThread().isInterrupted()がtrueにならないためフラグを使用
			try {
				// ログイン指示ファイルの読み取り
				param = roboFileManager.read(LoginParameter.class, checkInterval);
				// ログイン指示ファイルを削除
				roboFileManager.delete(LoginParameter.class);
				loginCommand = String.format(properties.getProperty("login.command", "mstsc /v:%s /w:%s /h:%s"),
						param.getIpAddress(), param.getWidth(), param.getHeight());
				addKeyCommand = String.format(
						properties.getProperty("key.add.command", "cmdkey /add:%s /user:%s /pass:%s"),
						param.getIpAddress(), param.getUserId(), param.getPassword());
				deleteKeyCommand = String.format(properties.getProperty("key.delete.command", "cmdkey /delete:%s"),
						param.getIpAddress());
			} catch (IOException e) {
				m_log.error("run() : " + e.getMessage(), e);
			}

			// ログイン処理を実行
			try {
				// 認証情報を一時的に保存
				if (m_log.isDebugEnabled()) {
					m_log.debug("run() : addKeyCommand=" + addKeyCommand.replace(param.getPassword(), "xxx"));
				}
				if (CommandProxy.execute(addKeyCommand) == null) {
					// interruptされた場合
					interrupted = true;
					return null;
				}
				m_log.debug("run() : loginCommand=" + loginCommand);
				// ログインコマンドを実行、プロセスが終了するまで待機する
				// mstscコマンドは認証情報の誤り等の場合もプロセスが終了せずにダイアログで停止するログインが成功した場合との区別がつかない。
				// 上記の場合はRPAシナリオジョブのログインされていない場合の通知でユーザに知らせる。
				if (CommandProxy.execute(loginCommand) == null) {
					// interruptされた場合
					interrupted = true;
					return null;
				}

				// 認証情報を削除
				m_log.debug("run() : deleteKeyCommand=" + deleteKeyCommand);
				if (CommandProxy.execute(deleteKeyCommand) == null) {
					// interruptされた場合
					interrupted = true;
					return null;
				}
			} catch (HinemosUnknown e) {
				m_log.error("run() : " + e.getMessage(), e);
			} finally {
				try {
					if (!interrupted) {
						// ログイン処理の終了をマネージャに通知するファイルを生成する
						m_log.info("run() : login task end");
						roboFileManager.write(new LoginTaskEnd(loginFileDir));
					} else {
						// コマンド終了待ちが中断された場合
						// ログイン中断指示ファイルを削除
						m_log.info("run() : login task interrupted");
						roboFileManager.delete(LoginTaskAbort.class);

					}
				} catch (IOException e) {
					m_log.error("run() : " + e.getMessage(), e);
				}
				// キャッシュを削除
				futureCache.remove(loginFileDir);
			}
			return null;
		}
	}

	/**
	 * ログイン指示ファイル生成確認を開始します。
	 */
	public void start() {
		m_log.info("start()");
		fileCheckExecutor.submit(new FileCheckTask());
	}

	/**
	 * ログイン指示ファイル生成確認を終了します。
	 */
	public void shutdown() {
		m_log.info("shutdown()");
		fileCheckExecutor.shutdown();
		loginExecutor.shutdown();
	}
}
