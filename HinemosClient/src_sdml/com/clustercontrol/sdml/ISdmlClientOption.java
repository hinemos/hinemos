/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml;

import java.util.List;

import com.clustercontrol.sdml.bean.SdmlUtilityActionResult;
import com.clustercontrol.sdml.bean.SdmlXmlFileName;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;

public interface ISdmlClientOption {
	public boolean isCommon();

	public String getUrl();

	public String getPerspectiveId();

	public String getPluginId(String managerName, String sdmlMonitorTypeId);

	public boolean isSdmlPluginId(String managerName, String pluginId);

	public List<String> getApplicationIdList(String managerName);

	public void updateCaches();

	// --- for Utility
	public String getUtilityFunctionId();

	public FuncInfo getSdmlFuncInfo();

	public SdmlUtilityActionResult launchActionLauncher(String[] args);

	public List<SdmlXmlFileName> getDefaultXML();
}
