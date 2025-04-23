/*
 * Copyright (c) 2024 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.commons.util.AlterModeArgsUtil;

/**
 * メンテナンスのクラウドサービスモード用Utilクラス
 */
public class MaintenanceCloudServiceModeUtil {

	private static Log m_log = LogFactory.getLog(MaintenanceCloudServiceModeUtil.class);

	// 設定プロパティファイル名
	private static final String CONSTRAINT_FILE_NAME = "cloudservicemode_hinemosproperties_constraint.properties";

	// シングルトンインスタンス
	private static MaintenanceCloudServiceModeUtil instance = new MaintenanceCloudServiceModeUtil();

	// 設定不可リスト（完全一致文字列のリスト）
	private List<String> cantRegistHinemosPropertyList;

	/**
	 * コンストラクタ（利用者で呼出し不可）
	 */
	private MaintenanceCloudServiceModeUtil(){
		Path path = HinemosManagerMain._etcDir.resolve(CONSTRAINT_FILE_NAME);

		List<String> propertyList;
		if(Files.exists(path)){
			try {
				propertyList = Files.readAllLines(path);
			} catch (IOException e) {
				m_log.info("loadProperties: Abort, " + e.getClass().getName() + ", " + e.getMessage());
				return;
			}
		} else{
			propertyList = Collections.emptyList();
		}
		propertyList.removeIf(String::isEmpty);
		cantRegistHinemosPropertyList = Collections.unmodifiableList(propertyList);
		loggingList("Can't Regist HinemosProperty List", cantRegistHinemosPropertyList);
	}

	/**
	 * シングルトンインスタンスの取得
	 * @return シングルトンインスタンス
	 */
	public static MaintenanceCloudServiceModeUtil getInstance(){
		return instance;
	}

	/**
	 * 制約状態のHinemosプロパティのキー名かを判断する
	 * @param key キー名
	 * @return　制約有無（true：制約あり、false:制約なし)
	 */
	public boolean isRestrictedHinemosProperty(String key){
		// クラウドサービスモードで無い場合は制約なし
		if (!AlterModeArgsUtil.isCloudServiceMode()){
			return false;
		}

		// 変更不可リストが無い場合は制約なし
		if (cantRegistHinemosPropertyList == null){
			return false;
		}

		// 変更不可リストに含まれていれば制約あり
		return cantRegistHinemosPropertyList.contains(key);
	}

	/**
	 * リストの中身をログに出力
	 * @param listName リスト名
	 * @param list リスト
	 */
	private void loggingList(String listName, List<?> list){
		m_log.info(String.format("%s is %s", listName, Arrays.toString(list.toArray())));
	}
}
