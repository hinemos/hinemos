/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.TypedQuery;

/**
 * データベース検索条件を構築します。
 * 
 * @since 6.2.0
 */
public class CriteriaBuilder {

	/**
	 * 比較条件の種類。
	 */
	public static enum OperationType {
		LIKE, FROM, TO, EQUAL,
	};

	public CriteriaBuilder() {
		this.elems = new ArrayList<>();
	}
	
	/**
	 * 条件を追加します。
	 * 
	 * @param entityName 比較対象のエンティティの名前。。
	 * @param propertyName 比較対象のプロパティの名前。
	 * @param value 比較する値。null(文字列比較の場合は空文字列も含む)の場合、この条件は無効となります。
	 * @param opType 比較条件の種類。
	 * @return このインスタンス自身を返します。
	 */
	public CriteriaBuilder add(String entityName, String propertyName, Object value, OperationType opType) {
		elems.add(new Element(elems.size(), entityName, propertyName, value, opType));
		return this;
	}
	
	/**
	 * JPQLの条件式を返します。
	 * 
	 * @param entityAlias FROM句でエンティティとエイリアスをどのように対応させているかを指定する配列です。<br/>
	 *        "{エンティティ}:{エイリアス}"の形式で指定します。<br/>
	 *        例えば、"SomeEntity"を"a"とした場合、
	 *        引数は <code>"SomeEntity:a"</code> となります。
	 */
	public String build(String entityAlias) {
		return build(Arrays.asList(entityAlias));
	}

	/**
	 * JPQLの条件式を返します。
	 * 
	 * @param entityAlias FROM句でエンティティとエイリアスをどのように対応させているかを指定する配列です。<br/>
	 *        "{エンティティ}:{エイリアス}"の形式で指定します。<br/>
	 *        例えば、"SomeEntity"を"a"、"AnotherEntity"を"b"とした場合、
	 *        引数は <code>{"SomeEntity:a", "AnotherEntity:b"}</code> となります。
	 */
	public String build(List<String> entityAlias) {
		// エンティティ-エイリアス対応リストをmapへ変換する
		Map<String, String> e2a = new HashMap<>();
		for (String it : entityAlias) {
			String[] ea = it.split(":");
			if (ea.length == 2) {
				e2a.put(ea[0], ea[1]);
			} else {
				throw new IllegalArgumentException("Illegal format. value=" + it);
			}
		}

		// 各フィルタ要素へ条件式を書き出させる
		StringBuilder buff = new StringBuilder();
		elems.forEach(f -> {
			f.appendPredicate(buff, e2a);
		});
		if (buff.length() == 0) {
			buff.append("1=1"); // 必ずtrue
		}
		return "(" + buff.toString() + ")";
	}

	/**
	 * {@link #build()}で生成した条件式のパラメータへ、各フィルタ要素の値を設定します。
	 */
	public <T> TypedQuery<T> fillParameters(TypedQuery<T> q) {
		elems.forEach(f -> {
			f.setParameter(q);
		});
		return q;
	}
	
	private List<Element> elems;

	/** 検索条件を表すクラス。 */
	private static class Element {
		/** 値にこのプレフィクスがついている場合は、not演算とする。 (文字列のみ)*/
		private static final String NOT_PREFIX = "NOT:";

		private String entityName;
		private String propertyName;
		private String paramName;
		private Object value;
		private OperationType type;
		private boolean negates;
		private boolean empty;

		public Element(int index, String entityName, String propertyName, Object value, OperationType type) {
			this.entityName = entityName;
			this.propertyName = propertyName;
			this.paramName = "p" + index;
			this.value = value;
			this.type = type;
			this.negates = false;
			this.empty = (value == null);

			if (value instanceof String) {
				String s = (String) value;
				if (s.startsWith(NOT_PREFIX)) {
					this.negates = true;
					this.value = s = s.substring(NOT_PREFIX.length());
				}

				if (s.isEmpty()) this.empty = true;
			}
		}

		/**
		 * JPQLの条件式を、指定されたStringBuilderへ追記します。
		 */
		public void appendPredicate(StringBuilder buff, Map<String, String> entity2Alias) {
			if (empty) return;

			if (buff.length() > 0) {
				buff.append(" AND ");
			}

			String col = entity2Alias.get(entityName) + "." + propertyName;

			// 余計な干渉をしないように、カッコで括る。
			buff.append('(');

			switch (type) {
			case LIKE:
				buff.append(col);
				if (negates) buff.append(" NOT");
				buff.append(" LIKE :");
				buff.append(paramName);
				break;
			case FROM:
				buff.append(col);
				buff.append(">= :");
				buff.append(paramName);
				break;
			case TO:
				buff.append(col);
				buff.append("<= :");
				buff.append(paramName);
				break;
			case EQUAL:
				buff.append(col);
				buff.append(negates ? " <> " : " = ");
				buff.append(":");
				buff.append(paramName);
				break;
			}

			buff.append(')');
		}

		/**
		 * {@link #appendPredicate(StringBuilder)}で追記した条件式に対応するパラメータへ、値を設定します。
		 */
		public void setParameter(TypedQuery<?> query) {
			if (empty) return;
			query.setParameter(paramName, value);
		}
	}

}
