/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.session;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import javax.persistence.TypedQuery;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * 以下の点に考慮したデータのイテレーションを行うためのクラス。
 * 
 * 1. 各データの位置情報のサイクル
 * 2. データをフェッチで取得する
 * 3. 取得した情報の型の変換
 * 
 * Transfer のインスタンスの転送関数に渡される。
 *
 * @param <T>
 * @param <R>
 */
public class JpaRowIterable<T, R> implements Iterable<R>, AutoCloseable {
	/**
	 * イテレータクラス。
	 *
	 */
	public class JpaRowIterator implements Iterator<R> {
		private final int fetchSize = HinemosPropertyCommon.hub_transfer_fetch_size.getIntegerValue();

		private Iterator<TypedQuery<T>> queryIter;
		private TypedQuery<T> currentQuery;
		private int nextPos = 0;

		private R nextRow;

		private Iterator<T> rows;
		
		public JpaRowIterator() {
			this.queryIter = util.createQueries();
			if (this.queryIter.hasNext()) {
				currentQuery = this.queryIter.next();
			}
		}
		
		/**
		 * 
		 * 
		 * @return
		 */
		@Override
		public boolean hasNext() {
			if (nextRow != null)
				return true;

			if (currentQuery == null)
				return false;

			nextRow = get();
			
			return nextRow != null;
		}
		
		/**
		 * データを取得する。
		 * 
		 * @return
		 */
		private R get() {
			// クエリの実行結果のリストが空か判断する。
			if (rows == null || !rows.hasNext()) {
				// 次回クエリの前に、JPA が管理するエンティティーを開放して、
				// メモリへの負荷を低減。
				util.getEntityManager().clear();
				// 新しいクエリの結果を取得。
				rows = nextList();
			}
			
			// クエリの内容が空か判断。
			if (rows == null || !rows.hasNext())
				return null;
			
			// 取得したデータをコンバーターを通して、型を変換。
			return converter.apply(rows.next());
		}
		
		/**
		 * 
		 * 
		 */
		@Override
		public R next() {
			if (!hasNext())
				throw new NoSuchElementException();
			
			R next = nextRow;
			nextRow = null;
			
			return next;
		}
		
		/**
		 * 複数のクエリを順々に実行し、また実行するクエリは、フェッチで行う。
		 * 
		 * @return
		 */
		private Iterator<T> nextList() {
			while (currentQuery != null) {
				// オフセットとフェッチサイズを設定し、部分クエリになるようにする
				currentQuery.setFirstResult(nextPos);
				currentQuery.setMaxResults(fetchSize);
				
				// クエリを実行。
				List<T> datas = currentQuery.getResultList();
				if (!datas.isEmpty()) {
					// クエリの内容が空でないなら、次回実行時のオフセットを更新。
					nextPos += fetchSize;
					return datas.iterator();
				}
				
				// クエリの内容が空なので、次のクエリの準備をする。
				nextPos = 0;
				currentQuery = null;
				if (queryIter.hasNext()) {
					currentQuery = queryIter.next();
				}
			};
			return null;
		}
	}
	
	/**
	 * データ型に応じたクエリをサポートするユーティリティ
	 */
	private JpaQueryUtil<T, R> util;
	/**
	 * クエリから取得したデータの型を変換する。
	 */
	private Function<T, R> converter;

	public JpaRowIterable(JpaQueryUtil<T, R> util) {
		this.util = util;
		this.converter = util.createConverter();
	}
	
	@Override
	public Iterator<R> iterator() {
		return new JpaRowIterator();
	}
	

	@Override
	public void close() throws Exception {
		util.getEntityManager().clear();
	}
}