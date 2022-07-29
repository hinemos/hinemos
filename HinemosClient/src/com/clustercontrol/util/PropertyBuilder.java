/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.editor.PropertyDefine;
import com.clustercontrol.rest.dto.EnumDto;

/**
 * Propertyオブジェクトの生成コードを簡略化します。
 * 
 */
public class PropertyBuilder {

	private Property prop;

	private String id;
	private String name;
	private String editor;
	private Object value;
	private PropertyDefine define;
	private Boolean modifiable;
	private Boolean copiable;
	private Object options[][];
	private Long upperBound;
	private Long lowerBound;

	public PropertyBuilder(String id, String nameKey, String editor) {
		this.id = id;
		this.name = Messages.get(nameKey);
		this.editor = editor;
	}

	public PropertyBuilder(String id, String nameKey) {
		this(id, nameKey, PropertyDefineConstant.EDITOR_TEXT);
	}

	/**
	 * [ショートカットメソッド]
	 * テキスト入力({@link PropertyDefineConstant#EDITOR_TEXT})として
	 * {@link Property}をビルドします。
	 * 
	 * @param value 初期入力値。nullは空欄になります。
	 * @param max 入力可能最大長。
	 * @return {@link Property}オブジェクト。
	 */
	public Property buildText(String value, int max) {
		editor = PropertyDefineConstant.EDITOR_TEXT;
		setModifiable(true);
		setValue(value);
		setUpperBound(max);
		return build();
	}

	/**
	 * [ショートカットメソッド]
	 * テキスト入力({@link PropertyDefineConstant#EDITOR_TEXT})として
	 * 初期入力値以外は同一設定の複数の{@link Property}をビルドします。
	 * 
	 * @param values 初期入力値のリスト。nullは空欄になります。
	 * @param max 入力可能最大長。
	 * @return {@link Property}オブジェクトのリスト。
	 */
	public List<Property> buildMultipleTexts(List<String> values, int max) {
		List<Property> rtn = new ArrayList<>();
		if (values == null) return rtn;
		for (String value : values) {
			rtn.add(buildText(value, max));
		}
		return rtn;
	}

	/**
	 * [ショートカットメソッド]
	 * テキスト入力({@link PropertyDefineConstant#EDITOR_TEXT})として
	 * 初期入力値以外は同一設定の複数の{@link Property}をビルドします。
	 * 
	 * @param joinnedValues 複数の初期入力値を結合した文字列。この値がnullの場合は空欄が1つビルドされます。
	 * @param delimRegex 結合した文字列のデリミタを表す正規表現パターン。
	 * @param max 入力可能最大長。
	 * @return {@link Property}オブジェクトのリスト。
	 */
	public List<Property> buildMultipleTexts(String joinnedValues, String delimRegex, int max) {
		List<String> values = new ArrayList<>();
		if (joinnedValues == null) {
			joinnedValues = "";
		}
		values.addAll(Arrays.asList(joinnedValues.split(delimRegex)));
		return buildMultipleTexts(values, max);
	}

	/**
	 * [ショートカットメソッド]
	 * チェックボックス入力({@link PropertyDefineConstant#EDITOR_BOOL})として
	 * {@link Property}をビルドします。
	 * 
	 * @param value 初期入力値。nullは空欄になります。
	 * @return {@link Property}オブジェクト。
	 */
	public Property buildBool(Boolean value) {
		editor = PropertyDefineConstant.EDITOR_BOOL;
		setModifiable(true);
		setValue(value);
		return build();
	}

	/**
	 * [ショートカットメソッド]
	 * 数値入力({@link PropertyDefineConstant#EDITOR_NUM_LONG})として
	 * {@link Property}をビルドします。
	 * 
	 * @param value 初期入力値。nullは空欄になります。
	 * @param min 入力可能最小値。
	 * @param max 入力可能最大値。
	 * @return {@link Property}オブジェクト。
	 */
	public Property buildLong(Long value, long min, long max) {
		editor = PropertyDefineConstant.EDITOR_NUM_LONG;
		setModifiable(true);
		setValue(value);
		setLowerBound(min);
		setUpperBound(max);
		return build();
	}

	/**
	 * [ショートカットメソッド]
	 * 日時入力({@link PropertyDefineConstant#EDITOR_DATETIME})として
	 * {@link Property}をビルドします。
	 * 
	 * @param value 初期入力値。nullは空欄になります。Date, Long, String のいずれかで指定可能です。
	 * @return {@link Property}オブジェクト。
	 */
	public Property buildDateTime(Object value) {
		if (value != null) {
			if (value instanceof Date || value instanceof Long || value instanceof String) {
				// OK
			} else {
				throw new IllegalArgumentException("Illegal value type=" + value.getClass());
			}
		}
		editor = PropertyDefineConstant.EDITOR_DATETIME;
		setModifiable(true);
		setValue(value);
		return build();
	}

	/**
	 * [ショートカットメソッド]
	 * リスト選択({@link PropertyDefineConstant#EDITOR_SELECT})として
	 * {@link Property}をビルドします。
	 * 
	 * @param value 初期入力値。nullは空欄になります。
	 * @param options 選択肢リスト。表示名と選択値は等しくなります。
	 * @return {@link Property}オブジェクト。
	 */
	public Property buildSelect(String value, Iterable<String> options) {
		editor = PropertyDefineConstant.EDITOR_SELECT;
		setModifiable(true);
		setOptions(options);
		setValue(value);
		return build();
	}

	/**
	 * [ショートカットメソッド]
	 * リスト選択({@link PropertyDefineConstant#EDITOR_SELECT})として
	 * {@link Property}をビルドします。
	 * 
	 * @param value
	 * 		初期入力値。nullは空欄になります。
	 * 		options とは {@link Enum#name()} の戻り値で一致するかどうかを判断するため、同じ型である必要はありません。
	 * @param options
	 * 		選択肢リスト。表示名と選択値は等しくなります。
	 * @param translator
	 * 		options の各値について {@link EnumDto#getCode()} を元に表示名を返す関数です。
	 *  
	 * @return {@link Property}オブジェクト。
	 */
	public <T, E extends Enum<E> & EnumDto<T>> Property buildSelect(Enum<?> value, Iterable<E> options,
			Function<T, String> translator) {
		String convValue = "";
		List<String> convOptions = new ArrayList<>();
		convOptions.add("");
		for (E opt : options) {
			String convOpt = translator.apply(opt.getCode());
			convOptions.add(convOpt);
			if (value != null && value.name().equals(opt.name())) {
				convValue = convOpt;
			}
		}
		return buildSelect(convValue, convOptions);
	}

	/**
	 * 初期入力値を設定します。
	 */
	public PropertyBuilder setValue(Object value) {
		this.value = value;
		return this;
	}

	/**
	 * defineを設定します。
	 */
	public PropertyBuilder setDefine(PropertyDefine define) {
		this.define = define;
		return this;
	}

	/**
	 * 入力可否を設定します。
	 */
	public PropertyBuilder setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
		return this;
	}

	/**
	 * コピー可否を設定します。
	 */
	public PropertyBuilder setCopiable(boolean copiable) {
		this.copiable = copiable;
		return this;
	}

	/**
	 * 選択リストを設定します。
	 */
	public PropertyBuilder setOptions(Object[][] options) {
		this.options = options;
		return this;
	}

	/**
	 * 選択リストを設定します。
	 */
	public PropertyBuilder setOptions(Object[] options) {
		this.options = new Object[][] { options, options };
		return this;
	}

	/**
	 * 選択リストを設定します。
	 */
	public PropertyBuilder setOptions(String... options) {
		this.options = new Object[][] { options, options };
		return this;
	}

	/**
	 * 選択リストを設定します。
	 */
	public PropertyBuilder setOptions(Iterable<String> options) {
		List<String> list = new ArrayList<>();
		options.forEach(list::add);
		String[] array = list.toArray(new String[list.size()]);
		this.options = new Object[][] { array, array };
		return this;
	}

	/**
	 * 入力上限値を設定します。
	 */
	public PropertyBuilder setUpperBound(long upperBound) {
		this.upperBound = upperBound;
		return this;
	}

	/**
	 * 入力下限値を設定します。
	 */
	public PropertyBuilder setLowerBound(long lowerBound) {
		this.lowerBound = lowerBound;
		return this;
	}

	/**
	 * これまで設定された内容を基に{@link Property}を生成して返します。
	 */
	public Property build() {
		prop = new Property(id, name, editor);

		if (define != null) {
			prop.setDefine(define);
		}
		if (modifiable != null) {
			prop.setModify(modifiable.booleanValue() ? PropertyDefineConstant.MODIFY_OK : PropertyDefineConstant.MODIFY_NG);
		}
		if (copiable != null) {
			prop.setCopy(copiable.booleanValue() ? PropertyDefineConstant.COPY_OK : PropertyDefineConstant.COPY_NG);
		}
		if (options != null) {
			prop.setSelectValues(options);
		}
		if (upperBound != null) {
			if (PropertyDefineConstant.EDITOR_TEXT.equals(editor) || PropertyDefineConstant.EDITOR_TEXTAREA.equals(editor)) {
				prop.setStringUpperValue(upperBound.intValue());
			} else {
				prop.setUpperBound(upperBound.longValue());
			}
		}
		if (lowerBound != null) {
			prop.setLowerBound(lowerBound.longValue());
		}

		// 値を変換して設定する
		Object v = value;
		if (value == null) {
			v = "";
		} else {
			// 日時で Long または String が指定されているなら Date へ変換
			if (PropertyDefineConstant.EDITOR_DATETIME.equals(editor)) {
				if (value instanceof Long) {
					v = new Date(((Long) value).longValue());
				} else if (value instanceof String) {
					if (!value.equals("")) {
						try {
							v = TimezoneUtil.getSimpleDateFormat().parse((String) value);
						} catch (ParseException e) {
							// ignore
						}
					}
				}
			}
		}
		prop.setValue(v);

		return prop;
	}
}
