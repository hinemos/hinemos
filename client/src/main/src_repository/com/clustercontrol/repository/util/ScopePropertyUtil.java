package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.ScopeConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ws.repository.ScopeInfo;

public class ScopePropertyUtil {

	private static Log m_log = LogFactory.getLog(ScopePropertyUtil.class);

	/** ----- 初期値キャッシュ ----- */
	private static Map<Locale, ConcurrentHashMap<Integer, Property>> cachedInitProperty = null;

	static {
		cachedInitProperty = new ConcurrentHashMap<Locale, ConcurrentHashMap<Integer, Property>>();
	}

	public static ScopeInfo property2scope(Property property) {
		ScopeInfo scopeInfo = new ScopeInfo();
		scopeInfo.setFacilityType(FacilityConstant.TYPE_SCOPE);
		scopeInfo.setDisplaySortOrder(100);
		scopeInfo.setValid(true);
		scopeInfo.setCreateDatetime(System.currentTimeMillis());
		scopeInfo.setModifyDatetime(System.currentTimeMillis());
		scopeInfo.setBuiltInFlg(false);

		ArrayList<?> object1 = null;

		object1 = (ArrayList<?>) PropertyUtil.getPropertyValue(property, ScopeConstant.FACILITY_ID);
		scopeInfo.setFacilityId((String)object1.get(0));

		object1 = (ArrayList<?>) PropertyUtil.getPropertyValue(property, ScopeConstant.FACILITY_NAME);
		scopeInfo.setFacilityName((String)object1.get(0));

		object1 = (ArrayList<?>) PropertyUtil.getPropertyValue(property, ScopeConstant.DESCRIPTION);
		scopeInfo.setDescription((String)object1.get(0));

		object1 = (ArrayList<?>) PropertyUtil.getPropertyValue(property,ScopeConstant.ICONIMAGE);
		scopeInfo.setIconImage((String)object1.get(0));

		return scopeInfo;
	}

	public static Property scope2property (ScopeInfo scopeInfo, int mode, Locale locale) {
		Property property = null;
		ArrayList<Property> propertyList = null;

		property = getProperty(mode, locale);

		propertyList = PropertyUtil.getProperty(property, ScopeConstant.FACILITY_ID);
		((Property)propertyList.get(0)).setValue(scopeInfo.getFacilityId());

		propertyList = PropertyUtil.getProperty(property, ScopeConstant.FACILITY_NAME);
		((Property)propertyList.get(0)).setValue(scopeInfo.getFacilityName());

		propertyList = PropertyUtil.getProperty(property, ScopeConstant.DESCRIPTION);
		((Property)propertyList.get(0)).setValue(scopeInfo.getDescription());

		propertyList = PropertyUtil.getProperty(property, ScopeConstant.ICONIMAGE);
		((Property)propertyList.get(0)).setValue(scopeInfo.getIconImage());

		return property;
	}


	/**
	 * スコープ用プロパティを返します。
	 *
	 * @param mode
	 * @return スコープ用プロパティ
	 */
	public static Property getProperty(int mode, Locale locale) {

		// 初期値のキャッシュが存在すれば、それを返す
		if (cachedInitProperty.containsKey(locale)) {
			if (cachedInitProperty.get(locale).containsKey(mode)) {
				m_log.debug("using a initial property of scope to cache. (locale = " + locale + ", mode = " + mode + ")");
				return PropertyUtil.copy(cachedInitProperty.get(locale).get(mode));
			}
		} else {
			m_log.info("adding a initial property of scope to cache. (locale = " + locale + ")");
			cachedInitProperty.put(locale, new ConcurrentHashMap<Integer, Property>());
		}

		//ファシリティID
		Property facilityId =
				new Property(ScopeConstant.FACILITY_ID, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//ファシリティ名
		Property facilityName =
				new Property(ScopeConstant.FACILITY_NAME, Messages.getString("facility.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//説明
		Property description =
				new Property(ScopeConstant.DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//アイコン
		Property iconIname =
				new Property(ScopeConstant.ICONIMAGE, Messages.getString("icon.image", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//値を初期化
		facilityId.setValue("");
		facilityName.setValue("");
		description.setValue("");
		iconIname.setValue("");

		//モードにより、変更を可に設定
		if(mode == PropertyDefineConstant.MODE_ADD){
			facilityId.setModify(PropertyDefineConstant.MODIFY_OK);
			facilityName.setModify(PropertyDefineConstant.MODIFY_OK);
			description.setModify(PropertyDefineConstant.MODIFY_OK);
			iconIname.setModify(PropertyDefineConstant.MODIFY_OK);
		}else if(mode == PropertyDefineConstant.MODE_MODIFY){
			facilityName.setModify(PropertyDefineConstant.MODIFY_OK);
			description.setModify(PropertyDefineConstant.MODIFY_OK);
			iconIname.setModify(PropertyDefineConstant.MODIFY_OK);
		}

		Property property = new Property(null, null, "");
		property.removeChildren();
		property.addChildren(facilityId);
		property.addChildren(facilityName);
		property.addChildren(description);
		property.addChildren(iconIname);

		// 初期値をキャッシュに登録（次回から高速化に初期値を取得）
		m_log.info("adding a initial property of scope to cache. (locale = " + locale + ", mode = " + mode + ")");
		cachedInitProperty.get(locale).put(mode, PropertyUtil.copy(property));

		return property;
	}

	public static FacilityTreeItem getManager(FacilityTreeItem item) {
		if (item == null) {
			return null;
		} else if (item.getData().getFacilityType() == FacilityConstant.TYPE_MANAGER) {
			return item;
		}

		return getManager(item.getParent());
	}
}
