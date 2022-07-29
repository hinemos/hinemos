/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.sdml.ISdmlClientOption;
import com.clustercontrol.sdml.SdmlClientOptionManager;
import com.clustercontrol.sdml.bean.SdmlUtilityActionResult;
import com.clustercontrol.sdml.bean.SdmlXmlFileName;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.bean.FuncTreeItem;
import com.clustercontrol.utility.util.IUtilityPreferenceStore;
import com.clustercontrol.utility.util.UtilityPreferenceStore;

/**
 * 設定インポートエクスポート向けのSDML利用クラス(org.eclipse に依存しないメソッド限定)
 *
 * ポーティングCLIはHineomoClient.jar内のUtiliyt関連クラスの一部を呼び出している。
 *  (BuildFunctionTreeAction CommandAction)
 * それらのクラスが画面表示向けライブラリ（ org.eclipseにパッケージ）を呼び出していると
 * ポーティングCLIではクラス呼び出しエラーが発生する。
 * 上記への対応として ポーティングCLIからの呼び出しに関わるメソッドをこのクラスに集約する。
 */
public class SdmlUtilityInterfaceNoEclipse {
	
	private static Log logger = LogFactory.getLog(SdmlUtilityInterface.class);

	public static void init(ISdmlClientOption option) {
		if (!option.isCommon()) {
			// 共通オプション以外
			// オプション追加時にPreferenceのデフォルト値を登録する
			List<SdmlXmlFileName> list = option.getDefaultXML();
			if (list != null) {
				IUtilityPreferenceStore store = UtilityPreferenceStore.get();
				for (SdmlXmlFileName bean : list) {
					store.setDefault(bean.getXmlDefaultName(), bean.getXmlDefaultName());
				}
			}
		}
	}

	/**
	 * 引数で受け取った{@FuncTreeItem}の子にSDMLの{@FuncTreeItem}を追加する
	 * 
	 * @param parentFunc
	 */
	public static void addSdmlFunction(FuncTreeItem parentFunc) {
		// 共通オプションを取得
		ISdmlClientOption common = SdmlClientOptionManager.getInstance().getCommonOption();
		if (common == null) {
			logger.debug("addSdmlFunction() : SDML Common Option is null.");
			return;
		}

		// まず共通オプションから取得した親Fucnctionを追加
		FuncTreeItem funcCommon = new FuncTreeItem();
		funcCommon.setData(common.getSdmlFuncInfo());
		parentFunc.addChildren(funcCommon);

		// 各オプションから子Functionを取得し親に追加
		FuncTreeItem funcOption = null;
		for (ISdmlClientOption option : SdmlClientOptionManager.getInstance().getOptionListIgnoreCommon()) {
			FuncInfo info = option.getSdmlFuncInfo();
			if (info != null) {
				funcOption = new FuncTreeItem();
				funcOption.setData(info);
				funcCommon.addChildren(funcOption);
			}
		}
	}
	/**
	 * 引数で受け取った{@FuncInfo}がSDMLで追加したものか判定する
	 * 
	 * @param info
	 * @return
	 */
	public static boolean isSdmlFunction(FuncInfo info) {
		// FuncInfoのIDを確認する
		return SdmlClientOptionManager.getInstance().getUtilityFunctionIdList().contains(info.getId());
	}
	
	/**
	 * オプション側で{@WSActionLauncher}を起動する
	 * 
	 * @param args
	 * @return
	 */
	public static SdmlUtilityActionResult launchActionLauncher(String[] args) {
		// ActionLauncherは共通オプションで定義されている
		ISdmlClientOption option = SdmlClientOptionManager.getInstance().getCommonOption();
		if (option == null) {
			logger.debug("launchActionLauncher() : SDML Common Option is null.");
			return new SdmlUtilityActionResult(); // 空で返す
		}

		return option.launchActionLauncher(args);
	}

}
