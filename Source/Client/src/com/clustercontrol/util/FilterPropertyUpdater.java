package com.clustercontrol.util;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.rap.rwt.RWT;

import com.clustercontrol.bean.Property;

/**
 * フィルタプロパティの登録を受け付け、それらを一括更新します。
 * <p>
 * 現在のところは、マネージャ選択肢のみが更新対象です。
 */
public class FilterPropertyUpdater {
	private static final String UISESSION_ATTR_KEY = FilterPropertyUpdater.class.getName();

	private FilterPropertyUpdater() {
	}

	private static class SingletonHolder {
		public static final FilterPropertyUpdater instance = new FilterPropertyUpdater();
	}

	public static FilterPropertyUpdater getInstance() {
		return SingletonHolder.instance;
	}

	private static class MapValue {
		public final Property filterCondition;
		public final String managerConstantId;

		public MapValue(Property filterCondition, String managerConstantId) {
			this.filterCondition = filterCondition;
			this.managerConstantId = managerConstantId;
		}
	}

	@SuppressWarnings("unchecked")
	private Map<Class<?>, MapValue> getPropertyMap() {
		synchronized (this) {
			Object r = RWT.getUISession().getAttribute(UISESSION_ATTR_KEY);
			if (r != null) {
				return (Map<Class<?>, MapValue>) r;
			} else {
				Map<Class<?>, MapValue> map = new ConcurrentHashMap<>();
				RWT.getUISession().setAttribute(UISESSION_ATTR_KEY, map);
				return map;
			}
		}
	}

	/**
	 * 更新が必要なフィルタプロパティを追加登録します。
	 * 
	 * @param owner
	 * *登録するフィルタプロパティを所有している(インスタンス管理義務のある)クラス。
	 * @param filterProperty
	 *            登録するフィルタプロパティ。null の場合は登録済みのフィルタプロパティを除去します。
	 * @param managerConstantId
	 *            フィルタプロパティ内の、マネージャ選択項目のID。
	 */
	public void addFilterProperty(Class<?> owner, Property filterProperty, String managerConstantId) {
		if (filterProperty == null) {
			getPropertyMap().remove(owner);
		} else {
			getPropertyMap().put(owner, new MapValue(filterProperty, managerConstantId));
		}
	}

	/**
	 * 登録されているフィルタプロパティを一括更新します。
	 */
	public void updateFilterProperties() {
		String[] currentActiveManagers = RestConnectManager.createManagerSelectValues();
		
		for (MapValue item : getPropertyMap().values()) {
			for (Property managerSelector : PropertyUtil.getProperty(item.filterCondition, item.managerConstantId)) {
				Object[][] selectValues = managerSelector.getSelectValues();

				if (selectValues.length > 0 && !Arrays.equals(selectValues[0], currentActiveManagers)) {
					PropertyUtil.deletePropertyDefine(managerSelector);
					managerSelector.setSelectValues(new Object[][] { currentActiveManagers, currentActiveManagers });
					
					if (!(Arrays.asList(currentActiveManagers).contains(managerSelector.getValue()))) {
						managerSelector.setValue("");
					}
				}
			}
		}
	}
}
