/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.clustercontrol.bean.Property;
import com.clustercontrol.rest.dto.EnumDto;

/**
 * 特定のPropertyオブジェクトに対して、連続して操作を行う場合のコードを簡略化します。
 * 
 * @since 6.2.0
 */
public class PropertyWrapper {

	private final Property target;
	private SimpleDateFormat sdf;

	public PropertyWrapper(Property target) {
		this.target = target;
		// findbugs対応 同期化の一貫性を担保
		synchronized (this) {
			sdf = null;
		}
	}

	/**
	 * {@link #findTimeString(String)} で使用する日付フォーマッタを指定します。
	 * 本メソッドで指定しない場合は {@link TimezoneUtil#getSimpleDateFormat()} を使用します。
	 */
	public void setSimpleDateFormat(SimpleDateFormat sdf) {
		// findbugs対応 同期化の一貫性を担保
		synchronized (this) {
			this.sdf = sdf;
		}
	}

	private SimpleDateFormat sdf() {
		// 遅延初期化
		// findbugs対応 同期化の一貫性を担保
		synchronized (this) {
			if (sdf == null) {
				sdf = TimezoneUtil.getSimpleDateFormat();
			}
			return sdf;
		}
	}

	/**
	 * 指定されたIDで、String型の値を持つプロパティを再帰検索し、最初に見つかった値を返します。
	 * 
	 * @param id 検索対象のID。
	 * @return 見つかった場合はString値、見つからなかった場合はnull。
	 */
	public String findString(String id) {
		return PropertyUtil.findStringValue(target, id);
	}

	/**
	 * 指定されたIDで、String型の値を持つプロパティを再帰検索し、最初に見つかった空文字列でない値を返します。
	 * 
	 * @param id 検索対象のID。
	 * @return 見つかった場合はString値、見つからなかった場合はnull。
	 */
	public String findNonEmptyString(String id) {
		return PropertyUtil.findNonEmptyStringValue(target, id);
	}

	/**
	 * 指定されたIDで、String型の値を持つプロパティを再帰検索し、空文字列でない値をリスト化して返します。
	 * 
	 * @param id 検索対象のID。
	 * @return 空文字列でない値のリスト。
	 */
	public List<String> findNonEmptyStrings(String id) {
		return PropertyUtil.findNonEmptyStringValues(target, id);
	}

	/**
	 * 指定されたIDで、String型の値を持つプロパティを再帰検索し、空文字列でない値を結合して返します。
	 * 
	 * @param id 検索対象のID。
	 * @param delim デリミタ。
	 * @return 結合された文字列。空文字列でない値が1件も見つからない場合はnull。
	 */
	public String joinNonEmptyStrings(String id, String delim) {
		StringBuilder buff = new StringBuilder();
		for (String s : PropertyUtil.findNonEmptyStringValues(target, id)) {
			if (buff.length() > 0) buff.append(delim);
			buff.append(s);
		}
		if (buff.length() == 0) return null;
		return buff.toString();
	}

	/**
	 * 指定されたIDで、Long型の値を持つプロパティを再帰検索し、最初に見つかった値を返します。
	 * 
	 * @param id 検索対象のID。
	 * @return 見つかった場合はLong値、見つからなかった場合はnull。
	 */
	public Long findLong(String id) {
		return PropertyUtil.findLongValue(target, id);
	}

	/**
	 * 指定されたIDで、Integer型の値を持つプロパティを再帰検索し、最初に見つかった値を返します。
	 * 
	 * @param id 検索対象のID。
	 * @return 見つかった場合はInteger値、見つからなかった場合はnull。
	 */
	public Integer findInteger(String id) {
		return PropertyUtil.findIntegerValue(target, id);
	}

	/**
	 * 指定されたIDで、Boolean型の値を持つプロパティを再帰検索し、最初に見つかった値を返します。
	 * 
	 * @param id 検索対象のID。
	 * @return 見つかった場合はBoolean値、見つからなかった場合はnull。
	 */
	public Boolean findBoolean(String id) {
		return PropertyUtil.findBooleanValue(target, id);
	}

	/**
	 * 指定されたIDで、Date型の値を持つプロパティを再帰検索し、最初に見つかった値をlong変換して返します。
	 * 
	 * @param id 検索対象のID。
	 * @return 見つかった場合はlong値、見つからなかった場合はnull。
	 */
	public Long findTime(String id) {
		return PropertyUtil.findTimeValue(target, id);
	}

	/**
	 * 指定されたIDで、Date型の値を持つプロパティを再帰検索し、最初に見つかった値をlong変換し、
	 * さらに下3桁(ミリ秒の部分)を"999"に設定した値を返します。
	 * <p>
	 * 日時範囲の終端値を取得したい場合に、本メソッドを使用します。
	 * 
	 * @param id 検索対象のID。
	 * @return 見つかった場合はlong値、見つからなかった場合はnull。
	 */
	public Long findEndTime(String id) {
		return PropertyUtil.findEndTimeValue(target, id);
	}

	/**
	 * 指定されたIDで、Date型の値を持つプロパティを再帰検索し、最初に見つかった値をString変換して返します。
	 * 
	 * @param id 検索対象のID。
	 * @return 見つかった場合はString値、見つからなかった場合はnull。
	 */
	public String findTimeString(String id) {
		return PropertyUtil.findTimeStringValue(target, id, sdf());
	}

	/**
	 * 指定されたIDで、指定された型の値を持つプロパティを再帰検索し、リスト化して返します。
	 * 
	 * @param id 検索対象のID。
	 * @param classOfValue 検索する型。
	 * @return 値のリスト。
	 */
	public <T> List<T> findValue(String id, Class<T> classOfValue) {
		List<T> rtn = new ArrayList<>();
		for (Object v : PropertyUtil.getPropertyValue(target, id)) {
			if (v == null) continue;
			if (classOfValue.isAssignableFrom(v.getClass())) {
				rtn.add(classOfValue.cast(v));
			}
		}
		return rtn;
	}

	/**
	 * 指定されたIDで、String型の値を持つプロパティを再帰検索し、最初に見つかった空文字列ではない値を、
	 * 指定された列挙型の値へ変換して返します。<br/>
	 * 本メソッドは列挙値を元にした選択型の入力値を得るのに便利です。
	 * <p>
	 * 変換手順
	 * <ol>
	 * <li>空文字列でない入力値を取得する。
	 * <li>取得した入力値を decoder によりコード値へ変換する。
	 * <li>codeEnum の値のうち、求めたコード値と同じコードを持った値を見つける。
	 * <li>求めた列挙値と同じ名前を持つ、resultEnum の値を見つけて返す。
	 * </ol>
	 * 例えば、codeEnum にはHinemosCommonで定義されている共通 enum を指定し、
	 * resultEnum にはDTO内部定義の enum を指定します。
	 * 
	 * @param id 検索対象のID。
	 * @param decoder 文字列値をコード値へ変換する関数。
	 * @param codeEnum コード値に対応した列挙型。
	 * @param resultEnum 戻り値の列挙型。
	 * @return 見つかった値、見つからなかった場合はnull。
	 */
	public <T, E1 extends Enum<E1> & EnumDto<T>, E2 extends Enum<?>> E2 findEnum(String id,
			Function<String, T> decoder, Class<E1> codeEnum, Class<E2> resultEnum) {
		String s = findNonEmptyString(id);
		if (s == null) return null;
		T code = decoder.apply(s);
		for (E1 e : codeEnum.getEnumConstants()) {
			if (e.getCode().equals(code)) {
				for (E2 r : resultEnum.getEnumConstants()) {
					if (e.name().equals(r.name())) return r;
				}
			}
		}
		return null;
	}
}
