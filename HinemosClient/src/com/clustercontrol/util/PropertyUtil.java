/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;

/**
 * プロパティユーティリティクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class PropertyUtil {
	/**
	 * プロパティ値の取得
	 * 
	 * @param property
	 * @param id
	 * @return プロパティの配列
	 */
	public static ArrayList<?> getPropertyValue(Property property, String id) {
		ArrayList<Object> list = new ArrayList<Object>();

		getPropertyValue(property, id, list);

		return list;
	}

	/**
	 * プロパティ値の取得（再帰呼び出し）
	 * 
	 * @param property
	 * @param id
	 * @param list
	 */
	public static void getPropertyValue(Property property, String id,
			ArrayList<Object> list) {

		//IDが一致するプロパティをリストに追加
		if (id != null && property.getID() != null) {
			if (id.compareTo(property.getID()) == 0) {
				list.add(property.getValue());
			}
		}

		//子プロパティを検索する
		if (property.getEditor() != null
				&& property.getEditor().compareTo(
						PropertyDefineConstant.EDITOR_SELECT) == 0) {
			Object[][] value = property.getSelectValues();
			for (int j = 0; j < value[PropertyDefineConstant.SELECT_VALUE].length; j++) {
				if (value[PropertyDefineConstant.SELECT_VALUE][j] instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) value[PropertyDefineConstant.SELECT_VALUE][j];

					//子Propertyを取得
					@SuppressWarnings("unchecked")
					ArrayList<Property> propertyList = (ArrayList<Property>) map.get(PropertyDefineConstant.MAP_PROPERTY);
					if (propertyList != null) {
						for (int k = 0; k < propertyList.size(); k++) {
							Property children2 = propertyList.get(k);
							getPropertyValue(children2, id, list);
						}
					}
				}
			}
		} else {
			//子Propertyを取得
			Object[] childrens = property.getChildren();

			for (int i = 0; i < childrens.length; i++) {
				Property children1 = (Property) childrens[i];
				getPropertyValue(children1, id, list);
			}
		}
	}

	/**
	 * 指定されたIDで、String型の値を持つプロパティを再帰検索し、最初に見つかった値を返します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 見つかった場合はString値、見つからなかった場合はnull。
	 */
	public static String findStringValue(Property prop, String id) {
		for (Object value : getPropertyValue(prop, id)) {
			if (value instanceof String) {
				return (String) value;
			}
		}
		return null;
	}

	/**
	 * 指定されたIDで、String型の値を持つプロパティを再帰検索し、最初に見つかった空文字列でない値を返します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 見つかった場合はString値、見つからなかった場合はnull。
	 */
	public static String findNonEmptyStringValue(Property prop, String id) {
		for (Object value : getPropertyValue(prop, id)) {
			if (value instanceof String) {
				String s = (String) value;
				if (s.length() > 0) {
					return s;
				}
			}
		}
		return null;
	}

	/**
	 * 指定されたIDで、String型の値を持つプロパティを再帰検索し、空文字列でない値をリスト化して返します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 空文字列でない値のリスト。
	 */
	public static List<String> findNonEmptyStringValues(Property prop, String id) {
		List<String> rtn = new ArrayList<>();
		for (Object value : getPropertyValue(prop, id)) {
			if (value instanceof String) {
				String s = (String) value;
				if (s.length() > 0) {
					rtn.add(s);
				}
			}
		}
		return rtn;
	}

	/**
	 * 指定されたIDで、Integer型の値を持つプロパティを再帰検索し、最初に見つかった値を返します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 見つかった場合はInteger値、見つからなかった場合はnull。
	 */
	public static Integer findIntegerValue(Property prop, String id) {
		for (Object value : getPropertyValue(prop, id)) {
			if (value instanceof Integer) {
				return (Integer) value;
			}
		}
		return null;
	}

	/**
	 * 指定されたIDで、Long型の値を持つプロパティを再帰検索し、最初に見つかった値を返します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 見つかった場合はLong値、見つからなかった場合はnull。
	 */
	public static Long findLongValue(Property prop, String id) {
		for (Object value : getPropertyValue(prop, id)) {
			if (value instanceof Long) {
				return (Long) value;
			}
		}
		return null;
	}

	/**
	 * 指定されたIDで、Boolean型の値を持つプロパティを再帰検索し、最初に見つかった値を返します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 見つかった場合はBoolean値、見つからなかった場合はnull。
	 */
	public static Boolean findBooleanValue(Property prop, String id) {
		for (Object value : getPropertyValue(prop, id)) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			}
		}
		//findbugs対応 Booleanの場合、明示的なnull返却は不可を回避
		Boolean ret = null;
		return ret;
	}

	/**
	 * 指定されたIDで、Date型の値を持つプロパティを再帰検索し、最初に見つかった値をlong変換して返します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 見つかった場合はlong値、見つからなかった場合はnull。
	 */
	public static Long findTimeValue(Property prop, String id) {
		for (Object value : getPropertyValue(prop, id)) {
			if (value instanceof Date) {
				Date d = (Date) value;
				return d.getTime();
			}
		}
		return null;
	}

	/**
	 * 指定されたIDで、Date型の値を持つプロパティを再帰検索し、最初に見つかった値をString変換して返します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 見つかった場合はString値、見つからなかった場合はnull。
	 */
	public static String findTimeStringValue(Property prop, String id, SimpleDateFormat sdf) {
		for (Object value : getPropertyValue(prop, id)) {
			if (value instanceof Date) {
				Date d = (Date) value;
				return sdf.format(d);
			}
		}
		return null;
	}

	/**
	 * 指定されたIDで、Date型の値を持つプロパティを再帰検索し、最初に見つかった値をlong変換し、
	 * さらに下3桁(ミリ秒の部分)を"999"に設定した値を返します。
	 * <p>
	 * 日時範囲の終端値を取得したい場合に、本メソッドを使用します。
	 * 
	 * @param prop 検索対象のプロパティ。
	 * @param id 検索対象のID。
	 * @return 見つかった場合はlong値、見つからなかった場合はnull。
	 */
	public static Long findEndTimeValue(Property prop, String id) {
		Long v = findTimeValue(prop, id);
		if (v == null) return null;
		return v / 1000 * 1000 + 999;
	}

	/**
	 * プロパティの取得
	 * 
	 * @param property
	 * @param id
	 * @return プロパティの配列
	 */
	public static ArrayList<Property> getProperty(Property property, String id) {
		ArrayList<Property> list = new ArrayList<Property>();

		getProperty(property, id, list);

		return list;
	}

	/**
	 * プロパティの取得（再帰呼び出し）
	 * 
	 * @param property
	 * @param id
	 * @param list
	 */
	public static void getProperty(Property property, String id, ArrayList<Property> list) {
		//IDが一致するプロパティをリストに追加
		if (id != null && property.getID() != null) {
			if (id.compareTo(property.getID()) == 0) {
				list.add(property);
			}
		}

		//子プロパティを検索する
		if (property.getEditor() != null 
				&& property.getEditor().compareTo(
						PropertyDefineConstant.EDITOR_SELECT) == 0) {
			Object[][] value = property.getSelectValues();
			for (int j = 0; j < value[PropertyDefineConstant.SELECT_VALUE].length; j++) {
				if (value[PropertyDefineConstant.SELECT_VALUE][j] instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) value[PropertyDefineConstant.SELECT_VALUE][j];

					//子Propertyを取得
					@SuppressWarnings("unchecked")
					ArrayList<Property> propertyList = (ArrayList<Property>) map.get(PropertyDefineConstant.MAP_PROPERTY);
					if (propertyList != null) {
						for (int k = 0; k < propertyList.size(); k++) {
							Property children2 = propertyList.get(k);
							getProperty(children2, id, list);
						}
					}
				}
			}
		} else {
			//子Propertyを取得
			Object[] childrens = property.getChildren();

			for (int i = 0; i < childrens.length; i++) {
				Property children1 = (Property) childrens[i];
				getProperty(children1, id, list);
			}
		}
	}

	/**
	 * プロパティの取得（再帰呼び出し）
	 * 
	 * @param property
	 */
	public static void deletePropertyDefine(Property property) {

		//子プロパティを検索する
		if (property.getEditor() != null 
				&& property.getEditor().compareTo(
						PropertyDefineConstant.EDITOR_SELECT) == 0) {
			Object[][] value = property.getSelectValues();
			for (int j = 0; j < value[PropertyDefineConstant.SELECT_VALUE].length; j++) {
				if (value[PropertyDefineConstant.SELECT_VALUE][j] instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) value[PropertyDefineConstant.SELECT_VALUE][j];

					//子Propertyを取得
					@SuppressWarnings("unchecked")
					ArrayList<Property> propertyList = (ArrayList<Property>) map.get(PropertyDefineConstant.MAP_PROPERTY);
					if (propertyList != null) {
						for (int k = 0; k < propertyList.size(); k++) {
							Property children2 = propertyList.get(k);
							deletePropertyDefine(children2);
						}
					}
				}
			}
		} else {
			//子Propertyを取得
			Object[] childrens = property.getChildren();

			for (int i = 0; i < childrens.length; i++) {
				Property children1 = (Property) childrens[i];
				deletePropertyDefine(children1);
			}
		}

		//PropertyDefineを削除する
		if (property.getDefine() != null) {
			property.setDefine(null);
		}
	}

	/**
	 * プロパティ位置の取得
	 * 
	 * @param parent
	 * @param property
	 * @return プロパティ位置
	 */
	public static int getPropertyIndex(Property parent, Property property) {
		Object[] childrens = parent.getChildren();
		int index = -1;

		for (int i = 0; i < childrens.length; i++) {
			Property children = (Property) childrens[i];
			if (children.hashCode() == property.hashCode()) {
				index = i;
				break;
			}
		}

		return index;
	}

	/**
	 * Propertyクラスをコピーする
	 * 
	 * @param original
	 * @return コピーしたPropertyクラス
	 */
	public static Property copy(Property original) {
		Property clone = new Property(original.getID(), original.getName(),
				original.getEditor());

		clone.setParent(original.getParent());
		clone.setModify(original.getModify());
		clone.setCopy(original.getCopy());
		clone.setUpperBound(original.getUpperBound());
		clone.setLowerBound(original.getLowerBound());
		clone.setStringUpperValue(original.getStringUpperValue());

		Object[][] selectValues = original.getSelectValues();
		Object[] childrens = original.getChildren();

		if (selectValues != null) {
			Object[][] cloneSelectValues = {
					new Object[selectValues[PropertyDefineConstant.SELECT_DISP_TEXT].length],
					new Object[selectValues[PropertyDefineConstant.SELECT_VALUE].length] };
			for (int i = 0; i < selectValues[PropertyDefineConstant.SELECT_DISP_TEXT].length; i++) {
				cloneSelectValues[PropertyDefineConstant.SELECT_DISP_TEXT][i] = selectValues[PropertyDefineConstant.SELECT_DISP_TEXT][i];
			}
			for (int i = 0; i < selectValues[PropertyDefineConstant.SELECT_VALUE].length; i++) {
				if (selectValues[PropertyDefineConstant.SELECT_VALUE][i] instanceof HashMap) {
					HashMap<String, Object> cloneMap = new HashMap<String, Object>();
					ArrayList<Property> clonPropertyList = new ArrayList<Property>();

					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) selectValues[PropertyDefineConstant.SELECT_VALUE][i];
					@SuppressWarnings("unchecked")
					ArrayList<Property> propertyList = (ArrayList<Property>) map.get(PropertyDefineConstant.MAP_PROPERTY);
					if (propertyList != null) {
						for (int j = 0; j < propertyList.size(); j++) {
							Property children = propertyList.get(j);
							Property clonechildren = copy(children);
							clonPropertyList.add(clonechildren);
						}
					}
					cloneMap.put(PropertyDefineConstant.MAP_VALUE, map
							.get(PropertyDefineConstant.MAP_VALUE));
					cloneMap.put(PropertyDefineConstant.MAP_PROPERTY,
							clonPropertyList);

					cloneSelectValues[PropertyDefineConstant.SELECT_VALUE][i] = cloneMap;
				} else {
					cloneSelectValues[PropertyDefineConstant.SELECT_VALUE][i] = selectValues[PropertyDefineConstant.SELECT_VALUE][i];
				}
			}
			clone.setSelectValues(cloneSelectValues);
		} else {
			for (int i = 0; i < childrens.length; i++) {
				Property children = (Property) childrens[i];
				Property cloneChildren = copy(children);
				clone.addChildren(cloneChildren);
			}
		}

		/*
		 * XXX ここの修正は怪しい。後で調査すること！！！
		 */
		clone.setDefine(original.getDefine());
		clone.setValue(original.getValue());

		return clone;
	}

	/**
	 * プロパティの変更を設定する（再帰呼び出し）
	 * 
	 * @param property
	 * @param modify
	 */
	public static void setPropertyModify(Property property, int modify) {

		//子プロパティを検索する
		if (property.getEditor() != null 
				&& property.getEditor().compareTo(
						PropertyDefineConstant.EDITOR_SELECT) == 0) {
			Object[][] value = property.getSelectValues();
			for (int j = 0; j < value[PropertyDefineConstant.SELECT_VALUE].length; j++) {
				if (value[PropertyDefineConstant.SELECT_VALUE][j] instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) value[PropertyDefineConstant.SELECT_VALUE][j];

					//子Propertyを取得
					@SuppressWarnings("unchecked")
					ArrayList<Property> propertyList = (ArrayList<Property>) map.get(PropertyDefineConstant.MAP_PROPERTY);
					if (propertyList != null) {
						for (int k = 0; k < propertyList.size(); k++) {
							Property children2 = (Property) propertyList.get(k);
							setPropertyModify(children2, modify);
						}
					}
				}
			}
		}

		//子Propertyを取得
		Object[] childrens = property.getChildren();

		for (int i = 0; i < childrens.length; i++) {
			Property children1 = (Property) childrens[i];
			setPropertyModify(children1, modify);
		}

		//Property変更をNGにする
		property.setModify(modify);
	}

	/**
	 * プロパティ値設定処理（再帰呼び出し）
	 * 
	 * @param property
	 * @param editor
	 * @param value
	 */
	public static void setPropertyValue(Property property, String editor,
			Object value) {

		//エディタが一致するプロパティに値を設定
		if (property.getEditor().equals(editor)) {
			property.setValue(value);
		}

		//子プロパティを検索する
		if (property.getEditor() != null 
				&& property.getEditor().compareTo(
						PropertyDefineConstant.EDITOR_SELECT) == 0) {
			Object[][] selectValue = property.getSelectValues();
			for (int j = 0; j < selectValue[PropertyDefineConstant.SELECT_VALUE].length; j++) {
				if (selectValue[PropertyDefineConstant.SELECT_VALUE][j] instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) selectValue[PropertyDefineConstant.SELECT_VALUE][j];

					//子Propertyを取得
					@SuppressWarnings("unchecked")
					ArrayList<Property> propertyList = (ArrayList<Property>) map.get(PropertyDefineConstant.MAP_PROPERTY);
					if (propertyList != null) {
						for (int k = 0; k < propertyList.size(); k++) {
							Property children2 = (Property) propertyList.get(k);
							setPropertyValue(children2, editor, value);
						}
					}
				}
			}
		} else {
			//子Propertyを取得
			Object[] childrens = property.getChildren();

			for (int i = 0; i < childrens.length; i++) {
				Property children1 = (Property) childrens[i];
				setPropertyValue(children1, editor, value);
			}
		}
	}
}
