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

import com.clustercontrol.sdml.action.GetSdmlControlSettingV1;
import com.clustercontrol.sdml.bean.SdmlUtilityActionResult;
import com.clustercontrol.sdml.bean.SdmlXmlFileName;
import com.clustercontrol.sdml.ui.SdmlSettingPerspective;
import com.clustercontrol.utility.settings.sdml.SdmlUtilityConstant;
import com.clustercontrol.utility.settings.sdml.action.SdmlControlV1Action;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.constant.CommandConstant;

/**
 * SDML Version1のクライアントオプションクラス
 *
 */
public class SdmlV1ClientOption implements ISdmlClientOption {
	private static Log logger = LogFactory.getLog(SdmlV1ClientOption.class);

	private static final String URL = "sdml/v1";

	@Override
	public boolean isCommon() {
		return false;
	}

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public String getPerspectiveId() {
		return SdmlSettingPerspective.class.getName();
	}

	@Override
	public String getPluginId(String managerName, String sdmlMonitorTypeId) {
		logger.error("getPluginId() : Not defined with this Option. url=" + URL);
		return null;
	}

	@Override
	public boolean isSdmlPluginId(String managerName, String pluginId) {
		logger.error("isSdmlPluginId() : Not defined with this Option. url=" + URL);
		return false;
	}

	@Override
	public List<String> getApplicationIdList(String managerName) {
		return new GetSdmlControlSettingV1().getApplicationIdList(managerName);
	}

	@Override
	public String getUtilityFunctionId() {
		return SdmlUtilityConstant.SDML_CONTROL_V1;
	}

	@Override
	public FuncInfo getSdmlFuncInfo() {
		return new FuncInfo(
				SdmlUtilityConstant.SDML_CONTROL_V1,
				SdmlUtilityConstant.STRING_SDML_CONTROL_V1,
				SdmlUtilityConstant.DEFAULT_XML_SDML_CONTROL_V1,
				CommandConstant.WEIGHT_SDML_BASE + 0, // v1はそのまま
				SdmlControlV1Action.class.getName(),
				true,
				SdmlUtilityConstant.OBJECT_TYPE_SDML);
	}

	@Override
	public SdmlUtilityActionResult launchActionLauncher(String[] args) {
		logger.error("launchActionLauncher() : Not defined with this Option. url=" + URL);
		// ActionLauncherでクラスを振り分けるためオプション個別では定義せずCommonに定義する
		return null;
	}

	@Override
	public List<SdmlXmlFileName> getDefaultXML() {
		List<SdmlXmlFileName> list = new ArrayList<>();
		list.add(new SdmlXmlFileName(SdmlUtilityConstant.DEFAULT_XML_SDML_CONTROL_V1,
				SdmlUtilityConstant.STRING_SDML_CONTROL_V1, 1));
		return list;
	}

}
