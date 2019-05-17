/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
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
import com.clustercontrol.ws.monitor.BinaryPatternInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;
import com.clustercontrol.ws.monitor.MonitorTruthValueInfo;
import com.clustercontrol.ws.notify.NotifyRelationInfo;

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
	private static NotifyId createNotifyId(NotifyRelationInfo notifyRelationInfo) {
		NotifyId notifyId = new NotifyId();
		
		notifyId.setNotifyGroupId(notifyRelationInfo.getNotifyGroupId());
		notifyId.setNotifyId(notifyRelationInfo.getNotifyId());
		notifyId.setNotifyType(notifyRelationInfo.getNotifyType());
		
		return notifyId;
	}
	
	private static PredictionNotifyId createPredictionNotifyId(NotifyRelationInfo notifyRelationInfo) {
		PredictionNotifyId notifyId = new PredictionNotifyId();
		
		notifyId.setNotifyGroupId(notifyRelationInfo.getNotifyGroupId());
		notifyId.setNotifyId(notifyRelationInfo.getNotifyId());
		notifyId.setNotifyType(notifyRelationInfo.getNotifyType());
		
		return notifyId;
	}

	private static ChangeNotifyId createChangeNotifyId(NotifyRelationInfo notifyRelationInfo) {
		ChangeNotifyId notifyId = new ChangeNotifyId();
		
		notifyId.setNotifyGroupId(notifyRelationInfo.getNotifyGroupId());
		notifyId.setNotifyId(notifyRelationInfo.getNotifyId());
		notifyId.setNotifyType(notifyRelationInfo.getNotifyType());
		
		return notifyId;
	}
	
	private static NotifyRelationInfo createNotifyRelationInfo(NotifyId notifyId, String groupId) {
		NotifyRelationInfo notifyRelationInfo = new NotifyRelationInfo();
		
		notifyRelationInfo.setNotifyGroupId(groupId);
		notifyRelationInfo.setNotifyId(notifyId.getNotifyId());
		notifyRelationInfo.setNotifyType(notifyId.getNotifyType());
		
		return notifyRelationInfo;
	}

	private static NotifyRelationInfo createPredictionNotifyRelationInfo(PredictionNotifyId notifyId, String groupId) {
		NotifyRelationInfo notifyRelationInfo = new NotifyRelationInfo();
		
		notifyRelationInfo.setNotifyGroupId(groupId);
		notifyRelationInfo.setNotifyId(notifyId.getNotifyId());
		notifyRelationInfo.setNotifyType(notifyId.getNotifyType());
		
		return notifyRelationInfo;
	}

	private static NotifyRelationInfo createChangeNotifyRelationInfo(ChangeNotifyId notifyId, String groupId) {
		NotifyRelationInfo notifyRelationInfo = new NotifyRelationInfo();
		
		notifyRelationInfo.setNotifyGroupId(groupId);
		notifyRelationInfo.setNotifyId(notifyId.getNotifyId());
		notifyRelationInfo.setNotifyType(notifyId.getNotifyType());
		
		return notifyRelationInfo;
	}
	
	/**
	 * DTO の MonitorInfo から、castor の Monitor を作成する。<BR>
	 * 
	 * @version 2.0.0
	 * 
	 */
	static Monitor createMonitor(com.clustercontrol.ws.monitor.MonitorInfo monitorInfo_ws) {
		Monitor monitor_cas = new Monitor();

		monitor_cas.setMonitorId(monitorInfo_ws.getMonitorId());
		monitor_cas.setDescription(monitorInfo_ws.getDescription());
		monitor_cas.setFacilityId(monitorInfo_ws.getFacilityId());
		monitor_cas.setScope(monitorInfo_ws.getScope());
		monitor_cas.setMonitorType(monitorInfo_ws.getMonitorType());
		monitor_cas.setMonitorTypeId(monitorInfo_ws.getMonitorTypeId());
		monitor_cas.setRunInterval(monitorInfo_ws.getRunInterval());
		monitor_cas.setCalendarId(monitorInfo_ws.getCalendarId());
		monitor_cas.setOwnerRoleId(monitorInfo_ws.getOwnerRoleId());
		monitor_cas.setLogFormatId(monitorInfo_ws.getLogFormatId());

		// 不要。
		//monitor_cas.setJudgementInfo(null);

		// "通知グループIDです。 入力は不要です。" と、part_monitor_master.xsd に記載されていましたが、
		// インポート時にないと 通知の登録情報が消えました。
//		monitor_cas.setNotifyGroupId(monitorInfo_ws.getNotifyGroupId());

		for (NotifyRelationInfo notifyRelationInfo: monitorInfo_ws.getNotifyRelationList()) {
			monitor_cas.addNotifyId(createNotifyId(notifyRelationInfo));
		}
		
		monitor_cas.setApplication(monitorInfo_ws.getApplication());
		monitor_cas.setMonitorFlg(monitorInfo_ws.isMonitorFlg());
		monitor_cas.setCollectorFlg(monitorInfo_ws.isCollectorFlg());

		// 数値監視のみで使用します。
		monitor_cas.setItemName(monitorInfo_ws.getItemName());
		// 数値監視のみで使用します。
		monitor_cas.setMeasure(monitorInfo_ws.getMeasure());

		if (MonitorTypeConstant.TYPE_NUMERIC == monitorInfo_ws.getMonitorType()) {
			// 数値監視のみで使用します。
			monitor_cas.setPredictionFlg(monitorInfo_ws.isPredictionFlg());
			monitor_cas.setPredictionMethod(monitorInfo_ws.getPredictionMethod());
			monitor_cas.setPredictionAnalysysRange(monitorInfo_ws.getPredictionAnalysysRange());
			monitor_cas.setPredictionTarget(monitorInfo_ws.getPredictionTarget());
			monitor_cas.setPredictionApplication(monitorInfo_ws.getPredictionApplication());
		}
		
		for (NotifyRelationInfo predictionNotifyRelationInfo: monitorInfo_ws.getPredictionNotifyRelationList()) {
			monitor_cas.addPredictionNotifyId(createPredictionNotifyId(predictionNotifyRelationInfo));
		}
		
		if (MonitorTypeConstant.TYPE_NUMERIC == monitorInfo_ws.getMonitorType()) {
			// 数値監視のみで使用します。
			monitor_cas.setChangeFlg(monitorInfo_ws.isChangeFlg());
			monitor_cas.setChangeAnalysysRange(monitorInfo_ws.getChangeAnalysysRange());
			monitor_cas.setChangeApplication(monitorInfo_ws.getChangeApplication());
		}
		
		for (NotifyRelationInfo changeNotifyRelationInfo: monitorInfo_ws.getChangeNotifyRelationList()) {
			monitor_cas.addChangeNotifyId(createChangeNotifyId(changeNotifyRelationInfo));
		}	
		
		monitor_cas.setRegDate(timeToString(monitorInfo_ws.getRegDate()));
		monitor_cas.setRegUser(monitorInfo_ws.getRegUser());
		monitor_cas.setUpdateDate(timeToString(monitorInfo_ws.getUpdateDate()));
		monitor_cas.setUpdateUser(monitorInfo_ws.getUpdateUser());
		
		return monitor_cas;
	}
	
	/**
	 * castor の Monitor から、DTO の MonitorInfo を作成する。<BR>
	 * 
	 * @version 2.0.0
	 * 
	 */
	static MonitorInfo createMonitorInfo(Monitor monitor_cas) {
		MonitorInfo monitorInfo_ws = new MonitorInfo();

		monitorInfo_ws.setMonitorId(monitor_cas.getMonitorId());
		monitorInfo_ws.setDescription(monitor_cas.getDescription());
		monitorInfo_ws.setFacilityId(monitor_cas.getFacilityId());
		monitorInfo_ws.setScope(monitor_cas.getScope());
		monitorInfo_ws.setMonitorType(monitor_cas.getMonitorType());
		monitorInfo_ws.setMonitorTypeId(monitor_cas.getMonitorTypeId());
		monitorInfo_ws.setRunInterval(monitor_cas.getRunInterval());
		monitorInfo_ws.setCalendarId(monitor_cas.getCalendarId());
		monitorInfo_ws.setFailurePriority(PriorityConstant.TYPE_UNKNOWN);
		monitorInfo_ws.setOwnerRoleId(monitor_cas.getOwnerRoleId());
		monitorInfo_ws.setLogFormatId(monitor_cas.getLogFormatId());

		// 不要。
		//monitor.setJudgementInfo(null);

		// Excel での編集では、NotifyGroupId を考慮しないため、インポート時には、この値が存在しない。
		// NotifyGroupId が存在しない場合は、既定値を作成。
//		monitorInfo_ws.setNotifyGroupId(
//			monitor_cas.getNotifyGroupId() != null ?
//			monitor_cas.getNotifyGroupId() :
//			NotifyGroupIdGenerator.createNotifyGroupId(monitor_cas.getMonitorTypeId(), monitor_cas.getMonitorId())
//			);
		for (NotifyId notifyId: monitor_cas.getNotifyId()) {
			monitorInfo_ws.getNotifyRelationList().add(createNotifyRelationInfo(notifyId, null));
		}
		
		monitorInfo_ws.setApplication(monitor_cas.getApplication());
		monitorInfo_ws.setMonitorFlg(monitor_cas.getMonitorFlg());
		monitorInfo_ws.setCollectorFlg(monitor_cas.getCollectorFlg());

		// 数値監視のみで使用します。
		monitorInfo_ws.setItemName(monitor_cas.getItemName());
		// 数値監視のみで使用します。
		monitorInfo_ws.setMeasure(monitor_cas.getMeasure());
		
		monitorInfo_ws.setPredictionFlg(monitor_cas.getPredictionFlg());
		monitorInfo_ws.setPredictionMethod(monitor_cas.getPredictionMethod());
		monitorInfo_ws.setPredictionAnalysysRange(monitor_cas.getPredictionAnalysysRange());
		monitorInfo_ws.setPredictionTarget(monitor_cas.getPredictionTarget());
		monitorInfo_ws.setPredictionApplication(monitor_cas.getPredictionApplication());
		//将来予測の項目設定が存在し（数値監視のみの想定）、かつfalseな場合、関連値が未設定なら画面入力時のデフォルト値を設定(数値監視のみで使用)
		if( monitor_cas.hasPredictionFlg() && monitor_cas.getPredictionFlg() == false){
			if( monitorInfo_ws.getPredictionMethod() == null || monitorInfo_ws.getPredictionMethod().equals("") ){
				monitorInfo_ws.setPredictionMethod(com.clustercontrol.monitor.run.bean.MonitorPredictionMethodConstant.POLYNOMIAL_1);
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
//		monitorInfo_ws.setRegDate(stringToTime(monitor_cas.getRegDate()));
//		monitorInfo_ws.setRegUser(monitor_cas.getRegUser());
//		monitorInfo_ws.setUpdateDate(stringToTime(monitor_cas.getUpdateDate()));
//		monitorInfo_ws.setUpdateUser(monitor_cas.getUpdateUser());
		
		return monitorInfo_ws;
	}

	private static String timeToString(long time) {
		return new SimpleDateFormat(DATE_FORMAT_PATTERN).format(new Date(time));
	}
	/*
	public static long stringToTime(String timeString) {
		try {
			return new SimpleDateFormat(DATE_FORMAT_PATTERN).parse(timeString).getTime();
		} catch (ParseException e) {
			log.error(e);
		}
		
		return 0;
	}
	*/
	/**
	 * DTO の MonitorTruthValueInfo から、Castor の TruthValue を作成する。<BR>
	 * 
	 * @version 2.0.0
	 * 
	 */
	static TruthValue createTruthValue(MonitorTruthValueInfo monitorTruthValueInfo) {
		TruthValue truthValue_cas = new TruthValue();
		truthValue_cas.setMonitorId(monitorTruthValueInfo.getMonitorId());
		truthValue_cas.setPriority(monitorTruthValueInfo.getPriority());
		truthValue_cas.setTruthValue(monitorTruthValueInfo.getTruthValue());
		
		return truthValue_cas;
	}

	static MonitorTruthValueInfo createTruthValue(TruthValue truthValue) {
		MonitorTruthValueInfo monitorTruthValueInfo = new MonitorTruthValueInfo();
		monitorTruthValueInfo.setMonitorId(truthValue.getMonitorId());
		monitorTruthValueInfo.setPriority(truthValue.getPriority());
		monitorTruthValueInfo.setTruthValue(truthValue.getTruthValue());
		
		return monitorTruthValueInfo;
	}
	
	static NumericValue createNumericValue(MonitorNumericValueInfo monitorNumericValueInfo) {
		NumericValue numericValue = new NumericValue();
		
		numericValue.setMonitorId(monitorNumericValueInfo.getMonitorId());
		numericValue.setNumericType(monitorNumericValueInfo.getMonitorNumericType());
		numericValue.setPriority(monitorNumericValueInfo.getPriority());
		numericValue.setThresholdLowerLimit(monitorNumericValueInfo.getThresholdLowerLimit());
		numericValue.setThresholdUpperLimit(monitorNumericValueInfo.getThresholdUpperLimit());
		
		return numericValue;
	}

	static NumericChangeAmount createNumericChangeAmount(MonitorNumericValueInfo monitorNumericValueInfo) {
		NumericChangeAmount changeValue = new NumericChangeAmount();
		
		changeValue.setMonitorId(monitorNumericValueInfo.getMonitorId());
		changeValue.setNumericType(monitorNumericValueInfo.getMonitorNumericType());
		changeValue.setPriority(monitorNumericValueInfo.getPriority());
		changeValue.setThresholdLowerLimit(monitorNumericValueInfo.getThresholdLowerLimit());
		changeValue.setThresholdUpperLimit(monitorNumericValueInfo.getThresholdUpperLimit());
		
		return changeValue;
	}
	
	static MonitorNumericValueInfo createMonitorNumericValueInfo(NumericValue numericValue) {
		MonitorNumericValueInfo monitorNumericValueInfo = new MonitorNumericValueInfo();
		
		monitorNumericValueInfo.setMonitorId(numericValue.getMonitorId());
		monitorNumericValueInfo.setMonitorNumericType("");
		monitorNumericValueInfo.setPriority(numericValue.getPriority());
		monitorNumericValueInfo.setThresholdLowerLimit(numericValue.getThresholdLowerLimit());
		monitorNumericValueInfo.setThresholdUpperLimit(numericValue.getThresholdUpperLimit());
		
		return monitorNumericValueInfo;
	}

	static MonitorNumericValueInfo createMonitorNumericValueInfo(NumericChangeAmount changeValue) {
		MonitorNumericValueInfo monitorNumericValueInfo = new MonitorNumericValueInfo();
		
		monitorNumericValueInfo.setMonitorId(changeValue.getMonitorId());
		monitorNumericValueInfo.setMonitorNumericType("CHANGE");
		monitorNumericValueInfo.setPriority(changeValue.getPriority());
		monitorNumericValueInfo.setThresholdLowerLimit(changeValue.getThresholdLowerLimit());
		monitorNumericValueInfo.setThresholdUpperLimit(changeValue.getThresholdUpperLimit());
		
		return monitorNumericValueInfo;
	}
	
	static void setMonitorChangeAmountDefault(MonitorInfo monitorInfo) {
		MonitorNumericValueInfo monitorNumericValueInfoDefault = new MonitorNumericValueInfo();
		monitorNumericValueInfoDefault.setMonitorId(monitorInfo.getMonitorId());
		monitorNumericValueInfoDefault.setMonitorNumericType("CHANGE");
		monitorNumericValueInfoDefault.setPriority(PriorityConstant.TYPE_INFO);
		monitorNumericValueInfoDefault.setThresholdLowerLimit(-1.0);
		monitorNumericValueInfoDefault.setThresholdUpperLimit(1.0);
		monitorInfo.getNumericValueInfo().add(monitorNumericValueInfoDefault);

		monitorNumericValueInfoDefault = new MonitorNumericValueInfo();
		monitorNumericValueInfoDefault.setMonitorId(monitorInfo.getMonitorId());
		monitorNumericValueInfoDefault.setMonitorNumericType("CHANGE");
		monitorNumericValueInfoDefault.setPriority(PriorityConstant.TYPE_WARNING);
		monitorNumericValueInfoDefault.setThresholdLowerLimit(-2.0);
		monitorNumericValueInfoDefault.setThresholdUpperLimit(2.0);
		monitorInfo.getNumericValueInfo().add(monitorNumericValueInfoDefault);
	}
	
	static StringValue createStringValue(MonitorStringValueInfo monitorStringValueInfo, int orderNo) {
		StringValue stringValue = new StringValue();
		
		stringValue.setMonitorId(monitorStringValueInfo.getMonitorId());
		stringValue.setPriority(monitorStringValueInfo.getPriority());
		stringValue.setCaseSensitivityFlg(monitorStringValueInfo.isCaseSensitivityFlg());
		stringValue.setDescription(monitorStringValueInfo.getDescription());
		stringValue.setOrderNo(orderNo);
		stringValue.setPattern(monitorStringValueInfo.getPattern());
		stringValue.setProcessType(monitorStringValueInfo.isProcessType());
		stringValue.setValidFlg(monitorStringValueInfo.isValidFlg());
		stringValue.setMessage(monitorStringValueInfo.getMessage());
		
		return stringValue;
	}

	static MonitorStringValueInfo createMonitorStringValueInfo(StringValue stringValue) {
		MonitorStringValueInfo monitorStringValueInfo = new MonitorStringValueInfo();
		
		monitorStringValueInfo.setMonitorId(stringValue.getMonitorId());
		monitorStringValueInfo.setPriority(stringValue.getPriority());
		monitorStringValueInfo.setCaseSensitivityFlg(stringValue.getCaseSensitivityFlg());
		monitorStringValueInfo.setDescription(stringValue.getDescription());
		monitorStringValueInfo.setPattern(stringValue.getPattern());
		monitorStringValueInfo.setProcessType(stringValue.getProcessType());
		monitorStringValueInfo.setValidFlg(stringValue.getValidFlg());
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
	static MonitorInfo makeErrorMonitor(String monitorId, String description) {
		MonitorInfo monitorInfo = new MonitorInfo();
		monitorInfo.setMonitorId(monitorId);
		monitorInfo.setMonitorType(null);
		monitorInfo.setDescription(description);
		return monitorInfo;
	}

	public static BinaryPatternInfo createMonitorBinaryValueInfo(MonitorBinaryPatternInfo binaryValue) {
		BinaryPatternInfo monitorBinaryValueInfo = new BinaryPatternInfo();
		
		monitorBinaryValueInfo.setMonitorId(binaryValue.getMonitorId());
		monitorBinaryValueInfo.setDescription(binaryValue.getDescription());
		
		monitorBinaryValueInfo.setGrepString(binaryValue.getPattern());
		monitorBinaryValueInfo.setProcessType(binaryValue.getProcessType());
		monitorBinaryValueInfo.setEncoding(binaryValue.getTextEncoding());
		monitorBinaryValueInfo.setPriority(binaryValue.getPriority());
		monitorBinaryValueInfo.setMessage(binaryValue.getMessage());
		monitorBinaryValueInfo.setValidFlg(binaryValue.getValidFlg());
		
		return monitorBinaryValueInfo;
	}

	public static BinaryValue createBinaryValue(BinaryPatternInfo monitorBinaryValueInfo, int orderNo) {
		BinaryValue binaryValue = new BinaryValue();

		binaryValue.setMonitorId(monitorBinaryValueInfo.getMonitorId());
		binaryValue.setTextEncoding(monitorBinaryValueInfo.getEncoding());

		binaryValue.setMonitorId(monitorBinaryValueInfo.getMonitorId());
		binaryValue.setPriority(monitorBinaryValueInfo.getPriority());
		binaryValue.setPattern(monitorBinaryValueInfo.getGrepString());
		binaryValue.setDescription(monitorBinaryValueInfo.getDescription());
		binaryValue.setProcessType(monitorBinaryValueInfo.isProcessType());
		binaryValue.setValidFlg(monitorBinaryValueInfo.isValidFlg());
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