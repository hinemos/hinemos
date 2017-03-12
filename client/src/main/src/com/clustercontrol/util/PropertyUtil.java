/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.util;

import java.util.ArrayList;
import java.util.HashMap;

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
		 * TODO ここの修正は怪しい。後で調査すること！！！
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
