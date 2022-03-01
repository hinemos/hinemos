/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;

import com.clustercontrol.sdml.ISdmlClientOption;
import com.clustercontrol.sdml.SdmlClientOptionManager;
import com.clustercontrol.sdml.bean.SdmlXmlFileName;

/**
 * 設定インポートエクスポート向けのSDML利用クラス
 * 
 */
public class SdmlUtilityInterface {
	private static Log logger = LogFactory.getLog(SdmlUtilityInterface.class);


	/**
	 * 設定インポートエクスポートのXMLファイル名を保持したBeanのリストを取得する
	 * 
	 * @return
	 */
	public static List<SdmlXmlFileName> getXmlFileList() {
		return new ArrayList<>(SdmlClientOptionManager.getInstance().getXmlFileNameMap().values());
	}

	/* 表示順に並び替えたい場合に使用する */
	private static List<SdmlXmlFileName> getSortedXmlFileList() {
		List<SdmlXmlFileName> list = getXmlFileList();

		Collections.sort(list, new Comparator<SdmlXmlFileName>() {
			@Override
			public int compare(SdmlXmlFileName obj1, SdmlXmlFileName obj2) {
				return obj1.getOrder() - obj2.getOrder();
			}
		});

		return list;
	}

	/**
	 * Preferenceに表示するGroupを生成する
	 * 
	 * @param parentGroup
	 * @return
	 */
	public static Group getSdmlGruopForPreference(Group parentGroup) {
		ISdmlClientOption common = SdmlClientOptionManager.getInstance().getCommonOption();
		if (common == null) {
			logger.debug("getSdmlGruopForPreference() : SDML Common Option is null.");
			return null; // 共通オプションがなければ何も返さない
		}
		Group group = new Group(parentGroup, SWT.SHADOW_NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		group.setLayoutData(gridData);
		group.setText(common.getDefaultXML().get(0).getFuncName());
		return group;
	}

	/**
	 * Preferenceに表示するFieldEditorを生成する
	 * 
	 * @param parentGroup
	 * @return
	 */
	public static List<StringFieldEditor> getFieldEditorForPreference(Group parentGroup) {
		List<StringFieldEditor> list = new ArrayList<>();
		for (SdmlXmlFileName bean : getSortedXmlFileList()) {
			StringFieldEditor editor = new StringFieldEditor(bean.getXmlDefaultName(), bean.getFuncName(), parentGroup);
			editor.setTextLimit(256);
			list.add(editor);
		}
		return list;
	}

}
