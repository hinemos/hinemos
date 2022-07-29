/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.ui.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.rap.rwt.SingletonUtil;

/**
 * フィルタ設定の処理方法を保持するシングルトンクラス
 * 
 * 
 */

public class FilterSettingProcessMode {
	private static FilterSettingProcessMode instance = SingletonUtil.getSessionInstance(FilterSettingProcessMode.class);
	
	/* キーはアクションクラス名 */
	private Map<String, ArrayList<Boolean>> filterTypeListMap = new ConcurrentHashMap<String, ArrayList<Boolean>>();
	private Map<String, Boolean> userFilterRangeMap = new ConcurrentHashMap<String, Boolean>();
	
	/* "同じ選択を次の設定にも適用" 向け項目 */
	private boolean isSameNextChoice = false;
	private ArrayList<Boolean> lastFilterTypeList = null;
	private Boolean lastUserFilterRange = null;

	private FilterSettingProcessMode() {
	}
	public static synchronized FilterSettingProcessMode getInstance() {
		return instance;
	}
	
	public static ArrayList<Boolean>  getFilterTypeList(String className) {
		return getInstance().filterTypeListMap.get(className);
	}
	public static void setFilterTypeList(String className,ArrayList<Boolean>  filterTypeList) {
		getInstance().filterTypeListMap.put(className, filterTypeList);
	}
	public static Boolean getUserFilterRange(String className) {
		return getInstance().userFilterRangeMap.get(className);
	}
	public static void setUserFilterRange(String className,Boolean userFilterRange) {
		getInstance().userFilterRangeMap.put(className,userFilterRange);
	}

	public static void init() {
		getInstance().filterTypeListMap  = new ConcurrentHashMap<String, ArrayList<Boolean>>();
		getInstance().userFilterRangeMap  = new ConcurrentHashMap<String, Boolean>();
		getInstance().isSameNextChoice = false;
		getInstance().lastFilterTypeList = null;
		getInstance().lastUserFilterRange = null;
	}

	public static boolean isSameNextChoice() {
		return getInstance().isSameNextChoice;
	}
	public static void setSameNextChoice(boolean isSameNextChoice) {
		getInstance().isSameNextChoice = isSameNextChoice;
	}

	public static ArrayList<Boolean> getLastFilterTypeList() {
		return getInstance().lastFilterTypeList;
	}
	public static void setLastFilterTypeList(ArrayList<Boolean> lastFilterTypeList) {
		getInstance().lastFilterTypeList = lastFilterTypeList;
	}

	public static Boolean getLastUserFilterRange() {
		return getInstance().lastUserFilterRange;
	}
	public static void setLastUserFilterRange(Boolean lastUserFilterRange) {
		getInstance().lastUserFilterRange = lastUserFilterRange;
	}
	public static void setLastSelect(String className) {
		FilterSettingProcessMode.setFilterTypeList(className, getInstance().lastFilterTypeList);
		FilterSettingProcessMode.setUserFilterRange(className, getInstance().lastUserFilterRange);
	}
	
}
