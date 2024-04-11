/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.sdml.bean.SdmlXmlFileName;
import com.clustercontrol.sdml.util.SdmlClientUtil;

/**
 * SDMLのクライアントオプション管理用クラス
 *
 */
public class SdmlClientOptionManager {
	private static Log logger = LogFactory.getLog(SdmlClientOptionManager.class);

	private static final SdmlClientOptionManager instance = new SdmlClientOptionManager();

	/** オプション管理用Map */
	private Map<String, ISdmlClientOption> sdmlClientOptionMap = new ConcurrentHashMap<>();

	// 動的にオプションが追加されることはないので、利便性のため初回のみ生成して保持しておく
	/** 設定インポートエクスポートのFunctionInfoのIDのリスト */
	private List<String> utilityFunctionIdList = null;
	/** XMLのデフォルトファイル名を保持したマップ */
	private Map<String, SdmlXmlFileName> xmlFileNameMap = null;

	/**
	 * コンストラクタ<BR>
	 * getInstance()を利用すること
	 */
	private SdmlClientOptionManager() {
	}

	/**
	 * インスタンス取得
	 * 
	 * @return
	 */
	public static SdmlClientOptionManager getInstance() {
		return instance;
	}

	public void addOption(ISdmlClientOption option) {
		logger.debug("addOption() : " + option.getUrl());
		sdmlClientOptionMap.put(option.getUrl(), option);
		// オプション追加時の処理
		SdmlClientUtil.init(option);
	}

	public ISdmlClientOption getOption(String url) {
		return sdmlClientOptionMap.get(url);
	}

	/**
	 * 全てのオプションを取得する
	 * 
	 * @return
	 */
	public List<ISdmlClientOption> getOptionList() {
		return new ArrayList<>(sdmlClientOptionMap.values());
	}

	/**
	 * 共通オプションを取得する
	 * 
	 * @return
	 */
	public ISdmlClientOption getCommonOption() {
		for (ISdmlClientOption option : sdmlClientOptionMap.values()) {
			if (option.isCommon()) {
				return option;
			}
		}
		return null;
	}

	/**
	 * 共通オプション以外のオプションを取得する
	 * 
	 * @return
	 */
	public List<ISdmlClientOption> getOptionListIgnoreCommon() {
		List<ISdmlClientOption> list = new ArrayList<>();
		for (ISdmlClientOption option : sdmlClientOptionMap.values()) {
			if (!option.isCommon()) {
				list.add(option);
			}
		}
		return list;
	}

	/**
	 * 存在するオプションのURLリストを取得する
	 * 
	 * @return
	 */
	public List<String> getUrlList() {
		return new ArrayList<>(sdmlClientOptionMap.keySet());
	}

	public List<String> getUtilityFunctionIdList() {
		if (utilityFunctionIdList == null || utilityFunctionIdList.isEmpty()) {
			utilityFunctionIdList = new ArrayList<>();
			// 共通オプション以外
			for (ISdmlClientOption option : getOptionListIgnoreCommon()) {
				if (option.getUtilityFunctionId() != null) {
					utilityFunctionIdList.add(option.getUtilityFunctionId());
				}
			}
		}
		return utilityFunctionIdList;
	}

	public Map<String, SdmlXmlFileName> getXmlFileNameMap() {
		if (xmlFileNameMap == null || xmlFileNameMap.isEmpty()) {
			xmlFileNameMap = new ConcurrentHashMap<>();
			// 共通オプション以外
			for (ISdmlClientOption option : getOptionListIgnoreCommon()) {
				List<SdmlXmlFileName> list = option.getDefaultXML();
				if (list != null) {
					for (SdmlXmlFileName bean : list) {
						xmlFileNameMap.put(bean.getXmlDefaultName(), bean);
					}
				}
			}
		}
		return xmlFileNameMap;
	}
}
