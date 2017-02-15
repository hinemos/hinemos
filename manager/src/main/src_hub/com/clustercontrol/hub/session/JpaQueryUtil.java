/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.model.TransferInfo.TransferType;
import com.clustercontrol.hub.util.StringDataIdGenerator;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.util.HinemosTime;

/**
 * 転送処理で扱うデータ型毎のクエリーを整理するユーティリティークラス。
 *
 * @param <T>
 * @param <R>
 */
public abstract class JpaQueryUtil<T, R> {
	private static final Logger logger = Logger.getLogger(FluentdTransferFactory.class);
	
	public static class RowPosision {
		public final long last;
		public final Long cycledMin;
		public final long max;
		
		public RowPosision(long last, Long cycledMin, long max) {
			this.last = last;
			this.cycledMin = cycledMin;
			this.max = max;
		}
		
		public RowPosision(long last, long max) {
			this.last = last;
			this.cycledMin = null;
			this.max = max;
		}
	}

	/**
	 * 
	 */
	protected EntityManager em;

	/**
	 * 
	 */
	protected TransferInfo ti;

	public JpaQueryUtil(TransferInfo ti, EntityManager em){
		this.ti = ti;
		this.em = em;
	}
	
	/**
	 * クエリーの対象になるデータのクラスを返す。
	 * 
	 * @return
	 */
	protected abstract Class<T> getDataClass();

	/**
	 * シーケンステーブル名を返す。
	 * 
	 * @return
	 */
	protected abstract String getSequenceTableName();

	/**
	 * データを取得するクエリを作成する。
	 * 
	 * @return
	 */
	protected abstract TypedQuery<T> createQuery();
	
	/**
	 * 位置情報を取得する。
	 * 
	 * @return
	 */
	protected JpaQueryUtil.RowPosision getPosition() {
		// 位置情報がサイクルしている場合の最小値を取得
		Table t = getDataClass().getAnnotation(Table.class);
		if (t == null) {
			throw new IllegalStateException(String.format("Unexpected Error. %s has not Table annotation", getDataClass().getSimpleName()));
		}

		String tableName = t.schema() + "." + t.name();
		Query cycledPosQuery = em.createNativeQuery(
				String.format("SELECT MIN(t0.position) FROM %s t0 WHERE t0.position > (SELECT last_value FROM %s) AND t0.position <= (SELECT max_value FROM %s)",
						tableName, getSequenceTableName(), getSequenceTableName()));
		Long cycled = (Long)cycledPosQuery.getSingleResult();
		
		// 位置情報の最新値と最大値を取得
		Query posQuery = em.createNativeQuery(String.format("SELECT last_value, max_value FROM %s", getSequenceTableName()));
		Object[] data = (Object[])posQuery.getSingleResult();
		if (data == null || data.length <= 0) {
			HubControllerBean.logger.warn("Fatal Error");
			return null;
		}
		
		return new RowPosision((Long)data[0], cycled, (Long)data[1]);
	};

	/**
	 * 位置情報の範囲に収まるデータを取得するクエリを作成する。
	 * 
	 * @param prevlast
	 * @param last
	 * @return
	 */
	protected TypedQuery<T> createQuery(Long prevlast, Long last) {
		TypedQuery<T> query = createQuery();
		if (query == null)
			return null;
		
		query.setParameter("prevlast", prevlast);
		query.setParameter("last", last);
		
		// 遅延処理の場合、クエリに対して追加設定
		if (ti.getTransType() == TransferType.delay) {
			// 遅延期間(日)を  ms へ変換
			long delay = ((long)ti.getInterval()) * 24 * 60 * 60 * 1000;
			long current = HinemosTime.currentTimeMillis();
			
			query.setParameter("delay", delay);
			query.setParameter("current", current);
		}
		
		// JPA のキャッシュは、必ず更新したクエリを実施する
		// 各データの position は、シリアル型なので、Hinemos から直接値指定せず、そのデータを登録する。
		// JPA のキャッシュには、posision を指定していない状態で登録されるので、position が null となり、
		// したがって、転送時に読み込む際もposition は、null のままとなる。
		// 以下の設定でキャッシュを使用せず、position を DB から直接取得するようにする。
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		
		return query;
	}
	
	/**
	 * サイクルを考慮したクエリインスタンスを作成する。
	 * 
	 * @return
	 */
	public Iterator<TypedQuery<T>> createQueries() {
		// 現在格納されているデータの最大位置を取得する
		RowPosision pos = getPosition();
		
		List<TypedQuery<T>> queries = new ArrayList<>();
		
		// 取得済み位置情報が設定されているか判定
		if (ti.getPosition() != null) {
			// 取得済み位置より格納されているデータの位置が小さいなら、
			// 位置情報がサイクルしていると判断。
			// 一回転以上のサイクルはサポートしない。
			if (pos.last < ti.getPosition().getLastPosition()) {
				// まず、取得済み位置から、位置情報が格納できる最大値まで範囲をクエリを作成
				TypedQuery<T> query = createQuery(ti.getPosition().getLastPosition(), pos.max);
				if (query == null)
					return Collections.emptyIterator();
				queries.add(query);
				// 次に、0 から、格納済みの位置まで取得するクエリを作成
				query = createQuery(0L, pos.last);
				if (query == null)
					return Collections.emptyIterator();
				queries.add(query);
			} else {
				// サイクルしていないので、普通に前回位置から格納位置までをクエリ
				TypedQuery<T> query = createQuery(ti.getPosition().getLastPosition(), pos.last);
				if (query == null)
					return Collections.emptyIterator();
				queries.add(query);
			}
		} else {
			// 前回取得位置が分らないので、それを考慮したクエリを実施。
			if (pos.cycledMin != null) {
				// サイクルしていることを考慮して、
				// 格納済みデータの最大位置から、位置情報が格納できる最大値までの範囲でクエリを作成
				TypedQuery<T> query = createQuery(pos.cycledMin - 1, pos.max);
				if (query == null)
					return Collections.emptyIterator();
				queries.add(query);
				// 次に、0 から、格納済みの位置まで取得するクエリを作成
				query = createQuery(0L, pos.last);
				if (query == null)
					return Collections.emptyIterator();
				queries.add(query);
			} else {
				// 0 から、格納済みの位置まで取得するクエリを作成
				TypedQuery<T> query = createQuery(0L, pos.last);
				if (query == null)
					return Collections.emptyIterator();
				queries.add(query);
			}
		}
		return queries.iterator();
	}
	
	/**
	 * コンバーターを取得する。
	 * 
	 * @return
	 */
	public Function<T, R> createConverter() {
		return new Function<T, R>() {
			@SuppressWarnings("unchecked")
			public R apply(T t) {
				return (R)t;
			}
		};
	}
	
	/**
	 * このインスタンスが使用している EntityManager を返す。
	 * 
	 * @return
	 */
	public EntityManager getEntityManager() {
		return em;
	}
	
	/**
	 * イベントに特化したRowQueryUtilのインスタンスを返す。
	 * 
	 * @param em
	 * @return
	 */
	public static JpaQueryUtil<EventLogEntity, EventLogEntity> createEventUtil(TransferInfo ti, HinemosEntityManager em) {
		// オブジェクト権限を考慮して、転送設定のオーナーロールでアクセス可能な監視設定を取得する。
		// なお、イベントを取得するにあたり、オブジェクト権限を考慮するのは、監視のみ。
		// EventCache.filterCheck() 関数を参考。
		List<String> monitorIds = new ArrayList<>();
		List<MonitorInfo> monitors = QueryUtil.getMonitorInfoByOwnerRoleId_NONE(ti.getOwnerRoleId());
		for (MonitorInfo mi: monitors) {
			monitorIds.add(mi.getMonitorId());
		}

		return new JpaQueryUtil<EventLogEntity, EventLogEntity>(ti, em) {
			@Override
			protected TypedQuery<EventLogEntity> createQuery() {
				TypedQuery<EventLogEntity> query = null;
				switch (ti.getTransType()) {
				case realtime:
				case batch:
					query = em.createNamedQuery(monitorIds.isEmpty() ? "EventLogEntity.transfer.only_ownerrole": "EventLogEntity.transfer", EventLogEntity.class);
					break;
				case delay:
					query = em.createNamedQuery(monitorIds.isEmpty() ? "EventLogEntity.transfer.delay.only_ownerrole": "EventLogEntity.transfer.delay", EventLogEntity.class);
					break;
				default:
					throw new InternalError(String.format("createEventUtil() : unexpected value, value=%s", ti.getTransType()));
				}
				
				if (monitorIds.isEmpty()) {
					query.setParameter("admin", RoleIdConstant.ADMINISTRATORS.equals(ti.getOwnerRoleId()) ? ti.getOwnerRoleId(): null);
					query.setParameter("ownerRoleId", ti.getOwnerRoleId());
				} else {
					query.setParameter("monitorIds", monitorIds);
					query.setParameter("admin", RoleIdConstant.ADMINISTRATORS.equals(ti.getOwnerRoleId()) ? ti.getOwnerRoleId(): null);
					query.setParameter("ownerRoleId", ti.getOwnerRoleId());
				}
				return query;
			}

			@Override
			protected Class<EventLogEntity> getDataClass() {
				return EventLogEntity.class;
			}

			@Override
			protected String getSequenceTableName() {
				return "log.cc_event_log_position_seq";
			}
		};
	}

	/**
	 * ジョブに特化したRowQueryUtilのインスタンスを返す。
	 * 
	 * @param em
	 * @return
	 */
	public static JpaQueryUtil<JobSessionEntity, JobSessionEntity> createJobUtil(TransferInfo ti, HinemosEntityManager em) {
		// オブジェクト権限を考慮して、転送設定のオーナーロールでアクセス可能なジョブを取得する。
		List<JobMstEntity> jobunitList = em.createNamedQuery_OR("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class, ti.getOwnerRoleId())
				.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
				.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
				.getResultList();
		
		List<String> jobunitIds = new ArrayList<>();
		for (JobMstEntity j: jobunitList) {
			jobunitIds.add(j.getParentJobunitId());
		}
		
		return new JpaQueryUtil<JobSessionEntity, JobSessionEntity>(ti, em) {
			@Override
			protected TypedQuery<JobSessionEntity> createQuery() {
				TypedQuery<JobSessionEntity> query = null;
				switch (ti.getTransType()) {
				case realtime:
				case batch:
					query = em.createNamedQuery(jobunitIds.isEmpty() ? "JobSessionEntity.transfer.only_ownerrole": "JobSessionEntity.transfer", JobSessionEntity.class);
					break;
				case delay:
					query = em.createNamedQuery(jobunitIds.isEmpty() ? "JobSessionEntity.transfer.delay.only_ownerrole": "JobSessionEntity.transfer.delay", JobSessionEntity.class);
					break;
				default:
					throw new InternalError(String.format("createEventUtil() : unexpected value, value=%s", ti.getTransType()));
				}
				
				if (jobunitIds.isEmpty()) {
					// オブジェクト権限を考慮しないクエリー
					query.setParameter("admin", RoleIdConstant.ADMINISTRATORS.equals(ti.getOwnerRoleId()) ? ti.getOwnerRoleId(): null);
					query.setParameter("ownerRoleId", ti.getOwnerRoleId());
				} else {
					// オブジェクト権限を考慮したクエリー
					query.setParameter("jobunitIds", jobunitIds);
					query.setParameter("admin", RoleIdConstant.ADMINISTRATORS.equals(ti.getOwnerRoleId()) ? ti.getOwnerRoleId(): null);
					query.setParameter("ownerRoleId", ti.getOwnerRoleId());
				}

				return query;
			}

			@Override
			protected Class<JobSessionEntity> getDataClass() {
				return JobSessionEntity.class;
			}

			@Override
			protected String getSequenceTableName() {
				return "log.cc_job_session_position_seq";
			}
		};
	}
		
	/**
	 * 数値情報に特化したRowQueryUtilのインスタンスを返す。
	 * 
	 * @param em
	 * @return
	 */
	public static JpaQueryUtil<CollectData, TransferNumericData> createNumericUtil(TransferInfo ti, HinemosEntityManager em) {
		Map<Integer, CollectKeyInfo> keyMap = new HashMap<>();
		List<Integer> collectIds = new ArrayList<>();
		
		// 転送設定のオーナーロールでアクセスできる数値監視の一覧を取得。
		List<String> monitorIds = new ArrayList<>();
		List<MonitorInfo> monitors = QueryUtil.getMonitorInfoByOwnerRoleId_NONE(ti.getOwnerRoleId());
		for (MonitorInfo mi: monitors) {
			if (mi.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
				monitorIds.add(mi.getMonitorId());
			}
		}
		
		if (!monitorIds.isEmpty()) {
			// 転送設定のオーナーロールでアクセスできる監視一覧から収集値のキーの一覧を取得。
			TypedQuery<CollectKeyInfo> keyQuery = em.createNamedQuery("CollectKeyInfo.transfer", CollectKeyInfo.class);
			keyQuery.setParameter("monitorIds", monitorIds);
			List<CollectKeyInfo> keys = keyQuery.getResultList();
			
			for (CollectKeyInfo key: keys) {
				collectIds.add(key.getCollectorid());
				keyMap.put(key.getCollectorid(), key);
			}
		}

		return new JpaQueryUtil<CollectData, TransferNumericData>(ti, em) {
			/**
			 * クエリに対する修飾を行う。
			 * 
			 * @param query
			 * @return
			 */
			@Override
			protected TypedQuery<CollectData> createQuery() {
				// アクセス可能な収集IDがないなら、スルー。
				if (collectIds.isEmpty())
					return null;

				TypedQuery<CollectData> query = null;
				switch (ti.getTransType()) {
				case realtime:
				case batch:
					query = em.createNamedQuery("CollectData.transfer", CollectData.class);
					break;
				case delay:
					query = em.createNamedQuery("CollectData.transfer.delay", CollectData.class);
					break;
				default:
					throw new InternalError(String.format("createEventUtil() : unexpected value, value=%s", ti.getTransType()));
				}
				
				// 取得する収集値をキーの一覧で制限する
				query.setParameter("collectIds", collectIds);
				
				return query;
			}
			
			@Override
			public Function<CollectData, TransferNumericData> createConverter() {
				// 取得した CollectData を CollectKeyInfo と突き合わせて、TransferNumericData として返すクラス。
				// CollectData と  CollectKeyInfo の結合を避けたクエリをしているため。
				return 	new Function<CollectData, TransferNumericData>() {
							@Override
							public TransferNumericData apply(CollectData t) {
								CollectKeyInfo key = keyMap.get(t.getCollectorId());
								if (key == null) {
									String message = String.format("unexpected error : not found CollectKeyInfo with %s as collectId", t.getCollectorId());
									logger.warn(message);
									throw new InternalError(String.format(message));
								}
								return new TransferNumericData(key, t);
							}
						};
			}

			@Override
			protected Class<CollectData> getDataClass() {
				return CollectData.class;
			}

			@Override
			protected String getSequenceTableName() {
				return "log.cc_collect_data_raw_position_seq";
			}
		};
	}
	
	/**
	 * 文字列情報に特化したRowQueryUtilのインスタンスを返す。
	 * 
	 * @param info
	 * @param em
	 * @return
	 */
	public static JpaQueryUtil<CollectStringData, TransferStringData> createStringUtil(TransferInfo ti, HinemosEntityManager em) {
		Map<Long, CollectStringKeyInfo> keyMap = new HashMap<>();
		List<Long> collectIds = new ArrayList<>();
		
		// 転送設定のオーナーロールでアクセスできる数値監視の一覧を取得。
		List<String> monitorIds = new ArrayList<>();
		List<MonitorInfo> monitors = QueryUtil.getMonitorInfoByOwnerRoleId_NONE(ti.getOwnerRoleId());
		for (MonitorInfo mi: monitors) {
			if (mi.getMonitorType() == MonitorTypeConstant.TYPE_STRING) {
				monitorIds.add(mi.getMonitorId());
			}
		}
		
		if (!monitorIds.isEmpty()) {
			// 転送設定のオーナーロールでアクセスできる監視一覧から収集値のキーの一覧を取得。
			TypedQuery<CollectStringKeyInfo> keyQuery = em.createNamedQuery("CollectStringKeyInfo.transfer", CollectStringKeyInfo.class);
			keyQuery.setParameter("monitorIds", monitorIds);
			List<CollectStringKeyInfo> keys = keyQuery.getResultList();
			
			for (CollectStringKeyInfo key: keys) {
				collectIds.add(key.getCollectId());
				keyMap.put(key.getCollectId(), key);
			}
		}

		return new JpaQueryUtil<CollectStringData, TransferStringData>(ti, em) {
			@Override
			protected TypedQuery<CollectStringData> createQuery() {
				// アクセス可能な収集IDがないなら、スルー。
				if (collectIds.isEmpty())
					return null;
				
				TypedQuery<CollectStringData> query = null;
				switch (ti.getTransType()) {
				case realtime:
				case batch:
					query = em.createNamedQuery("CollectStringData.transfer", CollectStringData.class);
					break;
				case delay:
					query = em.createNamedQuery("CollectStringData.transfer.delay", CollectStringData.class);
					break;
				default:
					throw new InternalError(String.format("createEventUtil() : unexpected value, value=%s", ti.getTransType()));
				}
				
				// 取得する収集値をキーの一覧で制限する
				query.setParameter("collectIds", collectIds);
				
				return query;
			}

			@Override
			public Function<CollectStringData, TransferStringData> createConverter() {
				// 取得した CollectData を CollectKeyInfo と突き合わせて、TransferNumericData として返すクラス。
				// CollectData と  CollectKeyInfo の結合を避けたクエリをしているため。
				return 	new Function<CollectStringData, TransferStringData>() {
							@Override
							public TransferStringData apply(CollectStringData t) {
								CollectStringKeyInfo key = keyMap.get(t.getCollectId());
								if (key == null) {
									String message = String.format("unexpected error : not found CollectStringKeyInfo with %s as collectId", t.getCollectId());
									logger.warn(message);
									throw new InternalError(String.format(message));
								}
								return new TransferStringData(key, t);
							}
						};
			}

			@Override
			protected Class<CollectStringData> getDataClass() {
				return CollectStringData.class;
			}

			@Override
			protected String getSequenceTableName() {
				return null;
			}
			
			/**
			 * 位置情報を取得する。
			 * 
			 * @return
			 */
			@Override
			protected JpaQueryUtil.RowPosision getPosition() {
				return new RowPosision(StringDataIdGenerator.getCurrent(), Long.MAX_VALUE);
			};
		};
	}
}