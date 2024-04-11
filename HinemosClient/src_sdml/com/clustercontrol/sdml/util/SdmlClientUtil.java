/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.MonitorInfoBeanResponse;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.sdml.ISdmlClientOption;
import com.clustercontrol.sdml.SdmlClientOptionManager;

/**
 * クライアント本体向けのSDML利用クラス
 * 本体機能で必要なSDML特有の処理をこのクラスに集約する
 *
 */
public class SdmlClientUtil {
	private static Log logger = LogFactory.getLog(SdmlClientUtil.class);

	/**
	 * オプション追加時に実行したい処理を定義する
	 * 
	 * @param option
	 */
	public static void init(ISdmlClientOption option) {
		// Utilityに関する初期化処理
		SdmlUtilityInterfaceNoEclipse.init(option);
	}

	/**
	 * 監視設定がSDMLで自動作成された監視設定か判定する
	 * 
	 * @param monitorInfo
	 * @return
	 */
	public static boolean isCreatedBySdml(MonitorInfoBeanResponse monitorInfo) {
		if (monitorInfo == null) {
			return false;
		}
		return monitorInfo.getSdmlMonitorTypeId() != null && !monitorInfo.getSdmlMonitorTypeId().equals("");
	}

	/**
	 * 監視設定がSDMLで自動作成された監視設定か判定する
	 * 
	 * @param monitorInfo
	 * @return
	 */
	public static boolean isCreatedBySdml(MonitorInfoResponse monitorInfo) {
		if (monitorInfo == null) {
			return false;
		}
		return monitorInfo.getSdmlMonitorTypeId() != null && !monitorInfo.getSdmlMonitorTypeId().equals("");
	}

	/**
	 * 追加されているオプションに該当のURLが存在するか判定する
	 * 
	 * @param srcUrl
	 * @return
	 */
	public static boolean isSdmlUrl(String srcUrl) {
		// 共通オプション以外
		for (ISdmlClientOption option : SdmlClientOptionManager.getInstance().getOptionListIgnoreCommon()) {
			if (option.getUrl().equals(srcUrl)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * URLから該当バージョンのパースペクティブIDを取得
	 * 
	 * @param url
	 * @return
	 */
	public static String getPerspectiveId(String url) {
		// URLから該当バージョンのオプションを取得
		ISdmlClientOption option = SdmlClientOptionManager.getInstance().getOption(url);

		if (option == null) {
			logger.warn("getPerspectiveId() : SDML Option is null. url=" + url);
			return "";
		}
		return option.getPerspectiveId();
	}

	/**
	 * SDML監視種別のプラグインIDを取得する
	 * 
	 * @param managerName
	 * @param monitorInfo
	 * @return
	 */
	public static String getPluginId(String managerName, MonitorInfoBeanResponse monitorInfo) {
		if (monitorInfo == null) {
			logger.warn("getPluginId() : monitorInfo is empty.");
			return "";
		}
		// 共通オプションを取得
		ISdmlClientOption option = SdmlClientOptionManager.getInstance().getCommonOption();

		if (option == null) {
			logger.debug("getPluginId() : SDML Common Option is null.");
			return "";
		}
		String rtn = option.getPluginId(managerName, monitorInfo.getSdmlMonitorTypeId());
		if (rtn == null || rtn.isEmpty()) {
			logger.debug("getPluginId() : Could not get pluginId from " + monitorInfo.getMonitorId());
		}
		return rtn;
	}

	/**
	 * プラグインIDがSDMLで自動作成した監視のものか判定する
	 * 
	 * @param managerName
	 * @param pluginId
	 * @return
	 */
	public static boolean isSdmlPluginId(String managerName, String pluginId) {
		if (managerName == null || managerName.isEmpty()) {
			return false;
		}
		if (pluginId == null || pluginId.isEmpty()) {
			logger.warn("isSdmlPluginId() : pluginId is empty.");
			return false;
		}
		// 共通オプションを取得
		ISdmlClientOption option = SdmlClientOptionManager.getInstance().getCommonOption();

		if (option == null) {
			logger.debug("isSdmlPluginId() : SDML Common Option is null.");
			return false;
		}
		return option.isSdmlPluginId(managerName, pluginId);
	}

	/**
	 * 指定された監視項目IDから本来のプラグインIDを取得する
	 * 
	 * @param managerName
	 * @param monitorId
	 * @return
	 */
	public static String getActualPluginId(String managerName, String monitorId) {
		String pluginId = "";
		try {
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
			MonitorInfoResponse info = wrapper.getMonitor(monitorId);
			pluginId = info.getMonitorTypeId();
		} catch (Exception e) {
			logger.warn("getActualPluginId() : " + e.getMessage());
		}
		if (pluginId == null || pluginId.isEmpty()) {
			logger.warn("getActualPluginId() : Could not get pluginId from " + monitorId);
			return "";
		}
		return pluginId;
	}

	/**
	 * 全てのバージョンのアプリケーションID一覧を取得する
	 * 
	 * @param managerName
	 * @return
	 */
	public static List<String> getAllApplicationIdList(String managerName) {
		List<String> rtn = new ArrayList<>();
		// 共通オプション以外
		for (ISdmlClientOption option : SdmlClientOptionManager.getInstance().getOptionListIgnoreCommon()) {
			rtn.addAll(option.getApplicationIdList(managerName));
		}
		return rtn;
	}

	/**
	 * ログイン後にキャッシュを更新する
	 */
	public static void updateCachesAfterLogin() {
		for (ISdmlClientOption option : SdmlClientOptionManager.getInstance().getOptionList()) {
			option.updateCaches();
		}
	}
}
