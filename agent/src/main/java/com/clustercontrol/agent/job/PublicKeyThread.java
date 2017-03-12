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

package com.clustercontrol.agent.job;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.jobmanagement.RunResultInfo;

/**
 * 公開鍵操作用のスレッドクラス<BR>
 * 
 * Hinemosのファイル転送では、ファイル転送に使用するsshの
 * 公開鍵をジョブ実行の中でやり取りします。<BR>
 * その一連の操作（鍵の取得、鍵の追加、鍵の削除）を
 * 行います。
 *
 */
public class PublicKeyThread extends AgentThread {
	//ロガー
	private static Log m_log = LogFactory.getLog(PublicKeyThread.class);

	private static final String PUBLIC_KEY = ".public.key";
	private static final String AUTHORIZED_KEY_PATH = ".authorized.keys.path";

	private String execUser;

	// http://java.sun.com/javase/ja/6/docs/ja/api/java/nio/channels/FileChannel.html#tryLock()
	// ---
	// ファイルロックは Java 仮想マシン全体のために保持されます。これらは、同一仮想マシン内の複数スレッドによるファイルへのアクセスを制御するのには適していません。
	// ---
	public static final long FILELOCK_WAIT;
	public static final String FILELOCK_WAIT_DEFAULT = "1000";
	public static final String KEY_FILELOCK_WAIT = "job.filetransfer.wait";

	public static final long FILELOCK_TIMEOUT;
	public static final String FILELOCK_TIMEOUT_DEFAULT = "600000";
	public static final String KEY_FILELOCK_TIMEOUT = "job.filetransfer.timeout";

	private static final Object authKeyLock = new Object();

	public static final boolean SKIP_KEYFILE_UPDATE;
	public static final String SKIP_KEYFILE_UPDATE_DEFAULT = "false";
	public static final String KEY_SKIP_KEYFILE_UPDATE = "file.transfer.skip.keyfile.update";

	static {
		String fileLockWaitStr = AgentProperties.getProperty(KEY_FILELOCK_WAIT, FILELOCK_WAIT_DEFAULT);
		long fileLockWait = 1000L;

		try {
			fileLockWait = Long.parseLong(fileLockWaitStr);
		} catch (Exception e) {
			fileLockWait = Long.parseLong(FILELOCK_WAIT_DEFAULT);
		}
		FILELOCK_WAIT = fileLockWait;

		String fileLockTimeoutStr = AgentProperties.getProperty(KEY_FILELOCK_TIMEOUT, FILELOCK_TIMEOUT_DEFAULT);
		long fileLockTimeout = 600000L;

		try {
			fileLockTimeout = Long.parseLong(fileLockTimeoutStr);
		} catch (Exception e) {
			fileLockTimeout = Long.parseLong(FILELOCK_TIMEOUT_DEFAULT);
		}
		FILELOCK_TIMEOUT = fileLockTimeout;

		String skipKeyFileUpdateStr = AgentProperties.getProperty(KEY_SKIP_KEYFILE_UPDATE, SKIP_KEYFILE_UPDATE_DEFAULT);
		boolean skipKeyFileUpdate = false;

		skipKeyFileUpdate = "true".equals(skipKeyFileUpdateStr);
		SKIP_KEYFILE_UPDATE = skipKeyFileUpdate;

		m_log.info("initialized parameters : FILELOCK_WAIT = " + FILELOCK_WAIT
				+ " [msec], FILELOCK_TIMEOUT = " + FILELOCK_TIMEOUT
				+ " [msec], SKIP_KEYFILE_UPDATE = " + SKIP_KEYFILE_UPDATE);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param props
	 */
	public PublicKeyThread(
			RunInstructionInfo info,
			SendQueue sendQueue) {
		super(info, sendQueue);
	}

	/**
	 * run()から呼び出される公開鍵操作のメソッド<BR>
	 * 
	 */
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		m_log.debug("run start");

		Date startDate = HinemosTime.getDateInstance();

		//実行履歴に追加
		RunHistoryUtil.addRunHistory(m_info, RunHistoryUtil.dummyProcess());

		//---------------------------
		//-- 開始メッセージ送信
		//---------------------------

		//メッセージ作成
		RunResultInfo info = new RunResultInfo();
		info.setSessionId(m_info.getSessionId());
		info.setJobunitId(m_info.getJobunitId());
		info.setJobId(m_info.getJobId());
		info.setFacilityId(m_info.getFacilityId());
		info.setCommand(m_info.getCommand());
		info.setCommandType(m_info.getCommandType());
		info.setStopType(m_info.getStopType());
		info.setSpecifyUser(m_info.isSpecifyUser());
		info.setUser(m_info.getUser());
		info.setStatus(RunStatusConstant.START);
		info.setTime(startDate.getTime());

		m_log.info("run SessionID=" + m_info.getSessionId() + ", JobID=" + m_info.getJobId());

		//送信
		m_sendQueue.put(info);

		if (m_info.isSpecifyUser().booleanValue()) {
			// ユーザを指定する場合
			execUser = m_info.getUser();
		} else {
			// エージェント実行ユーザを使用する場合
			execUser = CommandCreator.getSysUser();
		}

		if(m_info.getCommand().equals(CommandConstant.GET_PUBLIC_KEY)){
			//公開鍵をagent.propertiesから取得します。
			String key = AgentProperties.getProperty(execUser.toLowerCase() + PUBLIC_KEY);
			m_log.debug("key:" + key);
			if(key != null && key.length() > 0){
				info.setStatus(RunStatusConstant.END);
				info.setPublicKey(key);
				info.setTime(HinemosTime.getDateInstance().getTime());
				info.setErrorMessage("");
				info.setMessage("");
				info.setEndValue(0);
			}
			else{
				String message = "GET_PUBLIC_KEY is failure. " +
						"key=" + key + ", pub=" + execUser.toLowerCase() + PUBLIC_KEY;
				m_log.warn(message);
				info.setStatus(RunStatusConstant.ERROR);
				info.setPublicKey("");
				info.setTime(HinemosTime.getDateInstance().getTime());
				info.setErrorMessage("");
				info.setMessage(message);
				info.setEndValue(-1);
			}
		}
		else if(m_info.getCommand().equals(CommandConstant.ADD_PUBLIC_KEY)){
			//公開鍵設定
			if(addKey(m_info.getPublicKey())){
				info.setStatus(RunStatusConstant.END);
				info.setTime(HinemosTime.getDateInstance().getTime());
				info.setErrorMessage("");
				info.setMessage("");
				info.setEndValue(0);
			}
			else{
				String message = "ADD_PUBLIC_KEY is falure.";
				m_log.warn(message);
				info.setStatus(RunStatusConstant.ERROR);
				info.setTime(HinemosTime.getDateInstance().getTime());
				info.setErrorMessage("");
				info.setMessage(message);
				info.setEndValue(-1);
			}
		}
		else if(m_info.getCommand().equals(CommandConstant.DELETE_PUBLIC_KEY)){
			//公開鍵削除
			if(deleteKey(m_info.getPublicKey())){
				info.setStatus(RunStatusConstant.END);
				info.setTime(HinemosTime.getDateInstance().getTime());
				info.setErrorMessage("");
				info.setMessage("");
				info.setEndValue(0);
			}
			else{
				String message = "DELETE_PUBLIC_KEY is falure.";
				m_log.warn(message);
				info.setStatus(RunStatusConstant.ERROR);
				info.setTime(HinemosTime.getDateInstance().getTime());
				info.setErrorMessage("");
				info.setMessage(message);
				info.setEndValue(-1);
			}
		}

		//送信
		m_sendQueue.put(info);

		//実行履歴から削除
		RunHistoryUtil.delRunHistory(m_info);

		m_log.debug("run end");
	}

	/**
	 * 公開鍵をAuthorized_keyに追加します。<BR>
	 * 
	 * @param publicKey
	 * @return
	 */
	private synchronized boolean addKey(String publicKey) {
		m_log.debug("add key start");

		if (SKIP_KEYFILE_UPDATE) {
			m_log.info("skipped appending publicKey");
			return true;
		}

		//ファイル名取得
		String fileName = AgentProperties.getProperty(execUser.toLowerCase() + AUTHORIZED_KEY_PATH);

		m_log.debug("faileName" + fileName);
		if(fileName == null || fileName.length() == 0)
			return false;

		//File取得
		File fi = new File(fileName);

		RandomAccessFile randomAccessFile = null;
		FileChannel channel = null;
		FileLock lock = null;
		boolean add = false;
		try {
			//RandomAccessFile作成
			randomAccessFile = new RandomAccessFile(fi, "rw");
			//FileChannel作成
			channel = randomAccessFile.getChannel();

			// ファイルをロック
			for (int i = 0; i < (FILELOCK_TIMEOUT / FILELOCK_WAIT); i++) {
				if (null != (lock = channel.tryLock())) {
					break;
				}
				m_log.info("waiting for locked file... [" + (i + 1) + "/" + (FILELOCK_TIMEOUT / FILELOCK_WAIT) + " : " + fileName + "]");
				Thread.sleep(FILELOCK_WAIT);
			}
			if (null == lock) {
				m_log.warn("file locking timeout.");
				return false;
			}


			// ファイルロック(スレッド間の排他制御)
			synchronized (authKeyLock) {
				//ファイルポジションを最後に移動
				channel.position(channel.size());

				//追加文字列を取得
				String writeData = "\n" + publicKey;
				// ログ出力
				m_log.debug("add key : " + writeData);

				//書き込み用バッファを作成
				ByteBuffer buffer = ByteBuffer.allocate(512);

				//書き込み
				buffer.clear();
				buffer.put(writeData.getBytes());
				buffer.flip();
				channel.write(buffer);
			}

			add = true;
		} catch (Exception e) {
			m_log.error(e);
		} finally {
			try {
				if (channel != null) {
					channel.close();
				}
				if (randomAccessFile != null) {
					randomAccessFile.close();
				}
				if (lock != null) {
					//ロックをリリース
					lock.release();
				}
			} catch (Exception e) {
			}
		}

		return add;
	}

	/**
	 *  公開鍵をAuthorized_keyから削除します。<BR>
	 * 
	 * @param publicKey
	 * @return true : 成功　false:失敗
	 */
	private synchronized boolean deleteKey(String publicKey) {
		m_log.debug("delete key start");

		if (SKIP_KEYFILE_UPDATE) {
			m_log.info("skipped deleting publicKey");
			return true;
		}

		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();
		CharsetDecoder decoder = charset.newDecoder();

		//ファイル名取得
		String fileName = AgentProperties.getProperty(execUser.toLowerCase() + AUTHORIZED_KEY_PATH);
		if(fileName == null || fileName.length() == 0)
			return false;

		//File取得
		File fi = new File(fileName);

		RandomAccessFile randomAccessFile = null;
		FileChannel channel = null;
		FileLock lock = null;
		boolean delete = false;
		try {
			//RandomAccessFile作成
			randomAccessFile = new RandomAccessFile(fi, "rw");
			//FileChannel作成
			channel = randomAccessFile.getChannel();

			// ファイルをロック
			for (int i = 0; i < (FILELOCK_TIMEOUT / FILELOCK_WAIT); i++) {
				if (null != (lock = channel.tryLock())) {
					break;
				}
				m_log.info("waiting for locked file... [" + (i + 1) + "/" + (FILELOCK_TIMEOUT / FILELOCK_WAIT) + " : " + fileName + "]");
				Thread.sleep(FILELOCK_WAIT);
			}
			if (null == lock) {
				m_log.warn("file locking timeout.");
				return false;
			}

			// ファイルロック(スレッド間の排他制御)
			synchronized (authKeyLock) {
				//バッファを作成
				ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());

				//ファイル読み込み
				channel.read(buffer);

				// リミットの値を現在位置の値と等しくし、位置を0に設定
				buffer.flip();

				//文字列に変換
				String contents = decoder.decode(buffer).toString();

				// デバッグ出力
				m_log.debug("contents " + contents.length() + " : " + contents);

				//公開鍵を取得
				List<String> keyCheck = new ArrayList<String>();
				StringTokenizer tokenizer = new StringTokenizer(contents, "\n");
				while (tokenizer.hasMoreTokens()) {
					keyCheck.add(tokenizer.nextToken());
				}

				//引数の鍵と一致したものを削除
				int s = keyCheck.lastIndexOf(publicKey);
				if(s != -1){
					// デバッグ出力
					m_log.debug("remobe key : " + keyCheck.get(s));
					keyCheck.remove(s);
				}

				//書き込み文字列の作成
				encoder.reset();
				buffer.clear();

				int i;
				if(keyCheck.size() > 0){
					for (i = 0 ; i < keyCheck.size() - 1 ; i++) {
						encoder.encode(CharBuffer.wrap(keyCheck.get(i) + "\n"), buffer, false);
					}
					encoder.encode(CharBuffer.wrap(keyCheck.get(i)), buffer, true);
				}

				//ファイル書き込み
				buffer.flip();
				channel.truncate(0);
				channel.position(0);
				channel.write(buffer);
			}

			delete = true;
		} catch (IOException e) {
			m_log.error(e.getMessage(), e);
		} catch (RuntimeException e) {
			m_log.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			m_log.error(e.getMessage(), e);
		} finally {
			try {
				if (channel != null) {
					channel.close();
				}
				if (randomAccessFile != null) {
					randomAccessFile.close();
				}
				//ロックをリリースする
				if (lock != null) {
					lock.release();
				}
			} catch (Exception e) {
			}
		}

		return delete;
	}
}
