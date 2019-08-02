/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.auth.AuthenticationParams;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.util.HinemosTime;

/**
 * 認証結果を一時的に記憶・保持します。
 */
public class AuthResultCache {
	private static final Log log = LogFactory.getLog(AuthResultCache.class);

	/** 認証結果の保持期間[min] 最小値 */
	public static final long MIN_KEEP_TIME = 0;
	/** 認証結果の保持期間[min] 最大値 */
	public static final long MAX_KEEP_TIME = 480;

	/** [単体テスト用] 外部依存処理の切り出し */
	protected static class External {
		long getNow() {
			return HinemosTime.currentTimeMillis();
		}
		
		long getKeepTime() {
			return HinemosPropertyCommon.access_authenticator_authtime.getNumericValue();
		}
		
		long getDefaultKeepTime() {
			return HinemosPropertyCommon.access_authenticator_authtime.getBean().getDefaultNumericValue();
		}
	}

	/** [単体テスト用] 外部依存処理 */
	private External external;

	/** 認証結果キャッシュのエントリ */
	private static class CacheEntry {
		boolean authed;
		String password;
		long time;
	}
	
	/** 認証結果キャッシュ */
	private Map<String, CacheEntry> cache = new HashMap<>();
	
	/** 直前の認証処理がキャッシュによる認証成功だった場合はtrue */
	private boolean cachedSuccess;

	public AuthResultCache() {
		this(new External());
	}
	
	/** [単体テスト用] 外部依存処理を指定するコンストラクタ */
	protected AuthResultCache(External external) {
		this.external = external;
	}

	/**
	 * 認証成功の結果がキャッシュの有効期間内にあればすぐに成功の結果を返し、
	 * 無効な場合は認証処理を呼び出してその結果をキャッシングします。
	 * 
	 * @param params 認証パラメータ。
	 * @param execute 認証処理。認証成功なら true、失敗なら falseを返してください。
	 * @return 認証成功なら true、失敗なら false。
	 */
	public boolean authenticate(AuthenticationParams params, Predicate<AuthenticationParams> execute) {
		cachedSuccess = false;

		long keepTime = getKeepTime();
		long now = external.getNow();
		CacheEntry entry = getCacheEntry(params.getUserId(), now, keepTime * 2);

		// キャッシュエントリ単位(ユーザID単位)で同期
		synchronized (entry) {
			// 認証実施不要？
			if (!needsToAuthenticate(params, entry, now, keepTime)) {
				cachedSuccess = true;
				return true;
			}

			// 最初に false にしておかないと、続く認証処理の呼び出しで例外が投げられた場合に true のままになってしまう
			entry.authed = false;
			// 認証を実行して、キャッシュエントリを更新する
			entry.authed = execute.test(params);
			entry.password = params.getPassword();
			entry.time = now;
			return entry.authed;
		}
	}
	
	/**
	 * キャッシュ保持時間[min]をバリデーションして、ms単位で返します。
	 */
	private long getKeepTime() {
		long value = external.getKeepTime();
		long defaultValue = external.getDefaultKeepTime();

		// 範囲外の値はデフォルト値にする
		if (value < MIN_KEEP_TIME || MAX_KEEP_TIME < value) {
			log.warn("getKeepTime: 'access.authenticator.authtime'(" + value +") is out of the range("
					+ MIN_KEEP_TIME + ".." + MAX_KEEP_TIME + "), use default(" + defaultValue + ").");
			value = defaultValue;
		}

		// 分単位なので 60_000 ms を掛ける
		return value * 60_000;
	}

	/**
	 * 認証結果キャッシュから、ユーザIDに対応したエントリを返します。
	 * <p>
	 * エントリを取得するに先立って、キャッシュ内の古いエントリを削除します。
	 * 指定されたユーザIDに対応するエントリが古いものであった場合、
	 * そのエントリを削除した上で、新規に作成したエントリを返します。
	 * 
	 * @param userId ユーザID。
	 * @param now 現在日時。
	 * @param expires 前回の認証試行(成否は問わない)からこの時間(ミリ秒)が経過したエントリを削除します。
	 * @return ユーザIDに対応したエントリ。
	 */
	private CacheEntry getCacheEntry(String userId, long now, long expires) {
		synchronized (cache) {
			// まずはキャッシュ掃除
			cache.entrySet().removeIf(e -> {
				CacheEntry v = e.getValue();
				if (v.time + expires < now) {
					log.debug("getCacheEntry: remove " + e.getKey() + ", " + v.time);
					return true;
				} else {
					return false;
				}
			});
	
			// キャッシュにユーザIDと対応するエントリがなければ新規作成する
			CacheEntry e = cache.get(userId);
			if (e == null) {
				e = new CacheEntry();
				e.authed = false;
				e.password = "";
				e.time = now;
				cache.put(userId, e);
			}
			return e;
		}
	}

	/**
	 * 各種条件判定を行い、認証実施が必要ならtrueを返します。 
	 */
	private boolean needsToAuthenticate(AuthenticationParams params, CacheEntry entry, long now,
			long keepTime) {
		
		// キャシュ無効なら必ず認証実施
		if (params.isCacheDisabled()) {
			log.debug("needsToAuthenticate: [" + params.getUserId() + "] cache disabled.");
			return true;
		}

		// 前回認証成功ユーザでなければ認証実施
		if (!entry.authed) {
			log.debug("needsToAuthenticate: [" + params.getUserId() + "] unauthorized.");
			return true;
		}

		// パスワードが前回と異なるなら認証実施
		if (!params.getPassword().equals(entry.password)) {
			log.debug("needsToAuthenticate: [" + params.getUserId() + "] password mismatch.");
			return true;
		}

		// 前回からの時間差がkeepTimeを超えているなら認証実施
		long elapsed = now - entry.time;
		if (elapsed > keepTime) {
			log.debug("needsToAuthenticate: [" + params.getUserId() + "] elapsed=" + elapsed + " > " + keepTime);
			return true;
		}

		// システムクロックが変更されるなどして、経過時間が負数になった場合も認証実施
		if (elapsed < 0) {
			log.info("needsToAuthenticate: [" + params.getUserId() + "] elapsed=" + elapsed
					+ " < 0, maybe the system clock is rewinded.");
			return true;
		}
		
		// いずれにも該当しなければ認証は実施不要
		log.debug("needsToAuthenticate: [" + params.getUserId() + "] cache hit. until=" + (entry.time + keepTime));
		return false;
	}

	/**
	 * キャッシュをクリアします。
	 */
	public void clear() {
		synchronized (cache) {
			List<String> removed = new ArrayList<>();
			cache.entrySet().removeIf(e -> {
				removed.add(e.getKey());
				return true;
			});
			log.info("clear: Removed " + removed.size() + " entries.");
			log.debug("clear: " + String.join(", ", removed));
		}
	}

	/**
	 * 直前の {@link #authenticate(AuthenticationParams, Predicate)} で、
	 * キャッシュから認証成功を返した場合は true を返します。
	 */
	public boolean wasCachedSuccess() {
		return cachedSuccess;
	}
	
	/** 
	 * 現在の認証結果キャッシュのエントリを、文字列のコレクションで返します。
	 * <p>
	 * 各文字列はエントリの以下の値を","で結合したものです。
	 * <ul>
	 * <li>ユーザID
	 * <li>認証成功なら"true", 失敗なら"false"
	 * <li>認証時刻を表すlong値
	 * </ui>
	 */
	public Collection<String> getCacheEntries() {
		List<String> ret = new ArrayList<>();
		synchronized (cache) {
			for (Entry<String, CacheEntry> entry : cache.entrySet()) {
				String userId = entry.getKey();
				CacheEntry ce = entry.getValue();
				ret.add(String.format("%s,%s,%d", userId, ce.authed, ce.time));
			}
		}
		return ret;
	}

}