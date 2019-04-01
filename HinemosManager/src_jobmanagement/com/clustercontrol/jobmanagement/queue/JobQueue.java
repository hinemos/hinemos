/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.factory.FullJob;
import com.clustercontrol.jobmanagement.factory.JobSessionJobImpl;
import com.clustercontrol.jobmanagement.factory.SelectJob;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueContentsViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueContentsViewInfo.JobQueueContentsViewInfoListItem;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueReferrerViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueReferrerViewInfo.JobQueueReferrerViewInfoListItem;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSetting;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueEntity;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueExecutor;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueItemEntity;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueItemEntityPK;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueItemStatus;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueTx;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.Singletons;

/**
 * ジョブキューはジョブ詳細(Session Job)レベルでの同時実行制御を提供します。
 * <p>
 * ただし、ジョブキューが提供するのは、「ジョブ詳細のジョブキュー内における状態(*1)の管理」と、
 * 「ジョブキュー内でACTIVE状態になったジョブ詳細の実行」です。
 * <p>
 * *1 この「状態」とは、{@link JobQueueItemStatus}のことです。{@link StatusConstant}ではありません。
 * <p>
 * 具体的な機能は以下のとおりです。
 * <ul>
 * <li>ジョブキューにはジョブ詳細を登録します。
 * <li>登録されたジョブ詳細は、最初はSTANDBY(待機状態)となります。
 * <li>ジョブキューに設定されている同時実行可能数まで、登録された時刻順に、ジョブ詳細は自動的にACTIVE(実行状態)になります。
 * <li>ACTIVEになったジョブ詳細は、実行中(TYPE_RUNNING)状態へ遷移します。
 * <li>STANDBYのジョブ詳細を、PENDING(中断状態)へ変更することができます。
 * <li>PENDINGとなったジョブ詳細は、解除されてSTANDBYへ戻るまで、ACTIVEになることはありません。
 * </ul>
 * 
 * @since 6.2.0
 */
public class JobQueue {
	private static final Log log = LogFactory.getLog(JobQueue.class);

	/**
	 * キューアイテムのアクティブ化タスクにおいて、原因不明のNPEが出た場合のリトライ回数。
	 * この値が"1"のとき、初回実行のみ(リトライなし)とする。
	 * つまり"3"なら2回まで再試行する。
	 */
	private static final int NPE_MAX_RETRY = 3; 

	private External external;

	/**
	 * このジョブキューが有効であるかを示すフラグ。
	 * 削除された({@link JobQueueContainer}から除去された)ジョブキューはfalseになる。
	 * 削除されたジョブキューに対する操作を防御するために使用する。
	 */
	private boolean valid;

	/**
	 * 状態変更時に使用するロック。
	 * thisで代用しても良いが、コードの可読性を上げるために専用オブジェクトとする。
	 */
	private Object lock;

	private String queueId;
	private String queueName;
	private int concurrency;
	private String ownerRoleId;

	/**
	 * アクティブ化タスクの実行を待機しているジョブのリスト。
	 * アクティブ化可能と判断してから、実際にアクティブになるまで時間差があるため、
	 * アクティブ化の余地があるかどうかをチェックする際に必要となる。
	 */
	private Set<JobQueueItemEntityPK> toBeActiveList;

	/**
	 * 最後に{@link #getFreezingTime()}を呼んだときにアクティブだったジョブ(キューアイテム)のリスト。
	 */
	private Set<JobKey> lastActiveJobs;

	/**
	 * {link {@link #lastActiveJobs}を更新したときの時刻。
	 */
	private long lastActiveJobsChangedTime;

	/**
	 * コンストラクタではなく、{@link JobQueueContainer#create(JobQueueEntity)}を使用してください。
	 */
	JobQueue(JobQueueEntity entity) {
		this(new External(), entity);
	}

	JobQueue(External external, JobQueueEntity entity) {
		this.external = external;
		this.queueName = entity.getName();
		this.valid = true;
		this.concurrency = entity.getConcurrency().intValue();
		this.ownerRoleId = entity.getOwnerRoleId();
		this.lock = new Object();
		this.queueId = entity.getQueueId();
		this.lastActiveJobs = new HashSet<>();
		this.lastActiveJobsChangedTime = 0;
		this.toBeActiveList = new HashSet<>();

		log.info("ctor: Created from entity=" + entity.toShortString());
	}

	/**
	 * 以降のこのジョブキューへの操作を無効にします。
	 */
	public void invalidate() {
		valid = false;
		log.info("invalidate:");
	}
	
	/**
	 * このジョブキューが内部状態を変更する際に
	 * synchronized文で使用するロックオブジェクトを返します。
	 */
	public Object getLock() {
		return lock;
	}

	/**
	 * IDを返します。
	 */
	public String getId() {
		return queueId;
	}

	/**
	 * オーナーロールIDを返します。
	 */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	/**
	 * 実行領域と待機領域を合わせたキューの総サイズを返します。11
	 */
	public long getSize() {
		long result;
		try (JobQueueTx tx = external.newTransaction()) {
			result = tx.countJobQueueItem(queueId);
			tx.commit();
		}
		return result;
	}

	/**
	 * キューへジョブを追加します。
	 * 追加されたジョブはキュー内部で待機状態となります。
	 * ジョブに通知設定が行われている場合は、待機開始通知を行います。
	 * 
	 * @param sessionId 追加するジョブのジョブセッションID。
	 * @param jobunitId 追加するジョブのジョブユニットID。
	 * @param jobId 追加するジョブのジョブID。
	 * @throws JobQueueLimitExceededException キューの格納上限を超えました。
	 */
	public void add(String sessionId, String jobunitId, String jobId) throws JobQueueLimitExceededException {
		if (!valid) return;

		synchronized (lock) {
			JobQueueItemEntityPK id = new JobQueueItemEntityPK(queueId, sessionId, jobunitId, jobId);

			try (JobQueueTx tx = external.newTransaction()) {
				// 上限到達チェック
				long size = tx.countJobQueueItem(queueId);
				int limit = external.getJobQueueSize();
				if (size >= limit) {
					throw new JobQueueLimitExceededException(
							String.format("Rejected %s due to count[%d] exceeded limit[%d].", id, size, limit));
				}

				if (tx.findJobQueueItemEntity(id) != null) {
					log.info("add: Already exists. id=" + id);
					return;
				}

				JobQueueItemEntity entity = new JobQueueItemEntity(id);
				entity.setStatus(JobQueueItemStatus.STANDBY);
				entity.setRegDate(HinemosTime.currentTimeMillis());
				tx.persist(entity);

				// 待機開始通知
				// - ジョブから出る他の通知との出力順を正しくするため、postCloseコールバックで実行する。
				tx.addCallback(new PostCloseCallback() {
					@Override
					public void postClose() {
						JobQueue.this.notify(MessageConstant.MESSAGE_JOBQUEUE_NOTIFY_STANDBY, sessionId, jobunitId,
								jobId);
					}
				}); 

				tx.commit();
				log.info("add: id=" + id);
			}
		}
		activateJobs();
	}

	/**
	 * キューからジョブを除去します。
	 * 
	 * @param sessionId 除去するジョブのジョブセッションID。
	 * @param jobunitId 除去するジョブのジョブユニットID。
	 * @param jobId 除去するジョブのジョブID。
	 */
	public void remove(String sessionId, String jobunitId, String jobId) {
		if (!valid) return;

		synchronized (lock) {
			JobQueueItemEntityPK id = new JobQueueItemEntityPK(queueId, sessionId, jobunitId, jobId);

			try (JobQueueTx tx = external.newTransaction()) {
				JobQueueItemEntity entity = tx.findJobQueueItemEntity(id);
				// キューに入る前(待機状態のジョブを停止した場合)、キューから出た後(開始遅延後に終了)などで、
				// 普通にnullになる可能性がある。
				if (entity == null) {
					log.info("remove: Nothing to do. " + id);
					return;
				}

				tx.remove(entity);
				tx.commit();
				log.info("remove: id=" + id);
			}
		}
		activateJobs();
	}

	/**
	 * STANDBY状態のジョブを、PENDING状態にします。
	 * <p>
	 * 本メソッドが変更するのはあくまでもキュー内部の状態です。
	 * ジョブセッション側の中断処理は呼び出し元が行う必要があります。
	 * これは、既存のジョブ中断処理コードを踏まえた上での実装であることによる制限です。
	 * 
	 * @param sessionId 中断するジョブのジョブセッションID。
	 * @param jobunitId 中断するジョブのジョブユニットID。
	 * @param jobId 中断するジョブのジョブID。
	 * @throws JobQueueItemNotFoundException 指定されたジョブがキューに存在しませんでした。
	 */
	public void suspend(String sessionId, String jobunitId, String jobId) throws JobQueueItemNotFoundException {
		if (!valid) return;

		synchronized (lock) {
			JobQueueItemEntityPK id = new JobQueueItemEntityPK(queueId, sessionId, jobunitId, jobId);

			try (JobQueueTx tx = external.newTransaction()) {
				JobQueueItemEntity entity = tx.findJobQueueItemEntity(id);
				if (entity == null) {
					throw new JobQueueItemNotFoundException(id.toString());
				}

				switch (entity.getStatus()) {
				case ACTIVE:
					throw new IllegalStateException("Attempt to suspend active item. id=" + id);
				case STANDBY:
					entity.setStatus(JobQueueItemStatus.PENDING);
					break;
				case PENDING:
					/* NOP */
					break;
				}
				tx.commit();
				log.info("suspend: id=" + id);
			}
		}
	}

	/**
	 * PENDING状態のジョブをSTANDBY状態へ戻します。
	 * <p>
	 * 本メソッドが変更するのはあくまでもキュー内部の状態です。
	 * ジョブセッション側の中断解除処理は呼び出し元が行う必要があります。
	 * これは、既存のジョブ中断解除処理コードを踏まえた上での実装であることによる制限です。
	 * 
	 * @param sessionId 復帰するジョブのジョブセッションID。
	 * @param jobunitId 復帰するジョブのジョブユニットID。
	 * @param jobId 復帰するジョブのジョブID。
	 * @throws JobQueueItemNotFoundException 指定されたジョブがキューに存在しませんでした。
	 * @throws IllegalStateException 実行領域のジョブを復帰しようとしました。
	 */
	public void resume(String sessionId, String jobunitId, String jobId) throws JobQueueItemNotFoundException {
		if (!valid) return;

		synchronized (lock) {
			JobQueueItemEntityPK id = new JobQueueItemEntityPK(queueId, sessionId, jobunitId, jobId);

			try (JobQueueTx tx = external.newTransaction()) {
				JobQueueItemEntity entity = tx.findJobQueueItemEntity(id);
				if (entity == null) {
					throw new JobQueueItemNotFoundException(id.toString());
				}

				switch (entity.getStatus()) {
				case ACTIVE:
					throw new IllegalStateException("Attempt to resume active item. id=" + id);
				case STANDBY:
					/* NOP */
					break;
				case PENDING:
					entity.setStatus(JobQueueItemStatus.STANDBY);
					break;
				}
				tx.commit();
				log.info("resume: id=" + id);
			}
		}
		activateJobs();
	}

	/**
	 * 実行枠に空きがあり、STANDBY状態のジョブがあれば、アクティブ化(ジョブを開始し、ACTIVE状態に変更)します。
	 * ジョブに通知設定が行われている場合は、待機終了通知を行います。
	 * <p>
	 * ジョブセッションのデッドロック(後述)を避けるため、本メソッド内では候補選定のみ行い、
	 * 実際のアクティブ化処理は非同期実行します。
	 * <p> 
	 * <strong>デッドロックについて</strong><br/>
	 * ジョブの実行開始を同期で行った場合、本メソッドの制御下においてジョブセッションをロックします。
	 * もし、あるジョブセッションAの処理中(当該ジョブセッションのロックを所持中)に本メソッドが呼ばれ、
	 * 他のジョブセッションBのジョブを開始することになった場合、Bのロックを追加で獲得する形となります。
	 * 並列して、ジョブセッションBを処理中のスレッドが本メソッドを呼んで、Aのロックを獲得しなければならなくなった場合、
	 * デッドロックとなります。
	 */
	public void activateJobs() {
		if (!valid) return;

		synchronized (lock) {
			// タスク登録対象のリスト
			List<JobQueueItemEntityPK> targets = new ArrayList<>();

			try (JobQueueTx tx = external.newTransaction()) {
				long activeCountInDb = tx.countJobQueueItemByStatus(queueId, JobQueueItemStatus.ACTIVE.getId());
				long activeCount = activeCountInDb + toBeActiveList.size();

				log.debug("activateJobs: QID=" + queueId + ", DB=" + activeCountInDb + ", TOBE=" + toBeActiveList.size()
						+ ", CON=" + concurrency);
				if (activeCount >= concurrency) return;

				// 同時実行可能数に到達するまでアクティブ化リストへの追加候補をリストアップする
				for (JobQueueItemEntity job : tx.findJobQueueItemEntitiesByStatus(queueId,
						JobQueueItemStatus.STANDBY.getId())) {
					JobQueueItemEntityPK id = job.getId();
					if (toBeActiveList.contains(id)) continue;
					toBeActiveList.add(id);
					targets.add(id);
					log.debug("activateJobs: " + id + " will be active.");
					if (++activeCount >= concurrency) break;
				}
				
				// 先行するジョブの終了によりアクティブになったケースで、
				// 先行ジョブの終了通知(postCloseで実行される)の後からジョブを開始したいので、
				// postCloseコールバックでタスクを登録する。
				tx.addCallback(new PostCloseCallback() {
					@Override
					public void postClose() {
						for (JobQueueItemEntityPK target : targets) {
							Singletons.get(JobQueueExecutor.class).execute(new ItemActivationTask(target));
							log.info("postClose: " + target + " activation task enqueued.");
						}
					}
				});

				tx.commit();
			}
		}
	}
	
	/**
	 * 指定されたジョブを、実行枠の空きに関係なく、アクティブ化(ジョブを開始し、ACTIVE状態に変更)します。
	 * ジョブに通知設定が行われている場合は、待機終了通知を行います。
	 * <p>
	 * ジョブの実行は、ジョブセッションへのアクセスが必要(ロック獲得が必要)となるため、非同期処理です。
	 * 詳しくは{@link #activateJobs()}を参照してください。
	 * 
	 * @param sessionId 実行開始するジョブのジョブセッションID。
	 * @param jobunitId 実行開始するジョブのジョブユニットID。
	 * @param jobId 実行開始するジョブのジョブID。
	 */
	public void forceActivateJob(String sessionId, String jobunitId, String jobId) {
		if (!valid) return;

		synchronized (lock) {
			JobQueueItemEntityPK target = new JobQueueItemEntityPK(queueId, sessionId, jobunitId, jobId);
	
			// このメソッドは一連のジョブの流れとは無関係にジョブの状態を変更するため、
			// 通知の前後関係を気にする必要はない(＝ 特にpostCloseで処理を行う必要はない)。
			toBeActiveList.add(target);
			log.debug("forceActivateJob: " + target + " will be active.");
			Singletons.get(JobQueueExecutor.class).execute(new ItemActivationTask(target));
		}
	}

	/**
	 * キューの状態が固定化している(以下の条件を満たす)かどうかをチェックし、
	 * 「最後に固定化していないことを検知した時刻からの経過時間」を返します。
	 * <ul>
	 * <li>同時実行可能数と同数以上のジョブがACTIVE状態である。
	 * <li>前回の呼び出しから、ACTIVE状態のジョブが変化していない。
	 * </ul>
	 * 
	 * @return 経過時間(ミリ秒)。固定化していない場合は 0。
	 */
	public long getFreezingTime() {
		long result = 0;
		synchronized (lock) {
			Set<JobKey> previous = lastActiveJobs;
			Set<JobKey> current = new HashSet<>();
	
			try (JobQueueTx tx = external.newTransaction()) {
				for (JobQueueItemEntity it : tx.findJobQueueItemEntitiesByStatus(queueId,
						JobQueueItemStatus.ACTIVE.getId())) {
					JobQueueItemEntityPK id = it.getId();
					current.add(new JobKey(id.getSessionId(), id.getJobunitId(), id.getJobId(), it.getRegDate()));
				}
	
				long now = external.currentTimeMillis();
				if (lastActiveJobsChangedTime == 0 || !current.equals(previous)) {
					// 初回呼び出し or 変化あり -> 最終変更確認時刻を更新
					lastActiveJobsChangedTime = now;
					log.debug("getFreezingTime: Changed. QID=" + queueId);
				} else {
					// 前回から変化していない
					// ACTIVEなジョブで同時実行可能枠が埋まっている状態であるか判定する
					if (current.size() >= concurrency) {
						// 固定化しているので、差分時刻を算出
						result = now - lastActiveJobsChangedTime;
						log.debug("getFreezingTime: Freezing since " + lastActiveJobsChangedTime + ". QID=" + queueId
								+ ", Filled (" + " " + current.size() + " >= " + concurrency + ")");
					} else {
						log.debug("getFreezingTime: Freezing since " + lastActiveJobsChangedTime + ". QID=" + queueId
								+ ", Not filled (" + " " + current.size() + " < " + concurrency + ")");
					}
				}
				tx.commit();
			}
			lastActiveJobs = current;
		}
		return result;
	}

	/**
	 * 設定用の情報を返します。
	 * <p>
	 * クライアント向けのメソッドであり、ユーザが所属しているロールから参照可能な場合のみ情報を返します。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 * 
	 * @throws JobQueueNotFoundException 参照可能なジョブキューの情報が存在しませんでした。
	 */
	public JobQueueSetting getSetting() throws JobQueueNotFoundException {
		try (JobQueueTx tx = new JobQueueTx()) {
			JobQueueEntity entity = tx.findJobQueueEntitiyForRead(queueId);
			if (entity == null) {
				// 権限がないジョブキューへアクセスしたか、
				// 「コンテナからキュー取得 -> キュー削除 -> 本メソッド」の流れでnullになりうる。
				throw new JobQueueNotFoundException("queueId=" + queueId);
			}
			tx.commit();
			return new JobQueueSetting(entity);
		}
	}

	/**
	 * 設定を変更します。<br/>
	 * ただし、以下の項目のみ変更が有効です。
	 * <ul>
	 * <li>ジョブキュー名
	 * <li>同時実行可能数
	 * </ul>
	 * <p>
	 * クライアント向けのメソッドであり、ユーザが所属しているロールから変更可能な場合のみ変更を実施します。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 *
	 * @param setting 変更後のジョブキューの情報。
	 * @throws InvalidSetting 設定に不備があります。
	 * @throws JobQueueNotFoundException 参照可能なジョブキューの情報が存在しませんでした。
	 */
	public void setSetting(JobQueueSetting setting) throws InvalidSetting, JobQueueNotFoundException {
		setting.validate();
		synchronized (lock) {
			try (JobQueueTx tx = external.newTransaction()) {
				JobQueueEntity entity = tx.findJobQueueEntitiyForModify(queueId);
				if (entity == null) {
					// 権限がないジョブキューへアクセスしたか、
					// 「コンテナからキュー取得 -> キュー削除 -> 本メソッド」の流れでnullになりうる。
					throw new JobQueueNotFoundException("queueId=" + queueId);
				}
				entity.setName(setting.getName());
				entity.setConcurrency(setting.getConcurrency());
				entity.setUpdateUser(external.getLoginUserId());
				entity.setUpdateDate(external.currentTimeMillis());
				tx.commit();
	
				// フィールドも更新する
				queueName = setting.getName();
				concurrency = setting.getConcurrency().intValue();
				
				log.info("setSetting: Name=" + queueName + ", concurrency=" + concurrency);
			}
		}
		activateJobs();
	}

	/**
	 * このキューを参照しているジョブ設定の一覧情報を返します。
	 * <p>
	 * クライアント向けのメソッドであり、このキュー及び参照元ジョブ設定に関して、
	 * ユーザが所属しているロールから参照可能な場合のみ情報を返します。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 * 
	 * @throws JobQueueNotFoundException 参照可能なジョブキューの情報が存在しませんでした。
	 * @throws HinemosUnknown ジョブ設定の詳細を取得中に何らかのエラーが発生しました。
	 */
	public JobQueueReferrerViewInfo collectReferrerViewInfo() throws JobQueueNotFoundException, HinemosUnknown {
		JobQueueReferrerViewInfo result = new JobQueueReferrerViewInfo();
		try (JobQueueTx tx = external.newTransaction()) {
			// キューの情報
			JobQueueEntity entity = tx.findJobQueueEntitiyForRead(queueId);
			if (entity == null) {
				// 権限がないジョブキューへアクセスしたか、
				// 「コンテナからキュー取得 -> キュー削除 -> 本メソッド」の流れでnullになりうる。
				throw new JobQueueNotFoundException("queueId=" + queueId);
			}
			result.setQueueId(queueId);
			result.setQueueName(entity.getName());
			
			// ジョブの情報
			List<JobQueueReferrerViewInfoListItem> filledItems = new ArrayList<>();
			for (JobQueueReferrerViewInfoListItem item : tx.findReferrerViewInfoListItemForRead(queueId)) {
				// ジョブの詳細な情報は、別途 FullJob#getJobFull で取得する。
				// クエリ1回で必要最小限の情報のみ取得するのに比べて
				// 処理効率は低下する(キャッシュの効果次第ではあるが)かもしれないが、
				// メンテナンス性はこちらのほうが高いはず。
				String jobunitId = item.getJobunitId();
				String jobId = item.getJobId();
				try {
					// ジョブユニット以外はJobInfoのownerRoleIdがnullに設定されてしまうので、再設定する。
					JobInfo full = FullJob.getJobFull(new JobInfo(jobunitId, jobId, null, null));
					full.setOwnerRoleId(item.getOwnerRoleId());
					item.setJobInfoWithOwnerRoleId(full);
					filledItems.add(item);
				} catch (HinemosUnknown e) {
					throw e;
				} catch (JobMasterNotFound | InvalidRole e) {
					// 僅差で削除された？
					// 僅差で権限の変更があった？
					// いずれにせよ、処理中止はせず、このジョブのみリストから除外する。
					log.info("collectReferrerViewInfo: " + e.getClass().getSimpleName() + ": " + e.getMessage()
							+ " job=" + jobunitId + "," + jobId);
				} catch (UserNotFound e) {
					// 実際は投げてこないはず
					log.info("collectReferrerViewInfo: Never.");
				}
			}
			result.setItems(filledItems);
	
			tx.commit();
		}
		return result;
	}

	/**
	 * このキューの内部状況の一覧情報を返します。
	 * <p>
	 * クライアント向けのメソッドであり、このキュー及び関連するジョブ詳細に関して、
	 * ユーザが所属しているロールから参照可能な場合のみ情報を返します。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 * 
	 * @throws JobQueueNotFoundException 参照可能なジョブキューの情報が存在しませんでした。
	 */
	public JobQueueContentsViewInfo collectContentsViewInfo() throws JobQueueNotFoundException {
		JobQueueContentsViewInfo result = new JobQueueContentsViewInfo();
		try (JobQueueTx tx = external.newTransaction()) {
			// キューの情報
			JobQueueEntity entity = tx.findJobQueueEntitiyForRead(queueId);
			if (entity == null) {
				// 権限がないジョブキューへアクセスしたか、
				// 「コンテナからキュー取得 -> キュー削除 -> 本メソッド」の流れでnullになりうる。
				throw new JobQueueNotFoundException("queueId=" + queueId);
			}
			result.setQueueId(queueId);
			result.setQueueName(entity.getName());
			result.setConcurrency(entity.getConcurrency());
			
			// キューアイテムの情報
			List<JobQueueItemEntity> queueItems = tx.findJobQueueItemEntities(queueId);
			List<JobQueueContentsViewInfoListItem> items = new ArrayList<>();
			SelectJob selector = external.newSelectJob();
			int activeCount = 0;
			for (JobQueueItemEntity queueItem : queueItems) {
				// アクティブ数
				if (queueItem.getStatus() == JobQueueItemStatus.ACTIVE) {
					++activeCount;
				}
				// ジョブ詳細情報
				JobQueueItemEntityPK pk = queueItem.getId();
				try {
					JobQueueContentsViewInfoListItem item = new JobQueueContentsViewInfoListItem();
					item.setJobTreeItem(selector.getDetail(pk.getSessionId(), pk.getJobunitId(), pk.getJobId()));
					item.setSessionId(pk.getSessionId());
					item.setRegDate(queueItem.getRegDate());
					items.add(item);
				} catch (InvalidRole e) {
					// 全キューアイテムについてジョブ詳細情報の取得を試みるため、権限違反例外は普通に起こりうる。
					// - 参照できないジョブが多い権限設定の場合、権限チェックのコストが嵩むかもしれない。
					//   コスト回避するためにはキューアイテムにジョブのオーナーロールIDを持たせて最初から弾く必要があるが、
					//   権限変更への追随などで問題があるので、そこまではしない。
					log.debug("collectContentsViewInfo: InvalidRole: " + e.getMessage() + " " + pk.toString());
				} catch (JobInfoNotFound e) {
					// 僅差で削除された？
					// いずれにせよ、処理中止はせず、このジョブ詳細のみリストから除外する。
					log.info("collectContentsViewInfo: JobInfoNotFound: " + e.getMessage() + " " + pk.toString());
				}
			}
			result.setCount(queueItems.size());
			result.setActiveCount(activeCount);
			result.setItems(items);
			
			tx.commit();
		}
		return result;
	}

	/** 通知情報の収集及び、通知キューへの登録を行うサブ処理。例外はログへ記録し、握りつぶす。 */
	private void notify(MessageConstant message, String sessionId, String jobunitId, String jobId) {
		try {
			OutputBasicInfo info = new OutputBasicInfo();
			try (JobQueueTx tx = external.newTransaction()) {
				// 通知で使用する各種情報を収集する
				// - 参照のみなのでセッションロックは獲得しない
				JobSessionJobEntity sessionJob = external.findSessionJob(sessionId, jobunitId, jobId);
				JobInfoEntity jobInfo = sessionJob.getJobInfoEntity();
	
				// ジョブの通知設定がないのであれば、ここで終了
				String notifyGroupId = jobInfo.getNotifyGroupId();
				if (notifyGroupId == null || notifyGroupId.length() == 0) return;
	
				Locale locale = external.getNotifyLocale();
	
				log.debug("notify: message=" + message + ", session=" + String.join(",", sessionId, jobunitId, jobId)
						+ ", notify=" + notifyGroupId + ", locale=" + locale);
	
				// 通知情報作成
				info.setNotifyGroupId(jobInfo.getNotifyGroupId());
				info.setPluginId(HinemosModuleConstant.JOB);
				info.setApplication(HinemosMessage.replace(MessageConstant.JOB_MANAGEMENT.getMessage(), locale));
				info.setMonitorId(sessionId);
				info.setMessage(message.getMessage(
						Messages.getString(JobConstant.typeToMessageCode(jobInfo.getJobType()), locale),
						jobInfo.getId().getJobId(), jobInfo.getJobName(), queueId, queueName, sessionId));
				info.setFacilityId("");
				info.setScopeText("");
				info.setPriority(PriorityConstant.TYPE_INFO);
				info.setGenerationDate(external.currentTimeMillis());
	
				tx.commit();
			}
	
			// 通知実行(キューへ登録)
			external.notify(info);
		} catch (Throwable t) {
			log.warn("notify: Exception.", t);
		}
	}

	/**
	 * アクティブ化タスク。
	 * 待機終了通知を行い、ジョブを実行開始し、キューアイテムをACTIVEにする。
	 * テストのためデフォルトスコープ。
	 */
	class ItemActivationTask implements Runnable {
		private JobQueueItemEntityPK target;
	
		public ItemActivationTask(JobQueueItemEntityPK target) {
			this.target = target;
		}
	
		@Override
		public void run() {
			log.info("ItemActivationTask: Start " + target);
	
			// 待機終了通知
			JobQueue.this.notify(MessageConstant.MESSAGE_JOBQUEUE_NOTIFY_ACTIVE, target.getSessionId(),
					target.getJobunitId(), target.getJobId());

			// ジョブ＆キューアイテム操作
			int retryCount = 0;
			while (true) {
				if (!run0()) break;
				if (++retryCount >= NPE_MAX_RETRY) break;
				log.info("ItemActivationTask: Retry " + retryCount);
			}

			log.debug("ItemActivationTask: End");
		}

		/** run()下請け。true:リトライする、false:しない。 */
		private boolean run0() {
			boolean shouldActivateJobs = false;
			try {
				ILock sessionLock = external.getSessionLock(target.getSessionId());
				sessionLock.writeLock();
				try (JobQueueTx tx = external.newTransaction()) {
					// ジョブ実行
					external.startQueuedJob(target.getSessionId(), target.getJobunitId(), target.getJobId());

					// キューアイテムをACTIVEにする
					synchronized (lock) {
						JobQueueItemEntity queueItem = tx.findJobQueueItemEntity(target);
						if (queueItem != null) {
							queueItem.setStatus(JobQueueItemStatus.ACTIVE);
						} else {
							// ターゲットが開始と同時にスキップ終了した？
							// 僅差でアイテムが削除された？
							// いずれにせよ、「activeになるべき枠が1つ余っている」状態になっている
							// 可能性があるので、あとでactivateを試みる。
							log.info("ItemActivationTask: Item is null.");
							shouldActivateJobs = true;
						}

						tx.commit();

						// アクティブ化候補から除去する。
						toBeActiveList.remove(target);
					}
				} finally {
					sessionLock.writeUnlock();
				}
			} catch (Throwable t) {
				log.warn("ItemActivationTask: Failed.", t);
				
				// 開発中、極稀にJobSessionJobEntity#getJobInfoEntityが
				// nullを返すことがあった。
				// 処理タイミングとJPA(eclipselink)側処理が絡んだ問題の可能性
				// (あるいはテスト時の環境リセットが不十分でデータや内部状態に不整合が起きた可能性)
				// があるが、その後は再現することなく、原因の調査ができなかった。
				// 次善の策として、この即時リトライ機構を設ける。
				if (t instanceof NullPointerException) {
					return true;
				} else {
					// キューアイテムは除去しない。
					// (キュー待機状態でジョブ履歴が残留してしまうため。)
					// 次のactivateJobsで再度実行開始の対象とする。
					// 一時的な異常であればそのときに解消される可能性があり、
					// 継続的な異常であればユーザが気づきやすくなる。
					// (僅差でジョブ履歴が消されていたような場合は、次のactivateJobsのときには
					// cascadeでアイテムが消えているため、無限ループにはならないはず。)
					
					// INTERNALイベントを出したほうがよい？
	
					// 例外が起きた場合でも、候補リストからの除去は行う必要がある
					synchronized (lock) {
						toBeActiveList.remove(target);
					}
				}
			}
			if (shouldActivateJobs) {
				activateJobs();
			}
			return false;
		}
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + " target=" + target;
		}
	}

	/** postClose だけ実装したい */
	private static abstract class PostCloseCallback implements JpaTransactionCallback {
		@Override
		public boolean isTransaction() {
			return false;
		}
		@Override public void preRollback() {}
		@Override public void preFlush() {}
		@Override public void preCommit() {}
		@Override public void preClose() {}
		@Override public void postRollback() {}
		@Override public void postFlush() {}
		@Override public void postCommit() {}
	}

	/**
	 * キュー内のジョブの識別子。
	 * これが一致すれば同じアイテムとみなす。
	 * テストのためデフォルトスコープ。
	 */
	static class JobKey {
		String value;
	
		public JobKey(String sessionId, String jobunitId, String jobId, long regDate) {
			value = String.join("\t", sessionId, jobunitId, jobId, String.valueOf(regDate));
		}
	
		@Override
		public int hashCode() {
			return value.hashCode();
		}
	
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof JobKey) {
				JobKey jk = (JobKey) obj;
				return value.equals(jk.value);
			}
			return false;
		}
	}

	/** 外部依存動作をモックへ置換できるように分離 */
	static class External {
		JobQueueTx newTransaction() {
			return new JobQueueTx();
		}
	
		int getJobQueueSize() {
			return HinemosPropertyCommon.jobqueue_size.getIntegerValue();
		}
	
		ILock getSessionLock(String sessionId) {
			return JobRunManagementBean.getLock(sessionId);
		}
	
		void startQueuedJob(String sessionId, String jobunitId, String jobId)
				throws JobInfoNotFound, HinemosUnknown, InvalidRole, FacilityNotFound {
			new JobSessionJobImpl().startQueuedJob(sessionId, jobunitId, jobId);
		}
	
		long currentTimeMillis() {
			return HinemosTime.currentTimeMillis();
		}
	
		String getLoginUserId() {
			return HinemosSessionContext.getLoginUserId();
		}
		
		SelectJob newSelectJob() {
			return new SelectJob();
		}
		
		JobSessionJobEntity findSessionJob(String sessionId, String jobunitId, String jobId)
				throws JobInfoNotFound, InvalidRole {
			return QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		}
		
		Locale getNotifyLocale() {
			return NotifyUtil.getNotifyLocale();
		}
	
		public void notify(OutputBasicInfo info) {
			NotifyControllerBean.notify(Arrays.asList(info));
		}
	}
}
