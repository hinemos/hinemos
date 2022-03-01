/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.bean;

import java.io.File;
import java.util.Set;

import com.clustercontrol.utility.settings.ui.action.ReadXMLAction;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.util.MultiManagerPathUtil;

/**
 * {@ReadXMLAction}ではEnumで定義されており動的な変更ができないので個別クラスで定義する
 * 
 * @see ReadXMLAction
 */
public class SdmlXmlFileName {
	private String xmlDefaultName;
	private String funcName;
	private boolean isRequired;
	private String[] optionKey;
	// Preferenceの表示順を指定する
	private int order;

	/**
	 * コンストラクタ
	 * 
	 * @param xmlDefaultName
	 * @param funcName
	 * @param order
	 */
	public SdmlXmlFileName(String xmlDefaultName, String funcName, int order) {
		this.xmlDefaultName = xmlDefaultName;
		this.funcName = funcName;
		this.isRequired = true; // デフォルト
		this.optionKey = null; // デフォルト
		this.order = order;
	}

	/**
	 * @param xmlDefaultName
	 * @param funcName
	 * @param isRequired
	 * @param optionKey
	 * @param order
	 */
	public SdmlXmlFileName(String xmlDefaultName, String funcName, boolean isRequired, String[] optionKey, int order) {
		this.xmlDefaultName = xmlDefaultName;
		this.funcName = funcName;
		this.isRequired = isRequired;
		this.optionKey = optionKey;
		this.order = order;
	}

	public String getXmlDefaultName() {
		return xmlDefaultName;
	}

	public String getFuncName() {
		return funcName;
	}

	public int getOrder() {
		return order;
	}

	public boolean isRequired() {
		return isRequired;
	}

	// --- 以下{@ReadXMLAction}のXMLFileNameと同様の処理とする
	public String getFilePath() {
		String fileName = MultiManagerPathUtil.getXMLFileName(xmlDefaultName);
		return MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML) + File.separator + fileName;
	}

	public String getFileName() {
		return MultiManagerPathUtil.getXMLFileName(xmlDefaultName);
	}

	public boolean checkOption(Set<String> options) {
		if (optionKey == null) {
			// 現時点ではSDMLは基本trueになる
			return true;
		}
		if (optionKey.length != 0) {
			boolean ret = false;
			for (String key : optionKey) {
				if (options.contains(key)) {
					ret = true;
				}
			}
			return ret;
		} else {
			return true;
		}
	}
}
