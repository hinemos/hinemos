/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.sdml;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.util.Messages;

public class SdmlUtilityConstant {
	// オブジェクト権限の対象
	public static final String OBJECT_TYPE_SDML = HinemosModuleConstant.SDML_CONTROL;

	// --- for Parent
	public static final String SDML = "SDML";
	public static final String STRING_SDML = "SDML";

	// --- for SDML ver 1.x
	public static final String SDML_CONTROL_V1 = "SDML_CONTROL_V1";
	public static final String STRING_SDML_CONTROL_V1 = Messages.getString("sdml.control.setting") + "(1.x)";
	public static final String DEFAULT_XML_SDML_CONTROL_V1 = "sdmlControlV1.xml";
}
