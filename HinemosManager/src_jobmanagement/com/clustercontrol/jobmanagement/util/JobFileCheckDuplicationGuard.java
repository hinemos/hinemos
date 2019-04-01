/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;

/**
 * エージェントから同一ファイルチェック情報が複数回送信されてきた場合の、ジョブ重複起動を防止します。
 *
 * @since 6.2.0
 */
public class JobFileCheckDuplicationGuard implements Closeable {

	// トリガ情報の最大文字数
	private static final int MAXLEN_TRIGGER_INFO = 256;
	// ファイル名にユニークIDを付与する際のデリミタ文字列
	private static final String ID_DELIMITER = "/";
	// ユニークIDの長さ
	private static final int ID_LENGTH = 24;
	// ロック取得時のタイムアウト時間(秒)
	private static final int LOCK_TIMEOUT = 20;

	// logger
	private static final Log log = LogFactory.getLog(JobFileCheckDuplicationGuard.class);
	// 移行中（ジョブセッション生成タスク登録から永続化完了まで)のトリガ情報
	private static final Map<String, String> transitTriggerInfos = new ConcurrentHashMap<>();
	// 重複チェックを直列化するためのロック
	private static final ReentrantLock lock = new ReentrantLock();

	// ユニークID
	private String uniqueId;
	// トリガ情報 (用途: ビューへの表示、ジョブ変数、ジョブ重複起動の検知キー)
	private String triggerInfo;
	// ジョブセッションID
	private String sessionId;

	/**
	 * ジョブセッション生成タスクにより永続化されるまで、トリガ情報をメモリ上で一時的に管理します。
	 * 
	 * @param triggerInfo 登録するトリガ情報。
	 * @param jobSessionId ジョブセッションID。
	 */
	public static void putTransitTriggerInfo(String triggerInfo, String jobSessionId) {
		if (isNullOrEmpty(triggerInfo) || isNullOrEmpty(jobSessionId)) {
			log.warn(String.format("putWaitingTriggerInfo : Empty. triggerInfo=%s, jobSessionId=%s", triggerInfo,
					jobSessionId));
			return;
		}

		// 呼び出しスレッドがロック獲得中であること (マネージャ起動時のタスク復元の場合は問題ない)
		if (!lock.isHeldByCurrentThread()) {
			log.info("putWaitingTriggerInfo : Called in a lock-less context.");
		}

		transitTriggerInfos.put(triggerInfo, jobSessionId);

		if (log.isDebugEnabled()) {
			log.debug(String.format("putWaitingTriggerInfo : triggerInfo=%s, jobSessionId=%s", triggerInfo,
					jobSessionId));
		}
	}

	/**
	 * メモリ上で一時的に管理しているトリガ情報を削除します。
	 * トリガ情報の永続化が完了した際に呼び出されます。
	 * 
	 * @param triggerInfo 削除するトリガ情報。
	 */
	public static void removeTransitTriggerInfo(String triggerInfo) {
		if (isNullOrEmpty(triggerInfo)) {
			log.warn(String.format("removeWaitingTriggerInfo : Empty. triggerInfo=%s", triggerInfo));
			return;
		}

		transitTriggerInfos.remove(triggerInfo);

		if (log.isDebugEnabled()) {
			log.debug(String.format("removeWaitingTriggerInfo : triggerInfo=%s", triggerInfo));
		}
	}

	/**
	 * インスタンスを生成します。
	 * また、内部的にロックを取得し、{@link #close()} が呼ばれるまで、他のインスタンスの生成をブロックします。
	 * 
	 * @param jobFileCheck エージェントが送信してきたファイルチェック情報。
	 *            ファイル名にIDが付与されていた場合、IDを除去した値に更新します。
	 * @throws HinemosUnknown ロック取得時にタイムアウト、あるいは割り込みが発生した。
	 */
	public JobFileCheckDuplicationGuard(JobFileCheck jobFileCheck) throws HinemosUnknown {
		uniqueId = extractUniqueId(jobFileCheck);
		triggerInfo = createTriggerInfo(jobFileCheck, uniqueId);
		lock();
		sessionId = findJobSession(external, uniqueId, triggerInfo);

		if (log.isDebugEnabled()) {
			log.debug(String.format("JobFileCheckDuplicationGuard : triggerInfo=%s, jobSessionId=%s", triggerInfo,
					sessionId));
		}
	}

	/**
	 * 取得したロックを解放し、このインスタンスの利用を停止します。
	 */
	@Override
	public void close() {
		unlock();

		if (log.isDebugEnabled()) {
			log.debug(String.format("close : triggerInfo=%s, jobSessionId=%s", triggerInfo, sessionId));
		}
	}

	/**
	 * ジョブセッション生成済み、あるいは生成タスクの登録済みの場合、ジョブセッションIDを返します。
	 * 
	 * @return ジョブセッション生成済みの場合はジョブセッションID、そうでない場合はnull。
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * ファイルチェックのユニークIDを返します。
	 * 
	 * @return ユニークID、付与されていなかった場合はnull。
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * トリガ情報を返します。
	 * 
	 * @return トリガ情報文字列。
	 */
	public String getTriggerInfo() {
		return triggerInfo;
	}

	// ロックを取得する
	private static void lock() throws HinemosUnknown {
		try {
			if (!lock.tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				log.warn("lock : Timed out.");
				throw new HinemosUnknown("JobFileCheckDuplicationGuard: Lock aquiring timed out.");
			}
		} catch (InterruptedException e) {
			log.warn("lock : Interrupted.", e);
			throw new HinemosUnknown("JobFileCheckDuplicationGuard: Lock aquiring interrupted.", e);
		}
	}

	// ロックを解放する
	private static void unlock() {
		lock.unlock();
	}

	// ファイルチェック情報からユニークIDを抽出する
	private static String extractUniqueId(JobFileCheck jobFileCheck) {
		String filename = jobFileCheck.getFileName();
		int delimPos = filename.length() - ID_LENGTH - ID_DELIMITER.length();
		// デリミタがあるはずの位置にデリミタがあるなら、ユニークIDが付与されていると判定
		if (delimPos > 0 && filename.substring(delimPos, delimPos + ID_DELIMITER.length()).equals(ID_DELIMITER)) {
			jobFileCheck.setFileName(filename.substring(0, delimPos));
			return filename.substring(delimPos + ID_DELIMITER.length());
		} else {
			return null;
		}
	}

	// ファイルチェック情報からトリガ情報を生成する
	private static String createTriggerInfo(JobFileCheck jobFileCheck, String uniqueId) {
		StringBuilder trig = new StringBuilder();
		trig.append(jobFileCheck.getName());
		trig.append("(");
		trig.append(jobFileCheck.getId());
		trig.append(")");
		if (uniqueId != null) {
			trig.append(" ID=");
			trig.append(uniqueId);
		}
		trig.append(" file=");
		trig.append(jobFileCheck.getFileName());

		if (trig.length() > MAXLEN_TRIGGER_INFO) {
			return trig.substring(0, MAXLEN_TRIGGER_INFO);
		} else {
			return trig.toString();
		}
	}

	// 重複チェック
	private static String findJobSession(External external, String uniqueId, String triggerInfo) {
		if (uniqueId == null) return null;

		// メモリ上の管理情報をチェック
		String sessionId = transitTriggerInfos.get(triggerInfo);
		if (sessionId != null) {
			log.debug("findJobSession : Found in job session creation tasks.");
			return sessionId;
		}

		// DB上のジョブセッションをチェック
		List<JobSessionEntity> queryResult = external.queryJobSession(triggerInfo);
		if (queryResult.size() > 0) {
			if (queryResult.size() > 1) {
				String sessions = "";
				for (JobSessionEntity it : queryResult) {
					if (sessions.length() > 0) sessions += ",";
					sessions += it.getSessionId();
				}
				log.info("findJobSession : Multiple job sessions found. Ids=" + sessions);
			}
			log.debug("findJobSession : Found in job session entities.");
			return queryResult.get(0).getSessionId();
		}

		return null;
	}

	// どっかに同じものがあるんじゃないかな・・・
	private static boolean isNullOrEmpty(CharSequence cs) {
		return (cs == null || cs.length() == 0);
	}

	// 外部依存動作のモック置換を可能にする
	External external = new External();
	static class External {

		// DBのジョブセッションからトリガ情報を探して返す
		List<JobSessionEntity> queryJobSession(String triggerInfo) {
			return QueryUtil.getJobSessionByTriggerInfo(triggerInfo);
		}

	}

}
