/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueActivityViewFilter;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueActivityViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueActivityViewInfo.JobQueueActivityViewInfoListItem;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSetting;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewFilter;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewInfo.JobQueueSettingViewInfoListItem;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueEntity;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueExecutor;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueItemStatus;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueTx;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;

/**
 * ジョブキューのインスタンスを管理するコンテナです。
 * 本クラスの唯一のインスタンスを生成することでジョブキュー機能が開始し、
 * {@link #terminate()}でジョブキュー機能が終了します。
 *
 * @since 6.2.0
 */
public class JobQueueContainer {
	private static final Log log = LogFactory.getLog(JobQueueContainer.class);

	private External external;

	private Map<String, JobQueue> queues;

	/**
	 * コンストラクタではなく、{@link Singletons#get(Class)}を使用してください。
	 */
	public JobQueueContainer() {
		this(new External());
	}

	JobQueueContainer(External external) {
		this.external = external;
		log.info("ctor: Begin.");

		// JobQueueExecutorを生成
		Singletons.get(JobQueueExecutor.class);
		
		// DBからキューをリストア
		queues = new ConcurrentHashMap<>();
		try (JobQueueTx tx = external.newTransaction()) {
			for (JobQueueEntity entity : tx.findJobQueueEntities()) {
				queues.put(entity.getQueueId(), external.newJobQueue(entity));
			}
			tx.commit();
		}

		log.info("ctor: End.");
	}

	/**
	 * 終了処理を行います。
	 */
	public void terminate() {
		log.info("terminate: Begin");

		// JobQueueExecutorを停止
		if (Singletons.has(JobQueueExecutor.class)) {
			JobQueueExecutor executor = Singletons.get(JobQueueExecutor.class);
			executor.shutdown();
			// ある程度はタスク終了を待つべきか？
		}

		log.info("terminate: End.");
	}

	/**
	 * ジョブキューを返します。
	 * 
	 * @param queueId キューID。
	 * @return 指定されたIDに該当するジョブキュー。
	 * @throws JobQueueNotFoundException 該当するジョブキューが存在しません。
	 */
	public JobQueue get(String queueId) throws JobQueueNotFoundException {
		JobQueue target = queues.get(queueId);
		if (target == null) {
			throw new JobQueueNotFoundException("queueId=" + queueId);
		}
		return target;
	}

	/**
	 * 保持しているジョブキューのStreamを返します。
	 */
	public Stream<JobQueue> stream() {
		return queues.values().stream();
	}
	
	/**
	 * 新しいジョブキューを作成して追加します。
	 * <p>
	 * クライアント向けのメソッドであり、作成したいジョブキューのオーナーロールにユーザが所属しているかのチェックを行います。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 *
	 * @param setting 生成するジョブキューの情報。
	 * @throws InvalidSetting 設定に不備があります。
	 * @throws JobQueueNotFoundException 
	 */
	public JobQueueSetting add(JobQueueSetting setting) throws InvalidSetting, JobQueueNotFoundException {
		setting.validate();
		// 上記のvalidate()では書式チェックのみ、オーナーロールに関しては別途チェックする
		external.validateOwnerRoleId(setting.getOwnerRoleId(), setting.getQueueId());
		external.checkUserRoles(setting.getOwnerRoleId());

		synchronized (queues) {
			String qid = setting.getQueueId();
			if (queues.containsKey(qid)) {
				throw new InvalidSetting(MessageConstant.MESSAGE_JOBQUEUE_DUPLICATION.getMessage(qid));
			}

			try (JobQueueTx tx = external.newTransaction()) {
				long now = external.currentTimeMillis();
				String userId = external.getLoginUserId();
				JobQueueEntity entity = new JobQueueEntity(setting.getQueueId());
				entity.setName(setting.getName());
				entity.setConcurrency(setting.getConcurrency());
				entity.setOwnerRoleId(setting.getOwnerRoleId());
				entity.setRegUser(userId);
				entity.setRegDate(now);
				entity.setUpdateUser(userId);
				entity.setUpdateDate(now);
				tx.persist(entity);
				tx.commit();

				queues.put(qid, external.newJobQueue(entity));
			}

			log.info("add: id=" + qid);
			return queues.get(qid).getSetting();
		}
	}

	/**
	 * ジョブキューを削除します。
	 * <p>
	 * クライアント向けのメソッドであり、ユーザが所属しているロールから、指定されたキューが変更可能かのチェックを行います。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 * 
	 * @param queueId キューID。
	 * @throws InvalidSetting 設定上の問題で削除できませんでした。 
	 */
	public void remove(String queueId) throws InvalidSetting {
		synchronized (queues) {
			JobQueue queue = queues.get(queueId);
			if (queue == null) {
				log.info("remove: Not found. id=" + queueId);
				return;
			}

			// 同期して、JobQueue側で何らかの更新操作中に削除してしまうことを回避する
			synchronized (queue.getLock()) {
				// キューが空でないと削除できない
				if (queue.getSize() > 0) {
					throw new InvalidSetting(
							MessageConstant.MESSAGE_JOBQUEUE_NOT_EMPTY.getMessage(queueId));
				}
				try (JobQueueTx tx = external.newTransaction()) {
					// キューを参照しているジョブ定義があると削除できない
					for (String jobId : tx.findJobIdByQueueId(queueId)) {
						throw new InvalidSetting(
								MessageConstant.MESSAGE_DELETE_NG_JOB_REFERENCE_TO_QUEUE.getMessage(jobId, queueId));
					}
					JobQueueEntity entity = tx.findJobQueueEntitiyForModify(queueId);
					if (entity == null) {
						log.info("remove: Not found, may be invalid role. id=" + queueId);
						return;
					}
					tx.remove(entity);
					tx.commit();
				}

				queues.remove(queueId);
				queue.invalidate();
			}

			log.info("remove: id=" + queueId);
		}
	}

	/**
	 * 指定されたロールから参照可能なジョブキュー(同時実行制御キュー)の設定情報のリストを返します。
	 * <p>
	 * クライアント向けのメソッドですが、指定されたロールにユーザが所属しているかのチェックは行いません。
	 * これは、オブジェクト権限を付与されたジョブ定義からの紐付けで参照される可能性があるためです。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 * 
	 * @param roleId ロールID。
	 * @return ジョブキューの設定情報のリスト。
	 */
	public List<JobQueueSetting> findSettingsByRole(String roleId) {
		List<JobQueueSetting> result = new ArrayList<>();
		try (JobQueueTx tx = external.newTransaction()) {
			for (JobQueueEntity entity : tx.findJobQueueEntitiesWithRoleForRead(roleId)) {
				result.add(new JobQueueSetting(entity));
			}			
			tx.commit();
		}
		return result;
	}

	/**
	 * 指定されたオーナーロールのジョブキューを返します。
	 * 
	 * @param roleId オーナーロールのロールID。
	 * @return ジョブキューのリスト。
	 */
	public List<JobQueue> findByOwnerRoleId(String roleId) {
		return stream().filter(q -> q.getOwnerRoleId().equals(roleId)).collect(Collectors.toList());
	}
	
	/**
	 * ジョブキュー設定一覧表示用の情報を返します。
	 * <p>
	 * クライアント向けのメソッドであり、ユーザが所属しているロールから参照可能なキューの情報のみ返します。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 * 
	 * @param filter フィルタ条件。
	 * @throws InvalidSetting フィルタ条件の設定に不備があります。 
	 */
	public JobQueueSettingViewInfo collectSettingViewInfo(JobQueueSettingViewFilter filter) throws InvalidSetting {
		filter.validate();
		JobQueueSettingViewInfo info = new JobQueueSettingViewInfo();
		try (JobQueueTx tx = external.newTransaction()) {
			info.setItems(tx.findSettingViewInfoListItemForRead(filter));
			tx.commit();
		}
		return info;
	}

	/**
	 * ジョブキュー活動状況一覧表示用の情報を返します。
	 * <p>
	 * クライアント向けのメソッドであり、ユーザが所属しているロールから参照可能なキューの情報のみ返します。
	 * システム権限のチェックは呼び出し元で行う必要があります。
	 * 
	 * @param filter フィルタ条件。
	 * @throws InvalidSetting フィルタ条件の設定に不備があります。 
	 */
	public JobQueueActivityViewInfo collectActivityViewInfo(JobQueueActivityViewFilter filter) throws InvalidSetting {
		filter.validate();
		// eclipselinkの機能(FROM句でのサブクエリ、またはルートレベルオブジェクト同士の結合)を使えば
		// クエリ一発で取ってこれるが、オブジェクト権限用にクエリを加工するのに使用しているjpasecurityが
		// 解析エラーを出してしまうため、単純なクエリを2回実行する方向で対応している。
		JobQueueActivityViewInfo info = new JobQueueActivityViewInfo();
		try (JobQueueTx tx = external.newTransaction()) {
			// cc_job_queue_itemテーブルの全データをキューごとにカウントして、
			// 「キューID -> カウント」というMapを作る。
			// - 本来はキューの参照権限で対象を絞り込めるような状況でも、
			//   無駄にカウントしてしまう点で効率が悪い。
			Map<String, Long[]> qid2cnt = new HashMap<>();
			for (Object[] it : tx.countJobQueueItemPerQueueId(JobQueueItemStatus.ACTIVE.getId())) {
				// PGSQLはLong、MSSQLはIntegerで返してくるので、Numberで処理する。
				// (JPA仕様では集計関数はLongを返すことに決まっているが、どうもドライバ依存になっている？)
				qid2cnt.put((String) it[0], new Long[]{
						((Number) it[1]).longValue(),
						((Number) it[2]).longValue() });
			}
			// キューの詳細情報については、SettingViewのクエリを拝借する。
			List<JobQueueActivityViewInfoListItem> items = new ArrayList<>();
			for (JobQueueSettingViewInfoListItem queue :
					tx.findSettingViewInfoListItemForRead(filter)) {
				Long[] cnts = qid2cnt.get(queue.getQueueId());
				long count = cnts != null ? cnts[0] : 0L;
				long activeCount = cnts != null ? cnts[1] : 0L;

				// ジョブ実行数のフィルタリングはここで行う
				if (filter.getJobCountFrom() != null && 
						activeCount < filter.getJobCountFrom()) continue;
				if (filter.getJobCountTo() != null && 
						filter.getJobCountTo() < activeCount) continue;

				JobQueueActivityViewInfoListItem item = new JobQueueActivityViewInfoListItem();					
				item.setQueueId(queue.getQueueId());
				item.setName(queue.getName());
				item.setConcurrency(queue.getConcurrency());
				item.setOwnerRoleId(queue.getOwnerRoleId());
				item.setRegDate(queue.getRegDate());
				item.setRegUser(queue.getRegUser());
				item.setUpdateDate(queue.getUpdateDate());
				item.setUpdateUser(queue.getUpdateUser());
				item.setCount(count);
				item.setActiveCount(activeCount);
				items.add(item);
			}
			info.setItems(items);

			tx.commit();
		}
		return info;
	}

	/**
	 * 全てのジョブキューに対して、{@link JobQueue#activateJobs()}を呼び出します。
	 */
	public void activateJobs() {
		stream().forEach(q -> {
			q.activateJobs();
		});
	}
	
	/** 外部依存動作をモックへ置換できるように分離 */
	static class External {
		JobQueue newJobQueue(JobQueueEntity entity) {
			return new JobQueue(entity);
		}
		
		JobQueueTx newTransaction() {
			return new JobQueueTx();
		}
	
		long currentTimeMillis() {
			return HinemosTime.currentTimeMillis();
		}
		
		String getLoginUserId() {
			return HinemosSessionContext.getLoginUserId();
		}
		
		void validateOwnerRoleId(String ownerRoleId, String queueId) throws InvalidSetting {
			CommonValidator.validateOwnerRoleId(ownerRoleId, true, queueId, HinemosModuleConstant.JOB_QUEUE);
		}
		
		void checkUserRoles(String roleId) throws InvalidSetting {
			RoleValidator.validateUserBelongRole(roleId, HinemosSessionContext.getLoginUserId(),
					HinemosSessionContext.isAdministrator());
		}
	}
}
