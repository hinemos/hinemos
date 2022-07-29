/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.sql.SQLException;

import jakarta.persistence.PersistenceException;

/**
 * クエリに関数する操作を格納するクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class QueryDivergence {

	/**
	 * PostgreSQL エラーコードタイムアウト
	 * タイムアウト発生時に通知されるエラーコード
	 */
	private static final String SQLSTATE_QUERY_CANCELED = "57014";

	private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

	private static final String SQLSTATE_LOCK_NOT_AVAILABLE = "55P03";

	/**
	 * クエリのパラメータ数制限値(JDBCドライバのプレースホルダ上限32767 に抵触させないための閾値)
	 */
	private static final int QUERY_WHERE_IN_PARAM_THREASHOLD = 30000;	
	
	/**
	 * COUNTの結果の型の差分を吸収するためのメソッド。<br>
	 * Rhel：Long<br>
	 * Windowns：Integer<br>
	 *
	 * @param result
	 * @return
	 */
	public static Long countResult(Object result) {
		return (Long) result;
	}

	/**
	 * 渡された例外が、クエリ実行タイムアウト(実行キャンセル)により投げられたものかどうかを判定します。
	 *
	 * @param e 判定したい例外。
	 * @return クエリ実行タイムアウトなら true、そうでなければ false。
	 */
	public static boolean isQueryTimeout(PersistenceException e) {
		SQLException se = unwrapException(e);
		if (se == null) return false;
		return SQLSTATE_QUERY_CANCELED.equals(se.getSQLState());
	}

	/**
	 * 渡された例外が、データベースの一意制約違反により投げられたものかどうかを判定します。
	 *
	 * @param e 判定したい例外。
	 * @return 一意制約違反なら true、そうでなければ false。
	 */
	public static boolean isUniqueKeyViolation(Exception e) {
		SQLException se = unwrapException(e);
		if (se == null) return false;
		return SQLSTATE_UNIQUE_VIOLATION.equals(se.getSQLState());
	}

	/**
	 * 渡された例外が、データベースのロック失敗により投げられたものかどうかを判定します。
	 *
	 * @param e 判定したい例外。
	 * @return ロック失敗 true、そうでなければ false。
	 */
	public static boolean isLockFailure(Exception e) {
		SQLException se = unwrapException(e);
		if (se == null) return false;
		return SQLSTATE_LOCK_NOT_AVAILABLE.equals(se.getSQLState());
	}

	/**
	 * 渡されたExceptionの cause をさかのぼって、SQLExceptionを探します。
	 * 見つからなかった場合は null を返します。
	 */
	private static SQLException unwrapException(Exception e) {
		Throwable t = e;
		while (true) {
			if (t instanceof SQLException) {
				return (SQLException) t;
			}
			t = t.getCause();
			if (t == null) return null;
		}
	}

	/**
	 * LIKE文に関するエスケープ処理を施すメソッド。<br>
	 * %のみ、部分一致検索を実現するため、エスケープしない。<br>
	 *
	 * @param target
	 * @return
	 */
	public static String escapeLikeCondition(String target) {
		// 念の為、チェック
		if (target == null || target.length() == 0) {
			return target;
		}
		String editTarget = "";
		editTarget = escapeWildCards(target);
		return editTarget;
	}

	/**
	 * ノードマップのLIKE文に関するエスケープ処理を施すメソッド。<br>
	 * 入力されているワイルドカードと前方一致検索の都合、
	 * 末尾に"\"が入力されている場合の対応を施す。<br>
	 *
	 * @param target
	 * @return
	 */
	public static String escapeConditionNodeMap(String target) {
		// 念の為、チェック
		if (target == null || target.length() == 0) {
			return target;
		}
		String editTarget = "";
		editTarget = escapeWildCards(target);
		// 部分一致検索を行わせないため、エスケープ
		editTarget = editTarget.replace("%", "\\%");
		// 末尾"\"のチェック
		if (editTarget.endsWith("\\")) {
			// "\"のエスケープを追加
			editTarget = editTarget + "\\";
		}
		return editTarget;
	}

	/**
	 * ジョブ連携待機ジョブ、ジョブ連携受信実行契機LIKE文に関するエスケープ処理を施すメソッド。<br>
	 * 末尾の%だけエスケープしない<br>
	 *
	 * @param target
	 * @return
	 */
	public static String escapeConditionJobLinkRcv(String target) {
		String editTarget = "";
		boolean existsWildcard = false;

		if (target == null || target.length() == 0) {
			return target;
		}
		if (target.endsWith("%")) {
			editTarget = escapeWildCards(target.substring(0, target.length() - 1));
			existsWildcard = true;
		} else {
			editTarget = escapeWildCards(target);
		}

		// 部分一致検索を行わせないため、エスケープ
		editTarget = editTarget.replace("%", "\\%");
		// 末尾"\"のチェック
		if (editTarget.endsWith("\\")) {
			// "\"のエスケープを追加
			editTarget = editTarget + "\\";
		}
		if (existsWildcard) {
			return editTarget + "%";
		} else {
			return editTarget;
		}
	}

	/**
	 * アンダースコアのエスケープを実施する。
	 * @param target
	 * @return
	 */
	private static String escapeWildCards(String target) {
		// アンダースコアのエスケープを実施する。
		target = target.replace("_", "\\_");
		return target;
	}
	
	/**
	 * クエリのパラメータ数制限値(JDBCドライバのプレースホルダ上限32767 に抵触させないための閾値)
	 */
	public static int getQueryWhereInParamThreashold() {
		return QUERY_WHERE_IN_PARAM_THREASHOLD;
	}
}