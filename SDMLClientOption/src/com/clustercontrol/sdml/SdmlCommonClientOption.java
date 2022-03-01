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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.sdml.bean.SdmlUtilityActionResult;
import com.clustercontrol.sdml.bean.SdmlXmlFileName;
import com.clustercontrol.sdml.util.SdmlMonitorTypeUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.WSActionLauncher;
import com.clustercontrol.utility.settings.sdml.SdmlUtilityConstant;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.constant.CommandConstant;

/**
 * SDMLバージョン共通のクライアントオプションクラス
 *
 */
public class SdmlCommonClientOption implements ISdmlClientOption {
	private static Log logger = LogFactory.getLog(SdmlCommonClientOption.class);

	private static final String URL = "sdml";

	@Override
	public boolean isCommon() {
		return true;
	}

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public String getPerspectiveId() {
		logger.error("getPerspectiveId() : Not defined with this Option. url=" + URL);
		return null;
	}

	@Override
	public String getPluginId(String managerName, String sdmlMonitorTypeId) {
		return SdmlMonitorTypeUtil.getPluginId(managerName, sdmlMonitorTypeId);
	}

	@Override
	public boolean isSdmlPluginId(String managerName, String pluginId) {
		return SdmlMonitorTypeUtil.isSdmlPluginId(managerName, pluginId);
	}

	@Override
	public List<String> getApplicationIdList(String managerName) {
		logger.error("getApplicationIdList() : Not defined with this Option. url=" + URL);
		return null;
	}

	@Override
	public String getUtilityFunctionId() {
		logger.error("getUtilityFunctionId() : Not defined with this Option. url=" + URL);
		return null; // 共通オプションは返す必要なし
	}

	@Override
	public FuncInfo getSdmlFuncInfo() {
		return new FuncInfo(
				SdmlUtilityConstant.SDML,
				SdmlUtilityConstant.STRING_SDML,
				"",
				CommandConstant.WEIGHT_OTHER, 
				"",
				false,
				"");
	}

	@Override
	public SdmlUtilityActionResult launchActionLauncher(String[] args) {
		logger.info("launchActionLauncher() : " + URL);
		SdmlUtilityActionResult ret = new SdmlUtilityActionResult();

		WSActionLauncher helper = new WSActionLauncher(args , new WSActionLauncher.External() {
			@Override
			public Class<?> getClazz(String arg) throws Exception, Throwable {
				// 一見するとオーバーライド元と同様の処理だが、
				// Class.forName()は現在のクラスのクラスローダからクラスを探す。
				// プラグインごとにクラスローダは異なるため、このクラスで定義しなおすことによって
				// SDMLClientOptionのプラグイン側のActionクラスを呼び出すことを可能とする
				return Class.forName(arg);
			}
		});
		try {
			ret.setResult(helper.action());
		} catch (ConvertorException e) {
			logger.error(e.getMessage(), e);
		}
		ret.setStdOut(helper.getStdOut());
		ret.setErrOut(helper.getErrOut());

		logger.debug("launchActionLauncher() : " + ret.toString());
		return ret;
	}

	@Override
	public List<SdmlXmlFileName> getDefaultXML() {
		List<SdmlXmlFileName> list = new ArrayList<>();
		// Preference用に名前のみ定義する
		list.add(new SdmlXmlFileName(null, SdmlUtilityConstant.STRING_SDML, -1));
		return list;
	}

}
