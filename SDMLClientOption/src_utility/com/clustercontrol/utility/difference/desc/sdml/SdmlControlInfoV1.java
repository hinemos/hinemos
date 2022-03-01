/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.difference.desc.sdml;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.utility.difference.DiffAnnotation;

@DiffAnnotation("{\"type\":\"Element\"}")
public class SdmlControlInfoV1 {
	private String applicationId;
	private String description;
	private String ownerRoleId;
	private String facilityId;
	private String scope;
	private boolean validFlg;
	private String controlLogDirectory;
	private String controlLogFilename;
	private boolean controlLogCollectFlg;
	private String application;
	private boolean autoMonitorDeleteFlg;
	private String autoMonitorCalendarId;
	private int earlyStopThresholdSecond;
	private int earlyStopNotifyPriority;
	private int autoCreateSuccessPriority;
	private int autoEnableSuccessPriority;
	private int autoDisableSuccessPriority;
	private int autoUpdateSuccessPriority;
	private int autoControlFailedPriority;
	private List<NotifyRelationInfo> notifyIds = new ArrayList<>();
	private List<NotifyRelationInfo> autoMonitorCommonNotifyIds = new ArrayList<>();
	private List<MonitorNotifyRelation> monitorNotifyRelations = new ArrayList<>();

	@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
	public String getApplicationId() {
		return applicationId;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_description\"}"})
	public String getDescription() {
		return description;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_ownerRoleId\"}"})
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_facilityId\"}"})
	public String getFacilityId() {
		return facilityId;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_scope\"}"})
	public String getScope() {
		return scope;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_validFlg\"}"})
	public boolean getValidFlg() {
		return validFlg;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_controlLogDirectory\"}"})
	public String getControlLogDirectory() {
		return controlLogDirectory;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_controlLogFilename\"}"})
	public String getControlLogFilename() {
		return controlLogFilename;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_controlLogCollectFlg\"}"})
	public boolean getControlLogCollectFlg() {
		return controlLogCollectFlg;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_application\"}"})
	public String getApplication() {
		return application;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_autoMonitorDeleteFlg\"}"})
	public boolean isAutoMonitorDeleteFlg() {
		return autoMonitorDeleteFlg;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_autoMonitorCalendarId\"}"})
	public String getAutoMonitorCalendarId() {
		return autoMonitorCalendarId;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_earlyStopThresholdSecond\"}"})
	public int getEarlyStopThresholdSecond() {
		return earlyStopThresholdSecond;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_earlyStopNotifyPriority\"}",
			"{\"type\":\"Translate\"," +
				"\"values\":[" +
					"{\"value\":\"0\", \"name\":\"name_rick\"}," +
					"{\"value\":\"1\", \"name\":\"name_unknown\"}," +
					"{\"value\":\"2\", \"name\":\"name_warning\"}," +
					"{\"value\":\"3\", \"name\":\"name_information\"}," +
					"{\"value\":\"4\", \"name\":\"name_none\"}" +
				"]" +
			"}"})
	public int getEarlyStopNotifyPriority() {
		return earlyStopNotifyPriority;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_autoCreateSuccessPriority\"}",
			"{\"type\":\"Translate\"," +
				"\"values\":[" +
					"{\"value\":\"0\", \"name\":\"name_rick\"}," +
					"{\"value\":\"1\", \"name\":\"name_unknown\"}," +
					"{\"value\":\"2\", \"name\":\"name_warning\"}," +
					"{\"value\":\"3\", \"name\":\"name_information\"}," +
					"{\"value\":\"4\", \"name\":\"name_none\"}" +
				"]" +
			"}"})
	public int getAutoCreateSuccessPriority() {
		return autoCreateSuccessPriority;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_autoEnableSuccessPriority\"}",
			"{\"type\":\"Translate\"," +
				"\"values\":[" +
					"{\"value\":\"0\", \"name\":\"name_rick\"}," +
					"{\"value\":\"1\", \"name\":\"name_unknown\"}," +
					"{\"value\":\"2\", \"name\":\"name_warning\"}," +
					"{\"value\":\"3\", \"name\":\"name_information\"}," +
					"{\"value\":\"4\", \"name\":\"name_none\"}" +
				"]" +
			"}"})
	public int getAutoEnableSuccessPriority() {
		return autoEnableSuccessPriority;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_autoDisableSuccessPriority\"}",
			"{\"type\":\"Translate\"," +
				"\"values\":[" +
					"{\"value\":\"0\", \"name\":\"name_rick\"}," +
					"{\"value\":\"1\", \"name\":\"name_unknown\"}," +
					"{\"value\":\"2\", \"name\":\"name_warning\"}," +
					"{\"value\":\"3\", \"name\":\"name_information\"}," +
					"{\"value\":\"4\", \"name\":\"name_none\"}" +
				"]" +
			"}"})
	public int getAutoDisableSuccessPriority() {
		return autoDisableSuccessPriority;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_autoUpdateSuccessPriority\"}",
			"{\"type\":\"Translate\"," +
				"\"values\":[" +
					"{\"value\":\"0\", \"name\":\"name_rick\"}," +
					"{\"value\":\"1\", \"name\":\"name_unknown\"}," +
					"{\"value\":\"2\", \"name\":\"name_warning\"}," +
					"{\"value\":\"3\", \"name\":\"name_information\"}," +
					"{\"value\":\"4\", \"name\":\"name_none\"}" +
				"]" +
			"}"})
	public int getAutoUpdateSuccessPriority() {
		return autoUpdateSuccessPriority;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_autoControlFailedPriority\"}",
			"{\"type\":\"Translate\"," +
				"\"values\":[" +
					"{\"value\":\"0\", \"name\":\"name_rick\"}," +
					"{\"value\":\"1\", \"name\":\"name_unknown\"}," +
					"{\"value\":\"2\", \"name\":\"name_warning\"}," +
					"{\"value\":\"3\", \"name\":\"name_information\"}," +
					"{\"value\":\"4\", \"name\":\"name_none\"}" +
				"]" +
			"}"})
	public int getAutoControlFailedPriority() {
		return autoControlFailedPriority;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_notifyId\"}",
			"{\"type\":\"Array\"}"})
	public NotifyRelationInfo[] getNotifyIds() {
		return notifyIds.toArray(new NotifyRelationInfo[0]);
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"SdmlControlInfoV1Type_autoMonitorCommonNotifyId\"}",
			"{\"type\":\"Array\"}"})
	public NotifyRelationInfo[] getAutoMonitorCommonNotifyIds() {
		return autoMonitorCommonNotifyIds.toArray(new NotifyRelationInfo[0]);
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"MonitorNotifyRelation_monitorNotifyRelation\"}",
			"{\"type\":\"Array\", \"idType\":\"prop\", \"propName\":\"sdmlMonitorTypeId\"}"})
	public MonitorNotifyRelation[] getMonitorNotifyRelations() {
		return monitorNotifyRelations.toArray(new MonitorNotifyRelation[0]);
	}

	public static SdmlControlInfoV1 getCopiedInstance(com.clustercontrol.utility.settings.sdml.xml.SdmlControlInfoV1Type xmlInfo) {
		SdmlControlInfoV1 rtn = new SdmlControlInfoV1();
		rtn.applicationId = xmlInfo.getApplicationId();
		rtn.description = xmlInfo.getDescription();
		rtn.ownerRoleId = xmlInfo.getOwnerRoleId();
		rtn.facilityId = xmlInfo.getFacilityId();
		rtn.scope = xmlInfo.getScope();
		rtn.validFlg = xmlInfo.getValidFlg();
		rtn.controlLogDirectory = xmlInfo.getControlLogDirectory();
		rtn.controlLogFilename = xmlInfo.getControlLogFilename();
		rtn.controlLogCollectFlg = xmlInfo.getControlLogCollectFlg();
		rtn.application = xmlInfo.getApplication();
		rtn.autoMonitorDeleteFlg = xmlInfo.getAutoMonitorDeleteFlg();
		rtn.autoMonitorCalendarId = xmlInfo.getAutoMonitorCalendarId();
		rtn.earlyStopThresholdSecond = xmlInfo.getEarlyStopThresholdSecond();
		rtn.earlyStopNotifyPriority = xmlInfo.getEarlyStopNotifyPriority();
		rtn.autoCreateSuccessPriority = xmlInfo.getAutoCreateSuccessPriority();
		rtn.autoEnableSuccessPriority = xmlInfo.getAutoEnableSuccessPriority();
		rtn.autoDisableSuccessPriority = xmlInfo.getAutoDisableSuccessPriority();
		rtn.autoUpdateSuccessPriority = xmlInfo.getAutoUpdateSuccessPriority();
		rtn.autoControlFailedPriority = xmlInfo.getAutoControlFailedPriority();
		for (com.clustercontrol.utility.settings.sdml.xml.NotifyId notify : xmlInfo.getNotifyId()) {
			rtn.notifyIds.add(NotifyRelationInfo.getCopiedInstance(notify));
		}
		for (com.clustercontrol.utility.settings.sdml.xml.AutoMonitorCommonNotifyId notify : xmlInfo.getAutoMonitorCommonNotifyId()) {
			rtn.autoMonitorCommonNotifyIds.add(NotifyRelationInfo.getCopiedInstance(notify));
		}
		for (com.clustercontrol.utility.settings.sdml.xml.MonitorNotifyRelationList relation : xmlInfo.getMonitorNotifyRelationList()) {
			rtn.monitorNotifyRelations.add(MonitorNotifyRelation.getCopiedInstance(relation));
		}
		return rtn;
	}
}
