/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.logfile.bean.LogfileLineSeparatorConstant;
import com.clustercontrol.logfile.model.LogfileCheckInfo;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.bean.MonitorPredictionMethod;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.process.model.ProcessCheckInfo;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlInitializeData;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelation;
import com.clustercontrol.sdml.v1.bean.AutoMonitorValidInfo;
import com.clustercontrol.sdml.v1.constant.InitializeKeyEnum;
import com.clustercontrol.sdml.v1.constant.SdmlMonitorTypeEnum;
import com.clustercontrol.util.Messages;

/**
 * 自動作成する監視設定に関するユーティリティクラス
 *
 */
public class SdmlMonitorUtil {
	private static Log logger = LogFactory.getLog(SdmlMonitorUtil.class);

	// 閾値の区切り
	private static final String THRESHOLD_NUM_DELIMITER = "-";
	private static final int[] THRESHOLD_PRIORITIES = {
			PriorityConstant.TYPE_CRITICAL,
			PriorityConstant.TYPE_UNKNOWN,
			PriorityConstant.TYPE_WARNING,
			PriorityConstant.TYPE_INFO,
		};

	private static final String ENCODE_UTF8 = "UTF-8";
	// ログファイル監視の区切り条件
	private static final String SEPARATION_HEAD_PATTERN = "1";
	private static final String SEPARATION_TAIL_PATTERN = "2";
	private static final String SEPARATION_FILE_RETURN_CODE = "3";
	// フィルタの説明
	private static final String MONITER_INTERNAL_FILTER_DESC = "For SDML Monitoring Log";
	// フィルタのメッセージ
	private static final String MONITER_INTERNAL_FILTER_MSG = "#[LOG_LINE]";

	/**
	 * 監視設定作成
	 * 
	 * @param validInfo
	 * @param controlSetting
	 * @param dataMap
	 * @param facilityId
	 * @return
	 * @throws HinemosUnknown
	 */
	public static MonitorInfo createMonitorInfo(AutoMonitorValidInfo validInfo, SdmlControlSettingInfo controlSetting,
			Map<String, SdmlInitializeData> dataMap, String facilityId) throws HinemosUnknown {
		return setMonitorInfo(null, validInfo, controlSetting, facilityId, dataMap);
	}

	/**
	 * 監視設定更新
	 * 
	 * @param validInfo
	 * @param controlSetting
	 * @param dataMap
	 * @param monInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	public static MonitorInfo updateMonitorInfo(AutoMonitorValidInfo validInfo, SdmlControlSettingInfo controlSetting,
			Map<String, SdmlInitializeData> dataMap, MonitorInfo monInfo) throws HinemosUnknown {
		return setMonitorInfo(monInfo, validInfo, controlSetting, null, dataMap);
	}

	/**
	 * 監視設定
	 */
	private static MonitorInfo setMonitorInfo(MonitorInfo srcMonInfo, AutoMonitorValidInfo validInfo,
			SdmlControlSettingInfo controlSetting, String facilityId, Map<String, SdmlInitializeData> dataMap)
			throws HinemosUnknown {
		// 新規作成かどうか（false = update)
		boolean create = (srcMonInfo == null);
		MonitorInfo monInfo = new MonitorInfo();
		try {
			if (create) {
				// 新規作成時
				monInfo.setMonitorId(validInfo.getMonitorId());
				monInfo.setOwnerRoleId(controlSetting.getOwnerRoleId());
				monInfo.setFacilityId(facilityId);
				monInfo.setSdmlMonitorTypeId(validInfo.getSdmlMonitorType().getId());
				// 必ずfalse
				monInfo.setMonitorFlg(false);
				monInfo.setCollectorFlg(false);

				// 将来予測と変化量は本体側も文字列監視にもデフォルト値を設定しているので同様に設定する
				// 将来予測（デフォルト値を設定）
				monInfo.setPredictionFlg(false);
				monInfo.setPredictionMethod(MonitorPredictionMethod.DEFALUT);
				monInfo.setPredictionAnalysysRange(60);
				monInfo.setPredictionTarget(60);
				monInfo.setPredictionApplication("");
				// 変化量（デフォルト値を設定）
				monInfo.setChangeFlg(false);
				monInfo.setChangeAnalysysRange(60);
				monInfo.setChangeApplication("");
			} else {
				// 更新時
				monInfo.setMonitorId(srcMonInfo.getMonitorId());
				monInfo.setOwnerRoleId(srcMonInfo.getOwnerRoleId());
				monInfo.setFacilityId(srcMonInfo.getFacilityId());
				monInfo.setSdmlMonitorTypeId(srcMonInfo.getSdmlMonitorTypeId());
				// 更新しない
				monInfo.setMonitorFlg(srcMonInfo.getMonitorFlg());
				monInfo.setCollectorFlg(srcMonInfo.getCollectorFlg());
				// 将来予測
				monInfo.setPredictionFlg(srcMonInfo.getPredictionFlg());
				monInfo.setPredictionMethod(srcMonInfo.getPredictionMethod());
				monInfo.setPredictionAnalysysRange(srcMonInfo.getPredictionAnalysysRange());
				monInfo.setPredictionTarget(srcMonInfo.getPredictionTarget());
				monInfo.setPredictionApplication(srcMonInfo.getPredictionApplication());
				// 変化量
				monInfo.setChangeFlg(srcMonInfo.getChangeFlg());
				monInfo.setChangeAnalysysRange(srcMonInfo.getChangeAnalysysRange());
				monInfo.setChangeApplication(srcMonInfo.getChangeApplication());
			}

			monInfo.setApplication(controlSetting.getApplication());
			monInfo.setCalendarId(controlSetting.getAutoMonitorCalendarId());

			switch (validInfo.getSdmlMonitorType()) {
			case PROCESS:
				setMonitorProcess(monInfo, srcMonInfo, dataMap, create);
				break;
			case LOG_APPLICATION:
				setMonitorLogApplication(monInfo, srcMonInfo, dataMap, create);
				break;
			case INTERNAL_DEADLOCK:
				setMonitorIntDeadlock(monInfo, srcMonInfo, dataMap, create);
				break;
			case INTERNAL_HEAP_REMAINING:
				setMonitorIntHeapRemaining(monInfo, srcMonInfo, dataMap, create);
				break;
			case INTERNAL_GC_COUNT:
				setMonitorIntGcCount(monInfo, srcMonInfo, dataMap, create, validInfo.getNumber());
				break;
			case INTERNAL_CPU_USAGE:
				setMonitorIntCpuUsage(monInfo, srcMonInfo, dataMap, create);
				break;

			default:
				// 到達しない
				break;
			}

			// 通知
			String notifyGroupId = NotifyGroupIdGenerator.generate(monInfo);
			monInfo.setNotifyGroupId(notifyGroupId);

			List<NotifyRelationInfo> originalRelationList = null;
			boolean isIndivisual = false;
			// 先に個別設定を確認する
			for (SdmlMonitorNotifyRelation monitorIndivisualRelation : controlSetting
					.getSdmlMonitorNotifyRelationList()) {
				if (monitorIndivisualRelation.getSdmlMonitorTypeId().equals(validInfo.getSdmlMonitorType().getId())) {
					originalRelationList = monitorIndivisualRelation.getNotifyRelationList();
					isIndivisual = true;
					break;
				}
			}
			// 個別設定がない場合は共通設定を反映する
			if (!isIndivisual) {
				originalRelationList = controlSetting.getAutoMonitorCommonNotifyRelationList();
			}
			if (originalRelationList != null && !originalRelationList.isEmpty()) {
				List<NotifyRelationInfo> newRelationList = new ArrayList<>();
				for (NotifyRelationInfo originalRelation : originalRelationList) {
					NotifyRelationInfo newRelation = new NotifyRelationInfo(notifyGroupId,
							originalRelation.getNotifyId());
					newRelation.setNotifyType(originalRelation.getNotifyType());
					newRelationList.add(newRelation);
				}
				monInfo.setNotifyRelationList(newRelationList);
			}
		} catch (NullPointerException e) {
			// Keyが存在しない場合に発生し得るが、事前にチェックしているため通常到達しない
			logger.error("setMonitorInfo() :" + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (NumberFormatException e) {
			// parseIntの失敗で発生する（Hinemosロギング側で型のチェックはするが制御ログを意図的に編集すれば発生の可能性はある）
			logger.warn("setMonitorInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return monInfo;
	}

	/**
	 * プロセス死活監視
	 */
	private static void setMonitorProcess(MonitorInfo monInfo, MonitorInfo srcMonInfo,
			Map<String, SdmlInitializeData> dataMap, boolean create) {
		ProcessCheckInfo procCheckInfo = new ProcessCheckInfo();
		procCheckInfo.relateToMonitorInfo(monInfo);
		if (create) {
			monInfo.setMonitorType(MonitorTypeConstant.TYPE_NUMERIC);
			monInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_PROCESS);
			monInfo.setFailurePriority(PriorityConstant.TYPE_UNKNOWN);
			procCheckInfo.setCaseSensitivityFlg(false);

			// 収集値（デフォルト値を設定）
			// 通常はクライアントでデフォルト値を設定しているため修正する際は齟齬が発生しないよう注意すること
			monInfo.setItemName(Messages.getString("PROCESS_NUMBER"));
			monInfo.setMeasure(Messages.getString("PROCESS_MEASURE"));
			// 閾値
			for (int priority : THRESHOLD_PRIORITIES) {
				MonitorNumericValueInfo numericValueInfo = new MonitorNumericValueInfo(monInfo,
						MonitorNumericType.TYPE_BASIC.getType(), priority);
				if (priority == PriorityConstant.TYPE_INFO) {
					String[] values = dataMap.get(InitializeKeyEnum.PrcThresholdInfo.name()).getValue()
							.split(THRESHOLD_NUM_DELIMITER, 2);
					numericValueInfo.setThresholdLowerLimit(Double.parseDouble(values[0]));
					numericValueInfo.setThresholdUpperLimit(Double.parseDouble(values[1]));
				} else if (priority == PriorityConstant.TYPE_WARNING) {
					String[] values = dataMap.get(InitializeKeyEnum.PrcThresholdWarn.name()).getValue()
							.split(THRESHOLD_NUM_DELIMITER, 2);
					numericValueInfo.setThresholdLowerLimit(Double.parseDouble(values[0]));
					numericValueInfo.setThresholdUpperLimit(Double.parseDouble(values[1]));
				} else {
					numericValueInfo.setThresholdLowerLimit(0.0);
					numericValueInfo.setThresholdUpperLimit(0.0);
				}
				// 監視設定に紐づけ
				numericValueInfo.relateToMonitorInfo(monInfo);

				// 変化量（デフォルト値を設定）
				numericValueInfo = new MonitorNumericValueInfo(monInfo, MonitorNumericType.TYPE_CHANGE.getType(),
						priority);
				if (priority == PriorityConstant.TYPE_INFO) {
					numericValueInfo.setThresholdLowerLimit(-1.0);
					numericValueInfo.setThresholdUpperLimit(1.0);
				} else if (priority == PriorityConstant.TYPE_WARNING) {
					numericValueInfo.setThresholdLowerLimit(-2.0);
					numericValueInfo.setThresholdUpperLimit(2.0);
				} else {
					numericValueInfo.setThresholdLowerLimit(0.0);
					numericValueInfo.setThresholdUpperLimit(0.0);
				}
				// 監視設定に紐づけ
				numericValueInfo.relateToMonitorInfo(monInfo);
			}
		} else {
			monInfo.setMonitorType(srcMonInfo.getMonitorType());
			monInfo.setMonitorTypeId(srcMonInfo.getMonitorTypeId());
			monInfo.setFailurePriority(srcMonInfo.getFailurePriority());
			procCheckInfo.setCaseSensitivityFlg(srcMonInfo.getProcessCheckInfo().getCaseSensitivityFlg());
			monInfo.setItemName(srcMonInfo.getItemName());
			monInfo.setMeasure(srcMonInfo.getMeasure());

			for (MonitorNumericValueInfo srcNumInfo : srcMonInfo.getNumericValueInfo()) {
				MonitorNumericValueInfo numericValueInfo = new MonitorNumericValueInfo(monInfo,
						srcNumInfo.getMonitorNumericType(), srcNumInfo.getPriority());
				if (srcNumInfo.getMonitorNumericType().equals(MonitorNumericType.TYPE_BASIC.getType())
						&& srcNumInfo.getPriority().equals(PriorityConstant.TYPE_INFO)) {
					String[] values = dataMap.get(InitializeKeyEnum.PrcThresholdInfo.name()).getValue()
							.split(THRESHOLD_NUM_DELIMITER, 2);
					numericValueInfo.setThresholdLowerLimit(Double.parseDouble(values[0]));
					numericValueInfo.setThresholdUpperLimit(Double.parseDouble(values[1]));

				} else if (srcNumInfo.getMonitorNumericType().equals(MonitorNumericType.TYPE_BASIC.getType())
						&& srcNumInfo.getPriority().equals(PriorityConstant.TYPE_WARNING)) {
					String[] values = dataMap.get(InitializeKeyEnum.PrcThresholdWarn.name()).getValue()
							.split(THRESHOLD_NUM_DELIMITER, 2);
					numericValueInfo.setThresholdLowerLimit(Double.parseDouble(values[0]));
					numericValueInfo.setThresholdUpperLimit(Double.parseDouble(values[1]));

				} else {
					numericValueInfo.setThresholdLowerLimit(srcNumInfo.getThresholdLowerLimit());
					numericValueInfo.setThresholdUpperLimit(srcNumInfo.getThresholdUpperLimit());
				}
				// 監視設定に紐づけ
				numericValueInfo.relateToMonitorInfo(monInfo);
			}
		}
		// 作成・更新共通
		monInfo.setDescription(dataMap.get(InitializeKeyEnum.PrcDescription.name()).getValue());
		monInfo.setRunInterval(Integer.parseInt(dataMap.get(InitializeKeyEnum.PrcInterval.name()).getValue()));

		monInfo.getProcessCheckInfo().setCommand(dataMap.get(InitializeKeyEnum.PrcCommand.name()).getValue());
		monInfo.getProcessCheckInfo().setParam(dataMap.get(InitializeKeyEnum.PrcArgument.name()).getValue());
	}

	/**
	 * アプリケーションログ監視
	 */
	private static void setMonitorLogApplication(MonitorInfo monInfo, MonitorInfo srcMonInfo,
			Map<String, SdmlInitializeData> dataMap, boolean create) {
		monInfo.setDescription(dataMap.get(InitializeKeyEnum.LogAppDescription.name()).getValue());

		int logAppFilterCount = Integer.parseInt(dataMap.get(InitializeKeyEnum.LogAppFilterNCount.name()).getValue());
		for (int i = 1; i <= logAppFilterCount; i++) {
			MonitorStringValueInfo stringValueInfo = new MonitorStringValueInfo(monInfo.getMonitorId(), i);
			stringValueInfo.setDescription(dataMap.get(InitializeKeyEnum.LogAppFilterDescription.name(i)).getValue());
			stringValueInfo.setPattern(dataMap.get(InitializeKeyEnum.LogAppFilterPattern.name(i)).getValue());
			stringValueInfo.setProcessType(
					Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.LogAppFilterDoProcess.name(i)).getValue()));
			stringValueInfo.setCaseSensitivityFlg(Boolean
					.parseBoolean(dataMap.get(InitializeKeyEnum.LogAppFilterCaseSensitivity.name(i)).getValue()));
			stringValueInfo.setPriority(
					Integer.parseInt(dataMap.get(InitializeKeyEnum.LogAppFilterPriority.name(i)).getValue()));
			stringValueInfo.setMessage(dataMap.get(InitializeKeyEnum.LogAppFilterMessage.name(i)).getValue());
			// 常にtrue
			stringValueInfo.setValidFlg(true);
			stringValueInfo.relateToMonitorInfo(monInfo);
		}
		setMonitorLogfileCommon(monInfo, srcMonInfo, dataMap, create);
	}

	/**
	 * デッドロック監視
	 */
	private static void setMonitorIntDeadlock(MonitorInfo monInfo, MonitorInfo srcMonInfo,
			Map<String, SdmlInitializeData> dataMap, boolean create) {
		monInfo.setDescription(dataMap.get(InitializeKeyEnum.IntDlkDescription.name()).getValue());

		MonitorStringValueInfo stringValueInfo = new MonitorStringValueInfo(monInfo.getMonitorId(), 1);
		stringValueInfo.setDescription(MONITER_INTERNAL_FILTER_DESC);
		stringValueInfo.setPattern(MonitoringLogUtil.getFilterPatternIntDeadlock());
		stringValueInfo.setProcessType(true);
		stringValueInfo.setCaseSensitivityFlg(false);
		stringValueInfo.setPriority(Integer.parseInt(dataMap.get(InitializeKeyEnum.IntDlkPriority.name()).getValue()));
		stringValueInfo.setMessage(MONITER_INTERNAL_FILTER_MSG);
		stringValueInfo.setValidFlg(true);
		stringValueInfo.relateToMonitorInfo(monInfo);

		setMonitorLogfileCommon(monInfo, srcMonInfo, dataMap, create);
	}

	/**
	 * ヒープ未使用量監視
	 */
	private static void setMonitorIntHeapRemaining(MonitorInfo monInfo, MonitorInfo srcMonInfo,
			Map<String, SdmlInitializeData> dataMap, boolean create) {
		monInfo.setDescription(dataMap.get(InitializeKeyEnum.IntHprDescription.name()).getValue());

		MonitorStringValueInfo stringValueInfo = new MonitorStringValueInfo(monInfo.getMonitorId(), 1);
		stringValueInfo.setDescription(MONITER_INTERNAL_FILTER_DESC);
		stringValueInfo.setPattern(MonitoringLogUtil.getFilterPatternIntHeapRemaining());
		stringValueInfo.setProcessType(true);
		stringValueInfo.setCaseSensitivityFlg(false);
		stringValueInfo.setPriority(Integer.parseInt(dataMap.get(InitializeKeyEnum.IntHprPriority.name()).getValue()));
		stringValueInfo.setMessage(MONITER_INTERNAL_FILTER_MSG);
		stringValueInfo.setValidFlg(true);
		stringValueInfo.relateToMonitorInfo(monInfo);

		setMonitorLogfileCommon(monInfo, srcMonInfo, dataMap, create);
	}

	/**
	 * GC発生頻度監視
	 */
	private static void setMonitorIntGcCount(MonitorInfo monInfo, MonitorInfo srcMonInfo,
			Map<String, SdmlInitializeData> dataMap, boolean create, int i) {
		monInfo.setDescription(dataMap.get(InitializeKeyEnum.IntGccDescription.name(i)).getValue());

		MonitorStringValueInfo stringValueInfo = new MonitorStringValueInfo(monInfo.getMonitorId(), 1);
		stringValueInfo.setDescription(MONITER_INTERNAL_FILTER_DESC);
		stringValueInfo.setPattern(MonitoringLogUtil
				.getFilterPatternIntGcCount(dataMap.get(InitializeKeyEnum.IntGccMethod.name(i)).getValue()));
		stringValueInfo.setProcessType(true);
		stringValueInfo.setCaseSensitivityFlg(false);
		stringValueInfo.setPriority(Integer.parseInt(dataMap.get(InitializeKeyEnum.IntGccPriority.name(i)).getValue()));
		stringValueInfo.setMessage(MONITER_INTERNAL_FILTER_MSG);
		stringValueInfo.setValidFlg(true);
		stringValueInfo.relateToMonitorInfo(monInfo);

		setMonitorLogfileCommon(monInfo, srcMonInfo, dataMap, create);
	}

	/**
	 * CPU使用率監視
	 */
	private static void setMonitorIntCpuUsage(MonitorInfo monInfo, MonitorInfo srcMonInfo,
			Map<String, SdmlInitializeData> dataMap, boolean create) {
		monInfo.setDescription(dataMap.get(InitializeKeyEnum.IntCpuDescription.name()).getValue());

		MonitorStringValueInfo stringValueInfo = new MonitorStringValueInfo(monInfo.getMonitorId(), 1);
		stringValueInfo.setDescription(MONITER_INTERNAL_FILTER_DESC);
		stringValueInfo.setPattern(MonitoringLogUtil.getFilterPatternIntCpuUsage());
		stringValueInfo.setProcessType(true);
		stringValueInfo.setCaseSensitivityFlg(false);
		stringValueInfo.setPriority(Integer.parseInt(dataMap.get(InitializeKeyEnum.IntCpuPriority.name()).getValue()));
		stringValueInfo.setMessage(MONITER_INTERNAL_FILTER_MSG);
		stringValueInfo.setValidFlg(true);
		stringValueInfo.relateToMonitorInfo(monInfo);

		setMonitorLogfileCommon(monInfo, srcMonInfo, dataMap, create);
	}

	/**
	 * SDML監視ログを対象としたログファイル監視の共通設定
	 */
	private static void setMonitorLogfileCommon(MonitorInfo monInfo, MonitorInfo srcMonInfo,
			Map<String, SdmlInitializeData> dataMap, boolean create) {
		LogfileCheckInfo checkInfo = new LogfileCheckInfo();
		checkInfo.relateToMonitorInfo(monInfo);
		if (create) {
			monInfo.setMonitorType(MonitorTypeConstant.TYPE_STRING);
			monInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_LOGFILE);
			monInfo.setFailurePriority(PriorityConstant.TYPE_UNKNOWN);
			// 間隔は0を入れておく
			monInfo.setRunInterval(0);

			// 文字コードは固定
			checkInfo.setFileEncoding(ENCODE_UTF8);
		} else {
			monInfo.setMonitorType(srcMonInfo.getMonitorType());
			monInfo.setMonitorTypeId(srcMonInfo.getMonitorTypeId());
			monInfo.setFailurePriority(srcMonInfo.getFailurePriority());
			monInfo.setRunInterval(srcMonInfo.getRunInterval());

			checkInfo.setFileEncoding(srcMonInfo.getLogfileCheckInfo().getFileEncoding());
		}

		checkInfo.setDirectory(dataMap.get(InitializeKeyEnum.MonLogDirectory.name()).getValue());
		checkInfo.setFileName(dataMap.get(InitializeKeyEnum.MonLogFileName.name()).getValue());
		// ファイル区切り種別
		String separationType = dataMap.get(InitializeKeyEnum.MonLogSeparationType.name()).getValue();
		if (separationType.equals(SEPARATION_HEAD_PATTERN)) {
			checkInfo.setPatternHead(dataMap.get(InitializeKeyEnum.MonLogSeparationValue.name()).getValue());
			checkInfo.setPatternTail("");
			checkInfo.setFileReturnCode(LogfileLineSeparatorConstant.LF);
		} else if (separationType.equals(SEPARATION_TAIL_PATTERN)) {
			checkInfo.setPatternHead("");
			checkInfo.setPatternTail(dataMap.get(InitializeKeyEnum.MonLogSeparationValue.name()).getValue());
			checkInfo.setFileReturnCode(LogfileLineSeparatorConstant.LF);
		} else if (separationType.equals(SEPARATION_FILE_RETURN_CODE)) {
			checkInfo.setPatternHead("");
			checkInfo.setPatternTail("");
			checkInfo.setFileReturnCode(dataMap.get(InitializeKeyEnum.MonLogSeparationValue.name()).getValue());
		}
		checkInfo.setMaxBytes(Integer.parseInt(dataMap.get(InitializeKeyEnum.MonLogMaxBytes.name()).getValue()));
	}

	/**
	 * 自動作成された監視設定にオブジェクト権限を設定する
	 * 
	 * @param controlSetting
	 * @param monInfo
	 * @throws PrivilegeDuplicate
	 * @throws UsedObjectPrivilege
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void setObjectPlivilege(SdmlControlSettingInfo controlSetting, MonitorInfo monInfo)
			throws PrivilegeDuplicate, UsedObjectPrivilege, InvalidSetting, InvalidRole, HinemosUnknown {
		AccessControllerBean controllerBean = new AccessControllerBean();

		ObjectPrivilegeFilterInfo filter = new ObjectPrivilegeFilterInfo();
		filter.setObjectType(HinemosModuleConstant.SDML_CONTROL);
		filter.setObjectId(controlSetting.getApplicationId());
		List<ObjectPrivilegeInfo> srcList = null;
		try {
			srcList = controllerBean.getObjectPrivilegeInfoList(filter);

			if (srcList == null || srcList.isEmpty()) {
				// 取得されなかった場合、監視設定に設定されているオブジェクト権限を確認する
				filter = new ObjectPrivilegeFilterInfo();
				filter.setObjectType(HinemosModuleConstant.MONITOR);
				filter.setObjectId(monInfo.getMonitorId());
				List<ObjectPrivilegeInfo> monObjList = controllerBean.getObjectPrivilegeInfoList(filter);
				if (monObjList == null || monObjList.isEmpty()) {
					// 監視設定に設定されていない場合は終了する（されていた場合は削除する必要があるため続行）
					return;
				}
			}
		} catch (HinemosUnknown e) {
			throw e;
		}

		// SDML制御設定と同様のオブジェクト権限を自動作成される監視設定に設定する
		List<ObjectPrivilegeInfo> destList = new ArrayList<>();
		if (srcList != null) {
			for (ObjectPrivilegeInfo srcInfo : srcList) {
				ObjectPrivilegeInfo destInfo = new ObjectPrivilegeInfo();
				destInfo.setRoleId(srcInfo.getRoleId());
				destInfo.setObjectPrivilege(srcInfo.getObjectPrivilege());
				destList.add(destInfo);
			}
		}
		try {
			new AccessControllerBean().replaceObjectPrivilegeInfo(HinemosModuleConstant.MONITOR, monInfo.getMonitorId(),
					destList);
		} catch (PrivilegeDuplicate | UsedObjectPrivilege | HinemosUnknown | InvalidSetting | InvalidRole e) {
			throw e;
		} catch (JobMasterNotFound e) {
			// 到達しない
			logger.error("setObjectPlivilege() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * 監視もしくは収集が有効なSDML監視種別のリストを作成する
	 * 
	 * @param dataMap
	 * @return
	 * @throws HinemosUnknown
	 */
	public static List<AutoMonitorValidInfo> getEnableMonitorList(Map<String, SdmlInitializeData> dataMap)
			throws HinemosUnknown {
		List<AutoMonitorValidInfo> list = new ArrayList<>();
		try {
			boolean monitorFlg = false;
			boolean collectorFlg = false;
			// プロセス死活監視
			monitorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.PrcMonitor.name()).getValue());
			collectorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.PrcCollect.name()).getValue());
			if (monitorFlg || collectorFlg) {
				list.add(new AutoMonitorValidInfo(SdmlMonitorTypeEnum.PROCESS, monitorFlg, collectorFlg));
			}
			// アプリケーションログ監視
			monitorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.LogAppMonitor.name()).getValue());
			collectorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.LogAppCollect.name()).getValue());
			if (monitorFlg || collectorFlg) {
				list.add(new AutoMonitorValidInfo(SdmlMonitorTypeEnum.LOG_APPLICATION, monitorFlg, collectorFlg));
			}
			// デッドロック監視
			monitorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.IntDlkMonitor.name()).getValue());
			collectorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.IntDlkCollect.name()).getValue());
			if (monitorFlg || collectorFlg) {
				list.add(new AutoMonitorValidInfo(SdmlMonitorTypeEnum.INTERNAL_DEADLOCK, monitorFlg, collectorFlg));
			}
			// ヒープ未使用量監視
			monitorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.IntHprMonitor.name()).getValue());
			collectorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.IntHprCollect.name()).getValue());
			if (monitorFlg || collectorFlg) {
				list.add(new AutoMonitorValidInfo(SdmlMonitorTypeEnum.INTERNAL_HEAP_REMAINING, monitorFlg,
						collectorFlg));
			}
			// GC発生頻度監視
			int count = Integer.parseInt(dataMap.get(InitializeKeyEnum.IntGccNCount.name()).getValue());
			for (int i = 1; i <= count; i++) {
				monitorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.IntGccMonitor.name(i)).getValue());
				collectorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.IntGccCollect.name(i)).getValue());
				String gccMethod = dataMap.get(InitializeKeyEnum.IntGccMethod.name(i)).getValue();
				if (monitorFlg || collectorFlg) {
					list.add(new AutoMonitorValidInfo(SdmlMonitorTypeEnum.INTERNAL_GC_COUNT, monitorFlg, collectorFlg,
							i, gccMethod));
				}
			}
			// CPU使用率監視
			monitorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.IntCpuMonitor.name()).getValue());
			collectorFlg = Boolean.parseBoolean(dataMap.get(InitializeKeyEnum.IntCpuCollect.name()).getValue());
			if (monitorFlg || collectorFlg) {
				list.add(new AutoMonitorValidInfo(SdmlMonitorTypeEnum.INTERNAL_CPU_USAGE, monitorFlg, collectorFlg));
			}
		} catch (NullPointerException e) {
			// 事前にKeyが揃っているか確認しているのでNullになることはないはず
			logger.error("getMonitorValidList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (NumberFormatException e) {
			// parseIntの失敗で発生する（Hinemosロギング側で型のチェックはするが制御ログを意図的に編集すれば発生の可能性はある）
			logger.warn("getMonitorValidList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return list;
	}
}
