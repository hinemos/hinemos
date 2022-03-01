/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import org.openapitools.client.model.BinaryPatternInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoResponse;
import org.openapitools.client.model.MonitorTruthValueInfoResponse;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.utility.settings.monitor.xml.BinaryValue;
import com.clustercontrol.utility.settings.monitor.xml.ChangeNotifyId;
import com.clustercontrol.utility.settings.monitor.xml.Common;
import com.clustercontrol.utility.settings.monitor.xml.Monitor;
import com.clustercontrol.utility.settings.monitor.xml.MonitorBinaryPatternInfo;
import com.clustercontrol.utility.settings.monitor.xml.NotifyId;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.PredictionNotifyId;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.DateUtil;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

/**
 * 監視設定の共通部分でXMLのBeanとHinemosのDTOの変換を行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
class MonitorConv {

	private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss";

	/**
	 * <BR>
	 * 
	 * @return
	 */
	static Common versionDto2Xml(){
		Hashtable<String,String> ver = Config.getVersion();

		Common com = new Common();
		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		com.setGenerateDate(new SimpleDateFormat(DATE_FORMAT_PATTERN).format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));
		
		return com;
	}
	
	/**
	 * DTO の NotifyRelationInfo から、castor の NotifyId を作成する。<BR>
	 * 
	 * @version 2.0.0
	 * 
	 */
	private static NotifyId createNotifyId(NotifyRelationInfoResponse notifyRelationInfo) {
		NotifyId notifyId = new NotifyId();
		
		notifyId.setNotifyId(notifyRelationInfo.getNotifyId());
		notifyId.setNotifyType(OpenApiEnumConverter.enumToInteger(notifyRelationInfo.getNotifyType()));
		
		return notifyId;
	}
	
	private static PredictionNotifyId createPredictionNotifyId(NotifyRelationInfoResponse notifyRelationInfo) {
		PredictionNotifyId notifyId = new PredictionNotifyId();
		
		notifyId.setNotifyId(notifyRelationInfo.getNotifyId());
		notifyId.setNotifyType(OpenApiEnumConverter.enumToInteger(notifyRelationInfo.getNotifyType()));
		
		return notifyId;
	}

	private static ChangeNotifyId createChangeNotifyId(NotifyRelationInfoResponse notifyRelationInfo) {
		ChangeNotifyId notifyId = new ChangeNotifyId();
		
		notifyId.setNotifyId(notifyRelationInfo.getNotifyId());
		notifyId.setNotifyType(OpenApiEnumConverter.enumToInteger(notifyRelationInfo.getNotifyType()));
		
		return notifyId;
	}
	
	private static NotifyRelationInfoResponse createNotifyRelationInfo(NotifyId notifyId, String groupId) throws InvalidSetting, HinemosUnknown {
		NotifyRelationInfoResponse notifyRelationInfo = new NotifyRelationInfoResponse();
		
		notifyRelationInfo.setNotifyId(notifyId.getNotifyId());
		notifyRelationInfo.setNotifyType(OpenApiEnumConverter.integerToEnum((int)notifyId.getNotifyType() , NotifyRelationInfoResponse.NotifyTypeEnum.class));
		
		return notifyRelationInfo;
	}

	private static NotifyRelationInfoResponse createPredictionNotifyRelationInfo(PredictionNotifyId notifyId, String groupId) throws InvalidSetting, HinemosUnknown {
		NotifyRelationInfoResponse notifyRelationInfo = new NotifyRelationInfoResponse();
		
		notifyRelationInfo.setNotifyId(notifyId.getNotifyId());
		notifyRelationInfo.setNotifyType(OpenApiEnumConverter.integerToEnum((int)notifyId.getNotifyType(), NotifyRelationInfoResponse.NotifyTypeEnum.class));
		
		return notifyRelationInfo;
	}

	private static NotifyRelationInfoResponse createChangeNotifyRelationInfo(ChangeNotifyId notifyId, String groupId) throws InvalidSetting, HinemosUnknown {
		NotifyRelationInfoResponse notifyRelationInfo = new NotifyRelationInfoResponse();
		
		notifyRelationInfo.setNotifyId(notifyId.getNotifyId());
		notifyRelationInfo.setNotifyType(OpenApiEnumConverter.integerToEnum((int)notifyId.getNotifyType(), NotifyRelationInfoResponse.NotifyTypeEnum.class));
		
		return notifyRelationInfo;
	}
	
	/**
	 * DTO の MonitorInfo から、castor の Monitor を作成する。<BR>
	 * 
	 * @version 2.0.0
	 * @throws ParseException 
	 * @throws  
	 * 
	 */
	static Monitor createMonitor(MonitorInfoResponse monitorInfo_ws) throws ParseException {
		Monitor monitor_cas = new Monitor();

		monitor_cas.setMonitorId(monitorInfo_ws.getMonitorId());
		monitor_cas.setDescription(monitorInfo_ws.getDescription());
		monitor_cas.setFacilityId(monitorInfo_ws.getFacilityId());
		monitor_cas.setScope(monitorInfo_ws.getScope());
		monitor_cas.setMonitorType(OpenApiEnumConverter.enumToInteger(monitorInfo_ws.getMonitorType()));
		monitor_cas.setMonitorTypeId(monitorInfo_ws.getMonitorTypeId());
		monitor_cas.setRunInterval(OpenApiEnumConverter.enumToInteger(monitorInfo_ws.getRunInterval()));
		monitor_cas.setCalendarId(monitorInfo_ws.getCalendarId());
		monitor_cas.setOwnerRoleId(monitorInfo_ws.getOwnerRoleId());
		monitor_cas.setLogFormatId(monitorInfo_ws.getLogFormatId());

		// 不要。
		//monitor_cas.setJudgementInfo(null);

		for (NotifyRelationInfoResponse notifyRelationInfo: monitorInfo_ws.getNotifyRelationList()) {
			monitor_cas.addNotifyId(createNotifyId(notifyRelationInfo));
		}
		
		monitor_cas.setApplication(monitorInfo_ws.getApplication());
		monitor_cas.setMonitorFlg(monitorInfo_ws.getMonitorFlg());
		monitor_cas.setCollectorFlg(monitorInfo_ws.getCollectorFlg());
		
		if(monitorInfo_ws.getPriorityChangeJudgmentType()!=null){
			monitor_cas.setPriorityChangeJudgmentType(
					OpenApiEnumConverter.enumToInteger(monitorInfo_ws.getPriorityChangeJudgmentType()));
		}
		if(monitorInfo_ws.getPriorityChangeFailureType()!=null){
			monitor_cas.setPriorityChangeFailureType(
					OpenApiEnumConverter.enumToInteger(monitorInfo_ws.getPriorityChangeFailureType()));
		}

		// 数値監視のみで使用します。
		monitor_cas.setItemName(monitorInfo_ws.getItemName());
		// 数値監視のみで使用します。
		monitor_cas.setMeasure(monitorInfo_ws.getMeasure());

		if (MonitorInfoResponse.MonitorTypeEnum.NUMERIC == monitorInfo_ws.getMonitorType()) {
			// 数値監視のみで使用します。
			monitor_cas.setPredictionFlg(monitorInfo_ws.getPredictionFlg());
			monitor_cas.setPredictionMethod(OpenApiEnumConverter.enumToString(monitorInfo_ws.getPredictionMethod()));
			monitor_cas.setPredictionAnalysysRange(monitorInfo_ws.getPredictionAnalysysRange());
			monitor_cas.setPredictionTarget(monitorInfo_ws.getPredictionTarget());
			monitor_cas.setPredictionApplication(monitorInfo_ws.getPredictionApplication());
		}
		
		for (NotifyRelationInfoResponse predictionNotifyRelationInfo: monitorInfo_ws.getPredictionNotifyRelationList()) {
			monitor_cas.addPredictionNotifyId(createPredictionNotifyId(predictionNotifyRelationInfo));
		}
		
		if ( MonitorInfoResponse.MonitorTypeEnum.NUMERIC == monitorInfo_ws.getMonitorType()) {
			// 数値監視のみで使用します。
			monitor_cas.setChangeFlg(monitorInfo_ws.getChangeFlg());
			monitor_cas.setChangeAnalysysRange(monitorInfo_ws.getChangeAnalysysRange());
			monitor_cas.setChangeApplication(monitorInfo_ws.getChangeApplication());
		}
		
		for (NotifyRelationInfoResponse changeNotifyRelationInfo: monitorInfo_ws.getChangeNotifyRelationList()) {
			monitor_cas.addChangeNotifyId(createChangeNotifyId(changeNotifyRelationInfo));
		}	
		
		monitor_cas.setRegDate(DateUtil.convDateFormatHinemos2Iso8601(monitorInfo_ws.getRegDate()));
		monitor_cas.setRegUser(monitorInfo_ws.getRegUser());
		monitor_cas.setUpdateDate( DateUtil.convDateFormatHinemos2Iso8601(monitorInfo_ws.getUpdateDate()));
		monitor_cas.setUpdateUser(monitorInfo_ws.getUpdateUser());
		
		return monitor_cas;
	}
	
	/**
	 * castor の Monitor から、DTO の MonitorInfo を作成する。<BR>
	 * 
	 * @version 2.0.0
	 * @throws HinemosUnknown 
	 * @throws ParseException 
	 * @throws InvalidSetting 
	 * 
	 */
	static MonitorInfoResponse createMonitorInfo(Monitor monitor_cas) throws InvalidSetting, HinemosUnknown, ParseException {
		MonitorInfoResponse monitorInfo_ws = new MonitorInfoResponse();

		monitorInfo_ws.setMonitorId(monitor_cas.getMonitorId());
		monitorInfo_ws.setDescription(monitor_cas.getDescription());
		monitorInfo_ws.setFacilityId(monitor_cas.getFacilityId());
		monitorInfo_ws.setScope(monitor_cas.getScope());
		monitorInfo_ws.setMonitorType(OpenApiEnumConverter.integerToEnum(monitor_cas.getMonitorType(),MonitorInfoResponse.MonitorTypeEnum.class));
		monitorInfo_ws.setMonitorTypeId(monitor_cas.getMonitorTypeId());
		monitorInfo_ws.setRunInterval(OpenApiEnumConverter.integerToEnum(monitor_cas.getRunInterval(),MonitorInfoResponse.RunIntervalEnum.class));
		monitorInfo_ws.setCalendarId(monitor_cas.getCalendarId());
		monitorInfo_ws.setOwnerRoleId(monitor_cas.getOwnerRoleId());
		monitorInfo_ws.setLogFormatId(monitor_cas.getLogFormatId());

		// 不要。
		//monitor.setJudgementInfo(null);

		for (NotifyId notifyId: monitor_cas.getNotifyId()) {
			monitorInfo_ws.getNotifyRelationList().add(createNotifyRelationInfo(notifyId, null));
		}
		
		monitorInfo_ws.setMonitorFlg(monitor_cas.getMonitorFlg());
		monitorInfo_ws.setApplication(monitor_cas.getApplication());
		monitorInfo_ws.setCollectorFlg(monitor_cas.getCollectorFlg());

		if( monitor_cas.hasPriorityChangeJudgmentType()  ){
			monitorInfo_ws.setPriorityChangeJudgmentType(
					OpenApiEnumConverter.integerToEnum(monitor_cas.getPriorityChangeJudgmentType(),
							MonitorInfoResponse.PriorityChangeJudgmentTypeEnum.class));
		}
		if( monitor_cas.hasPriorityChangeFailureType()  ){
			monitorInfo_ws.setPriorityChangeFailureType(
					OpenApiEnumConverter.integerToEnum(monitor_cas.getPriorityChangeFailureType(),
							MonitorInfoResponse.PriorityChangeFailureTypeEnum.class));
		}
		// 数値監視のみで使用します。
		monitorInfo_ws.setItemName(monitor_cas.getItemName());
		// 数値監視のみで使用します。
		monitorInfo_ws.setMeasure(monitor_cas.getMeasure());
		
		monitorInfo_ws.setPredictionFlg(monitor_cas.getPredictionFlg());
		monitorInfo_ws.setPredictionMethod(OpenApiEnumConverter.stringToEnum(monitor_cas.getPredictionMethod(),MonitorInfoResponse.PredictionMethodEnum.class ));
		monitorInfo_ws.setPredictionAnalysysRange(monitor_cas.getPredictionAnalysysRange());
		monitorInfo_ws.setPredictionTarget(monitor_cas.getPredictionTarget());
		monitorInfo_ws.setPredictionApplication(monitor_cas.getPredictionApplication());
		//将来予測の項目設定が存在し（数値監視のみの想定）、かつfalseな場合、関連値が未設定なら画面入力時のデフォルト値を設定(数値監視のみで使用)
		if( monitor_cas.hasPredictionFlg() && monitor_cas.getPredictionFlg() == false){
			if( monitorInfo_ws.getPredictionMethod() == null ){
				monitorInfo_ws.setPredictionMethod(MonitorInfoResponse.PredictionMethodEnum._1);
			}
			if( monitor_cas.hasPredictionAnalysysRange() == false ){
				monitorInfo_ws.setPredictionAnalysysRange(60);
			}
			if( monitor_cas.hasPredictionTarget() == false ){
				monitorInfo_ws.setPredictionTarget(60);
			}
		}

		for (PredictionNotifyId notifyId: monitor_cas.getPredictionNotifyId()) {
			monitorInfo_ws.getPredictionNotifyRelationList().add(createPredictionNotifyRelationInfo(notifyId, null));
		}

		monitorInfo_ws.setChangeFlg(monitor_cas.getChangeFlg());
		monitorInfo_ws.setChangeAnalysysRange(monitor_cas.getChangeAnalysysRange());
		monitorInfo_ws.setChangeApplication(monitor_cas.getChangeApplication());		
		//変化量の項目設定が存在し（数値監視のみの想定）、かつfalseが無効な場合、関連値が未設定なら画面入力時ののデフォルト値を設定(数値監視のみで使用)
		if( monitor_cas.hasChangeFlg() && monitor_cas.getChangeFlg() == false){
			if( monitor_cas.hasChangeAnalysysRange() == false ){
				monitorInfo_ws.setChangeAnalysysRange(60);
			}
		}

		for (ChangeNotifyId notifyId: monitor_cas.getChangeNotifyId()) {
			monitorInfo_ws.getChangeNotifyRelationList().add(createChangeNotifyRelationInfo(notifyId, null));
		}

		// 以下の情報は、スキーマに記載されている通り無視します。
		monitorInfo_ws.setRegDate(DateUtil.convDateFormatIso86012Hinemos(monitor_cas.getRegDate()));
		monitorInfo_ws.setRegUser(monitor_cas.getRegUser());
		monitorInfo_ws.setUpdateDate(DateUtil.convDateFormatIso86012Hinemos(monitor_cas.getUpdateDate()));
		monitorInfo_ws.setUpdateUser(monitor_cas.getUpdateUser());
		
		return monitorInfo_ws;
	}

	/**
	 * DTO の MonitorTruthValueInfo から、Castor の TruthValue を作成する。<BR>
	 * 
	 * @version 2.0.0
	 * 
	 */
	static TruthValue createTruthValue(String monitorId, MonitorTruthValueInfoResponse monitorTruthValueInfo) {
		TruthValue truthValue_cas = new TruthValue();
		truthValue_cas.setMonitorId(monitorId);
		truthValue_cas.setPriority(OpenApiEnumConverter.enumToInteger(monitorTruthValueInfo.getPriority()));
		truthValue_cas.setTruthValue(OpenApiEnumConverter.enumToInteger(monitorTruthValueInfo.getTruthValue()));
		
		return truthValue_cas;
	}

	static MonitorTruthValueInfoResponse createTruthValue(TruthValue truthValue) throws InvalidSetting, HinemosUnknown {
		MonitorTruthValueInfoResponse monitorTruthValueInfo = new MonitorTruthValueInfoResponse();
		monitorTruthValueInfo.setPriority(OpenApiEnumConverter.integerToEnum(truthValue.getPriority(),MonitorTruthValueInfoResponse.PriorityEnum.class));
		monitorTruthValueInfo.setTruthValue(OpenApiEnumConverter.integerToEnum(truthValue.getTruthValue(),MonitorTruthValueInfoResponse.TruthValueEnum.class));
		
		return monitorTruthValueInfo;
	}
	
	static NumericValue createNumericValue(String monitorId, MonitorNumericValueInfoResponse monitorNumericValueInfo) {
		NumericValue numericValue = new NumericValue();
		
		numericValue.setMonitorId(monitorId);
		numericValue.setNumericType(OpenApiEnumConverter.enumToString(monitorNumericValueInfo.getMonitorNumericType()));
		numericValue.setPriority(OpenApiEnumConverter.enumToInteger(monitorNumericValueInfo.getPriority()));
		numericValue.setThresholdLowerLimit(monitorNumericValueInfo.getThresholdLowerLimit());
		numericValue.setThresholdUpperLimit(monitorNumericValueInfo.getThresholdUpperLimit());
		
		return numericValue;
	}

	static NumericChangeAmount createNumericChangeAmount(String monitorId,MonitorNumericValueInfoResponse monitorNumericValueInfo) {
		NumericChangeAmount changeValue = new NumericChangeAmount();
		
		changeValue.setMonitorId(monitorId);
		changeValue.setNumericType(OpenApiEnumConverter.enumToString(monitorNumericValueInfo.getMonitorNumericType()));
		changeValue.setPriority(OpenApiEnumConverter.enumToInteger(monitorNumericValueInfo.getPriority()));
		changeValue.setThresholdLowerLimit(monitorNumericValueInfo.getThresholdLowerLimit());
		changeValue.setThresholdUpperLimit(monitorNumericValueInfo.getThresholdUpperLimit());
		
		return changeValue;
	}
	
	static MonitorNumericValueInfoResponse createMonitorNumericValueInfo(NumericValue numericValue) throws InvalidSetting, HinemosUnknown {
		MonitorNumericValueInfoResponse monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
		
		monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.BASIC);
		monitorNumericValueInfo.setPriority(OpenApiEnumConverter.integerToEnum(numericValue.getPriority(),MonitorNumericValueInfoResponse.PriorityEnum.class));
		monitorNumericValueInfo.setThresholdLowerLimit(numericValue.getThresholdLowerLimit());
		monitorNumericValueInfo.setThresholdUpperLimit(numericValue.getThresholdUpperLimit());
		
		return monitorNumericValueInfo;
	}

	static MonitorNumericValueInfoResponse createMonitorNumericValueInfo(NumericChangeAmount changeValue) throws InvalidSetting, HinemosUnknown {
		MonitorNumericValueInfoResponse monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
		
		monitorNumericValueInfo.setMonitorNumericType( MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE );
		monitorNumericValueInfo.setPriority(OpenApiEnumConverter.integerToEnum(changeValue.getPriority(),MonitorNumericValueInfoResponse.PriorityEnum.class  ));
		monitorNumericValueInfo.setThresholdLowerLimit(changeValue.getThresholdLowerLimit());
		monitorNumericValueInfo.setThresholdUpperLimit(changeValue.getThresholdUpperLimit());
		
		return monitorNumericValueInfo;
	}
	
	static void setMonitorChangeAmountDefault(MonitorInfoResponse monitorInfo) {
		MonitorNumericValueInfoResponse monitorNumericValueInfoDefault = new MonitorNumericValueInfoResponse();
		monitorNumericValueInfoDefault.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
		monitorNumericValueInfoDefault.setPriority( MonitorNumericValueInfoResponse.PriorityEnum.INFO);
		monitorNumericValueInfoDefault.setThresholdLowerLimit(-1.0);
		monitorNumericValueInfoDefault.setThresholdUpperLimit(1.0);
		monitorInfo.getNumericValueInfo().add(monitorNumericValueInfoDefault);

		monitorNumericValueInfoDefault = new MonitorNumericValueInfoResponse();
		monitorNumericValueInfoDefault.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
		monitorNumericValueInfoDefault.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.WARNING);
		monitorNumericValueInfoDefault.setThresholdLowerLimit(-2.0);
		monitorNumericValueInfoDefault.setThresholdUpperLimit(2.0);
		monitorInfo.getNumericValueInfo().add(monitorNumericValueInfoDefault);
	}
	
	static StringValue createStringValue(String monitorId,MonitorStringValueInfoResponse monitorStringValueInfo, int orderNo) {
		StringValue stringValue = new StringValue();
		
		stringValue.setMonitorId(monitorId);
		stringValue.setPriority(OpenApiEnumConverter.enumToInteger(monitorStringValueInfo.getPriority()));
		stringValue.setCaseSensitivityFlg(monitorStringValueInfo.getCaseSensitivityFlg());
		stringValue.setDescription(monitorStringValueInfo.getDescription());
		stringValue.setOrderNo(orderNo);
		stringValue.setPattern(monitorStringValueInfo.getPattern());
		stringValue.setProcessType(monitorStringValueInfo.getProcessType());
		stringValue.setValidFlg(monitorStringValueInfo.getValidFlg());
		stringValue.setMessage(monitorStringValueInfo.getMessage());
		
		return stringValue;
	}

	static MonitorStringValueInfoResponse createMonitorStringValueInfo(StringValue stringValue) throws InvalidSetting, HinemosUnknown {
		MonitorStringValueInfoResponse monitorStringValueInfo = new MonitorStringValueInfoResponse();
		
		if(stringValue.hasCaseSensitivityFlg()){
			monitorStringValueInfo.setCaseSensitivityFlg(stringValue.getCaseSensitivityFlg());
		}
		monitorStringValueInfo.setDescription(stringValue.getDescription());
		monitorStringValueInfo.setPattern(stringValue.getPattern());
		if(stringValue.hasProcessType()){
			monitorStringValueInfo.setProcessType(stringValue.getProcessType());
		}
		monitorStringValueInfo.setValidFlg(stringValue.getValidFlg());
		if (stringValue.hasPriority()) {
			monitorStringValueInfo.setPriority(OpenApiEnumConverter.integerToEnum(stringValue.getPriority(),MonitorStringValueInfoResponse.PriorityEnum.class));
		}
		monitorStringValueInfo.setMessage(stringValue.getMessage());
		
		return monitorStringValueInfo;
	}

	protected static void sort(StringValue[] objects) {
		try {
			Arrays.sort(
				objects,
				new Comparator<StringValue>() {
					@Override
					public int compare(StringValue obj1, StringValue obj2) {
						return obj1.getOrderNo() - obj2.getOrderNo();
					}
				});
		}
		catch (Exception e) {
			
		}
	}
	static MonitorInfoResponse makeErrorMonitor(String monitorId, String description) {
		MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
		monitorInfo.setMonitorId(monitorId);
		monitorInfo.setMonitorType(null);
		monitorInfo.setDescription(description);
		return monitorInfo;
	}

	public static BinaryPatternInfoResponse createMonitorBinaryValueInfo(MonitorBinaryPatternInfo binaryValue) throws InvalidSetting, HinemosUnknown {
		BinaryPatternInfoResponse monitorBinaryValueInfo = new BinaryPatternInfoResponse();
		
		monitorBinaryValueInfo.setDescription(binaryValue.getDescription());
		
		monitorBinaryValueInfo.setGrepString(binaryValue.getPattern());
		monitorBinaryValueInfo.setProcessType(binaryValue.getProcessType());
		monitorBinaryValueInfo.setEncoding(binaryValue.getTextEncoding());
		monitorBinaryValueInfo.setPriority(OpenApiEnumConverter.integerToEnum(binaryValue.getPriority(), BinaryPatternInfoResponse.PriorityEnum.class));
		monitorBinaryValueInfo.setMessage(binaryValue.getMessage());
		monitorBinaryValueInfo.setValidFlg(binaryValue.getValidFlg());
		
		return monitorBinaryValueInfo;
	}

	public static BinaryValue createBinaryValue(String monitorId,BinaryPatternInfoResponse monitorBinaryValueInfo, int orderNo) {
		BinaryValue binaryValue = new BinaryValue();

		binaryValue.setMonitorId(monitorId);
		binaryValue.setTextEncoding(monitorBinaryValueInfo.getEncoding());

		binaryValue.setPriority(OpenApiEnumConverter.enumToInteger(monitorBinaryValueInfo.getPriority()));
		binaryValue.setPattern(monitorBinaryValueInfo.getGrepString());
		binaryValue.setDescription(monitorBinaryValueInfo.getDescription());
		binaryValue.setProcessType(monitorBinaryValueInfo.getProcessType());
		binaryValue.setValidFlg(monitorBinaryValueInfo.getValidFlg());
		binaryValue.setMessage(monitorBinaryValueInfo.getMessage());

		binaryValue.setOrderNo(orderNo);

		return binaryValue;
	}

	public static void sort(BinaryValue[] objects) {
		try {
			Arrays.sort(
				objects,
				new Comparator<BinaryValue>() {
					@Override
					public int compare(BinaryValue obj1, BinaryValue obj2) {
						return obj1.getOrderNo() - obj2.getOrderNo();
					}
				});
		}
		catch (Exception e) {
		}
	}
}