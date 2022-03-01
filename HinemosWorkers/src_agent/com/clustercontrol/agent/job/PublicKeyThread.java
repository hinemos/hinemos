/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.job;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.SetJobResultRequest;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.HinemosTime;

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
	private static final int PUBLIC_KEY_MAX_SIZE = 1024;
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
			AgtRunInstructionInfoResponse info,
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
		JobResultSendableObject sendme = new JobResultSendableObject();
		sendme.sessionId = m_info.getSessionId();
		sendme.jobunitId = m_info.getJobunitId();
		sendme.jobId = m_info.getJobId();
		sendme.facilityId = m_info.getFacilityId();
		sendme.body = new SetJobResultRequest();
		sendme.body.setCommand(m_info.getCommand());
		sendme.body.setCommandType(m_info.getCommandType());
		sendme.body.setStopType(m_info.getStopType());
		sendme.body.setSpecifyUser(m_info.getSpecifyUser());
		sendme.body.setUser(m_info.getUser());
		sendme.body.setStatus(RunStatusConstant.START);
		sendme.body.setTime(startDate.getTime());

		m_log.info("run SessionID=" + m_info.getSessionId() + ", JobID=" + m_info.getJobId());

		//送信
		m_sendQueue.put(sendme);

		if (m_info.getSpecifyUser().booleanValue()) {
			// ユーザを指定する場合
			execUser = m_info.getUser();
		} else {
			// エージェント実行ユーザを使用する場合
			execUser = CommandCreator.getSysUser();
		}

		if(m_info.getCommand().equals(CommandConstant.GET_PUBLIC_KEY)){
			// 公開鍵をagent.propertiesから取得し、コメントにsessionIdを追記します。
			String key = AgentProperties.getProperty(execUser.toLowerCase() + PUBLIC_KEY) + " " + m_info.getSessionId();
			m_log.debug("key:" + key);
			int keySize = key.length();
			if(key != null && keySize > 0 && keySize <= PUBLIC_KEY_MAX_SIZE){
				sendme.body.setStatus(RunStatusConstant.END);
				sendme.body.setPublicKey(key);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage("");
				sendme.body.setEndValue(0);
			}
			else{
				String message = "GET_PUBLIC_KEY is failure. " +
						"key=" + key + ", pub=" + execUser.toLowerCase() + PUBLIC_KEY;
				m_log.warn(message);
				sendme.body.setStatus(RunStatusConstant.ERROR);
				sendme.body.setPublicKey("");
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage(message);
				sendme.body.setEndValue(-1);
			}
		}
		else if(m_info.getCommand().equals(CommandConstant.ADD_PUBLIC_KEY)){
			//公開鍵設定
			if(addKey(m_info.getPublicKey())){
				sendme.body.setStatus(RunStatusConstant.END);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage("");
				sendme.body.setEndValue(0);
			}
			else{
				String message = "ADD_PUBLIC_KEY is falure.";
				m_log.warn(message);
				sendme.body.setStatus(RunStatusConstant.ERROR);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage(message);
				sendme.body.setEndValue(-1);
			}
		}
		else if(m_info.getCommand().equals(CommandConstant.DELETE_PUBLIC_KEY)){
			//公開鍵削除
			if(deleteKey(m_info.getPublicKey())){
				sendme.body.setStatus(RunStatusConstant.END);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage("");
				sendme.body.setEndValue(0);
			}
			else{
				String message = "DELETE_PUBLIC_KEY is falure.";
				m_log.warn(message);
				sendme.body.setStatus(RunStatusConstant.ERROR);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage(message);
				sendme.body.setEndValue(-1);
			}
		}

		//送信
		m_sendQueue.put(sendme);

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
		String propKey = execUser.toLowerCase() + AUTHORIZED_KEY_PATH;
		String fileName = AgentProperties.getProperty(propKey);
		
		if (fileName == null || fileName.length() == 0) {
			m_log.info(String.format("AgentProperty Authorizedkey %s = %s ", propKey, fileName));
			return false;
		}
		m_log.debug("Authorizedkey filepath: " + fileName);
			

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
				try {
					if (null != (lock = channel.tryLock())) {
						break;
					}
				} catch (OverlappingFileLockException e) {
					// OverlappingFileLockExceptionはログ出力だけして無視する
					m_log.info("addKey() : " + e.getClass().getSimpleName());
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
				Charset charset = Charset.forName("UTF-8");
				CharsetDecoder decoder = charset.newDecoder();
				// バッファを作成
				ByteBuffer readBuffer = ByteBuffer.allocate((int) channel.size());

				// ファイル読み込み
				channel.read(readBuffer);

				// リミットの値を現在位置の値と等しくし、位置を0に設定
				readBuffer.flip();

				// 文字列に変換
				String contents = decoder.decode(readBuffer).toString();
				// 公開鍵を取得
				List<String> keyCheck = new ArrayList<>();
				StringTokenizer tokenizer = new StringTokenizer(contents, "\n");
				while (tokenizer.hasMoreTokens()) {
					keyCheck.add(tokenizer.nextToken());
				}

				// 追加するpublicKeyがコメント含め既に登録済みか検証し、登録済みであれば追加しない。
				int s = keyCheck.lastIndexOf(publicKey);
				if (s != -1) {
					m_log.info("Duplicate publicKey. Skipped appending publicKey.");
					return true;
				}

				//ファイルポジションを最後に移動
				channel.position(channel.size());

				//追加文字列を取得
				String writeData = "\n" + publicKey;
				// ログ出力
				m_log.debug("add key : " + writeData);

				//書き込み用バッファを作成
				int bufferSize = writeData.getBytes().length;
				ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

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
	 *	公開鍵をAuthorized_keyから削除します。<BR>
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
				try {
					if (null != (lock = channel.tryLock())) {
						break;
					}
				} catch (OverlappingFileLockException e) {
					// OverlappingFileLockExceptionはログ出力だけして無視する
					m_log.info("addKey() : " + e.getClass().getSimpleName());
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
