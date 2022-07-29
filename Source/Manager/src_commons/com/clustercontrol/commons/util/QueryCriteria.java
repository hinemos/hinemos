/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.clustercontrol.util.FilterConstant;

import jakarta.persistence.TypedQuery;

/**
 * クエリの条件式を定義・構築します。
 * <ul>
 * <li>条件式の定義を保持します。
 * <li>JPQLへ埋め込む条件式の文字列を生成します。
 * <li>生成したQueryオブジェクトへパラメータ値を設定します。
 * <li>Javaオブジェクトに対して、JPQLの条件式と同等の比較を行います。
 * </ul>
 * <p>
 * <strong>使用イメージ</strong>
 * 
 * <pre>
 * public class YourClass {
 *     
 *     // サブクラスを導出して、そのpublicフィールドで条件式の種類と対象プロパティを定義する。
 *     private static class YourCriteria extends QueryCriteria {
 *         // イコール条件式
 *         public Equal&lt;String> userId = new Equal&lt;>("user.id");
 *         
 *         // LIKE条件式
 *         public Like userName = new Like("lower(user.name)")
 *         
 *         YourCriteria(String uniqueId) {
 *             super(uniqueId);
 *         }
 *     }
 *     
 *     public List&lt;User&gt; query() {
 *         // uniqueIdはクエリ内のパラメータ名のプレフィクスで使用されるので、1クエリ内でユニークであればよい。
 *         YourCriteria crt = new YourCriteria("hoge");
 *         
 *         // 条件式で使用する値を設定する。
 *         crt.userId.setValue("1201");
 *         crt.userName.setPattern("hinemos");
 *         
 *         // 条件式を構築する。
 *         String jpql = "SELECT * FROM user_table user WHERE " + crt.buildExpressions();
 *         TypedQuery&lt;User&gt; typedQuery = ...
 *         
 *         // 作成したTypedQueryへ、さきほどセットした条件値を流し込む。
 *         crt.submitParameters(typedQuery);
 *
 *         // クエリ実行
 *         return typedQuery.getResultList();
 *     }
 *     
 *     public void check(SomeDto dto) {
 *         // 各条件はJavaオブジェクトとの比較もできる。
 *         YourCriteria crt = new YourCriteria("a");
 *         crt.userName.setPattern("hine%");
 *         
 *         if (crt.userName.matches(dto.userName)) {
 *           ....
 * </pre>
 */
public abstract class QueryCriteria {

	// private static final Log logger = LogFactory.getLog(QueryCriteria.class);
	private static final String DEFAULT_UNIQUE_ID = "P";

	/** インスタンスのユニークID */
	private final String uniqueId;
	/** 条件フィールドのリスト */
	private final List<Condition> conditions;

	/** パラメータ番号採番カウンタ */
	private int paramPos;
	/** trueの場合、全条件を反転する */
	private boolean negative;

	/**
	 * コンストラクタ。
	 * 
	 * @param uniqueId
	 * 		SQL式を構築する際に、SQLパラメータ名のプレフィックスとして使用します。
	 * 		複数インスタンスを使用して、1つのSQLを構築する場合に、各インスタンスで重複しないものを指定してください。
	 */
	public QueryCriteria(String uniqueId) {
		if (uniqueId == null || uniqueId.isEmpty()) {
			uniqueId = DEFAULT_UNIQUE_ID;
		}
		this.uniqueId = uniqueId;
		this.conditions = new ArrayList<>();
		this.paramPos = 1;
		this.negative = false;
	}

	/**
	 * コンストラクタ。<br/>
	 * 複数インスタンスを使用して、1つのSQLを構築する場合は {@link #QueryCriteria(String)} を使用してください。
	 */
	public QueryCriteria() {
		this(DEFAULT_UNIQUE_ID);
	}

	/**
	 * 全条件の結果を反転する(true)/しない(false)を設定します。
	 */
	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	/**
	 * 全条件の結果を反転する(true)/しない(false)を返します。
	 */
	public boolean isNegative() {
		return negative;
	}

	/**
	 * SQLのWHERE句へ条件式として埋め込み可能な文字列を構築して返します。
	 */
	public String buildExpressions() {
		// リフレクションにより、条件フィールドをリストアップ (初回のみ)
		if (conditions.size() == 0) {
			Field[] fields;
			String[] fieldNames = getConditionFieldNames();
			if (fieldNames.length > 0) {
				// getConditionFieldNamesでの指定があれば、そのフィールドを取得する 
				fields = new Field[fieldNames.length];
				for (int i = 0; i < fieldNames.length; ++i) {
					try {
						fields[i] = getClass().getDeclaredField(fieldNames[i]);
					} catch (NoSuchFieldException | SecurityException e) {
						// プログラミングエラーしかありえないはず
						throw new RuntimeException(e);
					}
					fields[i].setAccessible(true);
				}
			} else {
				// getConditionFieldNamesでの指定がないなら、publicフィールドをすべて取得する
				fields = getClass().getFields();
			}
			// 検出したフィールドから条件オブジェクトを取ってきて保存する
			for (Field f : fields) {
				if (Condition.class.isAssignableFrom(f.getType())) {
					try {
						conditions.add((Condition) f.get(this));
					} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
						// プログラミングエラーしかありえないはず
						throw new RuntimeException(e);
					}
				}
			}
			if (conditions.size() == 0) {
				// 条件オブジェクトが取得できない場合、フィールド指定の誤りと考えられる
				throw new RuntimeException("Couldn't find condition fields.");
			}
		}
		// 各条件フィールドに式を生成させて AND 結合
		StringBuilder buff = new StringBuilder();
		paramPos = 1;
		for (Condition cnd : conditions) {
			String expr = cnd.buildExpressions();
			if (expr != null && !expr.isEmpty()) {
				if (buff.length() > 0) buff.append(" AND ");
				buff.append(expr);
			}
		}
		// 呼び出し元が条件がない場合を特別扱いしなくてすむように、最低限1つは式を作る
		if (buff.length() == 0) {
			buff.append("1 = 1"); // 必ず成立
		}

		// 条件全反転ならNOTで囲む
		if (negative) {
			buff.insert(0, "NOT (");
			buff.append(")");
		}

		return buff.toString();
	}

	/**
	 * {@link #buildExpressions()}で埋め込んだSQL条件式をもとに作成した{@link TypedQuery}へ、
	 * 実パラメータをセットします。
	 */
	public <T> TypedQuery<T> submitParameters(TypedQuery<T> query) {
		for (Condition cnd : conditions) {
			cnd.submitParameters(query);
		}
		return query;
	}

	/**
	 * [オーバーライド用]
	 * 条件フィールドを、リフレクションによるpublicフィールドの自動走査ではなく、
	 * 明示的に指定する場合、そのフィールド名の配列を返します。
	 */
	protected String[] getConditionFieldNames() {
		return new String[0];
	}

	/**
	 * 修飾表現可能な条件値を表します。
	 * 現在は条件反転のみ対応しています。
	 */
	private static class DecoValue {
		private String value = null;
		private boolean negates = false;

		private DecoValue(String v) {
			v = emptyToNull(v);
			if (v == null) return;
			negates = v.startsWith(FilterConstant.NEGATION_PREFIX);
			if (negates) {
				value = v.substring(FilterConstant.NEGATION_PREFIX.length());
			} else {
				value = v;
			}
		}
	}

	/**
	 * {@link DecoValue} の集合を表します。
	 * 現在は AND 結合のみ対応しています。
	 */
	private static class DecoValues {
		private List<DecoValue> values = new ArrayList<>();

		private DecoValues(String v) {
			v = emptyToNull(v);
			if (v == null) return;
			for (String vv : v.split(FilterConstant.AND_SEPARATOR)) {
				values.add(new DecoValue(vv));
			}
		}

		/**
		 * 集合値それぞれについて func を呼び出し、
		 * func からの戻り値をSQLの条件式として結合して返します。
		 */
		private String joinEachSQL(Function<DecoValue, String> func) {
			if (values.size() == 0) return "";
			StringBuilder buff = new StringBuilder();
			for (DecoValue v : values) {
				if (v.value == null) continue;
				if (buff.length() > 0) {
					buff.append(" AND ");
				}
				buff.append(func.apply(v));
			}
			return buff.toString();
		}

		/**
		 * 集合値それぞれについて judge を呼び出し、
		 * judge からの戻り値を総合的に評価した結果(現在は「AND ＝ 全てが成立ならtrue」のみ)を返します。
		 */
		private boolean evaluateEachPredicate(Predicate<DecoValue> judge) {
			if (values.size() == 0) return true;
			for (DecoValue v : values) {
				if (v.value == null) continue;
				if (!judge.test(v)) return false;
			}
			return true;
		}
	}

	/**
	 * 空文字列を null にします。
	 */
	static <T> T emptyToNull(T o) {
		if (o == null) return null;
		if (o instanceof String) {
			return ((String) o).isEmpty() ? null : o;
		}
		return o;
	}

	/**
	 * PostgreSQLのLIKEオペレーターと同等の文字列比較を行います。
	 */
	static boolean matchesLikePgsql(String pattern, String subject) {
		if (pattern == null) return true;
		if (subject == null) return false;

		if (pattern.contains("%")) {
			// LIKEパターンを同等の正規表現へ変換します。
			// 具体的には "%" を ".*" へ置き換え、
			// その他の部分をブロックエスケープ "\Q～\E" で括ります。
			StringBuilder regex = new StringBuilder();
			regex.append("\\Q");
			boolean esc = false;
			for (int i = 0; i < pattern.length(); ++i) {
				char c = pattern.charAt(i);
				if (c == '\\' && !esc) {
					esc = true;
				} else if (c == '%' && !esc) {
					regex.append("\\E.*\\Q");
					// } else if (c == '_' && !esc) {
					// regex.append("\\E.\\Q");
				} else {
					regex.append(c);
					esc = false;
				}
			}
			regex.append("\\E");
			return Pattern.matches(regex.toString(), subject);
		} else {
			return pattern.equals(subject);
		}
	}

	private abstract class Condition {
		protected final String propertyName;
		private int startParamPos;
		private List<Object> arguments;

		private Condition(String propertyName) {
			this.propertyName = propertyName;
			startParamPos = -1;
			arguments = Collections.emptyList();
		}

		private String buildExpressions() {
			startParamPos = paramPos;
			arguments = new ArrayList<>();
			return buildExprSub();
		}

		/** [オーバーライド用] 条件式のSQLを構築します。 */
		protected abstract String buildExprSub();

		protected String generateParamName(Object argument) {
			arguments.add(argument);
			return ":" + uniqueId + (paramPos++);
		}

		private void submitParameters(TypedQuery<?> query) {
			int pos = startParamPos;
			for (Object arg : arguments) {
				query.setParameter(uniqueId + (pos++), arg);
			}
		}
	}

	/**
	 * パターン一致(LIKE)条件。
	 */
	public class Like extends Condition {
		private DecoValues vals;

		/**
		 * パターン一致(LIKE)条件を生成します。
		 * 
		 * @param propertyName 対応するSQLの列名(JPQLのプロパティ名)。
		 */
		public Like(String propertyName) {
			super(propertyName);
			vals = null;
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * null を指定した場合、条件無効(比較する値に関わらず成立)となります。
		 * <p>
		 * 以下の機能に対応しています。
		 * <ul>
		 * <li>文字「%」は任意の文字列と一致します。
		 * <li>先頭に {@link FilterConstant#NEGATION_PREFIX} を付けると「一致しない」条件になります。
		 * <li>{@link FilterConstant#AND_SEPARATOR} により AND 結合で条件を追加できます。
		 * </ul>
		 */
		public void setPattern(String pattern) {
			vals = new DecoValues(pattern);
		}

		@Override
		protected String buildExprSub() {
			if (vals == null || vals.values.size() == 0) return "";
			return vals.joinEachSQL(v -> {
				return propertyName
						+ (v.negates ? " NOT" : "") + " LIKE "
						+ generateParamName(QueryDivergence.escapeLikeCondition(v.value));
			});
		}

		/**
		 * DB内の値ではなく、Javaの文字列と比較して、結果を返します。
		 */
		public boolean matches(String subject) {
			if (vals == null || vals.values.size() == 0) return true;
			return vals.evaluateEachPredicate(v -> {
				boolean matched = matchesLikePgsql(v.value, subject);
				return (matched && !v.negates) || (!matched && v.negates);
			});
		}
	}

	/**
	 * イコール(=)条件。
	 */
	public class Equal<T extends Comparable<T>> extends Condition {
		private T value;

		/**
		 * イコール(=)条件を生成します。
		 * 
		 * @param propertyName 対応するSQLの列名(JPQLのプロパティ名)。
		 */
		public Equal(String propertyName) {
			super(propertyName);
			value = null;
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * null を指定した場合、条件無効(比較する値に関わらず成立)となります。
		 */
		public void setValue(T value) {
			this.value = value;
		}

		@Override
		protected String buildExprSub() {
			if (value == null) return "";
			return propertyName + " = " + generateParamName(value);
		}

		/**
		 * DB内の値ではなく、Javaのオブジェクトと比較して、結果を返します。
		 */
		public boolean isEqualTo(T subject) {
			if (value == null) return true;
			if (subject == null) return false;
			// BigDecimal を選択した場合に、equals では精度とスケールの完全一致比較になるので、compareTo で比較する必要がある。
			return value.compareTo(subject) == 0;
		}
	}

	/**
	 * グループメンバー(IN)条件。
	 */
	public class In<T extends Comparable<T>> extends Condition {
		private Collection<T> values;
		private int maxCount;

		/**
		 * グループメンバー(IN)条件を生成します。
		 * 
		 * @param propertyName 対応するSQLの列名(JPQLのプロパティ名)。
		 * @param maxCount グループメンバーの総数が決まっている場合はその数。
		 */
		public In(String propertyName, int maxCount) {
			super(propertyName);
			this.maxCount = maxCount;
			values = null;
		}

		/**
		 * グループメンバー(IN)条件を生成します。
		 * 
		 * @param propertyName 対応するSQLの列名(JPQLのプロパティ名)。
		 */
		public In(String propertyName) {
			this(propertyName, Integer.MAX_VALUE);
		}

		/**
		 * このメソッドで設定するリスト値と、DB値(またはJavaオブジェクト)を比較します。
		 * null またはコンストラクタで指定した総数と同じサイズのコレクションを指定した場合、条件無効(比較する値に関わらず成立)となります。
		 * 空のコレクションを指定した場合、比較する値に関わらず条件不成立となります。
		 * <p>
		 * 渡されたコレクションをそのまま内部に保持して利用します。
		 * 要素数が多い場合は {@link Collection#contains(Object)} メソッドの性能が良い実装を使用すると
		 * {@link #contains(Object)} の性能が良くなります。
		 */
		public void setValues(Collection<T> values) {
			this.values = values;
		}

		/**
		 * {@link #setValues(Collection)}された値を返します。
		 */
		public Collection<T> getValues() {
			return values;
		}

		@Override
		protected String buildExprSub() {
			// 条件値の指定がない or 取りうる全パターンが揃っている場合は条件を付ける必要がない
			if (values == null || values.size() >= maxCount) {
				return "";
			}

			// 空のリストの場合は絶対に成立しない
			if (values.size() == 0) {
				return "1 = 0";
			}

			StringBuilder buff = new StringBuilder();
			buff.append(propertyName).append(" IN (");
			String delim = "";
			for (T value : values) {
				buff.append(delim).append(generateParamName(value));
				delim = ",";
			}
			buff.append(")");
			return buff.toString();
		}

		/**
		 * DB内の値ではなく、Javaのオブジェクトと比較して、結果を返します。
		 */
		public boolean contains(T subject) {
			if (values == null) return true;
			if (subject == null) return false;
			if (values.size() >= maxCount) return true;
			for (T v : values) {
				// BigDecimal を選択した場合に、equals では精度とスケールの完全一致比較になるので、compareTo で比較する必要がある。
				if (v.compareTo(subject) == 0) return true;
			}
			return false;
		}
	}

	/**
	 * 範囲(from <= x <= to)条件。
	 */
	public class Range<T extends Comparable<T>> extends Condition {
		private T from;
		private T to;

		/**
		 * 範囲(from <= x <= to)条件を生成します。
		 * 
		 * @param propertyName 対応するSQLの列名(JPQLのプロパティ名)。
		 */
		public Range(String propertyName) {
			super(propertyName);
			from = null;
			to = null;
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * 最小値と最大値を同時に指定します。
		 * null を指定した場合、条件無効(DB値に関わらず成立)となります。
		 */
		public void setFromTo(T from, T to) {
			setFrom(from);
			setTo(to);
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * 最小値のみ指定します。
		 * null を指定した場合、条件無効(比較する値に関わらず成立)となります。
		 */
		public void setFrom(T from) {
			this.from = emptyToNull(from);
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * 最大値のみ指定します。
		 * null を指定した場合、条件無効(比較する値に関わらず成立)となります。
		 */
		public void setTo(T to) {
			this.to = emptyToNull(to);
		}

		@Override
		protected String buildExprSub() {
			StringBuilder buff = new StringBuilder();
			if (from != null) {
				buff.append(propertyName).append(" >= ").append(generateParamName(from));
			}
			if (to != null) {
				if (buff.length() > 0) buff.append(" AND ");
				buff.append(propertyName).append(" <= ").append(generateParamName(to));
			}
			return buff.toString();
		}

		/**
		 * DB内の値ではなく、Javaのオブジェクトと比較して、結果を返します。
		 */
		public boolean contains(T subject) {
			if (subject == null) return false;
			if (from != null) {
				if (from.compareTo(subject) > 0) return false;
			}
			if (to != null) {
				if (to.compareTo(subject) < 0) return false;
			}
			return true;
		}
	}

	/**
	 * 時刻範囲(from <= x <= to)条件。<br/>
	 * {@link Range}との違いは、秒精度での比較を行う点です。
	 * すなわち、from のミリ秒部分を 0 に、to のミリ秒部分を 999 に強制設定して範囲比較します。<br/>
	 * ミリ秒精度での比較が必要な場合は、{@link Range}を使用してください。
	 */
	public class Period extends Condition {
		private Long from;
		private Long to;

		/**
		 * 時刻範囲(from <= x <= to)条件を生成します。
		 * 
		 * @param propertyName 対応するSQLの列名(JPQLのプロパティ名)。
		 */
		public Period(String propertyName) {
			super(propertyName);
			from = null;
			to = null;
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * (設定値には、時刻範囲特有の補正が掛かります。) 
		 * 最小値と最大値を同時に指定します。
		 * null を指定した場合、条件無効(DB値に関わらず成立)となります。
		 */
		public void setFromTo(Long from, Long to) {
			setFrom(from);
			setTo(to);
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * (設定値には、時刻範囲特有の補正が掛かります。) 
		 * 最小値を指定します。
		 * null を指定した場合、条件無効(DB値に関わらず成立)となります。
		 */
		public void setFrom(Long from) {
			if (from != null) {
				from -= (from % 1000);  // ミリ秒の位を 000 にする
			}
			this.from = from;
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * (設定値には、時刻範囲特有の補正が掛かります。) 
		 * 最大値を指定します。
		 * null を指定した場合、条件無効(DB値に関わらず成立)となります。
		 */
		public void setTo(Long to) {
			if (to != null) {
				to += 999 - (to % 1000);  // ミリ秒の位を 999 にする
			}
			this.to = to;
		}

		@Override
		protected String buildExprSub() {
			StringBuilder buff = new StringBuilder();
			if (from != null) {
				buff.append(propertyName).append(" >= ").append(generateParamName(from));
			}
			if (to != null) {
				if (buff.length() > 0) buff.append(" AND ");
				buff.append(propertyName).append(" <= ").append(generateParamName(to));
			}
			return buff.toString();
		}

		/**
		 * DB内の値ではなく、JavaのLong値と比較して、結果を返します。
		 */
		public boolean contains(Long subject) {
			if (subject == null) return false;
			if (from != null) {
				if (from.compareTo(subject) > 0) return false;
			}
			if (to != null) {
				if (to.compareTo(subject) < 0) return false;
			}
			return true;
		}
	}

	/**
	 * 連番が付与されたプロパティのパターン一致(LIKE)条件。
	 * <p>
	 * 現在のところ、イベント履歴データのユーザ定義項目のための条件です。
	 */
	public class NumberedLike extends Condition {
		private Map<Integer, DecoValues> map = new LinkedHashMap<>();

		/**
		 * 連番が付与されたプロパティのパターン一致(LIKE)条件を生成します。
		 * 
		 * @param propertyName
		 * 		対応するSQLの列名(JPQLのプロパティ名)。
		 * 		この値を{@link String#format(String, Object...)}の第1引数、
		 * 		連番を第2引数としてフォーマットしたものが実際に埋め込まれます。
		 */
		public NumberedLike(String propertyName) {
			super(propertyName);
		}

		/**
		 * このメソッドで設定する値と、DB値(またはJavaオブジェクト)を比較します。
		 * null を指定した場合、条件無効(比較する値に関わらず成立)となります。
		 * <p>
		 * 以下の機能に対応しています。
		 * <ul>
		 * <li>文字「%」は任意の文字列と一致します。
		 * <li>先頭に {@link FilterConstant#NEGATION_PREFIX} を付けると「一致しない」条件になります。
		 * <li>{@link FilterConstant#AND_SEPARATOR} により AND 結合で条件を追加できます。
		 * </ul>
		 * 
		 * @param number 項目の連番。
		 */
		public void setValue(int number, String value) {
			DecoValues vals = new DecoValues(value);
			map.put(number, vals);
		}

		/**
		 * 有効な条件値の設定されている連番のリストを返します。
		 */
		public List<Integer> getNumbers() {
			List<Integer> rtn = new ArrayList<>();
			for (Entry<Integer, DecoValues> et : map.entrySet()) {
				if (et.getValue().values.size() > 0) {
					rtn.add(et.getKey());
				}
			}
			return rtn;
		}

		@Override
		protected String buildExprSub() {
			StringBuilder buff = new StringBuilder();
			for (Entry<Integer, DecoValues> entry : map.entrySet()) {
				DecoValues vals = entry.getValue();
				if (vals.values.size() == 0) continue;
				if (buff.length() > 0) {
					buff.append(" AND ");
				}
				buff.append(vals.joinEachSQL(v -> {
					String col = String.format(propertyName, entry.getKey().intValue());
					String prm = generateParamName(QueryDivergence.escapeLikeCondition(v.value));
					if (v.negates) {
						return col + " NOT LIKE " + prm;
					} else {
						return col + " LIKE " + prm;
					}
				}));
			}
			return buff.toString();
		}

		/**
		 * DB内の値ではなく、Javaの文字列と比較して、結果を返します。
		 * 
		 * @param number 項目の連番。
		 */
		public boolean matches(int number, String subject) {
			if (subject == null) return false;
			DecoValues vals = map.get(number);
			if (vals == null || vals.values.size() == 0) return true;
			return vals.evaluateEachPredicate(v -> {
				boolean matched = matchesLikePgsql(v.value, subject);
				return (matched && !v.negates) || (!matched && v.negates);
			});
		}
	}

}
